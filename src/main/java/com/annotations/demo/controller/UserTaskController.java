package com.annotations.demo.controller;

import com.annotations.demo.entity.*;
import com.annotations.demo.service.*;
import com.annotations.demo.service.interfaces.TaskService;
import com.annotations.demo.service.implementations.AnnotationServiceImpl;
import com.annotations.demo.service.implementations.TaskProgressServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/user")
public class UserTaskController {
    private final AnnotateurService annotateurService;
    private final TaskService taskService;
    private final UserService userService;
    private final TaskProgressServiceImpl taskProgressService;
    private final AnnotationServiceImpl annotationService;
    public UserTaskController(AnnotateurService annotateurService, TaskService taskService, UserService userService, TaskProgressServiceImpl taskProgressService, AnnotationServiceImpl annotationService) {
        this.annotateurService = annotateurService;
        this.taskService = taskService;
        this.userService = userService;
        this.taskProgressService = taskProgressService;
        this.annotationService = annotationService;
    }

    @GetMapping("/tasks")
    public String showUserTaskHome(Model model) {
        User annotateur = userService.getCurrentAnnotateur();
        if (annotateur == null) {
            return "redirect:/login";
        }

        String currentUserName = StringUtils.capitalize(userService.getCurrentUserName());
        List<Task> tasks = taskService.getValidTasksForAnnotateur(annotateur.getId());

        // Map task ID → lastIndex
        Map<Long, Float> taskProgressMap = new HashMap<>();

        for (Task task : tasks) {
            Optional<TaskProgress> progressOpt = taskProgressService.getProgressForUserAndTask(annotateur, task.getId());
            taskProgressMap.put(task.getId(), Float.valueOf(progressOpt.map(TaskProgress::getLastIndex).orElse(0)));
        }

        model.addAttribute("tasks", tasks);
        model.addAttribute("taskProgressMap", taskProgressMap);
        model.addAttribute("currentUserName", currentUserName);
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
        if (index == null || index == 0) {
            Optional<TaskProgress> progressOpt = taskProgressService.getProgressForUserAndTask(annotateur, id);
            if (progressOpt.isPresent()) {
                index = progressOpt.get().getLastIndex();
            }
        }

        // Get the current couple
        CoupleText currentCouple = null;
        if (!couples.isEmpty()) {
            currentCouple = couples.get(index);
        }

        // Check if this couple already has an annotation from this user
        String selectedClassId = taskService.getSelectedClassId(task.getId(), currentCouple.getId(), annotateur.getId());
        String currentUserName = StringUtils.capitalize(userService.getCurrentUserName());
        model.addAttribute("currentUserName", currentUserName);
        model.addAttribute("task", task);
        model.addAttribute("currentCouple", currentCouple);
        model.addAttribute("currentIndex", index);
        model.addAttribute("totalCouples", totalCouples);
        model.addAttribute("selectedClassId", selectedClassId);

        return "user/task_view";
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


        int nextIndex = currentIndex + 1;
        taskProgressService.saveOrUpdateProgress(annotateur, taskId, nextIndex);
        redirectAttributes.addFlashAttribute("successMessage", "Annotation saved successfully!");

        Task task = taskService.findTaskById(taskId);
        if (task != null && nextIndex >= task.getCouples().size()) {
            redirectAttributes.addFlashAttribute("completedMessage", "Congratulations! You have completed all annotations for this task.");
            return "redirect:/user/home";
        }

        return "redirect:/user/tasks/" + taskId + "?index=" + nextIndex;
    }



    @GetMapping("/history")
    public String showUserHistory(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model) {
        User annotateur = userService.getCurrentAnnotateur();
        if (annotateur == null) {
            return "redirect:/login";
        }
        String currentUserName = StringUtils.capitalize(userService.getCurrentUserName());
        Page<Annotation> annotationsPage = annotationService.findAllAnnotationsByUser(
                annotateur,
                PageRequest.of(page, size)
        );
        model.addAttribute("annotations", annotationsPage.getContent());
        model.addAttribute("currentPage", annotationsPage.getNumber());
        model.addAttribute("totalPages", annotationsPage.getTotalPages());
        model.addAttribute("totalItems", annotationsPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("currentUserName", currentUserName);
        return "user/history";
    }

}
