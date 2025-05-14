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
            "entails", "entailment",
            "contradition", "contradiction", // Note the spelling: 'contradition'
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
        // Get annotations directly from the annotator
        List<Annotation> userAnnotations = annotator.getAnnotations();
        
        if (userAnnotations == null || userAnnotations.isEmpty()) {
            logger.info("No annotations found for annotator {}", annotator.getId());
            return 0.0;
        }
        
        logger.info("Found {} annotations for annotator {}", userAnnotations.size(), annotator.getId());
        
        // Process only valid annotations with coupleText
        List<Annotation> validAnnotations = userAnnotations.stream()
                .filter(a -> a != null && a.getCoupleText() != null && a.getChosenClass() != null)
                .collect(Collectors.toList());
        
        if (validAnnotations.isEmpty()) {
            logger.info("No valid annotations with text pairs found for annotator {}", annotator.getId());
            return 0.0;
        }
        
        // Create a map of couple texts to the user's chosen labels
        Map<CoupleText, String> annotatorLabelsMap = validAnnotations.stream()
                .collect(Collectors.toMap(
                    Annotation::getCoupleText,
                    a -> normalizeLabel(a.getChosenClass()),
                    // If there are duplicate couples, keep the first one
                    (label1, label2) -> label1
                ));
                
        // Extract the unique couple texts
        List<CoupleText> annotatedPairs = new ArrayList<>(annotatorLabelsMap.keySet());
                
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
        logger.info("====== SPAM DETECTION EVALUATION FOR ANNOTATOR ID: {} ======", annotator.getId());
        logger.info("Comparing {} model predictions with human annotations", predictions.size());
        
        // Print comparison table header
        logger.info("+-------+---------------+---------------+---------------+-------------+----------+");
        logger.info("| Pair  | Model Label    | Human Label    | Match/Mismatch | Couple ID   | Texts    |");
        logger.info("+-------+---------------+---------------+---------------+-------------+----------+");
        
        // Track all comparison results for summary
        Map<String, Integer> modelLabelCounts = new HashMap<>();
        Map<String, Integer> humanLabelCounts = new HashMap<>();
        Map<String, Integer> mismatchCounts = new HashMap<>(); // Tracks mismatch types
        
        for (int i = 0; i < predictions.size(); i++) {
            CoupleText pair = annotatedPairs.get(i);
            NliPrediction pred = predictions.get(i); 
            String modelLabel = normalizeLabel(pred.getLabel());
            String humanLabel = normalizeLabel(annotatorLabelsMap.get(pair));

            // Update counters for summary
            modelLabelCounts.put(modelLabel, modelLabelCounts.getOrDefault(modelLabel, 0) + 1);
            humanLabelCounts.put(humanLabel, humanLabelCounts.getOrDefault(humanLabel, 0) + 1);
            
            // Short text snippets for the table
            String text1Short = pair.getText_1() != null ? 
                (pair.getText_1().length() > 25 ? pair.getText_1().substring(0, 25) + "..." : pair.getText_1()) : "null";
            String text2Short = pair.getText_2() != null ? 
                (pair.getText_2().length() > 25 ? pair.getText_2().substring(0, 25) + "..." : pair.getText_2()) : "null";
            
            boolean isMatch = modelLabel.equals(humanLabel);
            String matchStatus = isMatch ? "MATCH" : "MISMATCH";
            
            if (!isMatch) {
                mismatches++;
                // Track mismatch types for analysis
                String mismatchType = modelLabel + " vs " + humanLabel;
                mismatchCounts.put(mismatchType, mismatchCounts.getOrDefault(mismatchType, 0) + 1);
            }
            
            // Print detailed comparison row
            logger.info("| {:5d} | {:13s} | {:13s} | {:13s} | {:11d} | {} |",
                i+1, modelLabel, humanLabel, matchStatus, pair.getId(), 
                text1Short.substring(0, Math.min(text1Short.length(), 8)));
        }
        logger.info("+-------+---------------+---------------+---------------+-------------+----------+");
        
        // Calculate and log the final mismatch rate
        double mismatchRate = predictions.isEmpty() ? 0.0 : (double) mismatches / predictions.size();
        
        // Print summary information
        logger.info("\nSPAM DETECTION SUMMARY:");
        logger.info("Total pairs analyzed: {}", predictions.size());
        logger.info("Total mismatches found: {} ({}%)", mismatches, String.format("%.2f", mismatchRate * 100));
        logger.info("Mismatch threshold: {}%", String.format("%.2f", defaultThreshold * 100));
        logger.info("Is annotator flagged as spammer: {}", mismatchRate > defaultThreshold ? "YES" : "NO");
        
        // Log mismatch types for analysis
        if (!mismatchCounts.isEmpty()) {
            logger.info("\nMismatch types breakdown:");
            for (Map.Entry<String, Integer> entry : mismatchCounts.entrySet()) {
                logger.info("  {} -> {} occurrences ({}%)", 
                    entry.getKey(), 
                    entry.getValue(),
                    String.format("%.2f", (entry.getValue() * 100.0) / mismatches));
            }
        }
        
        // Print separator for better readability
        logger.info("=================================================================\n");

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
        
        // Convert to lowercase and trim whitespace
        String normalized = label.toLowerCase().trim();
        
        // Log the original and normalized label using INFO level
        logger.info("Normalizing label: '{}' -> '{}'", label, normalized);
        
        // Apply mapping or return the lowercase version
        String result = LABEL_NORMALIZATION.getOrDefault(normalized, normalized);
        
        // Log the final result after applying mapping
        if (!normalized.equals(result)) {
            logger.info("Applied mapping: '{}' -> '{}'", normalized, result);
        }
        
        return result;
    }
}
