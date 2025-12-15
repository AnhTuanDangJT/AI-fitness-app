-- Migration: Create weekly_progress table
-- Description: Stores weekly progress logs for users to track fitness metrics over time.
-- This data will be used by the AI coach to analyze trends and provide insights.

CREATE TABLE IF NOT EXISTS weekly_progress (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    week_start_date DATE NOT NULL,
    weight REAL,
    sleep_hours_per_night_average INTEGER,
    stress_level INTEGER,
    hunger_level INTEGER,
    energy_level INTEGER,
    training_sessions_completed INTEGER,
    calories_average REAL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE(user_id, week_start_date)
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_user_week ON weekly_progress(user_id, week_start_date);
CREATE INDEX IF NOT EXISTS idx_user_created ON weekly_progress(user_id, created_at);






