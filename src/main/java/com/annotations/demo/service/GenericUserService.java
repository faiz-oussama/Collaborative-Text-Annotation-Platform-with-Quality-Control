package com.annotations.demo.service;

import com.annotations.demo.entity.User;
import com.annotations.demo.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Abstract base service for all user types providing common functionality
 */
public abstract class GenericUserService {
    protected final UserRepository userRepository;
    protected final PasswordEncoder passwordEncoder;

    public GenericUserService(UserRepository userRepository, 
                              PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Finds a user by ID, regardless of their specific type
     */
    public User findById(Long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("User not found"));
    }

    /**
     * Finds a user by login, regardless of their specific type
     */
    public User findByLogin(String login) {
        return userRepository.findByLogin(login);
    }

    /**
     * Abstract method for saving a user, to be implemented by specific user services
     */
    public abstract User save(User user);
    
    /**
     * Common method for logical deletion of users
     */
    @Transactional
    public void deleteLogically(Long id) {
        User user = findById(id);
        user.setDeleted(true);
        userRepository.save(user);
    }
    
    /**
     * Helper method for encoding passwords when needed
     */
    protected void encodePasswordIfNeeded(User user, User existingUser) {
        if (user.getPassword() != null && !user.getPassword().isEmpty() &&
                !user.getPassword().equals(existingUser.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else if (existingUser != null) {
            user.setPassword(existingUser.getPassword());
        }
    }

    /**
     * Copy basic user properties from source to target
     */
    protected void copyUserProperties(User source, User target) {
        target.setNom(source.getNom());
        target.setPrenom(source.getPrenom());
        target.setLogin(source.getLogin());
        target.setPassword(source.getPassword());
        target.setRole(source.getRole());
        target.setDeleted(source.isDeleted());
    }
}