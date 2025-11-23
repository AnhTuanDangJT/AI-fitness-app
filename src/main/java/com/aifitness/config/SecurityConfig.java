package com.aifitness.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Security Configuration
 * 
 * Configures Spring Security for authentication and authorization.
 * 
 * This will handle:
 * - JWT token authentication (instead of session-based)
 * - CORS configuration for React frontend
 * - Public endpoints (register, login) vs protected endpoints
 * - BCrypt password encoder
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    /**
     * BCrypt Password Encoder Bean
     * 
     * This replaces the custom password encryption from mainOne.java.
     * BCrypt is a one-way hashing algorithm (cannot be decrypted).
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Security Filter Chain
     * 
     * Configures which endpoints are public vs protected.
     * For now, all /auth endpoints are public (for signup/login).
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for API (JWT will be used)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // Stateless for JWT
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/health/**").permitAll() // Health check endpoint (public)
                .requestMatchers("/auth/**").permitAll() // Public endpoints
                .requestMatchers("/profile/**").permitAll() // Profile endpoints (JWT validation in controller)
                .requestMatchers("/user/**").permitAll() // User endpoints (JWT validation in controller)
                .requestMatchers("/calculate/**").permitAll() // Calculation endpoints (JWT validation in controller)
                .anyRequest().authenticated() // All other endpoints require authentication
            );
        
        return http.build();
    }
    
    /**
     * CORS Configuration
     * 
     * Allows requests from React frontend running on localhost:3000.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Get allowed origins from environment variable or use defaults
        String allowedOriginsEnv = System.getenv("CORS_ALLOWED_ORIGINS");
        if (allowedOriginsEnv != null && !allowedOriginsEnv.isEmpty()) {
            // Split by comma and trim whitespace
            List<String> origins = Arrays.asList(allowedOriginsEnv.split(","));
            configuration.setAllowedOrigins(origins.stream()
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        } else {
            // Default: localhost for development + production frontend URL
            configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173", // Vite default port
                "https://ai-fitness-app-one.vercel.app"
            ));
        }
        
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // Cache preflight for 1 hour
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

