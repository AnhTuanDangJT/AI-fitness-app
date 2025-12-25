import React, { useState, useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { motion } from 'framer-motion'
import clsx from 'clsx'
import api, { userAPI } from '../services/api'
import { logout } from '../utils/auth'
import { gamificationAPI } from '../services/gamificationApi'
import { generateFitnessProfilePdf } from '../utils/pdfProfile'
import { ERROR_MESSAGES } from '../config/constants'
import DailyChallenges from '../components/gamification/DailyChallenges'
import XPBoard from '../components/gamification/XPBoard'
import DailySummaryStrip from '../components/dashboard/DailySummaryStrip'
import FeedbackModal from '../components/FeedbackModal'
import AchievementsModal from '../components/AchievementsModal'
import MealPreferencesModal from '../components/MealPreferencesModal'
import AppNavbar from '@/components/layout/AppNavbar'
import Button from '@/components/ui/Button'
import Skeleton from '@/components/ui/Skeleton'
import EmptyState from '@/components/ui/EmptyState'
import { resolveTextWithFallback, humanizeTranslationKey } from '@/utils/i18nHelpers'

const BMI_SEGMENTS = [
  {
    key: 'underweight',
    labelKey: 'dashboard.underweight',
    min: 14,
    max: 18.5,
    rangeKey: 'dashboard.bmiRangeUnderweight',
  },
  {
    key: 'normal',
    labelKey: 'dashboard.normal',
    min: 18.5,
    max: 25,
    rangeKey: 'dashboard.bmiRangeNormal',
  },
  {
    key: 'overweight',
    labelKey: 'dashboard.overweight',
    min: 25,
    max: 30,
    rangeKey: 'dashboard.bmiRangeOverweight',
  },
  {
    key: 'obese',
    labelKey: 'dashboard.obese',
    min: 30,
    max: 45,
    rangeKey: 'dashboard.bmiRangeObese',
  },
]

const BMI_MIN = BMI_SEGMENTS[0].min
const BMI_MAX = BMI_SEGMENTS[BMI_SEGMENTS.length - 1].max

const alignmentClasses = {
  left: 'items-start text-left',
  center: 'items-center text-center',
  right: 'items-end text-right',
}

const segmentGradients = {
  underweight: 'from-sky-400/80 to-sky-500/70',
  normal: 'from-emerald-400/80 to-emerald-500/70',
  overweight: 'from-amber-400/80 to-amber-500/70',
  obese: 'from-rose-500/80 to-rose-600/70',
}

const SECTION_ACCENTS = {
  overview: {
    label: 'text-indigo-200',
    dot: 'bg-indigo-300',
    title: 'text-white',
    description: 'text-white/70',
    halo: 'from-indigo-500/15 via-transparent to-transparent',
    panel: 'border border-indigo-500/20 bg-gradient-to-br from-indigo-500/10 via-base-900/60 to-transparent shadow-[0_30px_80px_rgba(67,56,202,0.35)]',
    cardBorder: 'border border-indigo-500/20',
    badge: 'bg-indigo-400/20 text-indigo-100',
  },
  ai: {
    label: 'text-fuchsia-100',
    dot: 'bg-fuchsia-400',
    title: 'text-white',
    description: 'text-white/70',
    halo: 'from-fuchsia-500/15 via-transparent to-transparent',
    panel: 'border border-fuchsia-500/20 bg-gradient-to-br from-fuchsia-500/10 via-base-900/60 to-transparent shadow-[0_30px_80px_rgba(134,52,234,0.35)]',
    cardBorder: 'border border-fuchsia-500/20',
    badge: 'bg-fuchsia-400/20 text-fuchsia-100',
  },
  health: {
    label: 'text-cyan-100',
    dot: 'bg-cyan-300',
    title: 'text-white',
    description: 'text-white/70',
    halo: 'from-cyan-500/15 via-transparent to-transparent',
    panel: 'border border-cyan-500/20 bg-gradient-to-br from-cyan-500/10 via-base-900/60 to-transparent shadow-[0_30px_80px_rgba(6,111,153,0.35)]',
    cardBorder: 'border border-cyan-500/20',
    badge: 'bg-cyan-400/20 text-cyan-100',
  },
  nutrition: {
    label: 'text-emerald-100',
    dot: 'bg-emerald-400',
    title: 'text-white',
    description: 'text-white/70',
    halo: 'from-emerald-500/15 via-transparent to-transparent',
    panel: 'border border-emerald-500/20 bg-gradient-to-br from-emerald-500/10 via-base-900/60 to-transparent shadow-[0_30px_80px_rgba(16,112,83,0.35)]',
    cardBorder: 'border border-emerald-500/20',
    badge: 'bg-emerald-400/20 text-emerald-100',
  },
  default: {
    label: 'text-white/70',
    dot: 'bg-white/60',
    title: 'text-white',
    description: 'text-white/70',
    halo: 'from-white/10 via-transparent to-transparent',
    panel: 'border border-white/10 bg-white/5 shadow-[0_30px_80px_rgba(2,6,23,0.35)]',
    cardBorder: 'border border-white/10',
    badge: 'bg-white/10 text-white/70',
  },
}

const sectionContainerBaseClasses =
  'relative overflow-hidden rounded-[40px] bg-white/5 p-6 sm:p-10 backdrop-blur-xl'

const getSectionContainerClasses = (variant) => clsx(sectionContainerBaseClasses, SECTION_ACCENTS[variant]?.panel)

const SectionHeader = ({ label, title, description, variant = 'default' }) => {
  const accent = SECTION_ACCENTS[variant] || SECTION_ACCENTS.default
  return (
    <div className="relative space-y-2">
      <div className="inline-flex items-center gap-3 text-xs uppercase tracking-[0.35em]">
        <span className={clsx('h-2 w-2 rounded-full', accent.dot)} />
        <span className={clsx('font-semibold', accent.label)}>{label}</span>
      </div>
      <h2 className={clsx('text-3xl font-semibold leading-tight', accent.title)}>{title}</h2>
      {description && <p className={clsx('text-base', accent.description)}>{description}</p>}
      <span
        aria-hidden
        className={clsx(
          'pointer-events-none absolute inset-0 -z-10 opacity-60 blur-3xl',
          'bg-gradient-to-r',
          accent.halo
        )}
      />
    </div>
  )
}

// IMPORTANT: Keep translation maps declared ahead of their usage to avoid
// Temporal Dead Zone errors that previously crashed the dashboard at runtime.
const BMI_CATEGORY_TRANSLATIONS = {
  Underweight: 'dashboard.underweight',
  Normal: 'dashboard.normal',
  Overweight: 'dashboard.overweight',
  'Obese (Class I)': 'dashboard.obeseClassI',
  'Obese (Class II)': 'dashboard.obeseClassII',
  'Obese (Class III)': 'dashboard.obeseClassIII',
}

const WHR_RISK_TRANSLATIONS = {
  'Good condition': 'dashboard.goodCondition',
  'At risk': 'dashboard.atRisk',
}

function Dashboard() {
  const { t, i18n } = useTranslation()
  const resolveDashboardText = (key, fallback) => resolveTextWithFallback(t, key, fallback)
  const [analysis, setAnalysis] = useState(null)
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [gamificationStatus, setGamificationStatus] = useState(null)
  const [isFeedbackModalOpen, setIsFeedbackModalOpen] = useState(false)
  const [isAchievementsModalOpen, setIsAchievementsModalOpen] = useState(false)
  const [isMealPlannerOpen, setIsMealPlannerOpen] = useState(false)
  const [headerAlignment, setHeaderAlignment] = useState(() => localStorage.getItem('dashboardHeaderAlign') || 'center')
  const [expandedInsights, setExpandedInsights] = useState(['targets'])
  const navigate = useNavigate()
  const loadingCompletedRef = useRef(false)

  // Helper function to handle 401/403 errors
  const handleAuthError = (err) => {
    const status = err?.response?.status
    if (status === 401 || status === 403) {
      console.error('Authentication failed:', err)
      logout()
      navigate('/login')
      return true
    }
    return false
  }

  useEffect(() => {
    let timeoutId = null
    let isMounted = true
    loadingCompletedRef.current = false

    // Timeout safety mechanism - if loading hasn't completed after 8 seconds, redirect to login
    timeoutId = setTimeout(() => {
      if (isMounted && !loadingCompletedRef.current) {
        console.error('Dashboard loading timeout - redirecting to login')
        logout()
        navigate('/login')
        setLoading(false)
      }
    }, 8000)

    // First check if profile is complete before attempting to fetch data
    const checkProfileAndFetch = async () => {
      try {
        const profileCheck = await userAPI.checkProfileComplete()
        
        if (!profileCheck.success || !profileCheck.data?.isComplete) {
          // Profile is incomplete, redirect to profile setup
          if (isMounted) {
            loadingCompletedRef.current = true
            navigate('/profile-setup', { state: { incompleteProfile: true } })
          }
          return
        }
        
        // Profile is complete, proceed with fetching data
        await fetchFullAnalysis()
        await fetchProfile()
        await fetchGamificationStatus()
      } catch (err) {
        // Handle 401/403 explicitly
        if (handleAuthError(err)) {
          loadingCompletedRef.current = true
          return
        }
        
        // If check fails, assume incomplete and redirect
        console.error('Error checking profile completeness:', err)
        if (isMounted) {
          loadingCompletedRef.current = true
          navigate('/profile-setup', { state: { incompleteProfile: true } })
        }
      } finally {
        if (isMounted) {
          loadingCompletedRef.current = true
          setLoading(false)
        }
        // Clear timeout if we successfully completed loading
        if (timeoutId) {
          clearTimeout(timeoutId)
        }
      }
    }
    
    checkProfileAndFetch()

    // Cleanup function
    return () => {
      isMounted = false
      if (timeoutId) {
        clearTimeout(timeoutId)
      }
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [navigate]) // Only run on mount - do NOT add fetchGamificationStatus or other dependencies

  useEffect(() => {
    localStorage.setItem('dashboardHeaderAlign', headerAlignment)
  }, [headerAlignment])

  const cycleHeaderAlignment = () => {
    const order = ['left', 'center', 'right']
    const currentIndex = order.indexOf(headerAlignment)
    const nextAlignment = order[(currentIndex + 1) % order.length]
    setHeaderAlignment(nextAlignment)
  }

  const toggleInsightPanel = (panelId) => {
    setExpandedInsights((prev) =>
      prev.includes(panelId) ? prev.filter((id) => id !== panelId) : [...prev, panelId]
    )
  }

  const fetchFullAnalysis = async () => {
    try {
      setLoading(true)
      setError(null)
      const response = await api.get('/profile/full-analysis')
      if (response.data.success) {
        setAnalysis(response.data.data)
      } else {
        setError(response.data.message || ERROR_MESSAGES.ANALYSIS_LOAD_FAILED)
      }
    } catch (err) {
      // Handle 401/403 explicitly
      if (handleAuthError(err)) {
        return
      }
      
      // Use generic error message from API interceptor
      const errorMessage = err.genericMessage || ERROR_MESSAGES.ANALYSIS_LOAD_FAILED
      
      // If profile is incomplete, automatically redirect to profile setup
      if (errorMessage.includes('Profile incomplete') || errorMessage.includes('complete your profile')) {
        navigate('/profile-setup', { state: { incompleteProfile: true } })
        return
      }
      
      setError(errorMessage)
      console.error('Error fetching full analysis:', err)
    } finally {
      setLoading(false)
    }
  }

  const fetchProfile = async () => {
    try {
      const response = await api.get('/user/profile')
      if (response.data.success) {
        setProfile(response.data.data)
      }
    } catch (err) {
      // Handle 401/403 explicitly
      if (handleAuthError(err)) {
        return
      }
      console.error('Error fetching profile:', err)
    }
  }

  const fetchGamificationStatus = async () => {
    try {
      // ALWAYS forceRefresh on Dashboard load to ensure fresh data
      // This prevents stale cache issues after page refreshes, backend restarts, or timezone changes
      // Frontend is DISPLAY-ONLY - backend is the source of truth for all gamification data
      const result = await gamificationAPI.getGamificationStatus(true)
      if (result.type === 'SUCCESS') {
        setGamificationStatus(result.data)
      } else {
        // If API returns error, set safe defaults to prevent UI crash
        setGamificationStatus({
          xp: 0,
          currentStreakDays: 0,
          longestStreakDays: 0,
          badges: []
        })
      }
    } catch (err) {
      // Handle 401/403 explicitly - but don't break rendering
      if (handleAuthError(err)) {
        // Set safe defaults even on auth error to keep UI functional
        setGamificationStatus({
          xp: 0,
          currentStreakDays: 0,
          longestStreakDays: 0,
          badges: []
        })
        return
      }
      // For other errors, set safe defaults to prevent UI crash
      setGamificationStatus({
        xp: 0,
        currentStreakDays: 0,
        longestStreakDays: 0,
        badges: []
      })
    }
  }

  const downloadProfilePdf = async () => {
    try {
      const { data: apiResponse } = await api.get('/profile/export')
      if (!apiResponse?.success || !apiResponse?.data) {
        throw new Error(apiResponse?.message || ERROR_MESSAGES.EXPORT_FAILED)
      }

      await generateFitnessProfilePdf({
        profileData: apiResponse.data,
        t
      })
    } catch (err) {
      console.error('EXPORT ERROR:', err)
      setError(err.message || err.toString() || ERROR_MESSAGES.EXPORT_FAILED)
    }
  }

  const proteinTargetValue = analysis?.proteinTarget ?? null

  const getHealthTip = () => {
    if (!analysis) return null

    const tips = []
    
    // BMI tips
    const bmiCategory = analysis?.bmiCategory
    
    if (bmiCategory === 'Underweight') {
      tips.push(t('healthTips.underweightTip'))
    } else if (bmiCategory === 'Overweight' || bmiCategory?.startsWith('Obese')) {
      tips.push(t('healthTips.overweightTip'))
    } else if (bmiCategory === 'Normal') {
      tips.push(t('healthTips.normalWeightTip'))
    }

    // WHR tips
    if (analysis.whrRisk === 'At risk') {
      tips.push(t('healthTips.whrAtRiskTip'))
    } else {
      tips.push(t('healthTips.whrGoodTip'))
    }

    // TDEE and calories tips
    if (analysis.goalCalories < analysis.tdee - 300) {
      tips.push(t('healthTips.calorieDeficitTip'))
    } else if (analysis.goalCalories > analysis.tdee + 200) {
      tips.push(t('healthTips.calorieSurplusTip'))
    } else {
      tips.push(t('healthTips.calorieBalancedTip'))
    }

    // Protein tips
    if (proteinTargetValue) {
      tips.push(t('healthTips.proteinTip', { protein: Math.round(proteinTargetValue) }))
    }

    return tips[Math.floor(Math.random() * tips.length)] || t('healthTips.defaultTip')
  }

  const notAvailableLabel = resolveDashboardText('dashboard.notAvailable', 'Not available')
  const formatValueWithUnit = (value, unit) => {
    if (value === null || value === undefined || value === '') {
      return notAvailableLabel
    }
    const numericValue = typeof value === 'number' ? value : parseFloat(value)
    const displayValue = Number.isFinite(numericValue) ? Math.round(numericValue * 10) / 10 : value
    return unit ? `${displayValue} ${unit}` : displayValue
  }
  const getLocalizedCategoricalValue = (value, map) => {
    if (!value) return notAvailableLabel
    const translationKey = map[value]
    return translationKey ? t(translationKey) : value
  }
  const bmiCategoryLabel = getLocalizedCategoricalValue(analysis?.bmiCategory, BMI_CATEGORY_TRANSLATIONS)
  const whrValue = (() => {
    if (analysis?.whr === null || analysis?.whr === undefined) return null
    if (typeof analysis.whr === 'number' && Number.isFinite(analysis.whr)) {
      return analysis.whr
    }
    const parsed = parseFloat(analysis.whr)
    return Number.isFinite(parsed) ? parsed : null
  })()
  const whrRiskLabel = getLocalizedCategoricalValue(analysis?.whrRisk, WHR_RISK_TRANSLATIONS)
  const isWhrGoodCondition = analysis?.whrRisk === 'Good condition'

  const bmiValue = (() => {
    if (analysis?.bmi === null || analysis?.bmi === undefined) return null
    if (typeof analysis.bmi === 'number' && Number.isFinite(analysis.bmi)) {
      return analysis.bmi
    }
    const parsed = parseFloat(analysis.bmi)
    return Number.isFinite(parsed) ? parsed : null
  })()

  const clampBmi = (value) => Math.min(BMI_MAX, Math.max(BMI_MIN, value))
  const bmiIndicatorPosition =
    bmiValue === null ? '0%' : `${((clampBmi(bmiValue) - BMI_MIN) / (BMI_MAX - BMI_MIN)) * 100}%`
  const activeBmiSegmentKey =
    bmiValue === null
      ? null
      : BMI_SEGMENTS.find((segment, index) => {
          if (index === BMI_SEGMENTS.length - 1) {
            return bmiValue >= segment.min
          }
          return bmiValue < segment.max
        })?.key ?? BMI_SEGMENTS[BMI_SEGMENTS.length - 1].key
  const bmiIndicatorDisplay = bmiValue === null ? '—' : bmiValue.toFixed(1)
  const getSegmentWidth = (segment) => ((segment.max - segment.min) / (BMI_MAX - BMI_MIN)) * 100

  const handleLanguageToggle = () => {
    const newLang = i18n.language === 'en' ? 'vi' : 'en'
    i18n.changeLanguage(newLang)
  }

  const handleLogout = () => {
    if (confirm(t('dashboard.logOutConfirm'))) {
      logout()
      navigate('/')
    }
  }

  const openAiCoach = () => {
    window.open('/ai-coach', '_blank')
  }

  const headerAlignmentLabel = `${headerAlignment === 'left' ? '⇤' : headerAlignment === 'center' ? '⇆' : '⇥'} ${resolveDashboardText(
    'dashboard.headerAlignToggle',
    'Cycle header alignment'
  )}`
  const greetingAlignment = alignmentClasses[headerAlignment] || alignmentClasses.center

  const energyCards = [
    {
      label: t('dashboard.bmr'),
      value: analysis?.bmr ? Math.round(analysis.bmr) : notAvailableLabel,
      description: t('dashboard.bmrDescription'),
    },
    {
      label: t('dashboard.tdee'),
      value: analysis?.tdee ? Math.round(analysis.tdee) : notAvailableLabel,
      description: t('dashboard.tdeeDescription'),
    },
    {
      label: t('dashboard.goalCalories'),
      value: analysis?.goalCalories ? Math.round(analysis.goalCalories) : notAvailableLabel,
      description: t('dashboard.goalCaloriesDescription'),
    },
  ]

  const bodyStatsItems = [
    { label: t('dashboard.height'), value: formatValueWithUnit(profile?.height, t('dashboard.cm')) },
    { label: t('dashboard.weight'), value: formatValueWithUnit(profile?.weight, t('dashboard.kg')) },
    { label: t('dashboard.waist'), value: formatValueWithUnit(profile?.waist, t('dashboard.cm')) },
    { label: t('dashboard.hip'), value: formatValueWithUnit(profile?.hip, t('dashboard.cm')) },
    {
      label: t('dashboard.bmrValue'),
      value: analysis?.bmr ? `${Math.round(analysis.bmr)} ${t('dashboard.kcalPerDayShort')}` : notAvailableLabel,
    },
    {
      label: t('dashboard.tdeeValue'),
      value: analysis?.tdee ? `${Math.round(analysis.tdee)} ${t('dashboard.kcalPerDayShort')}` : notAvailableLabel,
    },
  ]

  const nutritionAccent = SECTION_ACCENTS.nutrition
  const insightPanels = [
    {
      id: 'targets',
      title: resolveDashboardText('dashboard.nutritionHub', 'Energy Targets'),
      description: resolveDashboardText('dashboard.nutritionHubSubtitle', 'Daily calorie and macro focus.'),
      content: (
        <div className="space-y-7 text-white">
            <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {energyCards.map((card) => (
              <div
                key={card.label}
                  className="rounded-2xl border border-white/10 bg-gradient-to-b from-white/10 via-white/5 to-transparent p-5 shadow-[0_20px_45px_rgba(2,6,23,0.35)]"
              >
                <p className="text-xs uppercase tracking-[0.3em] text-white/60">{card.label}</p>
                <p className="mt-3 text-2xl font-semibold text-white">{card.value}</p>
                <p className="text-xs text-white/50">{t('dashboard.kcalPerDay')}</p>
                <p className="mt-3 text-sm text-white/65 whitespace-pre-line">{card.description}</p>
              </div>
            ))}
          </div>
          <div className="rounded-3xl bg-gradient-to-br from-white/12 via-white/6 to-transparent p-7 text-white shadow-[0_25px_60px_rgba(2,6,23,0.4)]">
            <p className="text-xs uppercase tracking-[0.3em] text-white/60">{t('dashboard.dailyProteinTarget')}</p>
            <p className="mt-3 text-3xl font-semibold">
              {proteinTargetValue ? Math.round(proteinTargetValue) : notAvailableLabel}
              <span className="ml-2 text-base text-white/60">{t('dashboard.gramsPerDay')}</span>
            </p>
            <div className="mt-6 h-2 w-full rounded-full bg-white/10">
              <div
                className="h-full rounded-full bg-accent/80"
                style={{ width: `${Math.min(100, ((proteinTargetValue || 0) / 200) * 100)}%` }}
              />
            </div>
            <p className="mt-4 text-sm text-white/70">{t('dashboard.proteinTip')}</p>
          </div>
        </div>
      ),
    },
    {
      id: 'recommendations',
      title: resolveDashboardText('dashboard.healthRecommendations', 'Health Recommendations'),
      description: resolveDashboardText(
        'dashboard.healthRecommendationsSubtitle',
        'Clinical-style insights prioritized for you.'
      ),
      content: <HealthRecommendations analysis={analysis} profile={profile} />,
    },
    {
      id: 'tips',
      title: resolveDashboardText('dashboard.healthTips', 'Health Tips'),
      description: resolveDashboardText('dashboard.healthTipsSubtitle', 'Quick prompts to keep momentum.'),
      content: (
        <div className="flex flex-col gap-4 rounded-3xl bg-gradient-to-br from-white/12 via-white/6 to-transparent p-6 text-white shadow-[0_20px_45px_rgba(2,6,23,0.35)]">
          <p className="text-xs uppercase tracking-[0.3em] text-white/60">
            {resolveDashboardText('dashboard.healthInsightTag', 'Health insight')}
          </p>
          <p className="text-lg font-semibold">{getHealthTip()}</p>
          <Button variant="secondary" size="sm" onClick={() => setIsFeedbackModalOpen(true)}>
            {t('dashboard.feedback')}
          </Button>
        </div>
      ),
    },
  ]

  if (loading) {
    return (
      <div className="px-4 py-12 lg:px-6">
        <div className="mx-auto flex w-full max-w-6xl flex-col gap-6">
          <Skeleton className="h-24 w-full" />
          <div className="grid gap-4 md:grid-cols-2">
            <Skeleton className="h-32 w-full" />
            <Skeleton className="h-32 w-full" />
          </div>
          <Skeleton className="h-40 w-full" />
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="px-4 py-12 lg:px-6">
        <EmptyState
          title={t('dashboard.error')}
          description={error}
          actionLabel={t('dashboard.retry')}
          onAction={fetchFullAnalysis}
          secondaryActionLabel={t('dashboard.completeProfileSetup')}
          onSecondaryAction={() => navigate('/profile-setup')}
        />
      </div>
    )
  }

  if (!analysis) {
    return (
      <div className="px-4 py-12 lg:px-6">
        <EmptyState
          title={t('dashboard.noAnalysisData')}
          description={t('dashboard.completeProfileToSeeAnalysis')}
          actionLabel={t('dashboard.completeProfileSetup')}
          onAction={() => navigate('/profile-setup')}
          secondaryActionLabel={t('dashboard.retry')}
          onSecondaryAction={fetchFullAnalysis}
        />
      </div>
    )
  }

  return (
    <div className="relative z-10 min-h-screen px-4 pb-16 pt-6 lg:px-6">
      <AppNavbar
        language={i18n.language}
        onToggleLanguage={handleLanguageToggle}
        onAlignToggle={cycleHeaderAlignment}
        headerAlignmentLabel={headerAlignmentLabel}
        onEditProfile={() => navigate('/profile/edit')}
        onDownloadPdf={downloadProfilePdf}
        onFeedback={() => setIsFeedbackModalOpen(true)}
        onLogout={handleLogout}
        brandTitle={resolveDashboardText('dashboard.title', 'AI Fitness')}
        brandSubtitle={resolveDashboardText('dashboard.brandSubtitle', 'Personal Health HQ')}
        languageToggleLabel={resolveDashboardText('dashboard.toggleLanguage', 'Toggle language')}
        labels={{
          feedback: resolveDashboardText('dashboard.feedback', 'Feedback'),
          export: resolveDashboardText('dashboard.downloadPdf', 'Download PDF'),
          edit: resolveDashboardText('dashboard.editProfile', 'Edit profile'),
          logout: resolveDashboardText('dashboard.logOut', 'Log out'),
        }}
      />

      <main className="mx-auto mt-8 flex w-full max-w-6xl flex-col gap-16">
        <section className="space-y-8">
          <SectionHeader
            label={resolveDashboardText('dashboard.overviewLabel', 'Executive Summary')}
            title={resolveDashboardText('dashboard.overviewTitle', "Today's Health Overview")}
            description={resolveDashboardText('dashboard.overviewSubtitle', 'Your key vitals and guidance at a glance.')}
            variant="overview"
          />
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            className={clsx(getSectionContainerClasses('overview'), 'space-y-6')}
          >
            <div className={clsx('flex flex-col gap-3 text-white', greetingAlignment)}>
              <p className="text-xs uppercase tracking-[0.4em] text-white/60">
                {resolveDashboardText('dashboard.overview', 'Overview')}
              </p>
              <h1 className="text-4xl font-semibold leading-tight">
                {profile?.name ? t('dashboard.greeting', { name: profile.name }) : t('dashboard.greetingDefault')}
              </h1>
              <p className="max-w-2xl text-base text-white/70">
                {resolveDashboardText('dashboard.healthTipsSubtitle', 'Proactive insights to stay on track.')}
              </p>
            </div>
            <DailySummaryStrip
              gamificationStatus={gamificationStatus}
              calorieTarget={analysis?.goalCalories}
              proteinTarget={analysis?.proteinTarget}
              variant="overview"
            />
          </motion.div>
        </section>

        <section className="space-y-8">
          <SectionHeader
            label={resolveDashboardText('dashboard.aiGuidanceLabel', 'Guided Support')}
            title={resolveDashboardText('dashboard.aiGuidanceTitle', 'AI Guidance Center')}
            description={resolveDashboardText(
              'dashboard.aiGuidanceSubtitle',
              'Personal coaching, streaks, and intelligent nudges.'
            )}
            variant="ai"
          />
          <div className={clsx(getSectionContainerClasses('ai'), 'space-y-6')}>
            <div className="flex flex-wrap items-center justify-between gap-3 text-xs uppercase tracking-[0.3em] text-white/60">
              <span>{t('dashboard.aiPoweredLabel')}</span>
              <span
                className={clsx(
                  'rounded-full px-4 py-1 text-[11px] font-semibold',
                  SECTION_ACCENTS.ai?.badge
                )}
              >
                {t('dashboard.aiPoweredTag')}
              </span>
            </div>
            <div className="grid gap-6 lg:grid-cols-[1.2fr_0.8fr]">
              <div className="flex flex-col gap-6">
                <div className="rounded-3xl bg-white/5 p-6 text-white shadow-[inset_0_0_0_1px_rgba(255,255,255,0.04)]">
                  <div className="space-y-3">
                    <div>
                      <p className="text-xs uppercase tracking-[0.3em] text-white/50">{t('dashboard.aiCoach')}</p>
                      <h3 className="mt-2 text-2xl font-semibold">{t('dashboard.aiCoachHeadline')}</h3>
                      <p className="mt-3 text-sm text-white/70">{t('dashboard.aiCoachDescription')}</p>
                    </div>
                    <span
                      aria-hidden
                      className="inline-flex h-0.5 w-full rounded-full bg-gradient-to-r from-white/40 via-white/20 to-transparent"
                    />
                  </div>
                  <p className="mt-4 text-sm text-white/65">{t('dashboard.aiFeaturesSubtitle')}</p>
                  <div className="mt-6 flex flex-wrap gap-3">
                    <Button onClick={openAiCoach}>{t('dashboard.aiCoach')}</Button>
                    <Button variant="ghost" size="sm" onClick={() => setIsFeedbackModalOpen(true)}>
                      {t('dashboard.feedback')}
                    </Button>
                  </div>
                </div>
                {gamificationStatus && (
                  <XPBoard
                    xp={gamificationStatus.xp ?? 0}
                    currentStreakDays={gamificationStatus.currentStreakDays ?? 0}
                    onOpenDetails={() => setIsAchievementsModalOpen(true)}
                  />
                )}
              </div>
              <div className="flex flex-col gap-6">
                <div className="rounded-3xl bg-white/5 p-6 text-white shadow-[inset_0_0_0_1px_rgba(255,255,255,0.04)]">
                  <div className="space-y-3">
                    <div>
                      <p className="text-xs uppercase tracking-[0.3em] text-white/50">{t('dashboard.aiMealPlanner')}</p>
                      <h3 className="mt-2 text-xl font-semibold">{t('dashboard.aiMealPlannerDescription')}</h3>
                      <p className="mt-3 text-sm text-white/70">{t('dashboard.nutritionHubSubtitle')}</p>
                    </div>
                    <span
                      aria-hidden
                      className="inline-flex h-0.5 w-full rounded-full bg-gradient-to-r from-white/40 via-white/20 to-transparent"
                    />
                  </div>
                  <Button className="mt-6" variant="secondary" onClick={() => setIsMealPlannerOpen(true)}>
                    {t('dashboard.aiMealPlanner')}
                  </Button>
                </div>
                <div className="rounded-3xl bg-gradient-to-b from-white/12 to-white/5 p-6 text-white shadow-[0_20px_45px_rgba(2,6,23,0.35)]">
                  <p className="text-xs uppercase tracking-[0.3em] text-white/60">{t('gamification.dailyChallenges')}</p>
                  <div className="mt-4">
                    <DailyChallenges />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section className="space-y-8">
          <SectionHeader
            label={resolveDashboardText('dashboard.healthMetricsLabel', 'Core Metrics')}
            title={resolveDashboardText('dashboard.healthMetricsTitle', 'Body Metrics & Ratios')}
            description={resolveDashboardText(
              'dashboard.healthMetricsSubtitle',
              'BMI, WHR, and foundational measurements.'
            )}
            variant="health"
          />
          <div className={clsx(getSectionContainerClasses('health'), 'space-y-6')}>
            <div className="grid gap-6 md:grid-cols-2">
              <article className="rounded-3xl bg-white/5 p-6 text-white shadow-[inset_0_0_0_1px_rgba(255,255,255,0.04)]">
                <p className="text-xs uppercase tracking-[0.3em] text-white/60">{t('dashboard.bmi')}</p>
                <p className="mt-1 text-sm text-white/60">{t('dashboard.bmiSubtitle')}</p>
                <div className="mt-6 flex items-baseline gap-4">
                  <span className="text-5xl font-semibold text-white">
                    {bmiValue !== null ? bmiValue.toFixed(1) : notAvailableLabel}
                  </span>
                  <span className="rounded-full bg-white/10 px-3 py-1 text-xs uppercase tracking-wide text-white/70">
                    {bmiCategoryLabel}
                  </span>
                </div>
                <div className="mt-6 relative h-4 w-full overflow-hidden rounded-full bg-white/5">
                  <div className="flex h-full w-full">
                    {BMI_SEGMENTS.map((segment) => (
                      <div
                        key={segment.key}
                        className={clsx(
                          'h-full bg-gradient-to-r transition-opacity',
                          segmentGradients[segment.key],
                          activeBmiSegmentKey === segment.key ? 'opacity-100' : 'opacity-60'
                        )}
                        style={{ width: `${getSegmentWidth(segment)}%` }}
                      />
                    ))}
                  </div>
                  <div
                    className="absolute top-1/2 flex -translate-y-1/2 flex-col items-center"
                    style={{ left: bmiIndicatorPosition }}
                  >
                    <span className="rounded-full bg-white px-2 py-0.5 text-xs font-semibold text-base-800 shadow-card">
                      {bmiIndicatorDisplay}
                    </span>
                    <span className="mt-1 h-6 w-px bg-white/60" />
                  </div>
                </div>
                <div className="mt-6 grid gap-2 text-sm text-white/70 sm:grid-cols-2">
                  {BMI_SEGMENTS.map((segment) => (
                    <div key={segment.key} className="flex items-center justify-between rounded-2xl bg-white/5 px-4 py-2">
                      <span>{t(segment.labelKey)}</span>
                      <span className="text-white/60">{t(segment.rangeKey)}</span>
                    </div>
                  ))}
                </div>
              </article>
              <article className="rounded-3xl bg-white/5 p-6 text-white shadow-[inset_0_0_0_1px_rgba(255,255,255,0.04)]">
                <p className="text-xs uppercase tracking-[0.3em] text-white/60">{t('dashboard.whr')}</p>
                <p className="mt-1 text-sm text-white/60">{t('dashboard.whrSubtitle')}</p>
                <div className="mt-6 flex items-baseline gap-4">
                  <span className="text-5xl font-semibold text-white">
                    {whrValue !== null ? whrValue.toFixed(2) : notAvailableLabel}
                  </span>
                  <span
                    className={clsx(
                      'rounded-full px-3 py-1 text-xs uppercase tracking-wide',
                      isWhrGoodCondition ? 'bg-emerald-400/20 text-emerald-200' : 'bg-rose-500/20 text-rose-200'
                    )}
                  >
                    {whrRiskLabel}
                  </span>
                </div>
                <p className="mt-4 text-sm text-white/70">
                  {isWhrGoodCondition ? t('dashboard.whrGoodCondition') : t('dashboard.whrAtRisk')}
                </p>
              </article>
            </div>
            <div className="rounded-3xl bg-white/5 p-6 text-white shadow-[0_25px_60px_rgba(2,6,23,0.35)]">
              <div className="flex items-center justify-between gap-4">
                <h3 className="text-lg font-semibold">{t('dashboard.bodyMetrics')}</h3>
              </div>
              <div className="mt-6 grid gap-6 sm:grid-cols-2">
                {bodyStatsItems.map((item) => (
                  <div key={item.label} className="rounded-2xl bg-gradient-to-b from-white/10 to-transparent p-5">
                    <p className="text-xs uppercase tracking-[0.3em] text-white/60">{item.label}</p>
                    <p className="mt-2 text-xl font-semibold text-white">{item.value}</p>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </section>

        <section className="space-y-8">
          <SectionHeader
            label={resolveDashboardText('dashboard.nutritionInsightsLabel', 'Nutrition Insights')}
            title={resolveDashboardText('dashboard.nutritionInsightsTitle', 'Nutrition Intelligence')}
            description={resolveDashboardText(
              'dashboard.nutritionInsightsSubtitle',
              'Targets, recommendations, and quick guidance.'
            )}
            variant="nutrition"
          />
          <div className={clsx(getSectionContainerClasses('nutrition'), 'space-y-5')}>
            {insightPanels.map((panel) => {
              const isOpen = expandedInsights.includes(panel.id)
              return (
                <div
                  key={panel.id}
                  className={clsx(
                    'rounded-3xl bg-gradient-to-b from-white/12 to-white/5 p-6 text-white shadow-[0_25px_60px_rgba(2,6,23,0.35)]',
                    nutritionAccent?.cardBorder
                  )}
                >
                  <button
                    type="button"
                    className="flex w-full items-start justify-between gap-4 text-left"
                    onClick={() => toggleInsightPanel(panel.id)}
                    aria-expanded={isOpen}
                    aria-controls={`insight-${panel.id}`}
                    aria-label={`${isOpen ? resolveDashboardText('dashboard.collapse', 'Collapse') : resolveDashboardText('dashboard.expand', 'Expand')} ${panel.title}`}
                  >
                    <div>
                      <p className="text-xs uppercase tracking-[0.3em] text-white/60">{panel.title}</p>
                      <p className="mt-2 text-sm text-white/70">{panel.description}</p>
                    </div>
                    <span className="text-xl">{isOpen ? '-' : '+'}</span>
                  </button>
                  {isOpen && (
                    <div id={`insight-${panel.id}`} className="mt-4">
                      {panel.content}
                    </div>
                  )}
                </div>
              )
            })}
          </div>
        </section>
      </main>

      {isFeedbackModalOpen && <FeedbackModal onClose={() => setIsFeedbackModalOpen(false)} />}
      {isAchievementsModalOpen && gamificationStatus && (
        <AchievementsModal badges={gamificationStatus.badges || []} onClose={() => setIsAchievementsModalOpen(false)} />
      )}
      {isMealPlannerOpen && <MealPreferencesModal onClose={() => setIsMealPlannerOpen(false)} />}
    </div>
  )
}

function HealthRecommendations({ analysis, profile }) {
  const { t } = useTranslation()
  const [expandedSections, setExpandedSections] = useState([])
  const PREVIEW_LIMIT = 180
  const MAX_LIST_ITEMS = 3
  const notAvailableText = resolveTextWithFallback(t, 'dashboard.notAvailable', 'Not available')
  const resolveRecText = (key, fallback) => resolveTextWithFallback(t, key, fallback)

  if (!analysis) {
    return (
      <div className="space-y-3">
        <Skeleton className="h-20 w-full" />
        <Skeleton className="h-20 w-full" />
      </div>
    )
  }

  const toggleSection = (sectionKey) => {
    setExpandedSections((prev) =>
      prev.includes(sectionKey) ? prev.filter((key) => key !== sectionKey) : [...prev, sectionKey]
    )
  }

  const formatNumberValue = (value, digits = 1) => {
    if (value === null || value === undefined) return notAvailableText
    const numeric = Number(value)
    if (Number.isFinite(numeric)) {
      return numeric.toFixed(digits)
    }
    return value
  }

  const getMappedTranslation = (value, map) => {
    if (!value) return notAvailableText
    const key = map[value]
    return key ? resolveTextWithFallback(t, key, humanizeTranslationKey(value)) : value
  }

  const getContentPreview = (text) => {
    if (!text) return ''
    if (text.length <= PREVIEW_LIMIT) return text
    return `${text.slice(0, PREVIEW_LIMIT).trim()}…`
  }

  const renderListBlock = (items, heading, variant, prefix) => {
    if (!Array.isArray(items) || items.length === 0) return null
    const displayItems = items.slice(0, MAX_LIST_ITEMS)
    return (
      <div key={`${prefix}-list`}>
        <p className="text-xs uppercase tracking-[0.3em] text-white/55">{heading}</p>
        <ul className="mt-2 space-y-2 text-sm text-white/80 leading-relaxed">
          {displayItems.map((item, index) => (
            <li key={`${prefix}-${index}`} className="flex gap-3">
              <span className={clsx('mt-2 h-1.5 w-1.5 rounded-full', variant.bullet)} />
              <span>{item}</span>
            </li>
          ))}
        </ul>
      </div>
    )
  }

  const renderHighlights = (items, variant, prefix) => {
    if (!Array.isArray(items) || items.length === 0) return null
    return (
      <div className="flex flex-wrap gap-3" key={`${prefix}-highlights`}>
        {items.map((highlight, index) => (
          <div
            key={`${prefix}-highlight-${index}`}
            className={clsx(
              'min-w-[120px] flex-1 rounded-2xl border px-4 py-3 text-sm backdrop-blur',
              variant.highlightBorder,
              variant.highlightBg
            )}
          >
            <p className="text-[11px] uppercase tracking-[0.3em] text-white/55">{highlight.label}</p>
            <p className="mt-1 text-base font-semibold text-white">{highlight.value}</p>
          </div>
        ))}
      </div>
    )
  }

  const getWHRRecommendations = () => {
    const whrExplanation = t('healthRecommendations.whrExplanation')
    const whrRiskLabel = getMappedTranslation(analysis?.whrRisk, WHR_RISK_TRANSLATIONS)
    const risks = [
      t('healthRecommendations.heartDisease'),
      t('healthRecommendations.type2Diabetes'),
      t('healthRecommendations.stroke'),
      t('healthRecommendations.metabolicSyndrome')
    ]

    if (analysis.whrRisk === 'At risk') {
      return {
        title: t('healthRecommendations.whrHealthRisk'),
        explanation: whrExplanation,
        risks,
        detailText:
          profile?.sex === 'Male'
            ? t('healthRecommendations.whrAtRiskMale')
            : t('healthRecommendations.whrAtRiskFemale'),
        focus: {
          title: resolveRecText('dashboard.focusWhrAttention', 'Bring your ratio back into range'),
          body: resolveRecText(
            'dashboard.focusWhrAttentionBody',
            'Central adiposity pushes metabolic stress higher. Track the same waist/hip measurements each week and pair steady strength work with daily low-impact movement.'
          ),
          actions: [
            resolveRecText('dashboard.focusWhrAction1', 'Base meals around lean protein, vegetables, and fiber'),
            resolveRecText('dashboard.focusWhrAction2', 'Layer 20–30 minutes of moderate cardio 4x weekly')
          ],
        },
        highlights: [
          {
            label: resolveTextWithFallback(t, 'dashboard.whr', 'WHR'),
            value: formatNumberValue(analysis?.whr, 2)
          },
          {
            label: resolveTextWithFallback(t, 'dashboard.statusLabel', 'Status'),
            value: whrRiskLabel
          }
        ]
      }
    }

    return {
      title: t('healthRecommendations.whrHealthStatus'),
      explanation: whrExplanation,
      risks,
      detailText:
        profile?.sex === 'Male'
          ? t('healthRecommendations.whrGoodMale')
          : t('healthRecommendations.whrGoodFemale'),
      focus: {
        title: resolveRecText('dashboard.focusWhrMaintain', 'Protect this healthy ratio'),
        body: resolveRecText(
          'dashboard.focusWhrMaintainBody',
          'Your current measurements suggest balanced fat distribution. Keep reinforcing core strength and nutrition that favors satiating, minimally processed foods.'
        ),
        actions: [
          resolveRecText('dashboard.focusWhrMaintainAction1', 'Measure at the same time of day for consistency'),
          resolveRecText('dashboard.focusWhrMaintainAction2', 'Blend posture, core, and mobility sessions weekly')
        ],
      },
      highlights: [
        {
          label: resolveTextWithFallback(t, 'dashboard.whr', 'WHR'),
          value: formatNumberValue(analysis?.whr, 2)
        },
        {
          label: resolveTextWithFallback(t, 'dashboard.statusLabel', 'Status'),
          value: whrRiskLabel
        }
      ]
    }
  }

  const getHeartDiseaseInfo = () => {
    const bmiValue =
      analysis?.bmi !== null && analysis?.bmi !== undefined ? formatNumberValue(analysis?.bmi, 1) : notAvailableText
    const bmiCategory = getMappedTranslation(analysis?.bmiCategory, BMI_CATEGORY_TRANSLATIONS)

    return {
      title: resolveRecText('dashboard.heartHealthTitle', 'Lower cardiac strain'),
      detailText: t('healthRecommendations.heartDiseaseContent'),
      highlights: [
        {
          label: resolveTextWithFallback(t, 'dashboard.bmi', 'BMI'),
          value: bmiValue
        },
        {
          label: resolveTextWithFallback(t, 'dashboard.categoryLabel', 'Category'),
          value: bmiCategory
        }
      ],
      focus: {
        title: resolveRecText('dashboard.heartHealthFocusTitle', 'Why this matters'),
        body: resolveRecText(
          'dashboard.heartHealthFocusBody',
          'Cardiovascular tissue adapts best when training, recovery, and stress control stay balanced. Maintain predictable workouts, sleep, and hydration.'
        ),
        actions: [
          resolveRecText('dashboard.heartHealthAction1', 'Aim for 150+ minutes of moderate cardio each week'),
          resolveRecText('dashboard.heartHealthAction2', 'Keep sodium moderate and check resting heart rate weekly')
        ],
      }
    }
  }

  const getTranslatedList = (key) => {
    const list = t(key, { returnObjects: true })
    return Array.isArray(list) ? list : []
  }

  const getActivitySuggestions = () => {
    const activityLevel = profile?.activityLevel || 3
    const buildActivities = (levelKey) => getTranslatedList(`healthRecommendations.activityList.${levelKey}`)

    const suggestions = {
      1: {
        title: t('healthRecommendations.sedentaryTitle'),
        content: t('healthRecommendations.sedentaryContent'),
        activities: buildActivities(1)
      },
      2: {
        title: t('healthRecommendations.lightlyActiveTitle'),
        content: t('healthRecommendations.lightlyActiveContent'),
        activities: buildActivities(2)
      },
      3: {
        title: t('healthRecommendations.moderatelyActiveTitle'),
        content: t('healthRecommendations.moderatelyActiveContent'),
        activities: buildActivities(3)
      },
      4: {
        title: t('healthRecommendations.veryActiveTitle'),
        content: t('healthRecommendations.veryActiveContent'),
        activities: buildActivities(4)
      },
      5: {
        title: t('healthRecommendations.extraActiveTitle'),
        content: t('healthRecommendations.extraActiveContent'),
        activities: buildActivities(5)
      }
    }

    return {
      ...suggestions[activityLevel || 3],
      title: resolveRecText('dashboard.activityPlanTitle', 'Movement rhythm'),
      highlights: [
        {
          label: resolveTextWithFallback(t, 'dashboard.activityLevel', 'Activity profile'),
          value: suggestions[activityLevel || 3]?.title ?? notAvailableText
        },
        {
          label: resolveTextWithFallback(t, 'dashboard.frequencyLabel', 'Weekly rhythm'),
          value: resolveTextWithFallback(t, 'dashboard.steadyProgress', 'Steady progress')
        }
      ],
      focus: {
        title: resolveRecText('dashboard.activityPlanFocusTitle', 'How to apply this'),
        body: resolveRecText(
          'dashboard.activityPlanFocusBody',
          'Rotate higher-intensity days with restorative work. Consistency plus variety builds capacity without overloading joints or hormones.'
        ),
        actions: [
          resolveRecText('dashboard.activityPlanAction1', 'Note effort level after every session to track fatigue'),
          resolveRecText('dashboard.activityPlanAction2', 'Add 5-minute mobility or walk breaks between desk work')
        ],
      }
    }
  }

  const getGoalBasedTips = () => {
    const goal = profile?.calorieGoal || 2
    const buildGoalTips = (goalKey) => getTranslatedList(`healthRecommendations.goalTips.${goalKey}`)

    const tips = {
      1: {
        title: t('healthRecommendations.loseWeightTitle'),
        content: t('healthRecommendations.loseWeightContent'),
        tips: buildGoalTips(1)
      },
      2: {
        title: t('healthRecommendations.maintainWeightTitle'),
        content: t('healthRecommendations.maintainWeightContent'),
        tips: buildGoalTips(2)
      },
      3: {
        title: t('healthRecommendations.gainMuscleTitle'),
        content: t('healthRecommendations.gainMuscleContent'),
        tips: buildGoalTips(3)
      },
      4: {
        title: t('healthRecommendations.bodyRecompTitle'),
        content: t('healthRecommendations.bodyRecompContent'),
        tips: buildGoalTips(4)
      }
    }

    const selected = tips[goal] || tips[2]

    return {
      ...selected,
      title: resolveRecText('dashboard.nutritionFocusTitle', 'Nutrition alignment'),
      highlights: [
        {
          label: resolveTextWithFallback(t, 'dashboard.primaryGoal', 'Primary goal'),
          value: selected?.title ?? notAvailableText
        },
        {
          label: resolveTextWithFallback(t, 'dashboard.energyPlan', 'Energy plan'),
          value: resolveTextWithFallback(t, 'dashboard.guidedPlan', 'Guided plan')
        }
      ],
      focus: {
        title: resolveRecText('dashboard.nutritionFocusActionTitle', 'Turn goals into meals'),
        body: resolveRecText(
          'dashboard.nutritionFocusActionBody',
          'Anchor each meal with protein, produce, and slow carbs. Review weekly energy intake and adjust portions in small increments.'
        ),
        actions: [
          resolveRecText('dashboard.nutritionFocusAction1', 'Prep two nutrient-dense snacks to prevent reactive eating'),
          resolveRecText('dashboard.nutritionFocusAction2', 'Modify portions by roughly 10% rather than drastic cuts')
        ],
      }
    }
  }

  const whrRec = getWHRRecommendations()
  const heartInfo = getHeartDiseaseInfo()
  const activityRec = getActivitySuggestions()
  const goalTips = getGoalBasedTips()

  const visualVariants = {
    whr: {
      tagline: resolveRecText('dashboard.bodyCompositionTag', 'Body composition'),
      background: 'from-cyan-500/12 via-slate-900/50 to-slate-900/20',
      cardBorder: 'border-cyan-400/40',
      bullet: 'bg-cyan-200/80',
      accentLine: 'from-cyan-300/70 to-transparent',
      highlightBorder: 'border-cyan-300/30',
      highlightBg: 'bg-cyan-500/5',
      leftStripe: 'border-l-cyan-400/80'
    },
    heart: {
      tagline: resolveRecText('dashboard.cardioHealthTag', 'Cardiovascular health'),
      background: 'from-rose-500/12 via-slate-900/50 to-slate-900/20',
      cardBorder: 'border-rose-400/40',
      bullet: 'bg-rose-200/80',
      accentLine: 'from-rose-400/70 to-transparent',
      highlightBorder: 'border-rose-300/30',
      highlightBg: 'bg-rose-500/5',
      leftStripe: 'border-l-rose-400/80'
    },
    activity: {
      tagline: resolveRecText('dashboard.movementTag', 'Movement strategy'),
      background: 'from-amber-400/12 via-slate-900/50 to-slate-900/20',
      cardBorder: 'border-amber-400/40',
      bullet: 'bg-amber-200/80',
      accentLine: 'from-amber-300/70 to-transparent',
      highlightBorder: 'border-amber-300/30',
      highlightBg: 'bg-amber-400/5',
      leftStripe: 'border-l-amber-400/80'
    },
    goal: {
      tagline: resolveRecText('dashboard.nutritionTag', 'Nutrition focus'),
      background: 'from-emerald-400/12 via-slate-900/50 to-slate-900/20',
      cardBorder: 'border-emerald-400/40',
      bullet: 'bg-emerald-200/80',
      accentLine: 'from-emerald-300/70 to-transparent',
      highlightBorder: 'border-emerald-300/30',
      highlightBg: 'bg-emerald-400/5',
      leftStripe: 'border-l-emerald-400/80'
    }
  }

  const defaultVariant = {
    tagline: resolveRecText('dashboard.healthInsightTag', 'Health insight'),
    background: 'from-white/12 via-white/6 to-transparent',
    cardBorder: 'border-white/10',
    bullet: 'bg-white/70',
    accentLine: 'from-white/40 to-transparent',
    highlightBorder: 'border-white/20',
    highlightBg: 'bg-white/5',
    leftStripe: 'border-l-white/30'
  }

  const recommendationSections = [
    { key: 'whr', ...whrRec },
    { key: 'heart', ...heartInfo },
    { key: 'activity', ...activityRec },
    { key: 'goal', ...goalTips },
  ]

  return (
    <div className="grid gap-6 md:grid-cols-2">
      {recommendationSections.map((section) => {
        const variant = visualVariants[section.key] || defaultVariant
        const isExpanded = expandedSections.includes(section.key)
        const detailText = section.detailText
        const preview = getContentPreview(detailText)
        const showToggle = !!detailText && detailText.length > PREVIEW_LIMIT

        return (
          <article
            key={section.key}
            className={clsx(
              'group relative flex h-full flex-col rounded-3xl border bg-gradient-to-br p-6 text-white shadow-[0_30px_70px_rgba(2,6,23,0.45)] transition-all hover:shadow-[0_40px_90px_rgba(2,6,23,0.55)]',
              variant.background,
              variant.cardBorder,
              variant.leftStripe,
              'overflow-hidden border-l-[3px] lg:border-l-[4px]'
            )}
          >
            <div
              aria-hidden
              className={clsx(
                'absolute inset-x-6 top-3 h-0.5 rounded-full bg-gradient-to-r opacity-70',
                variant.accentLine
              )}
            />
            <span
              aria-hidden
              className={clsx(
                'pointer-events-none absolute inset-0 opacity-0 transition-opacity duration-300 group-hover:opacity-100',
                'bg-gradient-to-br from-white/10 via-transparent to-transparent blur-3xl'
              )}
            />
            <div className="relative z-10 flex flex-col gap-5 lg:flex-row">
              <div className="space-y-4 lg:w-2/5">
                <div className="space-y-2">
                  <p className="text-xs uppercase tracking-[0.3em] text-white/60">{variant.tagline}</p>
                  <div className="flex items-center gap-3">
                    <h3 className="text-xl font-semibold">{section.title}</h3>
                    <span
                      aria-hidden
                      className={clsx('inline-flex h-1 w-10 rounded-full opacity-90', variant.bullet)}
                    />
                  </div>
                </div>
                {section.explanation && <p className="text-sm leading-relaxed text-white/75">{section.explanation}</p>}
                {renderHighlights(section.highlights, variant, section.key)}
              </div>

              <div className="flex-1 space-y-5 border-t border-white/10 pt-5 lg:border-l lg:border-t-0 lg:pl-6 lg:pt-0">
                {renderListBlock(
                  section.risks,
                  resolveTextWithFallback(t, 'dashboard.keyRisks', 'Key risks'),
                  variant,
                  `${section.key}-risks`
                )}
                {renderListBlock(
                  section.activities,
                  resolveTextWithFallback(t, 'dashboard.activities', 'Suggested activities'),
                  variant,
                  `${section.key}-activities`
                )}
                {renderListBlock(
                  section.tips,
                  resolveTextWithFallback(t, 'dashboard.goalTips', 'Goal-focused tips'),
                  variant,
                  `${section.key}-tips`
                )}

                {section.focus && (
                  <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
                    <p className="text-xs uppercase tracking-[0.3em] text-white/60">{section.focus.title}</p>
                    <p className="mt-2 text-sm leading-relaxed text-white/90">{section.focus.body}</p>
                    {Array.isArray(section.focus.actions) && section.focus.actions.length > 0 && (
                      <ul className="mt-3 space-y-2 text-sm text-white/85 leading-relaxed">
                        {section.focus.actions.map((action, idx) => (
                          <li key={`${section.key}-focus-${idx}`} className="flex gap-2">
                            <span className={clsx('mt-1 h-1.5 w-1.5 rounded-full', variant.bullet)} />
                            <span>{action}</span>
                          </li>
                        ))}
                      </ul>
                    )}
                  </div>
                )}

                {detailText && (
                  <div className="rounded-2xl border border-white/10 bg-white/5 p-4">
                    <p className="text-xs uppercase tracking-[0.3em] text-white/60">
                      {resolveRecText('dashboard.fullGuidance', 'Full guidance')}
                    </p>
                    <p className="mt-2 text-sm leading-relaxed text-white/80">
                      {isExpanded || !showToggle ? detailText : preview}
                    </p>
                    {showToggle && (
                      <button
                        type="button"
                        className="mt-3 inline-flex items-center gap-2 text-sm font-semibold text-white/90"
                        onClick={() => toggleSection(section.key)}
                        aria-expanded={isExpanded}
                      >
                        {isExpanded
                          ? resolveRecText('dashboard.collapse', 'Show less')
                          : resolveRecText('dashboard.whyThisMatters', 'Why this matters')}
                        <span className="text-base">{isExpanded ? '-' : '>'}</span>
                      </button>
                    )}
                  </div>
                )}
              </div>
            </div>
          </article>
        )
      })}
    </div>
  )
}

export default Dashboard
