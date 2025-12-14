import React, { useState, useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { authAPI } from '../services/api'
import { ERROR_MESSAGES, SUCCESS_MESSAGES, UI_LABELS, BUTTON_TEXT, FORM_LABELS, PLACEHOLDERS, INFO_MESSAGES, PAGE_TITLES } from '../config/constants'
import './Auth.css'

function VerifyEmail() {
  const [code, setCode] = useState('')
  const [email, setEmail] = useState('')
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)
  const [resendLoading, setResendLoading] = useState(false)
  const [resendCooldown, setResendCooldown] = useState(0)
  const navigate = useNavigate()
  const location = useLocation()

  useEffect(() => {
    // Get email from location state (passed from signup)
    if (location.state?.email) {
      setEmail(location.state.email)
    } else {
      // If no email in state, redirect to signup
      navigate('/signup')
    }
  }, [location.state, navigate])

  useEffect(() => {
    // Countdown timer for resend cooldown
    if (resendCooldown > 0) {
      const timer = setTimeout(() => {
        setResendCooldown(resendCooldown - 1)
      }, 1000)
      return () => clearTimeout(timer)
    }
  }, [resendCooldown])

  const handleCodeChange = (e) => {
    const value = e.target.value.replace(/\D/g, '') // Only allow digits
    if (value.length <= 6) {
      setCode(value)
      setError('')
    }
  }

  const handleVerify = async (e) => {
    e.preventDefault()
    setError('')
    setSuccess('')
    
    if (code.length !== 6) {
      setError('Please enter a 6-digit verification code')
      return
    }

    if (!email) {
      setError('Email is required')
      return
    }

    setLoading(true)

    try {
      const response = await authAPI.verifyEmail(email, code)

      if (response.success) {
        setSuccess('Email verified successfully! Redirecting to login...')
        // Redirect to login after a brief delay
        setTimeout(() => {
          navigate('/login', { state: { message: 'Email verified successfully! Please log in to continue.', email: email } })
        }, 1500)
      } else {
        setError(response.message || ERROR_MESSAGES.VERIFICATION_FAILED || 'Invalid or expired verification code')
      }
    } catch (err) {
      // Log full error details for debugging
      console.error('Verify Email Error Details:', {
        message: err.message,
        response: err.response,
        responseStatus: err.response?.status,
        responseData: err.response?.data,
        responseHeaders: err.response?.headers,
        stack: err.stack,
        fullError: err
      })
      
      // Map error status codes to user-friendly messages
      const status = err.response?.status
      const backendMessage = err.response?.data?.message
      
      let errorMessage = ERROR_MESSAGES.VERIFICATION_FAILED || 'Invalid or expired verification code'
      
      if (status === 400) {
        // Invalid code format or wrong code
        errorMessage = backendMessage || 'Invalid verification code. Please check and try again.'
      } else if (status === 404) {
        // User not found
        errorMessage = backendMessage || 'User not found. Please sign up again.'
      } else if (status === 410) {
        // Code expired
        errorMessage = backendMessage || 'Verification code has expired. Please request a new code.'
      } else if (status === 429) {
        // Too many attempts
        errorMessage = backendMessage || 'Too many verification attempts. Please request a new code.'
      } else if (status === 503) {
        // Email service not configured
        errorMessage = backendMessage || 'Email service is temporarily unavailable. Please try again later.'
      } else if (status === 500) {
        // Server error
        errorMessage = 'Server error occurred. Please try again later.'
      } else if (backendMessage) {
        // Use backend message if available
        errorMessage = backendMessage
      } else {
        // Fallback to generic message
        errorMessage = err.genericMessage || ERROR_MESSAGES.VERIFICATION_FAILED || 'Invalid or expired verification code'
      }
      
      setError(errorMessage)
    } finally {
      setLoading(false)
    }
  }

  const handleResend = async () => {
    if (resendCooldown > 0 || !email) {
      return
    }

    setResendLoading(true)
    setError('')
    setSuccess('')

    try {
      const response = await authAPI.resendVerification(email)

      if (response.success) {
        setSuccess('Verification code sent successfully!')
        setResendCooldown(60) // 60 second cooldown
      } else {
        setError(response.message || ERROR_MESSAGES.RESEND_FAILED || 'Failed to resend verification code')
      }
    } catch (err) {
      // Log full error details for debugging
      console.error('Resend Verification Error Details:', {
        message: err.message,
        response: err.response,
        responseStatus: err.response?.status,
        responseData: err.response?.data,
        responseHeaders: err.response?.headers,
        stack: err.stack,
        fullError: err
      })
      
      // Map error status codes to user-friendly messages
      const status = err.response?.status
      const backendMessage = err.response?.data?.message
      
      let errorMessage = ERROR_MESSAGES.RESEND_FAILED || 'Failed to resend verification code'
      
      if (status === 404) {
        // User not found
        errorMessage = backendMessage || 'User not found. Please sign up again.'
      } else if (status === 503) {
        // Email service not configured
        errorMessage = backendMessage || 'Email service is temporarily unavailable. Please try again later.'
      } else if (status === 500) {
        // Server error
        errorMessage = 'Server error occurred. Please try again later.'
      } else if (backendMessage) {
        // Use backend message if available
        errorMessage = backendMessage
      } else {
        // Fallback to generic message
        errorMessage = err.genericMessage || ERROR_MESSAGES.RESEND_FAILED || 'Failed to resend verification code'
      }
      
      setError(errorMessage)
    } finally {
      setResendLoading(false)
    }
  }

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <h1>{PAGE_TITLES.VERIFY_EMAIL || 'Verify Your Email'}</h1>
          <p>We've sent a 6-digit verification code to <strong>{email}</strong></p>
        </div>

        {error && (
          <div className="error-message">
            {error}
          </div>
        )}

        {success && (
          <div className="success-message" style={{
            backgroundColor: '#d4edda',
            color: '#155724',
            padding: '12px',
            borderRadius: '4px',
            marginBottom: '16px',
            border: '1px solid #c3e6cb'
          }}>
            {success}
          </div>
        )}

        <form onSubmit={handleVerify} className="auth-form">
          <div className="form-group">
            <label htmlFor="code">{FORM_LABELS.VERIFICATION_CODE || 'Verification Code'}</label>
            <input
              type="text"
              id="code"
              name="code"
              value={code}
              onChange={handleCodeChange}
              placeholder={PLACEHOLDERS.VERIFICATION_CODE || 'Enter 6-digit code'}
              required
              maxLength={6}
              autoComplete="off"
              style={{
                textAlign: 'center',
                fontSize: '24px',
                letterSpacing: '8px',
                fontFamily: 'monospace'
              }}
            />
          </div>

          <button
            type="submit"
            className="submit-button"
            disabled={loading || code.length !== 6}
          >
            {loading ? (UI_LABELS.VERIFYING || 'Verifying...') : (BUTTON_TEXT.VERIFY || 'Verify')}
          </button>
        </form>

        <div className="auth-footer" style={{ textAlign: 'center' }}>
          <p style={{ marginBottom: '12px' }}>
            {INFO_MESSAGES.DIDNT_RECEIVE_CODE || "Didn't receive the code?"}
          </p>
          <button
            type="button"
            onClick={handleResend}
            disabled={resendLoading || resendCooldown > 0}
            style={{
              background: 'none',
              border: 'none',
              color: '#007bff',
              cursor: resendCooldown > 0 || resendLoading ? 'not-allowed' : 'pointer',
              textDecoration: 'underline',
              fontSize: '14px',
              opacity: resendCooldown > 0 || resendLoading ? 0.5 : 1
            }}
          >
            {resendLoading
              ? 'Sending...'
              : resendCooldown > 0
              ? `Resend code (${resendCooldown}s)`
              : 'Resend code'}
          </button>
        </div>
      </div>
    </div>
  )
}

export default VerifyEmail

