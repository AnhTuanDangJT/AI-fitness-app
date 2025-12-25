import React from 'react'
import { calculateLevel } from '../../utils/levels'
import './Gamification.css'

/**
 * XP Progress Bar Component
 * 
 * Displays user's current level, XP progress, and progress bar.
 * 
 * @param {number} xp - Current XP value
 */
function XPProgressBar({ xp = 0 }) {
  const level = calculateLevel(xp)
  const xpForCurrentLevel = level * 100
  const xpForNextLevel = (level + 1) * 100
  const progress = xp % 100
  const progressPercentage = Math.min(100, Math.max(0, progress))

  return (
    <div className="xp-progress-container">
      <div className="xp-header">
        <div className="xp-level-info">
          <span className="xp-level-label">Level</span>
          <span className="xp-level-value">{level}</span>
        </div>
        <div className="xp-points-info">
          <span className="xp-current">{xp}</span>
          <span className="xp-separator">/</span>
          <span className="xp-next">{xpForNextLevel}</span>
          <span className="xp-label">XP</span>
        </div>
      </div>
      
      <div className="xp-progress-bar-wrapper">
        <div className="xp-progress-bar">
          <div 
            className="xp-progress-fill"
            style={{ width: `${progressPercentage}%` }}
          />
        </div>
        <div className="xp-progress-text">
          {progress} / 100 XP to Level {level + 1}
        </div>
      </div>
    </div>
  )
}

export default XPProgressBar










