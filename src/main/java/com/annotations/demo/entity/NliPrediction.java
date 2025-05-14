package com.annotations.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "nli_predictions")
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class NliPrediction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String label;
    private double score;
    private Long coupleTextId; // Added field to store the couple text ID
    
    public NliPrediction(String label, double score) {
        this.label = label;
        this.score = score;
    }
    
    public NliPrediction(String label, double score, Long coupleTextId) {
        this.label = label;
        this.score = score;
        this.coupleTextId = coupleTextId;
    }
}
