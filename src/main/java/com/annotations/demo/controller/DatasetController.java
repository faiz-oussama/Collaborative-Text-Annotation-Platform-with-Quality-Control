package com.annotations.demo.controller;


import com.annotations.demo.entity.ClassPossible;
import com.annotations.demo.entity.Dataset;
import com.annotations.demo.service.DatasetServiceImpl;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class DatasetController {

    private final DatasetServiceImpl datasetService;

    public DatasetController(DatasetServiceImpl datasetService) {
        this.datasetService = datasetService;
    }

    @GetMapping("/datasets")
    public String showDatasetHome(Model model) {
        model.addAttribute("datasets", datasetService.findAllDatasets());
        return "admin/datasets_management/datasets";
    }

    @GetMapping("/datasets/add")
    public String addDataset(Model model) {
        model.addAttribute("dataset", new Dataset());
        return "admin/datasets_management/addDataset";
    }

    @PostMapping("/datasets/save")
    public String saveDataset(@ModelAttribute Dataset dataset,
                              @RequestParam("classes") String classesRaw,
                              @RequestParam("file") MultipartFile file,
                              RedirectAttributes redirectAttributes) {

        Dataset savedDataset = datasetService.createDataset(dataset.getName(), dataset.getDescription(), file, classesRaw);
        datasetService.SaveDataset(savedDataset);
        redirectAttributes.addFlashAttribute("success", "Dataset added successfully");


        return "redirect:/admin/datasets";
    }
}
