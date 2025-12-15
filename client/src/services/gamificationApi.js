import api from './api'

// In-memory cache for gamification status
let cachedStatus = null
let hasWarned403 = false // Track if we've already warned about 403

/**
 * Gamification API Client
 * 
 * Handles all gamification-related API calls.
 * Implements in-memory caching to avoid unnecessary API calls.
 */
export const gamificationAPI = {
  /**
   * Get gamification status for the authenticated user.
   * 
   * Returns: { xp, currentStreakDays, longestStreakDays, badges[] }
   * 
   * Uses in-memory cache to avoid duplicate calls within the same session.
   * Cache is cleared when explicitly requested via invalidateGamificationCache().
   * 
   * For 401/403 errors, returns safe defaults and caches them to prevent repeated requests.
   */
  getGamificationStatus: async (forceRefresh = false) => {
    if (cachedStatus && !forceRefresh) {
      return { type: 'SUCCESS', data: cachedStatus }
    }

    // Check for token before making request
    const token = localStorage.getItem('token')
    if (!token) {
      // No token - return safe defaults immediately
      const safeDefaults = {
        xp: 0,
        currentStreakDays: 0,
        longestStreakDays: 0,
        badges: []
      }
      if (!cachedStatus) {
        cachedStatus = safeDefaults
      }
      return { type: 'SUCCESS', data: safeDefaults }
    }

    try {
      const response = await api.get('/gamification/status')
      
      if (response.data?.success && response.data?.data) {
        // Cache the response
        cachedStatus = response.data.data
        
        return { type: 'SUCCESS', data: response.data.data }
      }
      
      // Unexpected response format
      return { type: 'ERROR', error: 'Unexpected response format' }
      
    } catch (error) {
      const status = error.response?.status
      
      // Handle 401/403 (unauthorized/forbidden) with safe defaults
      if (status === 401 || status === 403) {
        // Cache safe defaults to prevent repeated requests
        const safeDefaults = {
          xp: 0,
          currentStreakDays: 0,
          longestStreakDays: 0,
          badges: []
        }
        
        // Only cache if we don't already have cached data (to avoid overwriting valid data)
        if (!cachedStatus) {
          cachedStatus = safeDefaults
        }
        
        // Warn only once to prevent console spam
        if (!hasWarned403) {
          hasWarned403 = true
          console.warn('Gamification API returned 401/403, using safe defaults')
        }
        
        // Return safe defaults - do NOT retry
        return { type: 'SUCCESS', data: safeDefaults }
      }
      
      // Handle other errors gracefully
      const errorMessage = error.genericMessage || error.message || 'Failed to load gamification status'
      
      // If we have cached data, return it even on error
      if (cachedStatus) {
        console.warn('Gamification API error, using cached data:', errorMessage)
        return { type: 'SUCCESS', data: cachedStatus }
      }
      
      // For other errors, return safe defaults instead of error
      const safeDefaults = {
        xp: 0,
        currentStreakDays: 0,
        longestStreakDays: 0,
        badges: []
      }
      
      return { type: 'SUCCESS', data: safeDefaults }
    }
  },

  /**
   * Clear the in-memory cache.
   * Useful when you know the status has changed (e.g., after an action).
   */
  clearCache: () => {
    cachedStatus = null
  },

  /**
   * Record daily challenge completion.
   * 
   * IDEMPOTENT: Can only be completed once per calendar day (UTC).
   * Backend handles idempotency - calling multiple times is safe.
   * 
   * Returns: { type: 'SUCCESS' | 'ERROR', data?: {...}, error?: string }
   */
  recordDailyChallenge: async () => {
    try {
      const response = await api.post('/gamification/daily-challenge')
      
      if (response.data?.success) {
        // Invalidate cache to force refresh on next getGamificationStatus call
        cachedStatus = null
        
        return { type: 'SUCCESS', data: response.data.data }
      }
      
      return { type: 'ERROR', error: 'Unexpected response format' }
      
    } catch (error) {
      const errorMessage = error.genericMessage || error.message || 'Failed to record daily challenge'
      return { type: 'ERROR', error: errorMessage }
    }
  },
}

/**
 * Invalidate the gamification cache.
 * Call this after XP-granting actions to ensure fresh data is fetched.
 */
export const invalidateGamificationCache = () => {
  cachedStatus = null
}

export default gamificationAPI

