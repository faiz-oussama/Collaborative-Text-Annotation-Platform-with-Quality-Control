package com.annotations.demo.controller;

import com.annotations.demo.entity.Task;
import com.annotations.demo.service.TaskService;
import com.annotations.demo.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/user")
public class AnnotateurController {

    private final TaskService taskService;
    private final UserService userService;
    public AnnotateurController(TaskService taskService, UserService userService) {
        this.taskService = taskService;
        this.userService = userService;
    }


    @GetMapping("/home")
    public String showUserHome(Model model) {
        String currentUserName = StringUtils.capitalize(userService.getCurrentUserName());
        model.addAttribute("currentUserName", currentUserName);
        List<Task> tasks = taskService.findAllTasksByAnnotateurId(userService.getCurrentAnnotateurId());
        model.addAttribute("tasks", tasks);
        return "user/home";
    }
}
