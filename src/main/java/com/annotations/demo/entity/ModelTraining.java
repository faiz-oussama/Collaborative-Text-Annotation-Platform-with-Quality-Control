package com.annotations.demo.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
/**
 * Entity class to store information about model training sessions.
 * This tracks the training process, parameters, and results.
 */
@Entity
@Table(name = "model_training")
public class ModelTraining {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "dataset_id", nullable = false)
    private Long datasetId;
    
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;
    
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    @Column(length = 2000)
    private String hyperParameters;
    
    @Column(length = 50, nullable = false)
    private String status;
    
    @Column(name = "model_path")
    private String modelPath;
    
    @Column(name = "error_message", length = 2000)
    private String errorMessage;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dataset_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Dataset dataset;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;
    
    @Column(name = "last_modified_by")
    private String lastModifiedBy;
    
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;
    
    @Column(name = "version")
    private String version;
    
    @Column(name = "epoch_count")
    private Integer epochCount;
    
    @Column(name = "batch_size")
    private Integer batchSize;
    
    @Column(name = "learning_rate")
    private Double learningRate;
    
    // Default constructor
    public ModelTraining() {
        this.createdDate = LocalDateTime.now();
        this.status = "PENDING";
    }
    
    // Constructor with essential fields
    public ModelTraining(Long datasetId, String hyperParameters) {
        this.datasetId = datasetId;
        this.hyperParameters = hyperParameters;
        this.startTime = LocalDateTime.now();
        this.createdDate = LocalDateTime.now();
        this.status = "PENDING";
    }
    
    // Getter and setter methods
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getDatasetId() {
        return datasetId;
    }
    
    public void setDatasetId(Long datasetId) {
        this.datasetId = datasetId;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    public String getHyperParameters() {
        return hyperParameters;
    }
    
    public void setHyperParameters(String hyperParameters) {
        this.hyperParameters = hyperParameters;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getModelPath() {
        return modelPath;
    }
    
    public void setModelPath(String modelPath) {
        this.modelPath = modelPath;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    
    public Dataset getDataset() {
        return dataset;
    }
    
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
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
    
    public String getVersion() {
        return version;
    }
    
    public void setVersion(String version) {
        this.version = version;
    }
    
    public Integer getEpochCount() {
        return epochCount;
    }
    
    public void setEpochCount(Integer epochCount) {
        this.epochCount = epochCount;
    }
    
    public Integer getBatchSize() {
        return batchSize;
    }
    
    public void setBatchSize(Integer batchSize) {
        this.batchSize = batchSize;
    }
    
    public Double getLearningRate() {
        return learningRate;
    }
    
    public void setLearningRate(Double learningRate) {
        this.learningRate = learningRate;
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
        return "ModelTraining{" +
                "id=" + id +
                ", datasetId=" + datasetId +
                ", status='" + status + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
