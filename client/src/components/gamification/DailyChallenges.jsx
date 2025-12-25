import React, { useState, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { gamificationAPI, invalidateGamificationCache } from '../../services/gamificationApi'
import { DAILY_CHALLENGES } from '../../constants/dailyChallenges'
import './Gamification.css'

/**
 * Daily Challenges Component
 * 
 * DISPLAY-ONLY: Frontend never calculates streaks or dates.
 * Backend is the source of truth for all gamification data.
 * 
 * Challenges are determined based on gamification status from backend.
 * Completion status is checked via backend gamification events.
 */
function DailyChallenges() {
  const { t } = useTranslation()
  const [gamificationStatus, setGamificationStatus] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [completingChallenge, setCompletingChallenge] = useState(null)

  useEffect(() => {
    fetchGamificationStatus()
  }, [])

  const fetchGamificationStatus = async () => {
    try {
      setLoading(true)
      setError(null)
      // Force refresh to ensure we get latest status (including dailyChallengeCompleted)
      const result = await gamificationAPI.getGamificationStatus(true)
      if (result.type === 'SUCCESS') {
        setGamificationStatus(result.data)
      } else {
        setError(result.error || t('gamification.failedToLoad'))
      }
    } catch (err) {
      console.error('Error fetching gamification status:', err)
      setError(t('gamification.failedToLoad'))
    } finally {
      setLoading(false)
    }
  }

  /**
   * Handle challenge completion.
   * 
   * Frontend NEVER grants XP directly - only triggers backend endpoint.
   * Backend handles idempotency (can only earn once per calendar day).
   */
  const handleChallengeComplete = async (challengeId) => {
    // Only LOG_TODAY challenge can be manually completed via backend endpoint
    // CONSISTENCY_7 is automatically determined by streak
    if (challengeId !== 'LOG_TODAY') {
      return
    }

    // If already completed, don't call backend again
    if (getChallengeStatus(challengeId)) {
      return
    }

    try {
      setCompletingChallenge(challengeId)
      
      // Call backend endpoint to record daily challenge completion
      const result = await gamificationAPI.recordDailyChallenge()
      
      if (result.type === 'SUCCESS') {
        // Invalidate cache and refetch status
        invalidateGamificationCache()
        await fetchGamificationStatus()
      } else {
        setError(result.error || t('gamification.failedToComplete'))
      }
    } catch (err) {
      console.error('Error completing challenge:', err)
      setError(t('gamification.failedToComplete'))
    } finally {
      setCompletingChallenge(null)
    }
  }

  /**
   * Determine challenge completion status from backend.
   * 
   * Frontend is DISPLAY-ONLY - never calculates completion locally.
   * All logic is in backend gamification events.
   */
  const getChallengeStatus = (challengeId) => {
    if (!gamificationStatus) return false

    switch (challengeId) {
      case 'LOG_TODAY':
        // Check if DAILY_CHALLENGE_COMPLETED XP was already granted today
        // Backend returns dailyChallengeCompleted flag indicating if event exists for today
        return gamificationStatus.dailyChallengeCompleted === true
      
      case 'CONSISTENCY_7':
        // Check if user has 7+ day streak (from backend)
        return gamificationStatus.currentStreakDays >= 7
      
      default:
        return false
    }
  }

  if (loading) {
    return (
      <div className="daily-challenges-compact">
        <div className="daily-challenges-compact-loading">
          <div className="loading-spinner-small"></div>
          <span>{t('gamification.loadingChallenges')}</span>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="daily-challenges-compact">
        <div className="daily-challenges-compact-error">
          <span>{error}</span>
          <button onClick={fetchGamificationStatus} className="retry-button-small">
            {t('dashboard.retry')}
          </button>
        </div>
      </div>
    )
  }

  const challenges = Object.values(DAILY_CHALLENGES)
  const completedCount = challenges.filter(challenge => getChallengeStatus(challenge.id)).length

  return (
    <div className="daily-challenges-compact">
      <div className="daily-challenges-compact-header">
        <h3>{t('gamification.dailyChallenges')}</h3>
        <span className="daily-challenges-compact-progress">{t('gamification.completed', { completed: completedCount, total: challenges.length })}</span>
      </div>
      <ul className="daily-challenges-compact-list">
        {challenges.map((challenge) => {
          const isCompleted = getChallengeStatus(challenge.id)
          const isCompleting = completingChallenge === challenge.id
          const canComplete = challenge.id === 'LOG_TODAY' && !isCompleted && !isCompleting
          const title = t(challenge.nameKey)
          const description = t(challenge.descriptionKey)
          
          return (
            <li
              key={challenge.id}
              className={`daily-challenges-compact-item ${isCompleted ? 'completed' : ''}`}
            >
              <div className="daily-challenges-compact-content">
                <span className="daily-challenges-compact-icon" aria-hidden>
                  {challenge.icon}
                </span>
                <div className="daily-challenges-compact-text">
                  <span className="daily-challenges-compact-title">{title}</span>
                  <span className="daily-challenges-compact-description">{description}</span>
                </div>
              </div>
              <span className="daily-challenges-compact-xp">+{challenge.xpReward} XP</span>
              {isCompleted ? (
                <span className="daily-challenges-compact-status">✓</span>
              ) : canComplete ? (
                <button
                  onClick={() => handleChallengeComplete(challenge.id)}
                  className="daily-challenges-compact-complete-button"
                  disabled={isCompleting}
                >
                  {isCompleting ? t('gamification.completing') : t('gamification.complete')}
                </button>
              ) : (
                <span className="daily-challenges-compact-status">○</span>
              )}
            </li>
          )
        })}
      </ul>
    </div>
  )
}

export default DailyChallenges

