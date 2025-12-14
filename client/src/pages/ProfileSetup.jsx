import React, { useState, useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import api from '../services/api'
import { ERROR_MESSAGES, UI_LABELS, BUTTON_TEXT, FORM_LABELS, PLACEHOLDERS, INFO_MESSAGES, PAGE_TITLES, VALIDATION_MESSAGES } from '../config/constants'
import { ACTIVITY_LEVELS, FITNESS_GOALS, GENDER_OPTIONS } from '../config/profileFormConfig'
import './Auth.css'

function ProfileSetup() {
  const navigate = useNavigate()
  const location = useLocation()
  const [infoMessage, setInfoMessage] = useState('')
  const [formData, setFormData] = useState({
    name: '',
    weight: '',
    height: '',
    age: '',
    waist: '',
    hip: '',
    sex: 'male',
    activityLevel: 1,
    goal: 1,
  })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    // Check if redirected from incomplete profile
    if (location.state?.incompleteProfile) {
      setInfoMessage(INFO_MESSAGES.PROFILE_INCOMPLETE)
    } else if (location.state?.isNewUser) {
      setInfoMessage(INFO_MESSAGES.WELCOME_NEW_USER)
    }
  }, [location.state])

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData({
      ...formData,
      [name]: name === 'activityLevel' || name === 'goal' ? parseInt(value) : value,
    })
    setError('')
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    // Client-side validation
    if (!formData.name.trim()) {
      setError(VALIDATION_MESSAGES.NAME_REQUIRED)
      setLoading(false)
      return
    }

    if (!formData.weight || parseFloat(formData.weight) <= 0) {
      setError(VALIDATION_MESSAGES.WEIGHT_REQUIRED)
      setLoading(false)
      return
    }

    if (!formData.height || parseFloat(formData.height) <= 0) {
      setError(VALIDATION_MESSAGES.HEIGHT_REQUIRED)
      setLoading(false)
      return
    }

    if (!formData.age || parseInt(formData.age) <= 0) {
      setError(VALIDATION_MESSAGES.AGE_REQUIRED)
      setLoading(false)
      return
    }

    if (!formData.waist || parseFloat(formData.waist) <= 0) {
      setError(VALIDATION_MESSAGES.WAIST_REQUIRED)
      setLoading(false)
      return
    }

    if (!formData.hip || parseFloat(formData.hip) <= 0) {
      setError(VALIDATION_MESSAGES.HIP_REQUIRED)
      setLoading(false)
      return
    }

    try {
      const response = await api.post('/profile/save', {
        name: formData.name.trim(),
        weight: parseFloat(formData.weight),
        height: parseFloat(formData.height),
        age: parseInt(formData.age),
        waist: parseFloat(formData.waist),
        hip: parseFloat(formData.hip),
        sex: formData.sex === 'male',
        activityLevel: formData.activityLevel,
        goal: formData.goal,
      })

      if (response.data.success) {
        // Redirect to dashboard after successful save
        navigate('/dashboard')
      } else {
        setError(response.data.message || ERROR_MESSAGES.PROFILE_SAVE_FAILED)
      }
    } catch (err) {
      // Use generic error message from API interceptor
      setError(err.genericMessage || ERROR_MESSAGES.PROFILE_SAVE_FAILED)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <h1>{PAGE_TITLES.PROFILE_SETUP}</h1>
          <p>{INFO_MESSAGES.COMPLETE_PROFILE}</p>
        </div>

        {infoMessage && (
          <div className="info-message" style={{
            backgroundColor: '#e3f2fd',
            color: '#1976d2',
            padding: '12px',
            borderRadius: '4px',
            marginBottom: '16px',
            border: '1px solid #90caf9'
          }}>
            {infoMessage}
          </div>
        )}

        {error && (
          <div className="error-message">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label htmlFor="name">{FORM_LABELS.NAME}</label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              placeholder={PLACEHOLDERS.NAME}
              required
              autoComplete="name"
            />
          </div>

          <div className="form-group">
            <label htmlFor="weight">{FORM_LABELS.WEIGHT}</label>
            <input
              type="number"
              id="weight"
              name="weight"
              value={formData.weight}
              onChange={handleChange}
              placeholder={PLACEHOLDERS.WEIGHT}
              step="0.1"
              min="0"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="height">{FORM_LABELS.HEIGHT}</label>
            <input
              type="number"
              id="height"
              name="height"
              value={formData.height}
              onChange={handleChange}
              placeholder={PLACEHOLDERS.HEIGHT}
              step="0.1"
              min="0"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="age">{FORM_LABELS.AGE}</label>
            <input
              type="number"
              id="age"
              name="age"
              value={formData.age}
              onChange={handleChange}
              placeholder={PLACEHOLDERS.AGE}
              min="1"
              max="120"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="waist">{FORM_LABELS.WAIST}</label>
            <input
              type="number"
              id="waist"
              name="waist"
              value={formData.waist}
              onChange={handleChange}
              placeholder={PLACEHOLDERS.WAIST}
              step="0.1"
              min="0"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="hip">{FORM_LABELS.HIP}</label>
            <input
              type="number"
              id="hip"
              name="hip"
              value={formData.hip}
              onChange={handleChange}
              placeholder={PLACEHOLDERS.HIP}
              step="0.1"
              min="0"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="sex">{FORM_LABELS.GENDER}</label>
            <select
              id="sex"
              name="sex"
              value={formData.sex}
              onChange={handleChange}
              required
              className="select-input"
            >
              {GENDER_OPTIONS.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="activityLevel">{FORM_LABELS.ACTIVITY_LEVEL}</label>
            <select
              id="activityLevel"
              name="activityLevel"
              value={formData.activityLevel}
              onChange={handleChange}
              required
              className="select-input"
            >
              {ACTIVITY_LEVELS.map((level) => (
                <option key={level.value} value={level.value}>
                  {level.label}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="goal">{FORM_LABELS.FITNESS_GOAL}</label>
            <select
              id="goal"
              name="goal"
              value={formData.goal}
              onChange={handleChange}
              required
              className="select-input"
            >
              {FITNESS_GOALS.map((goal) => (
                <option key={goal.value} value={goal.value}>
                  {goal.label}
                </option>
              ))}
            </select>
          </div>

          <button
            type="submit"
            className="submit-button"
            disabled={loading}
          >
            {loading ? UI_LABELS.SAVING_PROFILE : BUTTON_TEXT.SAVE}
          </button>
        </form>

        <div className="auth-footer">
          <p>
            {INFO_MESSAGES.ALREADY_HAVE_PROFILE}{' '}
            <a href="/app/profile" className="link">
              {BUTTON_TEXT.VIEW_PROFILE}
            </a>
          </p>
        </div>
      </div>
    </div>
  )
}

export default ProfileSetup

