# Weekly Progress Data Entry Instructions

This guide explains how to send weekly progress data to the `/api/progress/weekly` endpoint.

## Method 1: Browser Console (Recommended)

1. **Log in to the application** and navigate to the dashboard
2. **Open the browser console** (Press `F12` or `Ctrl+Shift+I` / `Cmd+Option+I`)
3. **Copy the entire contents** of `send-weekly-progress.js`
4. **Paste it into the console** and press Enter
5. The script will automatically:
   - Use your current authentication token
   - Send both weekly progress entries
   - Display success/failure messages

## Method 2: Postman

### Setup:
1. **URL**: `http://localhost:8080/api/progress/weekly` (or your API base URL)
2. **Method**: `POST`
3. **Headers**:
   - `Content-Type`: `application/json`
   - `Authorization`: `Bearer YOUR_TOKEN_HERE`
     - Get your token from browser localStorage: `localStorage.getItem('token')`

### Request 1 - Week 1 (2025-12-01):
```json
{
  "weekStartDate": "2025-12-01",
  "weight": 75,
  "sleepHoursPerNightAverage": 7,
  "stressLevel": 4,
  "hungerLevel": 5,
  "energyLevel": 7,
  "trainingSessionsCompleted": 3,
  "caloriesAverage": 2100
}
```

### Request 2 - Week 2 (2025-12-08):
```json
{
  "weekStartDate": "2025-12-08",
  "weight": 74.2,
  "sleepHoursPerNightAverage": 6.5,
  "stressLevel": 5,
  "hungerLevel": 4,
  "energyLevel": 6,
  "trainingSessionsCompleted": 4,
  "caloriesAverage": 2050
}
```

## After Sending Data:

1. **Refresh the dashboard** page
2. **Click "Refresh advice"** button in the AI Coach section
3. The AI coach should now detect trends and provide recommendations based on:
   - Weight change (75kg → 74.2kg = weight loss)
   - Sleep decrease (7h → 6.5h)
   - Stress increase (4 → 5)
   - Training increase (3 → 4 sessions)
   - Calorie decrease (2100 → 2050)

## Expected AI Coach Response:

With 2 weeks of data, the AI coach will:
- Detect weight loss trend
- Analyze sleep, stress, and training patterns
- Provide rule-based recommendations based on the trends

## Notes:

- The endpoint automatically associates progress entries with the logged-in user via JWT token
- If a progress entry already exists for the same week, it will be updated
- All fields are validated on the backend
- The AI coach needs at least 2 weeks of data to provide meaningful recommendations




