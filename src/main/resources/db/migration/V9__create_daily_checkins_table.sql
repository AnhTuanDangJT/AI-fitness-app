-- Migration: Create daily_checkins table
-- Version: 9
-- Description: Create table for daily check-in logs used by AI Coach

CREATE TABLE IF NOT EXISTS daily_checkins (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL,
    date DATE NOT NULL,
    weight REAL,
    steps INTEGER,
    workout_done BOOLEAN NOT NULL DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    
    -- Foreign key constraint
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Unique constraint: one check-in per user per date
    CONSTRAINT unique_user_date UNIQUE (user_id, date)
);

-- Create indexes for faster lookups
CREATE INDEX IF NOT EXISTS idx_user_date ON daily_checkins(user_id, date);
CREATE INDEX IF NOT EXISTS idx_user_created ON daily_checkins(user_id, created_at);




