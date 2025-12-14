import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { mealPlanAPI } from '../../services/api'
import './MealPlanPanel.css'

function MealPlanPanel() {
  const navigate = useNavigate()
  const [status, setStatus] = useState('loading') // 'loading' | 'empty' | 'success' | 'error'
  const [mealPlan, setMealPlan] = useState(null)

  useEffect(() => {
    fetchMealPlan()
  }, [])

  const fetchMealPlan = async () => {
    try {
      setStatus('loading')
      const result = await mealPlanAPI.getCurrent()
      
      if (result.type === 'SUCCESS') {
        setMealPlan(result.data)
        setStatus('success')
      } else if (result.type === 'EMPTY') {
        setStatus('empty')
      } else if (result.type === 'ERROR') {
        setStatus('error')
      }
    } catch (err) {
      console.error('Error fetching meal plan:', err)
      setStatus('error')
    }
  }

  if (status === 'loading') {
    return (
      <div className="meal-plan-panel">
        <div className="meal-plan-loading">
          <p>Loading meal plan...</p>
        </div>
      </div>
    )
  }

  if (status === 'empty') {
    return (
      <div className="meal-plan-panel">
        <div className="meal-plan-placeholder">
          <p>Meal plan will appear here.</p>
          <button 
            className="generate-plan-button-small"
            onClick={() => navigate('/meal-plan')}
          >
            Generate Weekly Plan
          </button>
        </div>
      </div>
    )
  }

  if (status === 'error') {
    return (
      <div className="meal-plan-panel">
        <div className="meal-plan-error">
          <p>Unable to load meal plan.</p>
          <button 
            className="retry-button-small"
            onClick={fetchMealPlan}
          >
            Retry
          </button>
        </div>
      </div>
    )
  }

  if (status === 'success' && mealPlan) {
    // Show a preview of the meal plan
    const weekStart = mealPlan.weekStartDate ? new Date(mealPlan.weekStartDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }) : ''
    const entriesCount = mealPlan.entries ? mealPlan.entries.length : 0
    
    return (
      <div className="meal-plan-panel">
        <div className="meal-plan-preview">
          <p className="meal-plan-week">Week of {weekStart}</p>
          <p className="meal-plan-meals">{entriesCount} meals planned</p>
          <button 
            className="view-plan-button"
            onClick={() => navigate('/meal-plan')}
          >
            View Full Plan
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="meal-plan-panel">
      <div className="meal-plan-placeholder">
        <p>Meal plan will appear here.</p>
      </div>
    </div>
  )
}

export default MealPlanPanel



