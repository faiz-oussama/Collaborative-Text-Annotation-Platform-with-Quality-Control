package com.annotations.demo.controller;


import com.annotations.demo.entity.Annotation;
import com.annotations.demo.entity.Dataset;
import com.annotations.demo.service.AnnotationServiceImpl;
import com.annotations.demo.service.CoupleTextServiceImpl;
import com.annotations.demo.service.TaskProgressServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class ModifyAnnotationController {

    private final AnnotationServiceImpl annotationService;
    private final CoupleTextServiceImpl coupleTextService;
    private final TaskProgressServiceImpl taskProgressService;

    public ModifyAnnotationController(AnnotationServiceImpl annotationService, CoupleTextServiceImpl coupleTextService, TaskProgressServiceImpl taskProgressService) {
        this.annotationService = annotationService;
        this.coupleTextService = coupleTextService;
        this.taskProgressService = taskProgressService;
    }

    @GetMapping("/tasks/{id}/annotate/{annotationId}/{coupleId}")
    public String viewAnnotationForm(@PathVariable Long annotationId,
                                    @PathVariable Long coupleId,
                                    @PathVariable Long id,
                                    Model model) {

        Annotation annotation = annotationService.findAnnotationById(annotationId);
        Dataset dataset = annotation.getCoupleText().getDataset();
        model.addAttribute("taskId", id);
        model.addAttribute("dataset", dataset);
        model.addAttribute("coupleId", coupleId);
        model.addAttribute("annotation", annotation);
        return "admin/tasks_management/modify_annotation_form";
    }
    @PostMapping("/update-annotation/{taskId}/{annotationId}")
    public String updateAnnotation(@PathVariable Long annotationId,
                                  @PathVariable Long taskId,
                                  @RequestParam String chosenClass,
                                  RedirectAttributes redirectAttributes){
        Annotation annotation = annotationService.findAnnotationById(annotationId);
        annotation.setChosenClass(chosenClass);
        annotationService.saveAnnotation(annotation.getChosenClass(), annotation.getCoupleText().getId(), annotation.getAnnotateur().getId());
        redirectAttributes.addFlashAttribute("successMessage", "Annotation updated successfully!");
        return "redirect:/admin/annotations/{taskId}";
    }
}
