package com.annotations.demo.service;

import com.annotations.demo.entity.Administrator;
import com.annotations.demo.entity.Annotateur;
import com.annotations.demo.entity.Role;
import com.annotations.demo.entity.RoleType;
import com.annotations.demo.entity.User;
import com.annotations.demo.repository.AdministratorRepository;
import com.annotations.demo.repository.AnnotateurRepository;
import com.annotations.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl {

    private final UserRepository userRepository;
    private final AnnotateurRepository annotateurRepository;
    private final AdministratorRepository administratorRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(
            UserRepository userRepository,
            AnnotateurRepository annotateurRepository,
            AdministratorRepository administratorRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.annotateurRepository = annotateurRepository;
        this.administratorRepository = administratorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Saves a user based on their role. If the role is ADMIN, it creates an Administrator.
     * If the role is ANNOTATEUR, it creates an Annotateur.
     *
     * @param user The user to save
     * @return The saved user
     */
    @Transactional
    public User saveUserBasedOnRole(User user) {
        if (user == null || user.getRole() == null || user.getRole().getRole() == null) {
            throw new IllegalArgumentException("User and role must not be null");
        }
        Annotateur annotateur = new Annotateur();
        // Copy all basic user properties to annotateur
        copyUserProperties(user, annotateur);
        return annotateurRepository.save(annotateur);
        }
    }

    /**
     * Copy basic user properties from source to target
     */
    private void copyUserProperties(User source, User target) {
        target.setNom(source.getNom());
        target.setPrenom(source.getPrenom());
        target.setLogin(source.getLogin());
        target.setPassword(source.getPassword());
        target.setRole(source.getRole());
        target.setDeleted(source.isDeleted());
    }

    /**
     * Finds a user by ID, regardless of their specific type
     */
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    /**
     * Finds a user by login, regardless of their specific type
     */
    public User findByLogin(String login) {
        return userRepository.findByLogin(login);
    }
}
