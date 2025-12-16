-- Migration: Add email verification fields
-- Version: 3
-- Description: Add fields for email verification functionality

ALTER TABLE users ADD COLUMN is_email_verified BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE users ADD COLUMN email_verification_code VARCHAR(255);
ALTER TABLE users ADD COLUMN email_verification_expires_at TIMESTAMP WITHOUT TIME ZONE;

-- Create index for faster lookups on email verification
CREATE INDEX IF NOT EXISTS idx_email_verification_code ON users(email_verification_code);












