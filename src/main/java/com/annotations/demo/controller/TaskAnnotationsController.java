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

    public TaskAnnotationsController(TaskService taskService, UserService userService, TaskProgressServiceImpl taskProgressService) {
        this.taskService = taskService;
        this.userService = userService;
        this.taskProgressService = taskProgressService;
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
        
        // Add the task to the model
        String currentUserName = StringUtils.capitalize(userService.getCurrentUserName());
        model.addAttribute("currentUserName", currentUserName);
        model.addAttribute("task", task);
        
        // Add placeholder statistics - these would be calculated from real data in a production app
        model.addAttribute("completedCount", 0);
        model.addAttribute("totalCouples", 10); // Example value for UI display
        
        // Return the annotations view template
        return "admin/tasks_management/task_annotations";
    }
} 