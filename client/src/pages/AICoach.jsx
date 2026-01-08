import React, { useState, useEffect, useRef } from 'react'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router-dom'
import { motion } from 'framer-motion'
import AICoachChat from '../components/ai-coach/AICoachChat'
import api from '../services/api'
import { AppShell } from '../components/layout/AppShell'
import Button from '../components/ui/Button'
import Skeleton from '../components/ui/Skeleton'
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
  const navigate = useNavigate()
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
      <AppShell>
        <div className="p-6 lg:p-8">
          <div className="mx-auto max-w-4xl space-y-4">
            <Skeleton className="h-16 w-full" />
            <Skeleton className="h-96 w-full" />
          </div>
        </div>
      </AppShell>
    )
  }

  // Get userId from user object
  const userId = user?.id || user?.userId || null
  
  // Get goal description
  const getGoalDescription = (goal) => {
    if (!goal) return null
    const goalMap = {
      1: t('dashboard.calorieGoals.loseWeight'),
      2: t('dashboard.calorieGoals.maintain'),
      3: t('dashboard.calorieGoals.gainMuscle'),
      4: t('dashboard.calorieGoals.recomp')
    }
    return goalMap[goal] || null
  }

  return (
    <AppShell>
      <div className="flex min-h-screen flex-col bg-base-900 p-6 lg:p-8">
        <div className="mx-auto w-full max-w-4xl space-y-6">
          {/* Header */}
          <motion.div
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between"
          >
            <div className="flex-1">
              <h1 className="text-3xl font-semibold text-white">{t('aiCoach.title')}</h1>
              {aiContext && (
                <div className="mt-2 flex flex-wrap gap-2">
                  {aiContext.user?.name && (
                    <span className="rounded-full bg-accent/20 px-3 py-1 text-sm font-semibold text-accent">
                      {aiContext.user.name}
                    </span>
                  )}
                  {aiContext.user?.goal && (
                    <span className="rounded-full bg-emerald-500/20 px-3 py-1 text-sm font-semibold text-emerald-200">
                      {getGoalDescription(aiContext.user.goal)}
                    </span>
                  )}
                  {aiContext.nutritionTargets?.calories && (
                    <span className="rounded-full bg-cyan-500/20 px-3 py-1 text-sm font-semibold text-cyan-200">
                      {Math.round(aiContext.nutritionTargets.calories)} {t('dashboard.kcalPerDayShort')}
                    </span>
                  )}
                  {aiContext.gamification?.currentStreakDays > 0 && (
                    <span className="rounded-full bg-amber-500/20 px-3 py-1 text-sm font-semibold text-amber-200">
                      {t('aiCoach.personalizedStreak', {
                        count: aiContext.gamification.currentStreakDays,
                        unit:
                          aiContext.gamification.currentStreakDays === 1
                            ? t('dashboard.daySingular')
                            : t('dashboard.dayPlural'),
                      })}
                    </span>
                  )}
                </div>
              )}
              {!aiContext && <p className="mt-2 text-white/60">{t('aiCoach.subtitle')}</p>}
            </div>
            <Button variant="ghost" onClick={handleClearChat} title={t('aiCoach.clearChat')}>
              {t('aiCoach.clearChat')}
            </Button>
          </motion.div>

          {/* Chat Content */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
            className="flex-1"
          >
            <AICoachChat userId={userId} onClearChat={(clearFn) => { clearChatRef.current = clearFn }} />
          </motion.div>
        </div>
      </div>
    </AppShell>
  )
}

export default AICoach

