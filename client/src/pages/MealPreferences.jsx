import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { mealPreferencesAPI, mealPlanAPI } from '../services/api'
import './MealPreferences.css'

function MealPreferences({ onClose: onCloseProp }) {
  const navigate = useNavigate()
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
        setError(saveResponse.message || 'Failed to save preferences')
        setSaving(false)
        return
      }

      setSuccess('Generating your meal plan...')

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
        setError(generateResponse.message || 'Failed to generate meal plan. Please try again.')
        setSaving(false)
        return
      }

      // 3. Success - navigate to meal plan view
      setSuccess('Meal plan generated successfully!')
      
      // Close modal if opened from dashboard, then navigate
      if (onCloseProp) {
        onCloseProp()
      }
      
      // Navigate to meal plan page
      navigate('/meal-plan')
    } catch (err) {
      setError(err.genericMessage || 'Unable to generate meal plan. Please try again.')
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
            <p>Loading preferences...</p>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="meal-preferences-page">
      <div className="meal-preferences-container">
        <div className="meal-preferences-header">
          <h1>Meal Preferences</h1>
          <p className="subtitle">Customize your meal plan based on your preferences</p>
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
            <h2 className="section-title">Food Preferences</h2>
            
            <div className="form-group">
              <label htmlFor="preferredFoods">
                Preferred Foods <span className="optional">(optional)</span>
              </label>
              <textarea
                id="preferredFoods"
                name="preferredFoods"
                value={formData.preferredFoods}
                onChange={handleChange}
                placeholder="e.g., chicken, salmon, quinoa, broccoli, sweet potatoes"
                rows="3"
                className="glassy-input"
              />
              <small className="form-hint">Enter foods you enjoy, separated by commas</small>
            </div>

            <div className="form-group">
              <label htmlFor="dislikedFoods">
                Disliked Foods <span className="optional">(optional)</span>
              </label>
              <textarea
                id="dislikedFoods"
                name="dislikedFoods"
                value={formData.dislikedFoods}
                onChange={handleChange}
                placeholder="e.g., mushrooms, olives, spicy food"
                rows="3"
                className="glassy-input"
              />
              <small className="form-hint">Enter foods you don't like, separated by commas</small>
            </div>

            <div className="form-group">
              <label htmlFor="allergies">
                Allergies <span className="optional">(optional)</span>
              </label>
              <textarea
                id="allergies"
                name="allergies"
                value={formData.allergies}
                onChange={handleChange}
                placeholder="e.g., peanuts, shellfish, dairy, gluten"
                rows="3"
                className="glassy-input"
              />
              <small className="form-hint">Enter any food allergies, separated by commas</small>
            </div>
          </div>

          {/* Dietary & Lifestyle Section */}
          <div className="form-section">
            <h2 className="section-title">Dietary & Lifestyle</h2>
            
            <div className="form-group">
              <label htmlFor="dietaryRestriction">
                Dietary Restriction <span className="optional">(optional)</span>
              </label>
              <select
                id="dietaryRestriction"
                name="dietaryRestriction"
                value={formData.dietaryRestriction}
                onChange={handleChange}
                className="glassy-input"
              >
                <option value="">Select a dietary restriction</option>
                <option value="omnivore">Omnivore</option>
                <option value="vegetarian">Vegetarian</option>
                <option value="vegan">Vegan</option>
                <option value="pescatarian">Pescatarian</option>
                <option value="halal">Halal</option>
                <option value="kosher">Kosher</option>
              </select>
              <small className="form-hint">Select your dietary preference or restriction</small>
            </div>

            <div className="form-group">
              <label htmlFor="favoriteCuisines">
                Favorite Cuisines <span className="optional">(optional)</span>
              </label>
              <textarea
                id="favoriteCuisines"
                name="favoriteCuisines"
                value={formData.favoriteCuisines}
                onChange={handleChange}
                placeholder="e.g., Italian, Mexican, Asian, Mediterranean, American"
                rows="3"
                className="glassy-input"
              />
              <small className="form-hint">Enter your favorite cuisines, separated by commas</small>
            </div>
          </div>

          {/* Meal Planning Preferences Section */}
          <div className="form-section">
            <h2 className="section-title">Meal Planning Preferences</h2>
            
            <div className="form-group">
              <label htmlFor="cookingTimePreference">
                Maximum Cooking Time per Meal <span className="optional">(optional)</span>
              </label>
              <input
                type="number"
                id="cookingTimePreference"
                name="cookingTimePreference"
                value={formData.cookingTimePreference}
                onChange={handleChange}
                placeholder="e.g., 30"
                min="5"
                max="180"
                className="glassy-input"
              />
              <small className="form-hint">Maximum time in minutes you're willing to spend cooking per meal</small>
            </div>

            <div className="form-group">
              <label htmlFor="budgetPerDay">
                Budget Per Day <span className="optional">(optional)</span>
              </label>
              <input
                type="number"
                id="budgetPerDay"
                name="budgetPerDay"
                value={formData.budgetPerDay}
                onChange={handleChange}
                placeholder="e.g., 50"
                min="1"
                className="glassy-input"
              />
              <small className="form-hint">Maximum daily budget for meals (in your currency)</small>
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
              Cancel
            </button>
            <button
              type="submit"
              className="submit-button"
              disabled={saving}
            >
              {saving ? 'Saving...' : 'Save Preferences'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default MealPreferences

