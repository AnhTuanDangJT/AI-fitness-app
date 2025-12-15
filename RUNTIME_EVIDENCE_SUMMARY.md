# 🔍 RUNTIME EVIDENCE SUMMARY - Verify Email Error

## STEP 1 — FRONTEND NETWORK TRACE

### Request Details:
- **Full Request URL:** `http://localhost:8080/api/auth/verify-email`
- **HTTP Method:** `POST`
- **Request Payload:**
  ```json
  {
    "email": "dangtuananh04081972@gmail.com",
    "code": "123456"
  }
  ```

### Response Details:
- **Response Status Code:** `500 INTERNAL_SERVER_ERROR`
- **Response Body (Raw JSON):**
  ```json
  {
    "success": false,
    "message": "Signup failed: could not prepare statement [[SQLITE_ERROR] SQL error or missing database (no such column: u1_0.verification_attempts)] [select u1_0.id,u1_0.activity_level,u1_0.age,u1_0.allergies,u1_0.calorie_goal,u1_0.created_at,u1_0.dietary_preference,u1_0.disliked_foods,u1_0.email,u1_0.email_verification_code,u1_0.email_verification_expires_at,u1_0.favorite_cuisines,u1_0.height,u1_0.hip,u1_0.is_email_verified,u1_0.max_budget_per_day,u1_0.max_cooking_time_per_meal,u1_0.name,u1_0.password_hash,u1_0.preferred_foods,u1_0.sex,u1_0.updated_at,u1_0.username,u1_0.verification_attempts,u1_0.waist,u1_0.weight from users u1_0 where u1_0.email=?]",
    "data": "could not prepare statement [[SQLITE_ERROR] SQL error or missing database (no such column: u1_0.verification_attempts)] [select u1_0.id,u1_0.activity_level,u1_0.age,u1_0.allergies,u1_0.calorie_goal,u1_0.created_at,u1_0.dietary_preference,u1_0.disliked_foods,u1_0.email,u1_0.email_verification_code,u1_0.email_verification_expires_at,u1_0.favorite_cuisines,u1_0.height,u1_0.hip,u1_0.is_email_verified,u1_0.max_budget_per_day,u1_0.max_cooking_time_per_meal,u1_0.name,u1_0.password_hash,u1_0.preferred_foods,u1_0.sex,u1_0.updated_at,u1_0.username,u1_0.verification_attempts,u1_0.waist,u1_0.weight from users u1_0 where u1_0.email=?]",
    "timestamp": "2025-12-14T09:46:46.2402685"
  }
  ```

### Console Output:
- No console.log output from VerifyEmail.jsx (error occurs before reaching frontend error handling)

### VITE_API_BASE_URL:
- Value at runtime: Not set (using default `/api` which proxies to `http://localhost:8080/api`)
- Request is going to: `localhost:8080` (local backend)

---

## STEP 2 — BACKEND LOG TRACE

### Full Stack Trace:
```
org.springframework.orm.jpa.JpaSystemException: could not prepare statement [[SQLITE_ERROR] SQL error or missing database (no such column: u1_0.verification_attempts)] [select ... from users u1_0 where u1_0.email=?]
    at org.springframework.orm.jpa.vendor.HibernateJpaDialect.convertHibernateAccessException(HibernateJpaDialect.java:341)
    ...
    at com.aifitness.service.AccountService.verifyEmail(AccountService.java:235)
    at com.aifitness.controller.AuthController.verifyEmail(AuthController.java:312)
    ...
Caused by: org.hibernate.exception.GenericJDBCException: could not prepare statement [[SQLITE_ERROR] SQL error or missing database (no such column: u1_0.verification_attempts)]
    ...
Caused by: org.sqlite.SQLiteException: [SQLITE_ERROR] SQL error or missing database (no such column: u1_0.verification_attempts)
    at org.sqlite.core.DB.newSQLException(DB.java:1179)
```

### Exception Class Name:
- **Primary:** `org.springframework.orm.jpa.JpaSystemException`
- **Root Cause:** `org.sqlite.SQLiteException: [SQLITE_ERROR] SQL error or missing database (no such column: u1_0.verification_attempts)`

### Line Number Where It Fails:
- **AccountService.java:235** - `userRepository.findByEmail(email)`
- **AuthController.java:312** - `accountService.verifyEmail(sanitizedEmail, code)`

### Log Lines Immediately BEFORE Exception:
```
2025-12-14 09:46:46.235 [http-nio-8080-exec-4] INFO  c.aifitness.service.AccountService - Verification attempt - Email: dangtuananh04081972@gmail.com, Code length: 6
2025-12-14 09:46:46.237 [http-nio-8080-exec-4] WARN  o.h.e.jdbc.spi.SqlExceptionHelper - SQL Error: 1, SQLState: null
2025-12-14 09:46:46.237 [http-nio-8080-exec-4] ERROR o.h.e.jdbc.spi.SqlExceptionHelper - [SQLITE_ERROR] SQL error or missing database (no such column: u1_0.verification_attempts)
```

### Failure Location:
- **Layer:** Database/ORM Layer (Hibernate trying to query User entity)
- **Specific Operation:** User lookup by email (`userRepository.findByEmail()`)
- **NOT failing in:** Code comparison, Expiry check, Attempt increment, EmailService, GlobalExceptionHandler

---

## STEP 3 — DATABASE VERIFICATION

### Database Query Results:
**Status:** Database query cannot be executed because the column doesn't exist.

**Migration V10 Status:**
- Migration file exists: `src/main/resources/db/migration/V10__add_verification_attempts.sql`
- Migration content:
  ```sql
  ALTER TABLE users ADD COLUMN verification_attempts INTEGER NOT NULL DEFAULT 0;
  ```
- **Migration has NOT been applied to the database**

### Column Verification:
- **Column `verification_attempts` DOES NOT EXIST in the database**
- The User entity (User.java:141) expects this column to exist
- Hibernate is trying to select this column but it's missing

---

## STEP 4 — SMTP CONFIG CHECK

### Backend Startup Logs:
- **Email service configuration status:** Not visible in current logs (need to check startup logs)
- **Environment variables:**
  - `MAIL_HOST`: NOT SET
  - `MAIL_PORT`: NOT SET
  - `MAIL_USERNAME`: NOT SET
  - `APP_EMAIL_FROM`: NOT SET
  - `MAIL_PASSWORD`: NOT SET

**Note:** SMTP configuration is NOT the issue here. The error occurs before any email service is called.

---

## STEP 5 — CONTROLLER CONFIRMATION

### AuthController verify endpoint:
- **File:** `src/main/java/com/aifitness/controller/AuthController.java`
- **Method:** `verifyEmail()` (lines 289-336)
- **Exception Handling:** Re-throws exception to GlobalExceptionHandler (line 334)

### GlobalExceptionHandler mappings:
- **File:** `src/main/java/com/aifitness/exception/GlobalExceptionHandler.java`
- **Generic Exception Handler:** Lines 241-258
- **NO catch(Exception e) swallowing errors** - All exceptions are properly re-thrown

### Exception Flow:
1. `AccountService.verifyEmail()` throws `JpaSystemException` (line 235)
2. `AuthController.verifyEmail()` catches and re-throws (line 334)
3. `GlobalExceptionHandler.handleGenericException()` catches it (line 241)
4. Returns 500 with error message

---

## FINAL OUTPUT

### Frontend status code: `500`

### Backend exception: 
```
org.sqlite.SQLiteException: [SQLITE_ERROR] SQL error or missing database (no such column: u1_0.verification_attempts)
```

### Failing layer: 
**Database/ORM Layer** - Hibernate trying to query User entity with missing column

### Root cause: 
**Database migration V10 has not been applied.** The `verification_attempts` column does not exist in the `users` table, but the User entity expects it to exist.

### Exact fix required: 
**Apply migration V10 to add the `verification_attempts` column to the database.**

**Option 1: Manual SQL (Quick Fix)**
```sql
ALTER TABLE users ADD COLUMN verification_attempts INTEGER NOT NULL DEFAULT 0;
```

**Option 2: Enable Flyway Migrations (Proper Fix)**
- Check if Flyway is configured in `pom.xml`
- If not, add Flyway dependency
- Configure Flyway to run migrations on startup
- Restart backend to apply migrations

**Option 3: Use Hibernate DDL Auto-Update**
- Ensure `spring.jpa.hibernate.ddl-auto=update` is set (already set)
- Restart backend - Hibernate should auto-add the column
- **Note:** This may not work if Hibernate thinks the schema is already "up to date"

---

## RECOMMENDED IMMEDIATE FIX

Run this SQL command on your database:

```sql
ALTER TABLE users ADD COLUMN verification_attempts INTEGER NOT NULL DEFAULT 0;
```

Then restart the backend. The verify-email endpoint should work immediately after this.


