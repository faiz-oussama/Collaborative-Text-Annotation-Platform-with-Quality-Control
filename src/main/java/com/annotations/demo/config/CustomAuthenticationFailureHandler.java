package com.annotations.demo.config;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    // This method is called when authentication fails
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
    AuthenticationException exception) throws IOException, ServletException {
    // Print the stack trace of the exception for debugging purposes
    exception.printStackTrace();
    // Custom logic can be added here to handle different types of authentication failures
    // Check if the exception is a DisabledException (user account is disabled)
    if (exception instanceof DisabledException) {
    // Redirect the user to the login page with a "disabled" error message
    response.sendRedirect("login?error=disabled");
        return;
    }
    // Check if the exception is a LockedException (user account is locked)
    else if (exception instanceof LockedException) {
    // Redirect the user to the login page with a "locked" error message
    response.sendRedirect("login?error=locked");
        return;
    }
    // Check if the exception is a CredentialsExpiredException (user credentials are expired)
    else if (exception instanceof CredentialsExpiredException) {
    // Redirect the user to the login page with an "expired" error message
    response.sendRedirect("login?error=expired");
        return;
    }
    // Catch any other types of authentication exceptions
    else {
    // Redirect the user to the login page with a generic "other" error message
    response.sendRedirect("login?error=other");
    }
    }
}