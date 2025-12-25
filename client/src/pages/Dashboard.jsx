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
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '@/components/ui/Card'
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

function Dashboard() {
  const { t, i18n } = useTranslation()
  const [analysis, setAnalysis] = useState(null)
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [gamificationStatus, setGamificationStatus] = useState(null)
  const [activeStatTab, setActiveStatTab] = useState('BMI') // Tab state for Your Stats section
  const [isFeedbackModalOpen, setIsFeedbackModalOpen] = useState(false)
  const [isAchievementsModalOpen, setIsAchievementsModalOpen] = useState(false)
  const [isMealPlannerOpen, setIsMealPlannerOpen] = useState(false)
  const [headerAlignment, setHeaderAlignment] = useState(() => localStorage.getItem('dashboardHeaderAlign') || 'center')
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

  const getHealthTip = () => {
    if (!analysis) return null

    const tips = []
    
    // BMI tips
    if (analysis.bmiCategory === 'Underweight') {
      tips.push(t('healthTips.underweightTip'))
    } else if (analysis.bmiCategory === 'Overweight' || analysis.bmiCategory.startsWith('Obese')) {
      tips.push(t('healthTips.overweightTip'))
    } else if (analysis.bmiCategory === 'Normal') {
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
    tips.push(t('healthTips.proteinTip', { protein: Math.round(analysis.proteinTarget) }))

    return tips[Math.floor(Math.random() * tips.length)] || t('healthTips.defaultTip')
  }

  const notAvailableLabel = t('dashboard.notAvailable')
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
  const BMI_CATEGORY_TRANSLATIONS = {
    'Underweight': 'dashboard.underweight',
    'Normal': 'dashboard.normal',
    'Overweight': 'dashboard.overweight',
    'Obese (Class I)': 'dashboard.obeseClassI',
    'Obese (Class II)': 'dashboard.obeseClassII',
    'Obese (Class III)': 'dashboard.obeseClassIII',
  }

  const WHR_RISK_TRANSLATIONS = {
    'Good condition': 'dashboard.goodCondition',
    'At risk': 'dashboard.atRisk',
  }

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

  const headerAlignmentLabel = `${headerAlignment === 'left' ? '⇤' : headerAlignment === 'center' ? '⇆' : '⇥'} ${t('dashboard.headerAlignToggle')}`
  const greetingAlignment = alignmentClasses[headerAlignment] || alignmentClasses.center

  const aiFeatureCards = [
    {
      id: 'coach',
      title: t('dashboard.aiCoach'),
      description: t('dashboard.aiCoachDescription'),
      icon: '🤖',
      badge: t('dashboard.recommended'),
      onClick: () => window.open('/ai-coach', '_blank'),
    },
    {
      id: 'meal',
      title: t('dashboard.aiMealPlanner'),
      description: t('dashboard.aiMealPlannerDescription'),
      icon: '🍽️',
      badge: t('dashboard.aiMealPlanner'),
      onClick: () => setIsMealPlannerOpen(true),
    },
  ]

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

      <main className="mx-auto mt-8 flex w-full max-w-6xl flex-col gap-8">
        <motion.section
          initial={{ opacity: 0, y: 10 }}
          animate={{ opacity: 1, y: 0 }}
          className={clsx(
            'flex flex-col gap-4 rounded-3xl border border-white/12 bg-base-900/70 p-10 text-white',
            greetingAlignment
          )}
        >
          <p className="text-xs uppercase tracking-[0.4em] text-muted">{t('dashboard.overview')}</p>
          <h1 className="text-4xl font-semibold leading-tight">
            {profile?.name ? t('dashboard.greeting', { name: profile.name }) : t('dashboard.greetingDefault')}
          </h1>
          <p className="max-w-2xl text-base text-white/70">{t('dashboard.healthTipsSubtitle')}</p>
        </motion.section>

        <DailySummaryStrip
          gamificationStatus={gamificationStatus}
          calorieTarget={analysis?.goalCalories}
          proteinTarget={analysis?.proteinTarget}
        />

        <div className="grid gap-6 lg:grid-cols-[minmax(0,2fr)_minmax(0,1fr)]">
          <div className="flex flex-col gap-6">
            <section className="space-y-5">
              <div className="flex flex-wrap items-center justify-between gap-4">
                <div>
                  <p className="text-xs uppercase tracking-[0.4em] text-muted">{t('dashboard.aiFeatures')}</p>
                  <p className="text-sm text-white/60">{t('dashboard.aiFeaturesSubtitle')}</p>
                </div>
                <span className="rounded-full border border-white/15 px-4 py-1 text-xs text-white/60">
                  AI Generated
                </span>
              </div>
              <div className="grid gap-4 md:grid-cols-2">
                {aiFeatureCards.map((card, index) => (
                  <motion.button
                    key={card.id}
                    onClick={card.onClick}
                    initial={{ opacity: 0, y: 12 }}
                    animate={{ opacity: 1, y: 0 }}
                    transition={{ delay: index * 0.05 }}
                    className="group rounded-3xl border border-white/12 bg-transparent p-6 text-left transition hover:border-white/30 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent/40"
                  >
                    <span className="text-2xl">{card.icon}</span>
                    <p className="mt-4 text-lg font-semibold text-white">{card.title}</p>
                    <p className="mt-2 text-sm text-white/65">{card.description}</p>
                    <span className="mt-6 inline-flex text-xs uppercase tracking-[0.4em] text-white/50">
                      {card.badge}
                    </span>
                  </motion.button>
                ))}
              </div>
            </section>

            <Card className="bg-base-900/60">
              <CardHeader className="flex flex-col gap-2">
                <CardTitle>{t('dashboard.yourStats')}</CardTitle>
                <CardDescription>{t('dashboard.trackMetrics')}</CardDescription>
                <div className="inline-flex items-center gap-2 rounded-full border border-white/12 p-1">
                  {[
                    { id: 'BMI', label: t('dashboard.bmiValue') },
                    { id: 'WHR', label: t('dashboard.whrValue') },
                  ].map((tab) => (
                    <button
                      key={tab.id}
                      className={clsx(
                        'rounded-full px-4 py-2 text-sm transition',
                        activeStatTab === tab.id ? 'bg-white/15 text-white' : 'text-white/60'
                      )}
                      onClick={() => setActiveStatTab(tab.id)}
                    >
                      {tab.label}
                    </button>
                  ))}
                </div>
              </CardHeader>
              <CardContent>
                {activeStatTab === 'BMI' ? (
                  <motion.div initial={{ opacity: 0.5 }} animate={{ opacity: 1 }}>
                    <p className="text-sm text-muted">{t('dashboard.bmiSubtitle')}</p>
                    <div className="mt-4 flex items-baseline gap-3">
                      <span className="text-4xl font-semibold text-white">
                        {bmiValue !== null ? bmiValue.toFixed(1) : notAvailableLabel}
                      </span>
                      <span className="rounded-full bg-white/10 px-3 py-1 text-xs uppercase tracking-wide text-white/80">
                        {bmiCategoryLabel}
                      </span>
                    </div>
                  </motion.div>
                ) : (
                  <motion.div initial={{ opacity: 0.5 }} animate={{ opacity: 1 }}>
                    <p className="text-sm text-muted">{t('dashboard.whrSubtitle')}</p>
                    <div className="mt-4 flex items-baseline gap-3">
                      <span className="text-4xl font-semibold text-white">
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
                  </motion.div>
                )}
              </CardContent>
            </Card>

            <div className="grid gap-6 lg:grid-cols-2">
              <Card className="bg-base-900/60">
                <CardHeader>
                  <CardTitle>{t('dashboard.bmi')}</CardTitle>
                  <CardDescription>{t('dashboard.bmiSubtitle')}</CardDescription>
                </CardHeader>
                <CardContent className="space-y-6">
                  <div className="flex items-baseline gap-4">
                    <span className="text-5xl font-semibold text-white">
                      {bmiValue !== null ? bmiValue.toFixed(1) : notAvailableLabel}
                    </span>
                    <span className="rounded-full bg-white/10 px-3 py-1 text-xs uppercase tracking-wide text-white/70">
                      {bmiCategoryLabel}
                    </span>
                  </div>
                  <div className="relative h-4 w-full overflow-hidden rounded-full bg-white/5">
                    <div className="flex h-full w-full">
                      {BMI_SEGMENTS.map((segment) => (
                        <div
                          key={segment.key}
                          className={clsx(
                            'h-full bg-gradient-to-r',
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
                  <div className="grid gap-2 text-sm text-white/70 sm:grid-cols-2">
                    {BMI_SEGMENTS.map((segment) => (
                      <div key={segment.key} className="flex items-center justify-between rounded-2xl bg-white/5 px-4 py-2">
                        <span>{t(segment.labelKey)}</span>
                        <span className="text-white/60">{t(segment.rangeKey)}</span>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>

              <Card className="bg-base-900/60">
                <CardHeader>
                  <CardTitle>{t('dashboard.whr')}</CardTitle>
                  <CardDescription>{t('dashboard.whrSubtitle')}</CardDescription>
                </CardHeader>
                <CardContent className="space-y-4">
                  <div className="flex items-baseline gap-4">
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
                  <p className="text-white/70">
                    {isWhrGoodCondition ? t('dashboard.whrGoodCondition') : t('dashboard.whrAtRisk')}
                  </p>
                </CardContent>
              </Card>
            </div>

            <Card className="bg-base-900/60">
              <CardHeader>
                <CardTitle>{t('dashboard.nutritionHub')}</CardTitle>
                <CardDescription>{t('dashboard.nutritionHubSubtitle')}</CardDescription>
              </CardHeader>
              <CardContent className="space-y-8">
                <div className="grid gap-4 md:grid-cols-3">
                  {energyCards.map((card) => (
                    <div key={card.label} className="rounded-3xl border border-white/12 bg-transparent p-5">
                      <p className="text-xs uppercase tracking-[0.3em] text-muted">{card.label}</p>
                      <p className="mt-3 text-2xl font-semibold text-white">{card.value}</p>
                      <p className="text-xs text-white/50">{t('dashboard.kcalPerDay')}</p>
                      <p className="mt-4 text-sm text-white/60 whitespace-pre-line">{card.description}</p>
                    </div>
                  ))}
                </div>
                <div className="rounded-3xl border border-white/12 bg-transparent p-6">
                  <div className="flex flex-wrap items-baseline justify-between gap-4">
                    <div>
                      <p className="text-sm uppercase tracking-[0.3em] text-muted">{t('dashboard.nutritionTarget')}</p>
                      <p className="text-3xl font-semibold text-white">
                        {analysis.proteinTarget ? Math.round(analysis.proteinTarget) : notAvailableLabel}
                        <span className="ml-2 text-base text-white/60">{t('dashboard.gramsPerDay')}</span>
                      </p>
                    </div>
                  </div>
                  <div className="mt-6 h-2 w-full rounded-full bg-white/10">
                    <div className="h-full rounded-full bg-accent/80" style={{ width: `${Math.min(100, ((analysis.proteinTarget || 0) / 200) * 100)}%` }} />
                  </div>
                  <p className="mt-4 text-sm text-white/70">{t('dashboard.proteinTip')}</p>
                </div>
              </CardContent>
            </Card>

            <Card className="bg-base-900/60">
              <CardHeader>
                <CardTitle>{t('dashboard.healthRecommendations')}</CardTitle>
                <CardDescription>{t('dashboard.healthRecommendationsSubtitle')}</CardDescription>
              </CardHeader>
              <CardContent>
                <HealthRecommendations analysis={analysis} profile={profile} />
              </CardContent>
            </Card>
          </div>

          <div className="flex flex-col gap-6">
            {gamificationStatus && (
              <XPBoard
                xp={gamificationStatus.xp ?? 0}
                currentStreakDays={gamificationStatus.currentStreakDays ?? 0}
                onOpenDetails={() => setIsAchievementsModalOpen(true)}
              />
            )}

            <Card className="bg-base-900/60">
              <CardContent className="px-0 pb-0">
                <DailyChallenges />
              </CardContent>
            </Card>

            <Card className="bg-base-900/60">
              <CardHeader>
                <CardTitle>{t('dashboard.healthTips')}</CardTitle>
                <CardDescription>{t('dashboard.healthTipsSubtitle')}</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="rounded-3xl border border-white/15 bg-transparent p-6 text-white">
                  <div className="text-2xl">💡</div>
                  <p className="mt-4 text-lg font-semibold">{getHealthTip()}</p>
                  <Button className="mt-6" variant="secondary" size="sm" onClick={() => setIsFeedbackModalOpen(true)}>
                    {t('dashboard.feedback')}
                  </Button>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
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
        <Skeleton className="h-24 w-full" />
        <Skeleton className="h-24 w-full" />
      </div>
    )
  }

  const getWHRRecommendations = () => {
    // Based on Java code (mainOne.java lines 252-274)
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
        risks: risks,
        content: profile?.sex === 'Male' 
          ? t('healthRecommendations.whrAtRiskMale')
          : t('healthRecommendations.whrAtRiskFemale'),
        severity: 'warning'
      }
    } else {
      return {
        title: t('healthRecommendations.whrHealthStatus'),
        explanation: whrExplanation,
        risks: risks,
        content: profile?.sex === 'Male'
          ? t('healthRecommendations.whrGoodMale')
          : t('healthRecommendations.whrGoodFemale'),
        severity: 'good'
      }
    }
  }

  const getHeartDiseaseInfo = () => {
    return {
      title: t('healthRecommendations.heartDiseasePrevention'),
      content: t('healthRecommendations.heartDiseaseContent'),
      severity: 'info'
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

    return suggestions[activityLevel] || suggestions[3]
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

    return tips[goal] || tips[2]
  }

  const whrRec = getWHRRecommendations()
  const heartInfo = getHeartDiseaseInfo()
  const activityRec = getActivitySuggestions()
  const goalTips = getGoalBasedTips()

  const severityStyles = {
    warning: 'border border-white/15 text-white/80',
    good: 'border border-white/15 text-white/80',
    info: 'border border-white/15 text-white/80',
  }

  return (
    <div className="grid gap-4">
      <div className={clsx('rounded-3xl p-6', severityStyles[whrRec.severity])}>
        <div className="flex items-center justify-between text-white">
          <h3 className="text-lg font-semibold">{whrRec.title}</h3>
          <span className="text-base">{whrRec.severity === 'warning' ? '⚠️' : '✅'}</span>
        </div>
        <p className="mt-3 text-sm text-white/70">{whrRec.explanation}</p>
        <ul className="mt-4 space-y-1 text-sm text-white/65">
          {whrRec.risks.map((risk, index) => (
            <li key={index}>• {risk}</li>
          ))}
        </ul>
        <p className="mt-4 text-sm text-white/80">{whrRec.content}</p>
      </div>

      <div className="rounded-3xl border border-white/15 p-6 text-white/80">
        <div className="flex items-center justify-between text-white">
          <h3 className="text-lg font-semibold">{heartInfo.title}</h3>
          <span>❤️</span>
        </div>
        <p className="mt-3 text-sm">{heartInfo.content}</p>
      </div>

      <div className="rounded-3xl border border-white/15 p-6 text-white/80">
        <div className="flex items-center justify-between text-white">
          <h3 className="text-lg font-semibold">{activityRec.title}</h3>
          <span>🏃</span>
        </div>
        <p className="mt-3 text-sm">{activityRec.content}</p>
        <ul className="mt-3 space-y-1 text-sm text-white/65">
          {activityRec.activities.map((activity, index) => (
            <li key={index}>{activity}</li>
          ))}
        </ul>
      </div>

      <div className="rounded-3xl border border-white/15 p-6 text-white/80">
        <div className="flex items-center justify-between text-white">
          <h3 className="text-lg font-semibold">{goalTips.title}</h3>
          <span>🎯</span>
        </div>
        <p className="mt-3 text-sm">{goalTips.content}</p>
        <ul className="mt-3 space-y-1 text-sm text-white/65">
          {goalTips.tips.map((tip, index) => (
            <li key={index}>{tip}</li>
          ))}
        </ul>
      </div>
    </div>
  )
}

export default Dashboard
