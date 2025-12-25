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

const SectionHeader = ({ label, title, description }) => (
  <div className="space-y-1 text-white">
    <p className="text-xs uppercase tracking-[0.4em] text-white/60">{label}</p>
    <h2 className="text-2xl font-semibold">{title}</h2>
    {description && <p className="text-sm text-white/60">{description}</p>}
  </div>
)

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

  const notAvailableLabel = t('dashboard.notAvailable')
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

  const headerAlignmentLabel = `${headerAlignment === 'left' ? '⇤' : headerAlignment === 'center' ? '⇆' : '⇥'} ${t('dashboard.headerAlignToggle')}`
  const greetingAlignment = alignmentClasses[headerAlignment] || alignmentClasses.center
  const sectionContainerClasses =
    'rounded-[40px] bg-white/5 p-6 sm:p-10 shadow-[0_30px_80px_rgba(2,6,23,0.45)] backdrop-blur-xl'

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

  const insightPanels = [
    {
      id: 'targets',
      title: t('dashboard.nutritionHub'),
      description: t('dashboard.nutritionHubSubtitle'),
      content: (
        <div className="space-y-7 text-white">
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            {energyCards.map((card) => (
              <div
                key={card.label}
                className="rounded-2xl bg-gradient-to-b from-white/10 via-white/5 to-transparent p-5 shadow-[0_20px_45px_rgba(2,6,23,0.35)]"
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
      title: t('dashboard.healthRecommendations'),
      description: t('dashboard.healthRecommendationsSubtitle'),
      content: <HealthRecommendations analysis={analysis} profile={profile} />,
    },
    {
      id: 'tips',
      title: t('dashboard.healthTips'),
      description: t('dashboard.healthTipsSubtitle'),
      content: (
        <div className="flex flex-col gap-4 rounded-3xl bg-gradient-to-br from-white/12 via-white/6 to-transparent p-6 text-white shadow-[0_20px_45px_rgba(2,6,23,0.35)]">
          <div className="text-3xl">💡</div>
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
          icon="⚠️"
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
          icon="📊"
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
        brandTitle="AI Fitness"
        brandSubtitle={t('dashboard.overview')}
        labels={{
          feedback: t('dashboard.feedback'),
          export: t('dashboard.downloadPdf'),
          edit: t('dashboard.editProfile') || 'Edit Profile',
          logout: t('dashboard.logOut'),
        }}
      />

      <main className="mx-auto mt-8 flex w-full max-w-6xl flex-col gap-16">
        <section className="space-y-8">
          <SectionHeader
            label={t('dashboard.overviewLabel')}
            title={t('dashboard.overviewTitle')}
            description={t('dashboard.overviewSubtitle')}
          />
          <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className={clsx(sectionContainerClasses, 'space-y-6')}>
            <div className={clsx('flex flex-col gap-3 text-white', greetingAlignment)}>
              <p className="text-xs uppercase tracking-[0.4em] text-white/60">{t('dashboard.overview')}</p>
              <h1 className="text-4xl font-semibold leading-tight">
                {profile?.name ? t('dashboard.greeting', { name: profile.name }) : t('dashboard.greetingDefault')}
              </h1>
              <p className="max-w-2xl text-base text-white/70">{t('dashboard.healthTipsSubtitle')}</p>
            </div>
            <DailySummaryStrip
              gamificationStatus={gamificationStatus}
              calorieTarget={analysis?.goalCalories}
              proteinTarget={analysis?.proteinTarget}
            />
          </motion.div>
        </section>

        <section className="space-y-8">
          <SectionHeader
            label={t('dashboard.aiGuidanceLabel')}
            title={t('dashboard.aiGuidanceTitle')}
            description={t('dashboard.aiGuidanceSubtitle')}
          />
          <div className={clsx(sectionContainerClasses, 'space-y-6')}>
            <div className="flex flex-wrap items-center justify-between gap-3 text-xs uppercase tracking-[0.3em] text-white/60">
              <span>{t('dashboard.aiPoweredLabel')}</span>
              <span className="rounded-full bg-white/10 px-4 py-1 text-[11px] text-white">{t('dashboard.aiPoweredTag')}</span>
            </div>
            <div className="grid gap-6 lg:grid-cols-[1.2fr_0.8fr]">
              <div className="flex flex-col gap-6">
                <div className="rounded-3xl bg-white/5 p-6 text-white shadow-[inset_0_0_0_1px_rgba(255,255,255,0.04)]">
                  <div className="flex items-start justify-between gap-4">
                    <div>
                      <p className="text-xs uppercase tracking-[0.3em] text-white/50">{t('dashboard.aiCoach')}</p>
                      <h3 className="mt-2 text-2xl font-semibold">{t('dashboard.aiCoachHeadline')}</h3>
                      <p className="mt-3 text-sm text-white/70">{t('dashboard.aiCoachDescription')}</p>
                    </div>
                    <span className="text-3xl">🤖</span>
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
                  <div className="flex items-start justify-between gap-4">
                    <div>
                      <p className="text-xs uppercase tracking-[0.3em] text-white/50">{t('dashboard.aiMealPlanner')}</p>
                      <h3 className="mt-2 text-xl font-semibold">{t('dashboard.aiMealPlannerDescription')}</h3>
                      <p className="mt-3 text-sm text-white/70">{t('dashboard.nutritionHubSubtitle')}</p>
                    </div>
                    <span className="text-3xl">🍽️</span>
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
            label={t('dashboard.healthMetricsLabel')}
            title={t('dashboard.healthMetricsTitle')}
            description={t('dashboard.healthMetricsSubtitle')}
          />
          <div className={clsx(sectionContainerClasses, 'space-y-6')}>
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
            label={t('dashboard.nutritionInsightsLabel')}
            title={t('dashboard.nutritionInsightsTitle')}
            description={t('dashboard.nutritionInsightsSubtitle')}
          />
          <div className={clsx(sectionContainerClasses, 'space-y-5')}>
            {insightPanels.map((panel) => {
              const isOpen = expandedInsights.includes(panel.id)
              return (
                <div
                  key={panel.id}
                  className="rounded-3xl bg-gradient-to-b from-white/12 to-white/5 p-6 text-white shadow-[0_25px_60px_rgba(2,6,23,0.35)]"
                >
                  <button
                    type="button"
                    className="flex w-full items-start justify-between gap-4 text-left"
                    onClick={() => toggleInsightPanel(panel.id)}
                    aria-expanded={isOpen}
                    aria-controls={`insight-${panel.id}`}
                    aria-label={`${isOpen ? t('dashboard.collapse') : t('dashboard.expand')} ${panel.title}`}
                  >
                    <div>
                      <p className="text-xs uppercase tracking-[0.3em] text-white/60">{panel.title}</p>
                      <p className="mt-2 text-sm text-white/70">{panel.description}</p>
                    </div>
                    <span className="text-xl">{isOpen ? '−' : '+'}</span>
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

  if (!analysis) {
    return (
      <div className="space-y-3">
        <Skeleton className="h-20 w-full" />
        <Skeleton className="h-20 w-full" />
      </div>
    )
  }

  const getWHRRecommendations = () => {
    const whrExplanation = t('healthRecommendations.whrExplanation')
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
        content: profile?.sex === 'Male'
          ? t('healthRecommendations.whrAtRiskMale')
          : t('healthRecommendations.whrAtRiskFemale'),
        icon: '⚠️'
      }
    }

    return {
      title: t('healthRecommendations.whrHealthStatus'),
      explanation: whrExplanation,
      risks,
      content: profile?.sex === 'Male'
        ? t('healthRecommendations.whrGoodMale')
        : t('healthRecommendations.whrGoodFemale'),
      icon: '✅'
    }
  }

  const getHeartDiseaseInfo = () => ({
    title: t('healthRecommendations.heartDiseasePrevention'),
    content: t('healthRecommendations.heartDiseaseContent'),
    icon: '❤️'
  })

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

    return { ...suggestions[activityLevel || 3], icon: '🏃' }
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

    return { ...(tips[goal] || tips[2]), icon: '🎯' }
  }

  const whrRec = getWHRRecommendations()
  const heartInfo = getHeartDiseaseInfo()
  const activityRec = getActivitySuggestions()
  const goalTips = getGoalBasedTips()

  const recommendationSections = [
    { key: 'whr', ...whrRec },
    { key: 'heart', ...heartInfo },
    { key: 'activity', ...activityRec },
    { key: 'goal', ...goalTips },
  ]

  return (
    <div className="space-y-5 text-white">
      {recommendationSections.map((section) => (
        <article
          key={section.key}
          className="rounded-3xl bg-gradient-to-br from-white/12 via-white/6 to-transparent p-6 shadow-[0_25px_60px_rgba(2,6,23,0.35)]"
        >
          <div className="flex items-center justify-between">
            <div>
              <h3 className="text-lg font-semibold">{section.title}</h3>
            </div>
            <span className="text-xl">{section.icon}</span>
          </div>
          {section.explanation && <p className="mt-3 text-sm text-white/70">{section.explanation}</p>}
          {Array.isArray(section.risks) && section.risks.length > 0 && (
            <ul className="mt-3 space-y-1 text-sm text-white/70">
              {section.risks.map((risk, index) => (
                <li key={index}>{risk}</li>
              ))}
            </ul>
          )}
          {section.content && <p className="mt-3 text-sm text-white/80">{section.content}</p>}
          {Array.isArray(section.activities) && section.activities.length > 0 && (
            <ul className="mt-3 space-y-1 text-sm text-white/70">
              {section.activities.map((activity, index) => (
                <li key={index}>{activity}</li>
              ))}
            </ul>
          )}
          {Array.isArray(section.tips) && section.tips.length > 0 && (
            <ul className="mt-3 space-y-1 text-sm text-white/70">
              {section.tips.map((tip, index) => (
                <li key={index}>{tip}</li>
              ))}
            </ul>
          )}
        </article>
      ))}
    </div>
  )
}

export default Dashboard
