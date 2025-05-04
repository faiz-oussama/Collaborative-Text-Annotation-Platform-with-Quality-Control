package com.annotations.demo.controller;

import com.annotations.demo.entity.Annotateur;
import com.annotations.demo.entity.Role;
import com.annotations.demo.entity.RoleType;
import com.annotations.demo.entity.User;
import com.annotations.demo.repository.RoleRepository;
import com.annotations.demo.service.AnnotateurService;
import com.annotations.demo.service.GenericUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
@RequestMapping("/admin")
public class AdminController {
    private final AnnotateurService annotateurService;
    private final RoleRepository roleRepository;
    
    @Autowired
    public AdminController(
            AnnotateurService annotateurService,
        RoleRepository roleRepository) {
        this.annotateurService = annotateurService;
        this.roleRepository = roleRepository;
    }
    
    @GetMapping("/showAdminHome")
    public String showAdminHome(Model model) {
        return "admin/home";
    }
    
    @GetMapping("/annotateurs")
    public String showUsers(Model model) {
        List<Annotateur> annotateurs = annotateurService.findAllActive();
        model.addAttribute("annotateurs", annotateurs);
        return "admin/annotateurs";
    }
    
    @GetMapping("/annotateurs/add")
    public String addAnnotateur(Model model) {
        model.addAttribute("user", new User());
        return "admin/addAnnotateur";
    }

    @GetMapping("/annotateurs/update/{id}")
    public String updateAnnotateur(@PathVariable Long id, Model model) {
        Annotateur annotateur = annotateurService.findAnnotateurById(id);
        if (annotateur == null) {
            model.addAttribute("errorMessage", "Annotateur not found");
            return "redirect:/admin/annotateurs";
        }
        model.addAttribute("user", annotateur);
        return "admin/addAnnotateur";
    }



    @PostMapping("/annotateurs/save")
    public String saveAnnotateur(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        boolean isUpdate = false;
        try {
            isUpdate = user.getId() != null;
            if (!isUpdate) {
                Role annotateurRole = roleRepository.findByRole(RoleType.USER_ROLE);
                if (annotateurRole == null) {
                    annotateurRole = roleRepository.findByRole(RoleType.USER_ROLE);
                }
                if (annotateurRole == null) {
                    throw new IllegalStateException("Annotateur role not found in the database");
                }
                user.setRole(annotateurRole);
                user.setDeleted(false);
            } else {
                // For updates, retrieve existing user to preserve role and other fields
                Annotateur existingAnnotateur = annotateurService.findAnnotateurById(user.getId());
                if (existingAnnotateur == null) {
                    throw new IllegalStateException("Annotateur not found for update");
                }

                // Preserve role and deleted status
                user.setRole(existingAnnotateur.getRole());
                user.setDeleted(existingAnnotateur.isDeleted());

                // Note: We no longer need to handle password here as it will be done in UserServiceImpl
            }
    
            annotateurService.save(user);
    
            String successMessage = isUpdate ? "Annotateur updated successfully" : "Annotateur added successfully";
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
            return "redirect:/admin/annotateurs";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error " + (isUpdate ? "updating" : "adding") + " annotateur: " + e.getMessage());
            if (isUpdate) {
                return "redirect:/admin/annotateurs/update/" + user.getId();
            }
            else {
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