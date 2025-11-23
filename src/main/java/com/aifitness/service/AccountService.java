package com.aifitness.service;

import com.aifitness.entity.User;
import com.aifitness.exception.ResourceAlreadyExistsException;
import com.aifitness.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Account Service
 * 
 * Handles user account creation, authentication, and password management.
 * 
 * LOGIC TO MOVE FROM mainOne.java:
 * - Account creation flow (lines 105-330)
 * - Password hashing (will replace EncryptPass method lines 14-32)
 *   - OLD: Custom encryption using tablesign.txt
 *   - NEW: BCrypt password hashing (industry standard)
 * 
 * - Sign-in verification (lines 332-358)
 * - Password verification (will replace DecryptPass method lines 34-52)
 *   - OLD: Custom decryption
 *   - NEW: BCrypt password verification
 * 
 * - Password update with verification (lines 410-449)
 */
@Service
@Transactional
public class AccountService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public AccountService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * Creates a new user account.
     * 
     * LOGIC TO MOVE FROM mainOne.java:
     * - Account creation from menu option 1 (lines 105-330)
     * - User input collection and validation
     * - Password hashing (replacing EncryptPass)
     * - User entity creation (replacing Infoclient creation)
     * 
     * @param username The username for the new account
     * @param email The email address for the new account
     * @param password The plain text password (will be hashed)
     * @return The created User entity
     * @throws ResourceAlreadyExistsException if username or email already exists
     */
    public User registerUser(String username, String email, String password) {
        // Check if username already exists
        if (userRepository.existsByUsername(username)) {
            throw new ResourceAlreadyExistsException("Username already exists: " + username);
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException("Email already exists: " + email);
        }
        
        // Hash password using BCrypt (replaces EncryptPass from mainOne.java)
        String passwordHash = passwordEncoder.encode(password);
        
        // Create new User entity
        User user = new User(username, email, passwordHash);
        
        // Save to database
        // Note: SQLite doesn't support generated keys extraction, so we save and then refresh
        user = userRepository.save(user);
        
        // Refresh the entity to get the generated ID
        // This is needed because SQLite JDBC driver doesn't support getGeneratedKeys()
        if (user.getId() == null) {
            // If ID is null after save, query the user back from database
            user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Failed to create user"));
        }
        
        return user;
    }
    
    /**
     * Authenticates user credentials using username.
     * 
     * LOGIC TO MOVE FROM mainOne.java:
     * - Sign-in verification (lines 349-357)
     * - Password verification (replacing DecryptPass)
     * 
     * @param username The username
     * @param password The plain text password to verify
     * @return The User entity if authentication succeeds, null otherwise
     */
    public User authenticateUserByUsername(String username, String password) {
        // Find user by username
        User user = userRepository.findByUsername(username)
                .orElse(null);
        
        if (user == null) {
            return null;
        }
        
        // Verify password using BCrypt (replaces DecryptPass from mainOne.java)
        if (passwordEncoder.matches(password, user.getPasswordHash())) {
            return user;
        }
        
        return null;
    }
    
    /**
     * Authenticates user credentials using email.
     * 
     * @param email The email address
     * @param password The plain text password to verify
     * @return The User entity if authentication succeeds, null otherwise
     */
    public User authenticateUserByEmail(String email, String password) {
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElse(null);
        
        if (user == null) {
            return null;
        }
        
        // Verify password using BCrypt
        if (passwordEncoder.matches(password, user.getPasswordHash())) {
            return user;
        }
        
        return null;
    }
    
    /**
     * Authenticates user credentials using either username or email.
     * 
     * @param usernameOrEmail The username or email address
     * @param password The plain text password to verify
     * @return The User entity if authentication succeeds
     * @throws com.aifitness.exception.InvalidCredentialsException if credentials are invalid
     */
    public User authenticateUser(String usernameOrEmail, String password) {
        User user = null;
        
        // Determine if usernameOrEmail is an email or username
        if (usernameOrEmail.contains("@")) {
            // Try email first
            user = authenticateUserByEmail(usernameOrEmail, password);
        } else {
            // Try username first
            user = authenticateUserByUsername(usernameOrEmail, password);
            
            // If not found, try email as fallback (in case username contains @)
            if (user == null) {
                user = authenticateUserByEmail(usernameOrEmail, password);
            }
        }
        
        if (user == null) {
            throw new com.aifitness.exception.InvalidCredentialsException(
                "Invalid username/email or password"
            );
        }
        
        return user;
    }
    
    /**
     * Updates user password with current password verification.
     * 
     * LOGIC TO MOVE FROM mainOne.java:
     * - Password update flow (lines 410-449)
     * - Current password verification
     * - New password hashing (replacing EncryptPass)
     */
    public void updatePassword(/* TODO: Add parameters */) {
        // TODO: Implement password update
        // 1. Verify current password
        // 2. Hash new password with BCrypt
        // 3. Update in database
    }
    
    // NOTE: The following methods from mainOne.java will be REMOVED:
    // - EncryptPass() - Replaced with BCrypt
    // - DecryptPass() - Replaced with BCrypt verification
    // - OrginalTableSign() - No longer needed (tablesign.txt not used)
}

