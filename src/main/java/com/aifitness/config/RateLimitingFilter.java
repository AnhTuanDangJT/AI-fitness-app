package com.aifitness.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limiting Filter
 * 
 * Implements rate limiting for authentication endpoints to prevent brute force attacks.
 * 
 * Rate limits (Production):
 * - /auth/signup: 10 requests per 15 minutes per IP
 * - /auth/login: 10 requests per 15 minutes per IP
 * - /auth/verify-email: 20 requests per 15 minutes per IP
 * - /auth/resend-verification: 5 requests per 15 minutes per IP
 * 
 * Rate limits (Development):
 * - More relaxed limits for local development
 * 
 * Uses Bucket4j token bucket algorithm for efficient rate limiting.
 * 
 * Note: In production, consider using Redis-backed rate limiting for distributed systems.
 */
@Component
@Order(1) // Execute before other filters
public class RateLimitingFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);
    
    private final Environment environment;
    private final boolean isProduction;
    
    // In-memory storage for rate limit buckets (per IP)
    // In production, use Redis or similar distributed cache
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    // Production rate limits - more reasonable for signup
    private static final Bandwidth SIGNUP_LIMIT_PROD = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(15)));
    private static final Bandwidth LOGIN_LIMIT_PROD = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(15)));
    private static final Bandwidth VERIFY_LIMIT_PROD = Bandwidth.classic(20, Refill.intervally(20, Duration.ofMinutes(15)));
    private static final Bandwidth RESEND_VERIFY_LIMIT_PROD = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(15)));
    
    // Development rate limits - more relaxed
    private static final Bandwidth SIGNUP_LIMIT_DEV = Bandwidth.classic(50, Refill.intervally(50, Duration.ofMinutes(15)));
    private static final Bandwidth LOGIN_LIMIT_DEV = Bandwidth.classic(50, Refill.intervally(50, Duration.ofMinutes(15)));
    private static final Bandwidth VERIFY_LIMIT_DEV = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(15)));
    private static final Bandwidth RESEND_VERIFY_LIMIT_DEV = Bandwidth.classic(20, Refill.intervally(20, Duration.ofMinutes(15)));
    
    public RateLimitingFilter(Environment environment) {
        this.environment = environment;
        this.isProduction = environment.getActiveProfiles().length > 0 && 
                           java.util.Arrays.asList(environment.getActiveProfiles()).contains("production");
        logger.info("Rate limiting filter initialized - Environment: {}, Production: {}", 
                   String.join(",", environment.getActiveProfiles()), isProduction);
    }
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization logging is done in constructor
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String path = httpRequest.getRequestURI();
        
        // Only apply rate limiting to auth endpoints
        if (path.startsWith("/api/auth/") || path.startsWith("/auth/")) {
            String clientIp = getClientIpAddress(httpRequest);
            
            // Determine which rate limit to apply and create bucket key
            String bucketKey;
            Bandwidth limit;
            
            if (path.contains("/signup") || path.contains("/register")) {
                bucketKey = clientIp + ":signup";
                limit = isProduction ? SIGNUP_LIMIT_PROD : SIGNUP_LIMIT_DEV;
            } else if (path.contains("/login")) {
                bucketKey = clientIp + ":login";
                limit = isProduction ? LOGIN_LIMIT_PROD : LOGIN_LIMIT_DEV;
            } else if (path.contains("/verify-email")) {
                bucketKey = clientIp + ":verify";
                limit = isProduction ? VERIFY_LIMIT_PROD : VERIFY_LIMIT_DEV;
            } else if (path.contains("/resend-verification")) {
                bucketKey = clientIp + ":resend";
                limit = isProduction ? RESEND_VERIFY_LIMIT_PROD : RESEND_VERIFY_LIMIT_DEV;
            } else {
                // For other auth endpoints, use login limit
                bucketKey = clientIp + ":other";
                limit = isProduction ? LOGIN_LIMIT_PROD : LOGIN_LIMIT_DEV;
            }
            
            // Get or create bucket for this IP+endpoint combination
            Bucket bucket = buckets.computeIfAbsent(bucketKey, k -> {
                logger.debug("Creating new rate limit bucket for IP: {} endpoint: {}", clientIp, bucketKey);
                return Bucket.builder()
                    .addLimit(limit)
                    .build();
            });
            
            // Try to consume a token
            if (!bucket.tryConsume(1)) {
                logger.warn("Rate limit exceeded for IP: {} on path: {} (bucket: {})", clientIp, path, bucketKey);
                
                httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write(
                    "{\"success\":false,\"message\":\"Too many requests. Please try again later.\",\"timestamp\":\"" +
                    java.time.Instant.now().toString() + "\"}"
                );
                return;
            }
            
            // Log first request for debugging (only in production to avoid spam)
            if (isProduction) {
                logger.debug("Rate limit check passed for IP: {} on path: {}", clientIp, path);
            }
        }
        
        // Continue with the filter chain
        chain.doFilter(request, response);
    }
    
    /**
     * Extracts the client IP address from the request.
     * Handles proxy headers (X-Forwarded-For, X-Real-IP) for deployments behind load balancers.
     * 
     * With server.forward-headers-strategy=framework enabled, Spring Boot will automatically
     * process X-Forwarded-For headers and set request.getRemoteAddr() to the real client IP.
     * However, we still check headers directly as a fallback for compatibility.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = null;
        
        // First, try X-Forwarded-For header (most common in production)
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(forwardedFor)) {
            // X-Forwarded-For can contain multiple IPs: "client, proxy1, proxy2"
            // Take the first one (original client)
            ip = forwardedFor.split(",")[0].trim();
        }
        
        // Fallback to X-Real-IP header
        if (ip == null || ip.isEmpty()) {
            String realIp = request.getHeader("X-Real-IP");
            if (realIp != null && !realIp.isEmpty() && !"unknown".equalsIgnoreCase(realIp)) {
                ip = realIp.trim();
            }
        }
        
        // Final fallback: use getRemoteAddr()
        // With forward-headers-strategy=framework, this should already contain the real client IP
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        
        // Log IP detection for debugging (only in production, and only occasionally to avoid spam)
        if (isProduction && logger.isDebugEnabled()) {
            logger.debug("IP detection - X-Forwarded-For: {}, X-Real-IP: {}, RemoteAddr: {}, Final IP: {}", 
                        request.getHeader("X-Forwarded-For"), 
                        request.getHeader("X-Real-IP"), 
                        request.getRemoteAddr(), 
                        ip);
        }
        
        return ip != null && !ip.isEmpty() ? ip : "unknown";
    }
    
    @Override
    public void destroy() {
        buckets.clear();
        logger.info("Rate limiting filter destroyed");
    }
}

