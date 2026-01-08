import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { motion } from 'framer-motion'
import { User, Download, RefreshCw, Edit, Loader2 } from 'lucide-react'
import api from '../services/api'
import { ERROR_MESSAGES, UI_LABELS, BUTTON_TEXT, STATUS_MESSAGES, PAGE_TITLES } from '../config/constants'
import { generateFitnessProfilePdf } from '../utils/pdfProfile'
import { AppShell } from '../components/layout/AppShell'
import Button from '../components/ui/Button'
import { Badge } from '../components/ui/Badge'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../components/ui/Card'
import Skeleton from '../components/ui/Skeleton'
import EmptyState from '../components/ui/EmptyState'
import clsx from 'clsx'

function ProfilePage() {
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [downloading, setDownloading] = useState(false)
  const navigate = useNavigate()
  const { t } = useTranslation()

  useEffect(() => {
    fetchProfile()
  }, [])

  const fetchProfile = async () => {
    try {
      setLoading(true)
      setError(null)
      const response = await api.get('/user/profile')
      if (response.data.success) {
        setProfile(response.data.data)
      } else {
        setError(response.data.message || ERROR_MESSAGES.PROFILE_LOAD_FAILED)
      }
    } catch (err) {
      setError(err.genericMessage || ERROR_MESSAGES.PROFILE_LOAD_FAILED)
      console.error('Error fetching profile:', err)
    } finally {
      setLoading(false)
    }
  }

  const downloadProfilePdf = async () => {
    try {
      setDownloading(true)
      setError(null)

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
    } finally {
      setDownloading(false)
    }
  }

  if (loading) {
    return (
      <AppShell>
        <div className="p-6 lg:p-8">
          <div className="mx-auto max-w-6xl space-y-6">
            <Skeleton className="h-12 w-64" />
            <div className="grid gap-6 md:grid-cols-2">
              <Skeleton className="h-64 w-full" />
              <Skeleton className="h-64 w-full" />
            </div>
            <Skeleton className="h-96 w-full" />
          </div>
        </div>
      </AppShell>
    )
  }

  if (error && !profile) {
    return (
      <AppShell>
        <div className="flex min-h-screen items-center justify-center p-6">
          <EmptyState
            title="Error"
            description={error}
            actionLabel="Retry"
            onAction={fetchProfile}
          />
        </div>
      </AppShell>
    )
  }

  if (!profile) {
    return (
      <AppShell>
        <div className="flex min-h-screen items-center justify-center p-6">
          <EmptyState
            title={STATUS_MESSAGES.NO_PROFILE_DATA}
            description={STATUS_MESSAGES.PROFILE_NOT_AVAILABLE}
          />
        </div>
      </AppShell>
    )
  }

  const InfoItem = ({ label, value, icon: Icon }) => (
    <div className="flex items-start gap-3 rounded-xl border border-white/10 bg-white/5 p-4 transition hover:border-white/20">
      {Icon && (
        <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-accent/20">
          <Icon className="h-5 w-5 text-accent" />
        </div>
      )}
      <div className="flex-1">
        <p className="text-xs uppercase tracking-wider text-white/60">{label}</p>
        <p className="mt-1 text-lg font-semibold text-white">{value || 'N/A'}</p>
      </div>
    </div>
  )

  const MetricCard = ({ title, value, label, variant = 'default' }) => {
    const variantStyles = {
      default: 'from-white/10 to-white/5 border-white/10',
      success: 'from-emerald-500/20 to-emerald-500/10 border-emerald-500/30',
      warning: 'from-amber-500/20 to-amber-500/10 border-amber-500/30',
      danger: 'from-rose-500/20 to-rose-500/10 border-rose-500/30',
    }

    return (
      <div className={clsx(
        'rounded-2xl border bg-gradient-to-br p-6',
        variantStyles[variant]
      )}>
        <p className="text-xs uppercase tracking-wider text-white/60">{title}</p>
        <p className="mt-2 text-3xl font-semibold text-white">{value || 'N/A'}</p>
        {label && <p className="mt-1 text-sm text-white/70">{label}</p>}
      </div>
    )
  }

  return (
    <AppShell>
      <div className="min-h-screen bg-base-900 p-6 lg:p-8">
        <div className="mx-auto max-w-6xl space-y-6">
          {/* Header */}
          <motion.div
            initial={{ opacity: 0, y: -20 }}
            animate={{ opacity: 1, y: 0 }}
            className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between"
          >
            <div className="flex items-center gap-4">
              <div className="flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-accent to-accent/70">
                <User className="h-8 w-8 text-white" />
              </div>
              <div>
                <h1 className="text-3xl font-semibold text-white">{PAGE_TITLES.PROFILE}</h1>
                <p className="mt-1 text-sm text-white/60">{profile.name || profile.username}</p>
              </div>
            </div>
            <div className="flex gap-3">
              <Button
                variant="secondary"
                onClick={() => navigate('/profile/edit')}
                leftIcon={<Edit className="h-4 w-4" />}
              >
                {BUTTON_TEXT.EDIT || 'Edit'}
              </Button>
              <Button
                variant="primary"
                onClick={downloadProfilePdf}
                disabled={downloading}
                leftIcon={downloading ? <Loader2 className="h-4 w-4 animate-spin" /> : <Download className="h-4 w-4" />}
              >
                {downloading ? UI_LABELS.DOWNLOADING : BUTTON_TEXT.DOWNLOAD_PROFILE}
              </Button>
              <Button
                variant="ghost"
                onClick={fetchProfile}
                leftIcon={<RefreshCw className="h-4 w-4" />}
              >
                {BUTTON_TEXT.REFRESH}
              </Button>
            </div>
          </motion.div>

          {/* Personal Information */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.1 }}
          >
            <Card>
              <CardHeader>
                <CardTitle>Personal Information</CardTitle>
                <CardDescription>Your account and personal details</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="grid gap-4 sm:grid-cols-2">
                  <InfoItem label="Name" value={profile.name || 'N/A'} />
                  <InfoItem label="Username" value={profile.username} />
                  <InfoItem label="Email" value={profile.email} />
                  <InfoItem label="Age" value={profile.age ? `${profile.age} years` : 'N/A'} />
                  <InfoItem label="Sex" value={profile.sex || 'N/A'} />
                </div>
              </CardContent>
            </Card>
          </motion.div>

          {/* Body Measurements */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ delay: 0.2 }}
          >
            <Card>
              <CardHeader>
                <CardTitle>Body Measurements</CardTitle>
                <CardDescription>Your current physical measurements</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
                  <InfoItem
                    label="Weight"
                    value={profile.weight ? `${profile.weight.toFixed(1)} kg` : 'N/A'}
                  />
                  <InfoItem
                    label="Height"
                    value={profile.height ? `${profile.height.toFixed(1)} cm` : 'N/A'}
                  />
                  <InfoItem
                    label="Waist"
                    value={profile.waist ? `${profile.waist.toFixed(1)} cm` : 'N/A'}
                  />
                  <InfoItem
                    label="Hip"
                    value={profile.hip ? `${profile.hip.toFixed(1)} cm` : 'N/A'}
                  />
                </div>
              </CardContent>
            </Card>
          </motion.div>

          {/* Body Metrics */}
          {profile.bodyMetrics && (
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.3 }}
            >
              <Card>
                <CardHeader>
                  <CardTitle>Body Metrics</CardTitle>
                  <CardDescription>Calculated health indicators</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
                    <MetricCard
                      title="BMI"
                      value={profile.bodyMetrics.bmi ? profile.bodyMetrics.bmi.toFixed(2) : 'N/A'}
                      label={profile.bodyMetrics.bmiCategory || 'N/A'}
                      variant={
                        profile.bodyMetrics.bmiCategory?.includes('Normal') ? 'success' :
                        profile.bodyMetrics.bmiCategory?.includes('Overweight') || profile.bodyMetrics.bmiCategory?.includes('Obese') ? 'warning' :
                        'default'
                      }
                    />
                    <MetricCard
                      title="WHR"
                      value={profile.bodyMetrics.whr ? profile.bodyMetrics.whr.toFixed(2) : 'N/A'}
                      label={profile.bodyMetrics.whrHealthStatus || 'N/A'}
                      variant={profile.bodyMetrics.whrHealthStatus === 'Good condition' ? 'success' : 'warning'}
                    />
                    <MetricCard
                      title="WHtR"
                      value={profile.bodyMetrics.whtr ? profile.bodyMetrics.whtr.toFixed(2) : 'N/A'}
                      label={profile.bodyMetrics.whtrRiskLevel || 'N/A'}
                    />
                    <MetricCard
                      title="Body Fat"
                      value={profile.bodyMetrics.bodyFat ? `${profile.bodyMetrics.bodyFat.toFixed(1)}%` : 'N/A'}
                      label="Body Fat %"
                    />
                  </div>
                </CardContent>
              </Card>
            </motion.div>
          )}

          {/* Energy & Calories */}
          {profile.energy && (
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.4 }}
            >
              <Card>
                <CardHeader>
                  <CardTitle>Energy & Calories</CardTitle>
                  <CardDescription>Your daily energy requirements</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                    <InfoItem
                      label="BMR"
                      value={profile.energy.bmr ? `${Math.round(profile.energy.bmr)} kcal/day` : 'N/A'}
                    />
                    <InfoItem
                      label="TDEE"
                      value={profile.energy.tdee ? `${Math.round(profile.energy.tdee)} kcal/day` : 'N/A'}
                    />
                    <InfoItem
                      label="Goal Calories"
                      value={profile.energy.goalCalories ? `${Math.round(profile.energy.goalCalories)} kcal/day` : 'N/A'}
                    />
                    <InfoItem
                      label="Activity Level"
                      value={profile.activityLevelName || 'N/A'}
                    />
                    <InfoItem
                      label="Goal"
                      value={profile.calorieGoalName || 'N/A'}
                    />
                  </div>
                </CardContent>
              </Card>
            </motion.div>
          )}

          {/* Macronutrients */}
          {profile.macronutrients && (
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.5 }}
            >
              <Card>
                <CardHeader>
                  <CardTitle>Macronutrients</CardTitle>
                  <CardDescription>Daily macro targets</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                    {[
                      { key: 'protein', label: 'Protein', value: profile.macronutrients.protein, unit: 'g/day' },
                      { key: 'fat', label: 'Fat', value: profile.macronutrients.fat, unit: 'g/day' },
                      { key: 'carbs', label: 'Carbohydrates', value: profile.macronutrients.carbohydrates, unit: 'g/day' },
                      { key: 'fiber', label: 'Fiber', value: profile.macronutrients.fiber, unit: 'g/day' },
                      { key: 'water', label: 'Water', value: profile.macronutrients.water, unit: 'L/day' },
                    ].map((macro) => (
                      <InfoItem
                        key={macro.key}
                        label={macro.label}
                        value={macro.value ? `${typeof macro.value === 'number' ? macro.value.toFixed(1) : macro.value} ${macro.unit}` : 'N/A'}
                      />
                    ))}
                  </div>
                </CardContent>
              </Card>
            </motion.div>
          )}

          {/* Micronutrients */}
          {profile.micronutrients && (
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.6 }}
            >
              <Card>
                <CardHeader>
                  <CardTitle>Micronutrients</CardTitle>
                  <CardDescription>Daily vitamin and mineral targets</CardDescription>
                </CardHeader>
                <CardContent>
                  <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
                    {[
                      { key: 'iron', label: 'Iron', value: profile.micronutrients.iron, unit: 'mg/day' },
                      { key: 'calcium', label: 'Calcium', value: profile.micronutrients.calcium, unit: 'mg/day' },
                      { key: 'vitaminD', label: 'Vitamin D', value: profile.micronutrients.vitaminD, unit: 'µg/day' },
                      { key: 'magnesium', label: 'Magnesium', value: profile.micronutrients.magnesium, unit: 'mg/day' },
                      { key: 'zinc', label: 'Zinc', value: profile.micronutrients.zinc, unit: 'mg/day' },
                      { key: 'vitaminB12', label: 'Vitamin B12', value: profile.micronutrients.vitaminB12, unit: 'µg/day' },
                      { key: 'potassium', label: 'Potassium', value: profile.micronutrients.potassium, unit: 'mg/day' },
                      { key: 'sodium', label: 'Sodium', value: profile.micronutrients.sodium, unit: 'mg/day' },
                    ].map((micro) => (
                      <InfoItem
                        key={micro.key}
                        label={micro.label}
                        value={micro.value ? `${micro.value} ${micro.unit}` : 'N/A'}
                      />
                    ))}
                  </div>
                </CardContent>
              </Card>
            </motion.div>
          )}
        </div>
      </div>
    </AppShell>
  )
}

export default ProfilePage
