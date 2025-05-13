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
                spamScores.put(annotator, 0.0);
            }
        }

        return spamScores;
    }
    
    private double evaluateAnnotator(Annotateur annotator) {
        List<CoupleText> pairs = annotator.getTaches().stream()
                .flatMap(t -> t.getCouples().stream())
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (pairs.isEmpty()) {
            logger.info("No annotated pairs found for annotator {}", annotator.getId());
            return 0.0;
        }

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
                        break;
                    }
                }
            }
        }
        
        List<CoupleText> annotatedPairs = pairs.stream()
                .filter(annotatorLabelsMap::containsKey)
                .collect(Collectors.toList());
                
        if (annotatedPairs.isEmpty()) {
            logger.info("No valid annotations found for annotator {}", annotator.getId());
            return 0.0;
        }
        
        logger.info("Found {} annotated pairs for annotator {}", annotatedPairs.size(), annotator.getId());
        
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
            
            if (predictions.size() < annotatedPairs.size()) {
                annotatedPairs = annotatedPairs.subList(0, predictions.size());
            } else {
                predictions = predictions.subList(0, annotatedPairs.size());
            }
        }
        
        long mismatches = 0;
        logger.info("Comparing {} model predictions with human annotations", predictions.size());
        
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
    
    public List<SpamDetectionResults> saveDetectionResults(Map<Annotateur, Double> spamScores, double threshold) {
        if (spamScores == null || spamScores.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> annotateurIds = spamScores.keySet().stream()
                .map(Annotateur::getId)
                .collect(Collectors.toList());
        
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
    
    public List<SpamDetectionResults> runDetectionProcess(double threshold, List<Annotateur> annotateurs) {
        double thresholdValue = threshold > 0 ? threshold : defaultThreshold;
        logger.info("Running spam detection process with threshold: {}", thresholdValue);
        logger.info("Number of annotators to evaluate: {}", annotateurs.size());
        
        for (Annotateur annotateur : annotateurs) {
            logger.info("Evaluating annotator: ID={}, Name={} {}", 
                annotateur.getId(), 
                annotateur.getPrenom(), 
                annotateur.getNom());
            
            int taskCount = annotateur.getTaches() != null ? annotateur.getTaches().size() : 0;
            logger.info("  - Tasks assigned: {}", taskCount);
            
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
        
        Map<Annotateur, Double> spamScores = detectSpammers(annotateurs);
        logger.info("Spam scores calculated for {} annotators", spamScores.size());
        
        spamScores.forEach((annotateur, score) -> {
            logger.info("Spam score for annotator {}: {}", annotateur.getId(), score);
        });
        
        List<SpamDetectionResults> results = saveDetectionResults(spamScores, thresholdValue);
        logger.info("Saved {} spam detection results", results.size());
        
        long flaggedCount = results.stream().filter(SpamDetectionResults::isFlagged).count();
        logger.info("Flagged annotators: {}, Non-flagged: {}", flaggedCount, results.size() - flaggedCount);
        
        return results;
    }
    
    private String normalizeLabel(String label) {
        if (label == null) {
            return "";
        }
        
        String normalized = label.toLowerCase().trim();
        return LABEL_NORMALIZATION.getOrDefault(normalized, normalized);
    }
}
