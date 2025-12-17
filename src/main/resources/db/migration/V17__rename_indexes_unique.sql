-- Ensure all indexes have globally unique names so Flyway fully owns schema/index management

-- Rename legacy generic indexes on weekly_progress to table-specific names
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class idx
        JOIN pg_index i ON idx.oid = i.indexrelid
        JOIN pg_class tbl ON tbl.oid = i.indrelid
        WHERE idx.relname = 'idx_user_week'
          AND tbl.relname = 'weekly_progress'
    ) THEN
        EXECUTE 'ALTER INDEX idx_user_week RENAME TO idx_weekly_progress_user_week';
    END IF;
END $$;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM pg_class idx
        JOIN pg_index i ON idx.oid = i.indexrelid
        JOIN pg_class tbl ON tbl.oid = i.indrelid
        WHERE idx.relname = 'idx_user_created'
          AND tbl.relname = 'weekly_progress'
    ) THEN
        EXECUTE 'ALTER INDEX idx_user_created RENAME TO idx_weekly_progress_user_created';
    END IF;
END $$;

-- Recreate or backfill indexes with unique names across all tables
CREATE INDEX IF NOT EXISTS idx_weekly_progress_user_week ON weekly_progress(user_id, week_start_date);
CREATE INDEX IF NOT EXISTS idx_weekly_progress_user_created ON weekly_progress(user_id, created_at);
CREATE INDEX IF NOT EXISTS idx_meal_plans_user_week ON meal_plans(user_id, week_start_date);
CREATE INDEX IF NOT EXISTS idx_meal_plans_user_created ON meal_plans(user_id, created_at);
CREATE INDEX IF NOT EXISTS idx_daily_checkins_user_created ON daily_checkins(user_id, created_at);
CREATE INDEX IF NOT EXISTS idx_gamification_events_user_created ON gamification_events(user_id, created_at);

