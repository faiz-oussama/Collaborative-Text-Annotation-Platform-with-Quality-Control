package com.annotations.demo.controller;

import com.annotations.demo.ml.JavaModelEvaluator;
import com.annotations.demo.ml.JavaModelTrainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * REST Controller for Java-based model training and evaluation.
 * Provides endpoints for training models, evaluating their performance,
 * and retrieving training history.
 */
@RestController
@RequestMapping("/api/model")
public class JavaModelController {
    private static final Logger logger = LoggerFactory.getLogger(JavaModelController.class);

    @Autowired
    private JavaModelTrainer modelTrainer;

    @Autowired
    private JavaModelEvaluator modelEvaluator;

    private final Map<Long, String> trainingStatus = new HashMap<>();
    private final Map<Long, String> modelPaths = new HashMap<>();
    private final Map<Long, Map<String, Double>> evaluationResults = new HashMap<>();

    /**
     * Initialize the model training and evaluation environment
     */
    @PostMapping("/initialize")
    public ResponseEntity<String> initialize() {
        try {
            modelTrainer.initialize();
            modelEvaluator.initialize();
            return ResponseEntity.ok("Model environment initialized successfully");
        } catch (Exception e) {
            logger.error("Error initializing model environment: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    /**
     * Start training a model with the specified dataset and hyperparameters
     */
    @PostMapping("/train")
    public ResponseEntity<Map<String, Object>> trainModel(
            @RequestParam Long datasetId,
            @RequestBody Map<String, String> hyperParameters) {
        
        logger.info("Received request to train model for dataset ID: {} with hyperparameters: {}", 
                datasetId, hyperParameters);
        
        // Initialize response
        Map<String, Object> response = new HashMap<>();
        response.put("datasetId", datasetId);
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", "STARTED");
        
        // Set initial status
        trainingStatus.put(datasetId, "TRAINING");
        
        // Start training asynchronously
        CompletableFuture<Boolean> future = modelTrainer.trainModel(datasetId, hyperParameters);
        
        // Handle completion
        future.thenAccept(success -> {
            if (success) {
                trainingStatus.put(datasetId, "COMPLETED");
                
                // Generate a model path (simulated)
                String modelPath = "src/main/resources/java_models/model_" + datasetId + "_" + 
                                   LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + 
                                   ".java_model";
                modelPaths.put(datasetId, modelPath);
                
                logger.info("Model training completed successfully for dataset ID: {}", datasetId);
            } else {
                trainingStatus.put(datasetId, "FAILED");
                logger.error("Model training failed for dataset ID: {}", datasetId);
            }
        });
        
        response.put("trainingId", datasetId);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get the status of a training job
     */
    @GetMapping("/status/{datasetId}")
    public ResponseEntity<Map<String, Object>> getTrainingStatus(@PathVariable Long datasetId) {
        Map<String, Object> response = new HashMap<>();
        response.put("datasetId", datasetId);
        
        String status = trainingStatus.getOrDefault(datasetId, "NOT_FOUND");
        response.put("status", status);
        
        if (status.equals("COMPLETED")) {
            response.put("modelPath", modelPaths.getOrDefault(datasetId, ""));
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Evaluate a trained model
     */
    @PostMapping("/evaluate/{datasetId}")
    public ResponseEntity<Map<String, Object>> evaluateModel(
            @PathVariable Long datasetId,
            @RequestParam(required = false) String testDataPath) {
        
        logger.info("Received request to evaluate model for dataset ID: {}", datasetId);
        
        // Initialize response
        Map<String, Object> response = new HashMap<>();
        response.put("datasetId", datasetId);
        
        // Check if training is completed
        String status = trainingStatus.getOrDefault(datasetId, "NOT_FOUND");
        if (!status.equals("COMPLETED")) {
            response.put("status", "ERROR");
            response.put("message", "Model training not completed for this dataset");
            return ResponseEntity.badRequest().body(response);
        }
        
        String modelPath = modelPaths.get(datasetId);
        
        // If test data path not provided, use a default one
        if (testDataPath == null || testDataPath.isEmpty()) {
            testDataPath = "src/main/resources/java_training_data/dataset_" + datasetId + "_test.csv";
        }
        
        try {
            // Evaluate the model
            Map<String, Double> metrics = modelEvaluator.evaluateModel(modelPath, testDataPath);
            
            // Save evaluation results
            evaluationResults.put(datasetId, metrics);
            
            response.put("status", "SUCCESS");
            response.put("metrics", metrics);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error evaluating model: {}", e.getMessage(), e);
            
            response.put("status", "ERROR");
            response.put("message", "Error evaluating model: " + e.getMessage());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Get all available models
     */
    @GetMapping("/models")
    public ResponseEntity<List<Map<String, Object>>> getModels() {
        File modelsDir = new File("src/main/resources/java_models/");
        if (!modelsDir.exists() || !modelsDir.isDirectory()) {
            return ResponseEntity.ok(List.of());
        }
        
        // List all model files
        File[] modelFiles = modelsDir.listFiles((dir, name) -> name.endsWith(".java_model"));
        if (modelFiles == null) {
            return ResponseEntity.ok(List.of());
        }
        
        // Convert to response format
        List<Map<String, Object>> models = java.util.Arrays.stream(modelFiles)
                .map(file -> {
                    Map<String, Object> model = new HashMap<>();
                    model.put("name", file.getName());
                    model.put("path", file.getAbsolutePath());
                    model.put("size", file.length());
                    model.put("lastModified", LocalDateTime.ofInstant(
                            java.time.Instant.ofEpochMilli(file.lastModified()),
                            java.time.ZoneId.systemDefault()));
                    
                    // Try to extract dataset ID from filename
                    String fileName = file.getName();
                    if (fileName.startsWith("model_")) {
                        try {
                            int underscoreIndex = fileName.indexOf('_', 6);
                            if (underscoreIndex > 0) {
                                String datasetIdStr = fileName.substring(6, underscoreIndex);
                                Long datasetId = Long.parseLong(datasetIdStr);
                                model.put("datasetId", datasetId);
                                
                                // Add evaluation metrics if available
                                if (evaluationResults.containsKey(datasetId)) {
                                    model.put("metrics", evaluationResults.get(datasetId));
                                }
                            }
                        } catch (NumberFormatException e) {
                            // Ignore parsing errors
                        }
                    }
                    
                    return model;
                })
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(models);
    }
    
    /**
     * Get evaluation results for a specific model
     */
    @GetMapping("/evaluations/{datasetId}")
    public ResponseEntity<Map<String, Object>> getEvaluationResults(@PathVariable Long datasetId) {
        Map<String, Object> response = new HashMap<>();
        response.put("datasetId", datasetId);
        
        if (evaluationResults.containsKey(datasetId)) {
            response.put("status", "SUCCESS");
            response.put("metrics", evaluationResults.get(datasetId));
        } else {
            response.put("status", "NOT_FOUND");
            response.put("message", "No evaluation results found for this dataset");
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete a trained model
     */
    @DeleteMapping("/models/{datasetId}")
    public ResponseEntity<Map<String, Object>> deleteModel(@PathVariable Long datasetId) {
        Map<String, Object> response = new HashMap<>();
        response.put("datasetId", datasetId);
        
        if (modelPaths.containsKey(datasetId)) {
            String modelPath = modelPaths.get(datasetId);
            File modelFile = new File(modelPath);
            
            if (modelFile.exists() && modelFile.delete()) {
                modelPaths.remove(datasetId);
                trainingStatus.remove(datasetId);
                evaluationResults.remove(datasetId);
                
                response.put("status", "SUCCESS");
                response.put("message", "Model deleted successfully");
            } else {
                response.put("status", "ERROR");
                response.put("message", "Failed to delete model file");
            }
        } else {
            response.put("status", "NOT_FOUND");
            response.put("message", "No model found for this dataset");
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get model training history
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getTrainingHistory() {
        Map<String, Object> response = new HashMap<>();
        response.put("trainingCount", trainingStatus.size());
        
        List<Map<String, Object>> history = trainingStatus.entrySet().stream()
                .map(entry -> {
                    Long datasetId = entry.getKey();
                    String status = entry.getValue();
                    
                    Map<String, Object> item = new HashMap<>();
                    item.put("datasetId", datasetId);
                    item.put("status", status);
                    
                    if (status.equals("COMPLETED") && modelPaths.containsKey(datasetId)) {
                        item.put("modelPath", modelPaths.get(datasetId));
                        
                        if (evaluationResults.containsKey(datasetId)) {
                            item.put("evaluated", true);
                            item.put("metrics", evaluationResults.get(datasetId));
                        } else {
                            item.put("evaluated", false);
                        }
                    }
                    
                    return item;
                })
                .collect(Collectors.toList());
        
        response.put("history", history);
        return ResponseEntity.ok(response);
    }
}
