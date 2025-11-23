import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import './AppPage.css'

/**
 * Main App Page
 * 
 * This page represents the main application features from the Java program.
 * It will eventually contain:
 * - Profile overview
 * - Body metrics display
 * - Nutrition calculator
 * - Profile settings
 * 
 * For now, it's a placeholder with navigation structure.
 */
function App() {
  const [user, setUser] = useState(null)
  const navigate = useNavigate()

  useEffect(() => {
    // Get user info from localStorage
    const userData = localStorage.getItem('user')
    if (userData) {
      setUser(JSON.parse(userData))
    }
  }, [])

  const handleLogout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    navigate('/login')
  }

  if (!user) {
    return (
      <div className="app-page">
        <div className="app-container">Loading...</div>
      </div>
    )
  }

  return (
    <div className="app-page">
      <div className="app-header">
        <div className="app-header-content">
          <h1>AI Fitness</h1>
          <div className="app-header-actions">
            <span className="user-info">Welcome, {user.username}</span>
            <button onClick={handleLogout} className="logout-button">
              Logout
            </button>
          </div>
        </div>
      </div>

      <div className="app-container">
        <div className="app-sidebar">
          <nav className="app-nav">
            <h2>Navigation</h2>
            <ul>
              <li>
                <a href="/dashboard" className="nav-link">
                  Dashboard
                </a>
              </li>
              <li>
                <a href="/app/profile" className="nav-link">
                  Profile
                </a>
              </li>
              <li>
                <a href="#body-metrics" className="nav-link">
                  Body Metrics
                </a>
              </li>
              <li>
                <a href="#nutrition" className="nav-link">
                  Nutrition
                </a>
              </li>
              <li>
                <a href="#settings" className="nav-link">
                  Settings
                </a>
              </li>
            </ul>
          </nav>
        </div>

        <div className="app-main">
          <div className="app-content">
            <h2>Welcome to AI Fitness</h2>
            <p className="welcome-text">
              This is the main application page. Here you'll find all the features
              from your original Java program:
            </p>

            <div className="feature-grid">
              <div className="feature-card">
                <h3>Profile Overview</h3>
                <p>
                  View your complete fitness profile including personal information,
                  body measurements, and calculated metrics.
                </p>
                <span className="feature-status">Coming soon</span>
              </div>

              <div className="feature-card">
                <h3>Body Metrics</h3>
                <p>
                  See your BMI, WHR, WHtR, Body Fat percentage with health
                  interpretations and risk assessments.
                </p>
                <span className="feature-status">Coming soon</span>
              </div>

              <div className="feature-card">
                <h3>Nutrition Calculator</h3>
                <p>
                  Get personalized daily calorie goals, macronutrients (protein, fat, carbs),
                  and micronutrient requirements based on your goals.
                </p>
                <span className="feature-status">Coming soon</span>
              </div>

              <div className="feature-card">
                <h3>Profile Settings</h3>
                <p>
                  Update your profile information, body measurements, activity level,
                  and fitness goals. All metrics will be automatically recalculated.
                </p>
                <span className="feature-status">Coming soon</span>
              </div>
            </div>

            <div className="user-info-section">
              <h3>Your Account</h3>
              <div className="info-item">
                <strong>Username:</strong> {user.username}
              </div>
              <div className="info-item">
                <strong>Email:</strong> {user.email}
              </div>
              <div className="info-item">
                <strong>User ID:</strong> {user.id}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default App

