package com.annotations.demo.service;

import com.annotations.demo.entity.Annotateur;
import com.annotations.demo.entity.Task;
import com.annotations.demo.entity.Annotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {
    
    @Autowired
    private TaskRepository taskRepository;
    
    @Autowired
    private InterAnnotatorAgreement interAnnotatorAgreement;
    
    @Override
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }
    
    @Override
    public Task getTaskById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }
    
    @Override
    public void deleteTaskById(Long id) {
        taskRepository.deleteById(id);
    }
    
    @Override
    public List<Annotateur> getAnnotateursForTask(Task task) {
        return task.getCouples().stream()
            .flatMap(c -> c.getAnnotations().stream())
            .map(a -> a.getAnnotateur())
            .distinct()
            .collect(Collectors.toList());
    }
    
    @Override
    public double getInterAnnotatorAgreement(Task task) {
        List<Annotation> allAnnotations = task.getCouples().stream()
            .flatMap(c -> c.getAnnotations().stream())
            .collect(Collectors.toList());
        
        return interAnnotatorAgreement.calculateFleissKappa(allAnnotations);
    }
    
    @Override
    public List<Annotateur> identifySpamAnnotators(Task task) {
        return interAnnotatorAgreement.identifySpamAnnotators(task);
    }
}