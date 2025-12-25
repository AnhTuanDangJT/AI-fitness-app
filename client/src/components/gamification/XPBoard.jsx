import React from 'react'
import { useTranslation } from 'react-i18next'
import { motion } from 'framer-motion'
import clsx from 'clsx'
import { calculateLevel, getTitle } from '../../utils/levels'
import Button from '@/components/ui/Button'

function XPBoard({ xp = 0, currentStreakDays = 0, onOpenDetails, className = '' }) {
  const { t } = useTranslation()
  const level = calculateLevel(xp)
  const titleKey = getTitle(level)
  const levelTitle = t(`levels.titles.${titleKey}`, { defaultValue: titleKey })
  const xpInLevel = xp % 100
  const progressPercent = (xpInLevel / 100) * 100

  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      className={clsx(
        'rounded-3xl bg-gradient-to-br from-white/15 via-white/8 to-transparent p-6 text-white shadow-[0_25px_60px_rgba(2,6,23,0.35)] backdrop-blur-md',
        className
      )}
    >
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm uppercase tracking-[0.3em] text-muted">
            {t('gamification.level', { level })}
          </p>
          <p className="mt-1 text-2xl font-semibold text-white">{levelTitle}</p>
        </div>
        <div className="rounded-2xl border border-white/10 bg-white/5 px-4 py-2 text-right">
          <p className="text-[11px] uppercase tracking-[0.3em] text-white/60">{t('dashboard.streakLabel')}</p>
          <p className="text-sm font-semibold text-white">
            {currentStreakDays || 0}{' '}
            {currentStreakDays === 1 ? t('dashboard.daySingular') : t('dashboard.dayPlural')}
          </p>
        </div>
      </div>

      <div className="mt-6">
        <div className="h-2 w-full rounded-full bg-white/10">
          <div
            className="h-full rounded-full bg-gradient-to-r from-accent to-indigo-400 transition-all"
            style={{ width: `${progressPercent}%` }}
          />
        </div>
        <div className="mt-3 flex items-center justify-between text-sm text-muted">
          <span>{t('gamification.xp', { current: xpInLevel })}</span>
          <Button variant="ghost" size="sm" onClick={onOpenDetails}>
            {t('gamification.viewAchievements')}
          </Button>
        </div>
      </div>
    </motion.div>
  )
}

export default XPBoard

