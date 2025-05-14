package com.annotations.demo.repository;

import com.annotations.demo.entity.ModelTraining;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for ModelTraining entity.
 * Provides database operations for model training sessions.
 */
@Repository
public interface ModelTrainingRepository extends JpaRepository<ModelTraining, Long> {
    
    /**
     * Find all training sessions for a specific dataset
     */
    List<ModelTraining> findByDatasetId(Long datasetId);
    
    /**
     * Find all completed training sessions for a specific dataset
     */
    List<ModelTraining> findByDatasetIdAndStatus(Long datasetId, String status);
    
    /**
     * Find the most recent training session for a dataset
     */
    ModelTraining findFirstByDatasetIdOrderByStartTimeDesc(Long datasetId);
    
    /**
     * Find the most recent successful training session for a dataset
     */
    ModelTraining findFirstByDatasetIdAndStatusOrderByEndTimeDesc(Long datasetId, String status);
    
    /**
     * Count training sessions by status
     */
    @Query("SELECT mt.status, COUNT(mt) FROM ModelTraining mt GROUP BY mt.status")
    List<Object[]> countByStatus();
    
    /**
     * Find all training sessions in a specific status
     */
    List<ModelTraining> findByStatus(String status);
    
    /**
     * Search for training sessions by hyperparameters
     */
    @Query("SELECT mt FROM ModelTraining mt WHERE mt.hyperParameters LIKE %:paramValue%")
    List<ModelTraining> findByHyperParameterContaining(String paramValue);
    
    /**
     * Find training sessions within a date range
     */
    @Query("SELECT mt FROM ModelTraining mt WHERE mt.startTime >= :startDate AND mt.startTime <= :endDate")
    List<ModelTraining> findByDateRange(java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
    
    /**
     * Get average training time for successful trainings
     */
    @Query("SELECT AVG(TIMESTAMPDIFF(SECOND, mt.startTime, mt.endTime)) FROM ModelTraining mt WHERE mt.status = 'COMPLETED'")
    Double getAverageTrainingTimeInSeconds();
    
    /**
     * Find training sessions with errors
     */
    List<ModelTraining> findByErrorMessageIsNotNull();
    
    /**
     * Find training sessions by creator
     */
    List<ModelTraining> findByCreatedBy(String username);
    
    /**
     * Find training sessions with specific learning rate
     */
    List<ModelTraining> findByLearningRate(Double learningRate);
    
    /**
     * Find training sessions with specific epoch count
     */
    List<ModelTraining> findByEpochCount(Integer epochCount);
    
    /**
     * Find training sessions with specific batch size
     */
    List<ModelTraining> findByBatchSize(Integer batchSize);
}
