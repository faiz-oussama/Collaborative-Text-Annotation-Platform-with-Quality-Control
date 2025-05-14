package com.annotations.demo.service;

import com.annotations.demo.entity.ModelEvaluation;
import com.annotations.demo.entity.ModelTraining;
import com.annotations.demo.ml.JavaModelEvaluator;
import com.annotations.demo.repository.ModelEvaluationRepository;
import com.annotations.demo.repository.ModelTrainingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Service for managing model evaluation operations.
 * Provides methods for evaluating trained models and managing evaluation results.
 */
@Service
public class ModelEvaluationService {
    private static final Logger logger = LoggerFactory.getLogger(ModelEvaluationService.class);
    
    @Autowired
    private ModelEvaluationRepository modelEvaluationRepository;
    
    @Autowired
    private ModelTrainingRepository modelTrainingRepository;
    
    @Autowired
    private JavaModelEvaluator javaModelEvaluator;
    
    private final String EVALUATION_DIRECTORY = "src/main/resources/java_evaluations/";
    private final String TEST_DATA_DIRECTORY = "src/main/resources/java_test_data/";
    
    /**
     * Initialize the service
     */
    public void initialize() {
        // Initialize Java model evaluator
        javaModelEvaluator.initialize();
        logger.info("Model evaluation service initialized");
    }
    
    /**
     * Start a model evaluation for a trained model
     */
    public ModelEvaluation startEvaluation(Long trainingId, String testDataPath, String username) {
        logger.info("Starting model evaluation for training ID: {} with test data: {}", trainingId, testDataPath);
        
        // Check if training exists and is completed
        Optional<ModelTraining> trainingOpt = modelTrainingRepository.findById(trainingId);
        if (!trainingOpt.isPresent()) {
            throw new IllegalArgumentException("Model training not found: " + trainingId);
        }
        
        ModelTraining training = trainingOpt.get();
        if (!training.getStatus().equals("COMPLETED")) {
            throw new IllegalArgumentException("Model training is not completed: " + trainingId);
        }
        
        // Check if model file exists
        String modelPath = training.getModelPath();
        if (modelPath == null || modelPath.isEmpty()) {
            throw new IllegalArgumentException("Model path is not available for training: " + trainingId);
        }
        
        // Create and save model evaluation record
        ModelEvaluation evaluation = new ModelEvaluation();
        evaluation.setModelTrainingId(trainingId);
        evaluation.setTestDatasetPath(testDataPath);
        evaluation.setEvaluationTime(LocalDateTime.now());
        evaluation.setCreatedBy(username);
        modelEvaluationRepository.save(evaluation);
        
        // Start the evaluation process asynchronously
        startEvaluationAsync(evaluation, modelPath, testDataPath);
        
        return evaluation;
    }
    
    /**
     * Asynchronously execute the model evaluation
     */
    @Async
    private CompletableFuture<Void> startEvaluationAsync(ModelEvaluation evaluation, String modelPath, String testDataPath) {
        try {
            // Call the Java model evaluator
            Map<String, Double> metrics = javaModelEvaluator.evaluateModel(modelPath, testDataPath);
            
            // Update evaluation record with metrics
            evaluation.setAccuracy(metrics.getOrDefault("accuracy", 0.0));
            evaluation.setPrecision(metrics.getOrDefault("precision", 0.0));
            evaluation.setRecall(metrics.getOrDefault("recall", 0.0));
            evaluation.setF1Score(metrics.getOrDefault("f1_score", 0.0));
            
            // Store all metrics
            evaluation.setMetricsFromMap(metrics);
            
            // Set evaluation log path
            String evaluationLogPath = EVALUATION_DIRECTORY + "evaluation_" + evaluation.getId() + ".log";
            evaluation.setEvaluationLogPath(evaluationLogPath);
            
            // Create a simple confusion matrix for demonstration
            StringBuilder confusionMatrix = new StringBuilder();
            confusionMatrix.append("[[85, 10, 5], [12, 78, 10], [8, 15, 77]]");
            evaluation.setConfusionMatrix(confusionMatrix.toString());
            
            modelEvaluationRepository.save(evaluation);
            logger.info("Model evaluation completed successfully for ID: {}", evaluation.getId());
            
        } catch (Exception e) {
            logger.error("Error during model evaluation: {}", e.getMessage(), e);
            
            // Update evaluation record with error
            // For simplicity, we don't have an error field in the entity,
            // but in a real implementation, we would store the error
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    /**
     * Get all evaluation results
     */
    public List<ModelEvaluation> getAllEvaluations() {
        return modelEvaluationRepository.findAll();
    }
    
    /**
     * Get evaluation results for a specific training
     */
    public List<ModelEvaluation> getEvaluationsByTraining(Long trainingId) {
        return modelEvaluationRepository.findByModelTrainingId(trainingId);
    }
    
    /**
     * Get the latest evaluation for a training
     */
    public ModelEvaluation getLatestEvaluation(Long trainingId) {
        return modelEvaluationRepository.findFirstByModelTrainingIdOrderByEvaluationTimeDesc(trainingId);
    }
    
    /**
     * Get a specific evaluation by ID
     */
    public Optional<ModelEvaluation> getEvaluation(Long id) {
        return modelEvaluationRepository.findById(id);
    }
    
    /**
     * Delete an evaluation
     */
    public boolean deleteEvaluation(Long id) {
        Optional<ModelEvaluation> evaluationOpt = modelEvaluationRepository.findById(id);
        if (!evaluationOpt.isPresent()) {
            return false;
        }
        
        modelEvaluationRepository.delete(evaluationOpt.get());
        logger.info("Deleted evaluation: {}", id);
        
        return true;
    }
    
    /**
     * Get top-performing models based on accuracy
     */
    public List<ModelEvaluation> getTopPerformingModels() {
        return modelEvaluationRepository.findTop5ByOrderByAccuracyDesc();
    }
    
    /**
     * Get top-performing models based on F1 score
     */
    public List<ModelEvaluation> getTopPerformingModelsByF1() {
        return modelEvaluationRepository.findTop5ByOrderByF1ScoreDesc();
    }
    
    /**
     * Get evaluations with balanced precision and recall
     */
    public List<ModelEvaluation> getBalancedModels(Double threshold) {
        return modelEvaluationRepository.findWithBalancedPrecisionAndRecall(threshold);
    }
    
    /**
     * Get evaluation statistics
     */
    public Map<String, Object> getEvaluationStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // Get average metrics
        Object[] avgMetrics = modelEvaluationRepository.getAverageMetrics();
        if (avgMetrics != null && avgMetrics.length >= 4) {
            Map<String, Double> metrics = new HashMap<>();
            metrics.put("avgAccuracy", (Double) avgMetrics[0]);
            metrics.put("avgPrecision", (Double) avgMetrics[1]);
            metrics.put("avgRecall", (Double) avgMetrics[2]);
            metrics.put("avgF1Score", (Double) avgMetrics[3]);
            statistics.put("averageMetrics", metrics);
        }
        
        // Count by accuracy ranges
        Map<String, Long> accuracyCounts = new HashMap<>();
        accuracyCounts.put("0.0-0.6", modelEvaluationRepository.countByAccuracyBetween(0.0, 0.6));
        accuracyCounts.put("0.6-0.7", modelEvaluationRepository.countByAccuracyBetween(0.6, 0.7));
        accuracyCounts.put("0.7-0.8", modelEvaluationRepository.countByAccuracyBetween(0.7, 0.8));
        accuracyCounts.put("0.8-0.9", modelEvaluationRepository.countByAccuracyBetween(0.8, 0.9));
        accuracyCounts.put("0.9-1.0", modelEvaluationRepository.countByAccuracyBetween(0.9, 1.0));
        statistics.put("accuracyCounts", accuracyCounts);
        
        return statistics;
    }
    
    /**
     * Compare multiple model evaluations
     */
    public Map<String, Object> compareEvaluations(List<Long> evaluationIds) {
        Map<String, Object> comparison = new HashMap<>();
        
        List<ModelEvaluation> evaluations = modelEvaluationRepository.findAllById(evaluationIds);
        
        // Extract key metrics for each evaluation
        Map<Long, Map<String, Double>> metricsById = new HashMap<>();
        for (ModelEvaluation evaluation : evaluations) {
            Map<String, Double> metrics = new HashMap<>();
            metrics.put("accuracy", evaluation.getAccuracy());
            metrics.put("precision", evaluation.getPrecision());
            metrics.put("recall", evaluation.getRecall());
            metrics.put("f1_score", evaluation.getF1Score());
            metricsById.put(evaluation.getId(), metrics);
        }
        
        comparison.put("metrics", metricsById);
        
        // Find best for each metric
        ModelEvaluation bestAccuracy = evaluations.stream()
                .max((e1, e2) -> Double.compare(e1.getAccuracy(), e2.getAccuracy()))
                .orElse(null);
        
        ModelEvaluation bestF1 = evaluations.stream()
                .max((e1, e2) -> Double.compare(e1.getF1Score(), e2.getF1Score()))
                .orElse(null);
        
        Map<String, Long> best = new HashMap<>();
        if (bestAccuracy != null) best.put("accuracy", bestAccuracy.getId());
        if (bestF1 != null) best.put("f1_score", bestF1.getId());
        
        comparison.put("best", best);
        
        return comparison;
    }
}
