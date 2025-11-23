package com.aifitness.service;

import org.springframework.stereotype.Service;

/**
 * Body Metrics Service
 * 
 * Handles all body metric calculations (BMI, WHR, WHtR, Body Fat).
 * 
 * LOGIC TO MOVE FROM Infoclient.java:
 * 
 * 1. BMI Calculation:
 *    - Constructor line 96: this.BMI = weight * 10000 / (height*height)
 *    - Updated in setWeight() (line 268) and setHeight() (line 294)
 * 
 * 2. WHR (Waist-to-Hip Ratio) Calculation:
 *    - Constructor line 97: this.WHR = Waist/Hip
 *    - Updated in setWaist() (line 341) and setHip() (line 331)
 * 
 * 3. WHtR (Waist-to-Height Ratio) Calculation:
 *    - Constructor line 98: this.WHtR = Waist/height
 *    - Updated in setWaist() (line 342) and setHeight() (line 295)
 * 
 * 4. Body Fat Percentage Calculation:
 *    - BodyFatCal() method (lines 146-153)
 *      - Male: 1.20 * BMI + 0.23 * age - 16.2
 *      - Female: 1.20 * BMI + 0.23 * age - 5.4
 *    - Called in constructor (line 141)
 *    - Updated in setWeight() (line 269), setAge() (line 359), setSex() (line 385)
 * 
 * LOGIC TO MOVE FROM mainOne.java:
 * - BMI categorization and interpretation (lines 233-250)
 * - WHR health risk assessment (lines 259-274)
 * - WHtR risk analysis (lines 280-291)
 */
@Service
public class BodyMetricsService {
    
    /**
     * Calculates BMI from weight and height.
     * 
     * LOGIC FROM Infoclient.java constructor line 96:
     * this.BMI = weight * 10000 / (height*height)
     */
    /**
     * Calculates BMI from weight and height.
     * 
     * LOGIC FROM Infoclient.java constructor line 96:
     * this.BMI = weight * 10000 / (height*height)
     */
    public double calculateBMI(double weight, double height) {
        if (height == 0) return 0.0;
        return weight * 10000 / (height * height);
    }
    
    /**
     * Determines BMI category (Underweight, Normal, Overweight, Obese).
     * 
     * LOGIC FROM mainOne.java (lines 233-250)
     */
    public String getBMICategory(double bmi) {
        if (bmi < 18.5) {
            return "Underweight";
        } else if (bmi >= 18.5 && bmi <= 24.9) {
            return "Normal";
        } else if (bmi >= 25 && bmi <= 29.9) {
            return "Overweight";
        } else if (bmi >= 30 && bmi <= 34.9) {
            return "Obese (Class I)";
        } else if (bmi >= 35 && bmi <= 39.9) {
            return "Obese (Class II)";
        } else {
            return "Obese (Class III)";
        }
    }
    
    /**
     * Calculates WHR (Waist-to-Hip Ratio).
     * 
     * LOGIC FROM Infoclient.java constructor line 97:
     * this.WHR = Waist/Hip
     */
    public double calculateWHR(double waist, double hip) {
        if (hip == 0) return 0.0;
        return waist / hip;
    }
    
    /**
     * Assesses WHR health risk based on sex.
     * 
     * LOGIC FROM mainOne.java (lines 259-274)
     * - Male: Good if < 0.9, Risk if >= 0.9
     * - Female: Good if < 0.85, Risk if >= 0.85
     */
    public String assessWHRHealth(double whr, boolean isMale) {
        if (isMale) {
            if (whr < 0.9) {
                return "Good condition";
            } else {
                return "At risk";
            }
        } else {
            if (whr < 0.85) {
                return "Good condition";
            } else {
                return "At risk";
            }
        }
    }
    
    /**
     * Calculates WHtR (Waist-to-Height Ratio).
     * 
     * LOGIC FROM Infoclient.java constructor line 98:
     * this.WHtR = Waist/height
     */
    public double calculateWHtR(double waist, double height) {
        if (height == 0) return 0.0;
        return waist / height;
    }
    
    /**
     * Analyzes WHtR risk level.
     * 
     * LOGIC FROM mainOne.java (lines 280-291)
     * - < 0.4: Too lean
     * - 0.4-0.49: Normal
     * - 0.5-0.59: Central fat accumulation
     * - >= 0.6: High visceral fat
     */
    public String analyzeWHtRRisk(double whtr) {
        if (whtr < 0.4) {
            return "Too lean";
        } else if (whtr >= 0.4 && whtr <= 0.49) {
            return "Normal";
        } else if (whtr >= 0.5 && whtr <= 0.59) {
            return "Central fat accumulation";
        } else {
            return "High visceral fat";
        }
    }
    
    /**
     * Calculates body fat percentage.
     * 
     * LOGIC FROM Infoclient.java BodyFatCal() method (lines 146-153):
     * - Male: 1.20 * BMI + 0.23 * age - 16.2
     * - Female: 1.20 * BMI + 0.23 * age - 5.4
     */
    public double calculateBodyFat(double bmi, int age, boolean isMale) {
        if (isMale) {
            return 1.20 * bmi + 0.23 * age - 16.2;
        } else {
            return 1.20 * bmi + 0.23 * age - 5.4;
        }
    }
    
    /**
     * Recalculates all body metrics when profile data changes.
     * 
     * This is called when weight, height, waist, hip, age, or sex is updated.
     */
    public void recalculateAllMetrics(/* TODO: Add user parameter */) {
        // TODO: Implement full metric recalculation
        // Called by update methods to ensure all dependent metrics are updated
    }
}

