-- Migration: Ensure daily_checkins table exists in PostgreSQL
-- Version: 15
-- Description: Create minimal daily_checkins table if missing

CREATE TABLE IF NOT EXISTS daily_checkins (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    check_in_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

