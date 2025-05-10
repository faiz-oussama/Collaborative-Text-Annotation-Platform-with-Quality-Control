package com.annotations.demo.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class PasswordServiceImpl implements PasswordService {

    private final JavaMailSender mailSender;

    public PasswordServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++)
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String username, String password) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Welcome to Annotation Platform - Your Login Credentials");
        message.setText("Hello " + username + ",\n\n" +
                "Your account has been successfully created on our annotation platform.\n\n" +
                "Here are your login details:\n" +
                "Username: " + username + "\n" +
                "Initial Password: " + password + "\n\n" +
                "Please log in with these credentials and change your password as soon as possible.\n\n" +
                "Best regards,\n" +
                "The Annotation Platform Team");

        mailSender.send(message);
    }
}
