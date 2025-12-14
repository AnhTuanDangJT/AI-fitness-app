import React, { useState, useEffect } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import { authAPI, userAPI } from '../services/api'
import { ERROR_MESSAGES, UI_LABELS, BUTTON_TEXT, FORM_LABELS, PLACEHOLDERS, INFO_MESSAGES, PAGE_TITLES } from '../config/constants'
import './Auth.css'

function Login() {
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
          <h1>{PAGE_TITLES.LOGIN}</h1>
          <p>{INFO_MESSAGES.WELCOME_BACK}</p>
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
            <label htmlFor="usernameOrEmail">{FORM_LABELS.USERNAME_OR_EMAIL}</label>
            <input
              type="text"
              id="usernameOrEmail"
              name="usernameOrEmail"
              value={formData.usernameOrEmail}
              onChange={handleChange}
              placeholder={PLACEHOLDERS.USERNAME_OR_EMAIL}
              required
              autoComplete="username"
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">{FORM_LABELS.PASSWORD}</label>
            <input
              type="password"
              id="password"
              name="password"
              value={formData.password}
              onChange={handleChange}
              placeholder={PLACEHOLDERS.PASSWORD}
              required
              autoComplete="current-password"
            />
          </div>

          <button
            type="submit"
            className="submit-button"
            disabled={loading}
          >
            {loading ? UI_LABELS.SIGNING_IN : BUTTON_TEXT.SIGN_IN}
          </button>
        </form>

        <div className="auth-footer">
          <p>
            {INFO_MESSAGES.DONT_HAVE_ACCOUNT}{' '}
            <Link to="/signup" className="link">
              {INFO_MESSAGES.SIGN_UP_HERE}
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}

export default Login

