package com.annotations.demo.service;

import com.annotations.demo.entity.Administrator;
import com.annotations.demo.entity.User;
import com.annotations.demo.repository.AdministratorRepository;
import com.annotations.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for Administrator-specific operations, extending the GenericUserService
 */
@Service
public class AdministratorService extends GenericUserService {
    private final AdministratorRepository administratorRepository;

    public AdministratorService(UserRepository userRepository,
                              AdministratorRepository administratorRepository,
                              PasswordEncoder passwordEncoder) {
        super(userRepository, passwordEncoder);
        this.administratorRepository = administratorRepository;
    }

    /**
     * Find all active (non-deleted) administrators
     */
    public List<Administrator> findAllActive() {
        return administratorRepository.findAllByDeleted(false);
    }

    /**
     * Find administrator by ID
     */
    public Administrator findAdministratorById(Long id) {
        return administratorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Administrator not found"));
    }

    /**
     * Implementation of the abstract save method for administrators
     */
    @Override
    @Transactional
    public User save(User user) {
        if (!(user instanceof Administrator) && user.getId() == null) {
            // If it's a plain User object for a new administrator, convert it
            Administrator administrator = new Administrator();
            copyUserProperties(user, administrator);
            user = administrator;
        }
        
        // If it's an update operation
        if (user.getId() != null) {
            Administrator existingAdministrator = administratorRepository.findById(user.getId())
                .orElseThrow(() -> new EntityNotFoundException("Administrator not found"));
            
            // Update basic properties
            existingAdministrator.setNom(user.getNom());
            existingAdministrator.setPrenom(user.getPrenom());
            existingAdministrator.setLogin(user.getLogin());
            existingAdministrator.setRole(user.getRole());
            existingAdministrator.setDeleted(user.isDeleted());
            
            // Handle password
            encodePasswordIfNeeded(existingAdministrator, user);
            
            return administratorRepository.save(existingAdministrator);
        } else {
            // For new users, encode the password
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            return administratorRepository.save((Administrator) user);
        }
    }
}
