import React from 'react'
import './Gamification.css'

/**
 * Streak Indicator Component
 * 
 * Displays current streak with flame icon and optional mini calendar.
 * 
 * @param {number} currentStreakDays - Current streak in days
 * @param {number} longestStreakDays - Longest streak achieved
 */
function StreakIndicator({ currentStreakDays = 0, longestStreakDays = 0 }) {
  const isActive = currentStreakDays > 0
  const isHighlighted = currentStreakDays >= 3

  return (
    <div className={`streak-indicator ${isHighlighted ? 'highlighted' : ''}`}>
      <div className="streak-content">
        <div className="streak-icon-wrapper">
          <span className="streak-icon" aria-label="Streak">🔥</span>
        </div>
        <div className="streak-info">
          <div className="streak-days">
            {currentStreakDays === 0 ? (
              <span className="streak-text">No active streak</span>
            ) : (
              <span className="streak-text">
                <span className="streak-number">{currentStreakDays}</span>
                {' '}day{currentStreakDays !== 1 ? 's' : ''} streak
              </span>
            )}
          </div>
          {longestStreakDays > 0 && (
            <div className="streak-longest">
              Best: {longestStreakDays} days
            </div>
          )}
        </div>
      </div>
      
      {/* Mini calendar view for last 7 days */}
      {isActive && (
        <div className="streak-calendar">
          <div className="streak-calendar-label">Last 7 days</div>
          <div className="streak-calendar-dots">
            {Array.from({ length: 7 }, (_, i) => {
              const daysAgo = 6 - i
              const isActive = currentStreakDays > daysAgo
              return (
                <div
                  key={i}
                  className={`streak-calendar-dot ${isActive ? 'active' : 'inactive'}`}
                  title={`${daysAgo === 0 ? 'Today' : `${daysAgo} days ago`}`}
                />
              )
            })}
          </div>
        </div>
      )}
    </div>
  )
}

export default StreakIndicator








