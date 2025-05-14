package com.annotations.demo.service;

import com.annotations.demo.entity.Annotation;
import com.annotations.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AnnotationService {
    Annotation findAnnotationById(Long id);
    void saveAnnotation(String text, Long coupleId, Long annotateurId);
    long countTotalAnnotations();
    Integer countAnnotationsByDataset(Long id);
    List<Annotation> findAllAnnotationsByUser(User user);
    Page<Annotation> findAllAnnotationsByUser(User user, Pageable pageable);
    List<Annotation> findAllAnnotationsByDataset(Long id);
    List<Annotation> findAnnotationsByAnnotatorForTask(Long taskId, Long annotateurId);
    /**
     * Find all annotations for text couples in a specific task
     */
    List<Annotation> findAnnotationsByTaskId(Long taskId);
}
