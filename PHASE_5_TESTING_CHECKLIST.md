# PHASE 5 - TESTING CHECKLIST

## Manual Testing Guide

### Prerequisites
1. Backend running on `http://localhost:8080`
2. Frontend running on `http://localhost:3000` or `http://localhost:5173`
3. User account created and logged in
4. Profile completed (weight, height, age, gender, activity level, goal)

---

## Test Case A: New User with No Logs

**Steps:**
1. Log in as a new user (or clear daily check-ins if testing with existing user)
2. Navigate to Dashboard
3. Click "Open Chat" in AI Coach section
4. Verify chat opens with welcome message
5. Type: "make me a workout plan"
6. Click Send or press Enter

**Expected Results:**
- ✅ Chat interface loads
- ✅ Welcome message appears
- ✅ User message appears on right side
- ✅ Loading indicator shows while processing
- ✅ AI responds with a workout plan
- ✅ Response includes plan details (days/week, exercises, duration)
- ✅ If profile incomplete, AI asks for goal/activity level

**Notes:**
- AI should work even with 0 days of data
- Should NOT show "need 2 weeks" message

---

## Test Case B: User with Profile but No Check-ins

**Steps:**
1. Log in as user with complete profile
2. Ensure no daily check-ins exist (check database or clear them)
3. Open AI Coach Chat
4. Type: "how many calories should I eat?"
5. Send message

**Expected Results:**
- ✅ AI responds with calorie target
- ✅ Uses TDEE calculation from profile
- ✅ Mentions goal (lose/maintain/gain)
- ✅ If activity level missing, asks for it
- ✅ Response is personalized to user's profile

**Notes:**
- Should use profile data (weight, height, age, activity, goal)
- Should NOT require check-ins to answer

---

## Test Case C: Meal Plan Exists

**Steps:**
1. Log in as user
2. Generate a meal plan (go to Meal Plan page, click "Generate Weekly Plan")
3. Return to Dashboard
4. Open AI Coach Chat
5. Type: "summarize my meal plan for today"
6. Send message

**Expected Results:**
- ✅ AI acknowledges meal plan exists
- ✅ References meal plan data
- ✅ Provides summary or directs to Meal Plan page
- ✅ Can answer questions about meals

**Alternative Test:**
- Ask: "what should I eat today?"
- Should reference meal plan if available

---

## Test Case D: App Help Mode

**Steps:**
1. Open AI Coach Chat
2. Click "App Help" mode button (or type question with "how to")
3. Type: "how do I generate a meal plan?"
4. Send message

**Expected Results:**
- ✅ AI responds with step-by-step instructions
- ✅ Instructions are accurate for the website
- ✅ Mentions correct page names and buttons
- ✅ Provides clear, actionable steps

**Additional App Help Tests:**
- "how do I complete my profile?"
- "what is the dashboard?"
- "how do I get my grocery list?"
- "how do I download my profile PDF?"

**Expected:**
- All should return accurate, helpful instructions
- Should match actual app workflow

---

## Test Case E: Failure Modes

### E1: Network Error Simulation

**Steps:**
1. Stop backend server
2. Open AI Coach Chat
3. Send a message
4. Wait for error

**Expected Results:**
- ✅ Error message appears: "Network error. Please check your connection and try again."
- ✅ Retry button appears
- ✅ Clicking Retry re-sends last message
- ✅ No infinite loading

### E2: Timeout Simulation

**Steps:**
1. (If possible) Simulate slow backend response (>25 seconds)
2. Send a message
3. Wait for timeout

**Expected Results:**
- ✅ After 25 seconds, timeout error appears
- ✅ Error message: "Request took too long. Please try again."
- ✅ Retry button available
- ✅ No infinite loading

### E3: Invalid Input

**Steps:**
1. Open AI Coach Chat
2. Try to send empty message (just spaces)
3. Try to send very long message (>1000 chars)

**Expected Results:**
- ✅ Empty message: Send button disabled or message not sent
- ✅ Long message: Backend returns 400 error, frontend shows error message
- ✅ Clear error messages

### E4: Backend Returns Empty Response

**Steps:**
1. (Simulate backend returning empty/null response)
2. Send a message

**Expected Results:**
- ✅ Frontend detects empty response
- ✅ Shows error: "Received empty response. Please try again."
- ✅ Retry available

---

## Test Case F: Daily Check-ins

### F1: Create Check-in

**Steps:**
1. Use API or create UI to add daily check-in:
   ```
   POST /api/ai/coach/checkins
   {
     "date": "2024-01-15",
     "weight": 75.5,
     "steps": 8500,
     "workoutDone": true,
     "notes": "Feeling good"
   }
   ```

**Expected Results:**
- ✅ Check-in saved successfully
- ✅ Returns 200 OK with check-in data

### F2: Get Check-ins

**Steps:**
1. Call:
   ```
   GET /api/ai/coach/checkins?start=2024-01-10&end=2024-01-15
   ```

**Expected Results:**
- ✅ Returns list of check-ins in date range
- ✅ Ordered by date ascending
- ✅ Includes all fields

### F3: AI Uses Check-in Data

**Steps:**
1. Create 3-5 daily check-ins with workouts
2. Open AI Coach Chat
3. Ask: "how am I doing with my workouts?"

**Expected Results:**
- ✅ AI references check-in data
- ✅ Mentions workout frequency
- ✅ Provides feedback based on actual data

---

## Test Case G: Mode Switching

**Steps:**
1. Open AI Coach Chat
2. Click different mode buttons: General, Workout Plan, Nutrition, App Help
3. Send same question in each mode: "help me"

**Expected Results:**
- ✅ Mode button highlights when active
- ✅ Responses vary by mode (workout mode focuses on workouts, etc.)
- ✅ Mode persists in chat history

---

## Test Case H: Chat History Persistence

**Steps:**
1. Open AI Coach Chat
2. Send 3-4 messages
3. Refresh page
4. Return to Dashboard
5. Open chat again

**Expected Results:**
- ✅ Previous messages still visible
- ✅ Chat history loaded from localStorage
- ✅ Can continue conversation

---

## Test Case I: Updated Advice Endpoint (No 2-Week Gate)

**Steps:**
1. Log in as new user (no weekly progress)
2. Navigate to Dashboard
3. View AI Coach summary card (not chat)
4. Click "Refresh Advice"

**Expected Results:**
- ✅ Advice appears immediately
- ✅ NO "need 2 weeks" message
- ✅ Shows starter advice based on profile
- ✅ Suggests logging daily progress

---

## Test Case J: Integration - All Features Together

**Steps:**
1. Complete profile
2. Generate meal plan
3. Log 3-5 daily check-ins
4. Open AI Coach Chat
5. Ask various questions:
   - "what's my calorie target?"
   - "create a workout plan"
   - "what should I eat today?"
   - "how do I use the grocery list?"

**Expected Results:**
- ✅ All questions answered accurately
- ✅ Uses real user data
- ✅ Provides actionable advice
- ✅ No errors or infinite loading

---

## Automated Testing (Future)

### Backend Unit Tests
- [ ] DailyCheckInService.saveDailyCheckIn()
- [ ] DailyCheckInService.getCheckInsForDateRange()
- [ ] AiCoachService.buildCoachContext()
- [ ] AiCoachService.handleChat() with different modes
- [ ] AiCoachService.generateCoachAdvice() with 0 days data

### Frontend Unit Tests
- [ ] AICoachChat component renders
- [ ] Message sending works
- [ ] Error handling works
- [ ] Chat history persistence

### Integration Tests
- [ ] POST /api/ai/coach/chat with valid request
- [ ] POST /api/ai/coach/chat with invalid request
- [ ] GET /api/ai/coach/checkins
- [ ] POST /api/ai/coach/checkins

---

## Performance Testing

1. **Response Time:**
   - Chat responses should complete in < 3 seconds (rule-based)
   - Advice endpoint should complete in < 2 seconds

2. **Concurrent Users:**
   - Test with 10+ simultaneous chat requests
   - Should handle gracefully

3. **Large Chat History:**
   - Test with 100+ messages in localStorage
   - Should not cause performance issues

---

## Security Testing

1. **Authentication:**
   - Try accessing `/api/ai/coach/chat` without token → Should return 401
   - Try with invalid token → Should return 401

2. **Input Validation:**
   - Try SQL injection in message → Should be sanitized
   - Try XSS in message → Should be escaped
   - Try very long message → Should be rejected (>1000 chars)

3. **Authorization:**
   - Try accessing another user's check-ins → Should only see own data

---

## Browser Compatibility

Test on:
- [ ] Chrome (latest)
- [ ] Firefox (latest)
- [ ] Safari (latest)
- [ ] Edge (latest)
- [ ] Mobile browsers (iOS Safari, Chrome Mobile)

---

## Notes

- All tests should be performed in both development and production-like environments
- Document any bugs found during testing
- Verify error messages are user-friendly
- Ensure no sensitive data is exposed in error messages


