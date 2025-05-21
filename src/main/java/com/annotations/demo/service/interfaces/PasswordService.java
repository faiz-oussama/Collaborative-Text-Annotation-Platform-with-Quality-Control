package com.annotations.demo.service.interfaces;

public interface PasswordService {
    String generateRandomPassword(int length);
    void sendWelcomeEmail(String toEmail, String username, String password);
}
