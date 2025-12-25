import React, { useState } from 'react'
import './Gamification.css'

/**
 * Badge definitions with descriptions
 */
const BADGE_DEFINITIONS = {
  FIRST_LOG: {
    name: 'First Log',
    description: 'Log your first activity or meal',
    icon: '🎯',
  },
  STREAK_3: {
    name: '3-Day Streak',
    description: 'Maintain a 3-day streak',
    icon: '🔥',
  },
  STREAK_7: {
    name: '7-Day Streak',
    description: 'Maintain a 7-day streak',
    icon: '⚡',
  },
  STREAK_30: {
    name: '30-Day Streak',
    description: 'Maintain a 30-day streak',
    icon: '💎',
  },
  XP_100: {
    name: 'Centurion',
    description: 'Reach 100 XP',
    icon: '⭐',
  },
  XP_500: {
    name: 'Elite',
    description: 'Reach 500 XP',
    icon: '👑',
  },
}

/**
 * Badge Grid Component
 * 
 * Displays all badges, showing unlocked badges prominently and locked badges greyed out.
 * 
 * @param {Array<string>} badges - Array of unlocked badge IDs
 */
function BadgeGrid({ badges = [] }) {
  const [hoveredBadge, setHoveredBadge] = useState(null)
  const [isExpanded, setIsExpanded] = useState(false)

  // Get all badge IDs in order
  const allBadgeIds = Object.keys(BADGE_DEFINITIONS)

  // Determine which badges to show (all if expanded, or first 3 if collapsed)
  const badgesToShow = isExpanded ? allBadgeIds : allBadgeIds.slice(0, 3)

  return (
    <div className="badge-grid-container">
      <div className="badge-grid-header">
        <h3 className="badge-grid-title">Badges</h3>
        {allBadgeIds.length > 3 && (
          <button
            className="badge-grid-toggle"
            onClick={() => setIsExpanded(!isExpanded)}
            aria-label={isExpanded ? 'Collapse badges' : 'Expand badges'}
          >
            {isExpanded ? '▼' : '▶'}
          </button>
        )}
      </div>
      
      <div className="badge-grid">
        {badgesToShow.map((badgeId) => {
          const badge = BADGE_DEFINITIONS[badgeId]
          const isUnlocked = badges.includes(badgeId)
          
          if (!badge) return null
          
          return (
            <div
              key={badgeId}
              className={`badge-item ${isUnlocked ? 'unlocked' : 'locked'}`}
              onMouseEnter={() => setHoveredBadge(badgeId)}
              onMouseLeave={() => setHoveredBadge(null)}
            >
              <div className="badge-icon">{badge.icon}</div>
              <div className="badge-name">{badge.name}</div>
              
              {/* Tooltip on hover */}
              {hoveredBadge === badgeId && (
                <div className="badge-tooltip">
                  <div className="badge-tooltip-content">
                    <div className="badge-tooltip-name">{badge.name}</div>
                    <div className="badge-tooltip-description">
                      {isUnlocked 
                        ? badge.description 
                        : `Unlock: ${badge.description}`}
                    </div>
                  </div>
                </div>
              )}
            </div>
          )
        })}
      </div>
      
      {!isExpanded && allBadgeIds.length > 3 && (
        <div className="badge-grid-footer">
          <button
            className="badge-grid-show-more"
            onClick={() => setIsExpanded(true)}
          >
            Show all badges ({allBadgeIds.length})
          </button>
        </div>
      )}
    </div>
  )
}

export default BadgeGrid









