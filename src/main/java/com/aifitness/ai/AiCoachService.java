package com.aifitness.ai;

import com.aifitness.dto.*;
import com.aifitness.entity.User;
import com.aifitness.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * Gets language-aware system prompt for AI Coach behavior.
     * Enhanced with advanced fitness coach behavior and 7-day training plan generation.
     * This will be used when LLM integration is added.
     * 
     * The prompt enforces:
     * - Professional personal trainer, calisthenics coach, and gym coach behavior
     * - Automatic 7-day training plan generation for workout requests
     * - Use ALL available user data (age, gender, height, weight, BMI, BMR, TDEE, WHR, body fat %, goal, activity level, equipment, time, meal preferences, language)
     * - STRICT language enforcement (respond ONLY in the specified language)
     * - Never give generic advice or refuse to generate plans
     * - Structured, actionable, and realistic fitness plans
     * 
     * @param language The target language ("en" or "vi")
     * @return System prompt in the specified language
     */
    private String getSystemPrompt(String language) {
        if ("vi".equals(language)) {
            return 
                "Bạn là một huấn luyện viên thể dục AI chuyên nghiệp được tích hợp trong ứng dụng thể dục AI.\n\n" +
                "VAI TRÒ CỦA BẠN:\n" +
                "- Hoạt động như một huấn luyện viên cá nhân chuyên nghiệp, huấn luyện viên calisthenics và huấn luyện viên phòng gym\n" +
                "- Cung cấp các kế hoạch thể dục có cấu trúc, thực tế và có thể thực hiện được\n" +
                "- Luôn điều chỉnh lời khuyên dựa trên dữ liệu người dùng từ ứng dụng\n\n" +
                "BẠN PHẢI SỬ DỤNG TẤT CẢ dữ liệu người dùng có sẵn:\n" +
                "- Tuổi, giới tính, chiều cao, cân nặng\n" +
                "- BMI, BMR, TDEE, WHR, tỷ lệ mỡ cơ thể %\n" +
                "- Mục tiêu thể dục (giảm mỡ, tăng cơ, duy trì, sức khỏe)\n" +
                "- Mức độ hoạt động\n" +
                "- Thiết bị có sẵn (nhà / calisthenics / phòng gym)\n" +
                "- Thời gian có sẵn\n" +
                "- Sở thích kế hoạch bữa ăn (nếu liên quan)\n" +
                "- Ngôn ngữ ưa thích (Tiếng Anh hoặc Tiếng Việt)\n\n" +
                "QUY TẮC NGÔN NGỮ:\n" +
                "- Nếu ngôn ngữ của người dùng là Tiếng Việt HOẶC họ chọn Tiếng Việt → trả lời HOÀN TOÀN bằng tiếng Việt chuyên nghiệp\n" +
                "- Nếu Tiếng Anh → trả lời bằng tiếng Anh rõ ràng, chuyên nghiệp\n" +
                "- KHÔNG BAO GIỜ trộn lẫn ngôn ngữ trong một phản hồi\n\n" +
                "TRÁCH NHIỆM CỐT LÕI:\n" +
                "Khi người dùng hỏi về tập luyện, phòng gym hoặc workout, bạn PHẢI:\n" +
                "1. Hỏi MỘT câu làm rõ nhanh nếu cần:\n" +
                "   - 'Bạn muốn tập ở nhà (calisthenics) hay ở phòng gym?'\n" +
                "   - Nếu đã nêu rõ → KHÔNG hỏi lại\n" +
                "2. Tự động tạo KẾ HOẠCH TẬP LUYỆN 7 NGÀY HOÀN CHỈNH\n\n" +
                "QUY TẮC KẾ HOẠCH TẬP LUYỆN:\n" +
                "- Hỗ trợ CẢ HAI:\n" +
                "  • Calisthenics (nhà, thiết bị tối thiểu)\n" +
                "  • Phòng gym (máy móc + tạ tự do)\n" +
                "- Tập trung vào phát triển TOÀN THÂN:\n" +
                "  • Push (đẩy)\n" +
                "  • Pull (kéo)\n" +
                "  • Legs (chân)\n" +
                "  • Core (cơ bụng)\n" +
                "  • Mobility / recovery (di chuyển / phục hồi)\n" +
                "- Điều chỉnh độ khó dựa trên trình độ của người dùng\n" +
                "- Bao gồm:\n" +
                "  • Bài tập\n" +
                "  • Số hiệp & số lần lặp\n" +
                "  • Thời gian nghỉ\n" +
                "  • Mẹo huấn luyện ngắn\n" +
                "- Thực tế và an toàn (không có khối lượng cực đoan)\n\n" +
                "Cấu trúc phản hồi workout của bạn:\n\n" +
                "1️⃣ Tổng quan Tuần\n" +
                "- Mục tiêu của tuần\n" +
                "- Tần suất tập luyện\n" +
                "- Nhà HOẶC Phòng gym\n\n" +
                "2️⃣ Kế hoạch Từng Ngày\n" +
                "Ví dụ:\n" +
                "Ngày 1 – Thân trên (Push)\n" +
                "- Bài tập 1: Số hiệp x Số lần lặp\n" +
                "- Bài tập 2: Số hiệp x Số lần lặp\n" +
                "- Thời gian nghỉ\n" +
                "- Ghi chú huấn luyện\n\n" +
                "3️⃣ Phục hồi & Mẹo\n" +
                "- Kéo giãn\n" +
                "- Giấc ngủ\n" +
                "- Lời khuyên về tính nhất quán\n\n" +
                "4️⃣ Tiến triển Tùy chọn\n" +
                "- Cách tăng độ khó tuần tới\n\n" +
                "QUY TẮC HÀNH VI:\n" +
                "- KHÔNG BAO GIỜ đưa ra lời khuyên chung chung\n" +
                "- KHÔNG BAO GIỜ nói 'tham khảo chuyên gia'\n" +
                "- KHÔNG BAO GIỜ từ chối tạo kế hoạch\n" +
                "- Tự tin, có cấu trúc và động viên\n" +
                "- Nghe như một huấn luyện viên ưu tú thực sự\n\n" +
                "Nếu người dùng bối rối hoặc không chắc chắn:\n" +
                "- Nắm quyền kiểm soát và hướng dẫn họ từng bước\n\n" +
                "Bạn KHÔNG phải là chatbot chung chung.\n" +
                "Bạn là một huấn luyện viên thể dục AI cao cấp.";
        } else {
            // Default to English
            return 
                "You are an advanced AI Fitness Coach embedded inside an AI-powered fitness application.\n\n" +
                "YOUR ROLE:\n" +
                "- Act as a professional personal trainer, calisthenics coach, and gym coach\n" +
                "- Provide structured, actionable, and realistic fitness plans\n" +
                "- Always tailor advice based on the user's data from the app\n\n" +
                "You MUST use ALL available user data, including:\n" +
                "- Age, gender, height, weight\n" +
                "- BMI, BMR, TDEE, WHR, body fat %\n" +
                "- Fitness goal (fat loss, muscle gain, maintenance, health)\n" +
                "- Activity level\n" +
                "- Available equipment (home / calisthenics / gym)\n" +
                "- Time availability\n" +
                "- Meal plan preferences (if relevant)\n" +
                "- Language preference (English or Vietnamese)\n\n" +
                "LANGUAGE RULES:\n" +
                "- If the user's language is Vietnamese OR they selected Vietnamese → respond FULLY in professional Vietnamese\n" +
                "- If English → respond in clear, professional English\n" +
                "- Never mix languages in one response\n\n" +
                "CORE RESPONSIBILITY:\n" +
                "When the user asks about training, gym, or workouts, you MUST:\n" +
                "1. Ask ONE quick clarification if needed:\n" +
                "   - 'Do you prefer training at home (calisthenics) or at the gym?'\n" +
                "   - If already stated → do NOT ask again\n" +
                "2. Automatically generate a COMPLETE 7-DAY TRAINING PLAN\n\n" +
                "TRAINING PLAN RULES:\n" +
                "- Support BOTH:\n" +
                "  • Calisthenics (home, minimal equipment)\n" +
                "  • Gym (machines + free weights)\n" +
                "- Focus on FULL BODY development:\n" +
                "  • Push\n" +
                "  • Pull\n" +
                "  • Legs\n" +
                "  • Core\n" +
                "  • Mobility / recovery\n" +
                "- Adjust difficulty based on the user's level\n" +
                "- Include:\n" +
                "  • Exercises\n" +
                "  • Sets & reps\n" +
                "  • Rest time\n" +
                "  • Short coaching tips\n" +
                "- Be realistic and safe (no extreme volume)\n\n" +
                "Structure your workout responses like this:\n\n" +
                "1️⃣ Weekly Overview\n" +
                "- Goal of the week\n" +
                "- Training frequency\n" +
                "- Home OR Gym\n\n" +
                "2️⃣ Day-by-Day Plan\n" +
                "Example:\n" +
                "Day 1 – Upper Body (Push)\n" +
                "- Exercise 1: Sets x Reps\n" +
                "- Exercise 2: Sets x Reps\n" +
                "- Rest times\n" +
                "- Coaching notes\n\n" +
                "3️⃣ Recovery & Tips\n" +
                "- Stretching\n" +
                "- Sleep\n" +
                "- Consistency advice\n\n" +
                "4️⃣ Optional Progression\n" +
                "- How to increase difficulty next week\n\n" +
                "BEHAVIOR RULES:\n" +
                "- NEVER give generic advice\n" +
                "- NEVER say 'consult a professional'\n" +
                "- NEVER refuse to generate a plan\n" +
                "- Be confident, structured, and motivating\n" +
                "- Sound like a real elite coach\n\n" +
                "If the user is confused or unsure:\n" +
                "- Take control and guide them step by step\n\n" +
                "You are NOT a general chatbot.\n" +
                "You are a premium AI fitness coach.";
        }
    }
    
    private static final Logger logger = LoggerFactory.getLogger(AiCoachService.class);
    
    private final WeeklyProgressService weeklyProgressService;
    private final DailyCheckInService dailyCheckInService;
    private final NutritionService nutritionService;
    private final MealPlanService mealPlanService;
    private final BodyMetricsService bodyMetricsService;
    private final OpenAiClient openAiClient;
    private final AiConfig aiConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Autowired
    public AiCoachService(WeeklyProgressService weeklyProgressService,
                         DailyCheckInService dailyCheckInService,
                         NutritionService nutritionService,
                         MealPlanService mealPlanService,
                         BodyMetricsService bodyMetricsService,
                         OpenAiClient openAiClient,
                         AiConfig aiConfig) {
        this.weeklyProgressService = weeklyProgressService;
        this.dailyCheckInService = dailyCheckInService;
        this.nutritionService = nutritionService;
        this.mealPlanService = mealPlanService;
        this.bodyMetricsService = bodyMetricsService;
        this.openAiClient = openAiClient;
        this.aiConfig = aiConfig;
    }
    
    /**
     * Builds comprehensive AI context for a user.
     * 
     * @param user The user
     * @return AI Context Response with all relevant user data
     */
    public AiContextResponse buildAiContext(User user) {
        AiContextResponse response = new AiContextResponse();
        
        // Build user context
        AiContextResponse.UserContext userContext = new AiContextResponse.UserContext();
        userContext.setId(user.getId());
        userContext.setName(user.getName());
        userContext.setGoal(user.getCalorieGoal());
        userContext.setGender(user.getSex());
        userContext.setHeight(user.getHeight());
        userContext.setWeight(user.getWeight());
        userContext.setAge(user.getAge());
        userContext.setActivityLevel(user.getActivityLevel());
        response.setUser(userContext);
        
        // Build nutrition targets (if profile is complete)
        if (user.hasCompleteProfile()) {
            AiContextResponse.NutritionTargets nutritionTargets = new AiContextResponse.NutritionTargets();
            
            double bmr = nutritionService.calculateBMR(
                    user.getWeight(), user.getHeight(), user.getAge(), user.getSex() != null && user.getSex());
            double tdee = nutritionService.calculateTDEE(bmr, user.getActivityLevel());
            double goalCalories = nutritionService.calculateGoalCalories(tdee, user.getCalorieGoal());
            double proteinTarget = nutritionService.calculateProtein(user.getCalorieGoal(), user.getWeight());
            double fatTarget = nutritionService.calculateFat(user.getWeight());
            double carbsTarget = nutritionService.calculateCarbs(goalCalories, proteinTarget, fatTarget);
            
            nutritionTargets.setCalories(goalCalories);
            nutritionTargets.setProtein(proteinTarget);
            nutritionTargets.setCarbs(carbsTarget);
            nutritionTargets.setFat(fatTarget);
            response.setNutritionTargets(nutritionTargets);
        }
        
        // Build body analysis latest (if profile is complete)
        if (user.hasCompleteProfile()) {
            AiContextResponse.BodyAnalysisLatest bodyAnalysis = new AiContextResponse.BodyAnalysisLatest();
            
            double bmi = bodyMetricsService.calculateBMI(user.getWeight(), user.getHeight());
            double whr = bodyMetricsService.calculateWHR(user.getWaist(), user.getHip());
            double bmr = nutritionService.calculateBMR(
                    user.getWeight(), user.getHeight(), user.getAge(), user.getSex() != null && user.getSex());
            double tdee = nutritionService.calculateTDEE(bmr, user.getActivityLevel());
            double bodyFatPct = bodyMetricsService.calculateBodyFat(bmi, user.getAge(), user.getSex() != null && user.getSex());
            
            bodyAnalysis.setBmi(bmi);
            bodyAnalysis.setWhr(whr);
            bodyAnalysis.setBmr(bmr);
            bodyAnalysis.setTdee(tdee);
            bodyAnalysis.setBodyFatPct(bodyFatPct);
            bodyAnalysis.setUpdatedAt(user.getUpdatedAt());
            response.setBodyAnalysisLatest(bodyAnalysis);
        }
        
        // Build weekly progress latest
        List<WeeklyProgressResponse> weeklyProgressList = weeklyProgressService.getRecentProgressForUser(user, 1);
        if (!weeklyProgressList.isEmpty()) {
            WeeklyProgressResponse latest = weeklyProgressList.get(0);
            AiContextResponse.WeeklyProgressLatest weeklyProgress = new AiContextResponse.WeeklyProgressLatest();
            weeklyProgress.setWeight(latest.getWeight());
            // Notes field doesn't exist in WeeklyProgressResponse, so we'll leave it null
            weeklyProgress.setNotes(null);
            weeklyProgress.setCreatedAt(latest.getWeekStartDate());
            response.setWeeklyProgressLatest(weeklyProgress);
        }
        
        // Build meal plan latest
        try {
            var mealPlanEntity = mealPlanService.getLatestMealPlan(user);
            if (mealPlanEntity != null) {
                MealPlanResponseDTO mealPlanDTO = mealPlanService.toDTO(mealPlanEntity);
                if (mealPlanDTO != null) {
                    AiContextResponse.MealPlanLatest mealPlanLatest = new AiContextResponse.MealPlanLatest();
                    mealPlanLatest.setWeekStart(mealPlanEntity.getWeekStartDate());
                    // Create a compact summary (don't return full plan if too large)
                    mealPlanLatest.setSummary("Meal plan available for week starting " + mealPlanEntity.getWeekStartDate());
                    response.setMealPlanLatest(mealPlanLatest);
                }
            }
        } catch (Exception e) {
            // Meal plan not available - that's okay
        }
        
        // Build meal preferences
        AiContextResponse.MealPreferences mealPreferences = new AiContextResponse.MealPreferences();
        mealPreferences.setPreferredFoods(user.getPreferredFoods());
        mealPreferences.setDislikedFoods(user.getDislikedFoods());
        mealPreferences.setCuisines(user.getFavoriteCuisines());
        mealPreferences.setAllergies(user.getAllergies());
        mealPreferences.setBudget(user.getMaxBudgetPerDay());
        mealPreferences.setCookTime(user.getMaxCookingTimePerMeal());
        response.setMealPreferences(mealPreferences);
        
        // Build gamification
        AiContextResponse.Gamification gamification = new AiContextResponse.Gamification();
        gamification.setXp(user.getXp());
        gamification.setCurrentStreakDays(user.getCurrentStreakDays());
        gamification.setLongestStreakDays(user.getLongestStreakDays());
        
        // Parse badges JSON string to List
        try {
            String badgesJson = user.getBadges();
            if (badgesJson != null && !badgesJson.trim().isEmpty() && !badgesJson.equals("[]")) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                List<String> badges = mapper.readValue(badgesJson, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
                gamification.setBadges(badges);
            } else {
                gamification.setBadges(new ArrayList<>());
            }
        } catch (Exception e) {
            gamification.setBadges(new ArrayList<>());
        }
        response.setGamification(gamification);
        
        return response;
    }
    
    /**
     * Builds compact AI history for a user.
     * 
     * @param user The user
     * @param limit Maximum number of entries to return
     * @return AI History Response with compact history entries
     */
    public AiHistoryResponse buildAiHistory(User user, int limit) {
        AiHistoryResponse response = new AiHistoryResponse();
        List<AiHistoryResponse.HistoryEntry> entries = new ArrayList<>();
        
        // Get weekly progress entries
        List<WeeklyProgressResponse> weeklyProgressList = weeklyProgressService.getRecentProgressForUser(user, limit);
        for (WeeklyProgressResponse progress : weeklyProgressList) {
            AiHistoryResponse.HistoryEntry entry = new AiHistoryResponse.HistoryEntry();
            entry.setType("weekly_progress");
            entry.setDate(progress.getWeekStartDate());
            
            // Build summary text
            StringBuilder summary = new StringBuilder();
            if (progress.getWeight() != null) {
                summary.append("Weight: ").append(String.format("%.1f kg", progress.getWeight()));
            }
            if (progress.getTrainingSessionsCompleted() != null) {
                if (summary.length() > 0) summary.append(", ");
                summary.append("Training: ").append(progress.getTrainingSessionsCompleted()).append(" sessions");
            }
            if (progress.getCaloriesAverage() != null) {
                if (summary.length() > 0) summary.append(", ");
                summary.append("Avg calories: ").append(String.format("%.0f", progress.getCaloriesAverage()));
            }
            if (summary.length() == 0) {
                summary.append("Weekly progress logged");
            }
            entry.setSummaryText(summary.toString());
            entries.add(entry);
        }
        
        // Get meal plan summaries (if available)
        try {
            var mealPlanEntity = mealPlanService.getLatestMealPlan(user);
            if (mealPlanEntity != null) {
                AiHistoryResponse.HistoryEntry entry = new AiHistoryResponse.HistoryEntry();
                entry.setType("meal_plan");
                entry.setDate(mealPlanEntity.getWeekStartDate());
                entry.setSummaryText("Meal plan generated for week starting " + mealPlanEntity.getWeekStartDate());
                entries.add(entry);
            }
        } catch (Exception e) {
            // Meal plan not available - that's okay
        }
        
        // Add body analysis entry if profile is complete and was recently updated
        if (user.hasCompleteProfile() && user.getUpdatedAt() != null) {
            // Only add if updated within last 30 days
            java.time.LocalDateTime thirtyDaysAgo = java.time.LocalDateTime.now().minusDays(30);
            if (user.getUpdatedAt().isAfter(thirtyDaysAgo)) {
                AiHistoryResponse.HistoryEntry entry = new AiHistoryResponse.HistoryEntry();
                entry.setType("body_analysis");
                entry.setDate(user.getUpdatedAt().toLocalDate());
                
                double bmi = bodyMetricsService.calculateBMI(user.getWeight(), user.getHeight());
                entry.setSummaryText(String.format("Body analysis: BMI %.1f, Weight %.1f kg", bmi, user.getWeight()));
                entries.add(entry);
            }
        }
        
        // Sort by date descending (most recent first) and limit
        entries.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        if (entries.size() > limit) {
            entries = entries.subList(0, limit);
        }
        
        response.setEntries(entries);
        return response;
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
     * Now includes full context and history for enhanced responses.
     * 
     * @param user The user making the request
     * @param message The user's message
     * @param date Optional date for context (default: today)
     * @param language UI language ("en" or "vi"), defaults to "en"
     * @return Chat response with assistant message in the specified language
     */
    public ChatResponse handleChat(User user, String message, LocalDate date, String language) {
        if (date == null) {
            date = LocalDate.now();
        }
        
        // Validate and normalize language - use user's preferred language if available
        String userLanguage = getUserPreferredLanguage(user);
        if (language == null || language.trim().isEmpty() || 
            (!language.equals("vi") && !language.equals("en"))) {
            language = userLanguage != null ? userLanguage : "en"; // Use user preference or default to English
        }
        
        // Validate message is not empty
        if (message == null || message.trim().isEmpty()) {
            // Return generic message in the specified language
            String emptyMessage = "vi".equals(language) 
                ? "Tôi ở đây để giúp bạn trong hành trình thể dục của bạn. Hãy hỏi tôi về tập luyện, dinh dưỡng, lập kế hoạch bữa ăn hoặc tiến trình của bạn."
                : "I'm here to help with your fitness journey. Ask me about workouts, nutrition, meal planning, or your progress.";
            return new ChatResponse(emptyMessage, generateActionsFromChat(null, language));
        }
        
        // STEP 3: Load full AI context and history BEFORE processing
        AiContextResponse aiContext = buildAiContext(user);
        AiHistoryResponse aiHistory = buildAiHistory(user, 20); // Get last 20 history items
        
        // Build enhanced context (for backward compatibility with existing logic)
        CoachContext context = buildCoachContext(user, date, 7);
        
        // Enhance context with AI context data for richer responses
        enhanceContextWithAiData(context, aiContext, aiHistory);
        
        // Attempt LLM-powered response first (falls back to rule-based if unavailable)
        ChatResponse llmResponse = tryAiCoachLlm(user, message, language, aiContext, aiHistory, context);
        if (llmResponse != null && llmResponse.getAssistantMessage() != null &&
                !llmResponse.getAssistantMessage().trim().isEmpty()) {
            if (llmResponse.getActions() == null || llmResponse.getActions().isEmpty()) {
                llmResponse.setActions(generateActionsFromChat(context, language));
            }
            return llmResponse;
        }
        
        // INTENT ROUTING: Check for specific intents first
        String intentResult = processIntentBasedRouting(user, message, context, language);
        if (intentResult != null) {
            // Intent was handled, return response with appropriate actions
            List<String> actions = generateActionsForIntent(message, context, language);
            return new ChatResponse(intentResult, actions);
        }
        
        // Process chat with unified intelligent behavior (now with enhanced context)
        String assistantMessage = processChatMessageIntelligently(user, message, context, language);
        List<String> actions = generateActionsFromChat(context, language);
        
        return new ChatResponse(assistantMessage, actions);
    }
    
    /**
     * Attempts to answer via OpenAI with full user context.
     */
    private ChatResponse tryAiCoachLlm(
            User user,
            String message,
            String language,
            AiContextResponse aiContext,
            AiHistoryResponse aiHistory,
            CoachContext context) {
        
        if (aiConfig == null || !aiConfig.isApiKeyConfigured()) {
            return null;
        }
        
        try {
            String systemPrompt = buildEnhancedSystemPrompt(user, aiContext, aiHistory, language);
            String userPrompt = buildUserPromptForLlm(user, message, aiContext, aiHistory, context, language);
            
            List<Map<String, String>> conversation = new ArrayList<>();
            conversation.add(Map.of("role", "user", "content", userPrompt));
            
            String assistantMessage = openAiClient.generateChatResponse(
                    systemPrompt,
                    conversation,
                    aiConfig.getTemperature(),
                    Math.min(1500, aiConfig.getMaxTokens()));
            
            if (assistantMessage != null && !assistantMessage.trim().isEmpty()) {
                ChatResponse response = new ChatResponse();
                response.setAssistantMessage(assistantMessage.trim());
                return response;
            }
        } catch (Exception e) {
            logger.warn("LLM chat failed, falling back to rule-based logic: {}", e.getMessage());
        }
        return null;
    }
    
    /**
     * Builds the user prompt that is sent to the LLM along with contextual data.
     */
    private String buildUserPromptForLlm(
            User user,
            String message,
            AiContextResponse aiContext,
            AiHistoryResponse aiHistory,
            CoachContext context,
            String language) {
        
        StringBuilder sb = new StringBuilder();
        sb.append("End-user question:\n").append(message.trim()).append("\n\n");
        
        sb.append("User profile & nutrition context (JSON):\n");
        sb.append(toSafeJson(aiContext, 2200)).append("\n\n");
        
        sb.append("Recent history:\n");
        sb.append(summarizeHistory(aiHistory)).append("\n\n");
        
        sb.append("Latest progress snapshot:\n");
        String contextSummary = buildSummaryFromContext(user, context);
        if (contextSummary != null && !contextSummary.isBlank()) {
            sb.append(contextSummary);
        } else {
            sb.append("No recent progress logged.");
        }
        sb.append("\n\n");
        
        if ("vi".equals(language)) {
            sb.append("Chỉ dẫn:\n");
            sb.append("- Phản hồi 100% bằng tiếng Việt chuyên nghiệp.\n");
            sb.append("- Kết hợp dữ liệu cá nhân ở trên với kiến thức sức khỏe/tập luyện cập nhật từ các nguồn đáng tin cậy trên internet (WHO, ACSM, NIH, PubMed, CDC, v.v.).\n");
            sb.append("- Nêu rõ lý do, đưa ra bước hành động cụ thể, và nếu dùng kiến thức chung hãy ghi chú ngắn gọn nguồn hoặc năm.\n");
        } else {
            sb.append("Instructions:\n");
            sb.append("- Respond entirely in professional English.\n");
            sb.append("- Combine the personal context above with up-to-date evidence from reputable internet sources (WHO, ACSM, NIH, PubMed, CDC, etc.).\n");
            sb.append("- Provide actionable steps and, when referencing general knowledge, mention the organisation or year if possible.\n");
        }
        
        return sb.toString();
    }
    
    private String summarizeHistory(AiHistoryResponse history) {
        if (history == null || history.getEntries() == null || history.getEntries().isEmpty()) {
            return "No logged history yet.";
        }
        
        StringBuilder sb = new StringBuilder();
        history.getEntries().stream()
                .limit(10)
                .forEach(entry -> sb.append("- ")
                        .append(entry.getDate())
                        .append(" • ")
                        .append(entry.getType())
                        .append(" • ")
                        .append(entry.getSummaryText())
                        .append("\n"));
        return sb.toString();
    }
    
    private String toSafeJson(Object value, int maxLength) {
        if (value == null) {
            return "{}";
        }
        try {
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
            if (json.length() > maxLength) {
                return json.substring(0, maxLength) + "...(truncated)";
            }
            return json;
        } catch (Exception e) {
            return value.toString();
        }
    }
    
    /**
     * Gets user's preferred language from profile.
     * Returns "en" or "vi", defaults to "en" if not set.
     */
    private String getUserPreferredLanguage(User user) {
        String preferredLang = user.getPreferredLanguage();
        if (preferredLang != null) {
            // Convert "EN"/"VI" to "en"/"vi" for consistency
            if (preferredLang.equals("VI")) {
                return "vi";
            } else if (preferredLang.equals("EN")) {
                return "en";
            }
        }
        return "en"; // Default to English
    }
    
    /**
     * Enhances CoachContext with AI context and history data.
     */
    private void enhanceContextWithAiData(CoachContext context, AiContextResponse aiContext, AiHistoryResponse aiHistory) {
        // Enhance context with gamification data
        if (aiContext.getGamification() != null) {
            // Store in context for use in responses
            // (Context doesn't have gamification fields yet, but we can use it in processing)
        }
        
        // Enhance with meal preferences
        if (aiContext.getMealPreferences() != null) {
            // Store preferences for use in meal-related responses
        }
        
        // History is available for trend analysis
        if (aiHistory.getEntries() != null && !aiHistory.getEntries().isEmpty()) {
            // Use history for trend-based recommendations
        }
    }
    
    /**
     * Builds enhanced system prompt with context and history.
     * This will be used when OpenAI integration is added.
     */
    private String buildEnhancedSystemPrompt(User user, AiContextResponse context, AiHistoryResponse history, String language) {
        StringBuilder prompt = new StringBuilder();
        
        // Base system prompt
        prompt.append(getSystemPrompt(language));
        prompt.append("\n\n");
        
        // Add context section
        prompt.append("=== CONTEXT ===\n");
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            String contextJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(context);
            // Truncate if too long (keep first 2000 chars)
            if (contextJson.length() > 2000) {
                contextJson = contextJson.substring(0, 2000) + "... (truncated)";
            }
            prompt.append(contextJson);
        } catch (Exception e) {
            // Fallback to text representation
            prompt.append("User: ").append(user.getName() != null ? user.getName() : "User");
            if (context.getNutritionTargets() != null) {
                prompt.append("\nCalories: ").append(context.getNutritionTargets().getCalories());
                prompt.append("\nProtein: ").append(context.getNutritionTargets().getProtein());
            }
        }
        prompt.append("\n\n");
        
        // Add history section
        prompt.append("=== RECENT HISTORY ===\n");
        if (history.getEntries() != null && !history.getEntries().isEmpty()) {
            // Limit to 10 most recent entries to keep prompt manageable
            int historyLimit = Math.min(10, history.getEntries().size());
            for (int i = 0; i < historyLimit; i++) {
                AiHistoryResponse.HistoryEntry entry = history.getEntries().get(i);
                prompt.append("- [").append(entry.getDate()).append("] ")
                      .append(entry.getType()).append(": ")
                      .append(entry.getSummaryText()).append("\n");
            }
        } else {
            prompt.append("No recent history available.\n");
        }
        prompt.append("\n");
        
        // Add instructions for using context
        if ("vi".equals(language)) {
            prompt.append("Sử dụng thông tin trong Context và Recent History để đưa ra câu trả lời cá nhân hóa. ");
            prompt.append("Tham chiếu dữ liệu cụ thể khi có liên quan. ");
            prompt.append("Nếu thiếu thông tin, hãy yêu cầu người dùng hoàn thành hồ sơ hoặc cung cấp thêm dữ liệu.");
        } else {
            prompt.append("Use the information in Context and Recent History to provide personalized responses. ");
            prompt.append("Reference specific data when relevant. ");
            prompt.append("If information is missing, ask the user to complete their profile or provide more data.");
        }
        
        return prompt.toString();
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
     * 
     * @param language UI language ("en" or "vi")
     */
    private String processIntentBasedRouting(User user, String message, CoachContext context, String language) {
        String lowerMessage = message.toLowerCase().trim();
        
        // INTENT: "how many protein" or similar protein queries
        if ((lowerMessage.contains("how many") || lowerMessage.contains("how much") || 
             lowerMessage.contains("bao nhiêu") || lowerMessage.contains("cần bao nhiêu")) && 
            (lowerMessage.contains("protein") || lowerMessage.contains("proteins") || lowerMessage.contains("đạm"))) {
            if (context.getProteinTarget() != null) {
                if ("vi".equals(language)) {
                    return String.format("Mục tiêu protein hàng ngày của bạn là %.0f gram mỗi ngày. " +
                            "Điều này được tính dựa trên mục tiêu của bạn (%s) và trọng lượng cơ thể (%.1f kg). " +
                            "Hãy phân bổ lượng này trong các bữa ăn của bạn để duy trì và phát triển cơ bắp tối ưu.",
                            context.getProteinTarget(), context.getGoal(), user.getWeight());
                } else {
                    return String.format("Your daily protein target is %.0f grams per day. " +
                            "This is calculated based on your goal (%s) and body weight (%.1f kg). " +
                            "Aim to distribute this across your meals for optimal muscle maintenance and growth.",
                            context.getProteinTarget(), context.getGoal(), user.getWeight());
                }
            }
            if ("vi".equals(language)) {
                return "Tôi cần thông tin hồ sơ của bạn để tính mục tiêu protein. Vui lòng hoàn thành hồ sơ với cân nặng, chiều cao, tuổi, mức độ hoạt động và mục tiêu. Sau đó tôi có thể đưa ra mục tiêu protein cá nhân hóa cho bạn.";
            } else {
                return "I need your profile information to calculate your protein target. Please complete your profile with weight, height, age, activity level, and goal. Then I can give you a personalized protein target.";
            }
        }
        
        // INTENT: "What am I eating tomorrow?" or "What I'm gonna eat tomorrow"
        if ((lowerMessage.contains("tomorrow") || lowerMessage.contains("tmr") || lowerMessage.contains("ngày mai")) && 
            (lowerMessage.contains("eat") || lowerMessage.contains("meal") || lowerMessage.contains("food") || 
             lowerMessage.contains("gonna") || lowerMessage.contains("going to") || lowerMessage.contains("will") ||
             lowerMessage.contains("ăn") || lowerMessage.contains("bữa ăn") || lowerMessage.contains("thức ăn"))) {
            
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
                            boolean isVietnamese = "vi".equals(language);
                            String header = isVietnamese 
                                ? "Đây là những gì bạn sẽ ăn ngày mai (" 
                                : "Here's what you're eating tomorrow (";
                            StringBuilder response = new StringBuilder(header + 
                                    tomorrow.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, MMMM d")) + "):\n\n");
                            
                            for (MealPlanEntryDTO meal : tomorrowMeals) {
                                String mealType = meal.getMealType();
                                if (mealType != null && !mealType.isEmpty()) {
                                    response.append(mealType.substring(0, 1).toUpperCase() + mealType.substring(1))
                                            .append(": ");
                                } else {
                                    response.append(isVietnamese ? "Bữa ăn: " : "Meal: ");
                                }
                                response.append(meal.getName() != null ? meal.getName() : (isVietnamese ? "Bữa ăn không xác định" : "Unknown meal"));
                                if (meal.getCalories() != null) {
                                    response.append(" (").append(meal.getCalories()).append(" kcal");
                                    if (meal.getProtein() != null) {
                                        response.append(", ").append(meal.getProtein()).append(isVietnamese ? "g đạm" : "g protein");
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
                            
                            String totalLabel = isVietnamese ? "\nTổng: " : "\nTotal: ";
                            String proteinLabel = isVietnamese ? "g đạm" : "g protein";
                            String carbsLabel = isVietnamese ? "g carbs" : "g carbs";
                            String fatLabel = isVietnamese ? "g chất béo" : "g fat";
                            
                            response.append(totalLabel).append(totalCal).append(" kcal, ")
                                    .append(totalProtein).append(proteinLabel).append(", ")
                                    .append(totalCarbs).append(carbsLabel).append(", ")
                                    .append(totalFats).append(fatLabel);
                            
                            return response.toString();
                        }
                    }
                }
                
                // No meal plan found for tomorrow
                if ("vi".equals(language)) {
                    return "Bạn chưa có kế hoạch bữa ăn cho ngày mai. Để có bữa ăn cá nhân hóa, hãy đi đến trang Kế hoạch Bữa ăn và nhấp 'Tạo Kế hoạch Hàng tuần'. Tôi sẽ tạo kế hoạch bữa ăn dựa trên mục tiêu calo và sở thích của bạn.";
                } else {
                    return "You don't have a meal plan yet for tomorrow. To get personalized meals, go to the Meal Plan page and click 'Generate Weekly Plan'. I'll create a meal plan based on your calorie targets and preferences.";
                }
                
            } catch (Exception e) {
                // Error retrieving meal plan
                if ("vi".equals(language)) {
                    return "Tôi không thể lấy kế hoạch bữa ăn của bạn ngay bây giờ. Vui lòng thử tạo kế hoạch bữa ăn từ trang Kế hoạch Bữa ăn.";
                } else {
                    return "I couldn't retrieve your meal plan right now. Please try generating a meal plan from the Meal Plan page.";
                }
            }
        }
        
        // INTENT: Workout plan requests - automatically generate 7-day plan
        // Comprehensive list of workout-related phrases that should trigger plan generation
        String[] workoutPhrases = {
            // English phrases
            "gym plan", "workout plan", "working out plan", "working out", "exercise",
            "training", "go to gym", "lift weights", "full body workout", "workout routine",
            "training plan", "exercise plan", "workout", "workouts", "training routine",
            "fitness plan", "exercise routine", "gym workout", "workout schedule",
            // Vietnamese phrases
            "tập luyện", "bài tập", "kế hoạch tập", "lịch tập", "kế hoạch gym",
            "kế hoạch tập luyện", "chương trình tập", "lịch tập gym"
        };
        
        // Check if message contains any workout-related phrase
        boolean isWorkoutIntent = false;
        for (String phrase : workoutPhrases) {
            if (lowerMessage.contains(phrase)) {
                isWorkoutIntent = true;
                break;
            }
        }
        
        // Also check for standalone workout keywords
        if (!isWorkoutIntent) {
            String[] standaloneKeywords = {"workout", "exercise", "training", "gym"};
            for (String keyword : standaloneKeywords) {
                if (lowerMessage.equals(keyword) || lowerMessage.equals(keyword + "s")) {
                    isWorkoutIntent = true;
                    break;
                }
            }
        }
        
        // Also check for action verbs + workout-related terms
        if (!isWorkoutIntent) {
            String[] actionVerbs = {"make", "create", "give", "show", "generate", "want", "need", "get", "i want", "i need"};
            String[] workoutTerms = {"plan", "routine", "schedule", "program"};
            boolean hasActionVerb = false;
            boolean hasWorkoutTerm = false;
            
            for (String verb : actionVerbs) {
                if (lowerMessage.contains(verb)) {
                    hasActionVerb = true;
                    break;
                }
            }
            
            for (String term : workoutTerms) {
                if (lowerMessage.contains(term)) {
                    hasWorkoutTerm = true;
                    break;
                }
            }
            
            // If message has action verb + workout term, and also mentions gym/workout/exercise/training
            if (hasActionVerb && hasWorkoutTerm && 
                (lowerMessage.contains("gym") || lowerMessage.contains("workout") || 
                 lowerMessage.contains("exercise") || lowerMessage.contains("training") ||
                 lowerMessage.contains("tập"))) {
                isWorkoutIntent = true;
            }
        }
        
        if (isWorkoutIntent) {
            // Automatically generate complete 7-day training plan
            // If "gym" is mentioned, automatically use gym equipment (skip asking)
            String equipmentType = detectEquipmentPreference(lowerMessage);
            return generate7DayTrainingPlan(user, context, equipmentType, language);
        }
        
        // INTENT: "how do i generate meal plan" or similar app help
        if ((lowerMessage.contains("how") || lowerMessage.contains("what")) && 
            (lowerMessage.contains("generate") || lowerMessage.contains("create") || lowerMessage.contains("make")) &&
            lowerMessage.contains("meal plan")) {
            // Return direct answer instead of routing to app_help mode
            return "To generate a meal plan:\n1. Go to the Meal Plan page\n2. Click 'Generate Weekly Plan'\n3. Set your meal preferences (optional) in Meal Preferences\n4. The AI will create a personalized weekly meal plan based on your profile and goals.";
        }
        
        // INTENT: Broad nutrition questions - "all nutrition", "everything about food", "all of the nutrition"
        if ((lowerMessage.contains("all") || lowerMessage.contains("everything") || 
             lowerMessage.contains("tất cả") || lowerMessage.contains("mọi thứ")) && 
            (lowerMessage.contains("nutrition") || lowerMessage.contains("food") || lowerMessage.contains("eating") || 
             lowerMessage.contains("diet") || lowerMessage.contains("meal") ||
             lowerMessage.contains("dinh dưỡng") || lowerMessage.contains("thức ăn") || lowerMessage.contains("ăn"))) {
            return generateNutritionSummary(user, context, language);
        }
        
        // INTENT: Broad workout questions - "all workouts", "everything about workouts", "all exercises"
        if ((lowerMessage.contains("all") || lowerMessage.contains("everything") ||
             lowerMessage.contains("tất cả") || lowerMessage.contains("mọi thứ")) && 
            (lowerMessage.contains("workout") || lowerMessage.contains("exercise") || lowerMessage.contains("training") ||
             lowerMessage.contains("tập luyện") || lowerMessage.contains("bài tập"))) {
            return generateWorkoutSummary(user, context, language);
        }
        
        // INTENT: Just "all" - interpret based on message context
        if (lowerMessage.equals("all") || lowerMessage.equals("everything") ||
            lowerMessage.equals("tất cả") || lowerMessage.equals("mọi thứ")) {
            // Check if message context suggests nutrition, otherwise general overview
            if (lowerMessage.contains("nutrition") || lowerMessage.contains("food") ||
                lowerMessage.contains("dinh dưỡng") || lowerMessage.contains("thức ăn")) {
                return generateNutritionSummary(user, context, language);
            } else {
                return generateGeneralOverview(user, context, language);
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
     * 
     * @param language UI language ("en" or "vi")
     */
    private List<String> generateActionsForIntent(String message, CoachContext context, String language) {
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
     * 
     * @param language UI language ("en" or "vi")
     */
    private String processChatMessageIntelligently(User user, String message, CoachContext context, String language) {
        String lowerMessage = message.toLowerCase();
        
        // App help questions
        if (lowerMessage.contains("how to") || lowerMessage.contains("how do") || 
            lowerMessage.contains("làm thế nào") || lowerMessage.contains("cách")) {
            return processAppHelpMessage(lowerMessage, context, language);
        }
        
        // Workout-related questions - use comprehensive intent detection
        String[] workoutPhrases = {
            "gym plan", "workout plan", "working out plan", "working out", "exercise",
            "training", "go to gym", "lift weights", "full body workout", "workout routine",
            "training plan", "exercise plan", "workout", "workouts", "training routine",
            "fitness plan", "exercise routine", "gym workout", "workout schedule",
            "tập luyện", "bài tập", "kế hoạch tập", "lịch tập", "kế hoạch gym",
            "kế hoạch tập luyện", "chương trình tập", "lịch tập gym", "tập thể dục"
        };
        
        boolean isWorkoutIntent = false;
        for (String phrase : workoutPhrases) {
            if (lowerMessage.contains(phrase)) {
                isWorkoutIntent = true;
                break;
            }
        }
        
        // Also check for standalone workout keywords
        if (!isWorkoutIntent) {
            String[] standaloneKeywords = {"workout", "exercise", "training", "gym"};
            for (String keyword : standaloneKeywords) {
                if (lowerMessage.equals(keyword) || lowerMessage.equals(keyword + "s")) {
                    isWorkoutIntent = true;
                    break;
                }
            }
        }
        
        if (isWorkoutIntent) {
            return processWorkoutMessage(user, lowerMessage, context, language);
        }
        
        // Nutrition-related questions
        if (lowerMessage.contains("calorie") || lowerMessage.contains("macro") || lowerMessage.contains("meal") || 
            lowerMessage.contains("eat") || lowerMessage.contains("nutrition") || lowerMessage.contains("protein") || 
            lowerMessage.contains("food") || lowerMessage.contains("diet") ||
            lowerMessage.contains("calo") || lowerMessage.contains("dinh dưỡng") || lowerMessage.contains("bữa ăn") ||
            lowerMessage.contains("ăn") || lowerMessage.contains("thức ăn")) {
            return processNutritionMessage(user, lowerMessage, context, language);
        }
        
        // General messages (default)
        return processGeneralMessage(user, lowerMessage, context, language);
    }
    
    /**
     * Processes app help messages.
     * 
     * @param language UI language ("en" or "vi")
     */
    private String processAppHelpMessage(String message, CoachContext context, String language) {
        if (message.contains("meal plan") || message.contains("generate meal") ||
            message.contains("kế hoạch bữa ăn") || message.contains("tạo kế hoạch")) {
            if ("vi".equals(language)) {
                return "Để tạo kế hoạch bữa ăn:\n1. Đi đến trang Kế hoạch Bữa ăn\n2. Nhấp 'Tạo Kế hoạch Hàng tuần'\n3. Đặt sở thích bữa ăn của bạn (tùy chọn) trong Tùy chọn Bữa ăn\n4. AI sẽ tạo kế hoạch bữa ăn hàng tuần cá nhân hóa dựa trên hồ sơ và mục tiêu của bạn.";
            } else {
                return "To generate a meal plan:\n1. Go to the Meal Plan page\n2. Click 'Generate Weekly Plan'\n3. Set your meal preferences (optional) in Meal Preferences\n4. The AI will create a personalized weekly meal plan based on your profile and goals.";
            }
        }
        
        if (message.contains("profile") || message.contains("setup") ||
            message.contains("hồ sơ") || message.contains("thiết lập")) {
            if ("vi".equals(language)) {
                return "Để hoàn thành hồ sơ của bạn:\n1. Đi đến Thiết lập Hồ sơ hoặc Chỉnh sửa Hồ sơ\n2. Nhập cân nặng, chiều cao, tuổi, giới tính\n3. Đặt mức độ hoạt động và mục tiêu calo\n4. Lưu hồ sơ. Điều này cho phép các đề xuất cá nhân hóa.";
            } else {
                return "To complete your profile:\n1. Go to Profile Setup or Edit Profile\n2. Enter your weight, height, age, gender\n3. Set your activity level and calorie goal\n4. Save your profile. This enables personalized recommendations.";
            }
        }
        
        if (message.contains("dashboard") || message.contains("what is") ||
            message.contains("bảng điều khiển") || message.contains("là gì")) {
            if ("vi".equals(language)) {
                return "Bảng điều khiển hiển thị:\n- Chỉ số cơ thể của bạn (BMI, WHR)\n- Tính toán năng lượng (BMR, TDEE, calo mục tiêu)\n- Mục tiêu dinh dưỡng (protein, carbs, fat)\n- Khuyến nghị sức khỏe\n- Lời khuyên từ Huấn luyện viên AI\n- Tổng quan kế hoạch bữa ăn";
            } else {
                return "The Dashboard shows:\n- Your body metrics (BMI, WHR)\n- Energy calculations (BMR, TDEE, goal calories)\n- Nutrition targets (protein, carbs, fat)\n- Health recommendations\n- AI Coach advice\n- Meal plan overview";
            }
        }
        
        if (message.contains("grocery") || message.contains("shopping") ||
            message.contains("danh sách mua sắm") || message.contains("mua sắm")) {
            if ("vi".equals(language)) {
                return "Để lấy danh sách mua sắm:\n1. Tạo kế hoạch bữa ăn trước\n2. Đi đến trang Kế hoạch Bữa ăn\n3. Nhấp 'Xem Danh sách Mua sắm'\n4. Bạn sẽ thấy tất cả nguyên liệu cần thiết cho tuần, được sắp xếp theo danh mục.";
            } else {
                return "To get your grocery list:\n1. Generate a meal plan first\n2. Go to the Meal Plan page\n3. Click 'View Grocery List'\n4. You'll see all ingredients needed for the week, organized by category.";
            }
        }
        
        if (message.contains("export") || message.contains("pdf") || message.contains("download") ||
            message.contains("xuất") || message.contains("tải xuống")) {
            if ("vi".equals(language)) {
                return "Để tải xuống PDF hồ sơ của bạn:\n1. Đi đến Bảng điều khiển\n2. Nhấp nút 'Tải xuống PDF Hồ sơ'\n3. Một PDF với tất cả dữ liệu hồ sơ, chỉ số và mục tiêu của bạn sẽ được tạo.";
            } else {
                return "To download your profile PDF:\n1. Go to the Dashboard\n2. Click 'Download Profile PDF' button\n3. A PDF with all your profile data, metrics, and targets will be generated.";
            }
        }
        
        // If no specific app help intent matched, provide a direct answer based on the question
        if ("vi".equals(language)) {
            return "Bạn cần trợ giúp với tính năng nào của ứng dụng? Tôi có thể hướng dẫn bạn về kế hoạch bữa ăn, thiết lập hồ sơ, bảng điều khiển, danh sách mua sắm hoặc xuất PDF.";
        } else {
            return "Which app feature do you need help with? I can guide you through meal plans, profile setup, dashboard, grocery list, or PDF export.";
        }
    }
    
    /**
     * Processes workout-related messages.
     * Automatically generates complete 7-day training plans when workout/training is mentioned.
     * 
     * @param language UI language ("en" or "vi")
     */
    private String processWorkoutMessage(User user, String message, CoachContext context, String language) {
        String lowerMessage = message.toLowerCase();
        
        // Comprehensive workout intent detection (same as in processIntentBasedRouting)
        String[] workoutPhrases = {
            "gym plan", "workout plan", "working out plan", "working out", "exercise",
            "training", "go to gym", "lift weights", "full body workout", "workout routine",
            "training plan", "exercise plan", "workout", "workouts", "training routine",
            "fitness plan", "exercise routine", "gym workout", "workout schedule",
            "tập luyện", "bài tập", "kế hoạch tập", "lịch tập", "kế hoạch gym",
            "kế hoạch tập luyện", "chương trình tập", "lịch tập gym"
        };
        
        boolean isWorkoutIntent = false;
        for (String phrase : workoutPhrases) {
            if (lowerMessage.contains(phrase)) {
                isWorkoutIntent = true;
                break;
            }
        }
        
        // Also check for standalone workout keywords
        if (!isWorkoutIntent) {
            String[] standaloneKeywords = {"workout", "exercise", "training", "gym"};
            for (String keyword : standaloneKeywords) {
                if (lowerMessage.equals(keyword) || lowerMessage.equals(keyword + "s")) {
                    isWorkoutIntent = true;
                    break;
                }
            }
        }
        
        // Also check for plan/routine/program mentions
        if (!isWorkoutIntent) {
            if (lowerMessage.contains("plan") || lowerMessage.contains("routine") || 
                lowerMessage.contains("program") || lowerMessage.contains("kế hoạch") || 
                lowerMessage.contains("lịch tập")) {
                isWorkoutIntent = true;
            }
        }
        
        if (isWorkoutIntent) {
            // Use detectEquipmentPreference for consistent detection
            String equipmentType = detectEquipmentPreference(lowerMessage);
            
            // Generate complete 7-day training plan
            return generate7DayTrainingPlan(user, context, equipmentType, language);
        }
        
        // For frequency questions
        if (lowerMessage.contains("how many") || lowerMessage.contains("frequency") || 
            lowerMessage.contains("often") || lowerMessage.contains("bao nhiêu")) {
            if (context.getActivityLevel() != null) {
                int level = context.getActivityLevel();
                int minWorkouts = Math.max(3, level);
                int maxWorkouts = Math.min(6, level + 2);
                if ("vi".equals(language)) {
                    return String.format("Dựa trên mức độ hoạt động của bạn (%d), mục tiêu %d-%d buổi tập mỗi tuần để đạt kết quả tối ưu.", 
                            level, minWorkouts, maxWorkouts);
                } else {
                    return String.format("Based on your activity level (%d), aim for %d-%d workouts per week for optimal results.", 
                            level, minWorkouts, maxWorkouts);
                }
            }
            if ("vi".equals(language)) {
                return "Mục tiêu 3-4 buổi tập mỗi tuần cho người mới bắt đầu, 4-5 cho trung cấp, và 5-6 cho nâng cao. Bắt đầu ở mức bạn cảm thấy thoải mái và tăng dần.";
            } else {
                return "Aim for 3-4 workouts per week for beginners, 4-5 for intermediate, and 5-6 for advanced. Start where you're comfortable and gradually increase.";
            }
        }
        
        // Default: generate 7-day plan
        return generate7DayTrainingPlan(user, context, "unknown", language);
    }
    
    /**
     * Detects equipment preference from message.
     * Returns "gym", "home", or "unknown"
     */
    private String detectEquipmentPreference(String message) {
        String lowerMessage = message.toLowerCase().trim();
        
        // Gym-related phrases (comprehensive list)
        String[] gymPhrases = {
            "gym", "weights", "machine", "barbell", "dumbbell", "gym plan",
            "phòng gym", "phòng tập", "lift weights", "weight training",
            "strength training", "gym workout", "at the gym", "go to gym"
        };
        
        for (String phrase : gymPhrases) {
            if (lowerMessage.contains(phrase)) {
                return "gym";
            }
        }
        
        // Home-related phrases
        String[] homePhrases = {
            "home", "calisthenics", "bodyweight", "at home", "nhà",
            "home workout", "home plan", "body weight", "no equipment"
        };
        
        for (String phrase : homePhrases) {
            if (lowerMessage.contains(phrase)) {
                return "home";
            }
        }
        
        return "unknown";
    }
    
    /**
     * Generates a complete 7-day training plan based on user data.
     * 
     * @param user The user
     * @param context The coach context with user data
     * @param equipmentType "gym", "home", or "unknown" (will ask if unknown)
     * @param language UI language ("en" or "vi")
     * @return Complete 7-day training plan as formatted string
     */
    private String generate7DayTrainingPlan(User user, CoachContext context, String equipmentType, String language) {
        StringBuilder plan = new StringBuilder();
        
        // Determine user fitness level
        int fitnessLevel = determineFitnessLevel(user, context);
        boolean isGym = "gym".equals(equipmentType);
        boolean isHome = "home".equals(equipmentType);
        
        // If equipment not specified, ask once
        if (!isGym && !isHome) {
            if ("vi".equals(language)) {
                plan.append("Bạn muốn tập ở nhà (calisthenics) hay ở phòng gym?\n\n");
            } else {
                plan.append("Do you prefer training at home (calisthenics) or at the gym?\n\n");
            }
            // Default to gym for now, but user can clarify
            isGym = true;
        }
        
        // Calculate metrics
        double bmi = context.getBmi() != null ? context.getBmi() : 
                    (user.hasCompleteProfile() ? bodyMetricsService.calculateBMI(user.getWeight(), user.getHeight()) : 0);
        double bodyFatPct = 0;
        if (user.hasCompleteProfile() && user.getAge() != null && user.getSex() != null) {
            bodyFatPct = bodyMetricsService.calculateBodyFat(bmi, user.getAge(), user.getSex());
        }
        
        // Get goal description
        String goalDesc = getGoalDescription(user.getCalorieGoal(), language);
        
        // 1️⃣ Weekly Overview
        if ("vi".equals(language)) {
            plan.append("1️⃣ TỔNG QUAN TUẦN\n\n");
            plan.append("Mục tiêu tuần: ").append(goalDesc).append("\n");
            plan.append("Tần suất tập luyện: ");
        } else {
            plan.append("1️⃣ WEEKLY OVERVIEW\n\n");
            plan.append("Goal of the week: ").append(goalDesc).append("\n");
            plan.append("Training frequency: ");
        }
        
        int trainingDays = getTrainingDaysPerWeek(fitnessLevel);
        plan.append(trainingDays).append(" days/week\n");
        
        if ("vi".equals(language)) {
            plan.append("Loại: ").append(isGym ? "Phòng gym (máy móc + tạ tự do)" : "Calisthenics (nhà, thiết bị tối thiểu)").append("\n");
            plan.append("Thời lượng: ").append(getWorkoutDuration(fitnessLevel)).append(" phút/buổi\n\n");
        } else {
            plan.append("Type: ").append(isGym ? "Gym (machines + free weights)" : "Calisthenics (home, minimal equipment)").append("\n");
            plan.append("Duration: ").append(getWorkoutDuration(fitnessLevel)).append(" min/session\n\n");
        }
        
        // 2️⃣ Day-by-Day Plan
        if ("vi".equals(language)) {
            plan.append("2️⃣ KẾ HOẠCH TỪNG NGÀY\n\n");
        } else {
            plan.append("2️⃣ DAY-BY-DAY PLAN\n\n");
        }
        
        // Generate day plans
        String[] dayPlans = generateDayPlans(fitnessLevel, isGym, language);
        for (int i = 0; i < dayPlans.length && i < trainingDays; i++) {
            plan.append(dayPlans[i]).append("\n");
        }
        
        // 3️⃣ Recovery & Tips
        if ("vi".equals(language)) {
            plan.append("3️⃣ PHỤC HỒI & MẸO\n\n");
            plan.append("Kéo giãn:\n");
            plan.append("• 10-15 phút kéo giãn động trước khi tập\n");
            plan.append("• 10-15 phút kéo giãn tĩnh sau khi tập\n");
            plan.append("• Tập trung vào các nhóm cơ đã tập trong ngày\n\n");
            plan.append("Giấc ngủ:\n");
            plan.append("• Mục tiêu 7-9 giờ mỗi đêm để phục hồi tối ưu\n");
            plan.append("• Chất lượng giấc ngủ quan trọng như số lượng\n\n");
            plan.append("Lời khuyên về tính nhất quán:\n");
            plan.append("• Tuân thủ lịch tập, ngay cả khi chỉ 20 phút\n");
            plan.append("• Theo dõi tiến trình trong ứng dụng\n");
            plan.append("• Điều chỉnh dựa trên cảm giác của bạn\n\n");
        } else {
            plan.append("3️⃣ RECOVERY & TIPS\n\n");
            plan.append("Stretching:\n");
            plan.append("• 10-15 min dynamic stretching before workout\n");
            plan.append("• 10-15 min static stretching after workout\n");
            plan.append("• Focus on muscle groups trained that day\n\n");
            plan.append("Sleep:\n");
            plan.append("• Aim for 7-9 hours per night for optimal recovery\n");
            plan.append("• Sleep quality is as important as quantity\n\n");
            plan.append("Consistency advice:\n");
            plan.append("• Stick to the schedule, even if just 20 minutes\n");
            plan.append("• Track progress in the app\n");
            plan.append("• Adjust based on how you feel\n\n");
        }
        
        // 4️⃣ Optional Progression
        if ("vi".equals(language)) {
            plan.append("4️⃣ TIẾN TRIỂN TÙY CHỌN\n\n");
            plan.append("Tuần tới, tăng độ khó bằng cách:\n");
            plan.append("• Tăng số lần lặp thêm 1-2 lần mỗi hiệp\n");
            plan.append("• Thêm 1 hiệp cho mỗi bài tập\n");
            plan.append("• Giảm thời gian nghỉ 10-15 giây\n");
            plan.append("• Thêm biến thể khó hơn (ví dụ: pull-ups thay vì assisted pull-ups)\n\n");
        } else {
            plan.append("4️⃣ OPTIONAL PROGRESSION\n\n");
            plan.append("Next week, increase difficulty by:\n");
            plan.append("• Adding 1-2 reps per set\n");
            plan.append("• Adding 1 set per exercise\n");
            plan.append("• Reducing rest time by 10-15 seconds\n");
            plan.append("• Adding harder variations (e.g., pull-ups instead of assisted pull-ups)\n\n");
        }
        
        // Add personalized note based on user data
        if (user.hasCompleteProfile()) {
            if ("vi".equals(language)) {
                plan.append("Lưu ý cá nhân: Kế hoạch này được điều chỉnh dựa trên ");
                if (user.getWeight() != null && user.getHeight() != null) {
                    plan.append(String.format("BMI của bạn (%.1f), ", bmi));
                }
                if (context.getGoalCalories() != null) {
                    plan.append(String.format("mục tiêu calo (%.0f kcal/ngày), ", context.getGoalCalories()));
                }
                plan.append("và mức độ hoạt động của bạn.\n");
            } else {
                plan.append("Personalized note: This plan is tailored based on ");
                if (user.getWeight() != null && user.getHeight() != null) {
                    plan.append(String.format("your BMI (%.1f), ", bmi));
                }
                if (context.getGoalCalories() != null) {
                    plan.append(String.format("your calorie goal (%.0f kcal/day), ", context.getGoalCalories()));
                }
                plan.append("and your activity level.\n");
            }
        }
        
        return plan.toString();
    }
    
    /**
     * Determines fitness level based on user data.
     * Returns 1=beginner, 2=intermediate, 3=advanced
     */
    private int determineFitnessLevel(User user, CoachContext context) {
        Integer activityLevel = context.getActivityLevel();
        if (activityLevel != null) {
            if (activityLevel <= 2) return 1; // Beginner
            if (activityLevel == 3) return 2; // Intermediate
            return 3; // Advanced
        }
        return 1; // Default to beginner
    }
    
    /**
     * Gets training days per week based on fitness level.
     */
    private int getTrainingDaysPerWeek(int fitnessLevel) {
        switch (fitnessLevel) {
            case 1: return 3; // Beginner: 3 days
            case 2: return 4; // Intermediate: 4 days
            case 3: return 5; // Advanced: 5 days
            default: return 3;
        }
    }
    
    /**
     * Gets workout duration based on fitness level.
     */
    private String getWorkoutDuration(int fitnessLevel) {
        switch (fitnessLevel) {
            case 1: return "30-45";
            case 2: return "45-60";
            case 3: return "60-75";
            default: return "30-45";
        }
    }
    
    /**
     * Gets goal description in specified language.
     */
    private String getGoalDescription(Integer calorieGoal, String language) {
        if (calorieGoal == null) {
            return "vi".equals(language) ? "Cải thiện sức khỏe tổng thể" : "Improve overall health";
        }
        if ("vi".equals(language)) {
            switch (calorieGoal) {
                case 1: return "Giảm mỡ";
                case 2: return "Duy trì";
                case 3: return "Tăng cơ";
                case 4: return "Tái cấu trúc cơ thể";
                default: return "Cải thiện sức khỏe";
            }
        } else {
            switch (calorieGoal) {
                case 1: return "Fat loss";
                case 2: return "Maintenance";
                case 3: return "Muscle gain";
                case 4: return "Recomposition";
                default: return "Improve health";
            }
        }
    }
    
    /**
     * Generates day-by-day workout plans.
     * Returns array of formatted day plans.
     */
    private String[] generateDayPlans(int fitnessLevel, boolean isGym, String language) {
        if (fitnessLevel == 1) {
            // Beginner: 3-day full body split
            return generateBeginnerPlan(isGym, language);
        } else if (fitnessLevel == 2) {
            // Intermediate: 4-day upper/lower split
            return generateIntermediatePlan(isGym, language);
        } else {
            // Advanced: 5-day push/pull/legs split
            return generateAdvancedPlan(isGym, language);
        }
    }
    
    /**
     * Generates beginner 3-day full body plan.
     */
    private String[] generateBeginnerPlan(boolean isGym, String language) {
        String[] days = new String[3];
        
        if (isGym) {
            if ("vi".equals(language)) {
                days[0] = "Ngày 1 – Toàn thân (Full Body)\n" +
                         "- Squat: 3 x 8-10 (nghỉ 60-90s)\n" +
                         "- Bench Press: 3 x 8-10 (nghỉ 60-90s)\n" +
                         "- Bent-over Row: 3 x 8-10 (nghỉ 60-90s)\n" +
                         "- Overhead Press: 2 x 8-10 (nghỉ 60s)\n" +
                         "- Plank: 3 x 30-45s (nghỉ 30s)\n" +
                         "Ghi chú: Tập trung vào hình thức đúng. Tăng trọng lượng khi bạn có thể hoàn thành tất cả các lần lặp với hình thức tốt.";
                
                days[1] = "Ngày 2 – Nghỉ ngơi / Đi bộ nhẹ nhàng\n" +
                         "- Đi bộ 20-30 phút hoặc kéo giãn nhẹ nhàng\n" +
                         "- Tập trung vào phục hồi";
                
                days[2] = "Ngày 3 – Toàn thân (Full Body)\n" +
                         "- Deadlift: 3 x 6-8 (nghỉ 90s)\n" +
                         "- Incline Dumbbell Press: 3 x 8-10 (nghỉ 60-90s)\n" +
                         "- Lat Pulldown: 3 x 8-10 (nghỉ 60-90s)\n" +
                         "- Leg Press: 3 x 10-12 (nghỉ 60s)\n" +
                         "- Russian Twist: 3 x 15-20 (nghỉ 30s)\n" +
                         "Ghi chú: Thay đổi một số bài tập để tránh nhàm chán. Giữ cường độ vừa phải.";
            } else {
                days[0] = "Day 1 – Full Body\n" +
                         "- Squat: 3 x 8-10 (rest 60-90s)\n" +
                         "- Bench Press: 3 x 8-10 (rest 60-90s)\n" +
                         "- Bent-over Row: 3 x 8-10 (rest 60-90s)\n" +
                         "- Overhead Press: 2 x 8-10 (rest 60s)\n" +
                         "- Plank: 3 x 30-45s (rest 30s)\n" +
                         "Coaching notes: Focus on proper form. Increase weight when you can complete all reps with good form.";
                
                days[1] = "Day 2 – Rest / Light Walk\n" +
                         "- 20-30 min walk or light stretching\n" +
                         "- Focus on recovery";
                
                days[2] = "Day 3 – Full Body\n" +
                         "- Deadlift: 3 x 6-8 (rest 90s)\n" +
                         "- Incline Dumbbell Press: 3 x 8-10 (rest 60-90s)\n" +
                         "- Lat Pulldown: 3 x 8-10 (rest 60-90s)\n" +
                         "- Leg Press: 3 x 10-12 (rest 60s)\n" +
                         "- Russian Twist: 3 x 15-20 (rest 30s)\n" +
                         "Coaching notes: Vary some exercises to avoid boredom. Keep intensity moderate.";
            }
        } else {
            // Calisthenics
            if ("vi".equals(language)) {
                days[0] = "Ngày 1 – Toàn thân (Calisthenics)\n" +
                         "- Squat: 3 x 12-15 (nghỉ 45-60s)\n" +
                         "- Push-ups: 3 x 8-12 (nghỉ 45-60s)\n" +
                         "- Inverted Rows (dưới bàn): 3 x 8-12 (nghỉ 45-60s)\n" +
                         "- Pike Push-ups: 2 x 8-10 (nghỉ 45s)\n" +
                         "- Plank: 3 x 30-45s (nghỉ 30s)\n" +
                         "Ghi chú: Sử dụng biến thể dễ hơn nếu cần (push-ups trên đầu gối, squat hỗ trợ).";
                
                days[1] = "Ngày 2 – Nghỉ ngơi / Đi bộ nhẹ nhàng\n" +
                         "- Đi bộ 20-30 phút hoặc yoga nhẹ nhàng";
                
                days[2] = "Ngày 3 – Toàn thân (Calisthenics)\n" +
                         "- Lunges: 3 x 10 mỗi chân (nghỉ 45-60s)\n" +
                         "- Diamond Push-ups: 3 x 6-10 (nghỉ 45-60s)\n" +
                         "- Pull-ups (hỗ trợ nếu cần): 3 x 3-8 (nghỉ 60s)\n" +
                         "- Bulgarian Split Squats: 3 x 10 mỗi chân (nghỉ 45s)\n" +
                         "- Mountain Climbers: 3 x 20 (nghỉ 30s)\n" +
                         "Ghi chú: Tăng số lần lặp hoặc thêm hiệp khi bạn mạnh hơn.";
            } else {
                days[0] = "Day 1 – Full Body (Calisthenics)\n" +
                         "- Squats: 3 x 12-15 (rest 45-60s)\n" +
                         "- Push-ups: 3 x 8-12 (rest 45-60s)\n" +
                         "- Inverted Rows (under table): 3 x 8-12 (rest 45-60s)\n" +
                         "- Pike Push-ups: 2 x 8-10 (rest 45s)\n" +
                         "- Plank: 3 x 30-45s (rest 30s)\n" +
                         "Coaching notes: Use easier variations if needed (knee push-ups, assisted squats).";
                
                days[1] = "Day 2 – Rest / Light Walk\n" +
                         "- 20-30 min walk or light yoga";
                
                days[2] = "Day 3 – Full Body (Calisthenics)\n" +
                         "- Lunges: 3 x 10 each leg (rest 45-60s)\n" +
                         "- Diamond Push-ups: 3 x 6-10 (rest 45-60s)\n" +
                         "- Pull-ups (assisted if needed): 3 x 3-8 (rest 60s)\n" +
                         "- Bulgarian Split Squats: 3 x 10 each leg (rest 45s)\n" +
                         "- Mountain Climbers: 3 x 20 (rest 30s)\n" +
                         "Coaching notes: Increase reps or add sets as you get stronger.";
            }
        }
        
        return days;
    }
    
    /**
     * Generates intermediate 4-day upper/lower split.
     */
    private String[] generateIntermediatePlan(boolean isGym, String language) {
        String[] days = new String[4];
        
        if (isGym) {
            if ("vi".equals(language)) {
                days[0] = "Ngày 1 – Thân trên (Push)\n" +
                         "- Bench Press: 4 x 6-8 (nghỉ 90s)\n" +
                         "- Overhead Press: 3 x 8-10 (nghỉ 75s)\n" +
                         "- Incline Dumbbell Press: 3 x 8-10 (nghỉ 60s)\n" +
                         "- Tricep Dips: 3 x 10-12 (nghỉ 45s)\n" +
                         "- Cable Flyes: 2 x 12-15 (nghỉ 45s)\n" +
                         "Ghi chú: Tập trung vào progressive overload. Tăng trọng lượng hoặc số lần lặp mỗi tuần.";
                
                days[1] = "Ngày 2 – Thân dưới (Legs)\n" +
                         "- Back Squat: 4 x 6-8 (nghỉ 90-120s)\n" +
                         "- Romanian Deadlift: 3 x 8-10 (nghỉ 90s)\n" +
                         "- Leg Press: 3 x 10-12 (nghỉ 60s)\n" +
                         "- Leg Curls: 3 x 10-12 (nghỉ 45s)\n" +
                         "- Calf Raises: 3 x 15-20 (nghỉ 30s)\n" +
                         "Ghi chú: Giữ hình thức tốt trong tất cả các bài tập. Đừng hy sinh hình thức để tăng trọng lượng.";
                
                days[2] = "Ngày 3 – Nghỉ ngơi / Cardio nhẹ\n" +
                         "- 20-30 phút đi bộ nhanh hoặc đạp xe\n" +
                         "- Kéo giãn nhẹ nhàng";
                
                days[3] = "Ngày 4 – Thân trên (Pull)\n" +
                         "- Deadlift: 4 x 5-6 (nghỉ 120s)\n" +
                         "- Pull-ups: 4 x 6-10 (nghỉ 90s)\n" +
                         "- Barbell Row: 3 x 8-10 (nghỉ 75s)\n" +
                         "- Face Pulls: 3 x 12-15 (nghỉ 45s)\n" +
                         "- Bicep Curls: 3 x 10-12 (nghỉ 45s)\n" +
                         "Ghi chú: Deadlift là bài tập quan trọng nhất. Ưu tiên hình thức hoàn hảo.";
            } else {
                days[0] = "Day 1 – Upper Body (Push)\n" +
                         "- Bench Press: 4 x 6-8 (rest 90s)\n" +
                         "- Overhead Press: 3 x 8-10 (rest 75s)\n" +
                         "- Incline Dumbbell Press: 3 x 8-10 (rest 60s)\n" +
                         "- Tricep Dips: 3 x 10-12 (rest 45s)\n" +
                         "- Cable Flyes: 2 x 12-15 (rest 45s)\n" +
                         "Coaching notes: Focus on progressive overload. Increase weight or reps each week.";
                
                days[1] = "Day 2 – Lower Body (Legs)\n" +
                         "- Back Squat: 4 x 6-8 (rest 90-120s)\n" +
                         "- Romanian Deadlift: 3 x 8-10 (rest 90s)\n" +
                         "- Leg Press: 3 x 10-12 (rest 60s)\n" +
                         "- Leg Curls: 3 x 10-12 (rest 45s)\n" +
                         "- Calf Raises: 3 x 15-20 (rest 30s)\n" +
                         "Coaching notes: Maintain good form in all exercises. Don't sacrifice form for weight.";
                
                days[2] = "Day 3 – Rest / Light Cardio\n" +
                         "- 20-30 min brisk walk or cycling\n" +
                         "- Light stretching";
                
                days[3] = "Day 4 – Upper Body (Pull)\n" +
                         "- Deadlift: 4 x 5-6 (rest 120s)\n" +
                         "- Pull-ups: 4 x 6-10 (rest 90s)\n" +
                         "- Barbell Row: 3 x 8-10 (rest 75s)\n" +
                         "- Face Pulls: 3 x 12-15 (rest 45s)\n" +
                         "- Bicep Curls: 3 x 10-12 (rest 45s)\n" +
                         "Coaching notes: Deadlift is the most important exercise. Prioritize perfect form.";
            }
        } else {
            // Calisthenics
            if ("vi".equals(language)) {
                days[0] = "Ngày 1 – Thân trên (Push)\n" +
                         "- Push-ups: 4 x 12-15 (nghỉ 60s)\n" +
                         "- Pike Push-ups: 3 x 10-12 (nghỉ 60s)\n" +
                         "- Diamond Push-ups: 3 x 8-12 (nghỉ 60s)\n" +
                         "- Dips (ghế): 3 x 10-15 (nghỉ 45s)\n" +
                         "- Handstand Push-ups (tường): 2 x 5-8 (nghỉ 60s)\n" +
                         "Ghi chú: Tăng độ khó bằng cách thêm trọng lượng (ba lô) hoặc nâng cao chân.";
                
                days[1] = "Ngày 2 – Thân dưới (Legs)\n" +
                         "- Pistol Squats (hỗ trợ): 3 x 5-8 mỗi chân (nghỉ 60s)\n" +
                         "- Bulgarian Split Squats: 4 x 12-15 mỗi chân (nghỉ 45s)\n" +
                         "- Single-leg Glute Bridge: 3 x 12-15 mỗi chân (nghỉ 45s)\n" +
                         "- Calf Raises: 4 x 20-25 (nghỉ 30s)\n" +
                         "- Jump Squats: 3 x 10-15 (nghỉ 45s)\n" +
                         "Ghi chú: Tập trung vào phạm vi chuyển động đầy đủ và kiểm soát.";
                
                days[2] = "Ngày 3 – Nghỉ ngơi / Cardio nhẹ\n" +
                         "- 20-30 phút đi bộ nhanh hoặc chạy bộ nhẹ";
                
                days[3] = "Ngày 4 – Thân trên (Pull)\n" +
                         "- Pull-ups: 4 x 6-10 (nghỉ 90s)\n" +
                         "- Chin-ups: 3 x 6-10 (nghỉ 75s)\n" +
                         "- Inverted Rows: 4 x 10-15 (nghỉ 60s)\n" +
                         "- Archer Rows: 3 x 5-8 mỗi bên (nghỉ 60s)\n" +
                         "- Hanging Leg Raises: 3 x 10-15 (nghỉ 45s)\n" +
                         "Ghi chú: Nếu không có thanh kéo, sử dụng dây kháng lực hoặc biến thể dưới bàn.";
            } else {
                days[0] = "Day 1 – Upper Body (Push)\n" +
                         "- Push-ups: 4 x 12-15 (rest 60s)\n" +
                         "- Pike Push-ups: 3 x 10-12 (rest 60s)\n" +
                         "- Diamond Push-ups: 3 x 8-12 (rest 60s)\n" +
                         "- Dips (chair): 3 x 10-15 (rest 45s)\n" +
                         "- Handstand Push-ups (wall): 2 x 5-8 (rest 60s)\n" +
                         "Coaching notes: Increase difficulty by adding weight (backpack) or elevating feet.";
                
                days[1] = "Day 2 – Lower Body (Legs)\n" +
                         "- Pistol Squats (assisted): 3 x 5-8 each leg (rest 60s)\n" +
                         "- Bulgarian Split Squats: 4 x 12-15 each leg (rest 45s)\n" +
                         "- Single-leg Glute Bridge: 3 x 12-15 each leg (rest 45s)\n" +
                         "- Calf Raises: 4 x 20-25 (rest 30s)\n" +
                         "- Jump Squats: 3 x 10-15 (rest 45s)\n" +
                         "Coaching notes: Focus on full range of motion and control.";
                
                days[2] = "Day 3 – Rest / Light Cardio\n" +
                         "- 20-30 min brisk walk or light jog";
                
                days[3] = "Day 4 – Upper Body (Pull)\n" +
                         "- Pull-ups: 4 x 6-10 (rest 90s)\n" +
                         "- Chin-ups: 3 x 6-10 (rest 75s)\n" +
                         "- Inverted Rows: 4 x 10-15 (rest 60s)\n" +
                         "- Archer Rows: 3 x 5-8 each side (rest 60s)\n" +
                         "- Hanging Leg Raises: 3 x 10-15 (rest 45s)\n" +
                         "Coaching notes: If no pull-up bar, use resistance bands or table variations.";
            }
        }
        
        return days;
    }
    
    /**
     * Generates advanced 5-day push/pull/legs split.
     */
    private String[] generateAdvancedPlan(boolean isGym, String language) {
        String[] days = new String[5];
        
        if (isGym) {
            if ("vi".equals(language)) {
                days[0] = "Ngày 1 – Push (Đẩy)\n" +
                         "- Bench Press: 5 x 5 (nghỉ 120s)\n" +
                         "- Overhead Press: 4 x 6-8 (nghỉ 90s)\n" +
                         "- Incline Dumbbell Press: 3 x 8-10 (nghỉ 75s)\n" +
                         "- Lateral Raises: 3 x 12-15 (nghỉ 45s)\n" +
                         "- Tricep Dips: 3 x 10-12 (nghỉ 60s)\n" +
                         "- Cable Tricep Extensions: 2 x 12-15 (nghỉ 45s)\n" +
                         "Ghi chú: Tập trung vào progressive overload. Theo dõi trọng lượng và số lần lặp.";
                
                days[1] = "Ngày 2 – Pull (Kéo)\n" +
                         "- Deadlift: 5 x 3-5 (nghỉ 180s)\n" +
                         "- Weighted Pull-ups: 4 x 6-8 (nghỉ 90s)\n" +
                         "- Barbell Row: 4 x 8-10 (nghỉ 75s)\n" +
                         "- T-Bar Row: 3 x 10-12 (nghỉ 60s)\n" +
                         "- Face Pulls: 3 x 15-20 (nghỉ 45s)\n" +
                         "- Hammer Curls: 3 x 10-12 (nghỉ 45s)\n" +
                         "Ghi chú: Deadlift là bài tập quan trọng nhất. Đảm bảo phục hồi đầy đủ giữa các hiệp.";
                
                days[2] = "Ngày 3 – Legs (Chân)\n" +
                         "- Back Squat: 5 x 5 (nghỉ 120-150s)\n" +
                         "- Romanian Deadlift: 4 x 6-8 (nghỉ 90s)\n" +
                         "- Leg Press: 4 x 10-12 (nghỉ 60s)\n" +
                         "- Walking Lunges: 3 x 12 mỗi chân (nghỉ 60s)\n" +
                         "- Leg Curls: 3 x 10-12 (nghỉ 45s)\n" +
                         "- Calf Raises: 4 x 15-20 (nghỉ 30s)\n" +
                         "Ghi chú: Ngày chân cường độ cao. Đảm bảo dinh dưỡng và giấc ngủ đầy đủ.";
                
                days[3] = "Ngày 4 – Push (Đẩy) - Phụ\n" +
                         "- Incline Barbell Press: 4 x 6-8 (nghỉ 90s)\n" +
                         "- Dumbbell Shoulder Press: 3 x 8-10 (nghỉ 75s)\n" +
                         "- Cable Flyes: 3 x 12-15 (nghỉ 60s)\n" +
                         "- Lateral Raises: 3 x 15-20 (nghỉ 45s)\n" +
                         "- Overhead Tricep Extension: 3 x 10-12 (nghỉ 45s)\n" +
                         "Ghi chú: Tập trung vào các nhóm cơ yếu hơn. Sử dụng trọng lượng nhẹ hơn một chút.";
                
                days[4] = "Ngày 5 – Pull (Kéo) - Phụ\n" +
                         "- T-Bar Row: 4 x 8-10 (nghỉ 75s)\n" +
                         "- Lat Pulldown: 4 x 10-12 (nghỉ 60s)\n" +
                         "- Cable Rows: 3 x 12-15 (nghỉ 60s)\n" +
                         "- Rear Delt Flyes: 3 x 15-20 (nghỉ 45s)\n" +
                         "- Preacher Curls: 3 x 10-12 (nghỉ 45s)\n" +
                         "Ghi chú: Hoàn thiện các nhóm cơ kéo. Tập trung vào cảm giác cơ bắp.";
            } else {
                days[0] = "Day 1 – Push\n" +
                         "- Bench Press: 5 x 5 (rest 120s)\n" +
                         "- Overhead Press: 4 x 6-8 (rest 90s)\n" +
                         "- Incline Dumbbell Press: 3 x 8-10 (rest 75s)\n" +
                         "- Lateral Raises: 3 x 12-15 (rest 45s)\n" +
                         "- Tricep Dips: 3 x 10-12 (rest 60s)\n" +
                         "- Cable Tricep Extensions: 2 x 12-15 (rest 45s)\n" +
                         "Coaching notes: Focus on progressive overload. Track weight and reps.";
                
                days[1] = "Day 2 – Pull\n" +
                         "- Deadlift: 5 x 3-5 (rest 180s)\n" +
                         "- Weighted Pull-ups: 4 x 6-8 (rest 90s)\n" +
                         "- Barbell Row: 4 x 8-10 (rest 75s)\n" +
                         "- T-Bar Row: 3 x 10-12 (rest 60s)\n" +
                         "- Face Pulls: 3 x 15-20 (rest 45s)\n" +
                         "- Hammer Curls: 3 x 10-12 (rest 45s)\n" +
                         "Coaching notes: Deadlift is the most important exercise. Ensure full recovery between sets.";
                
                days[2] = "Day 3 – Legs\n" +
                         "- Back Squat: 5 x 5 (rest 120-150s)\n" +
                         "- Romanian Deadlift: 4 x 6-8 (rest 90s)\n" +
                         "- Leg Press: 4 x 10-12 (rest 60s)\n" +
                         "- Walking Lunges: 3 x 12 each leg (rest 60s)\n" +
                         "- Leg Curls: 3 x 10-12 (rest 45s)\n" +
                         "- Calf Raises: 4 x 15-20 (rest 30s)\n" +
                         "Coaching notes: High intensity leg day. Ensure adequate nutrition and sleep.";
                
                days[3] = "Day 4 – Push (Accessory)\n" +
                         "- Incline Barbell Press: 4 x 6-8 (rest 90s)\n" +
                         "- Dumbbell Shoulder Press: 3 x 8-10 (rest 75s)\n" +
                         "- Cable Flyes: 3 x 12-15 (rest 60s)\n" +
                         "- Lateral Raises: 3 x 15-20 (rest 45s)\n" +
                         "- Overhead Tricep Extension: 3 x 10-12 (rest 45s)\n" +
                         "Coaching notes: Focus on weaker muscle groups. Use slightly lighter weight.";
                
                days[4] = "Day 5 – Pull (Accessory)\n" +
                         "- T-Bar Row: 4 x 8-10 (rest 75s)\n" +
                         "- Lat Pulldown: 4 x 10-12 (rest 60s)\n" +
                         "- Cable Rows: 3 x 12-15 (rest 60s)\n" +
                         "- Rear Delt Flyes: 3 x 15-20 (rest 45s)\n" +
                         "- Preacher Curls: 3 x 10-12 (rest 45s)\n" +
                         "Coaching notes: Complete pulling muscles. Focus on mind-muscle connection.";
            }
        } else {
            // Calisthenics
            if ("vi".equals(language)) {
                days[0] = "Ngày 1 – Push (Đẩy)\n" +
                         "- Handstand Push-ups: 4 x 5-8 (nghỉ 90s)\n" +
                         "- Archer Push-ups: 4 x 6-10 mỗi bên (nghỉ 75s)\n" +
                         "- Dips: 4 x 10-15 (nghỉ 60s)\n" +
                         "- Pike Push-ups: 3 x 12-15 (nghỉ 45s)\n" +
                         "- Diamond Push-ups: 3 x 10-12 (nghỉ 45s)\n" +
                         "- Tricep Dips: 2 x 15-20 (nghỉ 30s)\n" +
                         "Ghi chú: Tăng độ khó bằng cách thêm trọng lượng hoặc nâng cao chân.";
                
                days[1] = "Ngày 2 – Pull (Kéo)\n" +
                         "- Weighted Pull-ups: 5 x 5-8 (nghỉ 120s)\n" +
                         "- Muscle-ups: 3 x 3-5 (nghỉ 90s)\n" +
                         "- Archer Pull-ups: 4 x 5-8 mỗi bên (nghỉ 75s)\n" +
                         "- Inverted Rows: 4 x 12-15 (nghỉ 60s)\n" +
                         "- Face Pulls (dây kháng lực): 3 x 15-20 (nghỉ 45s)\n" +
                         "- Hanging Leg Raises: 3 x 15-20 (nghỉ 45s)\n" +
                         "Ghi chú: Pull-ups là bài tập quan trọng nhất. Ưu tiên hình thức hoàn hảo.";
                
                days[2] = "Ngày 3 – Legs (Chân)\n" +
                         "- Pistol Squats: 4 x 5-8 mỗi chân (nghỉ 90s)\n" +
                         "- Jump Squats: 4 x 15-20 (nghỉ 60s)\n" +
                         "- Bulgarian Split Squats: 4 x 15-20 mỗi chân (nghỉ 60s)\n" +
                         "- Single-leg Glute Bridge: 3 x 15-20 mỗi chân (nghỉ 45s)\n" +
                         "- Calf Raises: 4 x 25-30 (nghỉ 30s)\n" +
                         "- Wall Sits: 3 x 45-60s (nghỉ 30s)\n" +
                         "Ghi chú: Ngày chân cường độ cao. Đảm bảo phục hồi đầy đủ.";
                
                days[3] = "Ngày 4 – Push (Đẩy) - Phụ\n" +
                         "- Decline Push-ups: 4 x 12-15 (nghỉ 60s)\n" +
                         "- Wide Push-ups: 3 x 12-15 (nghỉ 60s)\n" +
                         "- Dips: 3 x 12-15 (nghỉ 60s)\n" +
                         "- Pike Push-ups: 3 x 15-20 (nghỉ 45s)\n" +
                         "- Tricep Dips: 3 x 15-20 (nghỉ 45s)\n" +
                         "Ghi chú: Tập trung vào các nhóm cơ yếu hơn. Sử dụng biến thể dễ hơn nếu cần.";
                
                days[4] = "Ngày 5 – Pull (Kéo) - Phụ\n" +
                         "- Chin-ups: 4 x 8-12 (nghỉ 75s)\n" +
                         "- Inverted Rows: 4 x 15-20 (nghỉ 60s)\n" +
                         "- Archer Rows: 3 x 8-12 mỗi bên (nghỉ 60s)\n" +
                         "- Face Pulls (dây kháng lực): 3 x 20-25 (nghỉ 45s)\n" +
                         "- Hanging Knee Raises: 3 x 15-20 (nghỉ 45s)\n" +
                         "Ghi chú: Hoàn thiện các nhóm cơ kéo. Tập trung vào cảm giác cơ bắp.";
            } else {
                days[0] = "Day 1 – Push\n" +
                         "- Handstand Push-ups: 4 x 5-8 (rest 90s)\n" +
                         "- Archer Push-ups: 4 x 6-10 each side (rest 75s)\n" +
                         "- Dips: 4 x 10-15 (rest 60s)\n" +
                         "- Pike Push-ups: 3 x 12-15 (rest 45s)\n" +
                         "- Diamond Push-ups: 3 x 10-12 (rest 45s)\n" +
                         "- Tricep Dips: 2 x 15-20 (rest 30s)\n" +
                         "Coaching notes: Increase difficulty by adding weight or elevating feet.";
                
                days[1] = "Day 2 – Pull\n" +
                         "- Weighted Pull-ups: 5 x 5-8 (rest 120s)\n" +
                         "- Muscle-ups: 3 x 3-5 (rest 90s)\n" +
                         "- Archer Pull-ups: 4 x 5-8 each side (rest 75s)\n" +
                         "- Inverted Rows: 4 x 12-15 (rest 60s)\n" +
                         "- Face Pulls (resistance band): 3 x 15-20 (rest 45s)\n" +
                         "- Hanging Leg Raises: 3 x 15-20 (rest 45s)\n" +
                         "Coaching notes: Pull-ups are the most important exercise. Prioritize perfect form.";
                
                days[2] = "Day 3 – Legs\n" +
                         "- Pistol Squats: 4 x 5-8 each leg (rest 90s)\n" +
                         "- Jump Squats: 4 x 15-20 (rest 60s)\n" +
                         "- Bulgarian Split Squats: 4 x 15-20 each leg (rest 60s)\n" +
                         "- Single-leg Glute Bridge: 3 x 15-20 each leg (rest 45s)\n" +
                         "- Calf Raises: 4 x 25-30 (rest 30s)\n" +
                         "- Wall Sits: 3 x 45-60s (rest 30s)\n" +
                         "Coaching notes: High intensity leg day. Ensure adequate recovery.";
                
                days[3] = "Day 4 – Push (Accessory)\n" +
                         "- Decline Push-ups: 4 x 12-15 (rest 60s)\n" +
                         "- Wide Push-ups: 3 x 12-15 (rest 60s)\n" +
                         "- Dips: 3 x 12-15 (rest 60s)\n" +
                         "- Pike Push-ups: 3 x 15-20 (rest 45s)\n" +
                         "- Tricep Dips: 3 x 15-20 (rest 45s)\n" +
                         "Coaching notes: Focus on weaker muscle groups. Use easier variations if needed.";
                
                days[4] = "Day 5 – Pull (Accessory)\n" +
                         "- Chin-ups: 4 x 8-12 (rest 75s)\n" +
                         "- Inverted Rows: 4 x 15-20 (rest 60s)\n" +
                         "- Archer Rows: 3 x 8-12 each side (rest 60s)\n" +
                         "- Face Pulls (resistance band): 3 x 20-25 (rest 45s)\n" +
                         "- Hanging Knee Raises: 3 x 15-20 (rest 45s)\n" +
                         "Coaching notes: Complete pulling muscles. Focus on mind-muscle connection.";
            }
        }
        
        return days;
    }
    
    /**
     * Processes nutrition-related messages.
     * Answers the user's question directly using their data.
     * 
     * @param language UI language ("en" or "vi")
     */
    private String processNutritionMessage(User user, String message, CoachContext context, String language) {
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
     * 
     * @param language UI language ("en" or "vi")
     */
    private String processGeneralMessage(User user, String message, CoachContext context, String language) {
        // Check if it's a greeting
        if (message.contains("hello") || message.contains("hi") || message.contains("hey") ||
            message.contains("xin chào") || message.contains("chào") || message.contains("xin chào")) {
            // Don't spam stats in greetings - keep it simple
            if ("vi".equals(language)) {
                return "Xin chào! Tôi là huấn luyện viên AI của bạn. Tôi có thể giúp gì cho bạn hôm nay?";
            } else {
                return "Hello! I'm your AI Coach. What can I help you with today?";
            }
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
        if (message.contains("workout plan") || message.contains("workout routine") || message.contains("training plan") ||
            message.contains("kế hoạch tập") || message.contains("lịch tập")) {
            return processWorkoutMessage(user, message, context, language);
        }
        
        // Check if asking about meals/nutrition
        if (message.contains("meal") || message.contains("eat") || message.contains("nutrition") || message.contains("calorie") || message.contains("macro") ||
            message.contains("bữa ăn") || message.contains("ăn") || message.contains("dinh dưỡng") || message.contains("calo")) {
            return processNutritionMessage(user, message, context, language);
        }
        
        // Check if asking about app help
        if (message.contains("how") && (message.contains("generate") || message.contains("create") || message.contains("use") || message.contains("do")) ||
            message.contains("làm thế nào") || message.contains("cách")) {
            return processAppHelpMessage(message, context, language);
        }
        
        // For other questions, provide a direct helpful answer without stat spamming
        // Don't repeat calorie/protein unless directly relevant to the question
        if ("vi".equals(language)) {
            return "Tôi có thể giúp với tập luyện, dinh dưỡng, lập kế hoạch bữa ăn và theo dõi tiến trình của bạn. Bạn có câu hỏi cụ thể nào không?";
        } else {
            return "I can help with workouts, nutrition, meal planning, and tracking your progress. What specific question can I answer?";
        }
    }
    
    /**
     * Generates a comprehensive nutrition summary for broad questions.
     * Follows structured format without stat spamming.
     * 
     * @param language UI language ("en" or "vi")
     */
    private String generateNutritionSummary(User user, CoachContext context, String language) {
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
     * For workout-related questions, automatically generates complete 7-day plan.
     * 
     * @param language UI language ("en" or "vi")
     */
    private String generateWorkoutSummary(User user, CoachContext context, String language) {
        // For workout questions, generate complete 7-day plan instead of summary
        return generate7DayTrainingPlan(user, context, "unknown", language);
    }
    
    /**
     * Generates a general overview when user asks "all" without context.
     * Provides most relevant information based on available data.
     * 
     * @param language UI language ("en" or "vi")
     */
    private String generateGeneralOverview(User user, CoachContext context, String language) {
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
     * 
     * @param language UI language ("en" or "vi")
     */
    private List<String> generateActionsFromChat(CoachContext context, String language) {
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

