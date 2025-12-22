import React from 'react'
import { useTranslation } from 'react-i18next'
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
  const { t } = useTranslation()
  // Safe defaults if data is not available
  const xp = gamificationStatus?.xp ?? 0
  const currentStreakDays = gamificationStatus?.currentStreakDays ?? 0
  const level = calculateLevel(xp)
  const titleKey = getTitle(level)
  const levelTitle = t(`levels.titles.${titleKey}`, { defaultValue: titleKey })
  const notAvailableLabel = t('dashboard.notAvailable')

  return (
    <div className="daily-summary-strip">
      {/* XP + Level */}
      <div className="summary-item">
        <div className="summary-label">{t('dashboard.levelLabel', { level })}</div>
        <div className="summary-value">
          {xp} <span className="summary-unit">{t('dashboard.xpUnit')}</span>
        </div>
        <div className="summary-subtitle">{levelTitle}</div>
      </div>

      {/* Current Streak */}
      <div className="summary-item">
        <div className="summary-label">{t('dashboard.streakLabel')}</div>
        <div className="summary-value">
          <span className="streak-emoji">🔥</span> {currentStreakDays}
        </div>
        <div className="summary-subtitle">
          {currentStreakDays === 1 ? t('dashboard.daySingular') : t('dashboard.dayPlural')}
        </div>
      </div>

      {/* Daily Calorie Target */}
      <div className="summary-item">
        <div className="summary-label">{t('dashboard.caloriesLabel')}</div>
        <div className="summary-value">
          {calorieTarget ? Math.round(calorieTarget) : notAvailableLabel}
        </div>
        <div className="summary-subtitle">{t('dashboard.kcalPerDayShort')}</div>
      </div>

      {/* Protein Target */}
      <div className="summary-item">
        <div className="summary-label">{t('dashboard.proteinLabel')}</div>
        <div className="summary-value">
          {proteinTarget ? Math.round(proteinTarget) : notAvailableLabel}
        </div>
        <div className="summary-subtitle">{t('dashboard.gPerDay')}</div>
      </div>
    </div>
  )
}

export default DailySummaryStrip








