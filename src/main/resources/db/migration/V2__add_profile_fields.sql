-- Migration: Add profile fields to users table
-- Version: 2
-- Description: Add profile fields from Infoclient.java to users table

ALTER TABLE users ADD COLUMN name VARCHAR(100);
ALTER TABLE users ADD COLUMN age INTEGER;
ALTER TABLE users ADD COLUMN sex BOOLEAN;
ALTER TABLE users ADD COLUMN weight REAL;
ALTER TABLE users ADD COLUMN height REAL;
ALTER TABLE users ADD COLUMN waist REAL;
ALTER TABLE users ADD COLUMN hip REAL;
ALTER TABLE users ADD COLUMN activity_level INTEGER;
ALTER TABLE users ADD COLUMN calorie_goal INTEGER;

