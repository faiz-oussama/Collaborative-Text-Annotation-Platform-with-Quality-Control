package com.annotations.demo.controller;

import com.annotations.demo.entity.Annotateur;
import com.annotations.demo.entity.Annotation;
import com.annotations.demo.entity.Dataset;
import com.annotations.demo.entity.Task;
import com.annotations.demo.service.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class StatisticsController {
    private final TaskService taskService;
    private final AnnotateurService annotateurService;
    private final DatasetService datasetService;
    private final AnnotationService annotationService;
    private final UserService userService;

    @Autowired
    public StatisticsController(TaskService taskService,
                                AnnotateurService annotateurService,
                                DatasetService datasetService,
                                AnnotationService annotationService, UserService userService, InterAnnotatorAgreement interAnnotatorAgreement) {
        this.taskService = taskService;
        this.annotateurService = annotateurService;
        this.datasetService = datasetService;
        this.annotationService = annotationService;
        this.userService = userService;
    }

    @GetMapping("/overview")
    public String showStatistics(Model model) throws JsonProcessingException {
        // 1. Gather basic statistics
        long totalAnnotations = annotationService.countTotalAnnotations();
        long activeTasks = taskService.countActiveTasks();
        long totalDatasets = datasetService.countDatasets();
        long totalAnnotateurs = annotateurService.countActiveAnnotateurs();

        model.addAttribute("totalAnnotations", totalAnnotations);
        model.addAttribute("activeTasks", activeTasks);
        model.addAttribute("totalDatasets", totalDatasets);
        model.addAttribute("totalAnnotateurs", totalAnnotateurs);

        // 2. Dataset progress data
        Map<String, Object> datasetsProgressData = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> datasetNames = new ArrayList<>();
        List<Integer> totalCouples = new ArrayList<>();
        List<Integer> annotatedCouples = new ArrayList<>();

        for (Dataset dataset : datasetService.findAllDatasets()) {
            datasetNames.add(dataset.getName());
            totalCouples.add(dataset.getCoupleTexts().size());
            annotatedCouples.add(annotationService.countAnnotationsByDataset(dataset.getId()));
        }

        datasetsProgressData.put("labels", datasetNames);
        datasetsProgressData.put("totalCouples", totalCouples);
        datasetsProgressData.put("annotatedCouples", annotatedCouples);
        //pass the map as a JSON for the script retrieval
        String datasetsProgressJson = objectMapper.writeValueAsString(datasetsProgressData);
        model.addAttribute("datasetsProgressJson", datasetsProgressJson);



        // 3. Class distribution data
        Map<String, Integer> classDistributionMap = new HashMap<>();

        for (Dataset dataset : datasetService.findAllDatasets()) {
            for (Annotation annotation : annotationService.findAllAnnotationsByDataset(dataset.getId())) {
                String className = annotation.getChosenClass();
                classDistributionMap.put(className, classDistributionMap.getOrDefault(className, 0) + 1);
            }
        }

        // Convert to labels and data arrays
        List<String> labels = new ArrayList<>(classDistributionMap.keySet());
        List<Integer> data = labels.stream()
                .map(classDistributionMap::get)
                .collect(Collectors.toList());

        // Prepare JSON structure
        Map<String, Object> classDistributionChartData = new HashMap<>();
        classDistributionChartData.put("labels", labels);
        classDistributionChartData.put("data", data);

        String classDistributionJson = objectMapper.writeValueAsString(classDistributionChartData);
        model.addAttribute("classDistributionJson", classDistributionJson);



        // 4. Annotator performance data
        List<Annotateur> annotators = annotateurService.findAllActive();
        List<String> performanceLabels = new ArrayList<>();
        List<Integer> completionRates = new ArrayList<>();

        for (Annotateur annotator : annotators) {
            Long totalAssigned = taskService.countAssignedCouples(annotator);
            System.out.println("totalAssigned: " + totalAssigned);
            int totalAnnotated = annotationService.findAllAnnotationsByUser(annotator).size();
            System.out.println("totalAnnotated: " + totalAnnotated);
            int completionRate = (totalAssigned == 0) ? 0 : (int) ((totalAnnotated / (double) totalAssigned) * 100);

            performanceLabels.add(annotator.getNom());
            completionRates.add(completionRate);
        }

        Map<String, Object> performanceChartData = new HashMap<>();
        performanceChartData.put("labels", performanceLabels);
        performanceChartData.put("completionRates", completionRates);


        String performanceMapJson = objectMapper.writeValueAsString(performanceChartData);
        model.addAttribute("annotatorPerformanceData", performanceMapJson);

        //current user name
        String currentUserName = StringUtils.capitalize(userService.getCurrentUserName());
        model.addAttribute("currentUserName", currentUserName);

        return "admin/statistics_management/overview";
    }

}
