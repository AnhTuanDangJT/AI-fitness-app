/**
 * Profile Form Configuration
 * 
 * Centralized configuration for profile form fields, labels, and options.
 */

export const ACTIVITY_LEVELS = [
  { value: 1, label: 'Sedentary (no exercise)' },
  { value: 2, label: 'Lightly active (1–3×/week)' },
  { value: 3, label: 'Moderately active (3–5×/week)' },
  { value: 4, label: 'Very active (6–7×/week)' },
  { value: 5, label: 'Extra active (2×/day)' },
]

export const FITNESS_GOALS = [
  { value: 1, label: 'Lose weight' },
  { value: 2, label: 'Maintain weight' },
  { value: 3, label: 'Gain muscle' },
  { value: 4, label: 'Gain muscle and lose fat (Recomposition)' },
]

export const GENDER_OPTIONS = [
  { value: 'male', label: 'Male' },
  { value: 'female', label: 'Female' },
]

export const DIETARY_PREFERENCES = [
  { value: 'omnivore', label: 'Omnivore' },
  { value: 'vegetarian', label: 'Vegetarian' },
  { value: 'vegan', label: 'Vegan' },
  { value: 'pescatarian', label: 'Pescatarian' },
  { value: 'halal', label: 'Halal' },
  { value: 'kosher', label: 'Kosher' },
  { value: 'paleo', label: 'Paleo' },
  { value: 'keto', label: 'Keto' },
]

export const FORM_LABELS = {
  name: 'Name',
  email: 'Email',
  gender: 'Gender',
  age: 'Age',
  weight: 'Weight (kg)',
  height: 'Height (cm)',
  waist: 'Waist (cm)',
  hip: 'Hip (cm)',
  activityLevel: 'Activity Level',
  goal: 'Fitness Goal',
  dietaryPreference: 'Dietary Preference',
  dislikedFoods: 'Disliked Foods',
  maxBudgetPerDay: 'Max Budget Per Day',
  maxCookingTimePerMeal: 'Max Cooking Time Per Meal (minutes)',
}

export const FORM_PLACEHOLDERS = {
  name: 'Enter your full name',
  email: 'Email address (read-only)',
  weight: 'Enter your weight in kilograms',
  height: 'Enter your height in centimeters',
  age: 'Enter your age',
  waist: 'Enter your waist measurement in cm',
  hip: 'Enter your hip measurement in cm',
  activityLevel: 'Select activity level',
  goal: 'Select fitness goal',
  dietaryPreference: 'Select dietary preference',
  dislikedFoods: 'e.g., mushrooms, tuna, onions (comma-separated)',
  maxBudgetPerDay: 'Enter maximum budget per day',
  maxCookingTimePerMeal: 'Enter maximum cooking time in minutes',
}





