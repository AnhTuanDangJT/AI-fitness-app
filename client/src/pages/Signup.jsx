import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { authAPI } from '../services/api'
import { ERROR_MESSAGES } from '../config/constants'
import './Auth.css'

function Signup() {
  const { t } = useTranslation()
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    })
    setError('') // Clear error on input change
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    // Client-side validation
    if (formData.password !== formData.confirmPassword) {
      setError(t('validation.passwordMismatch'))
      setLoading(false)
      return
    }

    if (formData.password.length < 6) {
      setError(t('validation.passwordMinLength'))
      setLoading(false)
      return
    }

    try {
      const response = await authAPI.signup(
        formData.username,
        formData.email,
        formData.password
      )

      if (response.success) {
        // Check if email is already verified (e.g., verification was skipped due to email service failure)
        const isEmailVerified = response.data?.isEmailVerified || false
        if (isEmailVerified) {
          // Email already verified, redirect to login
          navigate('/login', { 
            state: { 
              email: formData.email,
              message: 'Account created successfully! You can now log in.'
            } 
          })
        } else {
          // Redirect to email verification page
          navigate('/verify-email', { state: { email: formData.email } })
        }
      } else {
        setError(response.message || ERROR_MESSAGES.SIGNUP_FAILED)
      }
    } catch (err) {
      // TEMPORARILY: Print actual error message to console for debugging
      console.error('Signup Error Details:', {
        message: err.message,
        response: err.response,
        responseData: err.response?.data,
        responseStatus: err.response?.status,
        responseHeaders: err.response?.headers,
        stack: err.stack,
        fullError: err
      })
      
      // Handle validation errors from backend
      if (err.response?.data?.data) {
        const validationErrors = Object.values(err.response.data.data).join(', ')
        setError(validationErrors)
      } else if (err.response?.data?.message) {
        // Show actual backend error message temporarily
        setError(err.response.data.message)
      } else {
        // Use generic error message from API interceptor
        setError(err.genericMessage || ERROR_MESSAGES.SIGNUP_FAILED)
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <h1>{t('auth.signup')}</h1>
          <p>{t('auth.createAccountMessage')}</p>
        </div>

        {error && (
          <div className="error-message">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label htmlFor="username">{t('auth.username')}</label>
            <input
              type="text"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleChange}
              placeholder={t('placeholders.username')}
              required
              minLength={3}
              maxLength={50}
              autoComplete="username"
            />
          </div>

          <div className="form-group">
            <label htmlFor="email">{t('auth.email')}</label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder={t('placeholders.email')}
              required
              autoComplete="email"
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">{t('auth.password')}</label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder={t('placeholders.password')}
              required
              minLength={6}
              autoComplete="new-password"
            />
          </div>

          <div className="form-group">
            <label htmlFor="confirmPassword">{t('auth.confirmPassword')}</label>
            <input
              type="password"
              id="confirmPassword"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleChange}
              placeholder={t('placeholders.confirmPassword')}
              required
              minLength={6}
              autoComplete="new-password"
            />
          </div>

          <button
            type="submit"
            className="submit-button"
            disabled={loading}
          >
            {loading ? t('auth.creatingAccount') : t('auth.signUp')}
          </button>
        </form>

        <div className="auth-footer">
          <p>
            {t('auth.alreadyHaveAccount')}{' '}
            <Link to="/login" className="link">
              {t('auth.signInHere')}
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}

export default Signup

