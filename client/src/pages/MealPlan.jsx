import React, { useState, useEffect } from 'react'
import { mealPlanAPI } from '../services/api'
import { 
  MEAL_PLAN_LABELS, 
  MEAL_PLAN_STATUS, 
  ERROR_MESSAGES,
  BUTTON_TEXT 
} from '../config/constants'
import GroceryList from '../components/grocery-list/GroceryList'
import './MealPlan.css'

function MealPlan() {
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
      `${MEAL_PLAN_LABELS.CONFIRM_GENERATE}\n\n${MEAL_PLAN_LABELS.CONFIRM_GENERATE_MESSAGE}`
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
      MEAL_PLAN_LABELS.SUNDAY,
      MEAL_PLAN_LABELS.MONDAY,
      MEAL_PLAN_LABELS.TUESDAY,
      MEAL_PLAN_LABELS.WEDNESDAY,
      MEAL_PLAN_LABELS.THURSDAY,
      MEAL_PLAN_LABELS.FRIDAY,
      MEAL_PLAN_LABELS.SATURDAY,
    ]
    return days[date.getDay()]
  }

  const formatDate = (dateString) => {
    const date = new Date(dateString)
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })
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

  const getMealTypeLabel = (mealType) => {
    switch (mealType) {
      case 'BREAKFAST':
        return MEAL_PLAN_LABELS.BREAKFAST
      case 'LUNCH':
        return MEAL_PLAN_LABELS.LUNCH
      case 'DINNER':
        return MEAL_PLAN_LABELS.DINNER
      case 'SNACK':
        return MEAL_PLAN_LABELS.SNACK
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
          <p>{MEAL_PLAN_STATUS.LOADING}</p>
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
          <h2>No Meal Plan Yet</h2>
          <p>Generate your first personalized meal plan</p>
          <button onClick={handleGenerate} className="generate-button" disabled={generating}>
            {generating ? MEAL_PLAN_LABELS.GENERATING : 'Generate Meal Plan'}
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
          <h2>Error Loading Meal Plan</h2>
          <p>{error}</p>
          <button onClick={fetchMealPlan} className="generate-button" disabled={generating}>
            Retry
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
          <p>Loading...</p>
        </div>
      </div>
    )
  }

  const mealsByDay = groupMealsByDay(mealPlan.entries)
  const dayOrder = [
    MEAL_PLAN_LABELS.MONDAY,
    MEAL_PLAN_LABELS.TUESDAY,
    MEAL_PLAN_LABELS.WEDNESDAY,
    MEAL_PLAN_LABELS.THURSDAY,
    MEAL_PLAN_LABELS.FRIDAY,
    MEAL_PLAN_LABELS.SATURDAY,
    MEAL_PLAN_LABELS.SUNDAY,
  ]

  return (
    <div className="meal-plan-page">
      <div className="meal-plan-header">
        <div>
          <h1>{MEAL_PLAN_LABELS.TITLE}</h1>
          {mealPlan.weekStartDate && (() => {
            const weekRange = formatWeekRange(mealPlan.weekStartDate)
            // Use week range if available, otherwise fallback to start date only
            const dateDisplay = weekRange || formatDate(mealPlan.weekStartDate)
            return (
              <p className="week-info">
                {MEAL_PLAN_LABELS.WEEK_START} {dateDisplay}
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
            {generating ? MEAL_PLAN_LABELS.GENERATING : MEAL_PLAN_LABELS.GENERATE_NEW}
          </button>
        </div>
      </div>


        {/* Tabs */}
        <div className="meal-plan-tabs">
          <button
            className={`tab-button ${activeTab === 'meal-plan' ? 'active' : ''}`}
            onClick={() => setActiveTab('meal-plan')}
          >
            Meal Plan
          </button>
          <button
            className={`tab-button ${activeTab === 'grocery-list' ? 'active' : ''}`}
            onClick={() => setActiveTab('grocery-list')}
          >
            Grocery List
          </button>
        </div>

        {/* Tab Content */}
        {activeTab === 'meal-plan' && (
          <>
            {mealPlan.dailyTargets && (
              <div className="daily-targets">
                <h2>{MEAL_PLAN_LABELS.DAILY_TARGETS}</h2>
                <div className="targets-grid">
                  <div className="target-item">
                    <span className="target-label">{MEAL_PLAN_LABELS.CALORIES}</span>
                    <span className="target-value">{mealPlan.dailyTargets.calories} {MEAL_PLAN_LABELS.KCAL}</span>
                  </div>
                  <div className="target-item">
                    <span className="target-label">{MEAL_PLAN_LABELS.PROTEIN}</span>
                    <span className="target-value">{mealPlan.dailyTargets.protein} {MEAL_PLAN_LABELS.GRAMS}</span>
                  </div>
                  <div className="target-item">
                    <span className="target-label">{MEAL_PLAN_LABELS.CARBS}</span>
                    <span className="target-value">{mealPlan.dailyTargets.carbs} {MEAL_PLAN_LABELS.GRAMS}</span>
                  </div>
                  <div className="target-item">
                    <span className="target-label">{MEAL_PLAN_LABELS.FATS}</span>
                    <span className="target-value">{mealPlan.dailyTargets.fats} {MEAL_PLAN_LABELS.GRAMS}</span>
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
                              <span className="meal-name">{meal.name}</span>
                            </div>
                            <div className="meal-macros">
                              <span className="macro-item">
                                {meal.calories} {MEAL_PLAN_LABELS.KCAL}
                              </span>
                              <span className="macro-item">
                                {MEAL_PLAN_LABELS.PROTEIN}: {meal.protein}{MEAL_PLAN_LABELS.GRAMS}
                              </span>
                              <span className="macro-item">
                                {MEAL_PLAN_LABELS.CARBS}: {meal.carbs}{MEAL_PLAN_LABELS.GRAMS}
                              </span>
                              <span className="macro-item">
                                {MEAL_PLAN_LABELS.FATS}: {meal.fats}{MEAL_PLAN_LABELS.GRAMS}
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

