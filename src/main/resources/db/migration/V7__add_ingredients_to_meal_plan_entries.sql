-- Migration: Add ingredients column to meal_plan_entries
-- Version: 7
-- Description: Adds ingredients column to store JSON array of ingredients for each meal

ALTER TABLE meal_plan_entries ADD COLUMN ingredients TEXT;




