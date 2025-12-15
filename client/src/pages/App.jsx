import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
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
  const { t } = useTranslation()
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
        <div className="app-container">{t('common.loading')}</div>
      </div>
    )
  }

  return (
    <div className="app-page">
      <div className="app-header">
        <div className="app-header-content">
          <h1>{t('appPage.title')}</h1>
          <div className="app-header-actions">
            <span className="user-info">{t('appPage.welcome', { username: user.username })}</span>
            <button onClick={handleLogout} className="logout-button">
              {t('appPage.logout')}
            </button>
          </div>
        </div>
      </div>

      <div className="app-container">
        <div className="app-sidebar">
          <nav className="app-nav">
            <h2>{t('appPage.navigation')}</h2>
            <ul>
              <li>
                <a href="/dashboard" className="nav-link">
                  {t('appPage.dashboard')}
                </a>
              </li>
              <li>
                <a href="/app/profile" className="nav-link">
                  {t('appPage.profile')}
                </a>
              </li>
              <li>
                <a href="/meal-plan" className="nav-link">
                  {t('appPage.mealPlan')}
                </a>
              </li>
              <li>
                <a href="#body-metrics" className="nav-link">
                  {t('appPage.bodyMetrics')}
                </a>
              </li>
              <li>
                <a href="#nutrition" className="nav-link">
                  {t('appPage.nutrition')}
                </a>
              </li>
              <li>
                <a href="#settings" className="nav-link">
                  {t('appPage.settings')}
                </a>
              </li>
            </ul>
          </nav>
        </div>

        <div className="app-main">
          <div className="app-content">
            <h2>{t('appPage.welcomeToAI')}</h2>
            <p className="welcome-text">
              {t('appPage.mainAppDescription')}
            </p>

            <div className="feature-grid">
              <div className="feature-card">
                <h3>{t('appPage.profileOverview')}</h3>
                <p>
                  {t('appPage.profileOverviewDescription')}
                </p>
                <span className="feature-status">{t('appPage.comingSoon')}</span>
              </div>

              <div className="feature-card">
                <h3>{t('appPage.bodyMetricsTitle')}</h3>
                <p>
                  {t('appPage.bodyMetricsDescription')}
                </p>
                <span className="feature-status">{t('appPage.comingSoon')}</span>
              </div>

              <div className="feature-card">
                <h3>{t('appPage.nutritionCalculator')}</h3>
                <p>
                  {t('appPage.nutritionCalculatorDescription')}
                </p>
                <span className="feature-status">{t('appPage.comingSoon')}</span>
              </div>

              <div className="feature-card">
                <h3>{t('appPage.profileSettings')}</h3>
                <p>
                  {t('appPage.profileSettingsDescription')}
                </p>
                <span className="feature-status">{t('appPage.comingSoon')}</span>
              </div>
            </div>

            <div className="user-info-section">
              <h3>{t('appPage.yourAccount')}</h3>
              <div className="info-item">
                <strong>{t('appPage.username')}</strong> {user.username}
              </div>
              <div className="info-item">
                <strong>{t('appPage.email')}</strong> {user.email}
              </div>
              <div className="info-item">
                <strong>{t('appPage.userId')}</strong> {user.id}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default App

