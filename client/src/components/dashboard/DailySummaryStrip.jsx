import React from 'react'
import { useTranslation } from 'react-i18next'
import { motion } from 'framer-motion'
import clsx from 'clsx'
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
    <div className="rounded-[28px] bg-gradient-to-br from-white/15 via-white/8 to-transparent p-8 text-white shadow-[0_25px_60px_rgba(2,6,23,0.4)] backdrop-blur">
      <div className="flex flex-col divide-y divide-white/10 md:flex-row md:items-center md:divide-y-0 md:divide-x md:gap-0">
        {items.map((item, index) => (
          <motion.div
            key={item.label}
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: index * 0.05 }}
            className={clsx('flex flex-1 flex-col gap-2 py-5', 'md:px-8', index === 0 && 'pt-0 md:pl-0', index === items.length - 1 && 'pb-0 md:pr-0')}
          >
            <p className="text-xs uppercase tracking-[0.3em] text-white/60">{item.label}</p>
            <div className="flex items-baseline gap-2">
              <span className="text-4xl font-semibold text-white">{item.value}</span>
              <span className="text-sm text-white/60">{item.suffix}</span>
            </div>
            <p className="text-sm text-white/70">{item.description}</p>
          </motion.div>
        ))}
      </div>
    </div>
  )
}

export default DailySummaryStrip
