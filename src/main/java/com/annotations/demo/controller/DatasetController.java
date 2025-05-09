package com.annotations.demo.controller;


import com.annotations.demo.entity.Annotateur;
import com.annotations.demo.entity.ClassPossible;
import com.annotations.demo.entity.CoupleText;
import com.annotations.demo.entity.Dataset;
import com.annotations.demo.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class DatasetController {

    private final DatasetServiceImpl datasetService;
    private final AnnotateurService annotateurService;
    private final CoupleTextServiceImpl coupleTextService;
    private final AsyncDatasetParserService asyncDatasetParserService;
    private final UserService userService;
    private final AssignTaskToAnnotator assignTaskToAnnotator;

    // Constructor-based injection for both services
    @Autowired
    public DatasetController(DatasetServiceImpl datasetService, AnnotateurService annotateurService, CoupleTextServiceImpl coupleTextService, AsyncDatasetParserService asyncDatasetParserService, UserService userService, AssignTaskToAnnotator assignTaskToAnnotator) {
        this.datasetService = datasetService;
        this.annotateurService = annotateurService;
        this.coupleTextService = coupleTextService;
        this.asyncDatasetParserService = asyncDatasetParserService;
        this.userService = userService;
        this.assignTaskToAnnotator = assignTaskToAnnotator;
    }

    @GetMapping("/datasets")
    public String showDatasetHome(Model model) {
        String currentUserName = StringUtils.capitalize(userService.getCurrentUserName());
        model.addAttribute("currentUserName", currentUserName);
        model.addAttribute("datasets", datasetService.findAllDatasets());
        return "admin/datasets_management/datasets";
    }

    @GetMapping("/datasets/details/{id}")
    public String DatasetDetails(@PathVariable Long id, Model model,
                                 @RequestParam(name = "page", defaultValue = "0") int page,
                                 @RequestParam(name = "size", defaultValue = "25") int size) {

        String currentUserName = StringUtils.capitalize(userService.getCurrentUserName());
        model.addAttribute("currentUserName", currentUserName);
        Dataset dataset = datasetService.findDatasetById(id);
        Page<CoupleText> coupleTextsPage = coupleTextService.getCoupleTextsByDatasetId(id, page, size);

        if (dataset == null) {
            model.addAttribute("errorMessage", "Dataset not found");
            return "redirect:/admin/datasets";
        }

        int totalPages = coupleTextsPage.getTotalPages();
        int currentPage = page;

        // Pagination window control (show up to 5 pages max, centered around current)
        int startPage = Math.max(0, currentPage - 2);
        int endPage = Math.min(totalPages - 1, currentPage + 2);

        model.addAttribute("coupleTextsPage", coupleTextsPage);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("dataset", dataset);

        return "admin/datasets_management/dataset_view";
    }


    @GetMapping("/datasets/add")
    public String addDataset(Model model) {
        String currentUserName = StringUtils.capitalize(userService.getCurrentUserName());
        model.addAttribute("currentUserName", currentUserName);
        model.addAttribute("dataset", new Dataset());
        return "admin/datasets_management/addDataset";
    }

    @PostMapping("/datasets/save")
    public String saveDataset(@ModelAttribute Dataset dataset,
                              @RequestParam("classes") String classesRaw,
                              @RequestParam("file") MultipartFile file,
                              RedirectAttributes redirectAttributes) throws IOException {

        try {
            Dataset savedDataset = datasetService.createDataset(dataset.getName(), dataset.getDescription(), file, classesRaw);
            datasetService.SaveDataset(savedDataset);

            asyncDatasetParserService.parseDatasetAsync(savedDataset);
            redirectAttributes.addFlashAttribute("success", "Dataset added successfully");
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to upload dataset: " + e.getMessage());
        }

        return "redirect:/admin/datasets";
    }

    @GetMapping("/datasets/{id}/assign_annotator")
    public String assignAnnotator(Model model,
                                  @PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        // Retrieve current user for display
        String currentUserName = StringUtils.capitalize(userService.getCurrentUserName());
        model.addAttribute("currentUserName", currentUserName);

        // Fetch active annotators
        List<Annotateur> annotateurs = annotateurService.findAllActive();

        // Proceed normally if enough annotators
        Dataset dataset = datasetService.findDatasetById(id);

        // Get already assigned annotators
        List<Long> assignedAnnotateurIds = new ArrayList<>();
        if (dataset.getTasks() != null) {
            assignedAnnotateurIds = dataset.getTasks().stream()
                    .filter(task -> task.getAnnotateur() != null)
                    .map(task -> task.getAnnotateur().getId())
                    .toList();
        }

        // Get deadline from existing tasks
        Date deadlineDate = null;
        if (dataset.getTasks() != null && !dataset.getTasks().isEmpty()) {
            deadlineDate = dataset.getTasks().get(0).getDateLimite();
        }

        // Add attributes to model
        model.addAttribute("deadlineDate", deadlineDate);
        model.addAttribute("assignedAnnotateurIds", assignedAnnotateurIds);
        model.addAttribute("dataset", dataset);
        model.addAttribute("annotateurs", annotateurs);

        return "admin/datasets_management/annotateur_assignment";
    }
    @GetMapping("/datasets/{id}/unassign_annotator")
    public String unassignAnnotator(@PathVariable Long id,
                                    @RequestParam Long annotatorId,
                                    RedirectAttributes redirectAttributes) {
        try {
            assignTaskToAnnotator.unassignAnnotator(id, annotatorId);
            redirectAttributes.addFlashAttribute("success", "Annotator unassigned successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to unassign annotator: " + e.getMessage());
        }
        return "redirect:/admin/datasets/details/" + id;
    }
}
