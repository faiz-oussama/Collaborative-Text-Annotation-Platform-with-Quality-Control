package com.annotations.demo.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);
    
    // Inject the UserDetailsService that will load user data for authentication
    @Autowired
    private UserDetailsService userService;
    
    // Constructor to log initialization of the SecurityConfig class
    public SecurityConfig() {
        LOGGER.debug("SecurityConfig Initialized");
    }
    
    // Bean to define the custom behavior after successful authentication (redirection)
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new RedirectionAfterAuthenticationSuccessHandler();
    }
    
    // Bean to define the custom behavior after failed authentication (failure handler)
    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return new CustomAuthenticationFailureHandler();
    }
    
    // Bean to provide the password encoder for encoding and comparing passwords
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    // Bean to define the AuthenticationProvider (DaoAuthenticationProvider) used to authenticate users
    @Bean
    public DaoAuthenticationProvider authProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService); // Set the custom UserDetailsService
        authProvider.setPasswordEncoder(passwordEncoder()); // Set the PasswordEncoder for comparing passwords
        return authProvider;
    }

    // Security filter chain to configure HTTP security, such as login, access control, and logout
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/admin/spam/**", "/admin/metrics-calculate", "/admin/model/**")
            )
            .authenticationProvider(authProvider()) // Use the custom AuthenticationProvider
            .authorizeHttpRequests(authz -> authz // Configure authorization for different URL patterns
                .requestMatchers("/user/**").hasRole("USER_ROLE") // Only allow users with the USER role for /user/** paths
                .requestMatchers("/admin/**").hasRole("ADMIN_ROLE") // Only allow users with the ADMIN role for /admin/** paths
                .requestMatchers("/main.css", "/css/**", "/js/**", "/images/**", "/favicon.ico", "/*.html", "/webjars/**").permitAll() // Allow access to static resources
                .anyRequest().permitAll() // Allow all other requests without restriction
            )
            .formLogin(form -> form // Configure login settings
                .loginPage("/login") // Custom login page URL
                .loginProcessingUrl("/login") // URL for processing login
                .failureHandler(authenticationFailureHandler()) // Custom failure handler
                .successHandler(authenticationSuccessHandler()) // Custom success handler
                .permitAll() // Allow everyone to access the login page
            )
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .deleteCookies("JSESSIONID") // Ensures session cookies are removed
                    .invalidateHttpSession(true) // Invalidate the session
                    .logoutSuccessUrl("/login?logout=true") // Optionally, redirect to the login page on successful logout
                    .permitAll()
            )
            .exceptionHandling(exception -> exception // Configure exception handling
                .accessDeniedPage("/access-denied") // Redirect users to /access-denied if they attempt to access unauthorized pages
            );
        return http.build(); // Build the security filter chain
    }
} 