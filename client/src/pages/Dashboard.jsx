import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../services/api'
import './Dashboard.css'

function Dashboard() {
  const [analysis, setAnalysis] = useState(null)
  const [profile, setProfile] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const navigate = useNavigate()

  useEffect(() => {
    fetchFullAnalysis()
    fetchProfile()
  }, [])

  const fetchFullAnalysis = async () => {
    try {
      setLoading(true)
      setError(null)
      const response = await api.get('/profile/full-analysis')
      if (response.data.success) {
        setAnalysis(response.data.data)
      } else {
        setError(response.data.message || 'Failed to load analysis')
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load analysis. Please try again.')
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
      console.error('Error fetching profile:', err)
    }
  }

  const handleRecalculate = () => {
    fetchFullAnalysis()
    fetchProfile() // Also refresh profile data for recommendations
  }

  const getHealthTip = () => {
    if (!analysis) return null

    const tips = []
    
    // BMI tips
    if (analysis.bmiCategory === 'Underweight') {
      tips.push('Consider a gradual weight gain plan with nutrient-dense foods and strength training.')
    } else if (analysis.bmiCategory === 'Overweight' || analysis.bmiCategory.startsWith('Obese')) {
      tips.push('Focus on creating a sustainable calorie deficit through balanced diet and regular exercise.')
    } else if (analysis.bmiCategory === 'Normal') {
      tips.push('Maintain your healthy weight with consistent nutrition and regular physical activity.')
    }

    // WHR tips
    if (analysis.whrRisk === 'At risk') {
      tips.push('Consider reducing waist circumference through cardiovascular exercise and a balanced diet.')
    } else {
      tips.push('Your waist-to-hip ratio indicates good health. Keep up the great work!')
    }

    // TDEE and calories tips
    if (analysis.goalCalories < analysis.tdee - 300) {
      tips.push('You\'re in a calorie deficit. Ensure adequate protein intake to preserve muscle mass.')
    } else if (analysis.goalCalories > analysis.tdee + 200) {
      tips.push('You\'re in a calorie surplus. Focus on nutrient-dense foods and strength training.')
    } else {
      tips.push('Your calorie target aligns with your TDEE. Maintain this balance for steady progress.')
    }

    // Protein tips
    tips.push(`Aim to consume ${Math.round(analysis.proteinTarget)}g of protein daily to support your fitness goals.`)

    return tips[Math.floor(Math.random() * tips.length)] || 'Stay consistent with your nutrition and exercise plan!'
  }

  if (loading) {
    return (
      <div className="dashboard-page">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>Loading your dashboard...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="dashboard-page">
        <div className="error-container">
          <h2>Error</h2>
          <p>{error}</p>
          <div className="error-actions">
            <button onClick={fetchFullAnalysis} className="retry-button">
              Retry
            </button>
            <button onClick={() => navigate('/profile-setup')} className="setup-button">
              Complete Profile Setup
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
          <h2>No Analysis Data</h2>
          <p>Complete your profile setup to see your fitness analysis.</p>
          <button onClick={() => navigate('/profile-setup')} className="setup-button">
            Complete Profile Setup
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="dashboard-page">
      <div className="dashboard-header">
        <h1>Fitness Dashboard</h1>
        <div className="header-actions">
          <button onClick={() => navigate('/profile/edit')} className="edit-profile-button">
            ✏️ Edit Profile
          </button>
          <button onClick={handleRecalculate} className="recalculate-button" disabled={loading}>
            {loading ? 'Recalculating...' : '🔄 Recalculate'}
          </button>
        </div>
      </div>

      {/* User Stats Section */}
      <div className="dashboard-section">
        <h2>Your Stats</h2>
        <div className="stats-grid">
          <div className="stat-card primary">
            <div className="stat-label">BMI</div>
            <div className="stat-value">{analysis.bmi ? analysis.bmi.toFixed(1) : 'N/A'}</div>
            <div className="stat-category">{analysis.bmiCategory || 'N/A'}</div>
          </div>
          <div className="stat-card secondary">
            <div className="stat-label">WHR</div>
            <div className="stat-value">{analysis.whr ? analysis.whr.toFixed(2) : 'N/A'}</div>
            <div className="stat-category">{analysis.whrRisk || 'N/A'}</div>
          </div>
        </div>
      </div>

      {/* BMI Details */}
      <div className="dashboard-section">
        <h2>Body Mass Index (BMI)</h2>
        <div className="metric-card bmi">
          <div className="metric-header">
            <h3>Your BMI</h3>
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
              <span>Underweight</span>
              <span>Normal</span>
              <span>Overweight</span>
              <span>Obese</span>
            </div>
          </div>
        </div>
      </div>

      {/* WHR Details */}
      <div className="dashboard-section">
        <h2>Waist-to-Hip Ratio (WHR)</h2>
        <div className="metric-card whr">
          <div className="metric-header">
            <h3>Your WHR</h3>
            <div className="whr-value">{analysis.whr ? analysis.whr.toFixed(2) : 'N/A'}</div>
          </div>
          <div className="metric-body">
            <div className={`risk-badge ${analysis.whrRisk === 'Good condition' ? 'good' : 'risk'}`}>
              {analysis.whrRisk || 'N/A'}
            </div>
            <p className="risk-interpretation">
              {analysis.whrRisk === 'Good condition' 
                ? 'Your waist-to-hip ratio indicates a healthy body fat distribution.'
                : 'Consider reducing waist circumference through targeted exercise and nutrition.'}
            </p>
          </div>
        </div>
      </div>

      {/* Energy & Calories */}
      <div className="dashboard-section">
        <h2>Energy & Calories</h2>
        <div className="energy-grid">
          <div className="energy-card">
            <div className="energy-label">BMR</div>
            <div className="energy-value">{analysis.bmr ? Math.round(analysis.bmr) : 'N/A'}</div>
            <div className="energy-unit">kcal/day</div>
            <div className="energy-description">Basal Metabolic Rate<br />Calories at rest</div>
          </div>
          <div className="energy-card">
            <div className="energy-label">TDEE</div>
            <div className="energy-value">{analysis.tdee ? Math.round(analysis.tdee) : 'N/A'}</div>
            <div className="energy-unit">kcal/day</div>
            <div className="energy-description">Total Daily Energy Expenditure<br />Including activity</div>
          </div>
          <div className="energy-card highlight">
            <div className="energy-label">Goal Calories</div>
            <div className="energy-value">{analysis.goalCalories ? Math.round(analysis.goalCalories) : 'N/A'}</div>
            <div className="energy-unit">kcal/day</div>
            <div className="energy-description">Your daily calorie target<br />Based on your goal</div>
          </div>
        </div>
      </div>

      {/* Protein Target */}
      <div className="dashboard-section">
        <h2>Nutrition Target</h2>
        <div className="protein-card">
          <div className="protein-header">
            <h3>Daily Protein Target</h3>
            <div className="protein-value">{analysis.proteinTarget ? Math.round(analysis.proteinTarget) : 'N/A'}</div>
            <div className="protein-unit">grams/day</div>
          </div>
          <div className="protein-body">
            <div className="protein-bar">
              <div 
                className="protein-fill"
                style={{ width: `${Math.min(100, ((analysis.proteinTarget || 0) / 200) * 100)}%` }}
              ></div>
            </div>
            <p className="protein-tip">
              Spread protein intake across all meals to optimize muscle protein synthesis throughout the day.
            </p>
          </div>
        </div>
      </div>

      {/* Health Tips */}
      <div className="dashboard-section">
        <h2>Health Tips</h2>
        <div className="health-tips-card">
          <div className="tip-icon">💡</div>
          <p className="health-tip">{getHealthTip()}</p>
        </div>
      </div>

      {/* Health Recommendations */}
      <div className="dashboard-section">
        <h2>Health Recommendations</h2>
        <div className="recommendations-card">
          <HealthRecommendations analysis={analysis} profile={profile} />
        </div>
      </div>
    </div>
  )
}

// Health Recommendations Component
function HealthRecommendations({ analysis, profile }) {
  if (!analysis) {
    return (
      <div className="recommendations-loading">
        <p>Loading recommendations...</p>
      </div>
    )
  }

  const getWHRRecommendations = () => {
    // Based on Java code (mainOne.java lines 252-274)
    const whrExplanation = 'WHR measures fat distribution — whether someone stores more fat around the abdomen (visceral fat) or the hips/thighs (subcutaneous fat). Visceral fat (around the waist) is more dangerous because it surrounds internal organs and raises the risk of:'
    const risks = ['Heart disease', 'Type 2 diabetes', 'Stroke', 'Metabolic syndrome']
    
    if (analysis.whrRisk === 'At risk') {
      return {
        title: 'WHR Health Risk',
        explanation: whrExplanation,
        risks: risks,
        content: profile?.sex === 'Male' 
          ? 'Your WHR is above 0.9, which means you are at risk of those problems above.'
          : 'Your WHR is above 0.85, which means you are at risk of those problems above.',
        severity: 'warning'
      }
    } else {
      return {
        title: 'WHR Health Status',
        explanation: whrExplanation,
        risks: risks,
        content: profile?.sex === 'Male'
          ? 'Your WHR is below 0.9, which means you are in a good condition.'
          : 'Your WHR is below 0.85, which means you are in a good condition.',
        severity: 'good'
      }
    }
  }

  const getHeartDiseaseInfo = () => {
    return {
      title: 'Heart Disease Prevention',
      content: 'Maintaining a healthy waist-to-hip ratio is important for cardiovascular health. Abdominal obesity is a major risk factor for heart disease, stroke, and type 2 diabetes. Regular exercise, especially cardiovascular activities like walking, running, cycling, or swimming, can help reduce abdominal fat and improve heart health. Aim for at least 150 minutes of moderate-intensity exercise per week.',
      severity: 'info'
    }
  }

  const getActivitySuggestions = () => {
    const activityLevel = profile?.activityLevel || 3
    const suggestions = {
      1: {
        title: 'Sedentary Lifestyle - Activity Suggestions',
        content: 'Starting from a sedentary lifestyle, begin with light activities such as walking for 20-30 minutes daily, gentle stretching, or light yoga. Gradually increase duration and intensity over time. Even small increases in daily activity can significantly improve your health and metabolism.',
        activities: ['Daily walking (20-30 min)', 'Light stretching or yoga', 'Gardening or household chores', 'Gentle swimming']
      },
      2: {
        title: 'Lightly Active - Activity Suggestions',
        content: 'You\'re already doing light exercise 1-3 times per week. To progress, add one more session per week or increase the duration of existing workouts. Consider mixing in strength training twice a week to build muscle and boost metabolism.',
        activities: ['Moderate walking or jogging (30-45 min)', 'Strength training 2×/week', 'Cycling or swimming', 'Group fitness classes']
      },
      3: {
        title: 'Moderately Active - Activity Suggestions',
        content: 'You\'re exercising 3-5 times per week, which is excellent! To maximize results, focus on workout quality and variety. Include both cardiovascular and strength training, and ensure adequate rest days for recovery.',
        activities: ['Cardiovascular exercise 3×/week', 'Strength training 2×/week', 'High-intensity intervals (HIIT)', 'Active recovery days (yoga, walking)']
      },
      4: {
        title: 'Very Active - Activity Suggestions',
        content: 'You\'re exercising 6-7 times per week - impressive dedication! At this level, focus on workout periodization, proper nutrition, and recovery. Consider varying intensities and incorporating active recovery to prevent overtraining.',
        activities: ['Mixed cardio and strength training', 'Sport-specific training', 'Flexibility and mobility work', 'Adequate rest and recovery']
      },
      5: {
        title: 'Extra Active - Activity Suggestions',
        content: 'You\'re training twice daily - excellent commitment! Ensure you\'re prioritizing recovery, nutrition, and sleep. Periodize your training with varying intensities and include active recovery sessions to maintain performance.',
        activities: ['Periodized training program', 'Sport-specific conditioning', 'Recovery and mobility work', 'Proper nutrition and hydration']
      }
    }

    return suggestions[activityLevel] || suggestions[3]
  }

  const getGoalBasedTips = () => {
    const goal = profile?.calorieGoal || 2
    const tips = {
      1: {
        title: 'Lose Weight - Goal-Based Recommendations',
        content: 'The information below is suggested for your selected goal! For weight loss, maintain a moderate calorie deficit. Prioritize protein intake to preserve muscle mass during weight loss. Combine resistance training with cardiovascular exercise. Focus on whole foods, minimize processed foods, and stay hydrated.',
        tips: ['Create a 500-750 calorie deficit daily', 'Prioritize protein (2g per kg body weight)', 'Include strength training 2-3×/week', 'Track food intake and stay consistent']
      },
      2: {
        title: 'Maintain Weight - Goal-Based Recommendations',
        content: 'The information below is suggested for your selected goal! To maintain your current weight, eat at your TDEE (Total Daily Energy Expenditure). Focus on balanced nutrition with adequate protein, healthy fats, and complex carbohydrates. Continue regular exercise to maintain muscle mass and metabolism.',
        tips: ['Eat at your TDEE (maintenance calories)', 'Maintain balanced macronutrient intake', 'Continue regular exercise routine', 'Monitor weight weekly and adjust if needed']
      },
      3: {
        title: 'Gain Muscle - Goal-Based Recommendations',
        content: 'The information below is suggested for your selected goal! To gain muscle, eat in a slight calorie surplus with adequate protein. Prioritize progressive overload in strength training - gradually increase weight, reps, or sets over time. Focus on compound exercises and ensure sufficient rest and recovery.',
        tips: ['Eat 300-500 calories above TDEE', 'Prioritize protein (1.6-2.2g per kg)', 'Progressive overload in strength training', 'Ensure adequate rest and recovery']
      },
      4: {
        title: 'Body Recomposition - Goal-Based Recommendations',
        content: 'The information below is suggested for your selected goal! Body recomposition (losing fat while gaining muscle) requires precise nutrition and training. Eat at maintenance calories or slight surplus, with high protein intake. Combine heavy strength training with moderate cardio.',
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
