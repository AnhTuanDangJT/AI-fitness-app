-- Database Evidence Collection SQL Queries
-- Run these queries to verify database state

-- ============================================
-- STEP 1: Check user verification data
-- ============================================
SELECT 
    email,
    is_email_verified as email_verified,
    email_verification_code as verification_code,
    email_verification_expires_at as verification_code_expires_at,
    verification_attempts
FROM users
WHERE email = 'dangtuananh04081972@gmail.com';

-- ============================================
-- STEP 2: Verify verification_attempts column exists
-- ============================================

-- For SQLite:
.schema users

-- For PostgreSQL:
-- SELECT column_name, data_type, is_nullable, column_default
-- FROM information_schema.columns 
-- WHERE table_name = 'users' AND column_name = 'verification_attempts';

-- ============================================
-- STEP 3: Check migration status
-- ============================================

-- For Flyway (if using):
SELECT * FROM flyway_schema_history WHERE version = '10';

-- Or check all migrations:
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 10;

-- ============================================
-- STEP 4: Check all users with verification data
-- ============================================
SELECT 
    id,
    email,
    is_email_verified,
    email_verification_code IS NOT NULL as has_code,
    email_verification_expires_at,
    verification_attempts,
    created_at
FROM users
ORDER BY created_at DESC
LIMIT 10;








