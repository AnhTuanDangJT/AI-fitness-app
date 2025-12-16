-- Migration: Create daily_checkins table
-- Version: 1
-- Description: Introduces basic table for daily user check-ins

CREATE TABLE daily_checkins (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    check_in_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

