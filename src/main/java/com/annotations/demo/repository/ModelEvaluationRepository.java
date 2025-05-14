package com.annotations.demo.repository;

import com.annotations.demo.entity.ModelEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for ModelEvaluation entity.
 * Provides database operations for model evaluation results.
 */
@Repository
public interface ModelEvaluationRepository extends JpaRepository<ModelEvaluation, Long> {
    
    /**
     * Find all evaluations for a specific model training
     */
    List<ModelEvaluation> findByModelTrainingId(Long modelTrainingId);
    
    /**
     * Find the most recent evaluation for a model training
     */
    ModelEvaluation findFirstByModelTrainingIdOrderByEvaluationTimeDesc(Long modelTrainingId);
    
    /**
     * Find evaluations with accuracy above threshold
     */
    List<ModelEvaluation> findByAccuracyGreaterThanEqual(Double threshold);
    
    /**
     * Find evaluations with F1 score above threshold
     */
    List<ModelEvaluation> findByF1ScoreGreaterThanEqual(Double threshold);
    
    /**
     * Find evaluations within a date range
     */
    @Query("SELECT me FROM ModelEvaluation me WHERE me.evaluationTime >= :startDate AND me.evaluationTime <= :endDate")
    List<ModelEvaluation> findByDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    /**
     * Find evaluations by creator
     */
    List<ModelEvaluation> findByCreatedBy(String username);
    
    /**
     * Get average metrics across all evaluations
     */
    @Query("SELECT AVG(me.accuracy), AVG(me.precision), AVG(me.recall), AVG(me.f1Score) FROM ModelEvaluation me")
    Object[] getAverageMetrics();
    
    /**
     * Find evaluations for models trained on a specific dataset
     */
    @Query("SELECT me FROM ModelEvaluation me JOIN me.modelTraining mt WHERE mt.datasetId = :datasetId")
    List<ModelEvaluation> findByDatasetId(Long datasetId);
    
    /**
     * Find top evaluations by accuracy
     */
    List<ModelEvaluation> findTop5ByOrderByAccuracyDesc();
    
    /**
     * Find top evaluations by F1 score
     */
    List<ModelEvaluation> findTop5ByOrderByF1ScoreDesc();
    
    /**
     * Find evaluations with precision above threshold
     */
    List<ModelEvaluation> findByPrecisionGreaterThanEqual(Double threshold);
    
    /**
     * Find evaluations with recall above threshold
     */
    List<ModelEvaluation> findByRecallGreaterThanEqual(Double threshold);
    
    /**
     * Count evaluations by accuracy range
     */
    @Query("SELECT COUNT(me) FROM ModelEvaluation me WHERE me.accuracy BETWEEN :minAccuracy AND :maxAccuracy")
    Long countByAccuracyBetween(Double minAccuracy, Double maxAccuracy);
    
    /**
     * Get evaluations with balanced precision and recall
     */
    @Query("SELECT me FROM ModelEvaluation me WHERE ABS(me.precision - me.recall) < :threshold")
    List<ModelEvaluation> findWithBalancedPrecisionAndRecall(Double threshold);
}
