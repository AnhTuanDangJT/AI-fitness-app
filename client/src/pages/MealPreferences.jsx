import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { mealPreferencesAPI, mealPlanAPI } from '../services/api'
import './MealPreferences.css'

function MealPreferences({ onClose: onCloseProp }) {
  const navigate = useNavigate()
  const { t } = useTranslation()
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  
  const [formData, setFormData] = useState({
    preferredFoods: '',
    dislikedFoods: '',
    allergies: '',
    dietaryRestriction: '',
    cookingTimePreference: '',
    budgetPerDay: '',
    favoriteCuisines: '',
  })

  useEffect(() => {
    fetchPreferences()
  }, [])

  const fetchPreferences = async () => {
    try {
      setLoading(true)
      setError('')
      const response = await mealPreferencesAPI.get()
      if (response.success && response.data) {
        setFormData({
          preferredFoods: response.data.preferredFoods || '',
          dislikedFoods: response.data.dislikedFoods || '',
          allergies: response.data.allergies || '',
          dietaryRestriction: response.data.dietaryRestriction || '',
          cookingTimePreference: response.data.cookingTimePreference || '',
          budgetPerDay: response.data.budgetPerDay || '',
          favoriteCuisines: response.data.favoriteCuisines || '',
        })
      }
    } catch (err) {
      // If preferences don't exist yet, that's okay - form will be empty
      console.log('No existing preferences found, starting fresh')
    } finally {
      setLoading(false)
    }
  }

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: value
    }))
    // Clear success message when user starts typing
    if (success) setSuccess('')
    if (error) setError('')
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    setError('')
    setSuccess('')

    try {
      const preferences = {
        preferredFoods: formData.preferredFoods.trim() || null,
        dislikedFoods: formData.dislikedFoods.trim() || null,
        allergies: formData.allergies.trim() || null,
        dietaryRestriction: formData.dietaryRestriction.trim() || null,
        cookingTimePreference: formData.cookingTimePreference ? parseInt(formData.cookingTimePreference) : null,
        budgetPerDay: formData.budgetPerDay ? parseInt(formData.budgetPerDay) : null,
        favoriteCuisines: formData.favoriteCuisines.trim() || null,
      }

      // 1. Save preferences
      const saveResponse = await mealPreferencesAPI.save(preferences)
      
      if (!saveResponse.success) {
        setError(saveResponse.message || t('mealPreferences.saveError'))
        setSaving(false)
        return
      }

      setSuccess(t('mealPreferences.generatingMessage'))

      // 2. Generate meal plan
      // Calculate next Monday (or today's Monday)
      const today = new Date()
      const dayOfWeek = today.getDay()
      const daysUntilMonday = dayOfWeek === 0 ? 1 : (8 - dayOfWeek) % 7 || 7
      const nextMonday = new Date(today)
      nextMonday.setDate(today.getDate() + daysUntilMonday)
      const weekStart = nextMonday.toISOString().split('T')[0]

      const generateResponse = await mealPlanAPI.generate(weekStart)
      
      if (!generateResponse.success) {
        setError(generateResponse.message || t('mealPreferences.generateError'))
        setSaving(false)
        return
      }

      // 3. Success - navigate to meal plan view
      setSuccess(t('mealPreferences.generateSuccess'))
      
      // Close modal if opened from dashboard, then navigate
      if (onCloseProp) {
        onCloseProp()
      }
      
      // Navigate to meal plan page
      navigate('/meal-plan')
    } catch (err) {
      setError(err.genericMessage || t('mealPreferences.generateError'))
      console.error('Error in meal plan flow:', err)
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="meal-preferences-page">
        <div className="meal-preferences-container">
          <div className="loading-container">
            <div className="loading-spinner"></div>
            <p>{t('mealPreferences.loading')}</p>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="meal-preferences-page">
      <div className="meal-preferences-container">
        <div className="meal-preferences-header">
          <h1>{t('mealPreferences.title')}</h1>
          <p className="subtitle">{t('mealPreferences.subtitle')}</p>
        </div>

        {error && (
          <div className="error-message">
            {error}
          </div>
        )}

        {success && (
          <div className="success-message">
            {success}
          </div>
        )}

        <form onSubmit={handleSubmit} className="meal-preferences-form">
          {/* Food Preferences Section */}
          <div className="form-section">
            <h2 className="section-title">{t('mealPreferences.foodPreferences')}</h2>
            
            <div className="form-group">
              <label htmlFor="preferredFoods">
                {t('mealPreferences.preferredFoods')} <span className="optional">{t('mealPreferences.optional')}</span>
              </label>
              <textarea
                id="preferredFoods"
                name="preferredFoods"
                value={formData.preferredFoods}
                onChange={handleChange}
                placeholder={t('mealPreferences.preferredFoodsPlaceholder')}
                rows="3"
                className="glassy-input"
              />
              <small className="form-hint">{t('mealPreferences.preferredFoodsHint')}</small>
            </div>

            <div className="form-group">
              <label htmlFor="dislikedFoods">
                {t('mealPreferences.dislikedFoods')} <span className="optional">{t('mealPreferences.optional')}</span>
              </label>
              <textarea
                id="dislikedFoods"
                name="dislikedFoods"
                value={formData.dislikedFoods}
                onChange={handleChange}
                placeholder={t('mealPreferences.dislikedFoodsPlaceholder')}
                rows="3"
                className="glassy-input"
              />
              <small className="form-hint">{t('mealPreferences.dislikedFoodsHint')}</small>
            </div>

            <div className="form-group">
              <label htmlFor="allergies">
                {t('mealPreferences.allergies')} <span className="optional">{t('mealPreferences.optional')}</span>
              </label>
              <textarea
                id="allergies"
                name="allergies"
                value={formData.allergies}
                onChange={handleChange}
                placeholder={t('mealPreferences.allergiesPlaceholder')}
                rows="3"
                className="glassy-input"
              />
              <small className="form-hint">{t('mealPreferences.allergiesHint')}</small>
            </div>
          </div>

          {/* Dietary & Lifestyle Section */}
          <div className="form-section">
            <h2 className="section-title">{t('mealPreferences.dietaryLifestyle')}</h2>
            
            <div className="form-group">
              <label htmlFor="dietaryRestriction">
                {t('mealPreferences.dietaryRestriction')} <span className="optional">{t('mealPreferences.optional')}</span>
              </label>
              <select
                id="dietaryRestriction"
                name="dietaryRestriction"
                value={formData.dietaryRestriction}
                onChange={handleChange}
                className="glassy-input"
              >
                <option value="">{t('mealPreferences.dietaryRestrictionPlaceholder')}</option>
                <option value="omnivore">{t('mealPreferences.dietaryOptions.omnivore')}</option>
                <option value="vegetarian">{t('mealPreferences.dietaryOptions.vegetarian')}</option>
                <option value="vegan">{t('mealPreferences.dietaryOptions.vegan')}</option>
                <option value="pescatarian">{t('mealPreferences.dietaryOptions.pescatarian')}</option>
                <option value="halal">{t('mealPreferences.dietaryOptions.halal')}</option>
                <option value="kosher">{t('mealPreferences.dietaryOptions.kosher')}</option>
              </select>
              <small className="form-hint">{t('mealPreferences.dietaryRestrictionHint')}</small>
            </div>

            <div className="form-group">
              <label htmlFor="favoriteCuisines">
                {t('mealPreferences.favoriteCuisines')} <span className="optional">{t('mealPreferences.optional')}</span>
              </label>
              <textarea
                id="favoriteCuisines"
                name="favoriteCuisines"
                value={formData.favoriteCuisines}
                onChange={handleChange}
                placeholder={t('mealPreferences.favoriteCuisinesPlaceholder')}
                rows="3"
                className="glassy-input"
              />
              <small className="form-hint">{t('mealPreferences.favoriteCuisinesHint')}</small>
            </div>
          </div>

          {/* Meal Planning Preferences Section */}
          <div className="form-section">
            <h2 className="section-title">{t('mealPreferences.mealPlanningPreferences')}</h2>
            
            <div className="form-group">
              <label htmlFor="cookingTimePreference">
                {t('mealPreferences.cookingTimePreference')} <span className="optional">{t('mealPreferences.optional')}</span>
              </label>
              <input
                type="number"
                id="cookingTimePreference"
                name="cookingTimePreference"
                value={formData.cookingTimePreference}
                onChange={handleChange}
                placeholder={t('mealPreferences.cookingTimePlaceholder')}
                min="5"
                max="180"
                className="glassy-input"
              />
              <small className="form-hint">{t('mealPreferences.cookingTimeHint')}</small>
            </div>

            <div className="form-group">
              <label htmlFor="budgetPerDay">
                {t('mealPreferences.budgetPerDay')} <span className="optional">{t('mealPreferences.optional')}</span>
              </label>
              <input
                type="number"
                id="budgetPerDay"
                name="budgetPerDay"
                value={formData.budgetPerDay}
                onChange={handleChange}
                placeholder={t('mealPreferences.budgetPerDayPlaceholder')}
                min="1"
                className="glassy-input"
              />
              <small className="form-hint">{t('mealPreferences.budgetPerDayHint')}</small>
            </div>
          </div>

          {/* Form Actions */}
          <div className="form-actions">
            <button
              type="button"
              onClick={() => {
                if (onCloseProp) {
                  onCloseProp()
                } else {
                  navigate('/dashboard')
                }
              }}
              className="cancel-button"
              disabled={saving}
            >
              {t('mealPreferences.cancel')}
            </button>
            <button
              type="submit"
              className="submit-button"
              disabled={saving}
            >
              {saving ? t('mealPreferences.saving') : t('mealPreferences.saveAndGenerate')}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default MealPreferences

