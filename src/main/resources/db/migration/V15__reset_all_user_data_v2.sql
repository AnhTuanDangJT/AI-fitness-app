-- Migration: Hard Reset All User Data (V2 Relaunch)
-- Version: 15
-- Description: COMPLETE HARD RESET - All user accounts and related data permanently deleted
-- WARNING: This is an intentional reset for relaunch - ALL USER DATA WILL BE DELETED
-- 
-- This migration performs a hard delete of:
-- - All user accounts (users table)
-- - All user profiles (stored in users table)
-- - All verification tokens/codes (stored in users table)
-- - All meal plans and entries
-- - All gamification events
-- - All weekly progress records
-- - All daily check-ins
--
-- After this migration:
-- 1. Database will have ZERO users
-- 2. All existing JWT tokens will be invalid (must rotate JWT_SECRET)
-- 3. All users must re-register
-- 4. App starts fresh like a brand-new product

-- Delete in order to respect foreign key constraints
-- Note: PostgreSQL enforces foreign keys, so order matters
-- Foreign keys have ON DELETE CASCADE, but we delete explicitly for clarity

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

-- 6. Finally, delete all users (this removes ALL user accounts, profiles, and verification data)
-- This deletes:
--   - All user accounts
--   - All user profiles (name, age, weight, height, etc.)
--   - All email verification codes
--   - All verification tokens
--   - All user preferences
--   - All gamification data (XP, streaks, badges)
DELETE FROM users;

-- Verify deletion (for PostgreSQL - will show 0 if successful)
-- SELECT COUNT(*) FROM users; -- Should return 0

-- CRITICAL: After this migration runs, rotate JWT_SECRET in production environment
-- This ensures all existing JWT tokens become invalid and users cannot log in with old tokens
-- Update JWT_SECRET environment variable to a new secure random value (minimum 32 characters)

