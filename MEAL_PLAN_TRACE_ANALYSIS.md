# "No Meal Plan Found" - Complete Code Path Trace

## Overview
This document traces all code paths that can lead to the "No Meal Plan Found" UI state on the `/meal-plan` route.

---

## 1. Route Definition

**File:** `client/src/App.jsx`
- **Route Path:** `/meal-plan`
- **Component:** `<MealPlan />`
- **Protection:** Wrapped in `<ProtectedRoute>` (requires JWT authentication)

```jsx
<Route
  path="/meal-plan"
  element={
    <ProtectedRoute>
      <MealPlan />
    </ProtectedRoute>
  }
/>
```

---

## 2. Component: MealPlan.jsx

**File:** `client/src/pages/MealPlan.jsx`

### 2.1 State Variables

The component uses these React states:

```javascript
const [mealPlan, setMealPlan] = useState(null)        // Main meal plan data
const [loading, setLoading] = useState(true)           // Loading indicator
const [error, setError] = useState('')                 // Error message
const [generating, setGenerating] = useState(false)    // Generation in progress
```

### 2.2 Initial Data Fetch

**Trigger:** `useEffect` hook runs on component mount (line 21-37)

```javascript
useEffect(() => {
  fetchMealPlan()  // Called immediately on mount
  // ... resize handler setup
}, [])
```

### 2.3 fetchMealPlan() Function

**Location:** Lines 39-60

**Flow:**
1. Sets `loading = true`, `error = ''`
2. Calls `mealPlanAPI.getCurrent()`
3. If `response.success === true`:
   - Sets `mealPlan = response.data`
   - Sets `loading = false`
4. If `response.success === false`:
   - Sets `error = response.message || MEAL_PLAN_STATUS.LOAD_FAILED`
   - Sets `loading = false`
5. If exception occurs:
   - Sets `error = err.genericMessage || MEAL_PLAN_STATUS.LOAD_FAILED`
   - Sets `loading = false`

---

## 3. Rendering Logic - "No Meal Plan Found" Conditions

**File:** `client/src/pages/MealPlan.jsx`

### 3.1 Condition 1: Loading State (Lines 167-178)

```javascript
if (loading) {
  return <LoadingSpinner />
}
```

**Shows:** Loading spinner, NOT "No Meal Plan Found"

### 3.2 Condition 2: Error State (Lines 180-194)

```javascript
if (error && !mealPlan) {
  return (
    <div>
      <h2>{MEAL_PLAN_LABELS.NO_MEAL_PLAN}</h2>  // "No Meal Plan Found"
      <p>{error}</p>
      <button>Generate New Meal Plan</button>
    </div>
  )
}
```

**Triggers when:**
- `error` is truthy (non-empty string)
- AND `mealPlan` is falsy (null, undefined, or empty)

**This is PATH 1 to "No Meal Plan Found"**

### 3.3 Condition 3: No Meal Plan (Lines 196-210)

```javascript
if (!mealPlan) {
  return (
    <div>
      <h2>{MEAL_PLAN_LABELS.NO_MEAL_PLAN}</h2>  // "No Meal Plan Found"
      <p>{MEAL_PLAN_LABELS.NO_MEAL_PLAN_DESCRIPTION}</p>
      <button>Generate New Meal Plan</button>
    </div>
  )
}
```

**Triggers when:**
- `mealPlan` is falsy (null, undefined, or empty)
- AND `loading === false` (already passed loading check)
- AND `error` is falsy OR `mealPlan` exists (already passed error check)

**This is PATH 2 to "No Meal Plan Found"**

---

## 4. API Service Layer

**File:** `client/src/services/api.js`

### 4.1 mealPlanAPI.getCurrent()

**Location:** Lines 151-155

```javascript
getCurrent: async () => {
  const response = await api.get('/ai/meals/current')
  return response.data
}
```

**Request:**
- Method: `GET`
- URL: `/api/ai/meals/current` (baseURL is `/api`, so full path is `/api/ai/meals/current`)
- Headers: 
  - `Authorization: Bearer <token>` (added by interceptor)
  - `Content-Type: application/json`

**Expected Response Shape:**
```javascript
{
  success: boolean,
  message: string,
  data: MealPlanResponseDTO | null,
  timestamp: string
}
```

**Success Case:**
```javascript
{
  success: true,
  message: "Meal plan retrieved successfully",
  data: {
    id: 1,
    userId: 1,
    weekStartDate: "2024-01-15",
    entries: [...],
    dailyTargets: {...},
    createdAt: "..."
  }
}
```

**Error Case (404):**
```javascript
{
  success: false,
  message: "No meal plan found for user. Please generate a meal plan first.",
  data: null,
  timestamp: "..."
}
```

### 4.2 Axios Interceptors

**Request Interceptor (Lines 19-30):**
- Adds JWT token from localStorage to `Authorization` header
- If no token, request proceeds without auth header

**Response Interceptor (Lines 75-105):**
- On 401: Removes token, redirects to `/login`
- On network error: Sets `error.genericMessage = ERROR_MESSAGES.NETWORK`
- On other errors: Sets `error.genericMessage` based on status code

---

## 5. Backend Controller

**File:** `src/main/java/com/aifitness/controller/MealPlanController.java`

### 5.1 Endpoint: GET /api/ai/meals/current

**Location:** Lines 179-208

**Mapping:** `@GetMapping("/current")` on `@RequestMapping("/ai/meals")`

**Flow:**
1. Extracts user from JWT token via `getAuthenticatedUser(request)`
2. Calls `mealPlanService.getLatestMealPlan(user)`
3. If `mealPlan == null`:
   - Returns `404 NOT_FOUND`
   - Response: `ApiResponse.error("No meal plan found for user. Please generate a meal plan first.")`
4. If `mealPlan != null`:
   - Converts to DTO via `mealPlanService.toDTO(mealPlan)`
   - Returns `200 OK`
   - Response: `ApiResponse.success("Meal plan retrieved successfully", responseDTO)`

**Response Types:**

**Success (200):**
```json
{
  "success": true,
  "message": "Meal plan retrieved successfully",
  "data": {
    "id": 1,
    "userId": 1,
    "weekStartDate": "2024-01-15",
    "entries": [...],
    "dailyTargets": {...},
    "createdAt": "..."
  },
  "timestamp": "..."
}
```

**Not Found (404):**
```json
{
  "success": false,
  "message": "No meal plan found for user. Please generate a meal plan first.",
  "data": null,
  "timestamp": "..."
}
```

**Error (400/500):**
```json
{
  "success": false,
  "message": "Error message here",
  "data": null,
  "timestamp": "..."
}
```

---

## 6. Backend Service

**File:** `src/main/java/com/aifitness/service/MealPlanService.java`

### 6.1 getLatestMealPlan()

**Location:** Lines 131-134

```java
public MealPlan getLatestMealPlan(User user) {
    return mealPlanRepository.findFirstByUserOrderByWeekStartDateDesc(user)
            .orElse(null);
}
```

**Returns:**
- `MealPlan` entity if found
- `null` if no meal plan exists for the user

### 6.2 toDTO()

**Location:** Lines 138-175

**Converts MealPlan entity to MealPlanResponseDTO:**
- Sets basic fields (id, userId, weekStartDate, createdAt)
- Converts entries to DTOs
- Calculates daily targets from first day's entries

**Returns:**
- `MealPlanResponseDTO` if mealPlan is not null
- `null` if mealPlan is null

---

## 7. Backend Repository

**File:** `src/main/java/com/aifitness/repository/MealPlanRepository.java`

### 7.1 findFirstByUserOrderByWeekStartDateDesc()

**Location:** Line 39

```java
Optional<MealPlan> findFirstByUserOrderByWeekStartDateDesc(User user);
```

**Behavior:**
- Queries database for meal plans where `user = ?`
- Orders by `weekStartDate DESC` (most recent first)
- Returns `Optional<MealPlan>`:
  - `Optional.of(mealPlan)` if found
  - `Optional.empty()` if not found

---

## 8. Complete Code Paths to "No Meal Plan Found"

### Path 1: Backend Returns 404 (No Meal Plan in Database)

**Flow:**
1. User navigates to `/meal-plan`
2. `MealPlan` component mounts
3. `useEffect` calls `fetchMealPlan()`
4. `mealPlanAPI.getCurrent()` sends GET `/api/ai/meals/current`
5. Backend `MealPlanController.getCurrentMealPlan()`:
   - Extracts user from JWT
   - Calls `mealPlanService.getLatestMealPlan(user)`
   - Repository returns `Optional.empty()`
   - Service returns `null`
   - Controller returns `404 NOT_FOUND` with `{success: false, message: "No meal plan found..."}`
6. Frontend receives response with `success: false`
7. `fetchMealPlan()` sets `error = response.message`
8. Render: `if (error && !mealPlan)` → Shows "No Meal Plan Found"

**Console Logs:**
- `[MealPlanController] Meal plan is NULL - returning 404 NOT_FOUND`
- `[api.js] API Response received: {success: false, ...}`
- `[MealPlan.jsx] Response indicates failure`

### Path 2: Backend Returns 200 but response.success is false

**Flow:**
1. Same as Path 1, but backend returns 200 with `success: false`
2. Frontend receives `{success: false, ...}`
3. `fetchMealPlan()` sets `error = response.message`
4. Render: `if (error && !mealPlan)` → Shows "No Meal Plan Found"

### Path 3: Network/API Error

**Flow:**
1. User navigates to `/meal-plan`
2. `fetchMealPlan()` calls `mealPlanAPI.getCurrent()`
3. Network error occurs (CORS, backend down, etc.)
4. Exception caught, `error.genericMessage` set
5. `fetchMealPlan()` sets `error = err.genericMessage`
6. Render: `if (error && !mealPlan)` → Shows "No Meal Plan Found"

**Possible Errors:**
- `ERR_NETWORK`: Backend not running or CORS issue
- `401 UNAUTHORIZED`: Invalid/expired token (redirects to login)
- `500 INTERNAL_SERVER_ERROR`: Backend exception

### Path 4: Response Success but Data is Null/Undefined

**Flow:**
1. Backend returns `{success: true, data: null}`
2. Frontend sets `mealPlan = null`
3. Render: `if (!mealPlan)` → Shows "No Meal Plan Found"

### Path 5: Loading Completes, No Error, No Meal Plan

**Flow:**
1. `fetchMealPlan()` completes
2. `loading = false`
3. `error = ''` (empty)
4. `mealPlan = null` (never set)
5. Render: `if (!mealPlan)` → Shows "No Meal Plan Found"

---

## 9. Files Involved

### Frontend:
1. `client/src/App.jsx` - Route definition
2. `client/src/components/ProtectedRoute.jsx` - Authentication check
3. `client/src/pages/MealPlan.jsx` - Main component
4. `client/src/services/api.js` - API service layer
5. `client/src/config/constants.js` - Constants (MEAL_PLAN_LABELS.NO_MEAL_PLAN)

### Backend:
1. `src/main/java/com/aifitness/controller/MealPlanController.java` - REST endpoint
2. `src/main/java/com/aifitness/service/MealPlanService.java` - Business logic
3. `src/main/java/com/aifitness/repository/MealPlanRepository.java` - Data access
4. `src/main/java/com/aifitness/dto/ApiResponse.java` - Response wrapper
5. `src/main/java/com/aifitness/dto/MealPlanResponseDTO.java` - Response DTO
6. `src/main/java/com/aifitness/entity/MealPlan.java` - Entity
7. `src/main/java/com/aifitness/util/JwtTokenService.java` - JWT validation

---

## 10. Console Log Statements Added

### Frontend Logs:

**In `MealPlan.jsx`:**
- `[MealPlan.jsx] fetchMealPlan() - START`
- `[MealPlan.jsx] Current state before fetch`
- `[MealPlan.jsx] Calling mealPlanAPI.getCurrent()...`
- `[MealPlan.jsx] API Response received`
- `[MealPlan.jsx] Response is successful` OR `Response indicates failure`
- `[MealPlan.jsx] RENDER - Current state`
- `[MealPlan.jsx] RENDER - Showing "No Meal Plan Found"`

**In `api.js`:**
- `[api.js] mealPlanAPI.getCurrent() - START`
- `[api.js] Making GET request to: /ai/meals/current`
- `[api.js] Token from localStorage`
- `[api.js] API Response received`
- `[api.js] Error in mealPlanAPI.getCurrent()`

### Backend Logs:

**In `MealPlanController.java`:**
- `[MealPlanController] GET /api/ai/meals/current - START`
- `[MealPlanController] Authenticated user found`
- `[MealPlanController] Calling mealPlanService.getLatestMealPlan()...`
- `[MealPlanController] getLatestMealPlan returned: NULL or MealPlan(...)`
- `[MealPlanController] Meal plan is NULL - returning 404 NOT_FOUND` OR `Returning 200 OK`

**In `MealPlanService.java`:**
- `[MealPlanService] getLatestMealPlan() - START`
- `[MealPlanService] Calling mealPlanRepository.findFirstByUserOrderByWeekStartDateDesc()...`
- `[MealPlanService] Repository returned: MealPlan found or Empty Optional`
- `[MealPlanService] No meal plan found in database` OR `Returning MealPlan`
- `[MealPlanService] toDTO() - START`
- `[MealPlanService] Converting entries...`
- `[MealPlanService] toDTO() - COMPLETE`

---

## 11. Expected vs Actual Response Analysis

### Expected Success Response:
```json
{
  "success": true,
  "message": "Meal plan retrieved successfully",
  "data": {
    "id": 1,
    "userId": 1,
    "weekStartDate": "2024-01-15",
    "entries": [
      {
        "id": 1,
        "date": "2024-01-15",
        "mealType": "BREAKFAST",
        "name": "...",
        "calories": 350,
        "protein": 20,
        "carbs": 35,
        "fats": 12
      }
    ],
    "dailyTargets": {
      "calories": 2000,
      "protein": 150,
      "carbs": 200,
      "fats": 60
    },
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

### Actual 404 Response (No Meal Plan):
```json
{
  "success": false,
  "message": "No meal plan found for user. Please generate a meal plan first.",
  "data": null,
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## 12. Debugging Checklist

When "No Meal Plan Found" appears, check:

1. **Browser Console:**
   - Look for `[MealPlan.jsx]` logs
   - Look for `[api.js]` logs
   - Check for network errors

2. **Backend Logs:**
   - Look for `[MealPlanController]` logs
   - Look for `[MealPlanService]` logs
   - Check if user is authenticated
   - Check if meal plan exists in database

3. **Network Tab:**
   - Check request to `/api/ai/meals/current`
   - Check response status (200, 404, 401, 500)
   - Check response body
   - Check request headers (Authorization token)

4. **Database:**
   - Query: `SELECT * FROM meal_plan WHERE user_id = ?`
   - Verify meal plan exists for the user

---

## 13. Summary

The "No Meal Plan Found" message appears when:

1. **Backend returns 404** - No meal plan in database for the user
2. **Backend returns 200 with success:false** - Unexpected error response
3. **Network error** - Cannot reach backend
4. **Response data is null** - Backend returns success but data is null
5. **State never set** - fetchMealPlan completes but mealPlan remains null

**Most Common Cause:** User has no meal plan in the database, backend returns 404, frontend shows error state.

