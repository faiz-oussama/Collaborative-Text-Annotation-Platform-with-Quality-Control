package com.annotations.demo.controller;


import com.annotations.demo.entity.Annotateur;
import com.annotations.demo.entity.CoupleText;
import com.annotations.demo.entity.Task;
import com.annotations.demo.entity.User;
import com.annotations.demo.service.AnnotateurService;
import com.annotations.demo.service.AnnotationServiceImpl;
import com.annotations.demo.service.TaskService;
import com.annotations.demo.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserTaskController {
    private final AnnotateurService annotateurService;
    private final TaskService taskService;
    private final UserService userService;
    private final AnnotationServiceImpl annotationService;
    public UserTaskController(AnnotateurService annotateurService, TaskService taskService, UserService userService, AnnotationServiceImpl annotationService) {
        this.annotateurService = annotateurService;
        this.taskService = taskService;
        this.userService = userService;
        this.annotationService = annotationService;
    }

    @GetMapping("/tasks")
    public String showUserTaskHome(Model model) {
        List<Task> tasks = taskService.findAllTasksByAnnotateurId(userService.getCurrentAnnotateurId());
        model.addAttribute("tasks", tasks);
        return "user/tasks";
    }
    /**
     * Display the task detail with the current couple to annotate
     */
    @GetMapping("/tasks/{id}")
    public String viewTaskDetail(@PathVariable Long id,
                                 @RequestParam(required = false, defaultValue = "0") Integer index,
                                 Model model) {
        // Get current user
        User annotateur = userService.getCurrentAnnotateur();
        if (annotateur == null) {
            return "redirect:/login";
        }

        // Get the task and verify it belongs to this user
        Task task = taskService.findTaskById(id);
        if (!task.getAnnotateur().getId().equals(annotateur.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not authorized to view this task");
        }

        // Get all couples for this task
        List<CoupleText> couples = new ArrayList<>(task.getCouples());
        int totalCouples = couples.size();

        // Validate the index
        if (index < 0) {
            index = 0;
        } else if (index >= totalCouples) {
            index = totalCouples - 1;
        }

        // Get the current couple
        CoupleText currentCouple = null;
        if (!couples.isEmpty()) {
            currentCouple = couples.get(index);
        }

        // Check if this couple already has an annotation from this user
        String selectedClassId = taskService.getSelectedClassId(task.getId(), currentCouple.getId(), annotateur.getId());

        model.addAttribute("task", task);
        model.addAttribute("currentCouple", currentCouple);
        model.addAttribute("currentIndex", index);
        model.addAttribute("totalCouples", totalCouples);
        model.addAttribute("selectedClassId", selectedClassId);

        return "user/tasks";
    }

    /**
     * Handle the annotation submission
     */
    @PostMapping("/tasks/{taskId}/annotate")
    public String annotateCouple(@PathVariable Long taskId,
                                 @RequestParam Long coupleId,
                                 @RequestParam String classSelection,
                                 @RequestParam(required = false) String notes,
                                 @RequestParam Integer currentIndex,
                                 RedirectAttributes redirectAttributes) {

        // Get current user
        User annotateur = userService.getCurrentAnnotateur();
        if (annotateur == null) {
            return "redirect:/login";
        }

        // Save the annotation
        annotationService.saveAnnotation(classSelection, coupleId, annotateur.getId());

        // Determine next index (move to next couple)
        int nextIndex = currentIndex + 1;

        // Add success message
        redirectAttributes.addFlashAttribute("successMessage", "Annotation saved successfully!");

        // Redirect to the next couple or back to the task list if this was the last one
        Task task = taskService.findTaskById(taskId);
        if (task != null && nextIndex >= task.getCouples().size()) {
            // This was the last couple, redirect to task list
            redirectAttributes.addFlashAttribute("completedMessage", "Congratulations! You have completed all annotations for this task.");
            return "redirect:/user/home";
        }

        return "redirect:/user/tasks/" + taskId + "?index=" + nextIndex;
    }

}
