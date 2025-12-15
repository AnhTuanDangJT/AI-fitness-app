package com.aifitness.service;

/**
 * Gamification Rules Constants
 * 
 * Defines XP rewards and badge thresholds for gamification system.
 */
public class GamificationRules {
    
    /**
     * XP Rewards for different event types
     */
    public static final int XP_WEEKLY_PROGRESS = 25;
    public static final int XP_MEAL_PLAN_CREATED = 15;
    public static final int XP_BODY_ANALYSIS_CREATED = 20;
    public static final int XP_DAILY_CHALLENGE_COMPLETED = 10;
    
    /**
     * Badge Thresholds
     */
    public static final String BADGE_FIRST_LOG = "FIRST_LOG";
    public static final String BADGE_STREAK_3 = "STREAK_3";
    public static final String BADGE_STREAK_7 = "STREAK_7";
    public static final String BADGE_STREAK_30 = "STREAK_30";
    public static final String BADGE_XP_100 = "XP_100";
    public static final String BADGE_XP_500 = "XP_500";
    
    /**
     * Badge threshold values
     */
    public static final int STREAK_THRESHOLD_3 = 3;
    public static final int STREAK_THRESHOLD_7 = 7;
    public static final int STREAK_THRESHOLD_30 = 30;
    public static final int XP_THRESHOLD_100 = 100;
    public static final int XP_THRESHOLD_500 = 500;
    
    private GamificationRules() {
        // Utility class - prevent instantiation
    }
}

