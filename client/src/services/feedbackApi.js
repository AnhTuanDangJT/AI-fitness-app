import api from './api'
import { ERROR_MESSAGES } from '../config/constants'

/**
 * Feedback API methods
 */
export const feedbackAPI = {
  /**
   * Submits user feedback.
   * 
   * @param {string} subject - Optional subject line
   * @param {string} message - Required feedback message
   * @returns {Promise<{type: 'SUCCESS' | 'ERROR', error?: string}>}
   */
  submit: async (subject, message) => {
    try {
      const response = await api.post('/feedback', {
        subject: subject || undefined,
        message: message.trim(),
      })
      
      if (response.data?.success) {
        return { type: 'SUCCESS' }
      }
      
      return { 
        type: 'ERROR', 
        error: response.data?.message || ERROR_MESSAGES.GENERIC
      }
    } catch (error) {
      return { 
        type: 'ERROR', 
        error: error.genericMessage || error.message || ERROR_MESSAGES.GENERIC
      }
    }
  },
}








