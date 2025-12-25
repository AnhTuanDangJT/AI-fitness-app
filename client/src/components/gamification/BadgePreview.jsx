import React, { useState } from 'react'
import BadgeGrid from './BadgeGrid'
import './BadgePreview.css'

/**
 * Badge Preview Component
 * 
 * Shows a preview of up to 4 badges with a "+X more" indicator.
 * Clicking opens a modal with the full badge grid.
 * 
 * @param {Array<string>} badges - Array of unlocked badge IDs
 */
function BadgePreview({ badges = [] }) {
  const [isModalOpen, setIsModalOpen] = useState(false)

  // Badge definitions (same as BadgeGrid)
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

  // Get all badge IDs in order
  const allBadgeIds = Object.keys(BADGE_DEFINITIONS)
  
  // Show first 4 badges in preview
  const previewBadges = allBadgeIds.slice(0, 4)
  const remainingCount = Math.max(0, allBadgeIds.length - 4)

  const handleOpenModal = () => {
    setIsModalOpen(true)
  }

  const handleCloseModal = () => {
    setIsModalOpen(false)
  }

  return (
    <>
      <div className="badge-preview-container">
        <div className="badge-preview-header">
          <h3 className="badge-preview-title">Badges</h3>
        </div>
        <div className="badge-preview-grid">
          {previewBadges.map((badgeId) => {
            const badge = BADGE_DEFINITIONS[badgeId]
            const isUnlocked = badges.includes(badgeId)
            
            if (!badge) return null
            
            return (
              <div
                key={badgeId}
                className={`badge-preview-item ${isUnlocked ? 'unlocked' : 'locked'}`}
              >
                <div className="badge-preview-icon">{badge.icon}</div>
                <div className="badge-preview-name">{badge.name}</div>
              </div>
            )
          })}
        </div>
        {remainingCount > 0 && (
          <button 
            className="badge-preview-more"
            onClick={handleOpenModal}
          >
            +{remainingCount} more
          </button>
        )}
      </div>

      {isModalOpen && (
        <div className="badge-modal-overlay" onClick={handleCloseModal}>
          <div className="badge-modal" onClick={(e) => e.stopPropagation()}>
            <div className="badge-modal-header">
              <h2>All Badges</h2>
              <button 
                className="badge-modal-close" 
                onClick={handleCloseModal}
                aria-label="Close"
              >
                ×
              </button>
            </div>
            <div className="badge-modal-content">
              <BadgeGrid badges={badges} />
            </div>
          </div>
        </div>
      )}
    </>
  )
}

export default BadgePreview









