# Database Reset Instructions

## Overview
This document describes the complete database reset process that wipes all existing user accounts and forces users to create new accounts.

## What Gets Deleted

The reset migration (V14) deletes ALL data from these tables:
1. `meal_plan_entries` - All meal plan entries
2. `meal_plans` - All meal plans
3. `gamification_events` - All gamification event history
4. `weekly_progress` - All weekly progress logs
5. `daily_checkins` - All daily check-in records
6. `users` - **ALL USER ACCOUNTS** (complete wipe)

## JWT Token Invalidation

The JWT secret has been rotated to invalidate all existing tokens. The new default secret is:
- Development: `aifitness-secret-key-reset-2025-rotate-this`
- Production: Update `JWT_SECRET` environment variable

**All old authentication tokens will be rejected** - users must log in again with new accounts.

## Running the Reset

### Option 1: Automatic (Recommended)
The reset runs automatically when the application starts if Flyway migrations are enabled (default).

1. Stop the application
2. Ensure Flyway is enabled: `spring.flyway.enabled=true` (default)
3. Start the application
4. Flyway will run migration V14 automatically, deleting all user data

### Option 2: Manual SQL Reset
If you need to reset manually without running migrations:

```sql
-- Run these in order:
DELETE FROM meal_plan_entries;
DELETE FROM meal_plans;
DELETE FROM gamification_events;
DELETE FROM weekly_progress;
DELETE FROM daily_checkins;
DELETE FROM users;
```

Then update the JWT secret in your environment or application.properties.

## Production Deployment

### Step 1: Update JWT Secret
**IMPORTANT**: Update the `JWT_SECRET` environment variable in your hosting platform (Render/Railway/etc.):

1. Generate a new secure secret (at least 32 characters)
2. Update the `JWT_SECRET` environment variable
3. **Do NOT** use the default secret in production

### Step 2: Deploy the Migration
Deploy the application with migration V14 included. The migration will run automatically on startup.

### Step 3: Verify Reset
After deployment:
1. Verify no old users can log in (test with old credentials - should fail)
2. Test that new user registration works
3. Verify all old tokens are rejected (401 Unauthorized)

## Safety Checks

After the reset:
- ✅ Database contains ZERO users
- ✅ All old JWT tokens are invalidated
- ✅ New user registration works
- ✅ New users can complete onboarding
- ✅ No orphaned data in related tables

## Rollback

**WARNING**: This reset is permanent and cannot be rolled back!

If you need to rollback:
1. Restore from a database backup (if available)
2. Revert to the previous JWT secret
3. Remove migration V14 from the codebase

## Notes

- The reset is intentional and part of a fresh product relaunch
- No user data will be preserved
- All visitors must register new accounts
- The app will behave like a brand-new product launch

