-- README:
-- This script is for manual admin use only. Run it intentionally via psql or the Railway
-- PostgreSQL console when you explicitly need to wipe every user account. Executing it will
-- permanently delete all users and their related records; the operation is irreversible.

BEGIN;

-- 1. Remove meal plan entries first (child table of meal_plans).
TRUNCATE TABLE meal_plan_entries RESTART IDENTITY;

-- 2. Remove meal plans (references users and owns meal_plan_entries, hence CASCADE).
TRUNCATE TABLE meal_plans RESTART IDENTITY CASCADE;

-- 3. Remove gamification audit trail tied to users.
TRUNCATE TABLE gamification_events RESTART IDENTITY;

-- 4. Remove weekly progress snapshots for every user.
TRUNCATE TABLE weekly_progress RESTART IDENTITY;

-- 5. Remove daily check-ins before clearing the parent users.
TRUNCATE TABLE daily_checkins RESTART IDENTITY;

-- 6. Finally delete all user accounts (includes embedded verification codes and profile data).
DELETE FROM users;

-- 7. Reset the users identity sequence manually (TRUNCATE RESTART handles the others).
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class
        WHERE relkind = 'S'
          AND relname = 'users_id_seq'
    ) THEN
        EXECUTE 'ALTER SEQUENCE users_id_seq RESTART WITH 1';
    END IF;
END $$;

COMMIT;








