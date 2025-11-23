package com.aifitness.service;

import com.aifitness.dto.MicronutrientsDTO;
import org.springframework.stereotype.Service;

/**
 * Nutrition Service
 * 
 * Handles all nutrition-related calculations (calories, macros, micros).
 * 
 * LOGIC TO MOVE FROM Infoclient.java:
 * 
 * 1. BMR (Basal Metabolic Rate) Calculation:
 *    - Constructor lines 100-107
 *      - Male: 10*weight + 6.25*height - 5*age + 5
 *      - Female: 10*weight + 6.25*height - 5*age - 161
 * 
 * 2. TDEE (Total Daily Energy Expenditure) Calculation:
 *    - Constructor lines 109-123
 *      - Uses activity multipliers (Sedentary=1.2, Lightly=1.375, etc.)
 *      - TDEE = BMR * activity_multiplier
 *    - changeTDEE() method (lines 204-221)
 *    - changeactFloor() method (lines 223-241)
 * 
 * 3. Calorie Goal Calculation:
 *    - Constructor lines 125-136
 *      - Goal 1 (Lose weight): TDEE - 500
 *      - Goal 2 (Maintain): TDEE
 *      - Goal 3 (Gain muscle): TDEE + 300
 *      - Goal 4 (Recomposition): TDEE
 *    - changecaloGoal() method (lines 243-259)
 * 
 * 4. Protein Calculation:
 *    - setProtein() method (lines 186-201)
 *      - Goal 1 (Lose weight): 2.0 * weight
 *      - Goal 2 (Maintain): 1.4 * weight
 *      - Goal 3 (Gain muscle): 1.8 * weight
 *      - Goal 4 (Recomposition): 2.2 * weight
 * 
 * 5. Fat Calculation:
 *    - Constructor line 138: this.Fat = 0.8 * weight
 * 
 * 6. Carbohydrate Calculation:
 *    - Constructor line 139: (Calo - (protein * 4 + Fat * 9)) / 4
 *    - Each gram: Protein=4 cal, Fat=9 cal, Carb=4 cal
 * 
 * 7. Micronutrients Setup:
 *    - NutritionBasedOnSex() method (lines 155-184)
 *      - Sets sex-specific requirements for:
 *        Fiber, Water, Iron, Calcium, VitaminD, Magnesium, Zinc,
 *        VitaminB12, Potassium, Sodium
 */
@Service
public class NutritionService {
    
    // Activity level multipliers
    private static final double SEDENTARY = 1.2;      // No exercise
    private static final double LIGHTLY = 1.375;      // 1-3×/week
    private static final double MODERATELY = 1.55;    // 3-5×/week
    private static final double VERY = 1.725;         // 6-7×/week
    private static final double EXTRA = 1.9;          // 2×/day
    
    /**
     * Calculates BMR (Basal Metabolic Rate) using Mifflin-St Jeor equation.
     * 
     * LOGIC FROM Infoclient.java constructor (lines 100-107)
     */
    public double calculateBMR(double weight, double height, int age, boolean isMale) {
        if (isMale) {
            return 10 * weight + 6.25 * height - 5 * age + 5;
        } else {
            return 10 * weight + 6.25 * height - 5 * age - 161;
        }
    }
    
    /**
     * Calculates TDEE (Total Daily Energy Expenditure) based on activity level.
     * 
     * LOGIC FROM Infoclient.java constructor (lines 109-123)
     * and changeTDEE() method (lines 204-221)
     */
    public double calculateTDEE(double bmr, int activityLevel) {
        double multiplier;
        switch (activityLevel) {
            case 1:
                multiplier = SEDENTARY;
                break;
            case 2:
                multiplier = LIGHTLY;
                break;
            case 3:
                multiplier = MODERATELY;
                break;
            case 4:
                multiplier = VERY;
                break;
            case 5:
                multiplier = EXTRA;
                break;
            default:
                multiplier = SEDENTARY;
        }
        return bmr * multiplier;
    }
    
    /**
     * Calculates goal calories based on TDEE and fitness goal.
     * 
     * LOGIC FROM Infoclient.java constructor (lines 125-136)
     * and changecaloGoal() method (lines 243-259)
     * 
     * @param tdee Total Daily Energy Expenditure
     * @param goal 1=Lose weight, 2=Maintain, 3=Gain muscle, 4=Recomposition
     */
    public double calculateGoalCalories(double tdee, int goal) {
        switch (goal) {
            case 1:
                return tdee - 500; // Lose weight
            case 2:
                return tdee; // Maintain weight
            case 3:
                return tdee + 300; // Gain muscle
            case 4:
                return tdee; // Recomposition
            default:
                return tdee;
        }
    }
    
    /**
     * Calculates daily protein requirement based on goal and weight.
     * 
     * LOGIC FROM Infoclient.java setProtein() method (lines 186-201)
     * 
     * @param goal 1=Lose weight, 2=Maintain, 3=Gain muscle, 4=Recomposition
     * @param weight in kg
     */
    public double calculateProtein(int goal, double weight) {
        switch (goal) {
            case 1:
                return 2.0 * weight; // Lose weight
            case 2:
                return 1.4 * weight; // Maintain
            case 3:
                return 1.8 * weight; // Gain muscle
            case 4:
                return 2.2 * weight; // Recomposition
            default:
                return 1.4 * weight;
        }
    }
    
    /**
     * Calculates daily fat requirement.
     * 
     * LOGIC FROM Infoclient.java constructor line 138:
     * this.Fat = 0.8 * weight
     */
    public double calculateFat(double weight) {
        return 0.8 * weight;
    }
    
    /**
     * Calculates daily carbohydrate requirement.
     * 
     * LOGIC FROM Infoclient.java constructor line 139:
     * (Calo - (protein * 4 + Fat * 9)) / 4
     * 
     * Calories per gram: Protein=4, Fat=9, Carb=4
     */
    public double calculateCarbs(double totalCalories, double protein, double fat) {
        return (totalCalories - (protein * 4 + fat * 9)) / 4;
    }
    
    /**
     * Sets micronutrient requirements based on sex.
     * 
     * LOGIC FROM Infoclient.java NutritionBasedOnSex() method (lines 155-184)
     */
    public MicronutrientsDTO getMicronutrients(boolean isMale) {
        MicronutrientsDTO micros = new MicronutrientsDTO();
        if (isMale) {
            micros.setIron(8);
            micros.setCalcium(1000);
            micros.setVitaminD(15);
            micros.setMagnesium(400);
            micros.setZinc(11);
            micros.setVitaminB12(2.4);
            micros.setPotassium(3400);
            micros.setSodium(2300);
        } else {
            micros.setIron(18);
            micros.setCalcium(1000);
            micros.setVitaminD(15);
            micros.setMagnesium(310);
            micros.setZinc(8);
            micros.setVitaminB12(2.4);
            micros.setPotassium(2600);
            micros.setSodium(2300);
        }
        return micros;
    }
    
    /**
     * Gets fiber and water requirements based on sex.
     */
    public int getFiber(boolean isMale) {
        return isMale ? 38 : 25;
    }
    
    public double getWater(boolean isMale) {
        return isMale ? 3.7 : 2.7;
    }
    
    /**
     * Recalculates all nutrition values when profile data changes.
     * 
     * This is called when weight, height, age, sex, activity level, or goal is updated.
     */
    public void recalculateNutrition(/* TODO: Add user parameter */) {
        // TODO: Implement full nutrition recalculation
        // Called by update methods to ensure all dependent values are updated
    }
    
    // TODO: Create MicronutrientsDTO class to hold micronutrient values
}

