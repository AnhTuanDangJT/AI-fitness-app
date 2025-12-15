-- Migration: Add meal preference fields to users table
-- Version: 8
-- Description: Add preferred foods, allergies, and favorite cuisines

ALTER TABLE users ADD COLUMN preferred_foods VARCHAR(500);
ALTER TABLE users ADD COLUMN allergies VARCHAR(500);
ALTER TABLE users ADD COLUMN favorite_cuisines VARCHAR(500);






