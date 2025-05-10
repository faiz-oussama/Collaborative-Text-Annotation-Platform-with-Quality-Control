package com.annotations.demo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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

    private String buildStyledEmailContent(String username, String password) {
        StringBuilder html = new StringBuilder();

        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <style>\n");
        html.append("        body {\n");
        html.append("            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;\n");
        html.append("            background-color: #f5f7fa;\n");
        html.append("            margin: 0;\n");
        html.append("            padding: 0;\n");
        html.append("        }\n");
        html.append("        .email-container {\n");
        html.append("            max-width: 600px;\n");
        html.append("            margin: 30px auto;\n");
        html.append("            background-color: #ffffff;\n");
        html.append("            border-radius: 12px;\n");
        html.append("            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);\n");
        html.append("            overflow: hidden;\n");
        html.append("            border: 1px solid #e0e0e0;\n");
        html.append("        }\n");
        html.append("        .header {\n");
        html.append("            background: linear-gradient(135deg, #4F46E5, #3B82F6);\n");
        html.append("            color: white;\n");
        html.append("            padding: 30px;\n");
        html.append("            text-align: center;\n");
        html.append("        }\n");
        html.append("        .header h1 {\n");
        html.append("            margin: 0;\n");
        html.append("            font-size: 24px;\n");
        html.append("            letter-spacing: 1px;\n");
        html.append("        }\n");
        html.append("        .content {\n");
        html.append("            padding: 30px;\n");
        html.append("            color: #333;\n");
        html.append("        }\n");
        html.append("        .content p {\n");
        html.append("            line-height: 1.6;\n");
        html.append("            margin-bottom: 20px;\n");
        html.append("        }\n");
        html.append("        .credentials {\n");
        html.append("            background-color: #f0f4ff;\n");
        html.append("            padding: 20px;\n");
        html.append("            border-left: 4px solid #3B82F6;\n");
        html.append("            border-radius: 6px;\n");
        html.append("            margin-top: 20px;\n");
        html.append("            font-family: monospace;\n");
        html.append("            color: #1f2937;\n");
        html.append("        }\n");
        html.append("        .footer {\n");
        html.append("            background-color: #f8fafc;\n");
        html.append("            text-align: center;\n");
        html.append("            font-size: 13px;\n");
        html.append("            color: #64748b;\n");
        html.append("            padding: 20px;\n");
        html.append("            border-top: 1px solid #e2e8f0;\n");
        html.append("        }\n");
        html.append("        .btn-primary {\n");
        html.append("            display: inline-block;\n");
        html.append("            background: linear-gradient(135deg, #4F46E5, #3B82F6);\n");
        html.append("            color: white;\n");
        html.append("            padding: 12px 24px;\n");
        html.append("            margin-top: 20px;\n");
        html.append("            border-radius: 6px;\n");
        html.append("            text-decoration: none;\n");
        html.append("            font-weight: bold;\n");
        html.append("            transition: opacity 0.3s ease;\n");
        html.append("        }\n");
        html.append("        .btn-primary:hover {\n");
        html.append("            opacity: 0.9;\n");
        html.append("        }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        html.append("    <div class=\"email-container\">\n");
        html.append("        <div class=\"header\">\n");
        html.append("            <h1>Welcome to Annotation Platform 🎉</h1>\n");
        html.append("        </div>\n");
        html.append("        <div class=\"content\">\n");
        html.append("            <p>Hello <strong>").append(username).append("</strong>,</p>\n");
        html.append("            <p>Your account has been successfully created on our annotation platform.</p>\n\n");

        html.append("            <p><strong>Here are your login credentials:</strong></p>\n\n");

        html.append("            <div class=\"credentials\">\n");
        html.append("                🔐 Username: <strong>").append(username).append("</strong><br/>\n");
        html.append("                🔑 Initial Password: <strong>").append(password).append("</strong>\n");
        html.append("            </div>\n\n");

        html.append("            <p>Please log in as soon as possible and change your password for security.</p>\n\n");

        html.append("            <a href=\"https://yourannotationplatform.com/login\" class=\"btn-primary\">Go to Login Page</a>\n");
        html.append("        </div>\n");
        html.append("        <div class=\"footer\">\n");
        html.append("            © 2025 Annotation Platform | All rights reserved<br/>\n");
        html.append("            This is an automated message. Please do not reply.\n");
        html.append("        </div>\n");
        html.append("    </div>\n");
        html.append("</body>\n");
        html.append("</html>");

        return html.toString();
    }

    @Override
    public void sendWelcomeEmail(String toEmail, String username, String password) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
            helper.setTo(toEmail);
            helper.setSubject("Welcome to Annotation Platform - Your Login Credentials");
            helper.setText(buildStyledEmailContent(username, password), true); // Set HTML flag to true
            helper.setFrom("no-reply@yourdomain.com"); // Optional but good practice

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

}
