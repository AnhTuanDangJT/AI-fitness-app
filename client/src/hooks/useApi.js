import { useState, useCallback } from 'react'
import api from '../services/api'
import { ERROR_MESSAGES } from '../config/constants'

/**
 * Custom hook for API calls with built-in error handling
 * 
 * This hook provides:
 * - Loading state management
 * - Error state management with generic error messages
 * - Automatic error handling
 * 
 * @returns {Object} { loading, error, execute }
 */
export const useApi = () => {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  /**
   * Execute an API call with automatic error handling
   * 
   * @param {Function} apiCall - The API function to execute (e.g., () => api.get('/endpoint'))
   * @param {Object} options - Optional configuration
   * @param {Function} options.onSuccess - Callback on success
   * @param {Function} options.onError - Callback on error (receives generic error message)
   * @param {boolean} options.showGenericError - Whether to set error state (default: true)
   * @returns {Promise} The API response or null if error
   */
  const execute = useCallback(async (apiCall, options = {}) => {
    const { onSuccess, onError, showGenericError = true } = options

    setLoading(true)
    setError(null)

    try {
      const response = await apiCall()
      
      if (onSuccess) {
        onSuccess(response)
      }
      
      return response
    } catch (err) {
      // Use generic error message from interceptor, or fallback
      const errorMessage = err.genericMessage || ERROR_MESSAGES.GENERIC
      
      if (showGenericError) {
        setError(errorMessage)
      }
      
      if (onError) {
        onError(errorMessage, err)
      }
      
      return null
    } finally {
      setLoading(false)
    }
  }, [])

  /**
   * Clear the error state
   */
  const clearError = useCallback(() => {
    setError(null)
  }, [])

  return { loading, error, execute, clearError }
}

export default useApi
















