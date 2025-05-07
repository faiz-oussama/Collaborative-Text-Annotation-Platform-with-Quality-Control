package com.annotations.demo.service;

import com.annotations.demo.entity.Annotateur;
import com.annotations.demo.entity.User;
import com.annotations.demo.repository.AnnotateurRepository;
import com.annotations.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for Annotateur-specific operations, extending the GenericUserService
 */
@Service
public class AnnotateurService extends GenericUserService {
    private final AnnotateurRepository annotateurRepository;

    @Autowired
    public AnnotateurService(UserRepository userRepository,
                             AnnotateurRepository annotateurRepository,
                             PasswordEncoder passwordEncoder) {
        super(userRepository, passwordEncoder);
        this.annotateurRepository = annotateurRepository;
    }

    /**
     * Find all active (non-deleted) annotateurs
     */
    public List<Annotateur> findAllActive() {
        return annotateurRepository.findAllByDeleted(false);
    }


    public List<Annotateur> findAllByIds(List<Long> ids) {
        return annotateurRepository.findAllById(ids);
    }

    /**
     * Find annotateur by ID with type safety
     */
    public Annotateur findAnnotateurById(Long id) {
        return annotateurRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Annotateur not found with ID: " + id));
    }

    /**
     * Implementation of the abstract save method for annotateurs
     */
    @Override
    @Transactional
    public User save(User user) {
        // Handle the case when it's a new user but not an Annotateur instance
        if (!(user instanceof Annotateur) && user.getId() == null) {
            Annotateur annotateur = new Annotateur();
            copyUserProperties(user, annotateur);
            user = annotateur;
        }
        
        // If it's an update operation
        if (user.getId() != null) {
            // Find the existing annotateur
            User finalUser = user;
            Annotateur existingAnnotateur = annotateurRepository.findById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Annotateur not found with ID: " + finalUser.getId()));
            
            // Update basic properties
            existingAnnotateur.setNom(user.getNom());
            existingAnnotateur.setPrenom(user.getPrenom());
            existingAnnotateur.setLogin(user.getLogin());
            existingAnnotateur.setRole(user.getRole());
            existingAnnotateur.setDeleted(user.isDeleted());
            
            // Handle password encoding if needed
            encodePasswordIfNeeded(existingAnnotateur, user);
            
            return annotateurRepository.save(existingAnnotateur);
        } else {
            // For new users, encode the password
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return annotateurRepository.save((Annotateur) user);
        }
    }

    public long countActiveAnnotateurs() {
        return annotateurRepository.count();
    }
}
