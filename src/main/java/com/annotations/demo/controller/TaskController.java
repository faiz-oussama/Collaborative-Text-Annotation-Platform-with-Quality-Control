package com.annotations.demo.controller;


import com.annotations.demo.entity.Annotateur;
import com.annotations.demo.entity.Dataset;
import com.annotations.demo.service.AnnotateurService;
import com.annotations.demo.service.AssignTaskToAnnotator;
import com.annotations.demo.service.DatasetService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class TaskController {

    private final DatasetService datasetService;
    private final AssignTaskToAnnotator assignTaskToAnnotator;
    private final AnnotateurService annotateurService;

    public TaskController(DatasetService datasetService, AssignTaskToAnnotator assignTaskToAnnotator, AnnotateurService annotateurService) {
        this.datasetService = datasetService;
        this.assignTaskToAnnotator = assignTaskToAnnotator;
        this.annotateurService = annotateurService;
    }


    @PostMapping("/datasets/{id}/assign")
    public String processAssignment(
            @PathVariable Long id,
            @RequestParam(value = "selectedAnnotateurs", required = false) List<Long> annotatorIds,
            @RequestParam("deadline") Date deadline,
            RedirectAttributes redirectAttributes) {

        Dataset dataset = datasetService.findDatasetById(id);
        if (dataset == null) {
            redirectAttributes.addFlashAttribute("error", "Dataset not found");
            return "redirect:/admin/datasets";
        }

        if (annotatorIds == null || annotatorIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("warning", "No annotators were selected");
            return "redirect:/admin/datasets/details/" + id;
        }

        //fetch the checked annotators
        List<Annotateur> annotateursList = annotateurService.findAllByIds(annotatorIds);
        try {
            assignTaskToAnnotator.assignTaskToAnnotator(annotateursList, dataset, deadline);
            redirectAttributes.addFlashAttribute("success", "Annotators successfully assigned to dataset");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error assigning annotators: " + e.getMessage());
        }

        return "redirect:/admin/datasets/details/" + id;
    }

}
