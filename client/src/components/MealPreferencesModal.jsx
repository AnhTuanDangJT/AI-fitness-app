import React, { useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import MealPreferences from '../pages/MealPreferences'
import './MealPreferencesModal.css'

function MealPreferencesModal({ onClose }) {
  const { t } = useTranslation()
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

  const handleOverlayClick = (e) => {
    if (e.target === e.currentTarget) {
      onClose()
    }
  }

  return (
    <div className="meal-preferences-modal-overlay" onClick={handleOverlayClick}>
      <div className="meal-preferences-modal" onClick={(e) => e.stopPropagation()}>
        <div className="meal-preferences-modal-header">
          <h2>{t('mealPlan.title')}</h2>
          <button 
            className="meal-preferences-modal-close" 
            onClick={onClose}
            aria-label={t('feedback.close')}
          >
            ×
          </button>
        </div>
        <div className="meal-preferences-modal-content">
          <MealPreferences onClose={onClose} />
        </div>
      </div>
    </div>
  )
}

export default MealPreferencesModal

