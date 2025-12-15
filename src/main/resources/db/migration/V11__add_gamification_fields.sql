-- Add gamification fields to users table
ALTER TABLE users ADD COLUMN xp INTEGER NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN current_streak_days INTEGER NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN longest_streak_days INTEGER NOT NULL DEFAULT 0;
ALTER TABLE users ADD COLUMN badges TEXT DEFAULT '[]';
ALTER TABLE users ADD COLUMN last_activity_date DATE;


