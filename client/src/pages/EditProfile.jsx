import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import api from '../services/api'
import { invalidateGamificationCache } from '../services/gamificationApi'
import { FORM_LABELS, FORM_PLACEHOLDERS, ACTIVITY_LEVELS, FITNESS_GOALS, GENDER_OPTIONS, DIETARY_PREFERENCES } from '../config/profileFormConfig'
import { ERROR_MESSAGES, UI_LABELS, BUTTON_TEXT, STATUS_MESSAGES, PAGE_TITLES, VALIDATION_MESSAGES, INFO_MESSAGES } from '../config/constants'
import './EditProfile.css'

function EditProfile() {
  const navigate = useNavigate()
  const { t } = useTranslation()
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    age: '',
    weight: '',
    height: '',
    waist: '',
    hip: '',
    sex: 'male',
    activityLevel: '',
    goal: '',
    dietaryPreference: '',
    dislikedFoods: '',
    maxBudgetPerDay: '',
    maxCookingTimePerMeal: '',
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [currentProfile, setCurrentProfile] = useState(null)
  const [loadingProfile, setLoadingProfile] = useState(true)

  useEffect(() => {
    fetchCurrentProfile()
  }, [])

  const fetchCurrentProfile = async () => {
    try {
      setLoadingProfile(true)
      setError('')
      const response = await api.get('/user/profile')
      if (response.data.success) {
        const profile = response.data.data
        setCurrentProfile(profile)
        // Pre-fill form with current values
        setFormData({
          name: profile.name || '',
          email: profile.email || '',
          age: profile.age || '',
          weight: profile.weight || '',
          height: profile.height || '',
          waist: profile.waist || '',
          hip: profile.hip || '',
          sex: profile.sex === 'Male' ? 'male' : 'female',
          activityLevel: profile.activityLevel || '',
          goal: profile.calorieGoal || '',
          dietaryPreference: profile.dietaryPreference || '',
          dislikedFoods: profile.dislikedFoods || '',
          maxBudgetPerDay: profile.maxBudgetPerDay || '',
          maxCookingTimePerMeal: profile.maxCookingTimePerMeal || '',
        })
      } else {
        setError(response.data.message || ERROR_MESSAGES.PROFILE_LOAD_FAILED)
      }
    } catch (err) {
      setError(err.genericMessage || ERROR_MESSAGES.PROFILE_LOAD_FAILED)
      console.error('Error fetching profile:', err)
    } finally {
      setLoadingProfile(false)
    }
  }

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData({
      ...formData,
      [name]: name === 'activityLevel' || name === 'goal' || name === 'age' || name === 'maxBudgetPerDay' || name === 'maxCookingTimePerMeal' 
        ? (value === '' ? '' : parseInt(value)) 
        : value,
    })
    setError('')
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    // Client-side validation
    if (!formData.name || !formData.name.trim()) {
      setError(VALIDATION_MESSAGES.NAME_REQUIRED)
      setLoading(false)
      return
    }

    if (!formData.age || parseInt(formData.age) <= 0 || parseInt(formData.age) > 120) {
      setError(VALIDATION_MESSAGES.AGE_REQUIRED)
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

    if (!formData.activityLevel || formData.activityLevel < 1 || formData.activityLevel > 5) {
      setError(VALIDATION_MESSAGES.ACTIVITY_LEVEL_REQUIRED)
      setLoading(false)
      return
    }

    if (!formData.goal || formData.goal < 1 || formData.goal > 4) {
      setError(VALIDATION_MESSAGES.GOAL_REQUIRED)
      setLoading(false)
      return
    }

    try {
      // Build update request with all fields (since all are editable except email)
      const updateData = {
        name: formData.name.trim(),
        age: parseInt(formData.age),
        weight: parseFloat(formData.weight),
        height: parseFloat(formData.height),
        waist: parseFloat(formData.waist),
        hip: parseFloat(formData.hip),
        sex: formData.sex === 'male',
        activityLevel: formData.activityLevel,
        goal: formData.goal,
      }
      
      // Add food preferences if provided (optional fields)
      if (formData.dietaryPreference) {
        updateData.dietaryPreference = formData.dietaryPreference
      }
      if (formData.dislikedFoods) {
        updateData.dislikedFoods = formData.dislikedFoods.trim()
      }
      if (formData.maxBudgetPerDay) {
        updateData.maxBudgetPerDay = parseInt(formData.maxBudgetPerDay)
      }
      if (formData.maxCookingTimePerMeal) {
        updateData.maxCookingTimePerMeal = parseInt(formData.maxCookingTimePerMeal)
      }

      const response = await api.put('/profile/update', updateData)

      if (response.data.success) {
        // Invalidate gamification cache after successful profile update
        // This ensures XP updates are reflected in the UI
        invalidateGamificationCache()
        
        // Redirect to dashboard after successful update
        // This will trigger a recalculation and update the dashboard
        navigate('/dashboard')
      } else {
        setError(response.data.message || ERROR_MESSAGES.PROFILE_UPDATE_FAILED)
      }
    } catch (err) {
      setError(err.genericMessage || ERROR_MESSAGES.PROFILE_UPDATE_FAILED)
      console.error('Error updating profile:', err)
    } finally {
      setLoading(false)
    }
  }

  const renderBackButton = () => (
    <div className="page-back-row">
      <button
        type="button"
        className="page-back-button"
        onClick={() => navigate('/dashboard')}
      >
        <span>←</span>
        {t('common.backToDashboard')}
      </button>
    </div>
  )

  if (loadingProfile) {
    return (
      <div className="edit-profile-page">
        {renderBackButton()}
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>{STATUS_MESSAGES.LOADING_PROFILE}</p>
        </div>
      </div>
    )
  }

  if (!currentProfile) {
    return (
      <div className="edit-profile-page">
        {renderBackButton()}
        <div className="error-container">
          <h2>{STATUS_MESSAGES.PROFILE_NOT_FOUND}</h2>
          <p>{STATUS_MESSAGES.PROFILE_SETUP_REQUIRED}</p>
          <button onClick={() => navigate('/profile-setup')} className="setup-button">
            {BUTTON_TEXT.COMPLETE_PROFILE_SETUP}
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="edit-profile-page">
      {renderBackButton()}
      <div className="edit-profile-container">
        <div className="edit-profile-header">
          <h1>{PAGE_TITLES.EDIT_PROFILE}</h1>
          <p>Update your measurements and goals. All calculations will be automatically updated.</p>
        </div>

        {error && (
          <div className="error-message">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="edit-profile-form">
          {/* Name */}
          <div className="form-group">
            <label htmlFor="name">{FORM_LABELS.name}</label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              placeholder={FORM_PLACEHOLDERS.name}
              required
              autoComplete="name"
            />
          </div>

          {/* Email (read-only) */}
          <div className="form-group">
            <label htmlFor="email">{FORM_LABELS.email}</label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              readOnly
              disabled
              className="read-only-input"
              placeholder={FORM_PLACEHOLDERS.email}
            />
            <span className="read-only-hint">{INFO_MESSAGES.EMAIL_READ_ONLY}</span>
          </div>

          {/* Gender */}
          <div className="form-group">
            <label htmlFor="sex">{FORM_LABELS.gender}</label>
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

          {/* Age */}
          <div className="form-group">
            <label htmlFor="age">{FORM_LABELS.age}</label>
            <input
              type="number"
              id="age"
              name="age"
              value={formData.age}
              onChange={handleChange}
              placeholder={FORM_PLACEHOLDERS.age}
              min="1"
              max="120"
              required
            />
          </div>

          {/* Weight */}
          <div className="form-group">
            <label htmlFor="weight">{FORM_LABELS.weight}</label>
            <input
              type="number"
              id="weight"
              name="weight"
              value={formData.weight}
              onChange={handleChange}
              placeholder={FORM_PLACEHOLDERS.weight}
              step="0.1"
              min="0"
              required
            />
          </div>

          {/* Height */}
          <div className="form-group">
            <label htmlFor="height">{FORM_LABELS.height}</label>
            <input
              type="number"
              id="height"
              name="height"
              value={formData.height}
              onChange={handleChange}
              placeholder={FORM_PLACEHOLDERS.height}
              step="0.1"
              min="0"
              required
            />
          </div>

          {/* Waist */}
          <div className="form-group">
            <label htmlFor="waist">{FORM_LABELS.waist}</label>
            <input
              type="number"
              id="waist"
              name="waist"
              value={formData.waist}
              onChange={handleChange}
              placeholder={FORM_PLACEHOLDERS.waist}
              step="0.1"
              min="0"
              required
            />
          </div>

          {/* Hip */}
          <div className="form-group">
            <label htmlFor="hip">{FORM_LABELS.hip}</label>
            <input
              type="number"
              id="hip"
              name="hip"
              value={formData.hip}
              onChange={handleChange}
              placeholder={FORM_PLACEHOLDERS.hip}
              step="0.1"
              min="0"
              required
            />
          </div>

          {/* Activity Level */}
          <div className="form-group">
            <label htmlFor="activityLevel">{FORM_LABELS.activityLevel}</label>
            <select
              id="activityLevel"
              name="activityLevel"
              value={formData.activityLevel}
              onChange={handleChange}
              required
              className="select-input"
            >
              <option value="">{FORM_PLACEHOLDERS.activityLevel}</option>
              {ACTIVITY_LEVELS.map((level) => (
                <option key={level.value} value={level.value}>
                  {level.label}
                </option>
              ))}
            </select>
          </div>

          {/* Fitness Goal */}
          <div className="form-group">
            <label htmlFor="goal">{FORM_LABELS.goal}</label>
            <select
              id="goal"
              name="goal"
              value={formData.goal}
              onChange={handleChange}
              required
              className="select-input"
            >
              <option value="">{FORM_PLACEHOLDERS.goal}</option>
              {FITNESS_GOALS.map((goal) => (
                <option key={goal.value} value={goal.value}>
                  {goal.label}
                </option>
              ))}
            </select>
          </div>

          {/* Dietary Preference */}
          <div className="form-group">
            <label htmlFor="dietaryPreference">{FORM_LABELS.dietaryPreference}</label>
            <select
              id="dietaryPreference"
              name="dietaryPreference"
              value={formData.dietaryPreference}
              onChange={handleChange}
              className="select-input"
            >
              <option value="">{FORM_PLACEHOLDERS.dietaryPreference}</option>
              {DIETARY_PREFERENCES.map((pref) => (
                <option key={pref.value} value={pref.value}>
                  {pref.label}
                </option>
              ))}
            </select>
          </div>

          {/* Disliked Foods */}
          <div className="form-group">
            <label htmlFor="dislikedFoods">{FORM_LABELS.dislikedFoods}</label>
            <textarea
              id="dislikedFoods"
              name="dislikedFoods"
              value={formData.dislikedFoods}
              onChange={handleChange}
              placeholder={FORM_PLACEHOLDERS.dislikedFoods}
              rows="3"
              className="textarea-input"
            />
          </div>

          {/* Max Budget Per Day */}
          <div className="form-group">
            <label htmlFor="maxBudgetPerDay">{FORM_LABELS.maxBudgetPerDay}</label>
            <input
              type="number"
              id="maxBudgetPerDay"
              name="maxBudgetPerDay"
              value={formData.maxBudgetPerDay}
              onChange={handleChange}
              placeholder={FORM_PLACEHOLDERS.maxBudgetPerDay}
              min="0"
              step="1"
            />
          </div>

          {/* Max Cooking Time Per Meal */}
          <div className="form-group">
            <label htmlFor="maxCookingTimePerMeal">{FORM_LABELS.maxCookingTimePerMeal}</label>
            <input
              type="number"
              id="maxCookingTimePerMeal"
              name="maxCookingTimePerMeal"
              value={formData.maxCookingTimePerMeal}
              onChange={handleChange}
              placeholder={FORM_PLACEHOLDERS.maxCookingTimePerMeal}
              min="1"
              step="1"
            />
          </div>

          {/* Form Actions */}
          <div className="form-actions">
            <button
              type="button"
              onClick={() => navigate('/dashboard')}
              className="cancel-button"
              disabled={loading}
            >
              {BUTTON_TEXT.CANCEL}
            </button>
            <button
              type="submit"
              className="save-button"
              disabled={loading}
            >
              {loading ? UI_LABELS.SAVING : BUTTON_TEXT.SAVE_CHANGES}
            </button>
          </div>
        </form>

        <div className="info-note">
          <p>{INFO_MESSAGES.PROFILE_AUTO_RECALCULATE}</p>
        </div>
      </div>
    </div>
  )
}

export default EditProfile

