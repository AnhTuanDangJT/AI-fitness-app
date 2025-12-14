package com.aifitness;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

/**
 * Main Spring Boot Application Entry Point
 * 
 * This is the server start file. When you run this class, it will start the Spring Boot server
 * on port 8080 (configured in application.properties).
 * 
 * The server will be available at: http://localhost:8080/api
 */
@SpringBootApplication
public class AiFitnessApplication {

    @Value("${spring.datasource.url}")
    private String databaseUrl;

    public static void main(String[] args) {
        SpringApplication.run(AiFitnessApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        System.out.println("========================================");
        System.out.println("AI Fitness Backend Server Started!");
        System.out.println("Server running at: http://localhost:8080/api");
        System.out.println("Database URL: " + databaseUrl);
        System.out.println("========================================");
    }
}

