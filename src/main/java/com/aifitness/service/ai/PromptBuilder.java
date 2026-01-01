package com.aifitness.service.ai;

import com.aifitness.dto.DailyMacrosDTO;
import com.aifitness.dto.MealPlanEntryDTO;
import com.aifitness.dto.MealPlanResponseDTO;
import com.aifitness.entity.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builds structured prompts for the shared AI helper.
 */
@Component
public class PromptBuilder {

    public AiPromptPayload buildChatPrompt(User user,
                                           String message,
                                           String language,
                                           MealPlanResponseDTO latestMealPlan) {
        String normalizedLanguage = "vi".equalsIgnoreCase(language) ? "vi" : "en";

        String systemPrompt = """
            You are the embedded AI fitness coach of the AI Fitness App.
            Always provide concise, encouraging answers (under 4 short paragraphs and roughly 120 words).
            Reference the provided user profile and the latest meal plan context when helpful.
            Never ask the user for information that already exists in the context.
            Always respond in the requested language and end with a single actionable suggestion.
            """;

        Map<String, Object> context = buildSharedProfileContext(user);
        context.put("language", normalizedLanguage);
        if (latestMealPlan != null && latestMealPlan.getEntries() != null && !latestMealPlan.getEntries().isEmpty()) {
            context.put("recentMealPlan", summarizeMealPlan(latestMealPlan));
        }

        String userPrompt = """
            User message: "%s"
            Respond in %s.
            If nutrition is mentioned, highlight how it maps to their plan without re-asking for preferences.
            """.formatted(message.trim(), normalizedLanguage.equals("vi") ? "professional Vietnamese" : "professional English");

        return new AiPromptPayload(systemPrompt, userPrompt, context);
    }

    public AiPromptPayload buildMealPrompt(User user,
                                           LocalDate weekStart,
                                           DailyMacrosDTO targets) {

        String systemPrompt = """
            You are a registered dietitian that generates safe weekly meal plans for the AI Fitness App.
            OUTPUT REQUIREMENT: Return ONLY valid JSON matching this shape:
            {
              "dailyCalories": number,
              "macros": { "calories": number, "protein": number, "carbs": number, "fats": number },
              "meals": [
                 {
                   "day": "Monday",
                   "mealType": "BREAKFAST|LUNCH|DINNER",
                   "name": "Meal name",
                   "calories": number,
                   "macros": { "protein": number, "carbs": number, "fats": number },
                   "ingredients": [ { "name": "ingredient", "quantityText": "e.g. 100g" } ]
                 }
              ],
              "shoppingList": [ "item + quantity", ... ]
            }
            Never include markdown fences, explanations, or text outside of the JSON object.
            """;

        Map<String, Object> context = buildSharedProfileContext(user);
        context.put("weekStartDate", weekStart.toString());
        Map<String, Object> targetBlock = new LinkedHashMap<>();
        targetBlock.put("calories", safeValue(targets.getCalories()));
        targetBlock.put("protein", safeValue(targets.getProtein()));
        targetBlock.put("carbs", safeValue(targets.getCarbs()));
        targetBlock.put("fats", safeValue(targets.getFats()));
        context.put("targets", targetBlock);

        Map<String, Object> formatBlock = new LinkedHashMap<>();
        formatBlock.put("mealsPerDay", 3);
        formatBlock.put("days", 7);
        formatBlock.put("mealTypes", List.of("BREAKFAST", "LUNCH", "DINNER"));
        context.put("format", formatBlock);
        context.put("safety", """
            Respect allergies, disliked foods, dietary rules, budget, maximum cooking time, and cuisine preferences.
            Meals must align with macros and stay realistic for a home cook.
            """);

        String userPrompt = """
            Generate a 7-day meal plan starting on %s for the authenticated user.
            Use the provided context instead of asking follow-up questions.
            Each day must include breakfast, lunch, and dinner and should stay close to the calorie/macro targets.
            """.formatted(weekStart);

        return new AiPromptPayload(systemPrompt, userPrompt, context);
    }

    private Map<String, Object> buildSharedProfileContext(User user) {
        Map<String, Object> profile = new LinkedHashMap<>();
        profile.put("name", nullSafe(user.getName()));
        profile.put("age", user.getAge());
        profile.put("gender", user.getSex());
        profile.put("heightCm", user.getHeight());
        profile.put("weightKg", user.getWeight());
        profile.put("activityLevel", nullSafe(user.getActivityLevel()));
        profile.put("goal", nullSafe(user.getCalorieGoal()));
        profile.put("preferredFoods", splitAndTrim(user.getPreferredFoods()));
        profile.put("dislikedFoods", splitAndTrim(user.getDislikedFoods()));
        profile.put("allergies", splitAndTrim(user.getAllergies()));
        profile.put("dietaryPreference", nullSafe(user.getDietaryPreference()));
        profile.put("favoriteCuisines", splitAndTrim(user.getFavoriteCuisines()));
        profile.put("maxCookingTimeMinutes", user.getMaxCookingTimePerMeal());
        profile.put("budgetPerDay", user.getMaxBudgetPerDay());
        return profile;
    }

    private Map<String, Object> summarizeMealPlan(MealPlanResponseDTO dto) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("weekStart", dto.getWeekStartDate());
        if (dto.getDailyTargets() != null) {
            Map<String, Object> daily = new LinkedHashMap<>();
            daily.put("calories", dto.getDailyTargets().getCalories());
            daily.put("protein", dto.getDailyTargets().getProtein());
            daily.put("carbs", dto.getDailyTargets().getCarbs());
            daily.put("fats", dto.getDailyTargets().getFats());
            summary.put("dailyTargets", daily);
        }

        List<MealPlanEntryDTO> entries = dto.getEntries();
        if (entries != null && !entries.isEmpty()) {
            List<Map<String, Object>> highlightedMeals = entries.stream()
                    .limit(5)
                    .map(entry -> {
                        Map<String, Object> meal = new LinkedHashMap<>();
                        meal.put("day", entry.getDate());
                        meal.put("mealType", entry.getMealType());
                        meal.put("name", entry.getName());
                        meal.put("calories", entry.getCalories());
                        return meal;
                    })
                    .collect(Collectors.toList());
            summary.put("sampleMeals", highlightedMeals);
        }
        return summary;
    }

    private List<String> splitAndTrim(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        String[] tokens = value.split(",");
        List<String> cleaned = new ArrayList<>();
        for (String token : tokens) {
            String trimmed = token.trim();
            if (!trimmed.isEmpty()) {
                cleaned.add(trimmed);
            }
        }
        return cleaned;
    }

    private Object nullSafe(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String str) {
            return str.isBlank() ? null : str;
        }
        return value;
    }

    private int safeValue(Integer value) {
        return value != null ? value : 0;
    }
}

