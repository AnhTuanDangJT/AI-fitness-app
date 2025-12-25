package com.aifitness.repository;

import com.aifitness.entity.EventType;
import com.aifitness.entity.GamificationEvent;
import com.aifitness.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for GamificationEvent entities
 */
@Repository
public interface GamificationEventRepository extends JpaRepository<GamificationEvent, Long> {
    
    /**
     * Finds an event by user, type, and sourceId.
     * Used to check if an event has already been recorded (prevent double-counting).
     * 
     * @param user The user
     * @param type The event type
     * @param sourceId The source ID
     * @return Optional GamificationEvent if found
     */
    Optional<GamificationEvent> findByUserAndTypeAndSourceId(User user, EventType type, String sourceId);
}









