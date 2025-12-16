-- Migration: Harmonize daily_checkins table with JPA entity
-- Version: 16
-- Description: Ensures daily_checkins has all required columns, constraints, and indexes

-- 1. Create table if it doesn't exist yet
CREATE TABLE IF NOT EXISTS daily_checkins (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    date DATE NOT NULL,
    weight DOUBLE PRECISION,
    steps INTEGER,
    workout_done BOOLEAN NOT NULL DEFAULT FALSE,
    notes TEXT,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT fk_daily_checkins_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_date UNIQUE (user_id, date)
);

-- 2. Rename legacy column if table was created manually (check_in_time -> created_at)
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'daily_checkins' AND column_name = 'check_in_time'
    ) THEN
        ALTER TABLE daily_checkins RENAME COLUMN check_in_time TO created_at;
    END IF;
END $$;

-- 3. Ensure all columns exist with correct types/defaults
ALTER TABLE daily_checkins
    ALTER COLUMN user_id TYPE BIGINT USING user_id::BIGINT,
    ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE daily_checkins
    ADD COLUMN IF NOT EXISTS date DATE NOT NULL DEFAULT CURRENT_DATE,
    ADD COLUMN IF NOT EXISTS weight DOUBLE PRECISION,
    ADD COLUMN IF NOT EXISTS steps INTEGER,
    ADD COLUMN IF NOT EXISTS workout_done BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS notes TEXT,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP WITHOUT TIME ZONE;

-- Remove temporary default for date once column exists everywhere
ALTER TABLE daily_checkins
    ALTER COLUMN date DROP DEFAULT;

ALTER TABLE daily_checkins
    ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP,
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE daily_checkins
    ALTER COLUMN workout_done SET DEFAULT FALSE,
    ALTER COLUMN workout_done SET NOT NULL;

-- 4. Ensure FK and unique constraint exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_daily_checkins_user'
    ) THEN
        ALTER TABLE daily_checkins
            ADD CONSTRAINT fk_daily_checkins_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'unique_user_date'
    ) THEN
        ALTER TABLE daily_checkins
            ADD CONSTRAINT unique_user_date UNIQUE (user_id, date);
    END IF;
END $$;

-- 5. Create indexes for efficient lookups
CREATE INDEX IF NOT EXISTS idx_user_date ON daily_checkins(user_id, date);
CREATE INDEX IF NOT EXISTS idx_user_created ON daily_checkins(user_id, created_at);

