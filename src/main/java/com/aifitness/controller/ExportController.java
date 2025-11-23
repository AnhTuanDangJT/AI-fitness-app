package com.aifitness.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Export Controller
 * 
 * Handles profile export functionality (PDF, TXT).
 * 
 * LOGIC TO MOVE FROM mainOne.java:
 * - createFile() method (lines 54-64)
 *   Creates a text file with client profile information
 *   This uses Infoclient.java toString() method for formatting
 * 
 * - Export option from sign-in menu (mainOne.java lines 315-329)
 * - Export option when logging out (mainOne.java lines 536-542)
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "http://localhost:3000")
public class ExportController {
    
    /**
     * GET /api/user/export/pdf
     * 
     * Downloads user profile as PDF.
     * 
     * This extends the createFile() functionality from mainOne.java
     * to support PDF format in addition to text files.
     */
    @GetMapping("/export/pdf")
    public ResponseEntity<?> exportToPDF() {
        // TODO: Implement PDF export
        // 1. Get current user profile
        // 2. Format using toString() logic from Infoclient.java
        // 3. Convert to PDF
        // 4. Return as downloadable file
        
        return ResponseEntity.ok("PDF export endpoint - to be implemented");
    }
    
    /**
     * GET /api/user/export/txt
     * 
     * Downloads user profile as text file.
     * 
     * LOGIC TO MOVE FROM mainOne.java:
     * - createFile() method (lines 54-64)
     *   Creates file: [username].txt with client.toString() content
     */
    @GetMapping("/export/txt")
    public ResponseEntity<?> exportToTXT() {
        // TODO: Implement text file export
        // Use createFile() logic from mainOne.java
        // Return file as downloadable attachment
        
        return ResponseEntity.ok("TXT export endpoint - to be implemented");
    }
}

