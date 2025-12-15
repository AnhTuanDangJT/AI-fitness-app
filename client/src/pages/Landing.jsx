import React, { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useTranslation } from 'react-i18next'
import { isAuthenticated } from '../utils/auth'
import '../styles/landing.css'

function Landing() {
  const navigate = useNavigate()
  const { t, i18n } = useTranslation()
  const sectionsRef = useRef([])
  const [isTransitioning, setIsTransitioning] = useState(false)

  useEffect(() => {
    // Check if user is already logged in
    if (isAuthenticated()) {
      // Optionally redirect authenticated users to dashboard
      navigate('/dashboard', { replace: true })
    }
  }, [navigate])

  useEffect(() => {
    // IntersectionObserver for fade-in animations
    const observerOptions = {
      threshold: 0.1,
      rootMargin: '0px 0px -50px 0px'
    }

    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-visible')
          // Unobserve after animation to prevent re-triggering
          observer.unobserve(entry.target)
        }
      })
    }, observerOptions)

    // Observe all sections
    sectionsRef.current.forEach((section) => {
      if (section) {
        observer.observe(section)
      }
    })

    return () => {
      sectionsRef.current.forEach((section) => {
        if (section) {
          observer.unobserve(section)
        }
      })
    }
  }, [])

  return (
    <div className={`landing-page ${isTransitioning ? 'transitioning' : ''}`}>
      {/* Top Navigation Bar */}
      <nav className="landing-nav">
        <div className="landing-nav-container">
          <div className="landing-logo">
            <h2>AI Fitness</h2>
          </div>
          <div className="landing-nav-buttons">
            <button
              className="landing-language-toggle"
              onClick={() => {
                const newLang = i18n.language === 'en' ? 'vi' : 'en'
                setIsTransitioning(true)
                i18n.changeLanguage(newLang).then(() => {
                  setTimeout(() => setIsTransitioning(false), 300)
                })
              }}
            >
              <span className={i18n.language === 'en' ? 'active' : ''}>EN</span>
              <span className="separator">|</span>
              <span className={i18n.language === 'vi' ? 'active' : ''}>VI</span>
            </button>
            <button 
              className="landing-nav-button secondary"
              onClick={() => navigate('/login')}
            >
              {t('nav.logIn')}
            </button>
            <button 
              className="landing-nav-button primary"
              onClick={() => navigate('/signup')}
            >
              {t('nav.createAccount')}
            </button>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section 
        className="landing-hero fade-in-section"
        ref={(el) => (sectionsRef.current[0] = el)}
      >
        <div className="landing-hero-content">
          <h1 className="landing-hero-title">{t('hero.title')}</h1>
          <p className="landing-hero-subtitle">
            {t('hero.subtitle')}
          </p>
          <button 
            className="landing-cta-button"
            onClick={() => navigate('/signup')}
          >
            {t('hero.getStarted')}
          </button>
        </div>
      </section>

      {/* Problem → Solution Section */}
      <section 
        className="landing-problem-solution fade-in-section"
        ref={(el) => (sectionsRef.current[1] = el)}
      >
        <div className="landing-problem-solution-container">
          <div className="landing-problem-column">
            <h2 className="landing-section-title">{t('challenge.title')}</h2>
            <div className="landing-problem-item" style={{ '--delay': '0ms' }}>
              <div className="landing-problem-icon">❌</div>
              <div>
                <h3>{t('challenge.genericPlans.title')}</h3>
                <p>{t('challenge.genericPlans.description')}</p>
              </div>
            </div>
            <div className="landing-problem-item" style={{ '--delay': '100ms' }}>
              <div className="landing-problem-icon">❌</div>
              <div>
                <h3>{t('challenge.inconsistentTracking.title')}</h3>
                <p>{t('challenge.inconsistentTracking.description')}</p>
              </div>
            </div>
            <div className="landing-problem-item" style={{ '--delay': '200ms' }}>
              <div className="landing-problem-icon">❌</div>
              <div>
                <h3>{t('challenge.nutritionConfusion.title')}</h3>
                <p>{t('challenge.nutritionConfusion.description')}</p>
              </div>
            </div>
            <div className="landing-problem-item" style={{ '--delay': '300ms' }}>
              <div className="landing-problem-icon">❌</div>
              <div>
                <h3>{t('challenge.motivationFades.title')}</h3>
                <p>{t('challenge.motivationFades.description')}</p>
              </div>
            </div>
          </div>

          <div className="landing-solution-column">
            <h2 className="landing-section-title">{t('solution.title')}</h2>
            <div className="landing-solution-item" style={{ '--delay': '0ms' }}>
              <div className="landing-solution-icon">✅</div>
              <div>
                <h3>{t('solution.aiPersonalization.title')}</h3>
                <p>{t('solution.aiPersonalization.description')}</p>
              </div>
            </div>
            <div className="landing-solution-item" style={{ '--delay': '100ms' }}>
              <div className="landing-solution-icon">✅</div>
              <div>
                <h3>{t('solution.realTimeAnalytics.title')}</h3>
                <p>{t('solution.realTimeAnalytics.description')}</p>
              </div>
            </div>
            <div className="landing-solution-item" style={{ '--delay': '200ms' }}>
              <div className="landing-solution-icon">✅</div>
              <div>
                <h3>{t('solution.smartMealPlanning.title')}</h3>
                <p>{t('solution.smartMealPlanning.description')}</p>
              </div>
            </div>
            <div className="landing-solution-item" style={{ '--delay': '300ms' }}>
              <div className="landing-solution-icon">✅</div>
              <div>
                <h3>{t('solution.gamifiedConsistency.title')}</h3>
                <p>{t('solution.gamifiedConsistency.description')}</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Feature Summary Section */}
      <section 
        className="landing-features fade-in-section"
        ref={(el) => (sectionsRef.current[2] = el)}
      >
        <div className="landing-features-header">
          <h2 className="landing-section-title">{t('features.title')}</h2>
          <p className="landing-section-subtitle">{t('features.subtitle')}</p>
        </div>
        <div className="landing-features-container">
          <div className="landing-feature-card">
            <div className="landing-feature-icon">🤖</div>
            <h3 className="landing-feature-title">{t('features.aiCoach.title')}</h3>
            <p className="landing-feature-value">{t('features.aiCoach.value')}</p>
            <p className="landing-feature-description">
              {t('features.aiCoach.description')}
            </p>
            <ul className="landing-feature-benefits">
              <li>{t('features.aiCoach.benefits.continuous')}</li>
              <li>{t('features.aiCoach.benefits.evidence')}</li>
              <li>{t('features.aiCoach.benefits.habits')}</li>
            </ul>
          </div>

          <div className="landing-feature-card">
            <div className="landing-feature-icon">🍽️</div>
            <h3 className="landing-feature-title">{t('features.mealPlanner.title')}</h3>
            <p className="landing-feature-value">{t('features.mealPlanner.value')}</p>
            <p className="landing-feature-description">
              {t('features.mealPlanner.description')}
            </p>
            <ul className="landing-feature-benefits">
              <li>{t('features.mealPlanner.benefits.macro')}</li>
              <li>{t('features.mealPlanner.benefits.workflow')}</li>
              <li>{t('features.mealPlanner.benefits.patterns')}</li>
            </ul>
          </div>

          <div className="landing-feature-card">
            <div className="landing-feature-icon">📊</div>
            <h3 className="landing-feature-title">{t('features.bodyMetrics.title')}</h3>
            <p className="landing-feature-value">{t('features.bodyMetrics.value')}</p>
            <p className="landing-feature-description">
              {t('features.bodyMetrics.description')}
            </p>
            <ul className="landing-feature-benefits">
              <li>{t('features.bodyMetrics.benefits.analysis')}</li>
              <li>{t('features.bodyMetrics.benefits.optimization')}</li>
              <li>{t('features.bodyMetrics.benefits.visualization')}</li>
            </ul>
          </div>

          <div className="landing-feature-card">
            <div className="landing-feature-icon">🎮</div>
            <h3 className="landing-feature-title">{t('features.gamification.title')}</h3>
            <p className="landing-feature-value">{t('features.gamification.value')}</p>
            <p className="landing-feature-description">
              {t('features.gamification.description')}
            </p>
            <ul className="landing-feature-benefits">
              <li>{t('features.gamification.benefits.tracking')}</li>
              <li>{t('features.gamification.benefits.recognition')}</li>
              <li>{t('features.gamification.benefits.framework')}</li>
            </ul>
          </div>
        </div>
      </section>

      {/* How It Works Section */}
      <section 
        className="landing-how-it-works fade-in-section"
        ref={(el) => (sectionsRef.current[4] = el)}
      >
        <div className="landing-how-it-works-container">
          <h2 className="landing-section-title">{t('howItWorks.title')}</h2>
          <p className="landing-section-subtitle">{t('howItWorks.subtitle')}</p>
          <div className="landing-steps">
            <div className="landing-step">
              <div className="landing-step-number">1</div>
              <h3 className="landing-step-title">{t('howItWorks.step1.title')}</h3>
              <p className="landing-step-description">
                {t('howItWorks.step1.description')}
              </p>
            </div>
            <div className="landing-step-separator"></div>
            <div className="landing-step">
              <div className="landing-step-number">2</div>
              <h3 className="landing-step-title">{t('howItWorks.step2.title')}</h3>
              <p className="landing-step-description">
                {t('howItWorks.step2.description')}
              </p>
            </div>
            <div className="landing-step-separator"></div>
            <div className="landing-step">
              <div className="landing-step-number">3</div>
              <h3 className="landing-step-title">{t('howItWorks.step3.title')}</h3>
              <p className="landing-step-description">
                {t('howItWorks.step3.description')}
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Vision / Motivation Section */}
      <section 
        className="landing-vision fade-in-section"
        ref={(el) => (sectionsRef.current[5] = el)}
      >
        <div className="landing-vision-container">
          <h2 className="landing-vision-title">{t('vision.title')}</h2>
          <p className="landing-vision-text">
            {t('vision.text1')}
          </p>
          <p className="landing-vision-text">
            {t('vision.text2')}
          </p>
        </div>
      </section>

      {/* Final CTA Section */}
      <section 
        className="landing-final-cta fade-in-section"
        ref={(el) => (sectionsRef.current[3] = el)}
      >
        <div className="landing-final-cta-container">
          <h2 className="landing-final-cta-title">{t('cta.title')}</h2>
          <p className="landing-final-cta-subtitle">
            {t('cta.subtitle')}
          </p>
          <button 
            className="landing-cta-button"
            onClick={() => navigate('/signup')}
          >
            {t('cta.createAccount')}
          </button>
        </div>
      </section>

      {/* Footer */}
      <footer className="landing-footer">
        <p>{t('footer.copyright')}</p>
      </footer>
    </div>
  )
}

export default Landing

