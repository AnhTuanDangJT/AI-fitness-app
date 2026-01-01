package com.aifitness.service;

import com.aifitness.ai.data.GlobalCuisineKnowledgeBase;
import com.aifitness.ai.data.GlobalCuisineKnowledgeBase.CuisineProfile;
import com.aifitness.ai.data.GlobalCuisineKnowledgeBase.MealBlueprint;
import com.aifitness.dto.DailyMacrosDTO;
import com.aifitness.dto.DailyMealPlanDTO;
import com.aifitness.dto.GroceryItem;
import com.aifitness.dto.MealPlanEntryDTO;
import com.aifitness.dto.MealPlanResponseDTO;
import com.aifitness.dto.RuleBasedMealPlanDocument;
import com.aifitness.entity.MealPlan;
import com.aifitness.entity.MealPlanEntry;
import com.aifitness.entity.User;
import com.aifitness.repository.MealPlanRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.Normalizer;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Meal Plan Service
 * 
 * Handles meal plan generation and retrieval.
 * Uses rule-based generation (no external AI yet).
 */
@Service
@Transactional
public class MealPlanService {
    
    private static final Logger logger = LoggerFactory.getLogger(MealPlanService.class);
    
    private final MealPlanRepository mealPlanRepository;
    private final NutritionService nutritionService;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public MealPlanService(MealPlanRepository mealPlanRepository,
                           NutritionService nutritionService) {
        this.mealPlanRepository = mealPlanRepository;
        this.nutritionService = nutritionService;
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * Helper method to get ingredients JSON for a meal name.
     * Maps hardcoded meal names to their ingredient lists.
     */
    private String getIngredientsForMeal(String mealName, String mealType) {
        // Normalize meal name for matching
        String normalizedName = mealName.toLowerCase().trim();
        
        // Map meal names to ingredients JSON
        Map<String, String> ingredientMap = new HashMap<>();
        
        // Breakfast meals
        ingredientMap.put("oatmeal with banana", "[{\"name\":\"rolled oats\",\"quantityText\":\"80g\"},{\"name\":\"banana\",\"quantityText\":\"1 medium\"},{\"name\":\"milk\",\"quantityText\":\"200ml\"}]");
        ingredientMap.put("greek yogurt with berries", "[{\"name\":\"Greek yogurt\",\"quantityText\":\"200g\"},{\"name\":\"mixed berries\",\"quantityText\":\"100g\"}]");
        ingredientMap.put("scrambled eggs with toast", "[{\"name\":\"eggs\",\"quantityText\":\"2 large\"},{\"name\":\"whole wheat bread\",\"quantityText\":\"2 slices\"},{\"name\":\"butter\",\"quantityText\":\"1 tbsp\"}]");
        ingredientMap.put("avocado toast with eggs", "[{\"name\":\"whole wheat bread\",\"quantityText\":\"2 slices\"},{\"name\":\"avocado\",\"quantityText\":\"1 medium\"},{\"name\":\"eggs\",\"quantityText\":\"2 large\"}]");
        ingredientMap.put("protein pancakes with syrup", "[{\"name\":\"flour\",\"quantityText\":\"100g\"},{\"name\":\"eggs\",\"quantityText\":\"2 large\"},{\"name\":\"milk\",\"quantityText\":\"150ml\"},{\"name\":\"maple syrup\",\"quantityText\":\"2 tbsp\"}]");
        ingredientMap.put("chia pudding with fruits", "[{\"name\":\"chia seeds\",\"quantityText\":\"30g\"},{\"name\":\"milk\",\"quantityText\":\"200ml\"},{\"name\":\"mixed fruits\",\"quantityText\":\"100g\"}]");
        ingredientMap.put("smoothie bowl with granola", "[{\"name\":\"frozen fruits\",\"quantityText\":\"150g\"},{\"name\":\"milk\",\"quantityText\":\"200ml\"},{\"name\":\"granola\",\"quantityText\":\"40g\"}]");
        
        // Lunch meals
        ingredientMap.put("grilled chicken + rice", "[{\"name\":\"chicken breast\",\"quantityText\":\"200g\"},{\"name\":\"white rice\",\"quantityText\":\"150g cooked\"},{\"name\":\"olive oil\",\"quantityText\":\"1 tbsp\"}]");
        ingredientMap.put("turkey wrap with vegetables", "[{\"name\":\"turkey breast\",\"quantityText\":\"150g\"},{\"name\":\"whole wheat tortilla\",\"quantityText\":\"1 large\"},{\"name\":\"mixed vegetables\",\"quantityText\":\"100g\"}]");
        ingredientMap.put("quinoa salad with chickpeas", "[{\"name\":\"quinoa\",\"quantityText\":\"100g cooked\"},{\"name\":\"chickpeas\",\"quantityText\":\"100g\"},{\"name\":\"mixed vegetables\",\"quantityText\":\"150g\"},{\"name\":\"olive oil\",\"quantityText\":\"1 tbsp\"}]");
        ingredientMap.put("salmon salad with mixed greens", "[{\"name\":\"salmon fillet\",\"quantityText\":\"150g\"},{\"name\":\"mixed greens\",\"quantityText\":\"100g\"},{\"name\":\"cherry tomatoes\",\"quantityText\":\"100g\"},{\"name\":\"olive oil\",\"quantityText\":\"1 tbsp\"}]");
        ingredientMap.put("chicken caesar salad", "[{\"name\":\"chicken breast\",\"quantityText\":\"200g\"},{\"name\":\"romaine lettuce\",\"quantityText\":\"150g\"},{\"name\":\"Caesar dressing\",\"quantityText\":\"2 tbsp\"},{\"name\":\"parmesan cheese\",\"quantityText\":\"30g\"}]");
        ingredientMap.put("vegetable curry with rice", "[{\"name\":\"mixed vegetables\",\"quantityText\":\"300g\"},{\"name\":\"brown rice\",\"quantityText\":\"150g cooked\"},{\"name\":\"coconut milk\",\"quantityText\":\"100ml\"},{\"name\":\"curry spices\",\"quantityText\":\"1 tbsp\"}]");
        ingredientMap.put("lentil soup with bread", "[{\"name\":\"lentils\",\"quantityText\":\"100g dry\"},{\"name\":\"whole grain bread\",\"quantityText\":\"2 slices\"},{\"name\":\"vegetables\",\"quantityText\":\"100g\"}]");
        
        // Dinner meals
        ingredientMap.put("salmon with veggies", "[{\"name\":\"salmon fillet\",\"quantityText\":\"200g\"},{\"name\":\"mixed vegetables\",\"quantityText\":\"200g\"},{\"name\":\"olive oil\",\"quantityText\":\"1 tbsp\"}]");
        ingredientMap.put("beef stir-fry with noodles", "[{\"name\":\"beef strips\",\"quantityText\":\"200g\"},{\"name\":\"noodles\",\"quantityText\":\"150g cooked\"},{\"name\":\"bell peppers\",\"quantityText\":\"100g\"},{\"name\":\"soy sauce\",\"quantityText\":\"2 tbsp\"}]");
        ingredientMap.put("baked chicken with sweet potato", "[{\"name\":\"chicken breast\",\"quantityText\":\"200g\"},{\"name\":\"sweet potato\",\"quantityText\":\"200g\"},{\"name\":\"olive oil\",\"quantityText\":\"1 tbsp\"}]");
        ingredientMap.put("pasta with marinara sauce", "[{\"name\":\"pasta\",\"quantityText\":\"100g dry\"},{\"name\":\"marinara sauce\",\"quantityText\":\"150g\"},{\"name\":\"parmesan cheese\",\"quantityText\":\"30g\"}]");
        ingredientMap.put("grilled steak with roasted vegetables", "[{\"name\":\"beef steak\",\"quantityText\":\"250g\"},{\"name\":\"mixed vegetables\",\"quantityText\":\"200g\"},{\"name\":\"olive oil\",\"quantityText\":\"2 tbsp\"}]");
        ingredientMap.put("pork tenderloin with quinoa", "[{\"name\":\"pork tenderloin\",\"quantityText\":\"200g\"},{\"name\":\"quinoa\",\"quantityText\":\"100g cooked\"}]");
        ingredientMap.put("fish tacos with vegetables", "[{\"name\":\"white fish fillet\",\"quantityText\":\"200g\"},{\"name\":\"corn tortillas\",\"quantityText\":\"3 medium\"},{\"name\":\"cabbage\",\"quantityText\":\"100g\"},{\"name\":\"lime\",\"quantityText\":\"1 medium\"}]");
        
        // Return ingredients if found, otherwise return empty array
        return ingredientMap.getOrDefault(normalizedName, "[]");
    }
    
    /**
     * Generates a weekly meal plan for a user using the shared AI helper.
     *
     * @param user The user to generate the meal plan for
     * @param startDate The start date of the week (typically Monday)
     * @return The generated meal plan
     */
    public MealPlan generateWeeklyMealPlanForUser(User user, LocalDate startDate) {
        logger.info("Rule-based AI mode active (no LLM)");
        logger.info("Generating deterministic meal plan for userId={}, startDate={}", 
                user.getId(), startDate);
        return generateWeeklyMealPlanForUserFallback(user, startDate);
    }
    
    /**
     * Validates that generated meals follow user preferences.
     */
    private boolean validatePreferencesFollowed(User user, List<MealPlanEntry> entries) {
        // Check cuisine preferences
        if (user.getFavoriteCuisines() != null && !user.getFavoriteCuisines().trim().isEmpty()) {
            String cuisines = user.getFavoriteCuisines().toLowerCase();
            if (cuisines.contains("asian") || cuisines.contains("chinese") || cuisines.contains("japanese") ||
                cuisines.contains("korean") || cuisines.contains("thai") || cuisines.contains("vietnamese")) {
                // Check for rice in meals
                boolean hasRice = entries.stream().anyMatch(entry -> {
                    String name = entry.getName().toLowerCase();
                    String ingredients = entry.getIngredients() != null ? entry.getIngredients().toLowerCase() : "";
                    return name.contains("rice") || ingredients.contains("rice");
                });
                if (!hasRice) {
                    return false; // Asian cuisine requires rice
                }
                
                // Check for Western staples (should not be present)
                boolean hasWesternStaples = entries.stream().anyMatch(entry -> {
                    String name = entry.getName().toLowerCase();
                    return name.contains("pasta") || name.contains("bread") || name.contains("sandwich") ||
                           name.contains("pizza") || name.contains("burger");
                });
                if (hasWesternStaples) {
                    return false; // Asian cuisine should not have Western staples
                }
            }
        }
        
        // Check preferred foods are included
        if (user.getPreferredFoods() != null && !user.getPreferredFoods().trim().isEmpty()) {
            String[] preferred = user.getPreferredFoods().split(",");
            for (String food : preferred) {
                String trimmed = food.trim().toLowerCase();
                boolean found = entries.stream().anyMatch(entry -> {
                    String name = entry.getName().toLowerCase();
                    String ingredients = entry.getIngredients() != null ? entry.getIngredients().toLowerCase() : "";
                    return name.contains(trimmed) || ingredients.contains(trimmed);
                });
                if (!found && (trimmed.contains("chicken") || trimmed.contains("tofu") || trimmed.contains("rice"))) {
                    // Important preferred foods should appear
                    return false;
                }
            }
        }
        
        // Check allergies and dislikes are avoided
        List<String> avoid = new ArrayList<>();
        if (user.getAllergies() != null && !user.getAllergies().trim().isEmpty()) {
            avoid.addAll(Arrays.asList(user.getAllergies().split(",")));
        }
        if (user.getDislikedFoods() != null && !user.getDislikedFoods().trim().isEmpty()) {
            avoid.addAll(Arrays.asList(user.getDislikedFoods().split(",")));
        }
        
        for (String avoidFood : avoid) {
            String trimmed = avoidFood.trim().toLowerCase();
            boolean found = entries.stream().anyMatch(entry -> {
                String name = entry.getName().toLowerCase();
                String ingredients = entry.getIngredients() != null ? entry.getIngredients().toLowerCase() : "";
                return name.contains(trimmed) || ingredients.contains(trimmed);
            });
            if (found) {
                return false; // Found a food that should be avoided
            }
        }
        
        return true;
    }
    
    /**
     * Parses AI response JSON into MealPlanEntry objects.
     */
    /**
     * Fallback method: Generates a weekly meal plan for a user using hardcoded data.
     * Used when AI is not available.
     * 
     * @param user The user to generate the meal plan for
     * @param startDate The start date of the week (typically Monday)
     * @return The generated meal plan
     */
    private MealPlan generateWeeklyMealPlanForUserFallback(User user, LocalDate startDate) {
        Optional<MealPlan> existingPlan = mealPlanRepository.findByUserAndWeekStartDate(user, startDate);
        existingPlan.ifPresent(mealPlanRepository::delete);
        
        MealPlan mealPlan = new MealPlan(user, startDate);
        mealPlan = mealPlanRepository.save(mealPlan);
        
        MacroTargets macroTargets = buildMacroTargets(user);
        Set<String> preferredFoods = parsePreferredFoods(user.getPreferredFoods());
        Set<String> dislikedFoods = parseDislikedFoods(user.getDislikedFoods());
        Set<String> cuisineKeywords = buildCuisineKeywords(user.getFavoriteCuisines());
        String dietaryPreference = user.getDietaryPreference();
        Set<String> usedMeals = new HashSet<>();
        
        Map<String, Double> mealSplit = Map.of(
            MealPlanEntry.BREAKFAST, 0.30,
            MealPlanEntry.LUNCH, 0.35,
            MealPlanEntry.DINNER, 0.35
        );
        
        for (int day = 0; day < 7; day++) {
            LocalDate date = startDate.plusDays(day);
            mealPlan.addEntry(createPersonalizedEntry(
                mealPlan, date, MealPlanEntry.BREAKFAST, macroTargets, mealSplit.get(MealPlanEntry.BREAKFAST),
                dietaryPreference, preferredFoods, dislikedFoods, cuisineKeywords, usedMeals, day, user.getFavoriteCuisines()));
            
            mealPlan.addEntry(createPersonalizedEntry(
                mealPlan, date, MealPlanEntry.LUNCH, macroTargets, mealSplit.get(MealPlanEntry.LUNCH),
                dietaryPreference, preferredFoods, dislikedFoods, cuisineKeywords, usedMeals, day + 7, user.getFavoriteCuisines()));
            
            mealPlan.addEntry(createPersonalizedEntry(
                mealPlan, date, MealPlanEntry.DINNER, macroTargets, mealSplit.get(MealPlanEntry.DINNER),
                dietaryPreference, preferredFoods, dislikedFoods, cuisineKeywords, usedMeals, day + 14, user.getFavoriteCuisines()));
        }
        
        mealPlan = mealPlanRepository.save(mealPlan);
        return mealPlan;
    }
    
    private MacroTargets buildMacroTargets(User user) {
        MacroTargets targets = new MacroTargets();
        targets.calories = 2000;
        targets.protein = user.getWeight() != null ? Math.max(90, user.getWeight() * 1.6) : 120;
        targets.fats = user.getWeight() != null ? Math.max(55, user.getWeight() * 0.8) : 70;
        targets.carbs = Math.max(180, (targets.calories - (targets.protein * 4) - (targets.fats * 9)) / 4);
        
        if (user.getWeight() != null && user.getHeight() != null && user.getAge() != null &&
            user.getSex() != null && user.getActivityLevel() != null && user.getCalorieGoal() != null) {
            double bmr = nutritionService.calculateBMR(user.getWeight(), user.getHeight(), user.getAge(), user.getSex());
            double tdee = nutritionService.calculateTDEE(bmr, user.getActivityLevel());
            double goalCalories = nutritionService.calculateGoalCalories(tdee, user.getCalorieGoal());
            double proteinTarget = nutritionService.calculateProtein(user.getCalorieGoal(), user.getWeight());
            double fatTarget = nutritionService.calculateFat(user.getWeight());
            double carbTarget = nutritionService.calculateCarbs(goalCalories, proteinTarget, fatTarget);
            
            targets.calories = goalCalories;
            targets.protein = proteinTarget;
            targets.fats = fatTarget;
            targets.carbs = carbTarget;
        }
        
        return targets;
    }
    
    private MealPlanEntry createPersonalizedEntry(
            MealPlan mealPlan,
            LocalDate date,
            String mealType,
            MacroTargets targets,
            double ratio,
            String dietaryPreference,
            Set<String> preferredFoods,
            Set<String> dislikedFoods,
            Set<String> cuisineKeywords,
            Set<String> usedMeals,
            int rotationIndex,
            String favoriteCuisinesRaw) {
        
        List<MealOption> knowledgeMeals = getKnowledgeBaseMeals(mealType, favoriteCuisinesRaw, dietaryPreference);
        List<MealOption> options = getMealOptions(mealType, dietaryPreference, dislikedFoods, cuisineKeywords);
        if (!knowledgeMeals.isEmpty()) {
            // Knowledge-base meals always take priority while keeping duplicates out.
            Map<String, MealOption> deduped = new LinkedHashMap<>();
            knowledgeMeals.forEach(meal -> deduped.putIfAbsent(meal.name, meal));
            options.forEach(meal -> deduped.putIfAbsent(meal.name, meal));
            options = new ArrayList<>(deduped.values());
        }
        if (options.isEmpty()) {
            options = getOmnivoreMeals(mealType);
        }
        
        MealOption selected = selectMealOption(options, preferredFoods, cuisineKeywords, usedMeals, rotationIndex);
        
        int calories = scaleMacro(selected.calories, targets.calories * ratio);
        int protein = scaleMacro(selected.protein, targets.protein * ratio);
        int carbs = scaleMacro(selected.carbs, targets.carbs * ratio);
        int fats = scaleMacro(selected.fats, targets.fats * ratio);
        
        return new MealPlanEntry(
            mealPlan,
            date,
            mealType,
            selected.name,
            calories,
            protein,
            carbs,
            fats,
            selected.ingredientsJson != null ? selected.ingredientsJson : "[]"
        );
    }
    
    private MealOption selectMealOption(
            List<MealOption> options,
            Set<String> preferredFoods,
            Set<String> cuisineKeywords,
            Set<String> usedMeals,
            int rotationIndex) {
        
        if (options == null || options.isEmpty()) {
            throw new IllegalArgumentException("No meal options available");
        }
        
        MealOption best = null;
        int bestScore = Integer.MIN_VALUE;
        boolean hasCuisinePreference = cuisineKeywords != null && !cuisineKeywords.isEmpty();
        
        for (MealOption option : options) {
            int score = 0;
            boolean matchesPreferred = containsPreferredKeyword(option, preferredFoods);
            boolean matchesCuisine = matchesCuisineKeyword(option, cuisineKeywords);
            
            if (matchesPreferred) {
                score += 6;
            }
            if (matchesCuisine) {
                score += 5;
            } else if (hasCuisinePreference) {
                score -= 2;
            }
            if (usedMeals.contains(option.name)) {
                score -= 4;
            }
            score += Math.abs(option.name.hashCode() + rotationIndex) % 3; // encourage variety
            
            if (score > bestScore) {
                best = option;
                bestScore = score;
            }
        }
        
        if (best == null) {
            best = options.get(rotationIndex % options.size());
        }
        
        usedMeals.add(best.name);
        return best;
    }
    
    private boolean containsPreferredKeyword(MealOption option, Set<String> preferredFoods) {
        if (preferredFoods == null || preferredFoods.isEmpty()) {
            return false;
        }
        String haystack = normalizeText(option.name + " " + option.ingredientsJson);
        return preferredFoods.stream().anyMatch(haystack::contains);
    }
    
    private boolean matchesCuisineKeyword(MealOption option, Set<String> cuisineKeywords) {
        if (cuisineKeywords == null || cuisineKeywords.isEmpty()) {
            return false;
        }
        String text = normalizeText(option.name + " " + option.ingredientsJson);
        return cuisineKeywords.stream().anyMatch(text::contains);
    }
    
    private int scaleMacro(int baseValue, double targetValue) {
        if (targetValue <= 0 && baseValue > 0) {
            return baseValue;
        }
        if (baseValue <= 0) {
            return Math.max(50, (int)Math.round(targetValue));
        }
        double scale = clamp(targetValue / baseValue, 0.7, 1.3);
        return (int)Math.max(50, Math.round(baseValue * scale));
    }
    
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
    
    private Set<String> parsePreferredFoods(String preferredFoods) {
        return parseCommaSeparatedValues(preferredFoods);
    }
    
    private Set<String> buildCuisineKeywords(String favoriteCuisines) {
        Set<String> baseKeywords = parseCommaSeparatedValues(favoriteCuisines);
        Set<String> expanded = new HashSet<>();
        
        for (String keyword : baseKeywords) {
            String normalized = normalizeText(keyword);
            expanded.add(normalized);
            
            if (normalized.contains("chau a") || normalized.contains("a dong") || normalized.contains("asian")) {
                expanded.add("asian");
                expanded.add("viet");
                expanded.add("thai");
                expanded.add("japanese");
                expanded.add("korean");
                expanded.add("chinese");
                expanded.add("noodles");
                expanded.add("rice");
            } else if (normalized.contains("viet")) {
                expanded.add("pho");
                expanded.add("bun");
                expanded.add("lemongrass");
            } else if (normalized.contains("thai")) {
                expanded.add("thai");
                expanded.add("basil");
                expanded.add("curry");
            } else if (normalized.contains("japan")) {
                expanded.add("teriyaki");
                expanded.add("miso");
                expanded.add("soba");
            } else if (normalized.contains("korean")) {
                expanded.add("kimchi");
                expanded.add("bulgogi");
            } else if (normalized.contains("mediterranean") || normalized.contains("greek")) {
                expanded.add("hummus");
                expanded.add("feta");
                expanded.add("shawarma");
            } else if (normalized.contains("italian")) {
                expanded.add("pasta");
                expanded.add("risotto");
                expanded.add("marinara");
            } else if (normalized.contains("mexican") || normalized.contains("latin")) {
                expanded.add("taco");
                expanded.add("burrito");
                expanded.add("enchilada");
            } else if (normalized.contains("indian")) {
                expanded.add("masala");
                expanded.add("tikka");
                expanded.add("dal");
            }
        }
        
        return expanded;
    }
    
    private Set<String> parseCommaSeparatedValues(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new HashSet<>();
        }
        return Arrays.stream(input.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(this::normalizeText)
                .collect(Collectors.toSet());
    }
    
    private String normalizeText(String input) {
        if (input == null) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");
        return normalized.toLowerCase(Locale.ROOT);
    }
    
    private static class MacroTargets {
        double calories;
        double protein;
        double carbs;
        double fats;
    }

    /**
     * Gets the latest meal plan for a user.
     * 
     * @param user The user
     * @return The latest meal plan, or null if none exists
     */
    public MealPlan getLatestMealPlan(User user) {
        System.out.println("[MealPlanService] getLatestMealPlan() - START");
        System.out.println("[MealPlanService] User: id=" + user.getId() + ", username=" + user.getUsername());
        
        System.out.println("[MealPlanService] Calling mealPlanRepository.findFirstByUserOrderByWeekStartDateDesc(userId=" + user.getId() + ")...");
        Optional<MealPlan> result = mealPlanRepository.findFirstByUserOrderByWeekStartDateDesc(user);
        
        System.out.println("[MealPlanService] Repository returned: " + (result.isPresent() ? "MealPlan found" : "Empty Optional"));
        
        if (result.isPresent()) {
            MealPlan mealPlan = result.get();
            System.out.println("[MealPlanService] MealPlan details: id=" + mealPlan.getId() + ", weekStartDate=" + mealPlan.getWeekStartDate() + ", entriesCount=" + (mealPlan.getEntries() != null ? mealPlan.getEntries().size() : "null"));
            System.out.println("[MealPlanService] Returning MealPlan");
            return mealPlan;
        } else {
            System.out.println("[MealPlanService] No meal plan found in database for user " + user.getId());
            System.out.println("[MealPlanService] Returning NULL");
            return null;
        }
    }
    
    /**
     * Converts a MealPlan entity to a MealPlanResponseDTO.
     */
    public MealPlanResponseDTO toDTO(MealPlan mealPlan) {
        System.out.println("[MealPlanService] toDTO() - START");
        System.out.println("[MealPlanService] Input MealPlan: " + (mealPlan == null ? "NULL" : "id=" + mealPlan.getId()));
        
        if (mealPlan == null) {
            System.out.println("[MealPlanService] MealPlan is NULL, returning NULL DTO");
            return null;
        }
        
        MealPlanResponseDTO dto = new MealPlanResponseDTO();
        dto.setId(mealPlan.getId());
        dto.setUserId(mealPlan.getUser().getId());
        dto.setWeekStartDate(mealPlan.getWeekStartDate());
        dto.setCreatedAt(mealPlan.getCreatedAt());
        
        System.out.println("[MealPlanService] Basic DTO fields set: id=" + dto.getId() + ", userId=" + dto.getUserId() + ", weekStartDate=" + dto.getWeekStartDate());
        
        // Convert entries
        System.out.println("[MealPlanService] Converting entries...");
        System.out.println("[MealPlanService] MealPlan.getEntries() is: " + (mealPlan.getEntries() == null ? "NULL" : "List with " + mealPlan.getEntries().size() + " entries"));
        
        List<MealPlanEntryDTO> entryDTOs = mealPlan.getEntries().stream()
                .map(this::entryToDTO)
                .sorted(Comparator.comparing(MealPlanEntryDTO::getDate)
                        .thenComparing(MealPlanEntryDTO::getMealType))
                .collect(Collectors.toList());
        dto.setEntries(entryDTOs);
        System.out.println("[MealPlanService] Converted " + entryDTOs.size() + " entries to DTOs");
        
        DailyMacrosDTO calculatedTargets = null;
        // Calculate daily targets from first day's entries
        if (!entryDTOs.isEmpty()) {
            Map<LocalDate, List<MealPlanEntryDTO>> entriesByDate = entryDTOs.stream()
                    .collect(Collectors.groupingBy(MealPlanEntryDTO::getDate));
            
            LocalDate firstDate = mealPlan.getWeekStartDate();
            List<MealPlanEntryDTO> firstDayEntries = entriesByDate.get(firstDate);
            if (firstDayEntries != null) {
                int totalCal = firstDayEntries.stream().mapToInt(MealPlanEntryDTO::getCalories).sum();
                int totalProtein = firstDayEntries.stream().mapToInt(MealPlanEntryDTO::getProtein).sum();
                int totalCarbs = firstDayEntries.stream().mapToInt(MealPlanEntryDTO::getCarbs).sum();
                int totalFats = firstDayEntries.stream().mapToInt(MealPlanEntryDTO::getFats).sum();
                
                calculatedTargets = new DailyMacrosDTO(totalCal, totalProtein, totalCarbs, totalFats);
                dto.setDailyTargets(calculatedTargets);
                System.out.println("[MealPlanService] Daily targets calculated: calories=" + totalCal + ", protein=" + totalProtein + ", carbs=" + totalCarbs + ", fats=" + totalFats);
            } else {
                System.out.println("[MealPlanService] No entries found for firstDate=" + firstDate);
            }
        } else {
            System.out.println("[MealPlanService] No entries to calculate daily targets from");
        }
        
        System.out.println("[MealPlanService] toDTO() - COMPLETE. Returning DTO with " + entryDTOs.size() + " entries");
        dto.setPlanDocument(buildRuleBasedPlanDocument(mealPlan, entryDTOs, calculatedTargets));
        return dto;
    }
    
    /**
     * Converts a MealPlanEntry entity to a MealPlanEntryDTO.
     */
    private MealPlanEntryDTO entryToDTO(MealPlanEntry entry) {
        return new MealPlanEntryDTO(
            entry.getId(),
            entry.getDate(),
            entry.getMealType(),
            entry.getName(),
            entry.getCalories(),
            entry.getProtein(),
            entry.getCarbs(),
            entry.getFats()
        );
    }

    private RuleBasedMealPlanDocument buildRuleBasedPlanDocument(MealPlan mealPlan,
                                                                 List<MealPlanEntryDTO> entryDTOs,
                                                                 DailyMacrosDTO dailyTargets) {
        if (mealPlan == null) {
            return null;
        }
        RuleBasedMealPlanDocument document = new RuleBasedMealPlanDocument();
        if (dailyTargets != null) {
            document.setDailyCalories(dailyTargets.getCalories());
        }
        document.setMacros(dailyTargets);

        if (entryDTOs != null && !entryDTOs.isEmpty()) {
            List<RuleBasedMealPlanDocument.MealItem> meals = entryDTOs.stream()
                    .map(dto -> {
                        RuleBasedMealPlanDocument.MealItem item = new RuleBasedMealPlanDocument.MealItem();
                        item.setDate(dto.getDate());
                        item.setMealType(dto.getMealType());
                        item.setName(dto.getName());
                        item.setCalories(dto.getCalories());
                        item.setProtein(dto.getProtein());
                        item.setCarbs(dto.getCarbs());
                        item.setFats(dto.getFats());
                        return item;
                    })
                    .collect(Collectors.toList());
            document.setMeals(meals);
        }

        List<GroceryItem> groceryItems = buildGroceryListFromEntries(mealPlan.getEntries());
        List<String> shoppingList = groceryItems.stream()
                .map(item -> {
                    String qty = item.getQuantityText();
                    if (qty == null || qty.isBlank()) {
                        return item.getName();
                    }
                    return item.getName() + " - " + qty;
                })
                .collect(Collectors.toList());
        document.setShoppingList(shoppingList);
        return document;
    }
    
    /**
     * Generates a meal entry based on targets and preferences.
     */
    private MealPlanEntry generateMeal(MealPlan mealPlan, LocalDate date, String mealType,
                                       int targetCal, int targetProtein, int targetCarbs, int targetFats,
                                       String dietaryPreference, Set<String> dislikedFoods, int dayIndex) {
        // Get meal options based on dietary preference
        List<MealOption> options = getMealOptions(mealType, dietaryPreference, dislikedFoods, Collections.emptySet());
        
        // Select meal based on day index (for variety)
        MealOption selected = options.get(dayIndex % options.size());
        
        // Adjust macros to match targets (simple scaling)
        int calories = Math.max(targetCal, selected.calories);
        int protein = Math.max(targetProtein, selected.protein);
        int carbs = Math.max(targetCarbs, selected.carbs);
        int fats = Math.max(targetFats, selected.fats);
        
        // Create entry with ingredients
        String ingredientsJson = selected.ingredientsJson != null ? selected.ingredientsJson : "[]";
        MealPlanEntry entry = new MealPlanEntry(
            mealPlan, date, mealType, selected.name,
            calories, protein, carbs, fats, ingredientsJson
        );
        
        return entry;
    }
    
    /**
     * Gets meal options based on meal type and dietary preference.
     */
    private List<MealOption> getMealOptions(String mealType, String dietaryPreference, Set<String> dislikedFoods, Set<String> cuisineKeywords) {
        List<MealOption> allOptions = new ArrayList<>();
        
        // Filter by dietary preference
        if (dietaryPreference == null || dietaryPreference.isEmpty() || "omnivore".equalsIgnoreCase(dietaryPreference)) {
            allOptions.addAll(getOmnivoreMeals(mealType));
        } else if ("vegetarian".equalsIgnoreCase(dietaryPreference)) {
            allOptions.addAll(getVegetarianMeals(mealType));
        } else if ("vegan".equalsIgnoreCase(dietaryPreference)) {
            allOptions.addAll(getVeganMeals(mealType));
        } else if ("pescatarian".equalsIgnoreCase(dietaryPreference)) {
            allOptions.addAll(getPescatarianMeals(mealType));
        } else if ("halal".equalsIgnoreCase(dietaryPreference)) {
            allOptions.addAll(getHalalMeals(mealType));
        } else if ("kosher".equalsIgnoreCase(dietaryPreference)) {
            allOptions.addAll(getKosherMeals(mealType));
        } else {
            // Default to omnivore
            allOptions.addAll(getOmnivoreMeals(mealType));
        }
        
        List<MealOption> cuisineSpecific = getCuisineSpecificMeals(mealType, cuisineKeywords);
        if (!cuisineSpecific.isEmpty()) {
            cuisineSpecific.addAll(allOptions);
            allOptions = cuisineSpecific;
        }
        
        // Filter out disliked foods
        return allOptions.stream()
                .filter(meal -> !containsDislikedFood(meal.name, dislikedFoods))
                .collect(Collectors.toList());
    }

    private List<MealOption> getKnowledgeBaseMeals(String mealType, String favoriteCuisinesRaw, String dietaryPreference) {
        List<MealBlueprint> blueprints = GlobalCuisineKnowledgeBase.getMealsForKeywords(favoriteCuisinesRaw, mealType);
        if ((blueprints == null || blueprints.isEmpty()) && dietaryPreference != null) {
            blueprints = GlobalCuisineKnowledgeBase.getMealsForKeywords(dietaryPreference, mealType);
        }
        if (blueprints == null || blueprints.isEmpty()) {
            return Collections.emptyList();
        }
        List<MealOption> options = new ArrayList<>();
        for (MealBlueprint blueprint : blueprints) {
            options.add(new MealOption(
                    blueprint.getEnglishName(),
                    blueprint.getCalories(),
                    blueprint.getProtein(),
                    blueprint.getCarbs(),
                    blueprint.getFats(),
                    blueprint.toIngredientsJson()
            ));
        }
        return options;
    }
    
    /**
     * Checks if a meal name contains any disliked foods.
     */
    private boolean containsDislikedFood(String mealName, Set<String> dislikedFoods) {
        if (dislikedFoods == null || dislikedFoods.isEmpty()) {
            return false;
        }
        String lowerMeal = mealName.toLowerCase();
        return dislikedFoods.stream()
                .anyMatch(disliked -> lowerMeal.contains(disliked.toLowerCase()));
    }
    
    /**
     * Parses disliked foods string into a set.
     */
    private Set<String> parseDislikedFoods(String dislikedFoods) {
        if (dislikedFoods == null || dislikedFoods.trim().isEmpty()) {
            return new HashSet<>();
        }
        return Arrays.stream(dislikedFoods.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }
    
    private List<MealOption> getCuisineSpecificMeals(String mealType, Set<String> cuisineKeywords) {
        if (cuisineKeywords == null || cuisineKeywords.isEmpty()) {
            return Collections.emptyList();
        }
        if (matchesAny(cuisineKeywords, "asian", "viet", "thai", "japanese", "korean", "chinese", "noodles", "rice", "pho")) {
            return getAsianMeals(mealType);
        }
        return Collections.emptyList();
    }
    
    private boolean matchesAny(Set<String> keywords, String... targets) {
        for (String target : targets) {
            if (keywords.contains(target)) {
                return true;
            }
        }
        return false;
    }
    
    private List<MealOption> getAsianMeals(String mealType) {
        List<MealOption> meals = new ArrayList<>();
        switch (mealType) {
            case MealPlanEntry.BREAKFAST:
                meals.add(new MealOption("Vietnamese chicken congee with ginger", 380, 28, 50, 8,
                    "[{\"name\":\"jasmine rice\",\"quantityText\":\"80g\"},{\"name\":\"shredded chicken\",\"quantityText\":\"120g\"},{\"name\":\"ginger\",\"quantityText\":\"5g\"}]"));
                meals.add(new MealOption("Japanese miso soup with tofu and rice", 360, 22, 48, 9,
                    "[{\"name\":\"miso paste\",\"quantityText\":\"1 tbsp\"},{\"name\":\"soft tofu\",\"quantityText\":\"120g\"},{\"name\":\"jasmine rice\",\"quantityText\":\"100g cooked\"}]"));
                meals.add(new MealOption("Thai basil omelette with jasmine rice", 400, 24, 45, 15,
                    "[{\"name\":\"eggs\",\"quantityText\":\"2 large\"},{\"name\":\"Thai basil\",\"quantityText\":\"10g\"},{\"name\":\"jasmine rice\",\"quantityText\":\"120g cooked\"}]"));
                break;
            case MealPlanEntry.LUNCH:
                meals.add(new MealOption("Vietnamese lemongrass chicken rice plate", 520, 42, 55, 16,
                    "[{\"name\":\"lemongrass chicken\",\"quantityText\":\"200g\"},{\"name\":\"jasmine rice\",\"quantityText\":\"180g cooked\"},{\"name\":\"pickled veggies\",\"quantityText\":\"80g\"}]"));
                meals.add(new MealOption("Teriyaki salmon bowl with steamed vegetables", 500, 38, 48, 18,
                    "[{\"name\":\"salmon fillet\",\"quantityText\":\"180g\"},{\"name\":\"jasmine rice\",\"quantityText\":\"150g cooked\"},{\"name\":\"vegetables\",\"quantityText\":\"120g\"}]"));
                meals.add(new MealOption("Korean bulgogi beef with kimchi and rice", 540, 40, 52, 20,
                    "[{\"name\":\"bulgogi beef\",\"quantityText\":\"200g\"},{\"name\":\"kimchi\",\"quantityText\":\"60g\"},{\"name\":\"short grain rice\",\"quantityText\":\"170g cooked\"}]"));
                break;
            case MealPlanEntry.DINNER:
                meals.add(new MealOption("Thai green curry with chicken and jasmine rice", 560, 38, 60, 22,
                    "[{\"name\":\"chicken thigh\",\"quantityText\":\"220g\"},{\"name\":\"green curry\",\"quantityText\":\"150g\"},{\"name\":\"jasmine rice\",\"quantityText\":\"180g cooked\"}]"));
                meals.add(new MealOption("Pho-inspired beef noodle soup", 530, 36, 58, 16,
                    "[{\"name\":\"rice noodles\",\"quantityText\":\"150g\"},{\"name\":\"beef sirloin\",\"quantityText\":\"180g\"},{\"name\":\"star anise broth\",\"quantityText\":\"400ml\"}]"));
                meals.add(new MealOption("Japanese soba noodles with tofu and veggies", 480, 30, 62, 14,
                    "[{\"name\":\"soba noodles\",\"quantityText\":\"120g\"},{\"name\":\"tofu\",\"quantityText\":\"150g\"},{\"name\":\"shiitake mushrooms\",\"quantityText\":\"80g\"}]"));
                break;
            default:
                break;
        }
        return meals;
    }
    
    // Meal option classes and data
    
    private static class MealOption {
        String name;
        int calories;
        int protein;
        int carbs;
        int fats;
        String ingredientsJson; // JSON string of ingredients
        
        MealOption(String name, int calories, int protein, int carbs, int fats) {
            this.name = name;
            this.calories = calories;
            this.protein = protein;
            this.carbs = carbs;
            this.fats = fats;
            this.ingredientsJson = "[]";
        }
        
        MealOption(String name, int calories, int protein, int carbs, int fats, String ingredientsJson) {
            this.name = name;
            this.calories = calories;
            this.protein = protein;
            this.carbs = carbs;
            this.fats = fats;
            this.ingredientsJson = ingredientsJson;
        }
    }
    
    // Hard-coded meal options for different dietary preferences
    
    private List<MealOption> getOmnivoreMeals(String mealType) {
        List<MealOption> meals = new ArrayList<>();
        switch (mealType) {
            case MealPlanEntry.BREAKFAST:
                meals.add(new MealOption("Scrambled eggs with whole wheat toast", 350, 20, 35, 12,
                    "[{\"name\":\"eggs\",\"quantityText\":\"2 large\"},{\"name\":\"whole wheat bread\",\"quantityText\":\"2 slices\"},{\"name\":\"butter\",\"quantityText\":\"1 tbsp\"}]"));
                meals.add(new MealOption("Greek yogurt with berries and granola", 320, 18, 45, 8,
                    "[{\"name\":\"Greek yogurt\",\"quantityText\":\"200g\"},{\"name\":\"mixed berries\",\"quantityText\":\"100g\"},{\"name\":\"granola\",\"quantityText\":\"30g\"}]"));
                meals.add(new MealOption("Oatmeal with banana and almonds", 380, 12, 55, 10,
                    "[{\"name\":\"rolled oats\",\"quantityText\":\"80g\"},{\"name\":\"banana\",\"quantityText\":\"1 medium\"},{\"name\":\"almonds\",\"quantityText\":\"20g\"},{\"name\":\"milk\",\"quantityText\":\"200ml\"}]"));
                meals.add(new MealOption("Avocado toast with poached eggs", 420, 18, 40, 20,
                    "[{\"name\":\"whole wheat bread\",\"quantityText\":\"2 slices\"},{\"name\":\"avocado\",\"quantityText\":\"1 medium\"},{\"name\":\"eggs\",\"quantityText\":\"2 large\"}]"));
                meals.add(new MealOption("Protein pancakes with maple syrup", 400, 25, 50, 12,
                    "[{\"name\":\"flour\",\"quantityText\":\"100g\"},{\"name\":\"eggs\",\"quantityText\":\"2 large\"},{\"name\":\"milk\",\"quantityText\":\"150ml\"},{\"name\":\"maple syrup\",\"quantityText\":\"2 tbsp\"}]"));
                break;
            case MealPlanEntry.LUNCH:
                meals.add(new MealOption("Grilled chicken breast with quinoa and vegetables", 450, 40, 45, 12,
                    "[{\"name\":\"chicken breast\",\"quantityText\":\"200g\"},{\"name\":\"quinoa\",\"quantityText\":\"100g cooked\"},{\"name\":\"mixed vegetables\",\"quantityText\":\"150g\"},{\"name\":\"olive oil\",\"quantityText\":\"1 tbsp\"}]"));
                meals.add(new MealOption("Salmon salad with mixed greens", 420, 35, 25, 20,
                    "[{\"name\":\"salmon fillet\",\"quantityText\":\"150g\"},{\"name\":\"mixed greens\",\"quantityText\":\"100g\"},{\"name\":\"cherry tomatoes\",\"quantityText\":\"100g\"},{\"name\":\"olive oil\",\"quantityText\":\"1 tbsp\"}]"));
                meals.add(new MealOption("Turkey and avocado wrap", 480, 35, 50, 18,
                    "[{\"name\":\"turkey breast\",\"quantityText\":\"150g\"},{\"name\":\"whole wheat tortilla\",\"quantityText\":\"1 large\"},{\"name\":\"avocado\",\"quantityText\":\"1/2 medium\"},{\"name\":\"lettuce\",\"quantityText\":\"50g\"}]"));
                meals.add(new MealOption("Beef stir-fry with brown rice", 520, 38, 55, 15,
                    "[{\"name\":\"beef strips\",\"quantityText\":\"200g\"},{\"name\":\"brown rice\",\"quantityText\":\"150g cooked\"},{\"name\":\"bell peppers\",\"quantityText\":\"100g\"},{\"name\":\"soy sauce\",\"quantityText\":\"2 tbsp\"}]"));
                meals.add(new MealOption("Chicken Caesar salad", 460, 42, 30, 22,
                    "[{\"name\":\"chicken breast\",\"quantityText\":\"200g\"},{\"name\":\"romaine lettuce\",\"quantityText\":\"150g\"},{\"name\":\"Caesar dressing\",\"quantityText\":\"2 tbsp\"},{\"name\":\"parmesan cheese\",\"quantityText\":\"30g\"}]"));
                break;
            case MealPlanEntry.DINNER:
                meals.add(new MealOption("Baked salmon with sweet potato and broccoli", 550, 45, 50, 20,
                    "[{\"name\":\"salmon fillet\",\"quantityText\":\"200g\"},{\"name\":\"sweet potato\",\"quantityText\":\"200g\"},{\"name\":\"broccoli\",\"quantityText\":\"150g\"},{\"name\":\"olive oil\",\"quantityText\":\"1 tbsp\"}]"));
                meals.add(new MealOption("Grilled steak with roasted vegetables", 580, 50, 35, 25,
                    "[{\"name\":\"beef steak\",\"quantityText\":\"250g\"},{\"name\":\"mixed vegetables\",\"quantityText\":\"200g\"},{\"name\":\"olive oil\",\"quantityText\":\"2 tbsp\"}]"));
                meals.add(new MealOption("Chicken pasta with marinara sauce", 520, 38, 60, 15,
                    "[{\"name\":\"chicken breast\",\"quantityText\":\"200g\"},{\"name\":\"pasta\",\"quantityText\":\"100g dry\"},{\"name\":\"marinara sauce\",\"quantityText\":\"150g\"},{\"name\":\"parmesan cheese\",\"quantityText\":\"30g\"}]"));
                meals.add(new MealOption("Pork tenderloin with quinoa and green beans", 540, 42, 45, 18,
                    "[{\"name\":\"pork tenderloin\",\"quantityText\":\"200g\"},{\"name\":\"quinoa\",\"quantityText\":\"100g cooked\"},{\"name\":\"green beans\",\"quantityText\":\"150g\"}]"));
                meals.add(new MealOption("Beef and vegetable stew", 500, 40, 40, 20,
                    "[{\"name\":\"beef chunks\",\"quantityText\":\"200g\"},{\"name\":\"potatoes\",\"quantityText\":\"150g\"},{\"name\":\"carrots\",\"quantityText\":\"100g\"},{\"name\":\"onions\",\"quantityText\":\"50g\"}]"));
                break;
            case MealPlanEntry.SNACK:
                meals.add(new MealOption("Apple with almond butter", 200, 6, 25, 10,
                    "[{\"name\":\"apple\",\"quantityText\":\"1 medium\"},{\"name\":\"almond butter\",\"quantityText\":\"2 tbsp\"}]"));
                meals.add(new MealOption("Protein shake", 180, 25, 15, 3,
                    "[{\"name\":\"protein powder\",\"quantityText\":\"30g\"},{\"name\":\"milk\",\"quantityText\":\"250ml\"}]"));
                meals.add(new MealOption("Mixed nuts", 220, 8, 10, 18,
                    "[{\"name\":\"mixed nuts\",\"quantityText\":\"40g\"}]"));
                meals.add(new MealOption("Greek yogurt", 150, 15, 10, 5,
                    "[{\"name\":\"Greek yogurt\",\"quantityText\":\"150g\"}]"));
                break;
        }
        return meals;
    }
    
    private List<MealOption> getVegetarianMeals(String mealType) {
        List<MealOption> meals = new ArrayList<>();
        switch (mealType) {
            case MealPlanEntry.BREAKFAST:
                meals.add(new MealOption("Greek yogurt with berries and granola", 320, 18, 45, 8,
                    "[{\"name\":\"Greek yogurt\",\"quantityText\":\"200g\"},{\"name\":\"mixed berries\",\"quantityText\":\"100g\"},{\"name\":\"granola\",\"quantityText\":\"30g\"}]"));
                meals.add(new MealOption("Oatmeal with banana and almonds", 380, 12, 55, 10,
                    "[{\"name\":\"rolled oats\",\"quantityText\":\"80g\"},{\"name\":\"banana\",\"quantityText\":\"1 medium\"},{\"name\":\"almonds\",\"quantityText\":\"20g\"},{\"name\":\"milk\",\"quantityText\":\"200ml\"}]"));
                meals.add(new MealOption("Scrambled eggs with whole wheat toast", 350, 20, 35, 12,
                    "[{\"name\":\"eggs\",\"quantityText\":\"2 large\"},{\"name\":\"whole wheat bread\",\"quantityText\":\"2 slices\"},{\"name\":\"butter\",\"quantityText\":\"1 tbsp\"}]"));
                meals.add(new MealOption("Avocado toast with feta cheese", 400, 15, 40, 18,
                    "[{\"name\":\"whole wheat bread\",\"quantityText\":\"2 slices\"},{\"name\":\"avocado\",\"quantityText\":\"1 medium\"},{\"name\":\"feta cheese\",\"quantityText\":\"50g\"}]"));
                break;
            case MealPlanEntry.LUNCH:
                meals.add(new MealOption("Quinoa salad with chickpeas and vegetables", 420, 18, 55, 12,
                    "[{\"name\":\"quinoa\",\"quantityText\":\"100g cooked\"},{\"name\":\"chickpeas\",\"quantityText\":\"100g\"},{\"name\":\"mixed vegetables\",\"quantityText\":\"150g\"},{\"name\":\"olive oil\",\"quantityText\":\"1 tbsp\"}]"));
                meals.add(new MealOption("Vegetarian wrap with hummus", 450, 15, 60, 15,
                    "[{\"name\":\"whole wheat tortilla\",\"quantityText\":\"1 large\"},{\"name\":\"hummus\",\"quantityText\":\"80g\"},{\"name\":\"mixed vegetables\",\"quantityText\":\"150g\"}]"));
                meals.add(new MealOption("Lentil soup with whole grain bread", 380, 20, 50, 8,
                    "[{\"name\":\"lentils\",\"quantityText\":\"100g dry\"},{\"name\":\"whole grain bread\",\"quantityText\":\"2 slices\"},{\"name\":\"vegetables\",\"quantityText\":\"100g\"}]"));
                meals.add(new MealOption("Caprese salad with balsamic", 400, 18, 35, 20,
                    "[{\"name\":\"mozzarella\",\"quantityText\":\"150g\"},{\"name\":\"tomatoes\",\"quantityText\":\"200g\"},{\"name\":\"basil\",\"quantityText\":\"10g\"},{\"name\":\"olive oil\",\"quantityText\":\"1 tbsp\"}]"));
                break;
            case MealPlanEntry.DINNER:
                meals.add(new MealOption("Vegetarian pasta with marinara sauce", 480, 20, 65, 12,
                    "[{\"name\":\"pasta\",\"quantityText\":\"100g dry\"},{\"name\":\"marinara sauce\",\"quantityText\":\"150g\"},{\"name\":\"parmesan cheese\",\"quantityText\":\"30g\"}]"));
                meals.add(new MealOption("Stuffed bell peppers with rice and beans", 520, 22, 60, 18,
                    "[{\"name\":\"bell peppers\",\"quantityText\":\"2 large\"},{\"name\":\"brown rice\",\"quantityText\":\"100g cooked\"},{\"name\":\"black beans\",\"quantityText\":\"100g\"},{\"name\":\"cheese\",\"quantityText\":\"50g\"}]"));
                meals.add(new MealOption("Vegetable curry with brown rice", 500, 18, 70, 15,
                    "[{\"name\":\"mixed vegetables\",\"quantityText\":\"300g\"},{\"name\":\"brown rice\",\"quantityText\":\"150g cooked\"},{\"name\":\"coconut milk\",\"quantityText\":\"100ml\"},{\"name\":\"curry spices\",\"quantityText\":\"1 tbsp\"}]"));
                meals.add(new MealOption("Eggplant parmesan", 550, 25, 55, 22,
                    "[{\"name\":\"eggplant\",\"quantityText\":\"300g\"},{\"name\":\"mozzarella\",\"quantityText\":\"150g\"},{\"name\":\"marinara sauce\",\"quantityText\":\"150g\"},{\"name\":\"parmesan cheese\",\"quantityText\":\"50g\"}]"));
                break;
            case MealPlanEntry.SNACK:
                meals.add(new MealOption("Apple with almond butter", 200, 6, 25, 10,
                    "[{\"name\":\"apple\",\"quantityText\":\"1 medium\"},{\"name\":\"almond butter\",\"quantityText\":\"2 tbsp\"}]"));
                meals.add(new MealOption("Mixed nuts", 220, 8, 10, 18,
                    "[{\"name\":\"mixed nuts\",\"quantityText\":\"40g\"}]"));
                meals.add(new MealOption("Greek yogurt", 150, 15, 10, 5,
                    "[{\"name\":\"Greek yogurt\",\"quantityText\":\"150g\"}]"));
                break;
        }
        return meals;
    }
    
    private List<MealOption> getVeganMeals(String mealType) {
        List<MealOption> meals = new ArrayList<>();
        switch (mealType) {
            case MealPlanEntry.BREAKFAST:
                meals.add(new MealOption("Oatmeal with banana and almonds", 380, 12, 55, 10));
                meals.add(new MealOption("Avocado toast", 350, 8, 40, 15));
                meals.add(new MealOption("Smoothie bowl with fruits and seeds", 320, 10, 50, 12));
                meals.add(new MealOption("Chia pudding with berries", 300, 8, 45, 10));
                break;
            case MealPlanEntry.LUNCH:
                meals.add(new MealOption("Quinoa salad with chickpeas and vegetables", 420, 18, 55, 12));
                meals.add(new MealOption("Vegan wrap with hummus", 450, 15, 60, 15));
                meals.add(new MealOption("Lentil soup with whole grain bread", 380, 20, 50, 8));
                meals.add(new MealOption("Buddha bowl with tahini", 480, 20, 65, 18));
                break;
            case MealPlanEntry.DINNER:
                meals.add(new MealOption("Vegetable curry with brown rice", 500, 18, 70, 15));
                meals.add(new MealOption("Stuffed bell peppers with rice and beans", 520, 22, 60, 18));
                meals.add(new MealOption("Vegan pasta with tomato sauce", 480, 15, 65, 12));
                meals.add(new MealOption("Tofu stir-fry with vegetables", 450, 25, 50, 15));
                break;
            case MealPlanEntry.SNACK:
                meals.add(new MealOption("Apple with almond butter", 200, 6, 25, 10));
                meals.add(new MealOption("Mixed nuts", 220, 8, 10, 18));
                meals.add(new MealOption("Hummus with vegetables", 180, 6, 20, 10));
                break;
        }
        return meals;
    }
    
    private List<MealOption> getPescatarianMeals(String mealType) {
        List<MealOption> meals = new ArrayList<>();
        meals.addAll(getVegetarianMeals(mealType));
        if (mealType.equals(MealPlanEntry.LUNCH) || mealType.equals(MealPlanEntry.DINNER)) {
            meals.add(new MealOption("Salmon salad with mixed greens", 420, 35, 25, 20));
            meals.add(new MealOption("Baked salmon with sweet potato and broccoli", 550, 45, 50, 20));
            meals.add(new MealOption("Tuna salad wrap", 450, 32, 45, 15));
        }
        return meals;
    }
    
    private List<MealOption> getHalalMeals(String mealType) {
        // Halal meals (no pork, alcohol, or non-halal meat)
        List<MealOption> meals = new ArrayList<>();
        switch (mealType) {
            case MealPlanEntry.BREAKFAST:
                meals.add(new MealOption("Scrambled eggs with whole wheat toast", 350, 20, 35, 12));
                meals.add(new MealOption("Greek yogurt with berries and granola", 320, 18, 45, 8));
                meals.add(new MealOption("Oatmeal with banana and almonds", 380, 12, 55, 10));
                break;
            case MealPlanEntry.LUNCH:
                meals.add(new MealOption("Grilled chicken breast with quinoa", 450, 40, 45, 12));
                meals.add(new MealOption("Salmon salad with mixed greens", 420, 35, 25, 20));
                meals.add(new MealOption("Lamb and vegetable stew", 480, 38, 40, 18));
                break;
            case MealPlanEntry.DINNER:
                meals.add(new MealOption("Baked salmon with sweet potato", 550, 45, 50, 20));
                meals.add(new MealOption("Grilled chicken with roasted vegetables", 520, 42, 40, 18));
                meals.add(new MealOption("Lamb curry with brown rice", 540, 40, 55, 20));
                break;
            case MealPlanEntry.SNACK:
                meals.add(new MealOption("Apple with almond butter", 200, 6, 25, 10));
                meals.add(new MealOption("Mixed nuts", 220, 8, 10, 18));
                break;
        }
        return meals;
    }
    
    private List<MealOption> getKosherMeals(String mealType) {
        // Kosher meals (similar to halal, but with kosher certification requirements)
        // For simplicity, using similar options to halal
        return getHalalMeals(mealType);
    }
    
    /**
     * Generates a weekly meal plan in the simple format (7 days with breakfast, lunch, dinner).
     * Returns hardcoded mock data for testing.
     * 
     * @return List of 7 DailyMealPlanDTO objects
     */
    public List<DailyMealPlanDTO> generateWeeklyMealPlanSimple() {
        List<DailyMealPlanDTO> weeklyPlan = new ArrayList<>();
        LocalDate startDate = LocalDate.now();
        
        // Get Monday of current week
        DayOfWeek dayOfWeek = startDate.getDayOfWeek();
        int daysToSubtract = dayOfWeek.getValue() - DayOfWeek.MONDAY.getValue();
        if (daysToSubtract < 0) {
            daysToSubtract += 7;
        }
        LocalDate monday = startDate.minusDays(daysToSubtract);
        
        // Day 1 - Monday
        weeklyPlan.add(new DailyMealPlanDTO(
            monday,
            "Oatmeal with banana",
            "Grilled chicken + rice",
            "Salmon with veggies",
            1450, 97, 175, 44
        ));
        
        // Day 2 - Tuesday
        weeklyPlan.add(new DailyMealPlanDTO(
            monday.plusDays(1),
            "Greek yogurt with berries",
            "Turkey wrap with vegetables",
            "Beef stir-fry with noodles",
            1400, 98, 173, 44
        ));
        
        // Day 3 - Wednesday
        weeklyPlan.add(new DailyMealPlanDTO(
            monday.plusDays(2),
            "Scrambled eggs with toast",
            "Quinoa salad with chickpeas",
            "Baked chicken with sweet potato",
            1400, 88, 157, 48
        ));
        
        // Day 4 - Thursday
        weeklyPlan.add(new DailyMealPlanDTO(
            monday.plusDays(3),
            "Avocado toast with eggs",
            "Salmon salad with mixed greens",
            "Pasta with marinara sauce",
            1450, 84, 147, 60
        ));
        
        // Day 5 - Friday
        weeklyPlan.add(new DailyMealPlanDTO(
            monday.plusDays(4),
            "Protein pancakes with syrup",
            "Chicken Caesar salad",
            "Grilled steak with roasted vegetables",
            1500, 124, 128, 62
        ));
        
        // Day 6 - Saturday
        weeklyPlan.add(new DailyMealPlanDTO(
            monday.plusDays(5),
            "Chia pudding with fruits",
            "Vegetable curry with rice",
            "Pork tenderloin with quinoa",
            1450, 78, 175, 48
        ));
        
        // Day 7 - Sunday
        weeklyPlan.add(new DailyMealPlanDTO(
            monday.plusDays(6),
            "Smoothie bowl with granola",
            "Lentil soup with bread",
            "Fish tacos with vegetables",
            1330, 74, 175, 36
        ));
        
        return weeklyPlan;
    }
    
    /**
     * Builds a grocery list from the user's current meal plan.
     * Aggregates ingredients by name and returns a list of GroceryItem.
     * 
     * @param user The user
     * @return List of GroceryItem
     */
    public List<GroceryItem> buildGroceryListForUser(User user) {
        // Get latest meal plan
        MealPlan mealPlan = getLatestMealPlan(user);
        
        if (mealPlan == null || mealPlan.getEntries() == null || mealPlan.getEntries().isEmpty()) {
            return new ArrayList<>();
        }
        
        return buildGroceryListFromEntries(mealPlan.getEntries());
    }

    private List<GroceryItem> buildGroceryListFromEntries(List<MealPlanEntry> entries) {
        if (entries == null || entries.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, GroceryItem> groceryMap = new HashMap<>();

        for (MealPlanEntry entry : entries) {
            String ingredientsJson = entry.getIngredients();
            if (ingredientsJson == null || ingredientsJson.trim().isEmpty()) {
                continue;
            }

            try {
                List<Map<String, String>> ingredients = objectMapper.readValue(
                        ingredientsJson,
                        new TypeReference<List<Map<String, String>>>() {}
                );

                for (Map<String, String> ingredient : ingredients) {
                    String name = ingredient.get("name");
                    String quantityText = ingredient.get("quantityText");

                    if (name == null || name.trim().isEmpty()) {
                        continue;
                    }

                    String normalizedName = name.trim().toLowerCase(Locale.ROOT);
                    GroceryItem existing = groceryMap.get(normalizedName);
                    if (existing != null) {
                        String existingQty = existing.getQuantityText();
                        String newQty = quantityText != null ? quantityText : "";
                        if (!newQty.isEmpty() && !newQty.equals(existingQty)) {
                            existing.setQuantityText(existingQty + " + " + newQty);
                        }
                    } else {
                        GroceryItem item = new GroceryItem(name.trim(), quantityText != null ? quantityText : "");
                        groceryMap.put(normalizedName, item);
                    }
                }
            } catch (Exception e) {
                logger.warn("Failed to parse ingredients for meal entry {}: {}", entry.getId(), e.getMessage());
            }
        }

        List<GroceryItem> groceryList = new ArrayList<>(groceryMap.values());
        groceryList.sort(Comparator.comparing(GroceryItem::getName, String.CASE_INSENSITIVE_ORDER));
        return groceryList;
    }
}

