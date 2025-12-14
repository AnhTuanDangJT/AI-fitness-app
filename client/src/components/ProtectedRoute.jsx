import React, { useEffect, useState } from 'react'
import { Navigate } from 'react-router-dom'
import api from '../services/api'

/**
 * ProtectedRoute Component
 * 
 * Wraps routes that require authentication.
 * Checks if user has a valid token before allowing access.
 * Redirects to /login if not authenticated.
 */
function ProtectedRoute({ children }) {
  const [isAuthenticated, setIsAuthenticated] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const checkAuthentication = async () => {
      const token = localStorage.getItem('token')
      
      // If no token, user is not authenticated
      if (!token) {
        setIsAuthenticated(false)
        setLoading(false)
        return
      }

      // Optionally validate token with backend
      // For now, we'll just check if token exists
      // In production, you might want to verify token validity with backend
      try {
        // Check if token is present and not expired (basic check)
        // You can add token validation here if needed
        setIsAuthenticated(true)
      } catch (error) {
        // Token is invalid
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        setIsAuthenticated(false)
      } finally {
        setLoading(false)
      }
    }

    checkAuthentication()
  }, [])

  // Show loading state while checking authentication
  if (loading) {
    return (
      <div style={{
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center',
        height: '100vh',
        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
        color: 'white'
      }}>
        <div style={{ textAlign: 'center' }}>
          <div style={{
            width: '40px',
            height: '40px',
            border: '4px solid rgba(255, 255, 255, 0.3)',
            borderTop: '4px solid white',
            borderRadius: '50%',
            animation: 'spin 1s linear infinite',
            margin: '0 auto 20px'
          }}></div>
          <p>Loading...</p>
        </div>
      </div>
    )
  }

  // Redirect to login if not authenticated
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />
  }

  // Render protected content if authenticated
  return children
}

// Add spin animation
const style = document.createElement('style')
style.textContent = `
  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }
`
if (!document.head.querySelector('style[data-spin]')) {
  style.setAttribute('data-spin', 'true')
  document.head.appendChild(style)
}

export default ProtectedRoute

