-- Migration: Reset All User Data (V2 Relaunch)
-- Version: 15
-- Description: Complete wipe of all user accounts and related data for v2 relaunch
-- WARNING: This is an intentional reset - all user data will be permanently deleted
-- This migration is identical to V14 but created for the v2 relaunch

-- Delete in order to respect foreign key constraints
-- Note: PostgreSQL enforces foreign keys, so order matters

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
-- The JWT_SECRET environment variable should be updated in production to a new secure value

