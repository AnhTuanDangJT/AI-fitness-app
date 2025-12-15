import React, { useState, useEffect } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { authAPI, userAPI } from '../services/api'
import { ERROR_MESSAGES } from '../config/constants'
import './Auth.css'

function Login() {
  const { t } = useTranslation()
  const [formData, setFormData] = useState({
    usernameOrEmail: '',
    password: '',
  })
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()

  useEffect(() => {
    // Pre-fill email if provided in location state
    if (location.state?.email) {
      setFormData(prev => ({
        ...prev,
        usernameOrEmail: location.state.email
      }))
    }
    // Show success message if provided
    if (location.state?.message) {
      setSuccess(location.state.message)
    }
  }, [location.state])

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

    try {
      const response = await authAPI.login(
        formData.usernameOrEmail,
        formData.password
      )

      if (response.success) {
        // Store token in localStorage
        localStorage.setItem('token', response.data.token)
        localStorage.setItem('user', JSON.stringify({
          id: response.data.userId,
          username: response.data.username,
          email: response.data.email,
        }))

        // Show dismissible banner if email verification was skipped
        const isEmailVerified = response.data?.isEmailVerified
        if (isEmailVerified === false) {
          // Note: This case shouldn't happen in relaxed mode, but handle it gracefully
          console.warn('User logged in but email is not verified (may be in relaxed verification mode)')
        }

        // Check if profile is complete and redirect accordingly
        try {
          const profileCheck = await userAPI.checkProfileComplete()
          if (profileCheck.success && profileCheck.data?.isComplete) {
            // Profile is complete, go to dashboard
            navigate('/dashboard')
          } else {
            // Profile is incomplete, redirect to profile setup
            navigate('/profile-setup', { state: { incompleteProfile: true } })
          }
        } catch (err) {
          // If check fails, assume incomplete and redirect to profile setup
          console.error('Error checking profile completeness:', err)
          navigate('/profile-setup', { state: { incompleteProfile: true } })
        }
      } else {
        setError(response.message || ERROR_MESSAGES.LOGIN_FAILED)
      }
    } catch (err) {
      // Use generic error message from API interceptor
      setError(err.genericMessage || ERROR_MESSAGES.LOGIN_FAILED)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <h1>{t('auth.login')}</h1>
          <p>{t('auth.welcomeBack')}</p>
        </div>

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

        {error && (
          <div className="error-message">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label htmlFor="usernameOrEmail">{t('auth.usernameOrEmail')}</label>
            <input
              type="text"
              id="usernameOrEmail"
              name="usernameOrEmail"
              value={formData.usernameOrEmail}
              onChange={handleChange}
              placeholder={t('placeholders.usernameOrEmail')}
              required
              autoComplete="username"
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
              autoComplete="current-password"
            />
          </div>

          <button
            type="submit"
            className="submit-button"
            disabled={loading}
          >
            {loading ? t('auth.signingIn') : t('auth.signIn')}
          </button>
        </form>

        <div className="auth-footer">
          <p>
            {t('auth.dontHaveAccount')}{' '}
            <Link to="/signup" className="link">
              {t('auth.signUpHere')}
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}

export default Login

