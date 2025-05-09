package com.annotations.demo.service;


import com.annotations.demo.entity.Annotateur;
import com.annotations.demo.entity.Annotation;
import com.annotations.demo.entity.CoupleText;
import com.annotations.demo.entity.User;
import com.annotations.demo.repository.AnnotationRepository;
import com.annotations.demo.entity.Task;
import com.annotations.demo.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnnotationServiceImpl implements AnnotationService {
    private final AnnotationRepository annotationRepository;
    private final AnnotateurService annotateurService;
    private final CoupleTextServiceImpl coupleTextServiceImpl;
    private final TaskRepository taskRepository;
    private final TaskService taskService;

    public AnnotationServiceImpl(AnnotationRepository annotationRepository, AnnotateurService annotateurService, CoupleTextServiceImpl coupleTextServiceImpl, TaskRepository taskRepository, TaskService taskService) {
        this.annotationRepository = annotationRepository;
        this.annotateurService = annotateurService;
        this.coupleTextServiceImpl = coupleTextServiceImpl;
        this.taskRepository = taskRepository;
        this.taskService = taskService;
    }

    @Override
    public void saveAnnotation(String classSelectionText, Long coupleId, Long annotateurId ) {
        Annotation annotation = annotationRepository.findByAnnotateurIdAndCoupleTextId(annotateurId, coupleId)
            .orElse(new Annotation());
    
        // Set the chosen class directly as the string value
        annotation.setChosenClass(classSelectionText);
    
        // Set the other fields if this is a new annotation
        if (annotation.getId() == null) {

            CoupleText coupleText = coupleTextServiceImpl.findCoupleTextById(coupleId);
            Annotateur annotateur = annotateurService.findAnnotateurById(annotateurId);
        
            annotation.setCoupleText(coupleText);
            annotation.setAnnotateur(annotateur);
        }
    
        // Save the annotation
        annotationRepository.save(annotation);
    }
    @Override
    public long countTotalAnnotations() {
        return annotationRepository.count();
    }

    @Override
    public Integer countAnnotationsByDataset(Long id) {
        List<CoupleText> coupleTexts = coupleTextServiceImpl.findAllCoupleTextsByDatasetId(id);
        int count = 0;
        for (CoupleText coupleText : coupleTexts) {
            count += annotationRepository.findByCoupleText(coupleText).size();
        }
        return count;
    }

    @Override
    public List<Annotation> findAllAnnotationsByUser(User user){
        return annotationRepository.findByAnnotateur(user);
    }

    @Override
    public List<Annotation> findAllAnnotationsByDataset(Long id){
        return annotationRepository.findByCoupleText_Dataset_Id(id);
    }

    @Override
    public List<Annotation> findAnnotationsByTaskId(Long taskId) {
        // Since we don't have a direct task-to-annotation relationship in the repository,
        // we'll use the task ID to find the annotator and filter only their annotations
        // related to couples in the task
        return annotationRepository.findByAnnotateur_Taches_Id(taskId);
    }
}