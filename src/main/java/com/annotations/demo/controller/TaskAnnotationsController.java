package com.annotations.demo.controller;

import com.annotations.demo.entity.*;
import com.annotations.demo.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/admin")
public class TaskAnnotationsController {
    private final TaskService taskService;
    private final UserService userService;
    private final TaskProgressServiceImpl taskProgressService;
    private final AnnotationServiceImpl annotationService;

    public TaskAnnotationsController(TaskService taskService, UserService userService, TaskProgressServiceImpl taskProgressService, AnnotationServiceImpl annotationService) {
        this.taskService = taskService;
        this.userService = userService;
        this.taskProgressService = taskProgressService;
        this.annotationService = annotationService;
    }


    @GetMapping("/tasks")
    public String showAdminTaskHome(Model model) {
        String currentUserName = StringUtils.capitalize(userService.getCurrentUserName());

        List<Task> tasks = taskService.findAllTasks();
        List<Map<String, Object>> tasksWithProgress = new ArrayList<>();

        for (Task task : tasks) {
            User annotateur = task.getAnnotateur();
            Long taskId = task.getId();

            Optional<TaskProgress> progressOpt = taskProgressService.getProgressForUserAndTask(annotateur, taskId);
            int done = progressOpt.map(TaskProgress::getLastIndex).orElse(0);
            int total = task.getCouples().size(); // ou task.getDataset().getCouples().size()

            Map<String, Object> data = new HashMap<>();
            data.put("task", task);
            data.put("annotatorName", annotateur.getLogin());
            data.put("done", done);
            data.put("total", total);
            data.put("percent", total == 0 ? 0 : (int)((done * 100.0) / total));

            tasksWithProgress.add(data);
        }

        model.addAttribute("tasksWithProgress", tasksWithProgress);
        model.addAttribute("currentUserName", currentUserName);

        return "admin/tasks_management/task_view";
    }


    /**
     * Display all annotations for a specific task
     */
    @GetMapping("/annotations/{taskId}")
    public String viewTaskAnnotations(@PathVariable Long taskId, Model model) {
        // Get the task
        Task task = taskService.findTaskById(taskId);

        // Get the annotator
        Annotateur annotateur = task.getAnnotateur(); // Or get from session/user context

        // Get the annotations done by that annotator on that specific task
        List<Annotation> annotations = annotationService.findAnnotationsByAnnotatorForTask(taskId, annotateur.getId());

        int completedCount = annotations.size();
        int totalCouples = task.getCouples().size();

        String currentUserName = StringUtils.capitalize(userService.getCurrentUserName());

        model.addAttribute("currentUserName", currentUserName);
        model.addAttribute("task", task);
        model.addAttribute("annotations", annotations);
        model.addAttribute("completedCount", completedCount);
        model.addAttribute("totalCouples", totalCouples);

        return "admin/tasks_management/task_annotations";
    }
} 