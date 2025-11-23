import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../services/api'
import './ProfilePage.css'

function ProfilePage() {
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const navigate = useNavigate()

  useEffect(() => {
    fetchProfile()
  }, [])

  const fetchProfile = async () => {
    try {
      setLoading(true)
      setError(null)
      const response = await api.get('/user/profile')
      if (response.data.success) {
        setProfile(response.data.data)
      } else {
        setError(response.data.message || 'Failed to load profile')
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load profile. Please try again.')
      console.error('Error fetching profile:', err)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="profile-page">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>Loading your profile...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="profile-page">
        <div className="error-container">
          <h2>Error</h2>
          <p>{error}</p>
          <button onClick={fetchProfile} className="retry-button">
            Retry
          </button>
        </div>
      </div>
    )
  }

  if (!profile) {
    return (
      <div className="profile-page">
        <div className="empty-container">
          <h2>No Profile Data</h2>
          <p>Your profile is not available. Please create your profile.</p>
        </div>
      </div>
    )
  }

  return (
    <div className="profile-page">
      <div className="profile-header">
        <h1>Your Profile</h1>
        <button onClick={fetchProfile} className="refresh-button">
          Refresh
        </button>
      </div>

      {/* Personal Information */}
      <div className="profile-section">
        <h2>Personal Information</h2>
        <div className="info-grid">
          <div className="info-item">
            <span className="info-label">Name:</span>
            <span className="info-value">{profile.name || 'N/A'}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Username:</span>
            <span className="info-value">{profile.username}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Email:</span>
            <span className="info-value">{profile.email}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Age:</span>
            <span className="info-value">{profile.age ? `${profile.age} years` : 'N/A'}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Sex:</span>
            <span className="info-value">{profile.sex || 'N/A'}</span>
          </div>
        </div>
      </div>

      {/* Body Measurements */}
      <div className="profile-section">
        <h2>Body Measurements</h2>
        <div className="info-grid">
          <div className="info-item">
            <span className="info-label">Weight:</span>
            <span className="info-value">{profile.weight ? `${profile.weight.toFixed(1)} kg` : 'N/A'}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Height:</span>
            <span className="info-value">{profile.height ? `${profile.height.toFixed(1)} cm` : 'N/A'}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Waist:</span>
            <span className="info-value">{profile.waist ? `${profile.waist.toFixed(1)} cm` : 'N/A'}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Hip:</span>
            <span className="info-value">{profile.hip ? `${profile.hip.toFixed(1)} cm` : 'N/A'}</span>
          </div>
        </div>
      </div>

      {/* Body Metrics */}
      {profile.bodyMetrics && (
        <div className="profile-section">
          <h2>Body Metrics</h2>
          <div className="metrics-grid">
            <div className="metric-card">
              <h3>BMI</h3>
              <div className="metric-value">{profile.bodyMetrics.bmi ? profile.bodyMetrics.bmi.toFixed(2) : 'N/A'}</div>
              <div className="metric-label">{profile.bodyMetrics.bmiCategory || 'N/A'}</div>
            </div>
            <div className="metric-card">
              <h3>WHR</h3>
              <div className="metric-value">{profile.bodyMetrics.whr ? profile.bodyMetrics.whr.toFixed(2) : 'N/A'}</div>
              <div className="metric-label">{profile.bodyMetrics.whrHealthStatus || 'N/A'}</div>
            </div>
            <div className="metric-card">
              <h3>WHtR</h3>
              <div className="metric-value">{profile.bodyMetrics.whtr ? profile.bodyMetrics.whtr.toFixed(2) : 'N/A'}</div>
              <div className="metric-label">{profile.bodyMetrics.whtrRiskLevel || 'N/A'}</div>
            </div>
            <div className="metric-card">
              <h3>Body Fat</h3>
              <div className="metric-value">{profile.bodyMetrics.bodyFat ? `${profile.bodyMetrics.bodyFat.toFixed(1)}%` : 'N/A'}</div>
              <div className="metric-label">Body Fat %</div>
            </div>
          </div>
        </div>
      )}

      {/* Energy & Calories */}
      {profile.energy && (
        <div className="profile-section">
          <h2>Energy & Calories</h2>
          <div className="info-grid">
            <div className="info-item">
              <span className="info-label">BMR:</span>
              <span className="info-value">{profile.energy.bmr ? `${profile.energy.bmr.toFixed(1)} kcal/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">TDEE:</span>
              <span className="info-value">{profile.energy.tdee ? `${profile.energy.tdee.toFixed(1)} kcal/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Goal Calories:</span>
              <span className="info-value">{profile.energy.goalCalories ? `${profile.energy.goalCalories.toFixed(1)} kcal/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Activity Level:</span>
              <span className="info-value">{profile.activityLevelName || 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Goal:</span>
              <span className="info-value">{profile.calorieGoalName || 'N/A'}</span>
            </div>
          </div>
        </div>
      )}

      {/* Macronutrients */}
      {profile.macronutrients && (
        <div className="profile-section">
          <h2>Macronutrients</h2>
          <div className="info-grid">
            <div className="info-item">
              <span className="info-label">Protein:</span>
              <span className="info-value">{profile.macronutrients.protein ? `${profile.macronutrients.protein.toFixed(1)} g/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Fat:</span>
              <span className="info-value">{profile.macronutrients.fat ? `${profile.macronutrients.fat.toFixed(1)} g/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Carbohydrates:</span>
              <span className="info-value">{profile.macronutrients.carbohydrates ? `${profile.macronutrients.carbohydrates.toFixed(1)} g/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Fiber:</span>
              <span className="info-value">{profile.macronutrients.fiber ? `${profile.macronutrients.fiber} g/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Water:</span>
              <span className="info-value">{profile.macronutrients.water ? `${profile.macronutrients.water.toFixed(1)} L/day` : 'N/A'}</span>
            </div>
          </div>
        </div>
      )}

      {/* Micronutrients */}
      {profile.micronutrients && (
        <div className="profile-section">
          <h2>Micronutrients</h2>
          <div className="info-grid">
            <div className="info-item">
              <span className="info-label">Iron:</span>
              <span className="info-value">{profile.micronutrients.iron ? `${profile.micronutrients.iron} mg/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Calcium:</span>
              <span className="info-value">{profile.micronutrients.calcium ? `${profile.micronutrients.calcium} mg/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Vitamin D:</span>
              <span className="info-value">{profile.micronutrients.vitaminD ? `${profile.micronutrients.vitaminD} µg/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Magnesium:</span>
              <span className="info-value">{profile.micronutrients.magnesium ? `${profile.micronutrients.magnesium} mg/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Zinc:</span>
              <span className="info-value">{profile.micronutrients.zinc ? `${profile.micronutrients.zinc} mg/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Vitamin B12:</span>
              <span className="info-value">{profile.micronutrients.vitaminB12 ? `${profile.micronutrients.vitaminB12} µg/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Potassium:</span>
              <span className="info-value">{profile.micronutrients.potassium ? `${profile.micronutrients.potassium} mg/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Sodium:</span>
              <span className="info-value">{profile.micronutrients.sodium ? `${profile.micronutrients.sodium} mg/day` : 'N/A'}</span>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default ProfilePage

