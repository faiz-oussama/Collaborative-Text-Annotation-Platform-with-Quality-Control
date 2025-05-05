package com.annotations.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class AnnotateurController {

    @GetMapping("/showUserHome")
    public String showUserHome() {
        return "user/home";
    }
}
