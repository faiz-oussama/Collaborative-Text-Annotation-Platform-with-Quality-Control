package com.annotations.demo.service;

import com.annotations.demo.entity.CoupleText;
import com.annotations.demo.entity.NliPrediction;
import com.annotations.demo.repository.NliPredictionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class PythonService {
    private static final Logger logger = LoggerFactory.getLogger(PythonService.class);
    private final RestTemplate restTemplate = new RestTemplate();
    
    @Autowired
    private NliPredictionRepository nliPredictionRepository;
    
    @Value("${python.api.base-url:http://localhost:5001}")
    private String pythonApiBaseUrl;
    
    @Value("${python.api.max-retries:3}")
    private int maxRetries;
    
    @Value("${python.api.retry-delay:1000}")
    private long retryDelay;
    
    /**
     * Check if the Python service is available and running
     * @return true if the service is healthy, false otherwise
     */
    public boolean isServiceHealthy() {
        try {
            ResponseEntity<?> response = restTemplate.getForEntity(
                    pythonApiBaseUrl + "/health", Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            logger.error("Health check failed for Python service: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Start the Python service if it's not already running
     * @return true if service started or was already running, false if failed to start
     */
    public boolean startPythonService() {
        if (isServiceHealthy()) {
            logger.info("Python service is already running");
            return true;
        }
        
        try {
            // Get the current working directory
            String currentDir = System.getProperty("user.dir");
            String scriptPath = currentDir + "\\spammers_detection\\start_service.py";
            
            logger.info("Starting Python service with script at: {}", scriptPath);
            
            // Check if the script file exists
            java.io.File scriptFile = new java.io.File(scriptPath);
            if (!scriptFile.exists()) {
                logger.error("Python script not found at: {}", scriptPath);
                return false;
            }
            
            // Try to start the Python service using ProcessBuilder with absolute path
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "python", scriptPath, "start");
            processBuilder.redirectErrorStream(true); // Redirect error stream to output stream
            
            // Set the working directory to the project root
            processBuilder.directory(new java.io.File(currentDir));
            
            // Start the process
            logger.info("Executing command: python {} start", scriptPath);
            Process process = processBuilder.start();
            
            // Capture and log the output
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder output = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            logger.info("Python service output: {}", output.toString());
            
            // Wait for the process to complete with a timeout
            boolean completed = process.waitFor(10, TimeUnit.SECONDS);
            
            if (completed) {
                // Check if service is now healthy
                for (int i = 0; i < 5; i++) {
                    if (isServiceHealthy()) {
                        logger.info("Python service started successfully");
                        return true;
                    }
                    Thread.sleep(1000); // Wait 1 second before checking again
                }
            }
            
            logger.error("Failed to start Python service");
            return false;
        } catch (IOException | InterruptedException e) {
            logger.error("Error starting Python service: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Send text pairs to the Python model for prediction with retry logic
     * @param texts List of text pairs to analyze
     * @return List of predictions from the model
     */
    @Retryable(value = {RestClientException.class, ResourceAccessException.class}, 
               maxAttempts = 3, 
               backoff = @Backoff(delay = 1000))
    public List<NliPrediction> sendToModel(List<CoupleText> texts) {
        if (texts == null || texts.isEmpty()) {
            logger.warn("Empty text list provided to sendToModel");
            return Collections.emptyList();
        }
        
        // Ensure Python service is running
        if (!isServiceHealthy()) {
            logger.warn("Python service is not healthy, attempting to start it");
            if (!startPythonService()) {
                logger.error("Could not start Python service, aborting prediction");
                throw new ServiceUnavailableException("Python prediction service is unavailable");
            }
        }
        
        try {
            // Prepare request payload
            List<Map<String, String>> requestPayload = texts.stream().map(pair -> {
                Map<String, String> m = new HashMap<>();
                m.put("premise", pair.getText_1() != null ? pair.getText_1() : "");
                m.put("hypothesis", pair.getText_2() != null ? pair.getText_2() : "");
                return m;
            }).toList();

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("examples", requestPayload);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            logger.info("Sending {} text pairs to Python model", texts.size());
            ResponseEntity<NliPrediction[]> response = restTemplate.postForEntity(
                    pythonApiBaseUrl + "/predict", request, NliPrediction[].class
            );

            if (response.getBody() == null) {
                logger.error("Received null response from Python service");
                return new ArrayList<>();
            }
            
            logger.info("Received {} predictions from Python model", response.getBody().length);
            List<NliPrediction> predictions = Arrays.asList(response.getBody());
            
            // Save predictions to database
            try {
                logger.info("Saving {} NLI predictions to database", predictions.size());
                nliPredictionRepository.saveAll(predictions);
                logger.info("Successfully saved predictions to database");
            } catch (Exception ex) {
                logger.error("Error saving NLI predictions to database: {}", ex.getMessage());
                // Continue even if saving fails
            }
            
            return predictions;
            
        } catch (RestClientException e) {
            logger.error("Error communicating with Python service: {}", e.getMessage());
            throw e; // Will trigger retry
        } catch (Exception e) {
            logger.error("Unexpected error in sendToModel: {}", e.getMessage());
            throw new RuntimeException("Failed to process model predictions", e);
        }
    }
    
    /**
     * Custom exception for service unavailability
     */
    public static class ServiceUnavailableException extends RuntimeException {
        public ServiceUnavailableException(String message) {
            super(message);
        }
    }
}
