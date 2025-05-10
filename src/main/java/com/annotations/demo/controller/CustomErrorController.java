package com.annotations.demo.controller;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(WebRequest request, Model model) {
        Map<String, Object> errorAttributes = new org.springframework.boot.web.servlet.error.DefaultErrorAttributes()
                .getErrorAttributes(request, ErrorAttributeOptions.defaults());

        Integer statusCode = (Integer) errorAttributes.getOrDefault("status", 500);
        String errorMessage = (String) errorAttributes.getOrDefault("message", "Unknown error");
        String exception = (String) errorAttributes.getOrDefault("exception", null);

        model.addAttribute("errorCode", statusCode);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("exception", exception);

        return "error";
    }
}