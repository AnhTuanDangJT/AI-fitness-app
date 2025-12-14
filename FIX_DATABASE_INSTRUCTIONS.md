# Fix Database Schema Issue

## Problem
The SQLite database is missing the email verification columns:
- `is_email_verified`
- `email_verification_code`
- `email_verification_expires_at`

Error: `[SQLITE_ERROR] SQL error or missing database (table users has no column named is_email_verified)`

## Solution

### Step 1: Stop the Spring Boot Backend
- Go to the terminal where Spring Boot is running
- Press `Ctrl+C` to stop the backend
- Wait until the process fully stops

### Step 2: Delete the Database File
Run this command in PowerShell:
```powershell
Remove-Item "aifitness.db" -Force
```

Or manually delete the file: `aifitness.db` in the project root

### Step 3: Verify Configuration
The following is already configured in `application.properties`:
```properties
spring.jpa.hibernate.ddl-auto=update
```

This will automatically recreate the database schema when the backend starts.

### Step 4: Restart the Backend
Start the backend again:
```bash
mvn spring-boot:run
```
or
```bash
java -jar target/aifitness-backend-1.0.0.jar
```

### Step 5: Test Signup
- The database will be automatically created with all required fields
- Try signup again from the frontend
- It should work now!

## What Happens
When Hibernate starts with `ddl-auto=update`, it will:
1. Detect that the database doesn't exist
2. Create the `users` table with ALL fields from the `User` entity, including:
   - `is_email_verified` (BOOLEAN, NOT NULL, DEFAULT false)
   - `email_verification_code` (VARCHAR(255))
   - `email_verification_expires_at` (TIMESTAMP)

## Verification
After restart, check the backend logs for:
- `Hibernate: create table users`
- The table creation SQL should include all email verification fields







