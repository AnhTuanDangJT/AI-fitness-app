import React from 'react'
import { useTranslation } from 'react-i18next'
import { motion } from 'framer-motion'
import { calculateLevel, getTitle } from '../../utils/levels'

function DailySummaryStrip({ gamificationStatus = null, calorieTarget = null, proteinTarget = null }) {
  const { t } = useTranslation()
  const xp = gamificationStatus?.xp ?? 0
  const currentStreakDays = gamificationStatus?.currentStreakDays ?? 0
  const level = calculateLevel(xp)
  const titleKey = getTitle(level)
  const levelTitle = t(`levels.titles.${titleKey}`, { defaultValue: titleKey })
  const notAvailableLabel = t('dashboard.notAvailable')

  const items = [
    {
      label: t('dashboard.levelLabel', { level }),
      value: `${xp}`,
      suffix: t('dashboard.xpUnit'),
      description: levelTitle,
    },
    {
      label: t('dashboard.streakLabel'),
      value: currentStreakDays,
      suffix: currentStreakDays === 1 ? t('dashboard.daySingular') : t('dashboard.dayPlural'),
      description: t('dashboard.keepGoing', { defaultValue: 'Keep the streak alive' }),
    },
    {
      label: t('dashboard.caloriesLabel'),
      value: calorieTarget ? Math.round(calorieTarget) : notAvailableLabel,
      suffix: t('dashboard.kcalPerDayShort'),
      description: t('dashboard.fuelToday', { defaultValue: 'Fuel for today' }),
    },
    {
      label: t('dashboard.proteinLabel'),
      value: proteinTarget ? Math.round(proteinTarget) : notAvailableLabel,
      suffix: t('dashboard.gPerDay'),
      description: t('dashboard.recoverySupport', { defaultValue: 'Recovery support' }),
    },
  ]

  return (
    <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
      {items.map((item, index) => (
        <motion.div
          key={item.label}
          initial={{ opacity: 0, y: 8 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ delay: index * 0.05 }}
          className="rounded-3xl border border-white/12 bg-base-900/70 p-6 backdrop-blur-md"
        >
          <p className="text-xs uppercase tracking-[0.3em] text-muted">{item.label}</p>
          <div className="mt-3 flex items-baseline gap-2">
            <span className="text-3xl font-semibold text-white">{item.value}</span>
            <span className="text-sm text-muted">{item.suffix}</span>
          </div>
          <p className="mt-3 text-sm text-white/70">{item.description}</p>
        </motion.div>
      ))}
    </div>
  )
}

export default DailySummaryStrip
