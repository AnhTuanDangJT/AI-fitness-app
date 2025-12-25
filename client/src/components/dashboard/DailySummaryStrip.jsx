import React from 'react'
import { useTranslation } from 'react-i18next'
import { motion } from 'framer-motion'
import clsx from 'clsx'
import { calculateLevel, getTitle } from '../../utils/levels'

const STRIP_VARIANTS = {
  overview: {
    background: 'bg-gradient-to-br from-indigo-500/10 via-base-900/60 to-transparent',
    border: 'border border-indigo-500/25',
    divider: 'divide-indigo-500/20',
    shadow: 'shadow-[0_30px_80px_rgba(67,56,202,0.35)]',
    halo: 'from-indigo-500/20 via-transparent to-transparent',
  },
  ai: {
    background: 'bg-gradient-to-br from-fuchsia-500/10 via-base-900/60 to-transparent',
    border: 'border border-fuchsia-500/25',
    divider: 'divide-fuchsia-500/20',
    shadow: 'shadow-[0_30px_80px_rgba(134,52,234,0.35)]',
    halo: 'from-fuchsia-500/20 via-transparent to-transparent',
  },
  health: {
    background: 'bg-gradient-to-br from-cyan-500/10 via-base-900/60 to-transparent',
    border: 'border border-cyan-500/25',
    divider: 'divide-cyan-500/20',
    shadow: 'shadow-[0_30px_80px_rgba(6,111,153,0.35)]',
    halo: 'from-cyan-500/20 via-transparent to-transparent',
  },
  nutrition: {
    background: 'bg-gradient-to-br from-emerald-500/10 via-base-900/60 to-transparent',
    border: 'border border-emerald-500/25',
    divider: 'divide-emerald-500/20',
    shadow: 'shadow-[0_30px_80px_rgba(16,112,83,0.35)]',
    halo: 'from-emerald-500/20 via-transparent to-transparent',
  },
}

function DailySummaryStrip({
  gamificationStatus = null,
  calorieTarget = null,
  proteinTarget = null,
  variant = 'overview',
}) {
  const { t } = useTranslation()
  const xp = gamificationStatus?.xp ?? 0
  const currentStreakDays = gamificationStatus?.currentStreakDays ?? 0
  const level = calculateLevel(xp)
  const titleKey = getTitle(level)
  const levelTitle = t(`levels.titles.${titleKey}`, { defaultValue: titleKey })
  const notAvailableLabel = t('dashboard.notAvailable')
  const streakUnit = currentStreakDays === 1 ? t('dashboard.daySingular') : t('dashboard.dayPlural')
  const accent = STRIP_VARIANTS[variant] || STRIP_VARIANTS.overview

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
      suffix: streakUnit,
      description: t('dashboard.keepGoing'),
    },
    {
      label: t('dashboard.caloriesLabel'),
      value: calorieTarget ? Math.round(calorieTarget) : notAvailableLabel,
      suffix: t('dashboard.kcalPerDayShort'),
      description: t('dashboard.fuelToday'),
    },
    {
      label: t('dashboard.proteinLabel'),
      value: proteinTarget ? Math.round(proteinTarget) : notAvailableLabel,
      suffix: t('dashboard.gPerDay'),
      description: t('dashboard.recoverySupport'),
    },
  ]

  return (
    <div
      className={clsx(
        'relative rounded-[28px] p-8 text-white backdrop-blur-xl overflow-hidden border',
        accent.background,
        accent.border,
        accent.shadow
      )}
    >
      <span
        aria-hidden
        className={clsx(
          'pointer-events-none absolute inset-0 -z-10 opacity-60 blur-3xl bg-gradient-to-br',
          accent.halo
        )}
      />
      <div
        className={clsx(
          'flex flex-col md:flex-row md:items-center md:gap-0',
          'divide-y md:divide-y-0 md:divide-x',
          accent.divider
        )}
      >
        {items.map((item, index) => (
          <motion.div
            key={item.label}
            initial={{ opacity: 0, y: 8 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: index * 0.05 }}
            className={clsx(
              'flex flex-1 flex-col gap-2 py-5',
              'md:px-8',
              index === 0 && 'pt-0 md:pl-0',
              index === items.length - 1 && 'pb-0 md:pr-0'
            )}
          >
            <p className="text-xs uppercase tracking-[0.3em] text-white/70">{item.label}</p>
            <div className="flex items-baseline gap-2">
              <span className="text-4xl font-semibold text-white">{item.value}</span>
              <span className="text-sm text-white/70">{item.suffix}</span>
            </div>
            <p className="text-sm text-white/80">{item.description}</p>
          </motion.div>
        ))}
      </div>
    </div>
  )
}

export default DailySummaryStrip
