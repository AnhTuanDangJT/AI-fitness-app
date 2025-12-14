package com.aifitness.service;

import com.aifitness.entity.User;
import com.aifitness.exception.ResourceAlreadyExistsException;
import com.aifitness.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

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
    
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);
    private static final int VERIFICATION_CODE_LENGTH = 6;
    private static final int VERIFICATION_CODE_EXPIRY_MINUTES = 10; // Changed to 10 minutes as per requirements
    private static final int MAX_VERIFICATION_ATTEMPTS = 5;
    private static final SecureRandom random = new SecureRandom();
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    @Autowired
    public AccountService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
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
        user.setIsEmailVerified(false);
        
        // Generate verification code
        String verificationCode = generateVerificationCode();
        user.setEmailVerificationCode(verificationCode);
        user.setEmailVerificationExpiresAt(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRY_MINUTES));
        user.setVerificationAttempts(0); // Reset attempts on new code
        
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
        
        // Send verification email
        try {
            emailService.sendVerificationEmail(email, verificationCode);
        } catch (Exception e) {
            logger.error("Failed to send verification email during signup", e);
            // Don't fail signup if email fails, but log the error
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
     * @throws com.aifitness.exception.InvalidCredentialsException if credentials are invalid or email not verified
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
        
        // Check if email is verified
        if (!user.getIsEmailVerified()) {
            throw new com.aifitness.exception.InvalidCredentialsException(
                "Please verify your email first"
            );
        }
        
        return user;
    }
    
    /**
     * Generates a 6-digit numeric verification code.
     * 
     * @return A 6-digit verification code as a string
     */
    private String generateVerificationCode() {
        int code = 100000 + random.nextInt(900000); // Generates a number between 100000 and 999999
        return String.valueOf(code);
    }
    
    /**
     * Verifies the email verification code for a user.
     * 
     * @param email The user's email address
     * @param code The verification code to check
     * @return The User entity if verification succeeds
     * @throws com.aifitness.exception.UserNotFoundException if user not found (404)
     * @throws com.aifitness.exception.CodeExpiredException if code expired (410)
     * @throws com.aifitness.exception.TooManyAttemptsException if too many attempts (429)
     * @throws com.aifitness.exception.InvalidCodeException if code is invalid (400)
     */
    public User verifyEmail(String email, String code) {
        logger.info("Verification attempt - Email: {}, Code length: {}", email, code != null ? code.length() : 0);
        
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Verification failed - User not found: {}", email);
                    return new com.aifitness.exception.UserNotFoundException("User not found");
                });
        
        logger.debug("User found - ID: {}, Email verified: {}, Code present: {}, Expires at: {}, Attempts: {}", 
            user.getId(), 
            user.getIsEmailVerified(),
            user.getEmailVerificationCode() != null,
            user.getEmailVerificationExpiresAt(),
            user.getVerificationAttempts());
        
        // Check if already verified
        if (user.getIsEmailVerified()) {
            logger.info("Email already verified for user: {}", email);
            return user;
        }
        
        // Check if too many attempts
        if (user.getVerificationAttempts() >= MAX_VERIFICATION_ATTEMPTS) {
            logger.warn("Verification failed - Too many attempts for user: {}, Attempts: {}", 
                email, user.getVerificationAttempts());
            throw new com.aifitness.exception.TooManyAttemptsException(
                "Too many verification attempts. Please request a new code."
            );
        }
        
        // Check if code has expired
        if (user.getEmailVerificationExpiresAt() == null ||
            user.getEmailVerificationExpiresAt().isBefore(LocalDateTime.now())) {
            logger.warn("Verification failed - Code expired for user: {}, Expires at: {}", 
                email, user.getEmailVerificationExpiresAt());
            throw new com.aifitness.exception.CodeExpiredException(
                "Verification code has expired. Please request a new code."
            );
        }
        
        // Validate code
        if (user.getEmailVerificationCode() == null || 
            !user.getEmailVerificationCode().equals(code)) {
            // Increment attempts
            int attempts = user.getVerificationAttempts() + 1;
            user.setVerificationAttempts(attempts);
            userRepository.save(user);
            
            logger.warn("Verification failed - Invalid code for user: {}, Attempts: {}", 
                email, attempts);
            throw new com.aifitness.exception.InvalidCodeException(
                "Invalid verification code. Attempts remaining: " + (MAX_VERIFICATION_ATTEMPTS - attempts)
            );
        }
        
        // Code is valid - verify email
        logger.info("Verification successful for user: {}", email);
        user.setIsEmailVerified(true);
        user.setEmailVerificationCode(null);
        user.setEmailVerificationExpiresAt(null);
        user.setVerificationAttempts(0); // Reset attempts on success
        
        user = userRepository.save(user);
        
        return user;
    }
    
    /**
     * Resends the verification code to the user's email.
     * 
     * @param email The user's email address
     * @return The User entity
     * @throws com.aifitness.exception.UserNotFoundException if user not found (404)
     * @throws com.aifitness.exception.EmailServiceException if email service fails (503)
     */
    public User resendVerificationCode(String email) {
        logger.info("Resend verification code request - Email: {}", email);
        
        // Find user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Resend verification failed - User not found: {}", email);
                    return new com.aifitness.exception.UserNotFoundException("User not found");
                });
        
        logger.debug("User found - ID: {}, Email verified: {}", user.getId(), user.getIsEmailVerified());
        
        // Check if already verified
        if (user.getIsEmailVerified()) {
            logger.info("Email already verified for user: {}", email);
            return user;
        }
        
        // Generate new verification code
        String verificationCode = generateVerificationCode();
        user.setEmailVerificationCode(verificationCode);
        user.setEmailVerificationExpiresAt(LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRY_MINUTES));
        user.setVerificationAttempts(0); // Reset attempts when resending
        
        user = userRepository.save(user);
        
        logger.debug("New verification code generated for user: {}, Expires at: {}", 
            email, user.getEmailVerificationExpiresAt());
        
        // Send verification email
        try {
            emailService.sendVerificationEmail(email, verificationCode);
            logger.info("Verification email sent successfully to: {}", email);
        } catch (Exception e) {
            logger.error("Failed to send verification email during resend to: {}", email, e);
            throw new com.aifitness.exception.EmailServiceException("Email service not configured", e);
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

