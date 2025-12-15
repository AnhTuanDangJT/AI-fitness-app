package com.aifitness.service;

import com.aifitness.entity.EventType;
import com.aifitness.entity.GamificationEvent;
import com.aifitness.entity.User;
import com.aifitness.repository.GamificationEventRepository;
import com.aifitness.repository.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;

/**
 * Gamification Service
 * 
 * Handles gamification rewards, XP, streaks, and badges.
 * 
 * ============================================================================
 * MANUAL VERIFICATION CHECKLIST
 * ============================================================================
 * 
 * After implementing changes, verify the following scenarios:
 * 
 * 1. XP Never Double-Counts:
 *    - Perform an XP action today (e.g., log weekly progress) → XP increases
 *    - Refresh page → XP remains the same (no duplicate increase)
 *    - Repeat same action today → XP does NOT increase again
 *    - Verify: XP only increases once per unique event
 * 
 * 2. Streaks Survive Refresh & Restart:
 *    - Perform an action today → streak increments (if consecutive day)
 *    - Refresh page → streak remains correct
 *    - Restart backend → streak remains correct after restart
 *    - Verify: lastActivityDate persists correctly in database
 * 
 * 3. Daily Challenges Reset Naturally:
 *    - Complete daily challenge today → XP granted, dailyChallengeCompleted = true
 *    - Try to complete again today → no additional XP (idempotent)
 *    - Change system date to tomorrow → dailyChallengeCompleted = false (new day)
 *    - Complete challenge tomorrow → XP granted again
 *    - Verify: Challenges reset by calendar date (UTC), not by frontend logic
 * 
 * 4. Timezone Safety:
 *    - User in timezone A completes action at 11:59 PM local time
 *    - User in timezone B sees same action at different local time
 *    - Both see correct UTC date-based streaks and challenges
 *    - Verify: All dates use UTC, no timezone-dependent behavior
 * 
 * 5. Streak Logic Correctness:
 *    - First activity ever → streak = 1
 *    - Same day activity → streak unchanged
 *    - Consecutive day activity → streak++
 *    - Skip a day → streak resets to 1
 *    - Verify: longestStreakDays updates ONLY when streak increases
 * 
 * 6. Backend Restart Persistence:
 *    - Perform actions, build up streak
 *    - Restart backend server
 *    - Check gamification status → all data persists correctly
 *    - Verify: Database persistence works correctly
 * 
 * ============================================================================
 */
@Service
@Transactional
public class GamificationService {
    
    private final GamificationEventRepository gamificationEventRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public GamificationService(GamificationEventRepository gamificationEventRepository,
                               UserRepository userRepository,
                               ObjectMapper objectMapper) {
        this.gamificationEventRepository = gamificationEventRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Records a gamification event and updates user rewards.
     * 
     * TIMEZONE SAFETY: All dates use UTC to ensure consistency across timezones.
     * 
     * Behavior:
     * - If event already exists -> return without changes
     * - Else save event, add XP, update streak using lastActivityDate:
     *   - null -> streak=1
     *   - same day -> streak unchanged
     *   - consecutive day -> streak++
     *   - gap -> streak=1
     *   - update longestStreakDays ONLY when streak increases
     *   - set lastActivityDate=activityDate ONLY after successful XP grant
     * - Parse badges JSON string into Set<String>, add badges when thresholds crossed, save back as JSON string
     * - Persist user changes.
     * 
     * @param user The user
     * @param type The event type
     * @param sourceId The source ID (e.g., weeklyProgressId, mealPlanId, bodyAnalysisId, or yyyy-MM-dd for daily challenges)
     * @param activityDate The date of the activity (IGNORED - always uses UTC today for date safety)
     */
    public void recordEvent(User user, EventType type, String sourceId, LocalDate activityDate) {
        // TIMEZONE SAFETY: Always use UTC date, never trust frontend-provided dates
        // This ensures consistency across timezones, page refreshes, and backend restarts
        LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);
        
        // Check if event already exists (prevent double-counting)
        // Uniqueness: (user_id, type, sourceId)
        if (gamificationEventRepository.findByUserAndTypeAndSourceId(user, type, sourceId).isPresent()) {
            return; // Event already recorded, skip
        }
        
        // Get XP reward for this event type
        int xpReward = getXpReward(type);
        
        // Update user's XP (before streak update to ensure XP is granted)
        int currentXp = user.getXp();
        user.setXp(currentXp + xpReward);
        
        // Update streak using UTC date
        // This ensures streak logic is date-safe and timezone-independent
        updateStreak(user, todayUtc);
        
        // Update badges
        updateBadges(user);
        
        // Create and save the event (after XP grant to ensure consistency)
        GamificationEvent event = new GamificationEvent(user, type, todayUtc, sourceId);
        gamificationEventRepository.save(event);
        
        // Persist user changes (lastActivityDate is updated in updateStreak)
        userRepository.save(user);
    }
    
    /**
     * Gets XP reward for an event type.
     */
    private int getXpReward(EventType type) {
        return switch (type) {
            case WEEKLY_PROGRESS -> GamificationRules.XP_WEEKLY_PROGRESS;
            case MEAL_PLAN_CREATED -> GamificationRules.XP_MEAL_PLAN_CREATED;
            case BODY_ANALYSIS_CREATED -> GamificationRules.XP_BODY_ANALYSIS_CREATED;
            case DAILY_CHALLENGE_COMPLETED -> GamificationRules.XP_DAILY_CHALLENGE_COMPLETED;
        };
    }
    
    /**
     * Updates user's streak based on lastActivityDate and activityDate.
     * 
     * STRICT DATE COMPARISON LOGIC (all dates in UTC):
     * - lastActivityDate == null → streak = 1 (first activity ever)
     * - activityDate.equals(lastActivityDate) → do NOTHING (same calendar day, no streak change)
     * - activityDate.equals(lastActivityDate.plusDays(1)) → streak++ (consecutive day)
     * - activityDate.isAfter(lastActivityDate.plusDays(1)) → streak = 1 (gap detected, reset)
     * 
     * longestStreakDays updates ONLY when streak increases (not on same day).
     * lastActivityDate persists ONLY after successful XP grant (in recordEvent).
     */
    private void updateStreak(User user, LocalDate activityDate) {
        LocalDate lastActivityDate = user.getLastActivityDate();
        int previousStreak = user.getCurrentStreakDays();
        
        if (lastActivityDate == null) {
            // Case 1: First activity ever → streak = 1
            user.setCurrentStreakDays(1);
        } else if (activityDate.equals(lastActivityDate)) {
            // Case 2: Same calendar day → do NOTHING (streak unchanged)
            // This prevents multiple activities on the same day from affecting streak
            return; // Exit early, no changes needed
        } else if (activityDate.equals(lastActivityDate.plusDays(1))) {
            // Case 3: Consecutive day → streak++
            user.setCurrentStreakDays(user.getCurrentStreakDays() + 1);
        } else if (activityDate.isAfter(lastActivityDate.plusDays(1))) {
            // Case 4: Gap detected (more than 1 day) → streak = 1
            user.setCurrentStreakDays(1);
        } else {
            // Case 5: activityDate is before lastActivityDate (shouldn't happen with UTC)
            // This is a data integrity issue, but we'll reset streak to be safe
            user.setCurrentStreakDays(1);
        }
        
        // Update longestStreakDays ONLY when streak increases
        // This ensures we don't update on same-day activities
        int newStreak = user.getCurrentStreakDays();
        if (newStreak > previousStreak && newStreak > user.getLongestStreakDays()) {
            user.setLongestStreakDays(newStreak);
        }
        
        // Persist lastActivityDate (only after successful streak update)
        // This is called after XP grant in recordEvent, ensuring consistency
        user.setLastActivityDate(activityDate);
    }
    
    /**
     * Updates user's badges based on current XP and streak.
     * 
     * Badge thresholds:
     * - FIRST_LOG: First activity (already handled by streak logic)
     * - STREAK_3: 3 day streak
     * - STREAK_7: 7 day streak
     * - STREAK_30: 30 day streak
     * - XP_100: 100 XP
     * - XP_500: 500 XP
     */
    private void updateBadges(User user) {
        // Parse badges JSON string into Set
        Set<String> badges = parseBadges(user.getBadges());
        boolean badgesChanged = false;
        
        // Check streak badges
        int currentStreak = user.getCurrentStreakDays();
        if (currentStreak >= GamificationRules.STREAK_THRESHOLD_3 && !badges.contains(GamificationRules.BADGE_STREAK_3)) {
            badges.add(GamificationRules.BADGE_STREAK_3);
            badgesChanged = true;
        }
        if (currentStreak >= GamificationRules.STREAK_THRESHOLD_7 && !badges.contains(GamificationRules.BADGE_STREAK_7)) {
            badges.add(GamificationRules.BADGE_STREAK_7);
            badgesChanged = true;
        }
        if (currentStreak >= GamificationRules.STREAK_THRESHOLD_30 && !badges.contains(GamificationRules.BADGE_STREAK_30)) {
            badges.add(GamificationRules.BADGE_STREAK_30);
            badgesChanged = true;
        }
        
        // Check XP badges
        int currentXp = user.getXp();
        if (currentXp >= GamificationRules.XP_THRESHOLD_100 && !badges.contains(GamificationRules.BADGE_XP_100)) {
            badges.add(GamificationRules.BADGE_XP_100);
            badgesChanged = true;
        }
        if (currentXp >= GamificationRules.XP_THRESHOLD_500 && !badges.contains(GamificationRules.BADGE_XP_500)) {
            badges.add(GamificationRules.BADGE_XP_500);
            badgesChanged = true;
        }
        
        // Check FIRST_LOG badge (if user has any activity)
        if (user.getLastActivityDate() != null && !badges.contains(GamificationRules.BADGE_FIRST_LOG)) {
            badges.add(GamificationRules.BADGE_FIRST_LOG);
            badgesChanged = true;
        }
        
        // Save badges back as JSON string if changed
        if (badgesChanged) {
            user.setBadges(serializeBadges(badges));
        }
    }
    
    /**
     * Parses badges JSON string into Set<String>.
     */
    private Set<String> parseBadges(String badgesJson) {
        if (badgesJson == null || badgesJson.trim().isEmpty() || badgesJson.equals("[]")) {
            return new HashSet<>();
        }
        
        try {
            return objectMapper.readValue(badgesJson, new TypeReference<Set<String>>() {});
        } catch (Exception e) {
            // If parsing fails, return empty set
            return new HashSet<>();
        }
    }
    
    /**
     * Serializes badges Set<String> into JSON string.
     */
    private String serializeBadges(Set<String> badges) {
        try {
            return objectMapper.writeValueAsString(badges);
        } catch (Exception e) {
            // If serialization fails, return empty array JSON
            return "[]";
        }
    }
    
    /**
     * Records a daily challenge completion event.
     * 
     * IDEMPOTENT: A user can earn DAILY_CHALLENGE XP only ONCE per calendar day.
     * Uses uniqueness constraint: (user_id, type=DAILY_CHALLENGE_COMPLETED, sourceId=yyyy-MM-dd)
     * 
     * @param user The user who completed the challenge
     */
    public void recordDailyChallenge(User user) {
        // Get today's date in UTC (timezone-safe)
        LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);
        
        // Use date string as sourceId for uniqueness: "yyyy-MM-dd"
        // This ensures one XP grant per calendar day
        String todayAsString = todayUtc.toString();
        
        // Record event (will check uniqueness internally)
        // activityDate parameter is ignored - always uses UTC today
        recordEvent(user, EventType.DAILY_CHALLENGE_COMPLETED, todayAsString, todayUtc);
    }
    
    /**
     * Checks if daily challenge was completed today.
     * 
     * @param user The user to check
     * @return true if DAILY_CHALLENGE_COMPLETED event exists for today (UTC), false otherwise
     */
    public boolean isDailyChallengeCompletedToday(User user) {
        LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);
        String todayAsString = todayUtc.toString();
        
        // Check if event exists for today
        return gamificationEventRepository.findByUserAndTypeAndSourceId(
            user, 
            EventType.DAILY_CHALLENGE_COMPLETED, 
            todayAsString
        ).isPresent();
    }
}

