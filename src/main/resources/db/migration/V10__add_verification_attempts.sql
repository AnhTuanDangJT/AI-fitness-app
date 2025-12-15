-- Migration: Add verification attempts field
-- Version: 10
-- Description: Add field to track verification attempts for rate limiting

ALTER TABLE users ADD COLUMN verification_attempts INTEGER NOT NULL DEFAULT 0;


