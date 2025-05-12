package com.annotations.demo.controller;


import com.annotations.demo.service.AnnotationService;
import com.annotations.demo.service.InterAnnotatorAgreement;
import com.annotations.demo.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdvancedStatisticsController {
    private InterAnnotatorAgreement interAnnotatorAgreement;
    private AnnotationService annotationService;
    private UserService userService;


    public AdvancedStatisticsController(InterAnnotatorAgreement interAnnotatorAgreement , AnnotationService annotationService, UserService userService) {
        this.interAnnotatorAgreement = interAnnotatorAgreement;
        this.annotationService = annotationService;
        this.userService = userService;
    }

    @GetMapping("/advanced-stats")
    public String showAdvancedStatistics(Model model) {



        String currentUserName = StringUtils.capitalize(userService.getCurrentUserName());
        model.addAttribute("currentUserName", currentUserName);
        return "admin/statistics_management/advanced_stats";
    }
}
