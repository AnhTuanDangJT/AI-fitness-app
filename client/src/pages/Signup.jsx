import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authAPI } from '../services/api'
import { ERROR_MESSAGES, SUCCESS_MESSAGES, UI_LABELS, BUTTON_TEXT, FORM_LABELS, PLACEHOLDERS, INFO_MESSAGES, PAGE_TITLES, VALIDATION_MESSAGES } from '../config/constants'
import './Auth.css'

function Signup() {
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
      setError(VALIDATION_MESSAGES.PASSWORD_MISMATCH)
      setLoading(false)
      return
    }

    if (formData.password.length < 6) {
      setError(VALIDATION_MESSAGES.PASSWORD_MIN_LENGTH)
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
        // Redirect to email verification page
        navigate('/verify-email', { state: { email: formData.email } })
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
          <h1>{PAGE_TITLES.SIGNUP}</h1>
          <p>{INFO_MESSAGES.CREATE_ACCOUNT}</p>
        </div>

        {error && (
          <div className="error-message">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label htmlFor="username">{FORM_LABELS.USERNAME}</label>
            <input
              type="text"
              id="username"
              name="username"
              value={formData.username}
              onChange={handleChange}
              placeholder={PLACEHOLDERS.USERNAME}
              required
              minLength={3}
              maxLength={50}
              autoComplete="username"
            />
          </div>

          <div className="form-group">
            <label htmlFor="email">{FORM_LABELS.EMAIL}</label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder={PLACEHOLDERS.EMAIL}
              required
              autoComplete="email"
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
              minLength={6}
              autoComplete="new-password"
            />
          </div>

          <div className="form-group">
            <label htmlFor="confirmPassword">{FORM_LABELS.CONFIRM_PASSWORD}</label>
            <input
              type="password"
              id="confirmPassword"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleChange}
              placeholder={PLACEHOLDERS.CONFIRM_PASSWORD}
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
            {loading ? UI_LABELS.CREATING_ACCOUNT : BUTTON_TEXT.SIGN_UP}
          </button>
        </form>

        <div className="auth-footer">
          <p>
            {INFO_MESSAGES.ALREADY_HAVE_ACCOUNT}{' '}
            <Link to="/login" className="link">
              {INFO_MESSAGES.SIGN_IN_HERE}
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}

export default Signup

