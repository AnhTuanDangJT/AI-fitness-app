-- Migration: Remove legacy duplicate index names and recreate unique per-table indexes
-- Purpose: Ensure Flyway owns all index definitions with globally unique names

DO $$
DECLARE
    idx_name text := 'idx_user_created';
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class c
        JOIN pg_namespace n ON n.oid = c.relnamespace
        WHERE c.relname = idx_name
          AND c.relkind = 'i'
          AND n.nspname = 'public'
    ) THEN
        EXECUTE format('DROP INDEX IF EXISTS %I', idx_name);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_daily_checkins_user_created
    ON daily_checkins (user_id, created_at);

CREATE INDEX IF NOT EXISTS idx_gamification_events_user_created
    ON gamification_events (user_id, created_at);






