package com.aifitness.ai;

import com.aifitness.dto.*;
import com.aifitness.entity.User;
import com.aifitness.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI Coach Service
 * 
 * Provides personalized fitness coaching advice and recommendations.
 * 
 * Currently uses rule-based logic as a placeholder for future LLM integration.
 * The API contract is stable and will remain the same when real AI is integrated.
 * 
 * Integration Points:
 * - Called from AiCoachController
 * - Uses DailyCheckInService to get daily progress data
 * - Uses WeeklyProgressService to get weekly progress data
 * - Uses NutritionService to get calorie/macro targets
 * - Uses MealPlanService to get meal plan data
 */
@Service
public class AiCoachService {
    
    /**
     * System prompt for AI Coach behavior.
     * This will be used when LLM integration is added.
     * 
     * The prompt enforces:
     * - Direct answers instead of generic help menus
     * - Intent interpretation for short/vague questions
     * - Automatic use of user data
     * - Intelligent summarization for broad questions
     * - No deflection or stat spamming
     */
    private static final String SYSTEM_PROMPT = 
        "You are a personal AI fitness coach.\n" +
        "You must ALWAYS:\n" +
        "1) Interpret the user's question, even if it is short or vague.\n" +
        "2) Infer the most likely intent instead of asking 'what do you want to know'.\n" +
        "3) Use available user data (calories, TDEE, BMI, goals) automatically.\n" +
        "4) Answer the question FIRST.\n" +
        "5) Ask at most ONE clarifying question, only if truly necessary.\n" +
        "6) NEVER respond with generic help menus or capability lists.\n" +
        "7) NEVER ask 'What would you like to know?'\n\n" +
        "You must NEVER:\n" +
        "- Ask 'What would you like to know?'\n" +
        "- Ask the user to narrow the question if it is broad\n" +
        "- Respond by listing your capabilities\n" +
        "- Repeat user stats unless they are directly relevant to the answer\n\n" +
        "If the user asks for a broad topic (e.g. 'all nutrition', 'everything about workouts'),\n" +
        "you must:\n" +
        "1) Summarize the MOST RELEVANT information for THIS user\n" +
        "2) Organize the answer into clear sections\n" +
        "3) Keep it concise and practical\n" +
        "4) End with ONE optional follow-up suggestion (not a question)\n\n" +
        "Response structure (ALL responses):\n" +
        "- Direct useful content (no greetings)\n" +
        "- Structured bullets or numbered sections\n" +
        "- Personalized to the user's data\n" +
        "- ONE optional next-step suggestion\n\n" +
        "Examples:\n" +
        "User: 'how many protein'\n" +
        "→ Interpret as: daily protein target\n" +
        "→ Respond with grams per day using user profile\n\n" +
        "User: 'what should i eat tomorrow'\n" +
        "→ Check meal plan\n" +
        "→ If exists: summarize tomorrow's meals\n" +
        "→ If not: suggest generating one\n\n" +
        "User: 'workout'\n" +
        "→ Suggest a reasonable workout plan and ask availability\n\n" +
        "User: 'All of the nutrition that you know'\n" +
        "→ Provide a personalized nutrition overview\n" +
        "→ Do NOT ask clarifying questions\n" +
        "→ Do NOT refuse\n" +
        "→ Do NOT ask user what they want\n\n" +
        "User: 'All'\n" +
        "→ Interpret as overview request\n" +
        "→ Summarize most relevant domain (nutrition or fitness context)";
    
    private final WeeklyProgressService weeklyProgressService;
    private final DailyCheckInService dailyCheckInService;
    private final NutritionService nutritionService;
    private final MealPlanService mealPlanService;
    private final BodyMetricsService bodyMetricsService;
    
    @Autowired
    public AiCoachService(WeeklyProgressService weeklyProgressService,
                         DailyCheckInService dailyCheckInService,
                         NutritionService nutritionService,
                         MealPlanService mealPlanService,
                         BodyMetricsService bodyMetricsService) {
        this.weeklyProgressService = weeklyProgressService;
        this.dailyCheckInService = dailyCheckInService;
        this.nutritionService = nutritionService;
        this.mealPlanService = mealPlanService;
        this.bodyMetricsService = bodyMetricsService;
    }
    
    /**
     * Generates personalized coaching advice based on user's progress data.
     * 
     * Now works with daily check-ins and weekly progress (if available).
     * NO LONGER requires 2 weeks - works with 0+ days of data.
     * 
     * Analyzes available data and provides recommendations based on:
     * - Daily check-ins (weight, steps, workouts)
     * - Weekly progress (if available)
     * - User profile metrics
     * 
     * @param user The user to generate advice for
     * @return AI Coach response with summary and recommendations
     */
    public AiCoachResponse generateCoachAdvice(User user) {
        // Build context with all available data
        CoachContext context = buildCoachContext(user, LocalDate.now(), 7);
        
        // Build summary using available data
        String summary = buildSummaryFromContext(user, context);
        
        // Generate recommendations
        List<String> recommendations = generateRecommendationsFromContext(user, context);
        
        return new AiCoachResponse(summary, recommendations);
    }
    
    /**
     * Handles chat requests from users.
     * Uses unified intelligent behavior to answer questions directly.
     * 
     * @param user The user making the request
     * @param message The user's message
     * @param date Optional date for context (default: today)
     * @return Chat response with assistant message
     */
    public ChatResponse handleChat(User user, String message, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        
        // Validate message is not empty
        if (message == null || message.trim().isEmpty()) {
            // Only return generic message if message is truly empty (not for ambiguous input)
            return new ChatResponse(
                "I'm here to help with your fitness journey. Ask me about workouts, nutrition, meal planning, or your progress.",
                generateActionsFromChat(null)
            );
        }
        
        // Build context
        CoachContext context = buildCoachContext(user, date, 7);
        
        // INTENT ROUTING: Check for specific intents first
        String intentResult = processIntentBasedRouting(user, message, context);
        if (intentResult != null) {
            // Intent was handled, return response with appropriate actions
            List<String> actions = generateActionsForIntent(message, context);
            return new ChatResponse(intentResult, actions);
        }
        
        // Process chat with unified intelligent behavior
        String assistantMessage = processChatMessageIntelligently(user, message, context);
        List<String> actions = generateActionsFromChat(context);
        
        return new ChatResponse(assistantMessage, actions);
    }
    
    /**
     * Builds a CoachContext object with all available user data.
     */
    private CoachContext buildCoachContext(User user, LocalDate targetDate, int daysBack) {
        CoachContext context = new CoachContext();
        context.setToday(LocalDate.now());
        
        // Get user profile metrics
        if (user.hasCompleteProfile()) {
            double bmi = bodyMetricsService.calculateBMI(user.getWeight(), user.getHeight());
            context.setBmi(bmi);
            
            double bmr = nutritionService.calculateBMR(
                    user.getWeight(), user.getHeight(), user.getAge(), user.getSex());
            double tdee = nutritionService.calculateTDEE(bmr, user.getActivityLevel());
            context.setTdee(tdee);
            
            if (user.getCalorieGoal() != null) {
                double goalCalories = nutritionService.calculateGoalCalories(tdee, user.getCalorieGoal());
                context.setGoalCalories(goalCalories);
                
                double proteinTarget = nutritionService.calculateProtein(
                        user.getCalorieGoal(), user.getWeight());
                context.setProteinTarget(proteinTarget);
                
                // Map calorie goal to string
                String goalStr = switch (user.getCalorieGoal()) {
                    case 1 -> "lose_weight";
                    case 2 -> "maintain";
                    case 3 -> "gain_muscle";
                    case 4 -> "recomposition";
                    default -> "maintain";
                };
                context.setGoal(goalStr);
            }
            
            context.setActivityLevel(user.getActivityLevel());
        }
        
        // Get daily check-ins (last N days)
        LocalDate startDate = targetDate.minusDays(daysBack);
        List<DailyCheckInResponse> checkIns = dailyCheckInService.getCheckInsForDateRange(
                user, startDate, targetDate);
        context.setRecentCheckIns(checkIns);
        
        // Get weekly progress (last 8 weeks) if available
        List<WeeklyProgressResponse> weeklyProgress = weeklyProgressService
                .getRecentProgressForUser(user, 8);
        context.setRecentWeeklyProgress(weeklyProgress);
        
        // Get meal plan if available (simplified)
        try {
            var mealPlanEntity = mealPlanService.getLatestMealPlan(user);
            if (mealPlanEntity != null) {
                MealPlanResponseDTO mealPlan = mealPlanService.toDTO(mealPlanEntity);
                if (mealPlan != null) {
                    Map<String, Object> mealPlanMap = new HashMap<>();
                    mealPlanMap.put("weekStart", mealPlan.getWeekStartDate());
                    mealPlanMap.put("hasPlan", true);
                    context.setMealPlan(mealPlanMap);
                }
            }
        } catch (Exception e) {
            // Meal plan not available - that's okay
            context.setMealPlan(null);
        }
        
        return context;
    }
    
    /**
     * Builds a compact summary from context (daily + weekly data).
     */
    private String buildSummaryFromContext(User user, CoachContext context) {
        StringBuilder summary = new StringBuilder();
        
        // Use daily check-ins if available
        List<DailyCheckInResponse> checkIns = context.getRecentCheckIns();
        List<WeeklyProgressResponse> weeklyProgress = context.getRecentWeeklyProgress();
        
        // If we have daily check-ins, use them
        if (!checkIns.isEmpty()) {
            // Count workouts done
            long workoutsDone = checkIns.stream()
                    .filter(c -> c.getWorkoutDone() != null && c.getWorkoutDone())
                    .count();
            summary.append(String.format("In the last %d days, you completed %d workouts. ", 
                    checkIns.size(), workoutsDone));
            
            // Average steps if available
            double avgSteps = checkIns.stream()
                    .filter(c -> c.getSteps() != null)
                    .mapToInt(DailyCheckInResponse::getSteps)
                    .average()
                    .orElse(0.0);
            if (avgSteps > 0) {
                summary.append(String.format("Average steps: %.0f/day. ", avgSteps));
            }
            
            // Weight trend if available
            List<DailyCheckInResponse> withWeight = checkIns.stream()
                    .filter(c -> c.getWeight() != null)
                    .toList();
            if (withWeight.size() >= 2) {
                double firstWeight = withWeight.get(0).getWeight();
                double lastWeight = withWeight.get(withWeight.size() - 1).getWeight();
                double weightChange = lastWeight - firstWeight;
                if (Math.abs(weightChange) > 0.1) {
                    summary.append(String.format("Weight change: %.1f kg. ", weightChange));
                }
            }
        } else if (!weeklyProgress.isEmpty()) {
            // Fall back to weekly progress
            summary.append(buildSummaryFromWeekly(user, weeklyProgress));
        } else {
            // No data yet - provide starter message
            summary.append("Welcome! Start logging your daily progress to get personalized coaching. ");
            if (context.getGoalCalories() != null) {
                summary.append(String.format("Your daily calorie target is %.0f kcal. ", 
                        context.getGoalCalories()));
            }
        }
        
        // Add profile-based info
        if (context.getTdee() != null) {
            summary.append(String.format("Your TDEE is %.0f kcal/day. ", context.getTdee()));
        }
        
        return summary.toString();
    }
    
    /**
     * Builds summary from weekly progress (legacy method, kept for compatibility).
     */
    private String buildSummary(User user, List<WeeklyProgressResponse> progressList) {
        StringBuilder summary = new StringBuilder();
        
        // Weight trend
        if (progressList.size() >= 2) {
            double firstWeight = progressList.get(0).getWeight();
            double lastWeight = progressList.get(progressList.size() - 1).getWeight();
            double weightChange = lastWeight - firstWeight;
            
            if (Math.abs(weightChange) < 0.5) {
                summary.append("Weight has been stable (");
            } else if (weightChange > 0) {
                summary.append(String.format("Weight increased by %.1f kg (", weightChange));
            } else {
                summary.append(String.format("Weight decreased by %.1f kg (", Math.abs(weightChange)));
            }
            summary.append(String.format("%.1f kg → %.1f kg). ", firstWeight, lastWeight));
        }
        
        // Average sleep
        double avgSleep = progressList.stream()
                .filter(p -> p.getSleepHoursPerNightAverage() != null)
                .mapToInt(WeeklyProgressResponse::getSleepHoursPerNightAverage)
                .average()
                .orElse(0.0);
        summary.append(String.format("Average sleep: %.1f hours/night. ", avgSleep));
        
        // Training adherence
        double avgTraining = progressList.stream()
                .filter(p -> p.getTrainingSessionsCompleted() != null)
                .mapToInt(WeeklyProgressResponse::getTrainingSessionsCompleted)
                .average()
                .orElse(0.0);
        summary.append(String.format("Average training sessions: %.1f/week. ", avgTraining));
        
        // Average calories vs target
        if (user.getWeight() != null && user.getHeight() != null && 
            user.getAge() != null && user.getSex() != null &&
            user.getActivityLevel() != null && user.getCalorieGoal() != null) {
            
            double bmr = nutritionService.calculateBMR(
                    user.getWeight(), user.getHeight(), user.getAge(), user.getSex());
            double tdee = nutritionService.calculateTDEE(bmr, user.getActivityLevel());
            double goalCalories = nutritionService.calculateGoalCalories(tdee, user.getCalorieGoal());
            
            double avgCalories = progressList.stream()
                    .filter(p -> p.getCaloriesAverage() != null)
                    .mapToDouble(WeeklyProgressResponse::getCaloriesAverage)
                    .average()
                    .orElse(0.0);
            
            if (avgCalories > 0) {
                double calorieDiff = avgCalories - goalCalories;
                if (Math.abs(calorieDiff) < 100) {
                    summary.append("Calories on target. ");
                } else if (calorieDiff > 0) {
                    summary.append(String.format("Calories %.0f above target. ", calorieDiff));
                } else {
                    summary.append(String.format("Calories %.0f below target. ", Math.abs(calorieDiff)));
                }
            }
        }
        
        // Average stress
        double avgStress = progressList.stream()
                .filter(p -> p.getStressLevel() != null)
                .mapToInt(WeeklyProgressResponse::getStressLevel)
                .average()
                .orElse(0.0);
        summary.append(String.format("Average stress level: %.1f/10.", avgStress));
        
        return summary.toString();
    }
    
    /**
     * Generates rule-based recommendations based on progress data.
     */
    private List<String> generateRecommendations(User user, List<WeeklyProgressResponse> progressList) {
        List<String> recommendations = new ArrayList<>();
        
        // Check weight plateau (last 2-3 weeks)
        if (progressList.size() >= 3) {
            List<WeeklyProgressResponse> recentWeeks = progressList.subList(
                    progressList.size() - 3, progressList.size());
            
            boolean isPlateau = true;
            double firstWeight = recentWeeks.get(0).getWeight();
            for (int i = 1; i < recentWeeks.size(); i++) {
                if (recentWeeks.get(i).getWeight() == null || 
                    Math.abs(recentWeeks.get(i).getWeight() - firstWeight) > 0.5) {
                    isPlateau = false;
                    break;
                }
            }
            
            if (isPlateau && user.getWeight() != null && user.getCalorieGoal() != null) {
                // Check if calories are close to target (small deficit)
                double avgCalories = recentWeeks.stream()
                        .filter(p -> p.getCaloriesAverage() != null)
                        .mapToDouble(WeeklyProgressResponse::getCaloriesAverage)
                        .average()
                        .orElse(0.0);
                
                if (user.getHeight() != null && user.getAge() != null && user.getSex() != null &&
                    user.getActivityLevel() != null && avgCalories > 0) {
                    
                    double bmr = nutritionService.calculateBMR(
                            user.getWeight(), user.getHeight(), user.getAge(), user.getSex());
                    double tdee = nutritionService.calculateTDEE(bmr, user.getActivityLevel());
                    double goalCalories = nutritionService.calculateGoalCalories(tdee, user.getCalorieGoal());
                    
                    double calorieDiff = goalCalories - avgCalories;
                    
                    // If plateau and small deficit (less than 300 calories)
                    if (calorieDiff < 300 && calorieDiff > 0) {
                        double currentProtein = nutritionService.calculateProtein(
                                user.getCalorieGoal(), user.getWeight());
                        double increaseProtein = currentProtein * 0.2; // Increase by 20%
                        double reduceCarbs = (increaseProtein * 4) / 4; // Equivalent calories from carbs
                        
                        recommendations.add(String.format(
                                "Weight plateau detected. Increase protein by %.0fg and reduce carbs by %.0fg to break through.",
                                increaseProtein, reduceCarbs));
                    }
                }
            }
        }
        
        // Check sleep
        double avgSleep = progressList.stream()
                .filter(p -> p.getSleepHoursPerNightAverage() != null)
                .mapToInt(WeeklyProgressResponse::getSleepHoursPerNightAverage)
                .average()
                .orElse(0.0);
        
        if (avgSleep < 6.0) {
            recommendations.add("Your average sleep is below 6 hours. Consider reducing training intensity tomorrow and prioritize 7-9 hours of sleep for better recovery.");
        } else if (avgSleep < 7.0) {
            recommendations.add("Your sleep could be improved. Aim for 7-9 hours per night for optimal recovery and performance.");
        }
        
        // Check stress
        double avgStress = progressList.stream()
                .filter(p -> p.getStressLevel() != null)
                .mapToInt(WeeklyProgressResponse::getStressLevel)
                .average()
                .orElse(0.0);
        
        if (avgStress >= 7) {
            recommendations.add("Your stress levels are high. Consider a deload week: reduce training volume by 40-50% and focus on recovery activities like walking, yoga, or stretching.");
        } else if (avgStress >= 6) {
            recommendations.add("Your stress levels are elevated. Monitor your recovery and consider reducing training intensity if you feel fatigued.");
        }
        
        // Check training adherence
        double avgTraining = progressList.stream()
                .filter(p -> p.getTrainingSessionsCompleted() != null)
                .mapToInt(WeeklyProgressResponse::getTrainingSessionsCompleted)
                .average()
                .orElse(0.0);
        
        if (avgTraining < 2) {
            recommendations.add("Your training frequency is low. Aim for at least 3-4 sessions per week for consistent progress.");
        }
        
        // If no specific recommendations, provide general encouragement
        if (recommendations.isEmpty()) {
            recommendations.add("Keep up the great work! Your progress looks consistent. Continue tracking your metrics and stay consistent with your nutrition and training.");
        }
        
        return recommendations;
    }
    
    /**
     * Builds summary from weekly progress (helper method).
     */
    private String buildSummaryFromWeekly(User user, List<WeeklyProgressResponse> progressList) {
        // Reverse list to get chronological order (oldest first)
        List<WeeklyProgressResponse> chronological = new ArrayList<>(progressList);
        java.util.Collections.reverse(chronological);
        return buildSummary(user, chronological);
    }
    
    /**
     * Generates recommendations from context (daily + weekly data).
     */
    private List<String> generateRecommendationsFromContext(User user, CoachContext context) {
        List<String> recommendations = new ArrayList<>();
        
        List<DailyCheckInResponse> checkIns = context.getRecentCheckIns();
        List<WeeklyProgressResponse> weeklyProgress = context.getRecentWeeklyProgress();
        
        // Use daily check-ins if available
        if (!checkIns.isEmpty()) {
            // Check workout frequency
            long workoutsDone = checkIns.stream()
                    .filter(c -> c.getWorkoutDone() != null && c.getWorkoutDone())
                    .count();
            double workoutRate = (double) workoutsDone / checkIns.size();
            
            if (workoutRate < 0.3) {
                recommendations.add("You've been working out less than 30% of days. Try to increase your workout frequency to 3-4 times per week for better results.");
            } else if (workoutRate < 0.5) {
                recommendations.add("Good start! Consider increasing your workout frequency to 4-5 times per week for optimal progress.");
            }
            
            // Check steps
            double avgSteps = checkIns.stream()
                    .filter(c -> c.getSteps() != null)
                    .mapToInt(DailyCheckInResponse::getSteps)
                    .average()
                    .orElse(0.0);
            if (avgSteps > 0 && avgSteps < 5000) {
                recommendations.add("Your average daily steps are below 5,000. Aim for at least 7,000-10,000 steps per day for better health.");
            }
        }
        
        // Fall back to weekly progress if no daily data
        if (checkIns.isEmpty() && !weeklyProgress.isEmpty()) {
            List<WeeklyProgressResponse> chronological = new ArrayList<>(weeklyProgress);
            java.util.Collections.reverse(chronological);
            recommendations.addAll(generateRecommendations(user, chronological));
        }
        
        // If no data at all, provide starter recommendations
        if (recommendations.isEmpty()) {
            if (context.getGoalCalories() != null) {
                recommendations.add(String.format("Your daily calorie target is %.0f kcal. Start logging your meals and workouts to track your progress.", 
                        context.getGoalCalories()));
            } else {
                recommendations.add("Complete your profile setup to get personalized calorie and macro targets. Then start logging your daily progress!");
            }
        }
        
        return recommendations;
    }
    
    /**
     * Intent-based routing that handles specific user intents before mode-based routing.
     * Returns a response string if intent was handled, null otherwise.
     */
    private String processIntentBasedRouting(User user, String message, CoachContext context) {
        String lowerMessage = message.toLowerCase().trim();
        
        // INTENT: "how many protein" or similar protein queries
        if ((lowerMessage.contains("how many") || lowerMessage.contains("how much")) && 
            (lowerMessage.contains("protein") || lowerMessage.contains("proteins"))) {
            if (context.getProteinTarget() != null) {
                return String.format("Your daily protein target is %.0f grams per day. " +
                        "This is calculated based on your goal (%s) and body weight (%.1f kg). " +
                        "Aim to distribute this across your meals for optimal muscle maintenance and growth.",
                        context.getProteinTarget(), context.getGoal(), user.getWeight());
            }
            return "I need your profile information to calculate your protein target. Please complete your profile with weight, height, age, activity level, and goal. Then I can give you a personalized protein target.";
        }
        
        // INTENT: "What am I eating tomorrow?" or "What I'm gonna eat tomorrow"
        if ((lowerMessage.contains("tomorrow") || lowerMessage.contains("tmr")) && 
            (lowerMessage.contains("eat") || lowerMessage.contains("meal") || lowerMessage.contains("food") || 
             lowerMessage.contains("gonna") || lowerMessage.contains("going to") || lowerMessage.contains("will"))) {
            
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            
            // Get meal plan for the user
            try {
                var mealPlanEntity = mealPlanService.getLatestMealPlan(user);
                if (mealPlanEntity != null) {
                    MealPlanResponseDTO mealPlan = mealPlanService.toDTO(mealPlanEntity);
                    if (mealPlan != null && mealPlan.getEntries() != null) {
                        // Find meals for tomorrow
                        List<MealPlanEntryDTO> tomorrowMeals = mealPlan.getEntries().stream()
                                .filter(entry -> entry.getDate().equals(tomorrow))
                                .sorted((a, b) -> {
                                    // Sort by meal type: breakfast, lunch, dinner
                                    int orderA = getMealTypeOrder(a.getMealType());
                                    int orderB = getMealTypeOrder(b.getMealType());
                                    return Integer.compare(orderA, orderB);
                                })
                                .collect(java.util.stream.Collectors.toList());
                        
                        if (!tomorrowMeals.isEmpty()) {
                            // Format response with tomorrow's meals
                            StringBuilder response = new StringBuilder("Here's what you're eating tomorrow (" + 
                                    tomorrow.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d")) + "):\n\n");
                            
                            for (MealPlanEntryDTO meal : tomorrowMeals) {
                                String mealType = meal.getMealType();
                                if (mealType != null && !mealType.isEmpty()) {
                                    response.append(mealType.substring(0, 1).toUpperCase() + mealType.substring(1))
                                            .append(": ");
                                } else {
                                    response.append("Meal: ");
                                }
                                response.append(meal.getName() != null ? meal.getName() : "Unknown meal");
                                if (meal.getCalories() != null) {
                                    response.append(" (").append(meal.getCalories()).append(" kcal");
                                    if (meal.getProtein() != null) {
                                        response.append(", ").append(meal.getProtein()).append("g protein");
                                    }
                                    response.append(")");
                                }
                                response.append("\n");
                            }
                            
                            // Calculate totals (handle nulls)
                            int totalCal = tomorrowMeals.stream()
                                    .mapToInt(m -> m.getCalories() != null ? m.getCalories() : 0)
                                    .sum();
                            int totalProtein = tomorrowMeals.stream()
                                    .mapToInt(m -> m.getProtein() != null ? m.getProtein() : 0)
                                    .sum();
                            int totalCarbs = tomorrowMeals.stream()
                                    .mapToInt(m -> m.getCarbs() != null ? m.getCarbs() : 0)
                                    .sum();
                            int totalFats = tomorrowMeals.stream()
                                    .mapToInt(m -> m.getFats() != null ? m.getFats() : 0)
                                    .sum();
                            
                            response.append("\nTotal: ").append(totalCal).append(" kcal, ")
                                    .append(totalProtein).append("g protein, ")
                                    .append(totalCarbs).append("g carbs, ")
                                    .append(totalFats).append("g fat");
                            
                            return response.toString();
                        }
                    }
                }
                
                // No meal plan found for tomorrow
                return "You don't have a meal plan yet for tomorrow. To get personalized meals, go to the Meal Plan page and click 'Generate Weekly Plan'. I'll create a meal plan based on your calorie targets and preferences.";
                
            } catch (Exception e) {
                // Error retrieving meal plan
                return "I couldn't retrieve your meal plan right now. Please try generating a meal plan from the Meal Plan page.";
            }
        }
        
        // INTENT: "workout" (standalone) - suggest a workout plan
        if (lowerMessage.equals("workout") || lowerMessage.equals("workouts") || 
            lowerMessage.equals("exercise") || lowerMessage.equals("exercises")) {
            StringBuilder response = new StringBuilder();
            if (context.getActivityLevel() != null) {
                int level = context.getActivityLevel();
                response.append("Here's a workout plan based on your activity level:\n\n");
                if (level <= 2) {
                    response.append("BEGINNER PLAN:\n- 3 days/week: Full body workouts\n- Focus: Cardio + light strength training\n- Duration: 30-45 min per session\n- Rest days: 4 days/week");
                } else if (level == 3) {
                    response.append("INTERMEDIATE PLAN:\n- 4-5 days/week: Split routine\n- Focus: Strength training + cardio\n- Duration: 45-60 min per session\n- Rest days: 2-3 days/week");
                } else {
                    response.append("ADVANCED PLAN:\n- 5-6 days/week: Specialized split\n- Focus: Progressive overload + conditioning\n- Duration: 60-90 min per session\n- Rest days: 1-2 days/week");
                }
            } else {
                response.append("Here's a starter workout plan:\n- 3 days/week: Full body workouts\n- Focus: Compound movements (squats, deadlifts, bench press, rows)\n- Duration: 30-45 min per session\n- Rest days: 4 days/week");
            }
            
            List<DailyCheckInResponse> checkIns = context.getRecentCheckIns();
            if (!checkIns.isEmpty()) {
                long workoutsDone = checkIns.stream()
                        .filter(c -> c.getWorkoutDone() != null && c.getWorkoutDone())
                        .count();
                response.append(String.format("\n\nYou've completed %d workouts in the last %d days. ", 
                        workoutsDone, checkIns.size()));
            }
            response.append("When are you available to start?");
            
            return response.toString();
        }
        
        // INTENT: "make me a workout plan" or similar
        if ((lowerMessage.contains("make") || lowerMessage.contains("create") || lowerMessage.contains("give") || 
             lowerMessage.contains("show") || lowerMessage.contains("generate")) && 
            (lowerMessage.contains("workout plan") || lowerMessage.contains("workout routine") || 
             lowerMessage.contains("training plan") || lowerMessage.contains("exercise plan"))) {
            // This will be handled by workout mode, return null to continue processing
            return null;
        }
        
        // INTENT: "how do i generate meal plan" or similar app help
        if ((lowerMessage.contains("how") || lowerMessage.contains("what")) && 
            (lowerMessage.contains("generate") || lowerMessage.contains("create") || lowerMessage.contains("make")) &&
            lowerMessage.contains("meal plan")) {
            // Return direct answer instead of routing to app_help mode
            return "To generate a meal plan:\n1. Go to the Meal Plan page\n2. Click 'Generate Weekly Plan'\n3. Set your meal preferences (optional) in Meal Preferences\n4. The AI will create a personalized weekly meal plan based on your profile and goals.";
        }
        
        // INTENT: Broad nutrition questions - "all nutrition", "everything about food", "all of the nutrition"
        if ((lowerMessage.contains("all") || lowerMessage.contains("everything")) && 
            (lowerMessage.contains("nutrition") || lowerMessage.contains("food") || lowerMessage.contains("eating") || 
             lowerMessage.contains("diet") || lowerMessage.contains("meal"))) {
            return generateNutritionSummary(user, context);
        }
        
        // INTENT: Broad workout questions - "all workouts", "everything about workouts", "all exercises"
        if ((lowerMessage.contains("all") || lowerMessage.contains("everything")) && 
            (lowerMessage.contains("workout") || lowerMessage.contains("exercise") || lowerMessage.contains("training"))) {
            return generateWorkoutSummary(user, context);
        }
        
        // INTENT: Just "all" - interpret based on message context
        if (lowerMessage.equals("all") || lowerMessage.equals("everything")) {
            // Check if message context suggests nutrition, otherwise general overview
            if (lowerMessage.contains("nutrition") || lowerMessage.contains("food")) {
                return generateNutritionSummary(user, context);
            } else {
                return generateGeneralOverview(user, context);
            }
        }
        
        // No specific intent matched
        return null;
    }
    
    /**
     * Helper to get meal type order for sorting.
     */
    private int getMealTypeOrder(String mealType) {
        if (mealType == null) return 999;
        switch (mealType.toLowerCase()) {
            case "breakfast": return 1;
            case "lunch": return 2;
            case "dinner": return 3;
            case "snack": return 4;
            default: return 999;
        }
    }
    
    /**
     * Generates actions for intent-based responses.
     */
    private List<String> generateActionsForIntent(String message, CoachContext context) {
        List<String> actions = new ArrayList<>();
        String lowerMessage = message.toLowerCase();
        
        if (lowerMessage.contains("tomorrow") && (lowerMessage.contains("eat") || lowerMessage.contains("meal"))) {
            if (context.getMealPlan() == null || !context.getMealPlan().containsKey("hasPlan")) {
                actions.add("Generate meal plan");
            }
            actions.add("View meal plan");
        }
        
        return actions;
    }
    
    /**
     * Processes chat message with unified intelligent behavior.
     * Analyzes the message content to determine intent and provides direct answers.
     * This is called only if intent routing didn't handle the request.
     */
    private String processChatMessageIntelligently(User user, String message, CoachContext context) {
        String lowerMessage = message.toLowerCase();
        
        // App help questions
        if (lowerMessage.contains("how to") || lowerMessage.contains("how do")) {
            return processAppHelpMessage(lowerMessage, context);
        }
        
        // Workout-related questions
        if (lowerMessage.contains("workout") || lowerMessage.contains("exercise") || lowerMessage.contains("training")) {
            return processWorkoutMessage(user, lowerMessage, context);
        }
        
        // Nutrition-related questions
        if (lowerMessage.contains("calorie") || lowerMessage.contains("macro") || lowerMessage.contains("meal") || 
            lowerMessage.contains("eat") || lowerMessage.contains("nutrition") || lowerMessage.contains("protein") || 
            lowerMessage.contains("food") || lowerMessage.contains("diet")) {
            return processNutritionMessage(user, lowerMessage, context);
        }
        
        // General messages (default)
        return processGeneralMessage(user, lowerMessage, context);
    }
    
    /**
     * Processes app help messages.
     */
    private String processAppHelpMessage(String message, CoachContext context) {
        if (message.contains("meal plan") || message.contains("generate meal")) {
            return "To generate a meal plan:\n1. Go to the Meal Plan page\n2. Click 'Generate Weekly Plan'\n3. Set your meal preferences (optional) in Meal Preferences\n4. The AI will create a personalized weekly meal plan based on your profile and goals.";
        }
        
        if (message.contains("profile") || message.contains("setup")) {
            return "To complete your profile:\n1. Go to Profile Setup or Edit Profile\n2. Enter your weight, height, age, gender\n3. Set your activity level and calorie goal\n4. Save your profile. This enables personalized recommendations.";
        }
        
        if (message.contains("dashboard") || message.contains("what is")) {
            return "The Dashboard shows:\n- Your body metrics (BMI, WHR)\n- Energy calculations (BMR, TDEE, goal calories)\n- Nutrition targets (protein, carbs, fat)\n- Health recommendations\n- AI Coach advice\n- Meal plan overview";
        }
        
        if (message.contains("grocery") || message.contains("shopping")) {
            return "To get your grocery list:\n1. Generate a meal plan first\n2. Go to the Meal Plan page\n3. Click 'View Grocery List'\n4. You'll see all ingredients needed for the week, organized by category.";
        }
        
        if (message.contains("export") || message.contains("pdf") || message.contains("download")) {
            return "To download your profile PDF:\n1. Go to the Dashboard\n2. Click 'Download Profile PDF' button\n3. A PDF with all your profile data, metrics, and targets will be generated.";
        }
        
        // If no specific app help intent matched, provide a direct answer based on the question
        return "Which app feature do you need help with? I can guide you through meal plans, profile setup, dashboard, grocery list, or PDF export.";
    }
    
    /**
     * Processes workout-related messages.
     */
    private String processWorkoutMessage(User user, String message, CoachContext context) {
        List<DailyCheckInResponse> checkIns = context.getRecentCheckIns();
        
        if (message.contains("plan") || message.contains("routine") || message.contains("program")) {
            StringBuilder response = new StringBuilder("Here's a workout plan based on your profile:\n\n");
            
            if (context.getActivityLevel() != null) {
                int level = context.getActivityLevel();
                if (level <= 2) {
                    response.append("BEGINNER PLAN:\n- 3 days/week: Full body workouts\n- Focus: Cardio + light strength training\n- Duration: 30-45 min per session\n- Rest days: 4 days/week");
                } else if (level == 3) {
                    response.append("INTERMEDIATE PLAN:\n- 4-5 days/week: Split routine\n- Focus: Strength training + cardio\n- Duration: 45-60 min per session\n- Rest days: 2-3 days/week");
                } else {
                    response.append("ADVANCED PLAN:\n- 5-6 days/week: Specialized split\n- Focus: Progressive overload + conditioning\n- Duration: 60-90 min per session\n- Rest days: 1-2 days/week");
                }
            } else {
                response.append("Start with 3 days/week full body workouts. Focus on compound movements: squats, deadlifts, bench press, rows.");
            }
            
            if (!checkIns.isEmpty()) {
                long workoutsDone = checkIns.stream()
                        .filter(c -> c.getWorkoutDone() != null && c.getWorkoutDone())
                        .count();
                response.append(String.format("\n\nYou've completed %d workouts in the last %d days. Keep it up!", 
                        workoutsDone, checkIns.size()));
            }
            
            return response.toString();
        }
        
        if (message.contains("how many") || message.contains("frequency") || message.contains("often")) {
            if (context.getActivityLevel() != null) {
                int level = context.getActivityLevel();
                return String.format("Based on your activity level (%d), aim for %d-%d workouts per week for optimal results.", 
                        level, Math.max(3, level), Math.min(6, level + 2));
            }
            return "Aim for 3-4 workouts per week for beginners, 4-5 for intermediate, and 5-6 for advanced. Start where you're comfortable and gradually increase.";
        }
        
        // For other workout questions, provide a direct answer
            if (context.getActivityLevel() != null) {
                int level = context.getActivityLevel();
            return String.format("Based on your activity level (%d), I recommend %d-%d workouts per week. " +
                    "Would you like me to create a specific workout plan for you?",
                    level, Math.max(3, level), Math.min(6, level + 2));
        }
        return "I can create a workout plan for you. What's your current fitness level? (beginner, intermediate, or advanced)";
    }
    
    /**
     * Processes nutrition-related messages.
     * Answers the user's question directly using their data.
     */
    private String processNutritionMessage(User user, String message, CoachContext context) {
        // Answer calories questions
        if (message.contains("calorie") || message.contains("how many calories")) {
            if (context.getGoalCalories() != null) {
                return String.format("Your daily calorie target is %.0f kcal based on your TDEE (%.0f kcal/day) and your goal (%s). " +
                        "Aim to stay within 100-200 kcal of this target for consistent progress.",
                        context.getGoalCalories(), context.getTdee(), context.getGoal());
            }
            return "Complete your profile to get your personalized calorie target. I'll calculate it based on your weight, height, age, activity level, and goal.";
        }
        
        // Answer protein questions (specific)
        if (message.contains("protein") && (message.contains("how many") || message.contains("how much") || 
            message.contains("target") || message.contains("need") || message.contains("should"))) {
            if (context.getProteinTarget() != null) {
                return String.format("Your daily protein target is %.0f grams per day. " +
                        "This supports muscle maintenance and growth based on your goal (%s) and body weight (%.1f kg). " +
                        "Spread this across 3-4 meals for optimal absorption.",
                        context.getProteinTarget(), context.getGoal(), user.getWeight());
            }
            return "I need your profile information to calculate your protein target. Please complete your profile with weight, height, age, activity level, and goal.";
        }
        
        // Answer macro questions (general)
        if (message.contains("macro") || message.contains("carb") || message.contains("fat")) {
            if (context.getProteinTarget() != null && context.getGoalCalories() != null) {
                double carbs = context.getGoalCalories() * 0.4 / 4;
                double fats = context.getGoalCalories() * 0.25 / 9;
                return String.format("Your daily macro targets:\n- Protein: %.0f g (for muscle maintenance/growth)\n- Carbs: ~%.0f g (for energy)\n- Fat: ~%.0f g (for hormone production)\n\n" +
                        "These are calculated based on your goal (%s) and body weight.",
                        context.getProteinTarget(), carbs, fats, context.getGoal());
            }
            return "I need your profile information to calculate your macro targets. Please complete your profile with weight, height, age, activity level, and goal.";
        }
        
        // Answer meal plan questions
        if (message.contains("meal plan") || message.contains("what should i eat") || message.contains("meals")) {
            if (context.getMealPlan() != null && context.getMealPlan().containsKey("hasPlan")) {
                return "You already have a meal plan! Check the Meal Plan page to see your weekly meals. You can also generate a new plan or modify your preferences.";
            }
            return "To get a personalized meal plan:\n1. Go to the Meal Plan page\n2. Click 'Generate Weekly Plan'\n3. The AI will create meals based on your calorie target, preferences, and dietary restrictions.";
        }
        
        // For other nutrition questions, provide a direct answer without spamming stats
        // Only mention stats if directly relevant to the question
        return "I can help with nutrition questions. What specific aspect would you like to know about? (calories, macros, meal planning, or meal plans)";
    }
    
    /**
     * Processes general messages.
     * Answers questions directly instead of returning generic responses.
     */
    private String processGeneralMessage(User user, String message, CoachContext context) {
        // Check if it's a greeting
        if (message.contains("hello") || message.contains("hi") || message.contains("hey")) {
            // Don't spam stats in greetings - keep it simple
            return "Hello! I'm your AI Coach. What can I help you with today?";
        }
        
        // Check if asking about progress
        if (message.contains("progress") || message.contains("how am i doing") || message.contains("how am i")) {
            List<DailyCheckInResponse> checkIns = context.getRecentCheckIns();
            if (!checkIns.isEmpty()) {
                long workoutsDone = checkIns.stream()
                        .filter(c -> c.getWorkoutDone() != null && c.getWorkoutDone())
                        .count();
                return String.format("You've completed %d workouts in the last %d days. That's a %.0f%% workout rate. " +
                        "Keep logging your daily progress to track your improvements!",
                        workoutsDone, checkIns.size(), (double) workoutsDone / checkIns.size() * 100);
            }
            return "Start logging your daily check-ins (weight, steps, workouts) to track your progress. I'll provide personalized insights based on your data.";
        }
        
        // Check if asking about workout plans
        if (message.contains("workout plan") || message.contains("workout routine") || message.contains("training plan")) {
            return processWorkoutMessage(user, message, context);
        }
        
        // Check if asking about meals/nutrition
        if (message.contains("meal") || message.contains("eat") || message.contains("nutrition") || message.contains("calorie") || message.contains("macro")) {
            return processNutritionMessage(user, message, context);
        }
        
        // Check if asking about app help
        if (message.contains("how") && (message.contains("generate") || message.contains("create") || message.contains("use") || message.contains("do"))) {
            return processAppHelpMessage(message, context);
        }
        
        // For other questions, provide a direct helpful answer without stat spamming
        // Don't repeat calorie/protein unless directly relevant to the question
        return "I can help with workouts, nutrition, meal planning, and tracking your progress. What specific question can I answer?";
    }
    
    /**
     * Generates a comprehensive nutrition summary for broad questions.
     * Follows structured format without stat spamming.
     */
    private String generateNutritionSummary(User user, CoachContext context) {
        StringBuilder summary = new StringBuilder();
        
        // Section 1: Daily Targets (only if available)
        if (context.getGoalCalories() != null || context.getProteinTarget() != null) {
            summary.append("YOUR DAILY TARGETS:\n");
        if (context.getGoalCalories() != null) {
                summary.append(String.format("• Calories: %.0f kcal/day\n", context.getGoalCalories()));
            }
            if (context.getProteinTarget() != null) {
                summary.append(String.format("• Protein: %.0f g/day\n", context.getProteinTarget()));
            }
            if (context.getGoalCalories() != null) {
                double carbs = context.getGoalCalories() * 0.4 / 4;
                double fats = context.getGoalCalories() * 0.25 / 9;
                summary.append(String.format("• Carbs: ~%.0f g/day\n", carbs));
                summary.append(String.format("• Fat: ~%.0f g/day\n", fats));
            }
            summary.append("\n");
        }
        
        // Section 2: Meal Planning
        summary.append("MEAL PLANNING:\n");
        if (context.getMealPlan() != null && context.getMealPlan().containsKey("hasPlan")) {
            summary.append("• You have an active meal plan\n");
            summary.append("• View it on the Meal Plan page\n");
        } else {
            summary.append("• Generate a personalized weekly meal plan\n");
            summary.append("• Based on your calorie targets and preferences\n");
        }
        summary.append("\n");
        
        // Section 3: Key Principles
        summary.append("KEY NUTRITION PRINCIPLES:\n");
        if (context.getGoal() != null) {
            String goalStr = context.getGoal();
            if (goalStr.contains("lose")) {
                summary.append("• Maintain a calorie deficit (500-750 kcal below TDEE)\n");
                summary.append("• Prioritize protein to preserve muscle mass\n");
            } else if (goalStr.contains("gain") || goalStr.contains("muscle")) {
                summary.append("• Eat at a slight calorie surplus (300-500 kcal above TDEE)\n");
                summary.append("• High protein intake for muscle growth\n");
            } else {
                summary.append("• Match calories to your TDEE for maintenance\n");
                summary.append("• Balanced macros for optimal health\n");
            }
        } else {
            summary.append("• Balance calories with your activity level\n");
            summary.append("• Prioritize protein for muscle maintenance\n");
        }
        summary.append("• Distribute protein across 3-4 meals daily\n");
        summary.append("• Stay hydrated (2.7-3.7L water/day)\n");
        summary.append("\n");
        
        // Optional follow-up suggestion (not a question)
        if (context.getMealPlan() == null || !context.getMealPlan().containsKey("hasPlan")) {
            summary.append("Next step: Generate your weekly meal plan to get started.");
        } else {
            summary.append("Next step: Review your meal plan and adjust preferences if needed.");
        }
        
        return summary.toString();
    }
    
    /**
     * Generates a comprehensive workout summary for broad questions.
     * Follows structured format without stat spamming.
     */
    private String generateWorkoutSummary(User user, CoachContext context) {
        StringBuilder summary = new StringBuilder();
        
        // Section 1: Recommended Plan (based on activity level)
        summary.append("RECOMMENDED WORKOUT PLAN:\n");
        if (context.getActivityLevel() != null) {
            int level = context.getActivityLevel();
            if (level <= 2) {
                summary.append("• Frequency: 3 days/week\n");
                summary.append("• Type: Full body workouts\n");
                summary.append("• Focus: Cardio + light strength training\n");
                summary.append("• Duration: 30-45 min per session\n");
            } else if (level == 3) {
                summary.append("• Frequency: 4-5 days/week\n");
                summary.append("• Type: Split routine\n");
                summary.append("• Focus: Strength training + cardio\n");
                summary.append("• Duration: 45-60 min per session\n");
            } else {
                summary.append("• Frequency: 5-6 days/week\n");
                summary.append("• Type: Specialized split\n");
                summary.append("• Focus: Progressive overload + conditioning\n");
                summary.append("• Duration: 60-90 min per session\n");
            }
        } else {
            summary.append("• Frequency: 3-4 days/week (start here)\n");
            summary.append("• Type: Full body workouts\n");
            summary.append("• Focus: Compound movements (squats, deadlifts, bench press, rows)\n");
            summary.append("• Duration: 30-45 min per session\n");
        }
        summary.append("\n");
        
        // Section 2: Progress Tracking (only if data available)
        List<DailyCheckInResponse> checkIns = context.getRecentCheckIns();
        if (!checkIns.isEmpty()) {
            long workoutsDone = checkIns.stream()
                    .filter(c -> c.getWorkoutDone() != null && c.getWorkoutDone())
                    .count();
            summary.append("YOUR RECENT PROGRESS:\n");
            summary.append(String.format("• Completed %d workouts in the last %d days\n", workoutsDone, checkIns.size()));
            double workoutRate = (double) workoutsDone / checkIns.size() * 100;
            summary.append(String.format("• Workout rate: %.0f%%\n", workoutRate));
            summary.append("\n");
        }
        
        // Section 3: Key Principles
        summary.append("WORKOUT PRINCIPLES:\n");
        summary.append("• Progressive overload: gradually increase weight or reps\n");
        summary.append("• Compound movements first (squats, deadlifts, presses)\n");
        summary.append("• Allow 48 hours rest between training same muscle groups\n");
        summary.append("• Track your workouts to monitor progress\n");
        summary.append("\n");
        
        // Optional follow-up suggestion (not a question)
        summary.append("Next step: Log your workouts to track progress and get personalized recommendations.");
        
        return summary.toString();
    }
    
    /**
     * Generates a general overview when user asks "all" without context.
     * Provides most relevant information based on available data.
     */
    private String generateGeneralOverview(User user, CoachContext context) {
        StringBuilder overview = new StringBuilder();
        
        // Prioritize nutrition if data is available, otherwise provide balanced overview
        boolean hasNutritionData = context.getGoalCalories() != null || context.getProteinTarget() != null;
        boolean hasWorkoutData = !context.getRecentCheckIns().isEmpty();
        
        if (hasNutritionData) {
            overview.append("NUTRITION OVERVIEW:\n");
            if (context.getGoalCalories() != null) {
                overview.append(String.format("• Daily calorie target: %.0f kcal\n", context.getGoalCalories()));
            }
            if (context.getProteinTarget() != null) {
                overview.append(String.format("• Daily protein target: %.0f g\n", context.getProteinTarget()));
            }
            if (context.getMealPlan() != null && context.getMealPlan().containsKey("hasPlan")) {
                overview.append("• Active meal plan available\n");
            } else {
                overview.append("• Generate a meal plan to get started\n");
            }
            overview.append("\n");
        }
        
        if (hasWorkoutData || context.getActivityLevel() != null) {
            overview.append("WORKOUT OVERVIEW:\n");
            if (context.getActivityLevel() != null) {
                int level = context.getActivityLevel();
                overview.append(String.format("• Activity level: %d\n", level));
                overview.append(String.format("• Recommended: %d-%d workouts/week\n", 
                        Math.max(3, level), Math.min(6, level + 2)));
            }
            if (hasWorkoutData) {
                List<DailyCheckInResponse> checkIns = context.getRecentCheckIns();
                long workoutsDone = checkIns.stream()
                        .filter(c -> c.getWorkoutDone() != null && c.getWorkoutDone())
                        .count();
                overview.append(String.format("• Recent: %d workouts completed\n", workoutsDone));
            }
            overview.append("\n");
        }
        
        if (!hasNutritionData && !hasWorkoutData) {
            overview.append("GET STARTED:\n");
            overview.append("• Complete your profile to get personalized targets\n");
            overview.append("• Start logging daily check-ins to track progress\n");
            overview.append("• Generate a meal plan for nutrition guidance\n");
            overview.append("\n");
        }
        
        // Optional follow-up suggestion
        if (!hasNutritionData) {
            overview.append("Next step: Complete your profile to unlock personalized nutrition and workout guidance.");
        } else if (context.getMealPlan() == null || !context.getMealPlan().containsKey("hasPlan")) {
            overview.append("Next step: Generate your weekly meal plan.");
        } else {
            overview.append("Next step: Log your workouts to track your fitness progress.");
        }
        
        return overview.toString();
    }
    
    /**
     * Generates suggested actions based on context.
     * Provides relevant actions based on user's profile and available features.
     */
    private List<String> generateActionsFromChat(CoachContext context) {
        List<String> actions = new ArrayList<>();
        
        if (context == null) {
            actions.add("View dashboard");
            actions.add("Complete Profile Setup");
            return actions;
        }
        
        // Always offer core actions
            actions.add("Log daily check-in");
            actions.add("View dashboard");
        
        // Add meal plan actions if relevant
        if (context.getMealPlan() == null || !context.getMealPlan().containsKey("hasPlan")) {
            actions.add("Generate meal plan");
        } else {
            actions.add("View meal plan");
        }
        
        return actions;
    }
}

