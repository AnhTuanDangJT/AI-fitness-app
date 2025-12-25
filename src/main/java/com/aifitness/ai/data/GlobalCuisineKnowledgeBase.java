package com.aifitness.ai.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Global cuisine knowledge base shared by the AI meal planner and coach.
 *
 * <p>This consolidates the curated datasets the product team provided so that:
 * <ul>
 *   <li>Meal plan prompts can inject cuisine-specific templates instead of relying
 *       on vague instructions.</li>
 *   <li>The rule-based fallback generator has authentic regional meals to pick from.</li>
 *   <li>Intent answers can cite the same ingredient → nutrition reasoning sheet.</li>
 * </ul>
 */
public final class GlobalCuisineKnowledgeBase {

    private static final List<CuisineProfile> PROFILES;
    private static final Map<String, String> INGREDIENT_LOGIC_EN;
    private static final Map<String, String> INGREDIENT_LOGIC_VI;

    static {
        List<CuisineProfile> profiles = new ArrayList<>();

        profiles.add(buildMediterranean());
        profiles.add(buildJapanese());
        profiles.add(buildKorean());
        profiles.add(buildItalian());
        profiles.add(buildIndian());
        profiles.add(buildMexican());
        profiles.add(buildVietnamese());
        profiles.add(buildPlantForward());

        PROFILES = Collections.unmodifiableList(profiles);

        INGREDIENT_LOGIC_EN = Map.ofEntries(
            Map.entry("chicken", "Lean protein that preserves muscle during deficits."),
            Map.entry("fish", "Provides omega-3 fats plus high-quality protein."),
            Map.entry("eggs", "Complete protein with satiating healthy fats."),
            Map.entry("rice", "Training fuel that restores glycogen for workouts."),
            Map.entry("oats", "Slow-digesting carbs that keep you full longer."),
            Map.entry("lentils", "Plant-based protein with gut-friendly fiber."),
            Map.entry("vegetables", "Micronutrients and volume for hunger control."),
            Map.entry("olive oil", "Heart-healthy monounsaturated fats.")
        );

        INGREDIENT_LOGIC_VI = Map.ofEntries(
            Map.entry("chicken", "Đạm nạc giúp giữ cơ khi cắt giảm calo."),
            Map.entry("fish", "Cung cấp omega-3 và nguồn protein chất lượng cao."),
            Map.entry("eggs", "Protein hoàn chỉnh kèm chất béo lành mạnh giúp no lâu."),
            Map.entry("rice", "Nguồn năng lượng phục hồi glycogen cho buổi tập."),
            Map.entry("oats", "Carb hấp thu chậm, tạo cảm giác no và ổn định năng lượng."),
            Map.entry("lentils", "Đạm thực vật kèm chất xơ tốt cho hệ tiêu hóa."),
            Map.entry("vegetables", "Giàu vi chất và tăng thể tích bữa ăn mà không tăng calo."),
            Map.entry("olive oil", "Chất béo không bão hòa tốt cho tim mạch.")
        );
    }

    private GlobalCuisineKnowledgeBase() {
    }

    // -------------------------------------------------------
    // Public API
    // -------------------------------------------------------

    public static List<CuisineProfile> matchCuisines(String favorites) {
        if (favorites == null || favorites.isBlank()) {
            return Collections.emptyList();
        }
        String normalized = favorites.toLowerCase(Locale.ROOT);
        return PROFILES.stream()
                .filter(profile -> profile.matches(normalized))
                .collect(Collectors.toList());
    }

    public static List<CuisineProfile> getAllProfiles() {
        return PROFILES;
    }

    public static List<CuisineProfile> getDefaultProfiles() {
        return PROFILES.subList(0, Math.min(3, PROFILES.size()));
    }

    public static List<MealBlueprint> getMeals(String cuisineCode, String mealType) {
        if (cuisineCode == null || mealType == null) {
            return Collections.emptyList();
        }
        return PROFILES.stream()
                .filter(p -> p.code.equalsIgnoreCase(cuisineCode))
                .findFirst()
                .map(p -> p.getMealsByType(mealType))
                .orElse(Collections.emptyList());
    }

    public static String buildCuisinePromptSection(List<CuisineProfile> profiles) {
        if (profiles == null || profiles.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (CuisineProfile profile : profiles) {
            sb.append(profile.englishName)
              .append(" • Fitness focus: ")
              .append(profile.fitnessProfile)
              .append("\n");

            profile.mealsByType.forEach((mealType, blueprints) -> {
                sb.append("  ").append(titleCase(mealType)).append(": ");
                String mealNames = blueprints.stream()
                        .limit(3)
                        .map(MealBlueprint::getEnglishName)
                        .collect(Collectors.joining(", "));
                sb.append(mealNames).append("\n");
            });
            if (!profile.snacksEnglish.isEmpty()) {
                sb.append("  Snacks: ").append(String.join(", ", profile.snacksEnglish)).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    public static String buildIngredientLogicPrompt(String language) {
        Map<String, String> source = "vi".equals(language) ? INGREDIENT_LOGIC_VI : INGREDIENT_LOGIC_EN;
        StringBuilder sb = new StringBuilder();
        source.forEach((ingredient, reason) -> sb
                .append("- ")
                .append(capitalize(ingredient))
                .append(": ")
                .append(reason)
                .append("\n"));
        return sb.toString();
    }

    public static Map<String, String> getIngredientLogic(String language) {
        return "vi".equals(language) ? INGREDIENT_LOGIC_VI : INGREDIENT_LOGIC_EN;
    }

    public static List<MealBlueprint> getMealsForKeywords(String favoritesOrPreference, String mealType) {
        List<CuisineProfile> profiles = favoritesOrPreference == null
                ? Collections.emptyList()
                : matchCuisines(favoritesOrPreference);
        if (profiles.isEmpty()) {
            return Collections.emptyList();
        }
        List<MealBlueprint> meals = new ArrayList<>();
        for (CuisineProfile profile : profiles) {
            meals.addAll(profile.getMealsByType(mealType));
        }
        return meals;
    }

    // -------------------------------------------------------
    // Data builders
    // -------------------------------------------------------

    private static CuisineProfile buildMediterranean() {
        Map<String, List<MealBlueprint>> meals = new LinkedHashMap<>();
        meals.put("BREAKFAST", List.of(
            meal("BREAKFAST", "Greek yogurt with honey and walnuts", "Sữa chua Hy Lạp với mật ong và óc chó",
                380, 24, 38, 14,
                ingredients(
                    "Greek yogurt", "200g",
                    "honey", "1 tbsp",
                    "walnuts", "20g")),
            meal("BREAKFAST", "Whole-grain toast with olive oil", "Bánh mì nguyên cám với dầu ô liu",
                320, 10, 35, 15,
                ingredients(
                    "whole-grain bread", "2 slices",
                    "extra-virgin olive oil", "1 tbsp")),
            meal("BREAKFAST", "Vegetable omelet with spinach", "Ốp la rau củ với rau bina",
                360, 28, 12, 20,
                ingredients(
                    "eggs", "3 large",
                    "spinach", "1 cup",
                    "tomatoes", "1/2 cup",
                    "onions", "1/4 cup"))
        ));

        meals.put("LUNCH", List.of(
            meal("LUNCH", "Grilled chicken salad", "Salad gà nướng",
                420, 45, 20, 18,
                ingredients(
                    "chicken breast", "180g",
                    "mixed lettuce", "2 cups",
                    "cucumber", "1/2 cup",
                    "olive oil", "1 tbsp")),
            meal("LUNCH", "Chickpea and feta salad", "Salad đậu gà với phô mai feta",
                410, 20, 45, 16,
                ingredients(
                    "chickpeas", "1 cup",
                    "feta cheese", "40g",
                    "grape tomatoes", "1 cup",
                    "olive oil", "1 tbsp")),
            meal("LUNCH", "Tuna salad wrap", "Bánh cuốn cá ngừ",
                450, 38, 40, 15,
                ingredients(
                    "tuna", "150g",
                    "whole-grain wrap", "1 large",
                    "mixed vegetables", "1 cup"))
        ));

        meals.put("DINNER", List.of(
            meal("DINNER", "Grilled salmon with roasted vegetables", "Cá hồi nướng với rau củ nướng",
                520, 40, 30, 24,
                ingredients(
                    "salmon fillet", "180g",
                    "zucchini", "1 cup",
                    "bell peppers", "1 cup",
                    "olive oil", "1 tbsp")),
            meal("DINNER", "Mediterranean lentil stew", "Súp đậu lăng Địa Trung Hải",
                480, 26, 60, 12,
                ingredients(
                    "lentils", "1 cup cooked",
                    "garlic", "2 cloves",
                    "onions", "1/2 cup",
                    "herbs", "oregano + thyme")),
            meal("DINNER", "Turkey meatballs with quinoa", "Thịt viên gà tây với quinoa",
                540, 42, 45, 18,
                ingredients(
                    "ground turkey", "200g",
                    "quinoa", "1 cup cooked",
                    "parsley", "2 tbsp"))
        ));

        List<String> snacksEn = List.of("Olives", "Mixed nuts", "Hummus with vegetables");
        List<String> snacksVi = List.of("Ô liu", "Hạt tổng hợp", "Hummus với rau củ");

        return new CuisineProfile(
            "mediterranean",
            List.of("mediterranean", "địa trung hải", "greek"),
            "Mediterranean Cuisine",
            "Ẩm thực Địa Trung Hải",
            "Heart-healthy, lean muscle, sustainable fat loss",
            meals,
            snacksEn,
            snacksVi
        );
    }

    private static CuisineProfile buildJapanese() {
        Map<String, List<MealBlueprint>> meals = new LinkedHashMap<>();
        meals.put("BREAKFAST", List.of(
            meal("BREAKFAST", "Rice with miso soup", "Cơm với súp miso",
                340, 15, 52, 8,
                ingredients(
                    "steamed rice", "1 cup",
                    "miso paste", "1 tbsp",
                    "tofu", "80g")),
            meal("BREAKFAST", "Tamagoyaki omelet", "Trứng cuộn tamagoyaki",
                310, 20, 18, 14,
                ingredients(
                    "eggs", "3 large",
                    "soy sauce", "1 tsp"))
        ));
        meals.put("LUNCH", List.of(
            meal("LUNCH", "Chicken bento box", "Cơm hộp gà Bento",
                480, 38, 55, 12,
                ingredients(
                    "chicken thigh", "150g",
                    "rice", "1 cup",
                    "steamed vegetables", "1 cup")),
            meal("LUNCH", "Soba noodles bowl", "Mì soba",
                450, 20, 60, 12,
                ingredients(
                    "buckwheat noodles", "90g dry",
                    "vegetables", "1 cup",
                    "soy sauce", "1 tbsp"))
        ));
        meals.put("DINNER", List.of(
            meal("DINNER", "Sushi platter (fish-focused)", "Sushi cá tươi",
                500, 35, 55, 10,
                ingredients(
                    "sushi rice", "1 cup",
                    "mixed fish", "180g",
                    "seaweed", "2 sheets")),
            meal("DINNER", "Stir-fried vegetables with tofu", "Rau xào với đậu hũ",
                420, 24, 40, 18,
                ingredients(
                    "tofu", "150g",
                    "mixed vegetables", "2 cups",
                    "sesame oil", "1 tbsp")),
            meal("DINNER", "Teriyaki salmon bowl", "Cơm cá hồi sốt teriyaki",
                520, 38, 55, 16,
                ingredients(
                    "salmon", "170g",
                    "rice", "1 cup",
                    "teriyaki sauce", "2 tbsp"))
        ));
        List<String> snacksEn = List.of("Edamame", "Roasted seaweed snacks");
        List<String> snacksVi = List.of("Đậu nành edamame", "Rong biển nướng");
        return new CuisineProfile(
            "japanese",
            List.of("japanese", "nhật", "japan"),
            "Japanese Cuisine",
            "Ẩm thực Nhật Bản",
            "Clean eating, fat loss, digestion support",
            meals,
            snacksEn,
            snacksVi
        );
    }

    private static CuisineProfile buildKorean() {
        Map<String, List<MealBlueprint>> meals = new LinkedHashMap<>();
        meals.put("BREAKFAST", List.of(
            meal("BREAKFAST", "Rice, eggs and kimchi", "Cơm, trứng và kimchi",
                360, 20, 48, 10,
                ingredients(
                    "rice", "1 cup",
                    "eggs", "2 large",
                    "kimchi", "1/4 cup")),
            meal("BREAKFAST", "Korean tofu soup", "Canh đậu phụ Hàn",
                330, 18, 30, 14,
                ingredients(
                    "soft tofu", "150g",
                    "vegetables", "1 cup"))
        ));
        meals.put("LUNCH", List.of(
            meal("LUNCH", "Bibimbap bowl", "Cơm trộn Bibimbap",
                520, 30, 65, 15,
                ingredients(
                    "rice", "1 cup",
                    "mixed vegetables", "1 cup",
                    "beef or chicken", "120g",
                    "egg", "1 sunny-side")),
            meal("LUNCH", "Light kimchi fried rice", "Cơm chiên kimchi ít dầu",
                480, 20, 60, 14,
                ingredients(
                    "rice", "1 cup",
                    "kimchi", "1/2 cup",
                    "eggs", "2 large"))
        ));
        meals.put("DINNER", List.of(
            meal("DINNER", "Lean Korean BBQ lettuce wraps", "BBQ Hàn ít béo cuốn xà lách",
                540, 38, 30, 24,
                ingredients(
                    "beef or chicken", "180g",
                    "garlic", "2 cloves",
                    "lettuce", "6 leaves")),
            meal("DINNER", "Spicy tofu stew", "Canh đậu phụ cay",
                460, 26, 35, 18,
                ingredients(
                    "tofu", "200g",
                    "gochujang chili paste", "1 tbsp",
                    "vegetables", "1.5 cups"))
        ));
        List<String> snacksEn = List.of("Roasted seaweed", "Boiled eggs");
        List<String> snacksVi = List.of("Rong biển rang", "Trứng luộc");
        return new CuisineProfile(
            "korean",
            List.of("korean", "hàn", "hanguk"),
            "Korean Cuisine",
            "Ẩm thực Hàn Quốc",
            "Metabolism boost, protein-rich",
            meals,
            snacksEn,
            snacksVi
        );
    }

    private static CuisineProfile buildItalian() {
        Map<String, List<MealBlueprint>> meals = new LinkedHashMap<>();
        meals.put("BREAKFAST", List.of(
            meal("BREAKFAST", "Whole-grain toast with ricotta", "Bánh mì nguyên cám với ricotta",
                340, 18, 42, 12,
                ingredients(
                    "whole-grain bread", "2 slices",
                    "ricotta cheese", "60g")),
            meal("BREAKFAST", "Oats with fruit", "Yến mạch với trái cây",
                360, 12, 55, 10,
                ingredients(
                    "oats", "1 cup cooked",
                    "mixed berries", "1/2 cup"))
        ));
        meals.put("LUNCH", List.of(
            meal("LUNCH", "Whole-wheat pasta with chicken", "Mì nguyên cám với gà",
                520, 38, 60, 14,
                ingredients(
                    "whole-wheat pasta", "100g dry",
                    "chicken breast", "180g",
                    "tomato sauce", "1/2 cup")),
            meal("LUNCH", "Caprese salad", "Salad Caprese",
                410, 25, 20, 24,
                ingredients(
                    "mozzarella", "120g",
                    "tomatoes", "1 cup",
                    "olive oil", "1 tbsp"))
        ));
        meals.put("DINNER", List.of(
            meal("DINNER", "Grilled fish with vegetables", "Cá nướng với rau củ",
                500, 42, 28, 20,
                ingredients(
                    "white fish", "200g",
                    "vegetables", "1.5 cups")),
            meal("DINNER", "Light parmesan risotto", "Risotto nhẹ với parmesan",
                520, 20, 70, 16,
                ingredients(
                    "arborio rice", "3/4 cup dry",
                    "broth", "2 cups",
                    "parmesan", "30g"))
        ));
        return new CuisineProfile(
            "italian",
            List.of("italian", "ý", "italy"),
            "Italian Cuisine (portion-controlled)",
            "Ẩm thực Ý (kiểm soát khẩu phần)",
            "Energy and muscle gain with mindful portions",
            meals,
            List.of(),
            List.of()
        );
    }

    private static CuisineProfile buildIndian() {
        Map<String, List<MealBlueprint>> meals = new LinkedHashMap<>();
        meals.put("BREAKFAST", List.of(
            meal("BREAKFAST", "Oats upma", "Yến mạch upma",
                360, 12, 55, 12,
                ingredients(
                    "oats", "1 cup",
                    "mixed vegetables", "1 cup")),
            meal("BREAKFAST", "Idli with sambar", "Bánh idli với canh sambar",
                330, 12, 55, 6,
                ingredients(
                    "idli", "2 pieces",
                    "sambar", "1 cup"))
        ));
        meals.put("LUNCH", List.of(
            meal("LUNCH", "Dal with brown rice", "Đậu lăng với gạo lứt",
                460, 20, 68, 10,
                ingredients(
                    "lentil dal", "1 cup",
                    "brown rice", "1 cup")),
            meal("LUNCH", "Chickpea curry", "Cà ri đậu gà",
                430, 18, 50, 14,
                ingredients(
                    "chickpeas", "1 cup",
                    "spice blend", "garam masala + turmeric"))
        ));
        meals.put("DINNER", List.of(
            meal("DINNER", "Grilled tandoori chicken", "Gà tandoori nướng",
                520, 46, 20, 24,
                ingredients(
                    "chicken", "200g",
                    "yogurt marinade", "1/2 cup",
                    "spices", "tandoori blend")),
            meal("DINNER", "Mixed vegetable curry", "Cà ri rau củ",
                450, 15, 55, 16,
                ingredients(
                    "mixed vegetables", "2 cups",
                    "spice blend", "cumin + coriander"))
        ));
        return new CuisineProfile(
            "indian",
            List.of("indian", "ấn độ", "india"),
            "Indian Cuisine",
            "Ẩm thực Ấn Độ",
            "High vegetarian protein and metabolic support",
            meals,
            List.of(),
            List.of()
        );
    }

    private static CuisineProfile buildMexican() {
        Map<String, List<MealBlueprint>> meals = new LinkedHashMap<>();
        meals.put("BREAKFAST", List.of(
            meal("BREAKFAST", "Eggs, avocado and tortilla", "Trứng, bơ và tortilla",
                380, 22, 28, 20,
                ingredients(
                    "eggs", "2 large",
                    "avocado", "1/2 medium",
                    "corn tortilla", "2 pieces"))
        ));
        meals.put("LUNCH", List.of(
            meal("LUNCH", "Chicken tacos", "Taco gà",
                480, 38, 45, 18,
                ingredients(
                    "chicken", "150g",
                    "tortillas", "3 small",
                    "vegetables", "1 cup")),
            meal("LUNCH", "Burrito bowl", "Bát burrito",
                520, 32, 60, 16,
                ingredients(
                    "rice", "1 cup",
                    "beans", "1/2 cup",
                    "chicken or beef", "120g"))
        ));
        meals.put("DINNER", List.of(
            meal("DINNER", "Grilled fish tacos", "Taco cá nướng",
                500, 35, 48, 18,
                ingredients(
                    "white fish", "160g",
                    "tortillas", "3 small",
                    "cabbage slaw", "1 cup")),
            meal("DINNER", "Stuffed peppers with beans", "Ớt chuông nhồi đậu",
                470, 28, 50, 16,
                ingredients(
                    "bell peppers", "2 large",
                    "beans", "1 cup",
                    "lean meat", "120g"))
        ));
        return new CuisineProfile(
            "mexican",
            List.of("mexican", "mexico", "latin"),
            "Mexican Cuisine (healthy style)",
            "Ẩm thực Mexico lành mạnh",
            "High-energy meals that still support lean muscle",
            meals,
            List.of(),
            List.of()
        );
    }

    private static CuisineProfile buildVietnamese() {
        Map<String, List<MealBlueprint>> meals = new LinkedHashMap<>();
        meals.put("BREAKFAST", List.of(
            meal("BREAKFAST", "Beef or chicken pho", "Phở bò hoặc phở gà",
                430, 28, 55, 10,
                ingredients(
                    "rice noodles", "1 bowl",
                    "beef or chicken", "150g",
                    "herbs", "Thai basil + cilantro")),
            meal("BREAKFAST", "Egg banh mi", "Bánh mì trứng",
                420, 20, 48, 16,
                ingredients(
                    "baguette", "1 small",
                    "eggs", "2 fried",
                    "vegetables", "pickled carrots + cucumbers"))
        ));
        meals.put("LUNCH", List.of(
            meal("LUNCH", "Bun thit nuong bowl", "Bún thịt nướng",
                520, 32, 60, 16,
                ingredients(
                    "rice vermicelli", "1 cup",
                    "grilled pork", "150g",
                    "herbs", "mint + basil")),
            meal("LUNCH", "Vietnamese chicken cabbage salad", "Gỏi gà",
                450, 30, 32, 18,
                ingredients(
                    "shredded chicken", "150g",
                    "cabbage", "2 cups",
                    "herbs", "coriander + mint"))
        ));
        meals.put("DINNER", List.of(
            meal("DINNER", "Steamed fish with rice", "Cá hấp với cơm trắng",
                500, 42, 50, 12,
                ingredients(
                    "white fish", "200g",
                    "rice", "1 cup",
                    "ginger", "1 knob")),
            meal("DINNER", "Vegetable soup (canh rau)", "Canh rau",
                360, 18, 40, 10,
                ingredients(
                    "seasonal vegetables", "2 cups",
                    "light broth", "2 cups"))
        ));
        return new CuisineProfile(
            "vietnamese",
            List.of("vietnamese", "việt", "viet"),
            "Vietnamese Cuisine",
            "Ẩm thực Việt Nam",
            "Lean and digestion-friendly meals",
            meals,
            List.of(),
            List.of()
        );
    }

    private static CuisineProfile buildPlantForward() {
        Map<String, List<MealBlueprint>> meals = new LinkedHashMap<>();
        meals.put("BREAKFAST", List.of(
            meal("BREAKFAST", "Smoothie bowl with toppings", "Smoothie bowl với topping",
                360, 12, 55, 12,
                ingredients(
                    "frozen fruit", "1.5 cups",
                    "plant milk", "1 cup",
                    "granola", "1/4 cup")),
            meal("BREAKFAST", "Oatmeal with fruits", "Cháo yến mạch với trái cây",
                340, 10, 58, 8,
                ingredients(
                    "rolled oats", "1 cup cooked",
                    "seasonal fruits", "1/2 cup"))
        ));
        meals.put("LUNCH", List.of(
            meal("LUNCH", "Lentil salad", "Salad đậu lăng",
                430, 24, 50, 12,
                ingredients(
                    "cooked lentils", "1 cup",
                    "mixed vegetables", "1.5 cups")),
            meal("LUNCH", "Tofu stir-fry", "Đậu hũ xào rau củ",
                420, 26, 40, 16,
                ingredients(
                    "tofu", "180g",
                    "vegetables", "2 cups"))
        ));
        meals.put("DINNER", List.of(
            meal("DINNER", "Chickpea curry", "Cà ri đậu gà chay",
                440, 20, 55, 14,
                ingredients(
                    "chickpeas", "1 cup",
                    "vegetables", "1.5 cups")),
            meal("DINNER", "Tofu vegetable stir-fry", "Đậu hũ xào rau",
                430, 28, 42, 16,
                ingredients(
                    "firm tofu", "200g",
                    "mixed vegetables", "2 cups"))
        ));
        return new CuisineProfile(
            "plant",
            List.of("vegetarian", "vegan", "plant", "plant-based"),
            "Vegetarian / Vegan",
            "Chế độ chay / thuần chay",
            "Fat loss and gut health with plant protein",
            meals,
            List.of(),
            List.of()
        );
    }

    // -------------------------------------------------------
    // Helper builders
    // -------------------------------------------------------

    private static MealBlueprint meal(String mealType,
                                      String englishName,
                                      String vietnameseName,
                                      int calories,
                                      int protein,
                                      int carbs,
                                      int fats,
                                      List<Ingredient> ingredients) {
        return new MealBlueprint(mealType, englishName, vietnameseName, calories, protein, carbs, fats, ingredients);
    }

    private static List<Ingredient> ingredients(String... values) {
        List<Ingredient> list = new ArrayList<>();
        for (int i = 0; i < values.length; i += 2) {
            String name = values[i];
            String qty = (i + 1) < values.length ? values[i + 1] : "";
            list.add(new Ingredient(name, qty));
        }
        return list;
    }

    private static String titleCase(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String lower = value.toLowerCase(Locale.ROOT);
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private static String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    // -------------------------------------------------------
    // Nested data holders
    // -------------------------------------------------------

    public static final class MealBlueprint {
        private final String mealType;
        private final String englishName;
        private final String vietnameseName;
        private final int calories;
        private final int protein;
        private final int carbs;
        private final int fats;
        private final List<Ingredient> ingredients;

        private MealBlueprint(String mealType,
                              String englishName,
                              String vietnameseName,
                              int calories,
                              int protein,
                              int carbs,
                              int fats,
                              List<Ingredient> ingredients) {
            this.mealType = mealType;
            this.englishName = englishName;
            this.vietnameseName = vietnameseName;
            this.calories = calories;
            this.protein = protein;
            this.carbs = carbs;
            this.fats = fats;
            this.ingredients = ingredients;
        }

        public String getMealType() {
            return mealType;
        }

        public String getEnglishName() {
            return englishName;
        }

        public String getVietnameseName() {
            return vietnameseName;
        }

        public int getCalories() {
            return calories;
        }

        public int getProtein() {
            return protein;
        }

        public int getCarbs() {
            return carbs;
        }

        public int getFats() {
            return fats;
        }

        public String toIngredientsJson() {
            if (ingredients == null || ingredients.isEmpty()) {
                return "[]";
            }
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < ingredients.size(); i++) {
                Ingredient ingredient = ingredients.get(i);
                sb.append("{\"name\":\"")
                  .append(escape(ingredient.name))
                  .append("\",\"quantityText\":\"")
                  .append(escape(ingredient.quantity))
                  .append("\"}");
                if (i < ingredients.size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("]");
            return sb.toString();
        }

        private String escape(String value) {
            if (value == null) {
                return "";
            }
            return value.replace("\"", "\\\"");
        }
    }

    public static final class CuisineProfile {
        private final String code;
        private final List<String> aliases;
        private final String englishName;
        private final String vietnameseName;
        private final String fitnessProfile;
        private final Map<String, List<MealBlueprint>> mealsByType;
        private final List<String> snacksEnglish;
        private final List<String> snacksVietnamese;

        private CuisineProfile(String code,
                               List<String> aliases,
                               String englishName,
                               String vietnameseName,
                               String fitnessProfile,
                               Map<String, List<MealBlueprint>> mealsByType,
                               List<String> snacksEnglish,
                               List<String> snacksVietnamese) {
            this.code = code;
            this.aliases = aliases;
            this.englishName = englishName;
            this.vietnameseName = vietnameseName;
            this.fitnessProfile = fitnessProfile;
            this.mealsByType = mealsByType;
            this.snacksEnglish = snacksEnglish;
            this.snacksVietnamese = snacksVietnamese;
        }

        public boolean matches(String normalizedPreferences) {
            return aliases.stream().anyMatch(normalizedPreferences::contains);
        }

        public List<MealBlueprint> getMealsByType(String mealType) {
            return mealsByType.getOrDefault(mealType, Collections.emptyList());
        }

        public String getCode() {
            return code;
        }

        public String getEnglishName() {
            return englishName;
        }

        public String getVietnameseName() {
            return vietnameseName;
        }

        public String getFitnessProfile() {
            return fitnessProfile;
        }

        public List<String> getSnacksEnglish() {
            return snacksEnglish;
        }

        public List<String> getSnacksVietnamese() {
            return snacksVietnamese;
        }

        public Set<String> getMealTypes() {
            return mealsByType.keySet();
        }
    }

    private static final class Ingredient {
        private final String name;
        private final String quantity;

        private Ingredient(String name, String quantity) {
            this.name = name;
            this.quantity = quantity;
        }
    }
}



