import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../services/api'
import { ERROR_MESSAGES, UI_LABELS, BUTTON_TEXT, STATUS_MESSAGES, PAGE_TITLES } from '../config/constants'
import jsPDF from 'jspdf'
import './ProfilePage.css'

function ProfilePage() {
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [downloading, setDownloading] = useState(false)
  const navigate = useNavigate()

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
    console.log("DOWNLOAD CLICKED");
    try {
      setDownloading(true)
      setError(null)
      
      // Call the export endpoint
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
        setDownloading(false);
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
      const pdfTitle = data.name ? `${data.name}'s Fitness Profile` : "Your Fitness Profile"
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
      doc.text(`Generated on ${today}`, pageCenter, 36, { align: "center" })
      
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
      doc.text("PERSONAL INFORMATION", sectionX + 5, y + 8)
      
      // Section content
      y += 15
      doc.setFontSize(11)
      doc.setFont("helvetica", "normal")
      doc.setTextColor(textRgb.r, textRgb.g, textRgb.b)
      doc.text(`Name: ${data.name || "N/A"}`, sectionX + 5, y)
      y += 8
      doc.text(`Email: ${data.email || "N/A"}`, sectionX + 5, y)
      y += 8
      doc.text(`Gender: ${data.sex !== null && data.sex !== undefined ? (data.sex ? "Male" : "Female") : "N/A"}`, sectionX + 5, y)
      y += 12 // Bottom padding

      // BODY METRICS Section
      // Section background
      doc.setFillColor(sectionBgRgb.r, sectionBgRgb.g, sectionBgRgb.b)
      doc.roundedRect(sectionX, y, sectionWidth, 50, 2, 2, "F")
      
      // Section title
      doc.setFontSize(14)
      doc.setFont("helvetica", "bold")
      doc.setTextColor(accentRgb.r, accentRgb.g, accentRgb.b)
      doc.text("BODY METRICS", sectionX + 5, y + 8)
      
      // Section content
      y += 15
      doc.setFontSize(11)
      doc.setFont("helvetica", "normal")
      doc.setTextColor(textRgb.r, textRgb.g, textRgb.b)
      
      // Height
      doc.text(`Height: `, sectionX + 5, y)
      doc.setFont("helvetica", "bold")
      doc.text(`${data.height ? formatNumber(data.height, 0) : "N/A"} cm`, sectionX + 35, y)
      y += 8
      
      // Weight
      doc.setFont("helvetica", "normal")
      doc.text(`Weight: `, sectionX + 5, y)
      doc.setFont("helvetica", "bold")
      doc.text(`${data.weight ? formatNumber(data.weight, 0) : "N/A"} kg`, sectionX + 35, y)
      y += 8
      
      // BMI
      doc.setFont("helvetica", "normal")
      doc.text(`BMI: `, sectionX + 5, y)
      doc.setFont("helvetica", "bold")
      doc.text(`${formatNumber(data.bmi, 1)}`, sectionX + 35, y)
      y += 8
      
      // WHR
      doc.setFont("helvetica", "normal")
      doc.text(`WHR: `, sectionX + 5, y)
      doc.setFont("helvetica", "bold")
      doc.text(`${formatNumber(data.whr, 3)}`, sectionX + 35, y)
      y += 8
      
      // Body Fat
      doc.setFont("helvetica", "normal")
      doc.text(`Body Fat: `, sectionX + 5, y)
      doc.setFont("helvetica", "bold")
      doc.text(`${formatNumber(data.bodyFat, 1)}%`, sectionX + 40, y)
      y += 8
      
      // BMR
      doc.setFont("helvetica", "normal")
      doc.text(`BMR: `, sectionX + 5, y)
      doc.setFont("helvetica", "bold")
      doc.text(`${formatNumber(data.bmr, 0)} kcal/day`, sectionX + 35, y)
      y += 8
      
      // TDEE
      doc.setFont("helvetica", "normal")
      doc.text(`TDEE: `, sectionX + 5, y)
      doc.setFont("helvetica", "bold")
      doc.text(`${formatNumber(data.tdee, 0)} kcal/day`, sectionX + 35, y)
      y += 12 // Bottom padding

      // DAILY NUTRITION TARGETS Section
      // Section background
      doc.setFillColor(sectionBgRgb.r, sectionBgRgb.g, sectionBgRgb.b)
      doc.roundedRect(sectionX, y, sectionWidth, 35, 2, 2, "F")
      
      // Section title
      doc.setFontSize(14)
      doc.setFont("helvetica", "bold")
      doc.setTextColor(accentRgb.r, accentRgb.g, accentRgb.b)
      doc.text("DAILY NUTRITION TARGETS", sectionX + 5, y + 8)
      
      // Section content
      y += 15
      doc.setFontSize(11)
      doc.setFont("helvetica", "normal")
      doc.setTextColor(textRgb.r, textRgb.g, textRgb.b)
      
      // Calories Needed
      doc.text(`Calories Needed: `, sectionX + 5, y)
      doc.setFont("helvetica", "bold")
      doc.text(`${formatNumber(data.caloriesNeeded, 0)} kcal/day`, sectionX + 50, y)
      y += 8
      
      // Protein
      doc.setFont("helvetica", "normal")
      doc.text(`Protein: `, sectionX + 5, y)
      doc.setFont("helvetica", "bold")
      doc.text(`${formatNumber(data.protein, 0)} g/day`, sectionX + 35, y)
      y += 8
      
      // Carbs
      doc.setFont("helvetica", "normal")
      doc.text(`Carbs: `, sectionX + 5, y)
      doc.setFont("helvetica", "bold")
      doc.text(`${formatNumber(data.carbs, 0)} g/day`, sectionX + 35, y)
      y += 8
      
      // Fat
      doc.setFont("helvetica", "normal")
      doc.text(`Fat: `, sectionX + 5, y)
      doc.setFont("helvetica", "bold")
      doc.text(`${formatNumber(data.fat, 0)} g/day`, sectionX + 35, y)
      
      const filename = `profile-${today.replace(/\//g, "-")}.pdf`
      doc.save(filename)
    } catch (err) {
      console.error("EXPORT ERROR:", err);
      console.error("EXPORT ERROR MESSAGE:", err.message);
      console.error("EXPORT ERROR STACK:", err.stack);
      console.error("EXPORT ERROR FULL:", JSON.stringify(err, null, 2));
      // Temporarily show raw error instead of generic message
      setError(err.message || err.toString() || ERROR_MESSAGES.EXPORT_FAILED)
    } finally {
      setDownloading(false)
    }
  }

  if (loading) {
    return (
      <div className="profile-page">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>{STATUS_MESSAGES.LOADING_PROFILE}</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="profile-page">
        <div className="error-container">
          <h2>Error</h2>
          <p>{error}</p>
          <button onClick={fetchProfile} className="retry-button">
            Retry
          </button>
        </div>
      </div>
    )
  }

  if (!profile) {
    return (
      <div className="profile-page">
        <div className="empty-container">
          <h2>{STATUS_MESSAGES.NO_PROFILE_DATA}</h2>
          <p>{STATUS_MESSAGES.PROFILE_NOT_AVAILABLE}</p>
        </div>
      </div>
    )
  }

  return (
    <div className="profile-page">
      <div className="profile-header">
        <h1>{PAGE_TITLES.PROFILE}</h1>
        <div className="header-actions">
          <button 
            onClick={(e) => {
              e.preventDefault();
              e.stopPropagation();
              downloadProfilePdf();
            }} 
            className="download-button"
            disabled={downloading}
          >
            {downloading ? UI_LABELS.DOWNLOADING : BUTTON_TEXT.DOWNLOAD_PROFILE}
          </button>
          <button onClick={fetchProfile} className="refresh-button">
            {BUTTON_TEXT.REFRESH}
          </button>
        </div>
      </div>

      {/* Personal Information */}
      <div className="profile-section">
        <h2>Personal Information</h2>
        <div className="info-grid">
          <div className="info-item">
            <span className="info-label">Name:</span>
            <span className="info-value">{profile.name || 'N/A'}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Username:</span>
            <span className="info-value">{profile.username}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Email:</span>
            <span className="info-value">{profile.email}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Age:</span>
            <span className="info-value">{profile.age ? `${profile.age} years` : 'N/A'}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Sex:</span>
            <span className="info-value">{profile.sex || 'N/A'}</span>
          </div>
        </div>
      </div>

      {/* Body Measurements */}
      <div className="profile-section">
        <h2>Body Measurements</h2>
        <div className="info-grid">
          <div className="info-item">
            <span className="info-label">Weight:</span>
            <span className="info-value">{profile.weight ? `${profile.weight.toFixed(1)} kg` : 'N/A'}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Height:</span>
            <span className="info-value">{profile.height ? `${profile.height.toFixed(1)} cm` : 'N/A'}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Waist:</span>
            <span className="info-value">{profile.waist ? `${profile.waist.toFixed(1)} cm` : 'N/A'}</span>
          </div>
          <div className="info-item">
            <span className="info-label">Hip:</span>
            <span className="info-value">{profile.hip ? `${profile.hip.toFixed(1)} cm` : 'N/A'}</span>
          </div>
        </div>
      </div>

      {/* Body Metrics */}
      {profile.bodyMetrics && (
        <div className="profile-section">
          <h2>Body Metrics</h2>
          <div className="metrics-grid">
            <div className="metric-card">
              <h3>BMI</h3>
              <div className="metric-value">{profile.bodyMetrics.bmi ? profile.bodyMetrics.bmi.toFixed(2) : 'N/A'}</div>
              <div className="metric-label">{profile.bodyMetrics.bmiCategory || 'N/A'}</div>
            </div>
            <div className="metric-card">
              <h3>WHR</h3>
              <div className="metric-value">{profile.bodyMetrics.whr ? profile.bodyMetrics.whr.toFixed(2) : 'N/A'}</div>
              <div className="metric-label">{profile.bodyMetrics.whrHealthStatus || 'N/A'}</div>
            </div>
            <div className="metric-card">
              <h3>WHtR</h3>
              <div className="metric-value">{profile.bodyMetrics.whtr ? profile.bodyMetrics.whtr.toFixed(2) : 'N/A'}</div>
              <div className="metric-label">{profile.bodyMetrics.whtrRiskLevel || 'N/A'}</div>
            </div>
            <div className="metric-card">
              <h3>Body Fat</h3>
              <div className="metric-value">{profile.bodyMetrics.bodyFat ? `${profile.bodyMetrics.bodyFat.toFixed(1)}%` : 'N/A'}</div>
              <div className="metric-label">Body Fat %</div>
            </div>
          </div>
        </div>
      )}

      {/* Energy & Calories */}
      {profile.energy && (
        <div className="profile-section">
          <h2>Energy & Calories</h2>
          <div className="info-grid">
            <div className="info-item">
              <span className="info-label">BMR:</span>
              <span className="info-value">{profile.energy.bmr ? `${profile.energy.bmr.toFixed(1)} kcal/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">TDEE:</span>
              <span className="info-value">{profile.energy.tdee ? `${profile.energy.tdee.toFixed(1)} kcal/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Goal Calories:</span>
              <span className="info-value">{profile.energy.goalCalories ? `${profile.energy.goalCalories.toFixed(1)} kcal/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Activity Level:</span>
              <span className="info-value">{profile.activityLevelName || 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Goal:</span>
              <span className="info-value">{profile.calorieGoalName || 'N/A'}</span>
            </div>
          </div>
        </div>
      )}

      {/* Macronutrients */}
      {profile.macronutrients && (
        <div className="profile-section">
          <h2>Macronutrients</h2>
          <div className="info-grid">
            <div className="info-item">
              <span className="info-label">Protein:</span>
              <span className="info-value">{profile.macronutrients.protein ? `${profile.macronutrients.protein.toFixed(1)} g/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Fat:</span>
              <span className="info-value">{profile.macronutrients.fat ? `${profile.macronutrients.fat.toFixed(1)} g/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Carbohydrates:</span>
              <span className="info-value">{profile.macronutrients.carbohydrates ? `${profile.macronutrients.carbohydrates.toFixed(1)} g/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Fiber:</span>
              <span className="info-value">{profile.macronutrients.fiber ? `${profile.macronutrients.fiber} g/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Water:</span>
              <span className="info-value">{profile.macronutrients.water ? `${profile.macronutrients.water.toFixed(1)} L/day` : 'N/A'}</span>
            </div>
          </div>
        </div>
      )}

      {/* Micronutrients */}
      {profile.micronutrients && (
        <div className="profile-section">
          <h2>Micronutrients</h2>
          <div className="info-grid">
            <div className="info-item">
              <span className="info-label">Iron:</span>
              <span className="info-value">{profile.micronutrients.iron ? `${profile.micronutrients.iron} mg/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Calcium:</span>
              <span className="info-value">{profile.micronutrients.calcium ? `${profile.micronutrients.calcium} mg/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Vitamin D:</span>
              <span className="info-value">{profile.micronutrients.vitaminD ? `${profile.micronutrients.vitaminD} µg/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Magnesium:</span>
              <span className="info-value">{profile.micronutrients.magnesium ? `${profile.micronutrients.magnesium} mg/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Zinc:</span>
              <span className="info-value">{profile.micronutrients.zinc ? `${profile.micronutrients.zinc} mg/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Vitamin B12:</span>
              <span className="info-value">{profile.micronutrients.vitaminB12 ? `${profile.micronutrients.vitaminB12} µg/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Potassium:</span>
              <span className="info-value">{profile.micronutrients.potassium ? `${profile.micronutrients.potassium} mg/day` : 'N/A'}</span>
            </div>
            <div className="info-item">
              <span className="info-label">Sodium:</span>
              <span className="info-value">{profile.micronutrients.sodium ? `${profile.micronutrients.sodium} mg/day` : 'N/A'}</span>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default ProfilePage

