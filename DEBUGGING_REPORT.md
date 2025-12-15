# AI Fitness App - Debugging Report
## Broken Features: AI Coach & AI Meal Plan

---

## STEP 1: FRONTEND API CALLS

### API Service File Location
**File:** `client/src/services/api.js`

### Function: `getCoachAdvice()`
**Location:** Lines 250-281 in `api.js`

**Details:**
- **Full Request URL:** `{API_BASE_URL}/ai/coach/advice`
  - `API_BASE_URL` = `import.meta.env.VITE_API_BASE_URL || '/api'`
  - Development: `/api/ai/coach/advice` (proxied to `http://localhost:8080/api/ai/coach/advice`)
  - Production: `{VITE_API_BASE_URL}/ai/coach/advice`

- **HTTP Method:** `GET`

- **Headers:**
  - `Content-Type: application/json` (from axios instance)
  - `Authorization: Bearer {token}` (from localStorage, added by interceptor on line 23)

- **Error Handling Logic:**
  - Returns structured response: `{ type: 'SUCCESS' | 'EMPTY' | 'ERROR', data?, error? }`
  - Success: `response.data.success === true && response.data.data` → Returns `{ type: 'SUCCESS', data }`
  - Empty (insufficient data): Summary contains "need at least 2 weeks" → Returns `{ type: 'EMPTY', data }`
  - Error: All exceptions → Returns `{ type: 'ERROR', error: genericMessage }`
  - Uses `getGenericErrorMessage()` helper which maps:
    - Network errors → `ERROR_MESSAGES.NETWORK`
    - 401 → `ERROR_MESSAGES.UNAUTHORIZED`
    - 500/502/503 → `ERROR_MESSAGES.SERVER_ERROR`
    - Default → `ERROR_MESSAGES.GENERIC`

---

### Function: `getCurrentMealPlan()`
**Location:** Lines 163-213 in `api.js`

**Details:**
- **Full Request URL:** `{API_BASE_URL}/ai/meals/current`
  - Development: `/api/ai/meals/current` (proxied to `http://localhost:8080/api/ai/meals/current`)
  - Production: `{VITE_API_BASE_URL}/ai/meals/current`

- **HTTP Method:** `GET`

- **Headers:**
  - `Content-Type: application/json`
  - `Authorization: Bearer {token}` (from localStorage)

- **Error Handling Logic:**
  - Returns structured response: `{ type: 'SUCCESS' | 'EMPTY' | 'ERROR', data?, error? }`
  - Success: `response.data.success === true && response.data.data` → Returns `{ type: 'SUCCESS', data }`
  - Empty (404): `error.response?.status === 404` → Returns `{ type: 'EMPTY' }` (NOT treated as error)
  - Error: All other exceptions → Returns `{ type: 'ERROR', error: genericMessage }`
  - **CRITICAL:** HTTP 404 is treated as a valid empty state (no meal plan exists yet)

---

### Function: `generateMealPlan(weekStart)`
**Location:** Lines 215-219 in `api.js`

**Details:**
- **Full Request URL:** `{API_BASE_URL}/ai/meals/generate?weekStart={weekStart}`
  - Development: `/api/ai/meals/generate?weekStart=YYYY-MM-DD`
  - Production: `{VITE_API_BASE_URL}/ai/meals/generate?weekStart=YYYY-MM-DD`

- **HTTP Method:** `POST`

- **Headers:**
  - `Content-Type: application/json`
  - `Authorization: Bearer {token}`

- **Request Body:** `null` (data sent via query params)

- **Error Handling Logic:**
  - Throws exception if response is not 2xx
  - Error handled by axios interceptor → Returns generic error message
  - No structured response (unlike getCurrent/getAdvice)

---

### Function: `saveMealPreferences(preferences)`
**Location:** Lines 234-237 in `api.js`

**Details:**
- **Full Request URL:** `{API_BASE_URL}/meal-preferences`
  - Development: `/api/meal-preferences` (proxied to `http://localhost:8080/api/meal-preferences`)
  - Production: `{VITE_API_BASE_URL}/meal-preferences`

- **HTTP Method:** `POST`

- **Headers:**
  - `Content-Type: application/json`
  - `Authorization: Bearer {token}`

- **Request Body:** `preferences` object with meal preference fields

- **Error Handling Logic:**
  - Standard axios error handling via interceptor
  - Returns generic error messages

---

## STEP 2: NETWORK FAILURES

### Component: AI Coach Retry Button
**Location:** `client/src/pages/Dashboard.jsx`

**Handler Function:** `handleRefreshAdvice()` (line 106-108)
```javascript
const handleRefreshAdvice = () => {
  fetchAiCoachAdvice()
}
```

**Main Fetch Function:** `fetchAiCoachAdvice()` (lines 82-104)

**API Function Called:**
- `aiCoachAPI.getAdvice()`

**On Success:**
- `result.type === 'SUCCESS'` → Sets `setAiCoachAdvice(result.data)`
- `result.type === 'EMPTY'` → Sets `setAiCoachAdvice(result.data)` (shows empty state message)

**On Error:**
- `result.type === 'ERROR'` → Sets `setAiCoachError(result.error || STATUS_MESSAGES.AI_COACH_UNAVAILABLE)`
- Exception caught → Sets `setAiCoachError(STATUS_MESSAGES.AI_COACH_UNAVAILABLE)`

**UI State on Failure:**
- `aiCoachError` state is set
- `AICoachPanel` component (lines 659-735) displays error message:
  ```jsx
  <div className="ai-coach-error">
    <p>Unable to load advice right now.</p>
    <button onClick={onRetry}>Retry</button>
  </div>
  ```

---

### Component: AI Meal Plan Retry Button (Dashboard)
**Location:** `client/src/components/meal-plan/MealPlanPanel.jsx`

**Handler Function:** `fetchMealPlan()` (line 66)
- Button calls `onClick={fetchMealPlan}` directly

**API Function Called:**
- `mealPlanAPI.getCurrent()`

**On Success:**
- `result.type === 'SUCCESS'` → Sets `setMealPlan(result.data)`, `setStatus('success')`

**On Error:**
- `result.type === 'ERROR'` → Sets `setStatus('error')`

**UI State on Failure:**
- `status === 'error'` displays:
  ```jsx
  <div className="meal-plan-error">
    <p>Unable to load meal plan.</p>
    <button onClick={fetchMealPlan}>Retry</button>
  </div>
  ```

---

### Component: Meal Plan Page Retry Button
**Location:** `client/src/pages/MealPlan.jsx`

**Handler Function:** `fetchMealPlan()` (lines 41-93)
- Button calls `onClick={fetchMealPlan}` directly (line 251)

**API Function Called:**
- `mealPlanAPI.getCurrent()`

**On Success:**
- `result.type === 'SUCCESS'` → Sets meal plan data, `setStatus('success')`

**On Error:**
- `result.type === 'ERROR'` → Sets error message, `setStatus('error')`

**UI State on Failure:**
- `status === 'error'` displays error container with retry button

---

### Component: Generate Weekly Plan Button
**Location:** Multiple locations:
1. Dashboard sidebar (line 633): Navigates to `/meal-plan` page
2. MealPlanPanel (line 51): Navigates to `/meal-plan` page  
3. MealPlan.jsx (line 299): Calls `handleGenerate()` directly

**Handler Function:** `handleGenerate()` (lines 95-132 in MealPlan.jsx)

**API Function Called:**
- `mealPlanAPI.generate(weekStart)`

**On Success:**
- `response.success === true` → Calls `fetchMealPlan()` to refresh

**On Error:**
- Sets error message: `setError(err.genericMessage || MEAL_PLAN_STATUS.GENERATE_FAILED)`
- Sets status: `setStatus('error')`

**UI State on Failure:**
- Error message displayed, status set to 'error'

---

## STEP 3: BACKEND ROUTES

### Controller: AI Coach
**File:** `src/main/java/com/aifitness/controller/AiCoachController.java`
**Base Path:** `/ai/coach`

**Route:** `GET /api/ai/coach/advice`
- **Method:** `@GetMapping("/advice")` (line 116)
- **Full Path:** `/api/ai/coach/advice` (context path `/api` + request mapping `/ai/coach` + get mapping `/advice`)
- **Auth Middleware:** Custom JWT validation in `getAuthenticatedUser()` method (lines 50-66)
  - Extracts token from `Authorization: Bearer {token}` header
  - Validates token using `jwtTokenService.validateToken()`
  - Retrieves user from database using `userId` from token
- **Response Structure:**
  - **Success (200 OK):**
    ```json
    {
      "success": true,
      "message": "AI coach advice generated successfully",
      "data": {
        "summary": "...",
        "recommendations": ["...", "..."]
      },
      "timestamp": "2024-01-15T10:30:00"
    }
    ```
  - **Error (400 Bad Request / 500 Internal Server Error):**
    ```json
    {
      "success": false,
      "message": "Error message",
      "timestamp": "2024-01-15T10:30:00"
    }
    ```

---

### Controller: Meal Plan
**File:** `src/main/java/com/aifitness/controller/MealPlanController.java`
**Base Path:** `/ai/meals`

**Route 1:** `GET /api/ai/meals/current`
- **Method:** `@GetMapping("/current")` (line 201)
- **Full Path:** `/api/ai/meals/current`
- **Auth Middleware:** Custom JWT validation in `getAuthenticatedUser()` method (lines 52-68)
- **Response Structure:**
  - **Success (200 OK):** Returns meal plan with entries and daily targets
  - **Not Found (404):** Returns error response: `"No meal plan found for user. Please generate a meal plan first."`
  - **Error (400/500):** Standard error response

**Route 2:** `POST /api/ai/meals/generate`
- **Method:** `@PostMapping("/generate")` (line 106)
- **Full Path:** `/api/ai/meals/generate?weekStart=YYYY-MM-DD`
- **Query Parameter:** `weekStart` (optional, defaults to Monday of current week)
- **Auth Middleware:** Custom JWT validation
- **Response Structure:**
  - **Success (200 OK):** Returns generated meal plan DTO
  - **Error (400/500):** Standard error response

**Route 3:** `GET /api/ai/meals/grocery-list`
- **Method:** `@GetMapping("/grocery-list")` (line 283)
- **Full Path:** `/api/ai/meals/grocery-list`
- **Auth Middleware:** Custom JWT validation

---

### Controller: Meal Preferences
**File:** `src/main/java/com/aifitness/controller/MealPreferencesController.java`
**Base Path:** `/meal-preferences`

**Route 1:** `GET /api/meal-preferences`
- **Method:** `@GetMapping` (line 67)
- **Full Path:** `/api/meal-preferences`
- **Auth Middleware:** Custom JWT validation

**Route 2:** `POST /api/meal-preferences`
- **Method:** `@PostMapping` (line 112)
- **Full Path:** `/api/meal-preferences`
- **Auth Middleware:** Custom JWT validation
- **Request Body:** `MealPreferencesRequest` (validated with `@Valid`)
- **Response Structure:**
  - **Success (200 OK):** Returns success message
  - **Error (400/500):** Standard error response

---

### Security Configuration
**File:** `src/main/java/com/aifitness/config/SecurityConfig.java`

**⚠️ CRITICAL ISSUE IDENTIFIED:**

Lines 96-104 show the authorization rules:
```java
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/health/**").permitAll()
    .requestMatchers("/auth/**").permitAll()
    .requestMatchers("/profile/**").permitAll()
    .requestMatchers("/user/**").permitAll()
    .requestMatchers("/calculate/**").permitAll()
    .requestMatchers("/meal-preferences/**").permitAll()
    .anyRequest().authenticated() // ⚠️ This blocks /ai/** routes!
);
```

**PROBLEM:**
- `/ai/coach/**` and `/ai/meals/**` routes are NOT in the `permitAll()` list
- They fall under `.anyRequest().authenticated()` which requires Spring Security authentication
- However, Spring Security is configured with `STATELESS` sessions and **NO JWT AUTHENTICATION FILTER**
- This means Spring Security will reject `/ai/**` requests with **401 Unauthorized** BEFORE they reach the controllers
- The controllers' custom JWT validation (`getAuthenticatedUser()`) is never reached

**ROOT CAUSE:**
The `/ai/**` endpoints need to be added to `permitAll()` so Spring Security allows them through, and then the controllers can handle JWT validation themselves (similar to `/profile/**`, `/user/**`, etc.).

---

## STEP 4: BACKEND ERRORS

### Expected Behavior When Testing:

When triggering **AI Coach Retry**:
1. Frontend calls `GET /api/ai/coach/advice`
2. Request should reach `AiCoachController.getCoachAdvice()`
3. Controller should log: `[RequestId: ...] GET /api/ai/coach/advice - START`
4. Controller should authenticate user and generate advice

When triggering **Meal Plan Retry**:
1. Frontend calls `GET /api/ai/meals/current`
2. Request should reach `MealPlanController.getCurrentMealPlan()`
3. Controller should log: `[RequestId: ...] GET /api/ai/meals/current - START`
4. Controller should authenticate user and retrieve meal plan

### Expected Error (Due to Security Config Issue):

**Before Request Reaches Controller:**
- Spring Security intercepts request
- Returns **401 Unauthorized** because no authentication filter is configured
- Request **NEVER REACHES** the controller
- No controller logs will appear

**Server Logs Expected:**
- No logs from `AiCoachController` or `MealPlanController`
- Possible Spring Security filter logs showing authentication failure
- Browser Network tab shows: `401 Unauthorized`

**Error Response:**
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/ai/coach/advice"  // or "/api/ai/meals/current"
}
```

---

## STEP 5: SUMMARY

### Which Requests Fail
1. **AI Coach:** `GET /api/ai/coach/advice`
2. **AI Meal Plan:** `GET /api/ai/meals/current`
3. **Meal Plan Generate:** `POST /api/ai/meals/generate` (also affected)
4. **Grocery List:** `GET /api/ai/meals/grocery-list` (also affected)

### Status Codes Returned
- **401 Unauthorized** (from Spring Security, before reaching controllers)

### Failure Point Analysis

**❌ AUTHENTICATION FAILURE (Spring Security Level)**

The failure occurs at the **Spring Security filter chain level**, NOT in:
- ❌ Not an auth middleware issue in controllers (code is correct)
- ❌ Not a routing issue (routes are correctly defined)
- ❌ Not an AI service issue (services are never called)
- ❌ Not a database issue (controllers never execute)

**Root Cause:**
- SecurityConfig line 103: `.anyRequest().authenticated()` requires authentication for `/ai/**` routes
- No JWT authentication filter configured in Spring Security
- Spring Security rejects requests with 401 before they reach controllers
- Controllers have correct JWT validation code, but it never executes

### Mismatch Between Frontend and Backend Paths

**✅ NO MISMATCH FOUND:**
- Frontend calls: `/api/ai/coach/advice` ✓
- Backend route: `/api/ai/coach/advice` ✓
- Frontend calls: `/api/ai/meals/current` ✓
- Backend route: `/api/ai/meals/current` ✓

Paths match correctly. The issue is purely at the Spring Security configuration level.

---

## RECOMMENDED FIX

**File:** `src/main/java/com/aifitness/config/SecurityConfig.java`

**Change Required (Line 102-103):**
```java
// BEFORE:
.requestMatchers("/meal-preferences/**").permitAll() // TEMPORARY: Meal preferences - permitAll for testing (change back to authenticated() after)
.anyRequest().authenticated() // All other endpoints require authentication

// AFTER:
.requestMatchers("/meal-preferences/**").permitAll()
.requestMatchers("/ai/**").permitAll() // AI endpoints (JWT validation in controllers)
.anyRequest().authenticated() // All other endpoints require authentication
```

This allows `/ai/**` requests to pass through Spring Security filters, reaching the controllers where JWT validation is properly implemented.

---

## ADDITIONAL NOTES

1. **CORS Configuration:** Properly configured in SecurityConfig (lines 119-170), allowing localhost origins and production domains.

2. **Vite Proxy:** Correctly configured to proxy `/api` requests to `http://localhost:8080/api` in development.

3. **Error Messages:** Frontend properly handles errors and displays user-friendly messages.

4. **Logging:** Backend controllers have comprehensive logging that would help debug if requests reached them.

5. **Token Handling:** Frontend correctly adds `Authorization: Bearer {token}` header via axios interceptor.

---

**Report Generated:** Debugging information collection complete.
**Next Step:** Fix SecurityConfig to allow `/ai/**` routes through Spring Security filters.


