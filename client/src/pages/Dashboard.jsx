import React, { useState, useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import api, { userAPI } from '../services/api'
import { logout } from '../utils/auth'
import { gamificationAPI } from '../services/gamificationApi'
import jsPDF from 'jspdf'
import { ERROR_MESSAGES, UI_LABELS, BUTTON_TEXT, STATUS_MESSAGES, PAGE_TITLES } from '../config/constants'
import DailyChallenges from '../components/gamification/DailyChallenges'
import XPBoard from '../components/gamification/XPBoard'
import DailySummaryStrip from '../components/dashboard/DailySummaryStrip'
import FeedbackModal from '../components/FeedbackModal'
import AchievementsModal from '../components/AchievementsModal'
import MealPreferencesModal from '../components/MealPreferencesModal'
import './Dashboard.css'

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
    console.log("DOWNLOAD CLICKED");
    try {
      const token = localStorage.getItem("token");
      const res = await fetch("/profile/export", {
        method: "GET",
        headers: {
          "Authorization": "Bearer " + token
        }
      })
      
      console.log("EXPORT RESPONSE:", res);
      console.log("EXPORT STATUS:", res.status);
      console.log("EXPORT STATUS TEXT:", res.statusText);
      
      const responseText = await res.text();
      console.log("EXPORT TEXT:", responseText);
      
      if (!res.ok) {
        console.error("EXPORT FAILED - Status:", res.status);
        console.error("EXPORT FAILED - Response text:", responseText);
        alert("Backend error (Status " + res.status + "): " + responseText);
        return;
      }
      
      const response = JSON.parse(responseText);
      
      // Handle both wrapped (ApiResponse) and direct Map responses
      const data = response.data || response
      
      // Always generate PDF, even if data is incomplete - show N/A for missing fields
      const doc = new jsPDF("p", "mm", "a4")
      const today = new Date().toLocaleDateString()
      
      const bg = "#0D1117"
      const accent = "#55C0FF"
      const textColor = "#E8EAF1"
      
      // Convert hex colors to RGB for jsPDF
      const hexToRgb = (hex) => {
        const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex)
        return result ? {
          r: parseInt(result[1], 16),
          g: parseInt(result[2], 16),
          b: parseInt(result[3], 16)
        } : null
      }
      
      const bgRgb = hexToRgb(bg)
      const accentRgb = hexToRgb(accent)
      const textRgb = hexToRgb(textColor)
      
      // Helper function to format numbers
      const formatNumber = (value, decimals) => {
        if (value === null || value === undefined || value === "N/A") return "N/A"
        const num = typeof value === 'string' ? parseFloat(value) : value
        if (isNaN(num)) return "N/A"
        return num.toFixed(decimals)
      }

      // Lighter background shade for sections (slightly lighter than main bg)
      const sectionBgRgb = {
        r: Math.min(255, bgRgb.r + 8),
        g: Math.min(255, bgRgb.g + 8),
        b: Math.min(255, bgRgb.b + 8)
      }

      // Background
      doc.setFillColor(bgRgb.r, bgRgb.g, bgRgb.b)
      doc.rect(0, 0, 210, 297, "F")
      
      // Neon border
      doc.setDrawColor(accentRgb.r, accentRgb.g, accentRgb.b)
      doc.setLineWidth(1.5)
      doc.rect(5, 5, 200, 287)
      
      // Centered Title (larger font)
      doc.setTextColor(accentRgb.r, accentRgb.g, accentRgb.b)
      doc.setFontSize(32)
      doc.setFont("helvetica", "bold")
      const pdfTitle = data.name ? t('dashboard.fitnessProfileName', { name: data.name }) : t('dashboard.fitnessProfile')
      const pageCenter = 105 // A4 width is 210mm, center is 105mm
      doc.text(pdfTitle, pageCenter, 25, { align: "center" })
      
      // Horizontal divider line under title
      doc.setDrawColor(accentRgb.r, accentRgb.g, accentRgb.b)
      doc.setLineWidth(0.5)
      doc.line(30, 30, 180, 30)
      
      // Date (centered, smaller)
      doc.setFontSize(10)
      doc.setFont("helvetica", "normal")
      doc.setTextColor(textRgb.r, textRgb.g, textRgb.b)
      doc.text(t('dashboard.generatedOn', { date: today }), pageCenter, 36, { align: "center" })
      
      let y = 50

      // PERSONAL INFORMATION Section
      const sectionPadding = 3
      const sectionHeight = 25
      const sectionWidth = 170
      const sectionX = 20
      
      // Section background (rounded rectangle effect)
      doc.setFillColor(sectionBgRgb.r, sectionBgRgb.g, sectionBgRgb.b)
      doc.roundedRect(sectionX, y, sectionWidth, sectionHeight, 2, 2, "F")
      
      // Section title
      doc.setFontSize(14)
      doc.setFont("helvetica", "bold")
      doc.setTextColor(accentRgb.r, accentRgb.g, accentRgb.b)
      doc.text(t('dashboard.personalInformation'), sectionX + 5, y + 8)
      
      // Section content
      y += 15
      doc.setFontSize(11)
      doc.setFont("helvetica", "normal")
      doc.setTextColor(textRgb.r, textRgb.g, textRgb.b)
      doc.text(`${t('dashboard.name')}: ${data.name || "N/A"}`, sectionX + 5, y)
      y += 8
      doc.text(`${t('dashboard.email')}: ${data.email || "N/A"}`, sectionX + 5, y)
      y += 8
      doc.text(`${t('dashboard.gender')}: ${data.sex !== null && data.sex !== undefined ? (data.sex ? t('dashboard.male') : t('dashboard.female')) : "N/A"}`, sectionX + 5, y)
      y += 12 // Bottom padding

      // BODY METRICS Section
      // Section background
      doc.setFillColor(sectionBgRgb.r, sectionBgRgb.g, sectionBgRgb.b)
      doc.roundedRect(sectionX, y, sectionWidth, 50, 2, 2, "F")
      
      // Section title
      doc.setFontSize(14)
      doc.setFont("helvetica", "bold")
      doc.setTextColor(accentRgb.r, accentRgb.g, accentRgb.b)
      doc.text(t('dashboard.bodyMetrics'), sectionX + 5, y + 8)
      
      // Section content
      y += 15
      doc.setFontSize(11)
      doc.setFont("helvetica", "normal")
      doc.setTextColor(textRgb.r, textRgb.g, textRgb.b)
      
      // Height
      doc.text(`${t('dashboard.height')}: `, sectionX + 5, y)
      doc.setFont("helvetica", "bold")
      doc.text(`${data.height ? formatNumber(data.height, 0) : "N/A"} ${t('dashboard.cm')}`, sectionX + 35, y)
      y += 8
      
      // Weight
      doc.setFont("helvetica", "normal")
      doc.text(`${t('dashboard.weight')}: `, sectionX + 5, y)
      doc.setFont("helvetica", "bold")
      doc.text(`${data.weight ? formatNumber(data.weight, 0) : "N/A"} ${t('dashboard.kg')}`, sectionX + 35, y)
      y += 8
      
      // BMI
      doc.setFont("helvetica", "normal")
      doc.text(`${t('dashboard.bmiValue')}: `, sectionX + 5, y)
      doc.setFont("helvetica", "bold")
      doc.text(`${formatNumber(data.bmi, 1)}`, sectionX + 35, y)
      y += 8
      
      // WHR
      doc.setFont("helvetica", "normal")
      doc.text(`${t('dashboard.whrValue')}: `, sectionX + 5, y)
      doc.setFont("helvetica", "bold")
      doc.text(`${formatNumber(data.whr, 3)}`, sectionX + 35, y)
      y += 8
      
      // Body Fat
      doc.setFont("helvetica", "normal")
      doc.text(`${t('dashboard.bodyFat')}: `, sectionX + 5, y)
      doc.setFont("helvetica", "bold")
      doc.text(`${formatNumber(data.bodyFat, 1)}${t('dashboard.percent')}`, sectionX + 40, y)
      y += 8
      
      // BMR
      doc.setFont("helvetica", "normal")
      doc.text(`${t('dashboard.bmrValue')}: `, sectionX + 5, y)
      doc.setFont("helvetica", "bold")
      doc.text(`${formatNumber(data.bmr, 0)} ${t('dashboard.kcalPerDayShort')}`, sectionX + 35, y)
      y += 8
      
      // TDEE
      doc.setFont("helvetica", "normal")
      doc.text(`${t('dashboard.tdeeValue')}: `, sectionX + 5, y)
      doc.setFont("helvetica", "bold")
      doc.text(`${formatNumber(data.tdee, 0)} ${t('dashboard.kcalPerDayShort')}`, sectionX + 35, y)
      y += 12 // Bottom padding

      // DAILY NUTRITION TARGETS Section
      // Section background
      doc.setFillColor(sectionBgRgb.r, sectionBgRgb.g, sectionBgRgb.b)
      doc.roundedRect(sectionX, y, sectionWidth, 35, 2, 2, "F")
      
      // Section title
      doc.setFontSize(14)
      doc.setFont("helvetica", "bold")
      doc.setTextColor(accentRgb.r, accentRgb.g, accentRgb.b)
      doc.text(t('dashboard.dailyNutritionTargets'), sectionX + 5, y + 8)
      
      // Section content
      y += 15
      doc.setFontSize(11)
      doc.setFont("helvetica", "normal")
      doc.setTextColor(textRgb.r, textRgb.g, textRgb.b)
      
      // Calories Needed
      doc.text(`${t('dashboard.caloriesNeeded')}: `, sectionX + 5, y)
      doc.setFont("helvetica", "bold")
      doc.text(`${formatNumber(data.caloriesNeeded, 0)} ${t('dashboard.kcalPerDayShort')}`, sectionX + 50, y)
      y += 8
      
      // Protein
      doc.setFont("helvetica", "normal")
      doc.text(`${t('dashboard.protein')}: `, sectionX + 5, y)
      doc.setFont("helvetica", "bold")
      doc.text(`${formatNumber(data.protein, 0)} ${t('dashboard.gPerDay')}`, sectionX + 35, y)
      y += 8
      
      // Carbs
      doc.setFont("helvetica", "normal")
      doc.text(`${t('dashboard.carbs')}: `, sectionX + 5, y)
      doc.setFont("helvetica", "bold")
      doc.text(`${formatNumber(data.carbs, 0)} ${t('dashboard.gPerDay')}`, sectionX + 35, y)
      y += 8
      
      // Fat
      doc.setFont("helvetica", "normal")
      doc.text(`${t('dashboard.fat')}: `, sectionX + 5, y)
      doc.setFont("helvetica", "bold")
      doc.text(`${formatNumber(data.fat, 0)} ${t('dashboard.gPerDay')}`, sectionX + 35, y)
      
      const filename = `profile-${today.replace(/\//g, "-")}.pdf`
      doc.save(filename)
    } catch (err) {
      console.error("EXPORT ERROR:", err);
      console.error("EXPORT ERROR MESSAGE:", err.message);
      console.error("EXPORT ERROR STACK:", err.stack);
      console.error("EXPORT ERROR FULL:", JSON.stringify(err, null, 2));
      // Temporarily show raw error instead of generic message
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

  if (loading) {
    return (
      <div className="dashboard-page">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>{t('dashboard.loadingDashboard')}</p>
        </div>
      </div>
    )
  }

  if (error) {
    // If error is about incomplete profile, we should have already redirected
    // But keep this as a fallback
    if (error.includes('Profile incomplete') || error.includes('complete your profile')) {
      // Redirect will happen in useEffect if not already done
      return null
    }
    
    return (
      <div className="dashboard-page">
        <div className="error-container">
          <h2>{t('dashboard.error')}</h2>
          <p>{error}</p>
          <div className="error-actions">
            <button onClick={fetchFullAnalysis} className="retry-button">
              {t('dashboard.retry')}
            </button>
            <button onClick={() => navigate('/profile-setup')} className="setup-button">
              {t('dashboard.completeProfileSetup')}
            </button>
          </div>
        </div>
      </div>
    )
  }

  if (!analysis) {
    return (
      <div className="dashboard-page">
        <div className="empty-container">
          <h2>{t('dashboard.noAnalysisData')}</h2>
          <p>{t('dashboard.completeProfileToSeeAnalysis')}</p>
          <button onClick={() => navigate('/profile-setup')} className="setup-button">
            {t('dashboard.completeProfileSetup')}
          </button>
        </div>
      </div>
    )
  }


  return (
    <div className="dashboard-root">
      <header className="app-header">
        <div className="app-header-inner">
          {/* Left: Logo */}
          <div className="app-logo" onClick={() => navigate('/dashboard')}>
            AI Fitness
          </div>

          {/* Center: Empty */}

          {/* Right: Language Toggle, Edit Profile, Download PDF, Feedback & Logout */}
          <div className="app-actions">
            <button
              className="language-toggle"
              onClick={() => {
                const newLang = i18n.language === 'en' ? 'vi' : 'en'
                i18n.changeLanguage(newLang)
              }}
            >
              <span className={i18n.language === 'en' ? 'active' : ''}>EN</span>
              <span className="divider">|</span>
              <span className={i18n.language === 'vi' ? 'active' : ''}>VI</span>
            </button>
            <button
              className="dashboard-action-btn primary-btn"
              onClick={() => navigate('/profile/edit')}
            >
              {t('dashboard.editProfile') || '✏️ Edit Profile'}
            </button>
            <button
              className="dashboard-action-btn primary-btn"
              onClick={downloadProfilePdf}
            >
              {t('dashboard.downloadPdf') || 'Download PDF'}
            </button>
            <button
              className="dashboard-action-btn primary-btn"
              onClick={() => setIsFeedbackModalOpen(true)}
            >
              {t('dashboard.feedback') || 'Feedback'}
            </button>
            <button
              className="dashboard-action-btn danger-btn"
              onClick={() => {
                if (confirm(t('dashboard.logOutConfirm'))) {
                  logout();
                  navigate("/");
                }
              }}
            >
              {t('dashboard.logOut')}
            </button>
          </div>
        </div>
      </header>

      <div className="dashboard-page">
        {/* Single wrapper for ALL dashboard sections with consistent width */}
        <div className="dashboard-content-wrapper">
          {/* Greeting Section - Below header */}
          <section className="dashboard-greeting">
            <h2>{profile?.name ? `Hello, ${profile.name} 👋` : 'Hello 👋'}</h2>
            <p>Here's your fitness overview today</p>
          </section>

          {/* Daily Summary Strip - Key metrics at a glance */}
          <div className="dashboard-top-section">
            <DailySummaryStrip
              gamificationStatus={gamificationStatus}
              calorieTarget={analysis?.goalCalories}
              proteinTarget={analysis?.proteinTarget}
            />
          </div>

          {/* XP Board - Compact gamification display */}
          {gamificationStatus && (
            <div className="dashboard-top-section">
              <XPBoard
                xp={gamificationStatus.xp ?? 0}
                currentStreakDays={gamificationStatus.currentStreakDays ?? 0}
                onOpenDetails={() => setIsAchievementsModalOpen(true)}
              />
            </div>
          )}

          {/* Dashboard Grid Layout */}
          <div className="dashboard-grid">
        {/* Main Content Column */}
        <div className="dashboard-main-content">

          {/* Daily Challenges */}
          {gamificationStatus && (
            <div className="dashboard-section">
              <DailyChallenges />
            </div>
          )}

          {/* Stats and Progress Container - Shared Width */}
          <div className="stats-progress-container">
            {/* User Stats Section - Tab Layout */}
            <div className="dashboard-section">
              <h2>{t('dashboard.yourStats')}</h2>
              <p className="section-subtitle">{t('dashboard.trackMetrics')}</p>
              
              {/* Tab Navigation */}
              <div className="stats-tabs">
                <button
                  className={`stats-tab ${activeStatTab === 'BMI' ? 'active' : ''}`}
                  onClick={() => setActiveStatTab('BMI')}
                >
                  {t('dashboard.bmiValue')}
                </button>
                <button
                  className={`stats-tab ${activeStatTab === 'WHR' ? 'active' : ''}`}
                  onClick={() => setActiveStatTab('WHR')}
                >
                  {t('dashboard.whrValue')}
                </button>
              </div>

              {/* Tab Content */}
              <div className="stats-tab-content">
                {activeStatTab === 'BMI' && (
                  <div className="stat-card primary">
                    <div className="stat-label">BMI</div>
                    <div className="stat-value">{analysis.bmi ? analysis.bmi.toFixed(1) : 'N/A'}</div>
                    <div className="stat-category">{analysis.bmiCategory || 'N/A'}</div>
                  </div>
                )}
                {activeStatTab === 'WHR' && (
                  <div className="stat-card secondary">
                    <div className="stat-label">WHR</div>
                    <div className="stat-value">{analysis.whr ? analysis.whr.toFixed(2) : 'N/A'}</div>
                    <div className="stat-category">{analysis.whrRisk || 'N/A'}</div>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* AI Features Section */}
          <div className="dashboard-section ai-features-section">
            <h2>{t('dashboard.aiFeatures')}</h2>
            <p className="section-subtitle">{t('dashboard.aiFeaturesSubtitle')}</p>
            <div className="ai-features-grid">
              <button
                className="ai-feature-card ai-feature-card-dominant"
                onClick={() => window.open('/ai-coach', '_blank')}
              >
                <div className="ai-feature-badge">{t('dashboard.recommended')}</div>
                <div className="ai-feature-icon">🤖</div>
                <div className="ai-feature-title">{t('dashboard.aiCoach')}</div>
                <div className="ai-feature-description">
                  {t('dashboard.aiCoachDescription')}
                </div>
              </button>
              <button
                className="ai-feature-card"
                onClick={() => setIsMealPlannerOpen(true)}
              >
                <div className="ai-feature-icon">🍽️</div>
                <div className="ai-feature-title">{t('dashboard.aiMealPlanner')}</div>
                <div className="ai-feature-description">
                  {t('dashboard.aiMealPlannerDescription')}
                </div>
              </button>
            </div>
          </div>

          {/* BMI Details */}
          <div className="dashboard-section">
            <h2>{t('dashboard.bmi')}</h2>
            <p className="section-subtitle">{t('dashboard.bmiSubtitle')}</p>
            <div className="metric-card bmi">
              <div className="metric-header">
                <h3>{t('dashboard.yourBMI')}</h3>
                <div className="bmi-value">{analysis.bmi ? analysis.bmi.toFixed(1) : 'N/A'}</div>
              </div>
              <div className="metric-body">
                <div className="category-badge">{analysis.bmiCategory || 'N/A'}</div>
                <div className="bmi-bar">
                  <div 
                    className="bmi-indicator"
                    style={{
                      left: `${Math.min(100, Math.max(0, ((analysis.bmi || 0) - 15) / 30 * 100))}%`
                    }}
                  ></div>
                </div>
                <div className="bmi-labels">
                  <span>{t('dashboard.underweight')}</span>
                  <span>{t('dashboard.normal')}</span>
                  <span>{t('dashboard.overweight')}</span>
                  <span>{t('dashboard.obese')}</span>
                </div>
              </div>
            </div>
          </div>

          {/* WHR Details */}
          <div className="dashboard-section">
            <h2>{t('dashboard.whr')}</h2>
            <p className="section-subtitle">{t('dashboard.whrSubtitle')}</p>
            <div className="metric-card whr">
              <div className="metric-header">
                <h3>{t('dashboard.yourWHR')}</h3>
                <div className="whr-value">{analysis.whr ? analysis.whr.toFixed(2) : 'N/A'}</div>
              </div>
              <div className="metric-body">
                <div className={`risk-badge ${analysis.whrRisk === 'Good condition' ? 'good' : 'risk'}`}>
                  {analysis.whrRisk || 'N/A'}
                </div>
                <p className="risk-interpretation">
                  {analysis.whrRisk === 'Good condition' 
                    ? t('dashboard.whrGoodCondition')
                    : t('dashboard.whrAtRisk')}
                </p>
              </div>
            </div>
          </div>

          {/* Nutrition Hub */}
          <div className="dashboard-section nutrition-hub">
            <h2>{t('dashboard.nutritionHub')}</h2>
            <p className="section-subtitle">{t('dashboard.nutritionHubSubtitle')}</p>
            
            {/* Energy & Calories */}
            <div className="nutrition-hub-section">
              <h3 className="nutrition-hub-subtitle">{t('dashboard.energyCalories')}</h3>
              <div className="energy-grid">
                <div className="energy-card">
                  <div className="energy-label">{t('dashboard.bmr')}</div>
                  <div className="energy-value">{analysis.bmr ? Math.round(analysis.bmr) : 'N/A'}</div>
                  <div className="energy-unit">{t('dashboard.kcalPerDay')}</div>
                  <div className="energy-description">{t('dashboard.bmrDescription')}</div>
                </div>
                <div className="energy-card">
                  <div className="energy-label">{t('dashboard.tdee')}</div>
                  <div className="energy-value">{analysis.tdee ? Math.round(analysis.tdee) : 'N/A'}</div>
                  <div className="energy-unit">{t('dashboard.kcalPerDay')}</div>
                  <div className="energy-description">{t('dashboard.tdeeDescription')}</div>
                </div>
                <div className="energy-card highlight">
                  <div className="energy-label">{t('dashboard.goalCalories')}</div>
                  <div className="energy-value">{analysis.goalCalories ? Math.round(analysis.goalCalories) : 'N/A'}</div>
                  <div className="energy-unit">{t('dashboard.kcalPerDay')}</div>
                  <div className="energy-description">{t('dashboard.goalCaloriesDescription')}</div>
                </div>
              </div>
            </div>

            {/* Protein Target */}
            <div className="nutrition-hub-section">
              <h3 className="nutrition-hub-subtitle">{t('dashboard.nutritionTarget')}</h3>
              <div className="protein-card">
                <div className="protein-header">
                  <h3>{t('dashboard.dailyProteinTarget')}</h3>
                  <div className="protein-value">{analysis.proteinTarget ? Math.round(analysis.proteinTarget) : 'N/A'}</div>
                  <div className="protein-unit">{t('dashboard.gramsPerDay')}</div>
                </div>
                <div className="protein-body">
                  <div className="protein-bar">
                    <div 
                      className="protein-fill"
                      style={{ width: `${Math.min(100, ((analysis.proteinTarget || 0) / 200) * 100)}%` }}
                    ></div>
                  </div>
                  <p className="protein-tip">
                    {t('dashboard.proteinTip')}
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Health Tips */}
          <div className="dashboard-section">
            <h2>{t('dashboard.healthTips')}</h2>
            <p className="section-subtitle">{t('dashboard.healthTipsSubtitle')}</p>
            <div className="health-tips-card">
              <div className="tip-icon">💡</div>
              <p className="health-tip">{getHealthTip()}</p>
            </div>
          </div>

            {/* Health Recommendations - Progress Container */}
            <div className="dashboard-section">
              <h2>{t('dashboard.healthRecommendations')}</h2>
              <p className="section-subtitle">{t('dashboard.healthRecommendationsSubtitle')}</p>
              <div className="recommendations-card">
                <HealthRecommendations 
                  analysis={analysis} 
                  profile={profile}
                  onOpenFeedback={() => setIsFeedbackModalOpen(true)}
                  onOpenAchievements={() => setIsAchievementsModalOpen(true)}
                  gamificationStatus={gamificationStatus}
                />
              </div>
            </div>
          </div>
        </div>
        </div>
      </div>

      {/* Modals - Rendered at root level */}
      {isFeedbackModalOpen && (
        <FeedbackModal onClose={() => setIsFeedbackModalOpen(false)} />
      )}

      {isAchievementsModalOpen && gamificationStatus && (
        <AchievementsModal 
          badges={gamificationStatus.badges || []}
          onClose={() => setIsAchievementsModalOpen(false)} 
        />
      )}

      {isMealPlannerOpen && (
        <MealPreferencesModal onClose={() => setIsMealPlannerOpen(false)} />
      )}
    </div>
  )
}

// AI Coach Panel Component
function AICoachPanel({ advice, loading, error, onRetry }) {
  // Show loading spinner while waiting for API response
  if (loading) {
    return (
      <div className="ai-coach-card">
        <div className="ai-coach-loading">
          <div className="loading-spinner-small"></div>
          <p>{t('dashboard.analyzingProgress')}</p>
        </div>
      </div>
    )
  }

  // Handle errors gracefully
  if (error) {
    return (
      <div className="ai-coach-card">
        <div className="ai-coach-error">
          <p>{t('dashboard.unableToLoadAdvice')}</p>
          {onRetry && (
            <button 
              onClick={onRetry} 
              className="retry-button-small"
              style={{ marginTop: '12px' }}
            >
              {t('dashboard.retry')}
            </button>
          )}
        </div>
      </div>
    )
  }

  // Check if advice is null, undefined, or empty
  const hasAdvice = advice && (
    (advice.summary && advice.summary.trim() !== '') ||
    (advice.recommendations && advice.recommendations.length > 0)
  )

  // Show fallback message if no advice is available
  if (!hasAdvice) {
    return (
      <div className="ai-coach-card">
        <div className="ai-coach-empty">
          <p>{t('dashboard.aiCoachAnalyzing')}</p>
        </div>
      </div>
    )
  }

  // Display advice content
  const summaryText = advice.summary && advice.summary.trim() !== '' 
    ? advice.summary 
    : t('dashboard.aiCoachAnalyzing')

  return (
    <div className="ai-coach-card">
      <div className="ai-coach-content">
        <div className="ai-coach-summary">
          <h3>{t('dashboard.summary')}</h3>
          <p>{summaryText}</p>
        </div>
        
        {advice.recommendations && advice.recommendations.length > 0 && (
          <div className="ai-coach-recommendations">
            <h3>{t('dashboard.recommendations')}</h3>
            <ul>
              {advice.recommendations.map((recommendation, index) => (
                <li key={index}>{recommendation}</li>
              ))}
            </ul>
          </div>
        )}
      </div>
    </div>
  )
}

// Health Recommendations Component
function HealthRecommendations({
  analysis,
  profile,
  onOpenFeedback,
  onOpenAchievements,
  gamificationStatus,
  ...props
}) {
  const { t } = useTranslation()

  if (!analysis) {
    return (
      <div className="recommendations-loading">
        <p>{t('healthRecommendations.loadingRecommendations')}</p>
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

  const getActivitySuggestions = () => {
    const activityLevel = profile?.activityLevel || 3
    const suggestions = {
      1: {
        title: t('healthRecommendations.sedentaryTitle'),
        content: t('healthRecommendations.sedentaryContent'),
        activities: ['Daily walking (20-30 min)', 'Light stretching or yoga', 'Gardening or household chores', 'Gentle swimming']
      },
      2: {
        title: t('healthRecommendations.lightlyActiveTitle'),
        content: t('healthRecommendations.lightlyActiveContent'),
        activities: ['Moderate walking or jogging (30-45 min)', 'Strength training 2×/week', 'Cycling or swimming', 'Group fitness classes']
      },
      3: {
        title: t('healthRecommendations.moderatelyActiveTitle'),
        content: t('healthRecommendations.moderatelyActiveContent'),
        activities: ['Cardiovascular exercise 3×/week', 'Strength training 2×/week', 'High-intensity intervals (HIIT)', 'Active recovery days (yoga, walking)']
      },
      4: {
        title: t('healthRecommendations.veryActiveTitle'),
        content: t('healthRecommendations.veryActiveContent'),
        activities: ['Mixed cardio and strength training', 'Sport-specific training', 'Flexibility and mobility work', 'Adequate rest and recovery']
      },
      5: {
        title: t('healthRecommendations.extraActiveTitle'),
        content: t('healthRecommendations.extraActiveContent'),
        activities: ['Periodized training program', 'Sport-specific conditioning', 'Recovery and mobility work', 'Proper nutrition and hydration']
      }
    }

    return suggestions[activityLevel] || suggestions[3]
  }

  const getGoalBasedTips = () => {
    const goal = profile?.calorieGoal || 2
    const tips = {
      1: {
        title: t('healthRecommendations.loseWeightTitle'),
        content: t('healthRecommendations.loseWeightContent'),
        tips: ['Create a 500-750 calorie deficit daily', 'Prioritize protein (2g per kg body weight)', 'Include strength training 2-3×/week', 'Track food intake and stay consistent']
      },
      2: {
        title: t('healthRecommendations.maintainWeightTitle'),
        content: t('healthRecommendations.maintainWeightContent'),
        tips: ['Eat at your TDEE (maintenance calories)', 'Maintain balanced macronutrient intake', 'Continue regular exercise routine', 'Monitor weight weekly and adjust if needed']
      },
      3: {
        title: t('healthRecommendations.gainMuscleTitle'),
        content: t('healthRecommendations.gainMuscleContent'),
        tips: ['Eat 300-500 calories above TDEE', 'Prioritize protein (1.6-2.2g per kg)', 'Progressive overload in strength training', 'Ensure adequate rest and recovery']
      },
      4: {
        title: t('healthRecommendations.bodyRecompTitle'),
        content: t('healthRecommendations.bodyRecompContent'),
        tips: ['Eat at maintenance or slight surplus', 'High protein intake (2.2-2.5g per kg)', 'Heavy strength training 3-4×/week', 'Moderate cardio 2-3×/week']
      }
    }

    return tips[goal] || tips[2]
  }

  const whrRec = getWHRRecommendations()
  const heartInfo = getHeartDiseaseInfo()
  const activityRec = getActivitySuggestions()
  const goalTips = getGoalBasedTips()

  return (
    <div className="recommendations-content">
      {/* WHR Recommendations */}
      <div className={`recommendation-item ${whrRec.severity}`}>
        <div className="recommendation-header">
          <h3>{whrRec.title}</h3>
          <span className={`severity-badge ${whrRec.severity}`}>
            {whrRec.severity === 'warning' ? '⚠️' : '✅'}
          </span>
        </div>
        <p className="recommendation-explanation">{whrRec.explanation}</p>
        <ul className="risk-list">
          {whrRec.risks.map((risk, index) => (
            <li key={index}>• {risk}</li>
          ))}
        </ul>
        <p className="recommendation-conclusion">{whrRec.content}</p>
      </div>

      {/* Heart Disease Information */}
      <div className={`recommendation-item ${heartInfo.severity}`}>
        <div className="recommendation-header">
          <h3>{heartInfo.title}</h3>
          <span className={`severity-badge ${heartInfo.severity}`}>❤️</span>
        </div>
        <p>{heartInfo.content}</p>
      </div>

      {/* Activity Suggestions */}
      <div className="recommendation-item info">
        <div className="recommendation-header">
          <h3>{activityRec.title}</h3>
          <span className="severity-badge info">🏃</span>
        </div>
        <p>{activityRec.content}</p>
        <ul className="activity-list">
          {activityRec.activities.map((activity, index) => (
            <li key={index}>{activity}</li>
          ))}
        </ul>
      </div>

      {/* Goal-Based Tips */}
      <div className="recommendation-item info">
        <div className="recommendation-header">
          <h3>{goalTips.title}</h3>
          <span className="severity-badge info">🎯</span>
        </div>
        <p>{goalTips.content}</p>
        <ul className="goal-tips-list">
          {goalTips.tips.map((tip, index) => (
            <li key={index}>{tip}</li>
          ))}
        </ul>
      </div>
    </div>
  )
}

export default Dashboard
