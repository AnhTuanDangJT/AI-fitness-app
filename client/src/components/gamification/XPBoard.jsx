import React, { useState } from 'react'
import { useTranslation } from 'react-i18next'
import { calculateLevel, getTitle } from '../../utils/levels'
import '@/styles/gamification/xpBoard.css'

/**
 * XP Board Component - Compact Card
 * 
 * Slim, professional gamification display showing:
 * - Current Level & Title
 * - XP progress bar
 * - Current streak
 * - Link to view achievements
 * 
 * @param {number} xp - Current XP value
 * @param {number} currentStreakDays - Current streak in days
 * @param {function} onOpenDetails - Callback when "View achievements" is clicked
 */
function XPBoard({ xp = 0, currentStreakDays = 0, onOpenDetails }) {
  const { t } = useTranslation()
  const level = calculateLevel(xp)
  const titleKey = getTitle(level)
  const levelTitle = t(`levels.titles.${titleKey}`, { defaultValue: titleKey })
  const xpInLevel = xp % 100
  const progressPercent = (xpInLevel / 100) * 100

  const handleViewAchievements = () => {
    if (onOpenDetails) {
      onOpenDetails()
    }
  }

  return (
    <div className="xp-compact-card">
      <div className="xp-header">
        <div>
          <span className="xp-level">{t('gamification.level', { level })}</span>
          <span className="xp-title">{levelTitle}</span>
        </div>
        <div className="xp-streak">
          🔥 {currentStreakDays || 0}
        </div>
      </div>

      <div className="xp-bar">
        <div
          className="xp-bar-fill"
          style={{ width: `${progressPercent}%` }}
        />
      </div>

      <div className="xp-meta">
        <span>{t('gamification.xp', { current: xpInLevel })}</span>
        <button className="xp-link" onClick={handleViewAchievements}>
          {t('gamification.viewAchievements')}
        </button>
      </div>
    </div>
  )
}

export default XPBoard

