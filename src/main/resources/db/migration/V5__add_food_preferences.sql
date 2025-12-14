-- Migration: Add food preferences and constraints to users table
-- Version: 5
-- Description: Add dietary preference, disliked foods, max budget per day, and max cooking time per meal

-- Add food preference fields
ALTER TABLE users ADD COLUMN dietary_preference VARCHAR(50);
ALTER TABLE users ADD COLUMN disliked_foods VARCHAR(500);
ALTER TABLE users ADD COLUMN max_budget_per_day INTEGER;
ALTER TABLE users ADD COLUMN max_cooking_time_per_meal INTEGER;




