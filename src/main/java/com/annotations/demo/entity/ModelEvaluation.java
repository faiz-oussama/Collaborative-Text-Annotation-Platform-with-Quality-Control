package com.annotations.demo.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity class to store information about model evaluation results.
 * This tracks the evaluation metrics, dataset used, and related model training.
 */
@Entity
@Table(name = "model_evaluation")
public class ModelEvaluation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "model_training_id", nullable = false)
    private Long modelTrainingId;
    
    @Column(name = "evaluation_time", nullable = false)
    private LocalDateTime evaluationTime;
    
    @Column(name = "test_dataset_path")
    private String testDatasetPath;
    
    @Column(name = "accuracy")
    private Double accuracy;
    
    @Column(name = "precision")
    private Double precision;
    
    @Column(name = "recall")
    private Double recall;
    
    @Column(name = "f1_score")
    private Double f1Score;
    
    @Column(name = "confusion_matrix", length = 4000)
    private String confusionMatrix;
    
    @Column(name = "evaluation_log_path")
    private String evaluationLogPath;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_training_id", referencedColumnName = "id", insertable = false, updatable = false)
    private ModelTraining modelTraining;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;
    
    @Column(name = "last_modified_by")
    private String lastModifiedBy;
    
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;
    
    // Additional metrics stored as JSON
    @Column(name = "additional_metrics", length = 4000)
    private String additionalMetrics;
    
    // Default constructor
    public ModelEvaluation() {
        this.evaluationTime = LocalDateTime.now();
        this.createdDate = LocalDateTime.now();
    }
    
    // Constructor with essential fields
    public ModelEvaluation(Long modelTrainingId, String testDatasetPath) {
        this.modelTrainingId = modelTrainingId;
        this.testDatasetPath = testDatasetPath;
        this.evaluationTime = LocalDateTime.now();
        this.createdDate = LocalDateTime.now();
    }
    
    // Getter and setter methods
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getModelTrainingId() {
        return modelTrainingId;
    }
    
    public void setModelTrainingId(Long modelTrainingId) {
        this.modelTrainingId = modelTrainingId;
    }
    
    public LocalDateTime getEvaluationTime() {
        return evaluationTime;
    }
    
    public void setEvaluationTime(LocalDateTime evaluationTime) {
        this.evaluationTime = evaluationTime;
    }
    
    public String getTestDatasetPath() {
        return testDatasetPath;
    }
    
    public void setTestDatasetPath(String testDatasetPath) {
        this.testDatasetPath = testDatasetPath;
    }
    
    public Double getAccuracy() {
        return accuracy;
    }
    
    public void setAccuracy(Double accuracy) {
        this.accuracy = accuracy;
    }
    
    public Double getPrecision() {
        return precision;
    }
    
    public void setPrecision(Double precision) {
        this.precision = precision;
    }
    
    public Double getRecall() {
        return recall;
    }
    
    public void setRecall(Double recall) {
        this.recall = recall;
    }
    
    public Double getF1Score() {
        return f1Score;
    }
    
    public void setF1Score(Double f1Score) {
        this.f1Score = f1Score;
    }
    
    public String getConfusionMatrix() {
        return confusionMatrix;
    }
    
    public void setConfusionMatrix(String confusionMatrix) {
        this.confusionMatrix = confusionMatrix;
    }
    
    public String getEvaluationLogPath() {
        return evaluationLogPath;
    }
    
    public void setEvaluationLogPath(String evaluationLogPath) {
        this.evaluationLogPath = evaluationLogPath;
    }
    
    public ModelTraining getModelTraining() {
        return modelTraining;
    }
    
    public void setModelTraining(ModelTraining modelTraining) {
        this.modelTraining = modelTraining;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }
    
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }
    
    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }
    
    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }
    
    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }
    
    public String getAdditionalMetrics() {
        return additionalMetrics;
    }
    
    public void setAdditionalMetrics(String additionalMetrics) {
        this.additionalMetrics = additionalMetrics;
    }
    
    /**
     * Sets all metrics from a map
     */
    public void setMetricsFromMap(Map<String, Double> metrics) {
        this.accuracy = metrics.getOrDefault("accuracy", 0.0);
        this.precision = metrics.getOrDefault("precision", 0.0);
        this.recall = metrics.getOrDefault("recall", 0.0);
        this.f1Score = metrics.getOrDefault("f1_score", 0.0);
        
        // Store all other metrics as additional metrics
        Map<String, Double> additionalMetricsMap = new HashMap<>(metrics);
        additionalMetricsMap.remove("accuracy");
        additionalMetricsMap.remove("precision");
        additionalMetricsMap.remove("recall");
        additionalMetricsMap.remove("f1_score");
        
        // Serialize to JSON - in a real implementation, this would use a proper JSON serializer
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Double> entry : additionalMetricsMap.entrySet()) {
            if (!first) {
                json.append(", ");
            }
            json.append("\"").append(entry.getKey()).append("\": ").append(entry.getValue());
            first = false;
        }
        json.append("}");
        
        this.additionalMetrics = json.toString();
    }
    
    /**
     * Gets all metrics as a map
     */
    public Map<String, Double> getMetricsAsMap() {
        Map<String, Double> metrics = new HashMap<>();
        metrics.put("accuracy", this.accuracy != null ? this.accuracy : 0.0);
        metrics.put("precision", this.precision != null ? this.precision : 0.0);
        metrics.put("recall", this.recall != null ? this.recall : 0.0);
        metrics.put("f1_score", this.f1Score != null ? this.f1Score : 0.0);
        
        // In a real implementation, this would parse the JSON using a proper JSON parser
        // For simplicity, we're not implementing that here
        
        return metrics;
    }
    
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastModifiedDate = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "ModelEvaluation{" +
                "id=" + id +
                ", modelTrainingId=" + modelTrainingId +
                ", evaluationTime=" + evaluationTime +
                ", accuracy=" + accuracy +
                ", f1Score=" + f1Score +
                '}';
    }
}
