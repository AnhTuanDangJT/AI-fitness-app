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
 * Rate limits:
 * - /auth/signup: 5 requests per hour per IP
 * - /auth/login: 10 requests per 15 minutes per IP
 * - /auth/register: 5 requests per hour per IP (alias for signup)
 * 
 * Uses Bucket4j token bucket algorithm for efficient rate limiting.
 * 
 * Note: In production, consider using Redis-backed rate limiting for distributed systems.
 */
@Component
@Order(1) // Execute before other filters
public class RateLimitingFilter implements Filter {
    
    private static final Logger logger = LoggerFactory.getLogger(RateLimitingFilter.class);
    
    // In-memory storage for rate limit buckets (per IP)
    // In production, use Redis or similar distributed cache
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    
    // Rate limit for signup/register endpoints: 5 requests per hour
    private static final Bandwidth SIGNUP_LIMIT = Bandwidth.classic(5, Refill.intervally(5, Duration.ofHours(1)));
    
    // Rate limit for login endpoint: 10 requests per 15 minutes
    private static final Bandwidth LOGIN_LIMIT = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(15)));
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("Rate limiting filter initialized");
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
                limit = SIGNUP_LIMIT;
            } else if (path.contains("/login")) {
                bucketKey = clientIp + ":login";
                limit = LOGIN_LIMIT;
            } else {
                // For other auth endpoints, use login limit
                bucketKey = clientIp + ":other";
                limit = LOGIN_LIMIT;
            }
            
            // Get or create bucket for this IP+endpoint combination
            Bucket bucket = buckets.computeIfAbsent(bucketKey, k -> {
                return Bucket.builder()
                    .addLimit(limit)
                    .build();
            });
            
            // Try to consume a token
            if (!bucket.tryConsume(1)) {
                logger.warn("Rate limit exceeded for IP: {} on path: {}", clientIp, path);
                
                httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                httpResponse.setContentType("application/json");
                httpResponse.getWriter().write(
                    "{\"success\":false,\"message\":\"Too many requests. Please try again later.\",\"timestamp\":\"" +
                    java.time.Instant.now().toString() + "\"}"
                );
                return;
            }
        }
        
        // Continue with the filter chain
        chain.doFilter(request, response);
    }
    
    /**
     * Extracts the client IP address from the request.
     * Handles proxy headers (X-Forwarded-For, X-Real-IP) for deployments behind load balancers.
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // If X-Forwarded-For contains multiple IPs, take the first one
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip != null ? ip : "unknown";
    }
    
    @Override
    public void destroy() {
        buckets.clear();
        logger.info("Rate limiting filter destroyed");
    }
}

