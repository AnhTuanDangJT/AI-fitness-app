import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../services/api'
import './EditProfile.css'

function EditProfile() {
  const navigate = useNavigate()
  const [formData, setFormData] = useState({
    weight: '',
    height: '',
    activityLevel: '',
    goal: '',
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
          weight: profile.weight || '',
          height: profile.height || '',
          activityLevel: profile.activityLevel || '',
          goal: profile.calorieGoal || '',
        })
      } else {
        setError(response.data.message || 'Failed to load profile')
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load profile. Please try again.')
      console.error('Error fetching profile:', err)
    } finally {
      setLoadingProfile(false)
    }
  }

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData({
      ...formData,
      [name]: name === 'activityLevel' || name === 'goal' ? parseInt(value) || '' : value,
    })
    setError('')
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    // Client-side validation
    if (!formData.weight || parseFloat(formData.weight) <= 0) {
      setError('Valid weight is required')
      setLoading(false)
      return
    }

    if (!formData.height || parseFloat(formData.height) <= 0) {
      setError('Valid height is required')
      setLoading(false)
      return
    }

    if (!formData.activityLevel || formData.activityLevel < 1 || formData.activityLevel > 5) {
      setError('Valid activity level is required')
      setLoading(false)
      return
    }

    if (!formData.goal || formData.goal < 1 || formData.goal > 4) {
      setError('Valid goal is required')
      setLoading(false)
      return
    }

    try {
      // Build update request with only changed fields
      const updateData = {}
      if (formData.weight && parseFloat(formData.weight) !== currentProfile?.weight) {
        updateData.weight = parseFloat(formData.weight)
      }
      if (formData.height && parseFloat(formData.height) !== currentProfile?.height) {
        updateData.height = parseFloat(formData.height)
      }
      if (formData.activityLevel && formData.activityLevel !== currentProfile?.activityLevel) {
        updateData.activityLevel = formData.activityLevel
      }
      if (formData.goal && formData.goal !== currentProfile?.calorieGoal) {
        updateData.goal = formData.goal
      }

      // Check if there are any changes
      if (Object.keys(updateData).length === 0) {
        setError('No changes detected. Please modify at least one field.')
        setLoading(false)
        return
      }

      const response = await api.put('/profile/update', updateData)

      if (response.data.success) {
        // Redirect to dashboard after successful update
        // This will trigger a recalculation and update the dashboard
        navigate('/dashboard')
      } else {
        setError(response.data.message || 'Failed to update profile')
      }
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.message ||
        'An error occurred while updating your profile'
      )
      console.error('Error updating profile:', err)
    } finally {
      setLoading(false)
    }
  }

  if (loadingProfile) {
    return (
      <div className="edit-profile-page">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>Loading your profile...</p>
        </div>
      </div>
    )
  }

  if (!currentProfile) {
    return (
      <div className="edit-profile-page">
        <div className="error-container">
          <h2>Profile Not Found</h2>
          <p>Please complete your profile setup first.</p>
          <button onClick={() => navigate('/profile-setup')} className="setup-button">
            Complete Profile Setup
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="edit-profile-page">
      <div className="edit-profile-container">
        <div className="edit-profile-header">
          <h1>Edit Profile</h1>
          <p>Update your measurements and goals. All calculations will be automatically updated.</p>
        </div>

        {error && (
          <div className="error-message">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="edit-profile-form">
          {/* Current Values Display */}
          <div className="current-values">
            <h3>Current Values</h3>
            <div className="values-grid">
              <div className="value-item">
                <span className="value-label">Weight:</span>
                <span className="value-text">{currentProfile.weight ? `${currentProfile.weight.toFixed(1)} kg` : 'N/A'}</span>
              </div>
              <div className="value-item">
                <span className="value-label">Height:</span>
                <span className="value-text">{currentProfile.height ? `${currentProfile.height.toFixed(1)} cm` : 'N/A'}</span>
              </div>
              <div className="value-item">
                <span className="value-label">Activity:</span>
                <span className="value-text">{currentProfile.activityLevelName || 'N/A'}</span>
              </div>
              <div className="value-item">
                <span className="value-label">Goal:</span>
                <span className="value-text">{currentProfile.calorieGoalName || 'N/A'}</span>
              </div>
            </div>
          </div>

          {/* Weight */}
          <div className="form-group">
            <label htmlFor="weight">Weight (kg)</label>
            <input
              type="number"
              id="weight"
              name="weight"
              value={formData.weight}
              onChange={handleChange}
              placeholder="Enter your weight in kilograms"
              step="0.1"
              min="0"
              required
            />
            {currentProfile.weight && (
              <span className="current-value-indicator">
                Current: {currentProfile.weight.toFixed(1)} kg
              </span>
            )}
          </div>

          {/* Height */}
          <div className="form-group">
            <label htmlFor="height">Height (cm)</label>
            <input
              type="number"
              id="height"
              name="height"
              value={formData.height}
              onChange={handleChange}
              placeholder="Enter your height in centimeters"
              step="0.1"
              min="0"
              required
            />
            {currentProfile.height && (
              <span className="current-value-indicator">
                Current: {currentProfile.height.toFixed(1)} cm
              </span>
            )}
          </div>

          {/* Activity Level */}
          <div className="form-group">
            <label htmlFor="activityLevel">Activity Level</label>
            <select
              id="activityLevel"
              name="activityLevel"
              value={formData.activityLevel}
              onChange={handleChange}
              required
              className="select-input"
            >
              <option value="">Select activity level</option>
              <option value={1}>Sedentary (no exercise)</option>
              <option value={2}>Lightly active (1–3×/week)</option>
              <option value={3}>Moderately active (3–5×/week)</option>
              <option value={4}>Very active (6–7×/week)</option>
              <option value={5}>Extra active (2×/day)</option>
            </select>
            {currentProfile.activityLevelName && (
              <span className="current-value-indicator">
                Current: {currentProfile.activityLevelName}
              </span>
            )}
          </div>

          {/* Goal */}
          <div className="form-group">
            <label htmlFor="goal">Fitness Goal</label>
            <select
              id="goal"
              name="goal"
              value={formData.goal}
              onChange={handleChange}
              required
              className="select-input"
            >
              <option value="">Select fitness goal</option>
              <option value={1}>Lose weight</option>
              <option value={2}>Maintain weight</option>
              <option value={3}>Gain muscle</option>
              <option value={4}>Gain muscle and lose fat (Recomposition)</option>
            </select>
            {currentProfile.calorieGoalName && (
              <span className="current-value-indicator">
                Current: {currentProfile.calorieGoalName}
              </span>
            )}
          </div>

          {/* Form Actions */}
          <div className="form-actions">
            <button
              type="button"
              onClick={() => navigate('/dashboard')}
              className="cancel-button"
              disabled={loading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="save-button"
              disabled={loading}
            >
              {loading ? 'Saving...' : 'Save Changes'}
            </button>
          </div>
        </form>

        <div className="info-note">
          <p>💡 <strong>Note:</strong> After saving, all metrics (BMI, WHR, BMR, TDEE, calories, protein) will be automatically recalculated and displayed on your dashboard.</p>
        </div>
      </div>
    </div>
  )
}

export default EditProfile

