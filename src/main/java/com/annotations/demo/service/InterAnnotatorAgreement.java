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

    public double calculateFleissKappa(List<Annotation> annotations) {
        if (annotations == null || annotations.isEmpty()) return 0.0;
        
        Map<CoupleText, Map<String, Integer>> annotationsByPair = new HashMap<>();
        
        for (Annotation annotation : annotations) {
            CoupleText pair = annotation.getCoupleText();
            String category = annotation.getChosenClass();
            
            annotationsByPair.putIfAbsent(pair, new HashMap<>());
            Map<String, Integer> categoryCounts = annotationsByPair.get(pair);
            categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
        }
        
        int n = annotationsByPair.size();
        if (n == 0) return 0.0;

        Map<String, Double> proportions = getStringDoubleMap(annotations, annotationsByPair);

        double observedAgreement = 0.0;
        double expectedAgreement = 0.0;
        
        for (Map<String, Integer> pairAnnotations : annotationsByPair.values()) {
            int totalPairAnnotations = pairAnnotations.values().stream().mapToInt(Integer::intValue).sum();
            if (totalPairAnnotations <= 1) continue;
            
            double pairAgreement = 0.0;
            for (int count : pairAnnotations.values()) {
                pairAgreement += count * (count - 1);
            }
            pairAgreement /= totalPairAnnotations * (totalPairAnnotations - 1);
            observedAgreement += pairAgreement;
            
            double pairExpected = 0.0;
            for (String category : proportions.keySet()) {
                double prop = proportions.getOrDefault(category, 0.0);
                pairExpected += prop * prop;
            }
            expectedAgreement += pairExpected;
        }
        
        observedAgreement /= n;
        expectedAgreement /= n;
        
        return (observedAgreement - expectedAgreement) / (1 - expectedAgreement);
    }

    private static Map<String, Double> getStringDoubleMap(List<Annotation> annotations, Map<CoupleText, Map<String, Integer>> annotationsByPair) {
        Map<String, Integer> totalCategoryCounts = new HashMap<>();
        for (Map<String, Integer> pairAnnotations : annotationsByPair.values()) {
            for (Map.Entry<String, Integer> entry : pairAnnotations.entrySet()) {
                totalCategoryCounts.put(entry.getKey(),
                    totalCategoryCounts.getOrDefault(entry.getKey(), 0) + entry.getValue());
            }
        }

        double totalAnnotations = annotations.size();
        Map<String, Double> proportions = new HashMap<>();
        for (Map.Entry<String, Integer> entry : totalCategoryCounts.entrySet()) {
            proportions.put(entry.getKey(), entry.getValue() / totalAnnotations);
        }
        return proportions;
    }

    public double calculateCohensKappa(List<Annotation> annotations) {
        if (annotations == null || annotations.isEmpty()) return 0.0;

        Map<CoupleText, Map<String, Integer>> annotationsByPair = new HashMap<>();

        for (Annotation annotation : annotations) {
            CoupleText pair = annotation.getCoupleText();
            String category = annotation.getChosenClass();

            annotationsByPair.putIfAbsent(pair, new HashMap<>());
            Map<String, Integer> categoryCounts = annotationsByPair.get(pair);
            categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
        }

        int n = annotationsByPair.size();
        if (n == 0) return 0.0;

        Map<String, Double> proportions = getStringDoubleMap(annotations, annotationsByPair);

        double observedAgreement = 0.0;
        double expectedAgreement = 0.0;

        for (Map<String, Integer> pairAnnotations : annotationsByPair.values()) {
            int totalPairAnnotations = pairAnnotations.values().stream().mapToInt(Integer::intValue).sum();
            if (totalPairAnnotations <= 1) continue; // Skip pairs with only one annotation

            double pairAgreement = 0.0;
            for (int count : pairAnnotations.values()) {
                pairAgreement += count * (count - 1);
            }
            pairAgreement /= totalPairAnnotations * (totalPairAnnotations - 1);
            observedAgreement += pairAgreement;

            double pairExpected = 0.0;
            for (String category : proportions.keySet()) {
                double prop = proportions.getOrDefault(category, 0.0);
                pairExpected += prop * prop;
            }
            expectedAgreement += pairExpected;
        }

        observedAgreement /= n;
        expectedAgreement /= n;

        return (observedAgreement - expectedAgreement) / (1 - expectedAgreement);
    }
    public double calculatePercentAgreement(List<Annotation> annotations) {
        if (annotations == null || annotations.isEmpty()) return 0.0;

        Map<CoupleText, Map<String, Integer>> annotationsByPair = new HashMap<>();

        for (Annotation annotation : annotations) {
            CoupleText pair = annotation.getCoupleText();
            String category = annotation.getChosenClass();

            annotationsByPair.putIfAbsent(pair, new HashMap<>());
            Map<String, Integer> categoryCounts = annotationsByPair.get(pair);
            categoryCounts.put(category, categoryCounts.getOrDefault(category, 0) + 1);
        }

        int n = annotationsByPair.size();
        if (n == 0) return 0.0;

        int matchingPairs = 0;
        for (Map<String, Integer> pairAnnotations : annotationsByPair.values()) {
            int maxCount = pairAnnotations.values().stream().mapToInt(Integer::intValue).max().orElse(0);
            if (maxCount > 1) matchingPairs++;
        }

        return (double) matchingPairs / n;
    }
    public String getAgreementStatus(double kappa) {
        if (kappa > 0.75) return "excellent";
        else if (kappa > 0.4) return "good";
        else return "poor";
    }
    
}