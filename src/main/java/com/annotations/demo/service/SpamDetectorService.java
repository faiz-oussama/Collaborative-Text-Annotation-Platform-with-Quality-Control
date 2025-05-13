package com.annotations.demo.service;

import com.annotations.demo.entity.Annotateur;
import com.annotations.demo.entity.Annotation;
import com.annotations.demo.entity.CoupleText;
import com.annotations.demo.entity.NliPrediction;
import com.annotations.demo.entity.SpamDetectionResults;
import com.annotations.demo.repository.SpamDetectionResultsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SpamDetectorService {
    private static final Logger logger = LoggerFactory.getLogger(SpamDetectorService.class);

    @Autowired
    private PythonService pythonService;
    
    @Autowired
    private SpamDetectionResultsRepository spamResultsRepository;
    
    @Value("${spam.detection.threshold:0.3}")
    private double defaultThreshold;
    
    /**
     * Map of known label equivalences to handle different formats
     * (e.g., "entailment" and "ENTAILMENT" should be considered the same)
     */
    private static final Map<String, String> LABEL_NORMALIZATION = Map.of(
            "entailment", "entailment",
            "contradiction", "contradiction",
            "neutral", "neutral",
            "entail", "entailment",
            "contradict", "contradiction",
            "yes", "entailment",
            "no", "contradiction",
            "maybe", "neutral"
    );

    /**
     * Detect spammers among the annotators based on their annotation quality
     * @param annotateurs List of annotators to evaluate
     * @return Map of annotators to their spam scores (mismatch rates)
     */
    public Map<Annotateur, Double> detectSpammers(List<Annotateur> annotateurs) {
        logger.info("Starting spam detection for {} annotators", annotateurs != null ? annotateurs.size() : 0);
        Map<Annotateur, Double> spamScores = new HashMap<>();

        if (annotateurs == null || annotateurs.isEmpty()) {
            logger.warn("Empty annotator list provided to detectSpammers");
            return spamScores;
        }

        for (Annotateur annotator : annotateurs) {
            try {
                double score = evaluateAnnotator(annotator);
                spamScores.put(annotator, score);
                logger.info("Annotator {} evaluated with spam score: {}", annotator.getId(), score);
            } catch (Exception e) {
                logger.error("Error evaluating annotator {}: {}", annotator.getId(), e.getMessage());
                // Continue with next annotator instead of failing the entire process
                spamScores.put(annotator, 0.0); // Default to 0 score on error
            }
        }

        return spamScores;
    }
    
    /**
     * Evaluate a single annotator for potential spam behavior
     * @param annotator The annotator to evaluate
     * @return The mismatch rate between model predictions and human annotations
     */
    private double evaluateAnnotator(Annotateur annotator) {
        // Get all text pairs annotated by this annotator
        List<CoupleText> pairs = annotator.getTaches().stream()
                .flatMap(t -> t.getCouples().stream())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (pairs.isEmpty()) {
            logger.info("No annotated pairs found for annotator {}", annotator.getId());
            return 0.0;
        }

        // Get all annotations by this annotator
        Map<CoupleText, String> annotatorLabelsMap = new HashMap<>();
        for (CoupleText pair : pairs) {
            List<Annotation> annotations = pair.getAnnotations();
            if (annotations != null) {
                for (Annotation annotation : annotations) {
                    if (annotation != null && 
                        annotation.getAnnotateur() != null && 
                        annotation.getAnnotateur().getId().equals(annotator.getId()) && 
                        annotation.getChosenClass() != null) {
                        
                        annotatorLabelsMap.put(pair, normalizeLabel(annotation.getChosenClass()));
                        break; // Found this annotator's annotation for this pair
                    }
                }
            }
        }
        
        // Filter pairs to only those that have annotations by this annotator
        List<CoupleText> annotatedPairs = pairs.stream()
                .filter(annotatorLabelsMap::containsKey)
                .collect(Collectors.toList());
                
        if (annotatedPairs.isEmpty()) {
            logger.info("No valid annotations found for annotator {}", annotator.getId());
            return 0.0;
        }
        
        logger.info("Found {} annotated pairs for annotator {}", annotatedPairs.size(), annotator.getId());
        
        // Get model predictions for these pairs
        List<NliPrediction> predictions;
        try {
            predictions = pythonService.sendToModel(annotatedPairs);
        } catch (Exception e) {
            logger.error("Error getting model predictions: {}", e.getMessage());
            throw new RuntimeException("Failed to get model predictions: " + e.getMessage(), e);
        }

        if (predictions.size() != annotatedPairs.size()) {
            logger.warn("Prediction count mismatch: expected {}, got {}", 
                    annotatedPairs.size(), predictions.size());
            
            // If we have fewer predictions than pairs, we'll only compare what we have
            if (predictions.size() < annotatedPairs.size()) {
                annotatedPairs = annotatedPairs.subList(0, predictions.size());
            } else {
                // If we have more predictions than pairs (shouldn't happen), truncate predictions
                predictions = predictions.subList(0, annotatedPairs.size());
            }
        }
        
        // Compare model predictions with human annotations
        long mismatches = 0;
        logger.info("Comparing {} model predictions with human annotations", predictions.size());
        
        // Log the total predictions and labels we're working with
        logger.info("Model predictions: ");
        for (int i = 0; i < predictions.size(); i++) {
            NliPrediction pred = predictions.get(i);
            logger.info("  - Prediction {}: Label={}, Score={}", 
                i, pred.getLabel(), pred.getScore());
        }
        
        for (int i = 0; i < predictions.size(); i++) {
            CoupleText pair = annotatedPairs.get(i);
            String modelLabel = normalizeLabel(predictions.get(i).getLabel());
            String humanLabel = annotatorLabelsMap.get(pair);

            logger.info("Comparing annotation for pair ID={}: ", pair.getId());
            logger.info("  - Text 1: {}", pair.getText_1() != null ? 
                (pair.getText_1().length() > 50 ? pair.getText_1().substring(0, 50) + "..." : pair.getText_1()) : "null");
            logger.info("  - Text 2: {}", pair.getText_2() != null ? 
                (pair.getText_2().length() > 50 ? pair.getText_2().substring(0, 50) + "..." : pair.getText_2()) : "null");
            logger.info("  - Model label: {}", modelLabel);
            logger.info("  - Human label: {}", humanLabel);

            if (!modelLabel.equals(humanLabel)) {
                mismatches++;
                logger.info("  - MISMATCH DETECTED");
            } else {
                logger.info("  - Labels match");
            }
        }
        
        logger.info("Found {} mismatches out of {} comparisons", mismatches, predictions.size());

        double mismatchRate = predictions.isEmpty() ? 0.0 : (double) mismatches / predictions.size();
        return mismatchRate;
    }
    
    /**
     * Save spam detection results for all annotators
     * @param spamScores Map of annotators to their spam scores
     * @param threshold Threshold for flagging annotators as spammers
     * @return List of saved spam detection results
     */
    public List<SpamDetectionResults> saveDetectionResults(Map<Annotateur, Double> spamScores, double threshold) {
        if (spamScores == null || spamScores.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Extract annotator IDs to delete existing results
        List<Long> annotateurIds = spamScores.keySet().stream()
                .map(Annotateur::getId)
                .collect(Collectors.toList());
        
        // Delete existing results for these annotators
        if (!annotateurIds.isEmpty()) {
            logger.info("Deleting existing spam detection results for {} annotators", annotateurIds.size());
            for (Long annotateurId : annotateurIds) {
                List<SpamDetectionResults> existingResults = spamResultsRepository.findByAnnotateurId(annotateurId);
                if (!existingResults.isEmpty()) {
                    logger.info("Found {} existing results for annotator ID: {}", existingResults.size(), annotateurId);
                    spamResultsRepository.deleteAll(existingResults);
                }
            }
        }
        
        // Create new results
        List<SpamDetectionResults> results = spamScores.entrySet().stream()
                .map(entry -> {
                    SpamDetectionResults result = new SpamDetectionResults();
                    result.setAnnotateur(entry.getKey());
                    result.setMismatchRate(entry.getValue());
                    result.setFlagged(entry.getValue() > threshold);
                    return result;
                })
                .collect(Collectors.toList());
        
        logger.info("Saving {} new spam detection results", results.size());
        return spamResultsRepository.saveAll(results);
    }
    
    /**
     * Run the complete spam detection process and save results
     * @param threshold Threshold for flagging annotators as spammers
     * @param annotateurs List of annotators to evaluate (if null, all annotators will be evaluated)
     * @return List of spam detection results
     */
    public List<SpamDetectionResults> runDetectionProcess(double threshold, List<Annotateur> annotateurs) {
        // Use provided threshold or default
        double thresholdValue = threshold > 0 ? threshold : defaultThreshold;
        logger.info("Running spam detection process with threshold: {}", thresholdValue);
        logger.info("Number of annotators to evaluate: {}", annotateurs.size());
        
        // Log annotator details
        for (Annotateur annotateur : annotateurs) {
            logger.info("Evaluating annotator: ID={}, Name={} {}", 
                annotateur.getId(), 
                annotateur.getPrenom(), 
                annotateur.getNom());
            
            // Log the number of tasks and annotations
            int taskCount = annotateur.getTaches() != null ? annotateur.getTaches().size() : 0;
            logger.info("  - Tasks assigned: {}", taskCount);
            
            // Log the actual annotations
            int totalAnnotations = 0;
            if (annotateur.getTaches() != null) {
                for (var task : annotateur.getTaches()) {
                    if (task.getCouples() != null) {
                        for (var couple : task.getCouples()) {
                            if (couple.getAnnotations() != null) {
                                for (var annotation : couple.getAnnotations()) {
                                    if (annotation.getAnnotateur() != null && 
                                        annotation.getAnnotateur().getId().equals(annotateur.getId())) {
                                        totalAnnotations++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            logger.info("  - Total annotations: {}", totalAnnotations);
        }
        
        // Detect spammers
        Map<Annotateur, Double> spamScores = detectSpammers(annotateurs);
        logger.info("Spam scores calculated for {} annotators", spamScores.size());
        
        // Log all spam scores
        spamScores.forEach((annotateur, score) -> {
            logger.info("Spam score for annotator {}: {}", annotateur.getId(), score);
        });
        
        // Save and return results
        List<SpamDetectionResults> results = saveDetectionResults(spamScores, thresholdValue);
        logger.info("Saved {} spam detection results", results.size());
        
        // Log the flagged vs. non-flagged counts
        long flaggedCount = results.stream().filter(SpamDetectionResults::isFlagged).count();
        logger.info("Flagged annotators: {}, Non-flagged: {}", flaggedCount, results.size() - flaggedCount);
        
        return results;
    }
    
    /**
     * Normalize label to a standard format for comparison
     * @param label The label to normalize
     * @return Normalized label
     */
    private String normalizeLabel(String label) {
        if (label == null) {
            return "";
        }
        
        String normalized = label.toLowerCase().trim();
        return LABEL_NORMALIZATION.getOrDefault(normalized, normalized);
    }
}
