# 🔍 RUNTIME EVIDENCE COLLECTION GUIDE

This guide will help you collect all runtime evidence for the Verify Email error.

## STEP 1 — FRONTEND NETWORK TRACE

### Manual Browser Method (Most Accurate)

1. **Start the frontend:**
   ```bash
   cd client
   npm run dev
   ```

2. **Open browser DevTools:**
   - Press `F12` or `Ctrl+Shift+I`
   - Go to **Network** tab
   - Check "Preserve log"

3. **Navigate to Verify Email page:**
   - Go to: `http://localhost:3000/verify-email`
   - Enter email: `dangtuananh04081972@gmail.com`
   - Enter any 6-digit code (e.g., `123456`)

4. **Click Verify button**

5. **In Network tab, find the request:**
   - Look for: `verify-email` or `auth/verify-email`
   - Click on it

6. **Capture these details:**
   - **Request URL:** (Full URL from Headers tab)
   - **Request Method:** (should be POST)
   - **Request Payload:** (from Payload tab or Request tab)
   - **Response Status:** (e.g., 500, 400, etc.)
   - **Response Body:** (from Response tab - copy the raw JSON)

7. **Check Console tab:**
   - Look for `console.log` output from VerifyEmail.jsx
   - Copy all error messages

8. **Check VITE_API_BASE_URL:**
   - In Console tab, run: `console.log(import.meta.env.VITE_API_BASE_URL)`
   - Or check: `client/.env` or `client/.env.local`

### Automated Method (Using Script)

Run the automated collection script:
```bash
node collect-frontend-evidence.js
```

---

## STEP 2 — BACKEND LOG TRACE

### Method 1: Check Log Files

1. **Find log file:**
   ```bash
   # Windows PowerShell
   Get-Content logs\spring-boot.log -Tail 100
   
   # Or if running in terminal, check console output
   ```

2. **Look for these log entries:**
   - `Verify email request received`
   - `Verification attempt`
   - `Email verification failed`
   - Any exception stack traces

3. **Capture:**
   - Full stack trace (not truncated)
   - Exception class name
   - Line number where it fails
   - Log lines immediately BEFORE the exception

### Method 2: Real-time Log Monitoring

1. **Start backend with verbose logging:**
   ```bash
   # Backend should already be logging to console
   # Watch for new log entries when you trigger verify
   ```

2. **Trigger verify request** (from frontend or script)

3. **Immediately capture console output**

### Method 3: Check Railway/Render Logs

If deployed to production:
- Go to Railway/Render dashboard
- Check deployment logs
- Look for recent error entries

---

## STEP 3 — DATABASE VERIFICATION

### SQLite (Development)

```bash
# Connect to database
sqlite3 aifitness.db

# Run query
SELECT email,
       is_email_verified as email_verified,
       email_verification_code as verification_code,
       email_verification_expires_at as verification_code_expires_at,
       verification_attempts
FROM users
WHERE email = 'dangtuananh04081972@gmail.com';

# Check if column exists
.schema users

# Check migration status
SELECT * FROM flyway_schema_history WHERE version = '10';
```

### PostgreSQL (Production)

If using PostgreSQL, connect via Railway/Render database console and run:

```sql
SELECT email,
       is_email_verified as email_verified,
       email_verification_code as verification_code,
       email_verification_expires_at as verification_code_expires_at,
       verification_attempts
FROM users
WHERE email = 'dangtuananh04081972@gmail.com';

-- Check if column exists
SELECT column_name, data_type 
FROM information_schema.columns 
WHERE table_name = 'users' AND column_name = 'verification_attempts';

-- Check migration status
SELECT * FROM flyway_schema_history WHERE version = '10';
```

---

## STEP 4 — SMTP CONFIG CHECK

### Check Backend Startup Logs

Look for these log messages at backend startup:

```
Email service is configured - Host: smtp.gmail.com, From: ...
```

OR

```
Email service is NOT configured - Missing: username=true, password=false, from=true
Set MAIL_USERNAME, MAIL_PASSWORD, and APP_EMAIL_FROM environment variables
```

### Check Environment Variables

**Development (local):**
```bash
# Check if .env file exists in root
cat .env

# Or check environment variables
echo $MAIL_HOST
echo $MAIL_PORT
echo $MAIL_USERNAME
echo $APP_EMAIL_FROM
```

**Production (Railway/Render):**
- Go to Railway/Render dashboard
- Check Environment Variables section
- Verify these are set:
  - `MAIL_HOST`
  - `MAIL_PORT`
  - `MAIL_USERNAME`
  - `MAIL_PASSWORD` (don't show value)
  - `APP_EMAIL_FROM`

---

## STEP 5 — CONTROLLER CONFIRMATION

### Verify Code Structure

The code is already in the repository. Key files to check:

1. **AuthController.java** - `/auth/verify-email` endpoint (line 289-336)
2. **GlobalExceptionHandler.java** - Exception mappings (lines 194-218)
3. **AccountService.java** - `verifyEmail()` method (lines 231-298)

### Check for catch(Exception e) blocks

Search for:
```bash
grep -r "catch.*Exception.*e" src/main/java/com/aifitness/
```

Ensure no catch blocks are swallowing errors without proper handling.

---

## AUTOMATED EVIDENCE COLLECTION

Run these scripts to automatically collect evidence:

```bash
# Frontend evidence (API call + network trace)
node collect-frontend-evidence.js

# Backend evidence (check logs, config)
node collect-backend-evidence.js

# Database evidence
node collect-database-evidence.js
```

---

## FINAL OUTPUT FORMAT

Once you have all evidence, format it like this:

```
Frontend status code: ___
Backend exception: ___
Failing layer: ___
Root cause: ___
Exact fix required: ___
```

