package com.annotations.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @GetMapping("/showAdminHome")
    public String showAdminHome(Model model) {
        // Add admin-specific data to the model
        return "admin/home";
    }
}