-- Migration: Reset All User Data
-- Version: 14
-- Description: Complete wipe of all user accounts and related data for fresh relaunch
-- WARNING: This is an intentional reset - all user data will be permanently deleted

-- Delete in order to respect foreign key constraints (SQLite foreign keys may not be enforced, but order matters for data integrity)

-- 1. Delete meal plan entries first (child of meal_plans)
DELETE FROM meal_plan_entries;

-- 2. Delete meal plans (child of users)
DELETE FROM meal_plans;

-- 3. Delete gamification events (child of users)
DELETE FROM gamification_events;

-- 4. Delete weekly progress (child of users)
DELETE FROM weekly_progress;

-- 5. Delete daily check-ins (child of users)
DELETE FROM daily_checkins;

-- 6. Finally, delete all users (this will remove all user accounts)
DELETE FROM users;

-- Note: After this migration, the JWT secret should be rotated to invalidate all existing tokens
-- Update the JWT_SECRET environment variable or application.properties to a new value








