package com.annotations.demo.controller;


import com.annotations.demo.entity.Annotation;
import com.annotations.demo.entity.Dataset;
import com.annotations.demo.service.AnnotationService;
import com.annotations.demo.service.DatasetService;
import com.annotations.demo.service.InterAnnotatorAgreement;
import com.annotations.demo.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdvancedStatisticsController {
    private final InterAnnotatorAgreement interAnnotatorAgreement;
    private final AnnotationService annotationService;
    private final UserService userService;
    private final DatasetService datasetService;


    public AdvancedStatisticsController(InterAnnotatorAgreement interAnnotatorAgreement , AnnotationService annotationService, UserService userService, DatasetService datasetService) {
        this.interAnnotatorAgreement = interAnnotatorAgreement;
        this.annotationService = annotationService;
        this.userService = userService;
        this.datasetService = datasetService;
    }

    @GetMapping("/advanced-stats")
    public String showAdvancedStatistics(Model model) {
        List<Dataset> datasets = datasetService.findAllDatasets();
        String currentUserName = StringUtils.capitalize(userService.getCurrentUserName());
        model.addAttribute("datasets", datasets);
        model.addAttribute("currentUserName", currentUserName);
        return "admin/statistics_management/advanced_stats";
    }

    @PostMapping("/metrics-calculate")
    public String calculateMetrics(@RequestParam("datasetId") Long datasetId, RedirectAttributes redirectAttributes) {
        Dataset dataset = datasetService.findDatasetById(datasetId);

        List<Annotation> annotations = dataset.getCoupleTexts().stream()
                .flatMap(c -> c.getAnnotations().stream())
                .collect(Collectors.toList());

        double fleissKappa = interAnnotatorAgreement.calculateFleissKappa(annotations);
        double cohenKappa = interAnnotatorAgreement.calculateCohensKappa(annotations);
        double percentAgreement = interAnnotatorAgreement.calculatePercentAgreement(annotations);
        String agreementStatus = interAnnotatorAgreement.getAgreementStatus(fleissKappa);

        int totalItems = dataset.getCoupleTexts().size();
        String datasetName = dataset.getName();

        // Add flash attributes to survive redirect
        redirectAttributes.addFlashAttribute("fleissKappa", fleissKappa);
        redirectAttributes.addFlashAttribute("cohenKappa", cohenKappa);
        redirectAttributes.addFlashAttribute("percentAgreement", percentAgreement);
        redirectAttributes.addFlashAttribute("agreementStatus", agreementStatus);
        redirectAttributes.addFlashAttribute("totalItems", totalItems);
        redirectAttributes.addFlashAttribute("selectedDatasetName", datasetName);

        return "redirect:/admin/advanced-stats";
    }


}
