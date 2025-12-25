-- Migration: Add preferred_language field to users table
-- Version: 13
-- Description: Add preferred language field (EN/VI) for AI Coach responses

ALTER TABLE users ADD COLUMN preferred_language VARCHAR(2) DEFAULT 'EN' NOT NULL;









