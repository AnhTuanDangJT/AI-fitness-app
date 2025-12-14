# AI Fitness App - Architecture Overview

## Project Structure

This is a full-stack fitness application with:
- **Backend**: Spring Boot 3.2.0 (Java 17)
- **Frontend**: React (Vite)
- **Database**: SQLite (development) / PostgreSQL (production)

---

## User/Profile Data Storage

### Backend Entities

**Location**: `src/main/java/com/aifitness/entity/User.java`

The `User` entity stores all user and profile data:
- **Authentication**: `id`, `username`, `email`, `passwordHash`, `createdAt`, `updatedAt`
- **Email Verification**: `isEmailVerified`, `emailVerificationCode`, `emailVerificationExpiresAt`
- **Profile Data**: `name`, `age`, `sex` (Boolean: true=male, false=female), `weight` (kg), `height` (cm), `waist` (cm), `hip` (cm)
- **Fitness Goals**: `activityLevel` (1-5), `calorieGoal` (1=Lose, 2=Maintain, 3=Gain, 4=Recomposition)

**Note**: Calculated metrics (BMI, WHR, BMR, TDEE, etc.) are **NOT stored** in the database. They are calculated on-the-fly when needed.

### Backend DTOs

**Location**: `src/main/java/com/aifitness/dto/`

Key DTOs:
- `ProfileResponseDTO` - Complete profile with all calculated metrics
- `ProfileSaveRequest` - For creating/updating full profile
- `ProfileUpdateRequest` - For partial profile updates
- `BodyMetricsDTO` - BMI, WHR, WHtR, Body Fat
- `EnergyDTO` - BMR, TDEE, Goal Calories
- `MacronutrientsDTO` - Protein, Fat, Carbs, Fiber, Water
- `MicronutrientsDTO` - Iron, Calcium, Vitamin D, etc.

### Backend Controllers

**Location**: `src/main/java/com/aifitness/controller/`

- **`ProfileController`** (`/api/profile/*`)
  - `POST /api/profile/save` - Save/update full profile
  - `PUT /api/profile/update` - Partial profile update
  - `GET /api/profile/full-analysis` - Get complete analysis
  - `GET /api/profile/export` - Export profile data for PDF

- **`UserController`** (`/api/user/*`)
  - `GET /api/user/profile` - Get user profile

- **`AuthController`** (`/api/auth/*`)
  - Handles signup, login, email verification

---

## Current Recommendation / Macro Logic

### Backend Services

**Location**: `src/main/java/com/aifitness/service/`

#### 1. **NutritionService** (`NutritionService.java`)
Contains all nutrition calculation logic:
- **BMR Calculation**: Mifflin-St Jeor equation (weight, height, age, sex)
- **TDEE Calculation**: BMR × activity multiplier (1.2 to 1.9)
- **Goal Calories**: Based on TDEE and goal (deficit/surplus)
- **Macronutrients**:
  - Protein: Based on goal (1.4-2.2g per kg)
  - Fat: 0.8 × weight (kg)
  - Carbs: Calculated from remaining calories
- **Micronutrients**: Sex-specific requirements (Iron, Calcium, etc.)

#### 2. **BodyMetricsService** (`BodyMetricsService.java`)
Contains body metric calculations:
- **BMI**: `weight / (height/100)²`
- **WHR**: `waist / hip`
- **WHtR**: `waist / height`
- **Body Fat**: Formula based on BMI, age, sex

#### 3. **ProfileService** (`ProfileService.java`)
Coordinates profile operations:
- Retrieves user from database
- Calls `BodyMetricsService` and `NutritionService` to calculate metrics
- Returns complete `ProfileResponseDTO` with all calculations

**Current Logic Flow**:
```
User Profile Data (Database)
    ↓
ProfileService.getProfile()
    ↓
BodyMetricsService (BMI, WHR, etc.)
    ↓
NutritionService (BMR, TDEE, Macros, Micros)
    ↓
ProfileResponseDTO (Complete response)
```

### Frontend Pages

**Location**: `client/src/pages/`

#### 1. **Dashboard** (`Dashboard.jsx`)
- Displays BMI, WHR, BMR, TDEE, Goal Calories, Protein Target
- Shows health tips and recommendations
- Health recommendations based on:
  - BMI category
  - WHR risk level
  - Activity level
  - Fitness goal (lose/maintain/gain/recomp)
- **Recommendation Logic**: Hardcoded rules in `HealthRecommendations` component

#### 2. **ProfilePage** (`ProfilePage.jsx`)
- Displays complete profile information
- Shows all body metrics, energy values, macros, and micros
- Read-only view of profile data

#### 3. **EditProfile** (`EditProfile.jsx`)
- Allows users to update profile fields
- Calls `PUT /api/profile/update`

#### 4. **ProfileSetup** (`ProfileSetup.jsx`)
- Initial profile creation
- Calls `POST /api/profile/save`

---

## Frontend API Integration

**Location**: `client/src/services/api.js`

The frontend uses axios to call backend endpoints:
- Base URL configured in `api.js`
- JWT token stored in localStorage
- Token sent in `Authorization: Bearer <token>` header

---

## Future AI Features - Integration Points

### 1. **AI Coach** (`AiCoachService`)
**Planned Location**: `src/main/java/com/aifitness/ai/AiCoachService.java`

**Integration Points**:
- **Backend**: New endpoint in `ProfileController` or new `AiCoachController`
  - `GET /api/ai/coach/advice` - Get personalized coaching advice
  - `POST /api/ai/coach/chat` - Interactive coaching chat
- **Frontend**: New page `client/src/pages/AICoach.jsx` or section in Dashboard
- **Data Source**: User profile data from `ProfileService.getProfile()`
- **Input**: User's current stats, goals, activity level
- **Output**: Personalized workout/nutrition advice

### 2. **AI Meal Plan Generator** (`AiMealsService`)
**Planned Location**: `src/main/java/com/aifitness/ai/AiMealsService.java`

**Integration Points**:
- **Backend**: New endpoint in `NutritionController` or new `AiMealsController`
  - `POST /api/ai/meals/generate` - Generate meal plan
  - `GET /api/ai/meals/plan/{planId}` - Get saved meal plan
- **Frontend**: New page `client/src/pages/MealPlan.jsx` or section in Dashboard
- **Data Source**: 
  - User's calorie goal from `NutritionService.calculateGoalCalories()`
  - Macronutrient targets from `NutritionService`
  - User preferences (dietary restrictions, allergies, etc.)
- **Output**: Weekly/daily meal plan with recipes

### 3. **Grocery List Generator**
**Planned Location**: `src/main/java/com/aifitness/ai/AiMealsService.java` (same service)

**Integration Points**:
- **Backend**: Endpoint `GET /api/ai/meals/grocery-list?planId={id}`
- **Frontend**: Component in `MealPlan.jsx` or separate `GroceryList.jsx`
- **Data Source**: Generated meal plan from `AiMealsService`
- **Output**: Shopping list organized by category

### 4. **AI Vision Service** (`AiVisionService`)
**Planned Location**: `src/main/java/com/aifitness/ai/AiVisionService.java`

**Integration Points**:
- **Backend**: New endpoint in `AiVisionController`
  - `POST /api/ai/vision/analyze-food` - Analyze food image
  - `POST /api/ai/vision/analyze-body` - Analyze body composition (future)
- **Frontend**: New component `client/src/components/FoodImageUpload.jsx`
- **Data Source**: Image uploads from user
- **Output**: Food identification, calorie/macro estimation

### 5. **AI Insights Service** (`AiInsightsService`)
**Planned Location**: `src/main/java/com/aifitness/ai/AiInsightsService.java`

**Integration Points**:
- **Backend**: New endpoint in `ProfileController` or `AiInsightsController`
  - `GET /api/ai/insights/trends` - Get long-term trends
  - `GET /api/ai/insights/predictions` - Get predictions
- **Frontend**: New section in Dashboard or `client/src/pages/Insights.jsx`
- **Data Source**: Historical profile data (requires new `ProfileHistory` entity)
- **Output**: Trend analysis, predictions, recommendations

---

## Configuration

### Backend Configuration
**Location**: `src/main/resources/application.properties`

Current configuration includes:
- Database connection (SQLite/PostgreSQL)
- JWT secret key
- Email server settings
- CORS configuration

**Future**: AI API key will be read from environment variable `AI_API_KEY` via `AiConfig` class.

### Frontend Configuration
**Location**: `client/src/config/constants.js`

Contains UI labels, error messages, button text, etc.

---

## Database Schema

**Location**: `src/main/resources/db/migration/`

Current migrations:
- `V1__create_users_table.sql` - Creates users table
- `V2__add_profile_fields.sql` - Adds profile fields
- `V3__add_email_verification_fields.sql` - Adds email verification

**Future**: May need new tables for:
- Meal plans
- Food logs
- Profile history (for trends)
- AI chat history

---

## Security

- **Authentication**: JWT tokens
- **Password**: BCrypt hashing
- **Input Validation**: Jakarta Validation annotations
- **Input Sanitization**: `StringSanitizer` utility
- **Rate Limiting**: `RateLimitingFilter` (Bucket4j)

---

## Summary

- **User/Profile Data**: Stored in `User` entity, accessed via `ProfileService`
- **Recommendation Logic**: Currently rule-based in `NutritionService`, `BodyMetricsService`, and frontend `HealthRecommendations` component
- **Frontend Pages**: Dashboard, ProfilePage, EditProfile, ProfileSetup
- **AI Integration Points**: All AI services will be in `com.aifitness.ai` package, called from controllers, and displayed in new frontend pages/components

