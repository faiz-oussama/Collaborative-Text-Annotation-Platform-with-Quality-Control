// File: src/main/java/com/annotations/demo/service/PasswordService.java
package com.annotations.demo.service;

public interface PasswordService {
    String generateRandomPassword(int length);
    void sendWelcomeEmail(String toEmail, String username, String password);
}
