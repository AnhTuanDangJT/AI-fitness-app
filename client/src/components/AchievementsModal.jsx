import React, { useEffect } from 'react'
import BadgeGrid from './gamification/BadgeGrid'
import './gamification/BadgePreview.css'

function AchievementsModal({ badges, onClose }) {
  // Handle ESC key to close modal
  useEffect(() => {
    const handleEsc = (e) => {
      if (e.key === 'Escape') {
        onClose()
      }
    }
    document.addEventListener('keydown', handleEsc)
    return () => document.removeEventListener('keydown', handleEsc)
  }, [onClose])

  return (
    <div className="badge-modal-overlay" onClick={onClose}>
      <div className="badge-modal" onClick={(e) => e.stopPropagation()}>
        <div className="badge-modal-header">
          <h2>Achievements</h2>
          <button 
            className="badge-modal-close" 
            onClick={onClose}
            aria-label="Close"
          >
            ×
          </button>
        </div>
        <div className="badge-modal-content">
          <BadgeGrid badges={badges || []} />
        </div>
      </div>
    </div>
  )
}

export default AchievementsModal

