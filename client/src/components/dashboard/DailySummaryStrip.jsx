import React from 'react'
import { calculateLevel, getTitle } from '../../utils/levels'
import './DailySummaryStrip.css'

/**
 * Daily Summary Strip Component
 * 
 * A horizontal summary bar displaying key daily metrics:
 * - XP + Level
 * - Current streak
 * - Daily calorie target
 * - Protein target
 * 
 * @param {Object} props
 * @param {Object} props.gamificationStatus - Gamification status object
 * @param {number} props.calorieTarget - Daily calorie target
 * @param {number} props.proteinTarget - Daily protein target
 */
function DailySummaryStrip({ 
  gamificationStatus = null, 
  calorieTarget = null, 
  proteinTarget = null 
}) {
  // Safe defaults if data is not available
  const xp = gamificationStatus?.xp ?? 0
  const currentStreakDays = gamificationStatus?.currentStreakDays ?? 0
  const level = calculateLevel(xp)
  const title = getTitle(level)

  return (
    <div className="daily-summary-strip">
      {/* XP + Level */}
      <div className="summary-item">
        <div className="summary-label">Level {level}</div>
        <div className="summary-value">{xp} XP</div>
        <div className="summary-subtitle">{title}</div>
      </div>

      {/* Current Streak */}
      <div className="summary-item">
        <div className="summary-label">Streak</div>
        <div className="summary-value">
          <span className="streak-emoji">🔥</span> {currentStreakDays}
        </div>
        <div className="summary-subtitle">{currentStreakDays === 1 ? 'day' : 'days'}</div>
      </div>

      {/* Daily Calorie Target */}
      <div className="summary-item">
        <div className="summary-label">Calories</div>
        <div className="summary-value">
          {calorieTarget ? Math.round(calorieTarget) : 'N/A'}
        </div>
        <div className="summary-subtitle">kcal/day</div>
      </div>

      {/* Protein Target */}
      <div className="summary-item">
        <div className="summary-label">Protein</div>
        <div className="summary-value">
          {proteinTarget ? Math.round(proteinTarget) : 'N/A'}
        </div>
        <div className="summary-subtitle">g/day</div>
      </div>
    </div>
  )
}

export default DailySummaryStrip



