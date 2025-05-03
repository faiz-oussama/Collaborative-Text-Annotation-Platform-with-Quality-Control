package com.annotations.demo.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RedirectionAfterAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    
    // The strategy to handle the redirection
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    
    // Default constructor
    public RedirectionAfterAuthenticationSuccessHandler() {
        super();
    }
    
    /**
     * This method is triggered when authentication is successful. It redirects
     * the user to the appropriate page based on their role and clears authentication
     * attributes stored in the session.
     */
    @Override
    public void onAuthenticationSuccess(final HttpServletRequest request, final HttpServletResponse response,
            final Authentication authentication) throws IOException, ServletException {
        // Handle the redirection after authentication success
        handle(request, response, authentication);
        // Clear temporary authentication-related attributes from the session
        clearAuthenticationAttributes(request);
    }
    
    /**
     * Handles the actual redirection after authentication success.
     * It determines the target URL based on the user's role and performs the redirection.
     */
    protected void handle(final HttpServletRequest request, final HttpServletResponse response,
            final Authentication authentication) throws IOException {
        // Determine the target URL based on the authentication object
        final String targetUrl = determineTargetUrl(authentication);
        // If the response has already been committed, prevent the redirection
        
        if (response.isCommitted()) {
            LOGGER.debug("Impossible to redirect because the response has already been sent: " + targetUrl);
            return;
        }
        // Perform the redirection
        redirectStrategy.sendRedirect(request, response, targetUrl);
    }
    
    /**
     * Determines the target URL based on the user's authorities (roles).
     * Users are redirected to different URLs based on their role.
     */
    protected String determineTargetUrl(final Authentication authentication) {
        // Map of roles to their corresponding target URLs after login
        Map<String, String> roleTargetUrlMap = new HashMap<>();
        // Define redirection URLs for roles
        roleTargetUrlMap.put("ROLE_USER_ROLE", "/user/showUserHome");
        roleTargetUrlMap.put("ROLE_ADMIN_ROLE", "/admin/showAdminHome");
        // Iterate through the authorities (roles) granted to the user
        final Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (final GrantedAuthority grantedAuthority : authorities) {
            // Get the role name from the authority
            String authorityName = grantedAuthority.getAuthority();
            // If the role matches one in the map, return the corresponding target URL
            if (roleTargetUrlMap.containsKey(authorityName)) {
                return roleTargetUrlMap.get(authorityName);
            }
        }
        // If no matching role is found, return a default URL
        return "/login";
    }
    
    /**
     * Removes temporary authentication-related data from the session after login.
     * This ensures no sensitive data remains in the session.
     */
    protected final void clearAuthenticationAttributes(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        // If no session exists, return
        if (session == null) {
            return;
        }
        // Remove authentication-related exception attributes from the session
        session.removeAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
    }
}