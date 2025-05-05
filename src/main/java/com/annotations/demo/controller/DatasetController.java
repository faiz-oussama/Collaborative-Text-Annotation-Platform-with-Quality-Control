package com.annotations.demo.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class DatasetController {
    @GetMapping("/datasets")
    public String showDatasetHome() {
        return "admin/datasets_management/datasets";
    }

    @GetMapping("/datasets/add")
    public String addDataset() {
        return "admin/datasets_management/addDataset";
    }
}
