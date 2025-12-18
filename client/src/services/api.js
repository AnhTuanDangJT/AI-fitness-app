import axios from 'axios'
import { ERROR_MESSAGES } from '../config/constants'

// Use environment variable for API base URL, fallback to relative path for Vite proxy
// In development, Vite proxy handles /api -> http://localhost:8080/api
// In production, use full URL from environment variable
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

const AUTH_ENDPOINTS = [
  '/auth/signup',
  '/auth/register',
  '/auth/login',
  '/auth/verify-email',
  '/auth/resend-verification',
]

const isAuthEndpoint = (url = '') => {
  return AUTH_ENDPOINTS.some((endpoint) => url.includes(endpoint))
}

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Enable credentials for CORS
})

// Add token to requests if available
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * Get a generic error message based on error type
 * This prevents exposing internal error details to users
 */
const getGenericErrorMessage = (error) => {
  // Network errors
  if (error.code === 'ERR_NETWORK' || error.message?.includes('Network Error')) {
    return ERROR_MESSAGES.NETWORK
  }

  // HTTP status code based errors
  if (error.response) {
    const status = error.response.status
    switch (status) {
      case 401:
        return ERROR_MESSAGES.UNAUTHORIZED
      case 403:
        return ERROR_MESSAGES.FORBIDDEN
      case 404:
        return ERROR_MESSAGES.NOT_FOUND
      case 400:
        // For validation errors, we might want to show the message from backend
        // but sanitize it to avoid exposing internal details
        return error.response.data?.message || ERROR_MESSAGES.VALIDATION
      case 500:
      case 502:
      case 503:
        return ERROR_MESSAGES.SERVER_ERROR
      default:
        return ERROR_MESSAGES.GENERIC
    }
  }

  // CORS or other blocking errors
  if (error.response?.status === 0) {
    return ERROR_MESSAGES.NETWORK
  }

  // Default generic error
  return ERROR_MESSAGES.GENERIC
}

// Handle token expiration and errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && !isAuthEndpoint(error.config?.url || '')) {
      // Token expired or invalid
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    } else if (error.code === 'ERR_NETWORK' || error.message?.includes('Network Error')) {
      // Network error - backend might be down or CORS issue
      // Log detailed error for debugging, but don't expose to user
      console.error('Network Error: Unable to connect to backend. Check:', {
        apiUrl: API_BASE_URL,
        error: error.message,
        suggestion: 'Verify backend is running and CORS is configured correctly'
      })
    } else if (error.response?.status === 0) {
      // CORS error or blocked request
      // Log detailed error for debugging, but don't expose to user
      console.error('CORS Error: Request blocked. Check CORS configuration:', {
        apiUrl: API_BASE_URL,
        suggestion: 'Verify CORS_ALLOWED_ORIGINS includes frontend URL'
      })
    }
    
    // Attach generic error message to error object for easy access
    error.genericMessage = getGenericErrorMessage(error)
    
    return Promise.reject(error)
  }
)

// Auth API methods
export const authAPI = {
  signup: async (username, email, password) => {
    const response = await api.post('/auth/signup', {
      username,
      email,
      password,
    })
    return response.data
  },

  login: async (usernameOrEmail, password) => {
    const response = await api.post('/auth/login', {
      usernameOrEmail,
      password,
    })
    return response.data
  },

  verifyEmail: async (email, code) => {
    const response = await api.post('/auth/verify-email', {
      email,
      code,
    })
    return response.data
  },

  resendVerification: async (email) => {
    const response = await api.post('/auth/resend-verification', {
      email,
    })
    return response.data
  },
}

// User API methods
export const userAPI = {
  checkProfileComplete: async () => {
    const response = await api.get('/user/profile/complete')
    return response.data
  },
}

// Meal Plan API methods
export const mealPlanAPI = {
  /**
   * Gets the current meal plan for the authenticated user.
   * 
   * Returns a structured response:
   * - { type: "SUCCESS", data } - Meal plan found (200 OK)
   * - { type: "EMPTY" } - No meal plan exists (404 NOT_FOUND) - This is NOT an error
   * - { type: "ERROR", error } - Real API/network error (non-2xx, non-404)
   * 
   * Note: HTTP 404 is treated as a valid empty state, not an error.
   * This allows the UI to distinguish between "no meal plan yet" and "something went wrong".
   */
  getCurrent: async () => {
    console.log('[api.js] mealPlanAPI.getCurrent() - START')
    console.log('[api.js] Making GET request to: /ai/meals/current')
    console.log('[api.js] Token from localStorage:', localStorage.getItem('token') ? 'Token exists' : 'No token')
    
    try {
      const response = await api.get('/ai/meals/current')
      console.log('[api.js] API Response received:', {
        status: response.status,
        statusText: response.statusText,
        data: response.data,
        dataSuccess: response.data?.success,
        dataMessage: response.data?.message,
        dataData: response.data?.data
      })
      
      // Success response (200 OK with data)
      if (response.data?.success && response.data?.data) {
        console.log('[api.js] Returning SUCCESS with data')
        return { type: 'SUCCESS', data: response.data.data }
      }
      
      // This shouldn't happen with current backend, but handle it
      console.log('[api.js] Response success but no data, treating as EMPTY')
      return { type: 'EMPTY' }
      
    } catch (error) {
      console.error('[api.js] Error in mealPlanAPI.getCurrent():', {
        error: error,
        message: error.message,
        response: error.response,
        status: error.response?.status,
        statusText: error.response?.statusText,
        data: error.response?.data
      })
      
      // HTTP 404 is a valid empty state, not an error
      // Backend returns 404 when user has no meal plan
      if (error.response?.status === 404) {
        console.log('[api.js] 404 received - treating as EMPTY state (no meal plan exists)')
        return { type: 'EMPTY' }
      }
      
      // All other errors (network, 401, 500, etc.) are real errors
      console.log('[api.js] Real error occurred, returning ERROR type')
      return { 
        type: 'ERROR', 
        error: error.genericMessage || error.message || ERROR_MESSAGES.GENERIC
      }
    }
  },
  
  generate: async (weekStart) => {
    const params = weekStart ? { weekStart } : {}
    const response = await api.post('/ai/meals/generate', null, { params })
    return response.data
  },
  
  /**
   * Gets the grocery list for the authenticated user's current meal plan.
   * 
   * Returns a structured response:
   * - { type: "SUCCESS", data } - Grocery list found (200 OK)
   * - { type: "EMPTY" } - No meal plan exists (404 NOT_FOUND) - This is NOT an error
   * - { type: "ERROR", error } - Real API/network error (non-2xx, non-404)
   * 
   * Note: HTTP 404 is treated as a valid empty state, not an error.
   * This allows the UI to distinguish between "no meal plan yet" and "something went wrong".
   */
  getGroceryList: async () => {
    try {
      const response = await api.get('/ai/meals/grocery-list')
      
      // Success response (200 OK with data)
      if (response.data?.success && response.data?.data) {
        return { type: 'SUCCESS', data: response.data.data }
      }
      
      // This shouldn't happen with current backend, but handle it
      return { type: 'EMPTY' }
      
    } catch (error) {
      // HTTP 404 is a valid empty state, not an error
      // Backend returns 404 when user has no meal plan or grocery list is empty
      if (error.response?.status === 404) {
        return { type: 'EMPTY' }
      }
      
      // All other errors (network, 401, 500, etc.) are real errors
      return { 
        type: 'ERROR', 
        error: error.genericMessage || error.message || ERROR_MESSAGES.GENERIC
      }
    }
  },
}

// Meal Preferences API methods
export const mealPreferencesAPI = {
  get: async () => {
    const response = await api.get('/meal-preferences')
    return response.data
  },
  
  save: async (preferences) => {
    const response = await api.post('/meal-preferences', preferences)
    return response.data
  },
}

// AI Coach API methods
export const aiCoachAPI = {
  /**
   * Gets AI coach advice for the authenticated user.
   * 
   * Returns a structured response:
   * - { type: "SUCCESS", data } - Advice found (200 OK)
   * - { type: "EMPTY" } - No data yet (200 OK with empty data message)
   * - { type: "ERROR", error } - Real API/network error
   */
  getAdvice: async () => {
    try {
      const response = await api.get('/ai/coach/advice')
      
      // Success response (200 OK with data)
      if (response.data?.success && response.data?.data) {
        const advice = response.data.data
        
        // Check if advice is actually empty (insufficient data message)
        const hasRealAdvice = advice.summary && 
          !advice.summary.includes('need at least 2 weeks') &&
          !advice.summary.includes('Please log your weekly progress')
        
        if (hasRealAdvice) {
          return { type: 'SUCCESS', data: advice }
        } else {
          // This is an empty state (user needs more data)
          return { type: 'EMPTY', data: advice }
        }
      }
      
      // Unexpected response format
      return { type: 'ERROR', error: ERROR_MESSAGES.GENERIC }
      
    } catch (error) {
      // All errors (network, 401, 500, etc.) are real errors
      return { 
        type: 'ERROR', 
        error: error.genericMessage || error.message || ERROR_MESSAGES.GENERIC
      }
    }
  },
  
  /**
   * Sends a chat message to AI Coach.
   * 
   * Returns a structured response:
   * - { type: "SUCCESS", data } - Chat response received (200 OK)
   * - { type: "ERROR", error } - Real API/network error
   * 
   * @param {string} message - User's message (AI infers intent from content)
   * @param {string} date - Optional date (YYYY-MM-DD), defaults to today
   * @param {string} language - UI language (en | vi), defaults to 'en'
   */
  chat: async (message, date = null, language = 'en') => {
    try {
      const requestBody = {
        message,
        language: language || 'en', // Ensure language is always provided
      }
      
      if (date) {
        requestBody.date = date
      }
      
      const response = await api.post('/ai/coach/chat', requestBody)
      
      // Success response (200 OK with data)
      if (response.data?.success && response.data?.data) {
        return { type: 'SUCCESS', data: response.data.data }
      }
      
      // Unexpected response format
      return { type: 'ERROR', error: ERROR_MESSAGES.GENERIC }
      
    } catch (error) {
      // All errors (network, 401, 500, etc.) are real errors
      return { 
        type: 'ERROR', 
        error: error.genericMessage || error.message || ERROR_MESSAGES.GENERIC
      }
    }
  },
  
  /**
   * GET /api/ai/context
   * Retrieves comprehensive user context for AI Coach
   */
  getAiContext: async () => {
    try {
      const response = await api.get('/ai/context')
      
      if (response.data?.success && response.data?.data) {
        return { type: 'SUCCESS', data: response.data.data }
      }
      
      return { type: 'ERROR', error: ERROR_MESSAGES.GENERIC }
    } catch (error) {
      return { 
        type: 'ERROR', 
        error: error.genericMessage || error.message || ERROR_MESSAGES.GENERIC
      }
    }
  },
  
  /**
   * PUT /api/users/language
   * Updates user's preferred language
   */
  updateLanguage: async (language) => {
    try {
      const response = await api.put('/users/language', { language })
      
      if (response.data?.success) {
        return { type: 'SUCCESS', data: response.data.data }
      }
      
      return { type: 'ERROR', error: ERROR_MESSAGES.GENERIC }
    } catch (error) {
      return { 
        type: 'ERROR', 
        error: error.genericMessage || error.message || ERROR_MESSAGES.GENERIC
      }
    }
  },
}

// Health Check API methods
export const healthAPI = {
  /**
   * Checks the email health configuration status.
   * 
   * Returns email service configuration status for debugging production email issues.
   * 
   * @returns {Promise<Object>} Email configuration status
   */
  checkEmailHealth: async () => {
    try {
      const response = await api.get('/health/email')
      return response.data
    } catch (error) {
      console.error('Error checking email health:', error)
      throw error
    }
  },
}

export default api

