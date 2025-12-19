import React, { useState, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { mealPlanAPI } from '../services/api'
import { invalidateGamificationCache } from '../services/gamificationApi'
import { 
  MEAL_PLAN_LABELS, 
  MEAL_PLAN_STATUS, 
  ERROR_MESSAGES,
  BUTTON_TEXT 
} from '../config/constants'
import GroceryList from '../components/grocery-list/GroceryList'
import './MealPlan.css'

function MealPlan() {
  const { t, i18n } = useTranslation()
  // Explicit status state: "loading" | "empty" | "success" | "error"
  // This replaces the ambiguous combination of mealPlan/error/loading states
  const [status, setStatus] = useState('loading') // 'loading' | 'empty' | 'success' | 'error'
  const [mealPlan, setMealPlan] = useState(null) // Only set when status === 'success'
  const [error, setError] = useState('') // Only set when status === 'error'
  const [generating, setGenerating] = useState(false)
  const [expandedDays, setExpandedDays] = useState(new Set())
  const [isMobile, setIsMobile] = useState(window.innerWidth < 768)
  const [activeTab, setActiveTab] = useState('meal-plan') // 'meal-plan' or 'grocery-list'

  useEffect(() => {
    fetchMealPlan()
    
    const handleResize = () => {
      setIsMobile(window.innerWidth < 768)
      // On desktop, expand all days
      if (window.innerWidth >= 768) {
        const allDays = new Set(['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'])
        setExpandedDays(allDays)
      }
    }
    
    window.addEventListener('resize', handleResize)
    handleResize() // Initial check
    
    return () => window.removeEventListener('resize', handleResize)
  }, [])

  const fetchMealPlan = async () => {
    console.log('[MealPlan.jsx] fetchMealPlan() - START')
    console.log('[MealPlan.jsx] Current status:', status)
    
    try {
      setStatus('loading')
      setError('')
      setMealPlan(null)
      console.log('[MealPlan.jsx] Calling mealPlanAPI.getCurrent()...')
      
      const result = await mealPlanAPI.getCurrent()
      
      console.log('[MealPlan.jsx] API Result received:', {
        type: result?.type,
        hasData: !!result?.data,
        hasError: !!result?.error
      })
      
      // Handle structured response from API
      if (result.type === 'SUCCESS') {
        // Meal plan found - set success state
        console.log('[MealPlan.jsx] Meal plan found, setting success state')
        setMealPlan(result.data)
        setStatus('success')
        // Expand all days by default on desktop
        if (!isMobile) {
          const allDays = new Set(['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'])
          setExpandedDays(allDays)
        }
      } else if (result.type === 'EMPTY') {
        // No meal plan exists - this is a valid empty state, not an error
        // HTTP 404 from backend means user hasn't generated a meal plan yet
        console.log('[MealPlan.jsx] No meal plan found (empty state)')
        setStatus('empty')
      } else if (result.type === 'ERROR') {
        // Real API/network error occurred
        console.log('[MealPlan.jsx] Error occurred:', result.error)
        setError(result.error)
        setStatus('error')
      } else {
        // Unexpected response format
        console.warn('[MealPlan.jsx] Unexpected result type:', result)
        setError(MEAL_PLAN_STATUS.LOAD_FAILED)
        setStatus('error')
      }
    } catch (err) {
      // This catch block should rarely execute since API service handles errors
      // But keep it as a safety net
      console.error('[MealPlan.jsx] Unexpected exception in fetchMealPlan:', err)
      setError(err.message || MEAL_PLAN_STATUS.LOAD_FAILED)
      setStatus('error')
    }
  }

  const handleGenerate = async () => {
    const confirmed = window.confirm(
      `${t('mealPlan.confirmGenerate')}\n\n${t('mealPlan.confirmGenerateMessage')}`
    )
    
    if (!confirmed) {
      return
    }

    try {
      setGenerating(true)
      setError('')
      
      // Calculate next Monday (or today's Monday)
      const today = new Date()
      const dayOfWeek = today.getDay()
      const daysUntilMonday = dayOfWeek === 0 ? 1 : (8 - dayOfWeek) % 7 || 7
      const nextMonday = new Date(today)
      nextMonday.setDate(today.getDate() + daysUntilMonday)
      const weekStart = nextMonday.toISOString().split('T')[0]
      
      const response = await mealPlanAPI.generate(weekStart)
      if (response.success) {
        // Invalidate gamification cache after successful meal plan generation
        // This ensures XP updates are reflected in the UI
        invalidateGamificationCache()
        
        // After successful generation, refresh the meal plan
        // This will fetch the newly created meal plan
        await fetchMealPlan()
      } else {
        setError(response.message || MEAL_PLAN_STATUS.GENERATE_FAILED)
        setStatus('error')
      }
    } catch (err) {
      setError(err.genericMessage || MEAL_PLAN_STATUS.GENERATE_FAILED)
      setStatus('error')
      console.error('Error generating meal plan:', err)
    } finally {
      setGenerating(false)
    }
  }

  const toggleDay = (dayName) => {
    const newExpanded = new Set(expandedDays)
    if (newExpanded.has(dayName)) {
      newExpanded.delete(dayName)
    } else {
      newExpanded.add(dayName)
    }
    setExpandedDays(newExpanded)
  }

  const getDayName = (dateString) => {
    const date = new Date(dateString)
    const days = [
      t('mealPlan.sunday'),
      t('mealPlan.monday'),
      t('mealPlan.tuesday'),
      t('mealPlan.wednesday'),
      t('mealPlan.thursday'),
      t('mealPlan.friday'),
      t('mealPlan.saturday'),
    ]
    return days[date.getDay()]
  }

  const getLocale = () => (i18n.language === 'vi' ? 'vi-VN' : 'en-US')

  const formatDate = (dateString) => {
    const date = new Date(dateString)
    return date.toLocaleDateString(getLocale(), { month: 'short', day: 'numeric' })
  }

  const formatWeekRange = (weekStartDate) => {
    if (!weekStartDate) return null
    
    try {
      const startDate = new Date(weekStartDate)
      if (isNaN(startDate.getTime())) return null
      
      // Calculate end date (6 days after start)
      const endDate = new Date(startDate)
      endDate.setDate(startDate.getDate() + 6)
      
      const startFormatted = formatDate(weekStartDate)
      const endFormatted = formatDate(endDate.toISOString().split('T')[0])
      
      return `${startFormatted} – ${endFormatted}`
    } catch (error) {
      console.error('Error formatting week range:', error)
      // Fallback: show only start date if calculation fails
      return formatDate(weekStartDate)
    }
  }

  const translateMealName = (name) => {
    if (!name) return ''
    const normalizedKey = name
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, '_')
      .replace(/^_+|_+$/g, '')
    const translation = t(`mealNames.${normalizedKey}`, { defaultValue: name })
    return translation
  }

  const getMealTypeLabel = (mealType) => {
    switch (mealType) {
      case 'BREAKFAST':
        return t('mealPlan.breakfast')
      case 'LUNCH':
        return t('mealPlan.lunch')
      case 'DINNER':
        return t('mealPlan.dinner')
      case 'SNACK':
        return t('mealPlan.snack')
      default:
        return mealType
    }
  }

  const groupMealsByDay = (entries) => {
    const grouped = {}
    entries.forEach(entry => {
      const dayName = getDayName(entry.date)
      if (!grouped[dayName]) {
        grouped[dayName] = {
          date: entry.date,
          meals: []
        }
      }
      grouped[dayName].meals.push(entry)
    })
    
    // Sort meals by meal type order
    Object.keys(grouped).forEach(day => {
      grouped[day].meals.sort((a, b) => {
        const order = { BREAKFAST: 1, LUNCH: 2, DINNER: 3, SNACK: 4 }
        return (order[a.mealType] || 99) - (order[b.mealType] || 99)
      })
    })
    
    return grouped
  }

  console.log('[MealPlan.jsx] RENDER - Current status:', status)

  // Render based on explicit status state
  // This replaces ambiguous checks like "if (!mealPlan)" or "if (error && !mealPlan)"
  
  // Loading state: Show spinner while fetching
  if (status === 'loading') {
    console.log('[MealPlan.jsx] RENDER - Showing LOADING state')
    return (
      <div className="meal-plan-page">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>{t('mealPlan.loading')}</p>
        </div>
      </div>
    )
  }

  // Empty state: User has no meal plan yet (HTTP 404 from backend)
  // This is NOT an error - it's a valid first-time user state
  if (status === 'empty') {
    console.log('[MealPlan.jsx] RENDER - Showing EMPTY state (no meal plan exists)')
    return (
      <div className="meal-plan-page">
        <div className="no-meal-plan-container">
          <h2>{t('mealPlan.noMealPlanYet')}</h2>
          <p>{t('mealPlan.generateFirstPlan')}</p>
          <button onClick={handleGenerate} className="generate-button" disabled={generating}>
            {generating ? t('mealPlan.generating') : t('mealPlan.generateNew')}
          </button>
        </div>
      </div>
    )
  }

  // Error state: Real API/network error occurred
  // This is separate from empty state - something actually went wrong
  if (status === 'error') {
    console.log('[MealPlan.jsx] RENDER - Showing ERROR state:', error)
    return (
      <div className="meal-plan-page">
        <div className="error-container">
          <h2>{t('mealPlan.errorLoading')}</h2>
          <p>{error}</p>
          <button onClick={fetchMealPlan} className="generate-button" disabled={generating}>
            {t('mealPlan.retry')}
          </button>
        </div>
      </div>
    )
  }

  // Success state: Meal plan loaded successfully
  // Only render meal plan when status is explicitly 'success'
  if (status !== 'success' || !mealPlan) {
    // This should never happen, but handle gracefully
    console.warn('[MealPlan.jsx] Unexpected state: status=' + status + ', mealPlan=' + mealPlan)
    return (
      <div className="meal-plan-page">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>{t('common.loading')}</p>
        </div>
      </div>
    )
  }

  const mealsByDay = groupMealsByDay(mealPlan.entries)
  const dayOrder = [
    t('mealPlan.monday'),
    t('mealPlan.tuesday'),
    t('mealPlan.wednesday'),
    t('mealPlan.thursday'),
    t('mealPlan.friday'),
    t('mealPlan.saturday'),
    t('mealPlan.sunday'),
  ]

  return (
    <div className="meal-plan-page">
      <div className="meal-plan-header">
        <div>
          <h1>{t('mealPlan.title')}</h1>
          {mealPlan.weekStartDate && (() => {
            const weekRange = formatWeekRange(mealPlan.weekStartDate)
            // Use week range if available, otherwise fallback to start date only
            const dateDisplay = weekRange || formatDate(mealPlan.weekStartDate)
            return (
              <p className="week-info">
                {t('mealPlan.weekOf', { date: dateDisplay })}
              </p>
            )
          })()}
        </div>
        <div className="header-actions">
          <button 
            onClick={handleGenerate} 
            className="generate-button"
            disabled={generating}
          >
            {generating ? t('mealPlan.generating') : t('mealPlan.generateNew')}
          </button>
        </div>
      </div>


        {/* Tabs */}
        <div className="meal-plan-tabs">
          <button
            className={`tab-button ${activeTab === 'meal-plan' ? 'active' : ''}`}
            onClick={() => setActiveTab('meal-plan')}
          >
            {t('mealPlan.mealPlanTab')}
          </button>
          <button
            className={`tab-button ${activeTab === 'grocery-list' ? 'active' : ''}`}
            onClick={() => setActiveTab('grocery-list')}
          >
            {t('mealPlan.groceryListTab')}
          </button>
        </div>

        {/* Tab Content */}
        {activeTab === 'meal-plan' && (
          <>
            {mealPlan.dailyTargets && (
              <div className="daily-targets">
                <h2>{t('mealPlan.dailyTargets')}</h2>
                <div className="targets-grid">
                  <div className="target-item">
                    <span className="target-label">{t('mealPlan.calories')}</span>
                    <span className="target-value">{mealPlan.dailyTargets.calories} {t('mealPlan.kcal')}</span>
                  </div>
                  <div className="target-item">
                    <span className="target-label">{t('mealPlan.protein')}</span>
                    <span className="target-value">{mealPlan.dailyTargets.protein} {t('mealPlan.grams')}</span>
                  </div>
                  <div className="target-item">
                    <span className="target-label">{t('mealPlan.carbs')}</span>
                    <span className="target-value">{mealPlan.dailyTargets.carbs} {t('mealPlan.grams')}</span>
                  </div>
                  <div className="target-item">
                    <span className="target-label">{t('mealPlan.fats')}</span>
                    <span className="target-value">{mealPlan.dailyTargets.fats} {t('mealPlan.grams')}</span>
                  </div>
                </div>
              </div>
            )}

            <div className="meals-grid">
              {dayOrder.map(dayName => {
                const dayData = mealsByDay[dayName]
                if (!dayData) return null

                const isExpanded = expandedDays.has(dayName)

                return (
                  <div key={dayName} className="day-card">
                    <div 
                      className="day-header"
                      onClick={() => isMobile && toggleDay(dayName)}
                    >
                      <h3>{dayName}</h3>
                      <span className="day-date">{formatDate(dayData.date)}</span>
                      {isMobile && (
                        <span className="expand-icon">{isExpanded ? '−' : '+'}</span>
                      )}
                    </div>
                    
                    {(isExpanded || !isMobile) && (
                      <div className="day-meals">
                        {dayData.meals.map(meal => (
                          <div key={meal.id} className="meal-item">
                            <div className="meal-header">
                              <span className="meal-type">{getMealTypeLabel(meal.mealType)}</span>
                              <span className="meal-name">{translateMealName(meal.name)}</span>
                            </div>
                            <div className="meal-macros">
                              <span className="macro-item">
                                {meal.calories} {t('mealPlan.kcal')}
                              </span>
                              <span className="macro-item">
                                {t('mealPlan.protein')}: {meal.protein}{t('mealPlan.grams')}
                              </span>
                              <span className="macro-item">
                                {t('mealPlan.carbs')}: {meal.carbs}{t('mealPlan.grams')}
                              </span>
                              <span className="macro-item">
                                {t('mealPlan.fats')}: {meal.fats}{t('mealPlan.grams')}
                              </span>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                )
              })}
            </div>
          </>
        )}

        {activeTab === 'grocery-list' && (
          <GroceryList mealPlan={mealPlan} />
        )}
    </div>
  )
}

export default MealPlan

