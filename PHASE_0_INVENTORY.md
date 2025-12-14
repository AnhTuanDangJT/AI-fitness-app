# PHASE 0 - INVENTORY SUMMARY

## Existing AI Coach Implementation

### Frontend
- **Location**: `client/src/pages/Dashboard.jsx`
- **Component**: `AICoachPanel` (lines 658-735)
- **API Service**: `client/src/services/api.js` - `aiCoachAPI.getAdvice()` (lines 274-315)
- **Current Behavior**: 
  - Calls `GET /api/ai/coach/advice`
  - Shows static summary card with loading/error states
  - Displays summary and recommendations list
  - Shows "need 2 weeks" message if insufficient data

### Backend
- **Controller**: `src/main/java/com/aifitness/controller/AiCoachController.java`
  - Endpoint: `GET /api/ai/coach/advice`
  - Uses JWT authentication
  - Returns `ApiResponse<AiCoachResponse>`

- **Service**: `src/main/java/com/aifitness/ai/AiCoachService.java`
  - Method: `generateCoachAdvice(User user)`
  - **Current Logic**:
    - Fetches last 8 weeks of progress via `WeeklyProgressService`
    - **BLOCKS if < 2 weeks of data** (line 60-66)
    - Uses rule-based logic (not real AI)
    - Analyzes: weight trends, sleep, training, calories, stress

- **DTO**: `src/main/java/com/aifitness/dto/AiCoachResponse.java`
  - Fields: `summary` (String), `recommendations` (List<String>)

### Data Sources Available

1. **User Profile** (`User` entity):
   - Weight, height, age, sex
   - Activity level (1-5)
   - Calorie goal (1-4)
   - BMI, WHR, TDEE (calculated via services)
   - Meal preferences (dietary preference, allergies, etc.)

2. **Weekly Progress** (`WeeklyProgress` entity):
   - Table: `weekly_progress`
   - Fields: weight, sleep, stress, hunger, energy, training sessions, calories
   - Aggregated by week (week_start_date)

3. **Meal Plan** (exists):
   - Entity: `MealPlan`, `MealPlanEntry`
   - Can query current meal plan for user

4. **Daily Check-ins**: **DOES NOT EXIST** - Need to create

### Current Endpoints

- `GET /api/ai/coach/advice` - Returns weekly-based advice (requires 2 weeks)

### Authentication
- Uses JWT token from `Authorization: Bearer <token>` header
- Extracted via `JwtTokenService` in controllers

---

## Implementation Plan

### Phase 1: Daily Coaching (Remove 2-week gate)
- Create `DailyCheckIn` entity, repository, service
- Add endpoints: `GET/POST /api/ai/coach/checkins`
- Update `AiCoachService` to work with 0+ days
- Add new `POST /api/ai/coach/chat` endpoint

### Phase 2: Chat UI
- Create `AICoachChat.jsx` component
- Add `aiCoachAPI.chat()` method
- Replace/expand `AICoachPanel` in Dashboard

### Phase 3: App-Aware AI
- Add app knowledge base to prompts
- Enhance context building with app features

### Phase 4: Safety & Reliability
- Add timeouts, error handling, retry logic

### Phase 5: Testing
- Manual test checklist


