import axios from 'axios'

// Use environment variable for API base URL, fallback to localhost for development
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

// Create axios instance
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
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

// Handle token expiration and errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Token expired or invalid
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    } else if (error.code === 'ERR_NETWORK' || error.message.includes('Network Error')) {
      // Network error - backend might be down or CORS issue
      console.error('Network Error: Unable to connect to backend. Check:', {
        apiUrl: API_BASE_URL,
        error: error.message,
        suggestion: 'Verify backend is running and CORS is configured correctly'
      })
    } else if (error.response?.status === 0) {
      // CORS error or blocked request
      console.error('CORS Error: Request blocked. Check CORS configuration:', {
        apiUrl: API_BASE_URL,
        suggestion: 'Verify CORS_ALLOWED_ORIGINS includes frontend URL'
      })
    }
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
}

export default api

