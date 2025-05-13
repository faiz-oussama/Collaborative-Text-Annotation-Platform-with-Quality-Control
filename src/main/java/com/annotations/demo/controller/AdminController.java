package com.annotations.demo.controller;

import com.annotations.demo.entity.*;
import com.annotations.demo.repository.RoleRepository;
import com.annotations.demo.service.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/admin")
public class AdminController {
    private final UserService userService;
    private final AnnotateurService annotateurService;
    private final RoleRepository roleRepository;
    private final TaskService taskService;
    private final DatasetService datasetService;
    private final AnnotationService annotationService;
    private final TaskProgressServiceImpl taskProgressService;
    private final PasswordService passwordService;
    
    @Autowired
    public AdminController(
            UserService userService, AnnotateurService annotateurService,
            RoleRepository roleRepository, TaskService taskService, DatasetService datasetService, AnnotationService annotationService, TaskProgressService taskProgressService, TaskProgressServiceImpl taskProgressService1, PasswordService passwordService) {
        this.userService = userService;
        this.annotateurService = annotateurService;
        this.roleRepository = roleRepository;
        this.taskService = taskService;
        this.datasetService = datasetService;
        this.annotationService = annotationService;
        this.taskProgressService = taskProgressService1;
        this.passwordService = passwordService;
    }
    
    @GetMapping("/showAdminHome")
    public String showAdminHome(Model model) {
        long totalAnnotations = annotationService.countTotalAnnotations();
        long activeTasks = taskService.countActiveTasks();
        long totalDatasets = datasetService.countDatasets();
        long totalAnnotateurs = annotateurService.findAllActive().size();
        String currentUserName = StringUtils.capitalize(userService.getCurrentUserName());
        model.addAttribute("currentUserName", currentUserName);
        model.addAttribute("totalAnnotations", totalAnnotations);
        model.addAttribute("activeTasks", activeTasks);
        model.addAttribute("totalDatasets", totalDatasets);
        model.addAttribute("totalAnnotateurs", totalAnnotateurs);

        return "admin/home";
    }
    
    @GetMapping("/annotateurs")
    public String showUsers(@RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "5") int size,
                            Model model) {
        Page<Annotateur> annotateurs = annotateurService.findAllActive(page, size);
        Map<Long, LocalDateTime> lastActivity = new HashMap<>();
        long totalAnnotateurs = annotateurService.countActiveAnnotateurs();

        for (Annotateur annotateur : annotateurs) {
            TaskProgress latestProgress = taskProgressService.getLastAnnotationByUser(annotateur);

            if (latestProgress != null) {
                lastActivity.put(annotateur.getId(), latestProgress.getUpdatedAt());
            } else {
                lastActivity.put(annotateur.getId(), null);
            }
        }
        String currentUserName = StringUtils.capitalize(userService.getCurrentUserName());
        model.addAttribute("currentUserName", currentUserName);
        model.addAttribute("lastActivity", lastActivity);
        model.addAttribute("annotateurs", annotateurs);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", annotateurs.getTotalPages());
        model.addAttribute("totalItems", totalAnnotateurs);
        return "admin/annotateur_management/annotateurs";
    }
    
    @GetMapping("/annotateurs/add")
    public String addAnnotateur(Model model) {
        String currentUserName = StringUtils.capitalize(userService.getCurrentUserName());
        model.addAttribute("currentUserName", currentUserName);
        model.addAttribute("user", new User());
        return "admin/annotateur_management/addAnnotateur";
    }

    @GetMapping("/annotateurs/update/{id}")
    public String updateAnnotateur(@PathVariable Long id, Model model) {
        String currentUserName = StringUtils.capitalize(userService.getCurrentUserName());
        model.addAttribute("currentUserName", currentUserName);
        Annotateur annotateur = annotateurService.findAnnotateurById(id);
        if (annotateur == null) {
            model.addAttribute("errorMessage", "Annotateur not found");
            return "redirect:/admin/annotateurs";
        }
        model.addAttribute("user", annotateur);
        return "admin/annotateur_management/addAnnotateur";
    }



    @PostMapping("/annotateurs/save")
    public String saveAnnotateur(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        boolean isUpdate = false;
        try {
            isUpdate = user.getId() != null;

            if (!isUpdate) {
                // Generate random password for new users
                String rawPassword = passwordService.generateRandomPassword(12);

                // Set the password in encoded form
                user.setPassword(rawPassword); // This will be encoded by UserServiceImpl

                Role annotateurRole = roleRepository.findByRole(RoleType.USER_ROLE);
                if (annotateurRole == null) {
                    annotateurRole = roleRepository.findByRole(RoleType.USER_ROLE);
                }
                if (annotateurRole == null) {
                    throw new IllegalStateException("Annotateur role not found in the database");
                }
                user.setRole(annotateurRole);
                user.setDeleted(false);

                // Save the user first to get the ID
                annotateurService.save(user);

                // Now send the welcome email with login details
                passwordService.sendWelcomeEmail(user.getEmail(), user.getLogin(), rawPassword);
            } else {
                // For updates, retrieve existing user to preserve role and other fields
                Annotateur existingAnnotateur = annotateurService.findAnnotateurById(user.getId());
                if (existingAnnotateur == null) {
                    throw new IllegalStateException("Annotateur not found for update");
                }

                // Preserve role and deleted status
                user.setRole(existingAnnotateur.getRole());
                user.setDeleted(existingAnnotateur.isDeleted());
                annotateurService.save(user);
            }

            String successMessage = isUpdate ? "Annotateur updated successfully" : "Annotateur added successfully";
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
            return "redirect:/admin/annotateurs";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error " + (isUpdate ? "updating" : "adding") + " annotateur: " + e.getMessage());
            if (isUpdate) {
                return "redirect:/admin/annotateurs/update/" + user.getId();
            } else {
                return "redirect:/admin/annotateurs/add";
            }
        }
    }

    @GetMapping("/annotateurs/delete/{id}")
    public String deleteAnnotateur(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Annotateur annotateur = annotateurService.findAnnotateurById(id);
        if (annotateur == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Annotateur not found");
            return "redirect:/admin/annotateurs";
        }
        annotateurService.deleteLogically(id);
        redirectAttributes.addFlashAttribute("successMessage", "Annotateur deleted successfully");
        return "redirect:/admin/annotateurs";
    }


}