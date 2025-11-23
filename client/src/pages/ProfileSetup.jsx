import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../services/api'
import './Auth.css'

function ProfileSetup() {
  const navigate = useNavigate()
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
      setError('Name is required')
      setLoading(false)
      return
    }

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

    if (!formData.age || parseInt(formData.age) <= 0) {
      setError('Valid age is required')
      setLoading(false)
      return
    }

    if (!formData.waist || parseFloat(formData.waist) <= 0) {
      setError('Valid waist measurement is required')
      setLoading(false)
      return
    }

    if (!formData.hip || parseFloat(formData.hip) <= 0) {
      setError('Valid hip measurement is required')
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
        setError(response.data.message || 'Failed to save profile')
      }
    } catch (err) {
      setError(
        err.response?.data?.message ||
        err.message ||
        'An error occurred while saving your profile'
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="auth-header">
          <h1>Profile Setup</h1>
          <p>Complete your fitness profile to get personalized recommendations.</p>
        </div>

        {error && (
          <div className="error-message">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label htmlFor="name">Name</label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              placeholder="Enter your full name"
              required
              autoComplete="name"
            />
          </div>

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
          </div>

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
          </div>

          <div className="form-group">
            <label htmlFor="age">Age</label>
            <input
              type="number"
              id="age"
              name="age"
              value={formData.age}
              onChange={handleChange}
              placeholder="Enter your age"
              min="1"
              max="120"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="waist">Waist (cm)</label>
            <input
              type="number"
              id="waist"
              name="waist"
              value={formData.waist}
              onChange={handleChange}
              placeholder="Enter your waist measurement in cm"
              step="0.1"
              min="0"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="hip">Hip (cm)</label>
            <input
              type="number"
              id="hip"
              name="hip"
              value={formData.hip}
              onChange={handleChange}
              placeholder="Enter your hip measurement in cm"
              step="0.1"
              min="0"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="sex">Sex</label>
            <select
              id="sex"
              name="sex"
              value={formData.sex}
              onChange={handleChange}
              required
              className="select-input"
            >
              <option value="male">Male</option>
              <option value="female">Female</option>
            </select>
          </div>

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
              <option value={1}>Sedentary (no exercise)</option>
              <option value={2}>Lightly active (1–3×/week)</option>
              <option value={3}>Moderately active (3–5×/week)</option>
              <option value={4}>Very active (6–7×/week)</option>
              <option value={5}>Extra active (2×/day)</option>
            </select>
          </div>

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
              <option value={1}>Lose weight</option>
              <option value={2}>Maintain weight</option>
              <option value={3}>Gain muscle</option>
              <option value={4}>Gain muscle and lose fat (Recomposition)</option>
            </select>
          </div>

          <button
            type="submit"
            className="submit-button"
            disabled={loading}
          >
            {loading ? 'Saving Profile...' : 'Save Profile'}
          </button>
        </form>

        <div className="auth-footer">
          <p>
            Already have a profile?{' '}
            <a href="/app/profile" className="link">
              View your profile
            </a>
          </p>
        </div>
      </div>
    </div>
  )
}

export default ProfileSetup

