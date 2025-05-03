package com.annotations.demo.controller;

import com.annotations.demo.entity.Annotateur;
import com.annotations.demo.entity.Role;
import com.annotations.demo.entity.RoleType;
import com.annotations.demo.entity.User;
import com.annotations.demo.repository.AnnotateurRepository;
import com.annotations.demo.repository.RoleRepository;
import com.annotations.demo.service.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;


@Controller
@RequestMapping("/admin")
public class AdminController {
    private final AnnotateurRepository annotateurRepository;
    private final UserServiceImpl userService;
    private final RoleRepository roleRepository;
    
    @Autowired
    public AdminController(
        AnnotateurRepository annotateurRepository,
        UserServiceImpl userService,
        RoleRepository roleRepository) {
        this.annotateurRepository = annotateurRepository;
        this.userService = userService;
        this.roleRepository = roleRepository;
    }
    
    @GetMapping("/showAdminHome")
    public String showAdminHome(Model model) {
        return "admin/home";
    }
    
    @GetMapping("/annotateurs")
    public String showUsers(Model model) {
        List<Annotateur> annotateurs = annotateurRepository.findAll();
        model.addAttribute("annotateurs", annotateurs);
        return "admin/annotateurs";
    }
    
    @GetMapping("/annotateurs/add")
    public String addAnnotateur(Model model) {
        model.addAttribute("user", new User());
        return "admin/addAnnotateur";
    }

    @PostMapping("/annotateurs/save")
    public String saveAnnotateur(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            // Set the role to ANNOTATEUR
            Role annotateurRole = roleRepository.findByRole(RoleType.USER_ROLE);
            if (annotateurRole == null) {
                // Fallback to USER_ROLE if ANNOTATEUR role not found
                annotateurRole = roleRepository.findByRole(RoleType.USER_ROLE);
            }
            
            if (annotateurRole == null) {
                throw new IllegalStateException("Annotateur role not found in the database");
            }
            
            user.setRole(annotateurRole);
            // Set default values for new Annotateur
            user.setDeleted(false);

            // Save the user as an Annotateur
            userService.saveUserBasedOnRole(user);

            redirectAttributes.addFlashAttribute("successMessage", "Annotateur added successfully");
            return "redirect:/admin/annotateurs";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding annotateur: " + e.getMessage());
            return "redirect:/admin/annotateurs/add";
        }
    }
}