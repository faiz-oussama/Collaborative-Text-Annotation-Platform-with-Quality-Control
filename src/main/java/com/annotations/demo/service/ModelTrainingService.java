package com.annotations.demo.service;

import com.annotations.demo.entity.Dataset;
import com.annotations.demo.entity.ModelTraining;
import com.annotations.demo.ml.JavaModelTrainer;
import com.annotations.demo.repository.DatasetRepository;
import com.annotations.demo.repository.ModelTrainingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing model training operations.
 * Provides methods for starting, tracking, and managing model training sessions.
 */
@Service
public class ModelTrainingService {
    private static final Logger logger = LoggerFactory.getLogger(ModelTrainingService.class);
    
    @Autowired
    private ModelTrainingRepository modelTrainingRepository;
    
    @Autowired
    private DatasetRepository datasetRepository;
    
    @Autowired
    private JavaModelTrainer javaModelTrainer;
    
    private final String MODELS_DIRECTORY = "src/main/resources/java_models/";
    
    /**
     * Initialize the service and required directories
     */
    public void initialize() {
        // Create models directory if it doesn't exist
        createDirectoryIfNotExists(MODELS_DIRECTORY);
        
        // Initialize Java model trainer
        javaModelTrainer.initialize();
        
        logger.info("Model training service initialized");
    }
    
    /**
     * Start a new model training session
     */
    public ModelTraining startTraining(Long datasetId, Map<String, String> hyperParameters, String username) {
        logger.info("Starting model training for dataset ID: {} with hyperparameters: {}", datasetId, hyperParameters);
        
        // Check if dataset exists
        Optional<Dataset> datasetOpt = datasetRepository.findById(datasetId);
        if (!datasetOpt.isPresent()) {
            throw new IllegalArgumentException("Dataset not found: " + datasetId);
        }
        
        // Create and save model training record
        ModelTraining modelTraining = new ModelTraining();
        modelTraining.setDatasetId(datasetId);
        modelTraining.setStartTime(LocalDateTime.now());
        modelTraining.setStatus("TRAINING");
        modelTraining.setHyperParameters(convertMapToString(hyperParameters));
        modelTraining.setCreatedBy(username);
        
        // Extract and set hyperparameters as individual fields if available
        if (hyperParameters.containsKey("epochs")) {
            try {
                modelTraining.setEpochCount(Integer.parseInt(hyperParameters.get("epochs")));
            } catch (NumberFormatException e) {
                logger.warn("Invalid epoch count: {}", hyperParameters.get("epochs"));
            }
        }
        
        if (hyperParameters.containsKey("batchSize")) {
            try {
                modelTraining.setBatchSize(Integer.parseInt(hyperParameters.get("batchSize")));
            } catch (NumberFormatException e) {
                logger.warn("Invalid batch size: {}", hyperParameters.get("batchSize"));
            }
        }
        
        if (hyperParameters.containsKey("learningRate")) {
            try {
                modelTraining.setLearningRate(Double.parseDouble(hyperParameters.get("learningRate")));
            } catch (NumberFormatException e) {
                logger.warn("Invalid learning rate: {}", hyperParameters.get("learningRate"));
            }
        }
        
        modelTrainingRepository.save(modelTraining);
        
        // Start the training process asynchronously
        startTrainingAsync(modelTraining, hyperParameters);
        
        return modelTraining;
    }
    
    /**
     * Asynchronously execute the model training
     */
    @Async
    private CompletableFuture<Void> startTrainingAsync(ModelTraining modelTraining, Map<String, String> hyperParameters) {
        try {
            // Call the Java model trainer
            CompletableFuture<Boolean> trainingResult = javaModelTrainer.trainModel(
                    modelTraining.getDatasetId(), hyperParameters);
            
            // Wait for completion
            Boolean success = trainingResult.get();
            
            // Update training record
            modelTraining.setEndTime(LocalDateTime.now());
            modelTraining.setStatus(success ? "COMPLETED" : "FAILED");
            
            if (success) {
                // Set model path (simplified for this implementation)
                String modelPath = MODELS_DIRECTORY + "model_" + modelTraining.getDatasetId() + "_" + 
                                   modelTraining.getId() + ".java_model";
                modelTraining.setModelPath(modelPath);
            } else {
                modelTraining.setErrorMessage("Training failed without specific error");
            }
            
            modelTrainingRepository.save(modelTraining);
            logger.info("Model training completed with status: {}", modelTraining.getStatus());
            
        } catch (Exception e) {
            logger.error("Error during model training: {}", e.getMessage(), e);
            
            // Update training record with error
            modelTraining.setEndTime(LocalDateTime.now());
            modelTraining.setStatus("FAILED");
            modelTraining.setErrorMessage(e.getMessage());
            modelTrainingRepository.save(modelTraining);
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Get all training sessions
     */
    public List<ModelTraining> getAllTrainingSessions() {
        return modelTrainingRepository.findAll();
    }
    
    /**
     * Get training sessions for a specific dataset
     */
    public List<ModelTraining> getTrainingSessionsByDataset(Long datasetId) {
        return modelTrainingRepository.findByDatasetId(datasetId);
    }
    
    /**
     * Get a specific training session by ID
     */
    public Optional<ModelTraining> getTrainingSession(Long id) {
        return modelTrainingRepository.findById(id);
    }
    
    /**
     * Get the latest training session for a dataset
     */
    public ModelTraining getLatestTrainingSession(Long datasetId) {
        return modelTrainingRepository.findFirstByDatasetIdOrderByStartTimeDesc(datasetId);
    }
    
    /**
     * Get the latest successful training session for a dataset
     */
    public ModelTraining getLatestSuccessfulTrainingSession(Long datasetId) {
        return modelTrainingRepository.findFirstByDatasetIdAndStatusOrderByEndTimeDesc(datasetId, "COMPLETED");
    }
    
    /**
     * Cancel a running training session
     */
    public boolean cancelTraining(Long id) {
        Optional<ModelTraining> trainingOpt = modelTrainingRepository.findById(id);
        if (!trainingOpt.isPresent()) {
            return false;
        }
        
        ModelTraining training = trainingOpt.get();
        if (!training.getStatus().equals("TRAINING")) {
            return false; // Can only cancel if still training
        }
        
        // Update status
        training.setStatus("CANCELLED");
        training.setEndTime(LocalDateTime.now());
        modelTrainingRepository.save(training);
        
        // Note: This doesn't actually stop the training process, which would require
        // additional implementation to properly cancel the running thread/process
        
        logger.info("Marked training session as cancelled: {}", id);
        return true;
    }
    
    /**
     * Delete a training session and its model file
     */
    public boolean deleteTraining(Long id) {
        Optional<ModelTraining> trainingOpt = modelTrainingRepository.findById(id);
        if (!trainingOpt.isPresent()) {
            return false;
        }
        
        ModelTraining training = trainingOpt.get();
        
        // Delete model file if it exists
        if (training.getModelPath() != null) {
            File modelFile = new File(training.getModelPath());
            if (modelFile.exists()) {
                boolean deleted = modelFile.delete();
                if (!deleted) {
                    logger.warn("Failed to delete model file: {}", training.getModelPath());
                }
            }
        }
        
        // Delete training record
        modelTrainingRepository.delete(training);
        logger.info("Deleted training session: {}", id);
        
        return true;
    }
    
    /**
     * Get training statistics
     */
    public Map<String, Object> getTrainingStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // Count by status
        List<Object[]> statusCounts = modelTrainingRepository.countByStatus();
        Map<String, Long> countsByStatus = new HashMap<>();
        for (Object[] result : statusCounts) {
            String status = (String) result[0];
            Long count = (Long) result[1];
            countsByStatus.put(status, count);
        }
        statistics.put("countsByStatus", countsByStatus);
        
        // Average training time
        Double avgTime = modelTrainingRepository.getAverageTrainingTimeInSeconds();
        statistics.put("averageTrainingTimeSeconds", avgTime != null ? avgTime : 0);
        
        return statistics;
    }
    
    // Helper methods
    
    private void createDirectoryIfNotExists(String dirPath) {
        File directory = new File(dirPath);
        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            if (created) {
                logger.info("Created directory: {}", dirPath);
            } else {
                logger.warn("Failed to create directory: {}", dirPath);
            }
        }
    }
    
    private String convertMapToString(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }
}
