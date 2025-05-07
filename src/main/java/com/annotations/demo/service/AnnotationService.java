package com.annotations.demo.service;

public interface AnnotationService {
    void saveAnnotation(String text, Long coupleId, Long annotateurId);
    long countTotalAnnotations();
    Integer countAnnotationsByDataset(Long id);
}
