/**
 * Application Constants
 * 
 * Centralized constants for labels, messages, and UI text.
 * This helps maintain consistency and makes internationalization easier.
 */

// Error Messages (Generic - don't expose internal details)
export const ERROR_MESSAGES = {
  GENERIC: 'An error occurred. Please try again.',
  NETWORK: 'Unable to connect to the server. Please check your internet connection.',
  UNAUTHORIZED: 'Your session has expired. Please log in again.',
  FORBIDDEN: 'You do not have permission to perform this action.',
  NOT_FOUND: 'The requested resource was not found.',
  SERVER_ERROR: 'Server error occurred. Please try again later.',
  VALIDATION: 'Please check your input and try again.',
  LOGIN_FAILED: 'Invalid credentials. Please try again.',
  SIGNUP_FAILED: 'Unable to create account. Please try again.',
  PROFILE_LOAD_FAILED: 'Failed to load profile. Please try again.',
  PROFILE_SAVE_FAILED: 'Failed to save profile. Please try again.',
  PROFILE_UPDATE_FAILED: 'Failed to update profile. Please try again.',
  ANALYSIS_LOAD_FAILED: 'Failed to load analysis. Please try again.',
  EXPORT_FAILED: 'Failed to export data. Please try again.',
}

// Success Messages
export const SUCCESS_MESSAGES = {
  LOGIN_SUCCESS: 'Login successful',
  SIGNUP_SUCCESS: 'Account created successfully',
  PROFILE_SAVED: 'Profile saved successfully',
  PROFILE_UPDATED: 'Profile updated successfully',
  EXPORT_SUCCESS: 'Data exported successfully',
}

// UI Labels
export const UI_LABELS = {
  APP_NAME: 'AI Fitness',
  LOGIN: 'Sign In',
  SIGNUP: 'Sign Up',
  LOGOUT: 'Logout',
  DASHBOARD: 'Dashboard',
  PROFILE: 'Profile',
  EDIT_PROFILE: 'Edit Profile',
  SAVE: 'Save',
  CANCEL: 'Cancel',
  SUBMIT: 'Submit',
  RETRY: 'Retry',
  REFRESH: 'Refresh',
  DOWNLOAD: 'Download',
  LOADING: 'Loading...',
  SAVING: 'Saving...',
  RECALCULATING: 'Recalculating...',
  DOWNLOADING: 'Downloading...',
  SIGNING_IN: 'Signing in...',
  CREATING_ACCOUNT: 'Creating account...',
  SAVING_PROFILE: 'Saving Profile...',
  EDITING: 'Editing...',
}

// Button Text
export const BUTTON_TEXT = {
  SIGN_IN: 'Sign In',
  SIGN_UP: 'Sign Up',
  LOG_OUT: 'Logout',
  SAVE_CHANGES: 'Save Changes',
  CANCEL: 'Cancel',
  RETRY: 'Retry',
  REFRESH: 'Refresh',
  DOWNLOAD_PROFILE: 'Download my profile',
  DOWNLOAD_PROFILE_PDF: 'DOWNLOAD PROFILE (PDF)',
  RECALCULATE: '🔄 Recalculate',
  EDIT_PROFILE: '✏️ Edit Profile',
  COMPLETE_PROFILE_SETUP: 'Complete Profile Setup',
  VIEW_PROFILE: 'View your profile',
}

// Form Labels
export const FORM_LABELS = {
  USERNAME: 'Username',
  EMAIL: 'Email',
  PASSWORD: 'Password',
  CONFIRM_PASSWORD: 'Confirm Password',
  USERNAME_OR_EMAIL: 'Username or Email',
  NAME: 'Name',
  AGE: 'Age',
  WEIGHT: 'Weight (kg)',
  HEIGHT: 'Height (cm)',
  WAIST: 'Waist (cm)',
  HIP: 'Hip (cm)',
  GENDER: 'Gender',
  ACTIVITY_LEVEL: 'Activity Level',
  FITNESS_GOAL: 'Fitness Goal',
}

// Placeholders
export const PLACEHOLDERS = {
  USERNAME: 'Choose a username',
  EMAIL: 'Enter your email',
  PASSWORD: 'Enter your password',
  CONFIRM_PASSWORD: 'Confirm your password',
  USERNAME_OR_EMAIL: 'Enter your username or email',
  NAME: 'Enter your full name',
  AGE: 'Enter your age',
  WEIGHT: 'Enter your weight in kilograms',
  HEIGHT: 'Enter your height in centimeters',
  WAIST: 'Enter your waist measurement in cm',
  HIP: 'Enter your hip measurement in cm',
  SELECT_ACTIVITY_LEVEL: 'Select activity level',
  SELECT_FITNESS_GOAL: 'Select fitness goal',
}

// Info Messages
export const INFO_MESSAGES = {
  WELCOME_BACK: 'Welcome back! Please sign in to your account.',
  CREATE_ACCOUNT: 'Create your account to get started.',
  COMPLETE_PROFILE: 'Complete your fitness profile to get personalized recommendations.',
  PROFILE_INCOMPLETE: 'Please complete your profile before using the app.',
  WELCOME_NEW_USER: 'Welcome! Please complete your profile setup to get started.',
  EMAIL_READ_ONLY: 'Email cannot be changed',
  PROFILE_AUTO_RECALCULATE: '💡 Note: After saving, all metrics (BMI, WHR, BMR, TDEE, calories, protein) will be automatically recalculated and displayed on your dashboard.',
  ALREADY_HAVE_ACCOUNT: 'Already have an account?',
  DONT_HAVE_ACCOUNT: "Don't have an account?",
  SIGN_IN_HERE: 'Sign in here',
  SIGN_UP_HERE: 'Sign up here',
  ALREADY_HAVE_PROFILE: 'Already have a profile?',
}

// Validation Messages
export const VALIDATION_MESSAGES = {
  REQUIRED: 'This field is required',
  PASSWORD_MISMATCH: 'Passwords do not match',
  PASSWORD_MIN_LENGTH: 'Password must be at least 6 characters long',
  NAME_REQUIRED: 'Name is required',
  AGE_REQUIRED: 'Valid age is required (1-120)',
  WEIGHT_REQUIRED: 'Valid weight is required',
  HEIGHT_REQUIRED: 'Valid height is required',
  WAIST_REQUIRED: 'Valid waist measurement is required',
  HIP_REQUIRED: 'Valid hip measurement is required',
  ACTIVITY_LEVEL_REQUIRED: 'Valid activity level is required',
  GOAL_REQUIRED: 'Valid goal is required',
}

// Page Titles
export const PAGE_TITLES = {
  DASHBOARD: 'Fitness Dashboard',
  PROFILE: 'Your Profile',
  EDIT_PROFILE: 'Edit Profile',
  PROFILE_SETUP: 'Profile Setup',
  LOGIN: 'AI Fitness',
  SIGNUP: 'AI Fitness',
}

// Status Messages
export const STATUS_MESSAGES = {
  LOADING_DASHBOARD: 'Loading your dashboard...',
  LOADING_PROFILE: 'Loading your profile...',
  NO_ANALYSIS_DATA: 'No Analysis Data',
  NO_PROFILE_DATA: 'No Profile Data',
  PROFILE_NOT_AVAILABLE: 'Your profile is not available. Please create your profile.',
  PROFILE_NOT_FOUND: 'Profile Not Found',
  PROFILE_SETUP_REQUIRED: 'Please complete your profile setup first.',
  COMPLETE_PROFILE_TO_SEE_ANALYSIS: 'Complete your profile setup to see your fitness analysis.',
  ANALYZING_PROGRESS: 'Analyzing your progress...',
  AI_COACH_UNAVAILABLE: 'AI coach is not available right now.',
}

// AI Coach Labels
export const AI_COACH_LABELS = {
  TITLE: 'AI Coach',
  REFRESH_ADVICE: 'Refresh advice',
  SUMMARY: 'Summary',
  RECOMMENDATIONS: 'Recommendations',
}

// Meal Plan Labels
export const MEAL_PLAN_LABELS = {
  TITLE: 'Meal Plan',
  GENERATE_NEW: 'Generate New Meal Plan',
  GENERATING: 'Generating...',
  NO_MEAL_PLAN: 'No Meal Plan Found',
  NO_MEAL_PLAN_DESCRIPTION: 'You don\'t have a meal plan yet. Generate one to get started!',
  CONFIRM_GENERATE: 'Generate New Meal Plan?',
  CONFIRM_GENERATE_MESSAGE: 'This will overwrite your current meal plan. Are you sure?',
  DAILY_TARGETS: 'Daily Targets',
  CALORIES: 'Calories',
  PROTEIN: 'Protein',
  CARBS: 'Carbs',
  FATS: 'Fats',
  GRAMS: 'g',
  KCAL: 'kcal',
  BREAKFAST: 'Breakfast',
  LUNCH: 'Lunch',
  DINNER: 'Dinner',
  SNACK: 'Snack',
  MONDAY: 'Monday',
  TUESDAY: 'Tuesday',
  WEDNESDAY: 'Wednesday',
  THURSDAY: 'Thursday',
  FRIDAY: 'Friday',
  SATURDAY: 'Saturday',
  SUNDAY: 'Sunday',
  WEEK_START: 'Week of',
}

// Meal Plan Status Messages
export const MEAL_PLAN_STATUS = {
  LOADING: 'Loading meal plan...',
  GENERATING: 'Generating your personalized meal plan...',
  LOAD_FAILED: 'Failed to load meal plan. Please try again.',
  GENERATE_FAILED: 'Failed to generate meal plan. Please try again.',
  GENERATE_SUCCESS: 'Meal plan generated successfully!',
}




