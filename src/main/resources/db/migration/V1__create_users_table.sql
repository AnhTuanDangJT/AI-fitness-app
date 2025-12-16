-- Migration: Create users table
-- Version: 1
-- Description: Initial users table with required fields

CREATE TABLE IF NOT EXISTS users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Create indexes for faster lookups
CREATE INDEX IF NOT EXISTS idx_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_email ON users(email);

