import React, { useState, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router-dom'
import { motion, AnimatePresence } from 'framer-motion'
import { Calendar, Plus, ChevronDown, ChevronUp, ShoppingCart, UtensilsCrossed, Loader2 } from 'lucide-react'
import { mealPlanAPI } from '../services/api'
import { invalidateGamificationCache } from '../services/gamificationApi'
import { MEAL_PLAN_LABELS, MEAL_PLAN_STATUS, ERROR_MESSAGES } from '../config/constants'
import GroceryList from '../components/grocery-list/GroceryList'
import { AppShell } from '../components/layout/AppShell'
import Button from '../components/ui/Button'
import { Badge } from '../components/ui/Badge'
import { Card, CardContent, CardHeader, CardTitle } from '../components/ui/Card'
import { Tabs, TabsTrigger, TabsContent } from '../components/ui/Tabs'
import Skeleton from '../components/ui/Skeleton'
import EmptyState from '../components/ui/EmptyState'
import clsx from 'clsx'

function MealPlan() {
  const { t, i18n } = useTranslation()
  const navigate = useNavigate()
  const [status, setStatus] = useState('loading')
  const [mealPlan, setMealPlan] = useState(null)
  const [error, setError] = useState('')
  const [generating, setGenerating] = useState(false)
  const [expandedDays, setExpandedDays] = useState(new Set())
  const [isMobile, setIsMobile] = useState(window.innerWidth < 768)
  const [activeTab, setActiveTab] = useState('meal-plan')

  useEffect(() => {
    fetchMealPlan()
    
    const handleResize = () => {
      setIsMobile(window.innerWidth < 768)
      if (window.innerWidth >= 768) {
        const allDays = new Set(['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'])
        setExpandedDays(allDays)
      }
    }
    
    window.addEventListener('resize', handleResize)
    handleResize()
    
    return () => window.removeEventListener('resize', handleResize)
  }, [])

  const fetchMealPlan = async () => {
    try {
      setStatus('loading')
      setError('')
      setMealPlan(null)
      
      const result = await mealPlanAPI.getCurrent()
      
      if (result.type === 'SUCCESS') {
        setMealPlan(result.data)
        setStatus('success')
        if (!isMobile) {
          const allDays = new Set(['Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday'])
          setExpandedDays(allDays)
        }
      } else if (result.type === 'EMPTY') {
        setStatus('empty')
      } else if (result.type === 'ERROR') {
        setError(result.error)
        setStatus('error')
      } else {
        setError(MEAL_PLAN_STATUS.LOAD_FAILED)
        setStatus('error')
      }
    } catch (err) {
      console.error('[MealPlan.jsx] Unexpected exception:', err)
      setError(err.message || MEAL_PLAN_STATUS.LOAD_FAILED)
      setStatus('error')
    }
  }

  const handleGenerate = async () => {
    const confirmed = window.confirm(
      `${t('mealPlan.confirmGenerate')}\n\n${t('mealPlan.confirmGenerateMessage')}`
    )
    
    if (!confirmed) return

    try {
      setGenerating(true)
      setError('')
      
      const today = new Date()
      const dayOfWeek = today.getDay()
      const daysUntilMonday = dayOfWeek === 0 ? 1 : (8 - dayOfWeek) % 7 || 7
      const nextMonday = new Date(today)
      nextMonday.setDate(today.getDate() + daysUntilMonday)
      const weekStart = nextMonday.toISOString().split('T')[0]
      
      const response = await mealPlanAPI.generate(weekStart)
      if (response.success) {
        invalidateGamificationCache()
        await fetchMealPlan()
      } else {
        setError(response.message || MEAL_PLAN_STATUS.GENERATE_FAILED)
        setStatus('error')
      }
    } catch (err) {
      setError(err.genericMessage || MEAL_PLAN_STATUS.GENERATE_FAILED)
      setStatus('error')
      console.error('Error generating meal plan:', err)
    } finally {
      setGenerating(false)
    }
  }

  const toggleDay = (dayName) => {
    const newExpanded = new Set(expandedDays)
    if (newExpanded.has(dayName)) {
      newExpanded.delete(dayName)
    } else {
      newExpanded.add(dayName)
    }
    setExpandedDays(newExpanded)
  }

  const getDayName = (dateString) => {
    const date = new Date(dateString)
    const days = [
      t('mealPlan.sunday'),
      t('mealPlan.monday'),
      t('mealPlan.tuesday'),
      t('mealPlan.wednesday'),
      t('mealPlan.thursday'),
      t('mealPlan.friday'),
      t('mealPlan.saturday'),
    ]
    return days[date.getDay()]
  }

  const getLocale = () => (i18n.language === 'vi' ? 'vi-VN' : 'en-US')

  const formatDate = (dateString) => {
    const date = new Date(dateString)
    return date.toLocaleDateString(getLocale(), { month: 'short', day: 'numeric' })
  }

  const formatWeekRange = (weekStartDate) => {
    if (!weekStartDate) return null
    
    try {
      const startDate = new Date(weekStartDate)
      if (isNaN(startDate.getTime())) return null
      
      const endDate = new Date(startDate)
      endDate.setDate(startDate.getDate() + 6)
      
      const startFormatted = formatDate(weekStartDate)
      const endFormatted = formatDate(endDate.toISOString().split('T')[0])
      
      return `${startFormatted} – ${endFormatted}`
    } catch (error) {
      console.error('Error formatting week range:', error)
      return formatDate(weekStartDate)
    }
  }

  const translateMealName = (name) => {
    if (!name) return ''
    const normalizedKey = name
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, '_')
      .replace(/^_+|_+$/g, '')
    return t(`mealNames.${normalizedKey}`, { defaultValue: name })
  }

  const getMealTypeLabel = (mealType) => {
    switch (mealType) {
      case 'BREAKFAST':
        return t('mealPlan.breakfast')
      case 'LUNCH':
        return t('mealPlan.lunch')
      case 'DINNER':
        return t('mealPlan.dinner')
      case 'SNACK':
        return t('mealPlan.snack')
      default:
        return mealType
    }
  }

  const getMealTypeColor = (mealType) => {
    switch (mealType) {
      case 'BREAKFAST':
        return 'bg-amber-500/20 text-amber-200 border-amber-500/30'
      case 'LUNCH':
        return 'bg-emerald-500/20 text-emerald-200 border-emerald-500/30'
      case 'DINNER':
        return 'bg-indigo-500/20 text-indigo-200 border-indigo-500/30'
      case 'SNACK':
        return 'bg-purple-500/20 text-purple-200 border-purple-500/30'
      default:
        return 'bg-white/10 text-white/90 border-white/20'
    }
  }

  const groupMealsByDay = (entries) => {
    const grouped = {}
    entries.forEach(entry => {
      const dayName = getDayName(entry.date)
      if (!grouped[dayName]) {
        grouped[dayName] = {
          date: entry.date,
          meals: []
        }
      }
      grouped[dayName].meals.push(entry)
    })
    
    Object.keys(grouped).forEach(day => {
      grouped[day].meals.sort((a, b) => {
        const order = { BREAKFAST: 1, LUNCH: 2, DINNER: 3, SNACK: 4 }
        return (order[a.mealType] || 99) - (order[b.mealType] || 99)
      })
    })
    
    return grouped
  }

  // Loading state
  if (status === 'loading') {
    return (
      <AppShell>
        <div className="p-6 lg:p-8">
          <div className="mx-auto max-w-7xl space-y-6">
            <Skeleton className="h-12 w-64" />
            <Skeleton className="h-32 w-full" />
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
              {[1, 2, 3, 4, 5, 6, 7].map((i) => (
                <Skeleton key={i} className="h-48 w-full" />
              ))}
            </div>
          </div>
        </div>
      </AppShell>
    )
  }

  // Empty state
  if (status === 'empty') {
    return (
      <AppShell>
        <div className="flex min-h-screen items-center justify-center p-6">
          <EmptyState
            title={t('mealPlan.noMealPlanYet')}
            description={t('mealPlan.generateFirstPlan')}
            actionLabel={generating ? t('mealPlan.generating') : t('mealPlan.generateNew')}
            onAction={handleGenerate}
            icon={<Calendar className="h-12 w-12 text-white/40" />}
          />
        </div>
      </AppShell>
    )
  }

  // Error state
  if (status === 'error') {
    return (
      <AppShell>
        <div className="flex min-h-screen items-center justify-center p-6">
          <EmptyState
            title={t('mealPlan.errorLoading')}
            description={error}
            actionLabel={t('mealPlan.retry')}
            onAction={fetchMealPlan}
          />
        </div>
      </AppShell>
    )
  }

  // Success state
  if (status !== 'success' || !mealPlan) {
    return (
      <AppShell>
        <div className="flex min-h-screen items-center justify-center p-6">
          <Skeleton className="h-32 w-full max-w-md" />
        </div>
      </AppShell>
    )
  }

  const mealsByDay = groupMealsByDay(mealPlan.entries)
  const dayOrder = [
    t('mealPlan.monday'),
    t('mealPlan.tuesday'),
    t('mealPlan.wednesday'),
    t('mealPlan.thursday'),
    t('mealPlan.friday'),
    t('mealPlan.saturday'),
    t('mealPlan.sunday'),
  ]

  return (
    <AppShell>
      <div className="min-h-screen bg-base-900 p-6 lg:p-8">
        <div className="mx-auto max-w-7xl space-y-6">
          {/* Header */}
          <motion.div
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between"
          >
            <div>
              <h1 className="text-3xl font-semibold text-white">{t('mealPlan.title')}</h1>
              {mealPlan.weekStartDate && (
                <p className="mt-2 flex items-center gap-2 text-sm text-white/60">
                  <Calendar className="h-4 w-4" />
                  {formatWeekRange(mealPlan.weekStartDate) || formatDate(mealPlan.weekStartDate)}
                </p>
              )}
            </div>
            <Button
              onClick={handleGenerate}
              disabled={generating}
              leftIcon={generating ? <Loader2 className="h-4 w-4 animate-spin" /> : <Plus className="h-4 w-4" />}
            >
              {generating ? t('mealPlan.generating') : t('mealPlan.generateNew')}
            </Button>
          </motion.div>

          {/* Daily Targets */}
          {mealPlan.dailyTargets && (
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.1 }}
            >
              <Card>
                <CardHeader>
                  <CardTitle>{t('mealPlan.dailyTargets')}</CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
                    {[
                      { key: 'calories', label: t('mealPlan.calories'), value: mealPlan.dailyTargets.calories, unit: t('mealPlan.kcal') },
                      { key: 'protein', label: t('mealPlan.protein'), value: mealPlan.dailyTargets.protein, unit: t('mealPlan.grams') },
                      { key: 'carbs', label: t('mealPlan.carbs'), value: mealPlan.dailyTargets.carbs, unit: t('mealPlan.grams') },
                      { key: 'fats', label: t('mealPlan.fats'), value: mealPlan.dailyTargets.fats, unit: t('mealPlan.grams') },
                    ].map((target) => (
                      <div
                        key={target.key}
                        className="rounded-2xl border border-white/10 bg-gradient-to-br from-white/5 to-transparent p-4"
                      >
                        <p className="text-xs uppercase tracking-wider text-white/60">{target.label}</p>
                        <p className="mt-2 text-2xl font-semibold text-white">
                          {target.value} <span className="text-sm text-white/60">{target.unit}</span>
                        </p>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </motion.div>
          )}

          {/* Tabs */}
          <Tabs value={activeTab} onValueChange={setActiveTab}>
            <div className="flex gap-2">
              <TabsTrigger value="meal-plan" activeValue={activeTab} onValueChange={setActiveTab}>
                <UtensilsCrossed className="mr-2 h-4 w-4" />
                {t('mealPlan.mealPlanTab')}
              </TabsTrigger>
              <TabsTrigger value="grocery-list" activeValue={activeTab} onValueChange={setActiveTab}>
                <ShoppingCart className="mr-2 h-4 w-4" />
                {t('mealPlan.groceryListTab')}
              </TabsTrigger>
            </div>

            <TabsContent value="meal-plan" activeValue={activeTab}>
              <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                {dayOrder.map((dayName, index) => {
                  const dayData = mealsByDay[dayName]
                  if (!dayData) return null

                  const isExpanded = expandedDays.has(dayName)

                  return (
                    <motion.div
                      key={dayName}
                      initial={{ opacity: 0, y: 20 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ delay: index * 0.05 }}
                    >
                      <Card className="overflow-hidden">
                        <button
                          onClick={() => isMobile && toggleDay(dayName)}
                          className="w-full"
                          type="button"
                        >
                          <CardHeader className="flex flex-row items-center justify-between">
                            <div className="flex items-center gap-3">
                              <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-accent/20">
                                <Calendar className="h-5 w-5 text-accent" />
                              </div>
                              <div>
                                <CardTitle className="text-lg">{dayName}</CardTitle>
                                <p className="text-xs text-white/60">{formatDate(dayData.date)}</p>
                              </div>
                            </div>
                            {isMobile && (
                              <motion.div
                                animate={{ rotate: isExpanded ? 180 : 0 }}
                                transition={{ duration: 0.2 }}
                              >
                                <ChevronDown className="h-5 w-5 text-white/60" />
                              </motion.div>
                            )}
                          </CardHeader>
                        </button>

                        <AnimatePresence>
                          {(isExpanded || !isMobile) && (
                            <motion.div
                              initial={{ height: 0, opacity: 0 }}
                              animate={{ height: 'auto', opacity: 1 }}
                              exit={{ height: 0, opacity: 0 }}
                              transition={{ duration: 0.2 }}
                            >
                              <CardContent className="space-y-3">
                                {dayData.meals.map((meal) => (
                                  <div
                                    key={meal.id}
                                    className="rounded-xl border border-white/10 bg-white/5 p-4 transition hover:border-white/20"
                                  >
                                    <div className="mb-2 flex items-center justify-between">
                                      <Badge
                                        variant="default"
                                        size="sm"
                                        className={getMealTypeColor(meal.mealType)}
                                      >
                                        {getMealTypeLabel(meal.mealType)}
                                      </Badge>
                                      <span className="text-sm font-semibold text-white">
                                        {meal.calories} {t('mealPlan.kcal')}
                                      </span>
                                    </div>
                                    <h4 className="mb-3 font-semibold text-white">
                                      {translateMealName(meal.name)}
                                    </h4>
                                    <div className="grid grid-cols-3 gap-2 text-xs">
                                      <div>
                                        <span className="text-white/60">{t('mealPlan.protein')}:</span>
                                        <span className="ml-1 font-semibold text-white">{meal.protein}g</span>
                                      </div>
                                      <div>
                                        <span className="text-white/60">{t('mealPlan.carbs')}:</span>
                                        <span className="ml-1 font-semibold text-white">{meal.carbs}g</span>
                                      </div>
                                      <div>
                                        <span className="text-white/60">{t('mealPlan.fats')}:</span>
                                        <span className="ml-1 font-semibold text-white">{meal.fats}g</span>
                                      </div>
                                    </div>
                                  </div>
                                ))}
                              </CardContent>
                            </motion.div>
                          )}
                        </AnimatePresence>
                      </Card>
                    </motion.div>
                  )
                })}
              </div>
            </TabsContent>

            <TabsContent value="grocery-list" activeValue={activeTab}>
              <GroceryList mealPlan={mealPlan} />
            </TabsContent>
          </Tabs>
        </div>
      </div>
    </AppShell>
  )
}

export default MealPlan
