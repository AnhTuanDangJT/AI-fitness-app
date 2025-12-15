package com.aifitness.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
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
 * Security features:
 * - JWT token authentication (stateless, no sessions)
 * - CORS configuration locked to allowed domains only
 * - Security headers (X-Content-Type-Options, X-Frame-Options, X-XSS-Protection, CSP)
 * - CSRF disabled (documented below - safe for stateless JWT APIs)
 * - Rate limiting applied via RateLimitingFilter
 * 
 * CSRF Protection Note:
 * CSRF is disabled because this API uses stateless JWT authentication.
 * - No server-side sessions are maintained (SessionCreationPolicy.STATELESS)
 * - JWT tokens are stored client-side (not in cookies)
 * - Tokens are sent via Authorization header (not automatically included by browsers)
 * - This makes CSRF attacks ineffective as tokens must be explicitly included in requests
 * 
 * If cookies are used in the future, CSRF protection must be re-enabled and tokens
 * should be set as HttpOnly, Secure, SameSite=LAX or STRICT.
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
     * Applies security headers and CORS configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF disabled for stateless JWT API (see class documentation)
            .csrf(csrf -> csrf.disable())
            
            // CORS configuration (locked to allowed origins)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // Stateless session management (no server-side sessions for JWT)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Security headers (equivalent to helmet.js)
            .headers(headers -> headers
                // Prevent MIME type sniffing
                .contentTypeOptions(contentTypeOptions -> {})
                // Prevent clickjacking
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
                // HSTS (HTTP Strict Transport Security)
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000) // 1 year
                    .includeSubDomains(true))
                // Referrer policy
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                // Content Security Policy (restrictive for API)
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; script-src 'none'; style-src 'none'; img-src 'none'; font-src 'none'; connect-src 'self'"))
            )
            
            // Authorization rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/health/**").permitAll() // Health check endpoint (public)
                .requestMatchers("/auth/**").permitAll() // Public endpoints (rate limited in filter)
                .requestMatchers("/profile/**").permitAll() // All profile endpoints (JWT validation in controller)
                .requestMatchers("/user/**").permitAll() // User endpoints (JWT validation in controller)
                .requestMatchers("/calculate/**").permitAll() // Calculation endpoints (JWT validation in controller)
                .requestMatchers("/meal-preferences/**").permitAll() // TEMPORARY: Meal preferences - permitAll for testing (change back to authenticated() after)
                .requestMatchers("/ai/**").permitAll() // AI endpoints (JWT validation in controllers)
                .requestMatchers("/feedback/**").permitAll() // Feedback endpoint (JWT validation in controller)
                .requestMatchers("/gamification/**").permitAll() // Gamification endpoints (JWT validation in controller)
                .anyRequest().authenticated() // All other endpoints require authentication
            );
        
        return http.build();
    }
    
    /**
     * CORS Configuration
     * 
     * Strictly limits allowed origins to:
     * - http://localhost:3000 (development)
     * - http://localhost:5173 (Vite development)
     * - https://ai-fitness-app-one.vercel.app (production)
     * 
     * Only necessary HTTP methods and headers are allowed.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Get allowed origins from environment variable or use strict defaults
        String allowedOriginsEnv = System.getenv("CORS_ALLOWED_ORIGINS");
        if (allowedOriginsEnv != null && !allowedOriginsEnv.isEmpty()) {
            // Split by comma and trim whitespace
            List<String> origins = Arrays.asList(allowedOriginsEnv.split(","));
            configuration.setAllowedOrigins(origins.stream()
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        } else {
            // Strict default: only allowed development and production origins
            configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173", // Vite default port
                "https://ai-fitness-app-one.vercel.app"
            ));
        }
        
        // Only allow necessary HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        
        // Only allow necessary headers (not wildcard)
        configuration.setAllowedHeaders(Arrays.asList(
            HttpHeaders.AUTHORIZATION,
            HttpHeaders.CONTENT_TYPE,
            HttpHeaders.ACCEPT,
            HttpHeaders.ORIGIN,
            HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
            HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
            "X-Requested-With"
        ));
        
        // Allow credentials (for cookies if needed in future)
        configuration.setAllowCredentials(true);
        
        // Cache preflight requests for 1 hour
        configuration.setMaxAge(3600L);
        
        // Expose only necessary headers to client
        configuration.setExposedHeaders(Arrays.asList(
            HttpHeaders.AUTHORIZATION,
            HttpHeaders.CONTENT_TYPE
        ));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
