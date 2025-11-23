package com.aifitness.repository;

import com.aifitness.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * User Repository
 * 
 * Provides data access methods for User entity.
 * 
 * Spring Data JPA will automatically implement these methods based on naming conventions.
 * No manual SQL queries needed for basic CRUD operations.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Finds user by username.
     * 
     * Used for login authentication.
     * Spring Data JPA automatically implements this based on method name.
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Finds user by email.
     * 
     * Used for email-based login or account recovery.
     * Spring Data JPA automatically implements this based on method name.
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Checks if username exists.
     * 
     * Used during registration to prevent duplicate usernames.
     */
    boolean existsByUsername(String username);
    
    /**
     * Checks if email exists.
     * 
     * Used during registration to prevent duplicate emails.
     */
    boolean existsByEmail(String email);
    
    // Spring Data JPA provides these methods automatically:
    // - save(User user)
    // - findById(Long id)
    // - findAll()
    // - delete(User user)
    // - deleteById(Long id)
    // - count()
}

