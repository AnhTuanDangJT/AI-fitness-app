# 🔍 Debugging Guide - AI Coach & Meal Plan Features

## 📋 What to Check (In Order)

---

## 1️⃣ Network Response Details (MOST IMPORTANT)

### How to Check:
1. Open **Chrome DevTools** (F12)
2. Go to **Network** tab
3. Click **Retry** on AI Coach
4. Click **Retry** on AI Meal Plan
5. For EACH request, copy:

### Expected Endpoints:

#### AI Coach Advice
- **URL:** `GET /api/ai/coach/advice`
- **Full URL:** `http://localhost:8080/api/ai/coach/advice` (or your backend URL)
- **Method:** GET
- **Headers:**
  ```
  Authorization: Bearer <your-jwt-token>
  Content-Type: application/json
  ```
- **Expected Status:** 200 OK
- **Expected Response:**
  ```json
  {
    "success": true,
    "message": "AI coach advice generated successfully",
    "data": {
      "summary": "...",
      "recommendations": ["..."]
    },
    "timestamp": "2024-01-15T10:30:00"
  }
  ```

#### Meal Plan Current
- **URL:** `GET /api/ai/meals/current`
- **Full URL:** `http://localhost:8080/api/ai/meals/current`
- **Method:** GET
- **Headers:**
  ```
  Authorization: Bearer <your-jwt-token>
  Content-Type: application/json
  ```
- **Expected Status:** 
  - 200 OK (if meal plan exists)
  - 404 NOT_FOUND (if no meal plan - this is OK, treated as empty state)
- **Expected Response (200):**
  ```json
  {
    "success": true,
    "message": "Meal plan retrieved successfully",
    "data": {
      "id": 1,
      "userId": 1,
      "weekStartDate": "2024-01-15",
      "entries": [...],
      "dailyTargets": {...}
    }
  }
  ```
- **Expected Response (404 - Empty State):**
  ```json
  {
    "success": false,
    "message": "No meal plan found for user. Please generate a meal plan first."
  }
  ```

#### Meal Plan Generate
- **URL:** `POST /api/ai/meals/generate?weekStart=YYYY-MM-DD`
- **Full URL:** `http://localhost:8080/api/ai/meals/generate?weekStart=2024-01-15`
- **Method:** POST
- **Headers:**
  ```
  Authorization: Bearer <your-jwt-token>
  Content-Type: application/json
  ```
- **Expected Status:** 200 OK
- **Expected Response:**
  ```json
  {
    "success": true,
    "message": "Meal plan generated successfully",
    "data": {
      "id": 1,
      "userId": 1,
      "weekStartDate": "2024-01-15",
      "entries": [...],
      "dailyTargets": {...}
    }
  }
  ```

#### Meal Preferences Save
- **URL:** `POST /api/meal-preferences`
- **Full URL:** `http://localhost:8080/api/meal-preferences`
- **Method:** POST
- **Headers:**
  ```
  Authorization: Bearer <your-jwt-token>
  Content-Type: application/json
  ```
- **Expected Status:** 200 OK

---

## 2️⃣ Frontend API Service Code

### File: `client/src/services/api.js`

#### Base Configuration:
```javascript
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
})

// Token injection
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)
```

#### AI Coach API:
```javascript
export const aiCoachAPI = {
  getAdvice: async () => {
    try {
      const response = await api.get('/ai/coach/advice')
      
      if (response.data?.success && response.data?.data) {
        const advice = response.data.data
        
        // Check if advice is actually empty (insufficient data message)
        const hasRealAdvice = advice.summary && 
          !advice.summary.includes('need at least 2 weeks') &&
          !advice.summary.includes('Please log your weekly progress')
        
        if (hasRealAdvice) {
          return { type: 'SUCCESS', data: advice }
        } else {
          return { type: 'EMPTY', data: advice }
        }
      }
      
      return { type: 'ERROR', error: ERROR_MESSAGES.GENERIC }
      
    } catch (error) {
      return { 
        type: 'ERROR', 
        error: error.genericMessage || error.message || ERROR_MESSAGES.GENERIC
      }
    }
  },
}
```

#### Meal Plan API:
```javascript
export const mealPlanAPI = {
  getCurrent: async () => {
    try {
      const response = await api.get('/ai/meals/current')
      
      if (response.data?.success && response.data?.data) {
        return { type: 'SUCCESS', data: response.data.data }
      }
      
      return { type: 'EMPTY' }
      
    } catch (error) {
      // HTTP 404 is a valid empty state, not an error
      if (error.response?.status === 404) {
        return { type: 'EMPTY' }
      }
      
      return { 
        type: 'ERROR', 
        error: error.genericMessage || error.message || ERROR_MESSAGES.GENERIC
      }
    }
  },
  
  generate: async (weekStart) => {
    const params = weekStart ? { weekStart } : {}
    const response = await api.post('/ai/meals/generate', null, { params })
    return response.data
  },
}
```

#### Meal Preferences API:
```javascript
export const mealPreferencesAPI = {
  get: async () => {
    const response = await api.get('/meal-preferences')
    return response.data
  },
  
  save: async (preferences) => {
    const response = await api.post('/meal-preferences', preferences)
    return response.data
  },
}
```

---

## 3️⃣ Backend Controller Files

### AI Coach Controller
**File:** `src/main/java/com/aifitness/controller/AiCoachController.java`

**Route:** `@RequestMapping("/ai/coach")`
**Endpoint:** `@GetMapping("/advice")`
**Full Path:** `/api/ai/coach/advice`

**Key Points:**
- ✅ Auth handled in `getAuthenticatedUser()` method
- ✅ Logs with request IDs
- ✅ Returns 200 OK with advice data
- ✅ Handles exceptions with proper error responses

### Meal Plan Controller
**File:** `src/main/java/com/aifitness/controller/MealPlanController.java`

**Route:** `@RequestMapping("/ai/meals")`
**Endpoints:**
- `@GetMapping("/current")` → `/api/ai/meals/current`
- `@PostMapping("/generate")` → `/api/ai/meals/generate`
- `@GetMapping("/grocery-list")` → `/api/ai/meals/grocery-list`

**Key Points:**
- ✅ Auth handled in `getAuthenticatedUser()` method
- ✅ `/current` returns 404 when no meal plan exists (treated as empty state)
- ✅ `/generate` creates and persists meal plan
- ✅ Logs with request IDs

### Meal Preferences Controller
**File:** `src/main/java/com/aifitness/controller/MealPreferencesController.java`

**Route:** `@RequestMapping("/meal-preferences")`
**Endpoints:**
- `@GetMapping` → `/api/meal-preferences`
- `@PostMapping` → `/api/meal-preferences`

**Key Points:**
- ✅ Auth handled in `getAuthenticatedUser()` method
- ✅ Saves preferences to User entity
- ✅ Logs with request IDs

---

## 4️⃣ Backend Error Logs

### Where to Find Logs:

#### Development (Local):
- **Console output** where you run `mvn spring-boot:run`
- **Log file:** `logs/spring-boot.log`

#### Production:
- **Railway:** Check Railway dashboard → Logs tab
- **Render:** Check Render dashboard → Logs tab
- **Local logs:** `logs/spring-boot.log`

### What to Look For:

#### Success Logs (Should See):
```
[RequestId: abc12345] GET /api/ai/coach/advice - START
[RequestId: abc12345] Authenticated user: userId=1, username=testuser
[RequestId: abc12345] AI coach advice generated successfully for userId=1
```

#### Error Logs (If Issues):
```
[RequestId: abc12345] RuntimeException in getCoachAdvice for userId=1: Unauthorized: No token provided
[RequestId: abc12345] Exception in getCurrentMealPlan for userId=1: ...
```

### Common Issues:

1. **401 Unauthorized:**
   - Check: Token exists in localStorage
   - Check: Token is valid (not expired)
   - Check: Authorization header is sent

2. **404 Not Found:**
   - For `/ai/meals/current`: This is OK if no meal plan exists
   - For other endpoints: Check route path matches exactly

3. **500 Internal Server Error:**
   - Check backend logs for stack trace
   - Check database connection
   - Check if required data exists

4. **CORS Error:**
   - Check `SecurityConfig.java` CORS configuration
   - Check frontend URL is in allowed origins
   - Check backend is running

---

## 🔧 Quick Debugging Steps

### Step 1: Check Token
```javascript
// In browser console:
console.log('Token:', localStorage.getItem('token'))
```

### Step 2: Check API Base URL
```javascript
// In browser console:
console.log('API Base URL:', import.meta.env.VITE_API_BASE_URL || '/api')
```

### Step 3: Test Endpoint Directly
```bash
# Using curl (replace TOKEN with actual token):
curl -X GET http://localhost:8080/api/ai/coach/advice \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -H "Content-Type: application/json"
```

### Step 4: Check Backend is Running
```bash
# Health check:
curl http://localhost:8080/api/health
```

### Step 5: Check CORS Configuration
**File:** `src/main/java/com/aifitness/config/SecurityConfig.java`

Look for:
```java
.allowedOrigins("http://localhost:3000", "http://localhost:5173", "https://ai-fitness-app-one.vercel.app")
```

---

## 📝 Expected Response Formats

### Success Response:
```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... },
  "timestamp": "2024-01-15T10:30:00"
}
```

### Error Response:
```json
{
  "success": false,
  "message": "Error message here",
  "timestamp": "2024-01-15T10:30:00"
}
```

### Empty State (404 for meal plan):
```json
{
  "success": false,
  "message": "No meal plan found for user. Please generate a meal plan first.",
  "timestamp": "2024-01-15T10:30:00"
}
```

---

## ✅ Verification Checklist

- [ ] Backend is running (check health endpoint)
- [ ] Token exists in localStorage
- [ ] Token is valid (not expired)
- [ ] Authorization header is sent in requests
- [ ] CORS is configured correctly
- [ ] Route paths match exactly (case-sensitive)
- [ ] Database connection is working
- [ ] User exists in database
- [ ] Backend logs show requests arriving
- [ ] No CORS errors in browser console
- [ ] No network errors in DevTools

---

## 🚨 Common Issues & Solutions

### Issue: "Unable to load advice right now"
**Possible Causes:**
1. Token missing or invalid → Check localStorage
2. Backend not running → Check health endpoint
3. CORS error → Check SecurityConfig
4. Network error → Check backend URL

### Issue: "Meal plan will appear here" (stays empty)
**Possible Causes:**
1. No meal plan generated yet → Generate one first
2. 404 not handled correctly → Check frontend handles 404 as EMPTY
3. Database issue → Check backend logs

### Issue: Preferences not saving
**Possible Causes:**
1. Validation error → Check request body format
2. Database error → Check backend logs
3. Auth error → Check token

---

## 📞 Next Steps

1. **Run the network checks** (Step 1) and share the results
2. **Check browser console** for any JavaScript errors
3. **Check backend logs** for request IDs and errors
4. **Verify token** is valid and not expired
5. **Test endpoints directly** with curl/Postman

Share the network response details and I can help diagnose the exact issue!



