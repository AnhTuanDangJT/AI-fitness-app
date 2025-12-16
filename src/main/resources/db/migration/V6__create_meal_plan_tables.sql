-- Migration: Create meal plan tables
-- Version: 6
-- Description: Creates meal_plans and meal_plan_entries tables for storing weekly meal plans

-- Create meal_plans table
CREATE TABLE IF NOT EXISTS meal_plans (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    week_start_date DATE NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Create meal_plan_entries table
CREATE TABLE IF NOT EXISTS meal_plan_entries (
    id BIGSERIAL PRIMARY KEY,
    meal_plan_id BIGINT NOT NULL,
    date DATE NOT NULL,
    meal_type VARCHAR(20) NOT NULL,
    name VARCHAR(200) NOT NULL,
    calories INTEGER NOT NULL,
    protein INTEGER NOT NULL,
    carbs INTEGER NOT NULL,
    fats INTEGER NOT NULL,
    FOREIGN KEY (meal_plan_id) REFERENCES meal_plans(id) ON DELETE CASCADE
);

-- Create indexes for better query performance
CREATE INDEX IF NOT EXISTS idx_user_week ON meal_plans(user_id, week_start_date);
CREATE INDEX IF NOT EXISTS idx_user_created ON meal_plans(user_id, created_at);
CREATE INDEX IF NOT EXISTS idx_meal_plan_date ON meal_plan_entries(meal_plan_id, date);
CREATE INDEX IF NOT EXISTS idx_meal_plan_type ON meal_plan_entries(meal_plan_id, meal_type);









