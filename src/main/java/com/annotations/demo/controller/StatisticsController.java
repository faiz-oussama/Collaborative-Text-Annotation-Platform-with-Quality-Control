package com.annotations.demo.controller;

import com.annotations.demo.entity.Annotateur;
import com.annotations.demo.entity.Annotation;
import com.annotations.demo.entity.Dataset;
import com.annotations.demo.entity.Task;
import com.annotations.demo.service.*;
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
                                AnnotationService annotationService, UserService userService) {
        this.taskService = taskService;
        this.annotateurService = annotateurService;
        this.datasetService = datasetService;
        this.annotationService = annotationService;
        this.userService = userService;
    }

    @GetMapping("/overview")
    public String showStatistics(Model model) {
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
        model.addAttribute("datasetsProgressData", datasetsProgressData);


        // 4. Annotator performance data
        Map<String, Object> performanceData = new HashMap<>();
        List<String> annotatorNames = new ArrayList<>();
        List<Double> completionRates = new ArrayList<>();
        List<Double> annotationsPerDay = new ArrayList<>();

        for (Annotateur annotateur : annotateurService.findAllActive()) {
            String name = annotateur.getPrenom() + " " + annotateur.getNom();
            annotatorNames.add(name);
        }

        performanceData.put("annotators", annotatorNames);
        performanceData.put("completionRates", completionRates);
        model.addAttribute("annotatorPerformanceData", performanceData);

        // 5. Inter-annotator agreement data
        Map<String, Object> agreementData = new HashMap<>();
        List<String> datasetsWithMultipleAnnotators = new ArrayList<>();
        List<Double> kappaValues = new ArrayList<>();


        agreementData.put("datasets", datasetsWithMultipleAnnotators);
        agreementData.put("kappaValues", kappaValues);
        model.addAttribute("agreementData", agreementData);


        String currentUserName = StringUtils.capitalize(userService.getCurrentUserName());
        model.addAttribute("currentUserName", currentUserName);

        return "admin/statistics_management/overview";
    }

}
