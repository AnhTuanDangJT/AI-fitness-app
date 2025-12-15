import React, { useState, useEffect, useRef } from 'react'
import { useTranslation } from 'react-i18next'
import AICoachChat from '../components/ai-coach/AICoachChat'
import api from '../services/api'
import './AICoach.css'

/**
 * AI Coach Page
 * 
 * Page component that wraps the AI Coach Chat interface.
 * Provides page layout and retrieves user information.
 * Now loads AI context to show personalized header.
 */
function AICoach() {
  const { t } = useTranslation()
  const [user, setUser] = useState(null)
  const [aiContext, setAiContext] = useState(null)
  const [loading, setLoading] = useState(true)
  const clearChatRef = useRef(null)

  useEffect(() => {
    // Get user info from localStorage
    const userData = localStorage.getItem('user')
    if (userData) {
      try {
        const parsedUser = JSON.parse(userData)
        setUser(parsedUser)
      } catch (e) {
        console.error('Error parsing user data:', e)
      }
    }
    
    // Load AI context
    loadAiContext()
    
    setLoading(false)
  }, [])
  
  const loadAiContext = async () => {
    try {
      const result = await api.getAiContext()
      if (result.type === 'SUCCESS') {
        setAiContext(result.data)
      }
    } catch (error) {
      console.error('Error loading AI context:', error)
    }
  }

  const handleClearChat = () => {
    if (clearChatRef.current) {
      clearChatRef.current()
    }
  }

  // Show loading state while getting user info
  if (loading) {
    return (
      <div className="ai-coach-page">
        <div className="ai-coach-container">
          <div className="loading-container">
            <div className="loading-spinner"></div>
            <p>{t('common.loading')}</p>
          </div>
        </div>
      </div>
    )
  }

  // Get userId from user object
  const userId = user?.id || user?.userId || null
  
  // Get goal description
  const getGoalDescription = (goal) => {
    if (!goal) return null
    const goals = {
      1: t('dashboard.goalLoseWeight') || 'Lose Weight',
      2: t('dashboard.goalMaintain') || 'Maintain',
      3: t('dashboard.goalGainMuscle') || 'Gain Muscle',
      4: t('dashboard.goalRecomposition') || 'Recomposition'
    }
    return goals[goal] || null
  }

  return (
    <div className="ai-coach-page">
      <div className="ai-coach-container">
        <div className="ai-coach-header">
          <div className="ai-coach-header-content">
            <div>
              <h1>{t('aiCoach.title')}</h1>
              {/* Personalized header with user data */}
              {aiContext && (
                <div className="ai-coach-personalized-header">
                  {aiContext.user?.name && (
                    <span className="personalized-name">{aiContext.user.name}</span>
                  )}
                  {aiContext.user?.goal && (
                    <span className="personalized-goal">
                      {getGoalDescription(aiContext.user.goal)}
                    </span>
                  )}
                  {aiContext.nutritionTargets?.calories && (
                    <span className="personalized-calories">
                      {Math.round(aiContext.nutritionTargets.calories)} kcal/day
                    </span>
                  )}
                  {aiContext.gamification?.currentStreakDays > 0 && (
                    <span className="personalized-streak">
                      🔥 {aiContext.gamification.currentStreakDays} day streak
                    </span>
                  )}
                </div>
              )}
              {!aiContext && <p>{t('aiCoach.subtitle')}</p>}
            </div>
            <button 
              className="clear-chat-button"
              onClick={handleClearChat}
              title={t('aiCoach.clearChat')}
            >
              {t('aiCoach.clearChat')}
            </button>
          </div>
        </div>
        <div className="ai-coach-content">
          <AICoachChat userId={userId} onClearChat={(clearFn) => { clearChatRef.current = clearFn }} />
        </div>
      </div>
    </div>
  )
}

export default AICoach

