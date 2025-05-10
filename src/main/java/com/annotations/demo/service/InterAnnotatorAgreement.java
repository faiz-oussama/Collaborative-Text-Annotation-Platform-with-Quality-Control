package com.annotations.demo.service;

import com.annotations.demo.entity.Annotateur;
import com.annotations.demo.entity.Annotation;
import com.annotations.demo.entity.CoupleText;
import com.annotations.demo.entity.Task;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InterAnnotatorAgreement {
    
    // Calculate Fleiss' Kappa for inter-annotator agreement
    public double calculateFleissKappa(List<Annotation> annotations) {
        if (annotations == null || annotations.isEmpty()) return 0.0;
        
        // Group annotations by text pair
        Map<CoupleText, Map<String, Integer>> annotationsByPair = new HashMap<>();
        
        // First, count total annotations per category per text pair
        for (Annotation annotation : annotations) {
            CoupleText pair = annotation.getCoupleText();
            String category = annotation.getChosenClass();
            
            annotationsByPair.putIfAbsent(pair, new HashMap<>());
            Map<String, Integer> categoryCounts = annotationsByPair.get(pair);
            categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
        }
        
        // Calculate Fleiss' Kappa
        int n = annotationsByPair.size(); // Number of text pairs
        if (n == 0) return 0.0;
        
        // Calculate total annotations per category
        Map<String, Integer> totalCategoryCounts = new HashMap<>();
        for (Map<String, Integer> pairAnnotations : annotationsByPair.values()) {
            for (Map.Entry<String, Integer> entry : pairAnnotations.entrySet()) {
                totalCategoryCounts.put(entry.getKey(), 
                    totalCategoryCounts.getOrDefault(entry.getKey(), 0) + entry.getValue());
            }
        }
        
        // Calculate proportions
        double totalAnnotations = annotations.size();
        Map<String, Double> proportions = new HashMap<>();
        for (Map.Entry<String, Integer> entry : totalCategoryCounts.entrySet()) {
            proportions.put(entry.getKey(), entry.getValue() / totalAnnotations);
        }
        
        // Calculate observed agreement and expected agreement
        double observedAgreement = 0.0;
        double expectedAgreement = 0.0;
        
        for (Map<String, Integer> pairAnnotations : annotationsByPair.values()) {
            int totalPairAnnotations = pairAnnotations.values().stream().mapToInt(Integer::intValue).sum();
            if (totalPairAnnotations <= 1) continue; // Skip pairs with only one annotation
            
            // Calculate observed agreement for this pair
            double pairAgreement = 0.0;
            for (int count : pairAnnotations.values()) {
                pairAgreement += count * (count - 1);
            }
            pairAgreement /= totalPairAnnotations * (totalPairAnnotations - 1);
            observedAgreement += pairAgreement;
            
            // Calculate expected agreement for this pair
            double pairExpected = 0.0;
            for (String category : proportions.keySet()) {
                double prop = proportions.getOrDefault(category, 0.0);
                pairExpected += prop * prop;
            }
            expectedAgreement += pairExpected;
        }
        
        observedAgreement /= n;
        expectedAgreement /= n;
        
        // Fleiss' Kappa formula
        return (observedAgreement - expectedAgreement) / (1 - expectedAgreement);
    }
    
    // Identify potential spam annotators
    public List<Annotateur> identifySpamAnnotators(Task task) {
        List<Annotation> allAnnotations = task.getCouples().stream()
            .flatMap(couple -> couple.getAnnotations().stream())
            .collect(Collectors.toList());
        
        // Group annotations by annotator
        Map<Annotateur, List<Annotation>> annotationsByAnnotator = new HashMap<>();
        for (Annotation annotation : allAnnotations) {
            annotationsByAnnotator.putIfAbsent(annotation.getAnnotateur(), new ArrayList<>());
            annotationsByAnnotator.get(annotation.getAnnotateur()).add(annotation);
        }
        
        // Calculate average agreement for each annotator
        Map<Annotateur, Double> annotatorAgreementMap = new HashMap<>();
        for (Map.Entry<Annotateur, List<Annotation>> entry : annotationsByAnnotator.entrySet()) {
            Annotateur annotator = entry.getKey();
            List<Annotation> annotatorAnnotations = entry.getValue();
            
            // Calculate agreement with other annotators for the same text pairs
            double agreement = 0.0;
            int totalPairs = 0;
            
            for (Annotation annotation : annotatorAnnotations) {
                CoupleText pair = annotation.getCoupleText();
                // Get all annotations for this pair from other annotators
                List<Annotation> otherAnnotations = pair.getAnnotations().stream()
                    .filter(a -> !a.getAnnotateur().getId().equals(annotator.getId()))
                    .collect(Collectors.toList());
                
                if (!otherAnnotations.isEmpty()) {
                    // Calculate agreement with other annotators
                    long matchingAnnotations = otherAnnotations.stream()
                        .filter(a -> a.getChosenClass().equals(annotation.getChosenClass()))
                        .count();
                    
                    agreement += (double) matchingAnnotations / otherAnnotations.size();
                    totalPairs++;
                }
            }
            
            double avgAgreement = totalPairs > 0 ? agreement / totalPairs : 0.0;
            annotatorAgreementMap.put(annotator, avgAgreement);
        }
        
        // Identify potential spammers (those with very low agreement)
        double averageAgreement = annotatorAgreementMap.values().stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
        
        double threshold = Math.max(0.2, averageAgreement * 0.5); // At least 20% agreement or half the average
        
        return annotatorAgreementMap.entrySet().stream()
            .filter(entry -> entry.getValue() < threshold)
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
}