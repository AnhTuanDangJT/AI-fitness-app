import React, { useState, useEffect } from 'react'
import { useTranslation } from 'react-i18next'
import { feedbackAPI } from '../services/feedbackApi'
import './FeedbackModal.css'

function FeedbackModal({ onClose }) {
  const { t } = useTranslation()
  const [subject, setSubject] = useState('')
  const [message, setMessage] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(false)

  // Handle ESC key to close modal
  useEffect(() => {
    const handleEsc = (e) => {
      if (e.key === 'Escape' && !loading) {
        setSubject('')
        setMessage('')
        setError(null)
        setSuccess(false)
        onClose()
      }
    }
    document.addEventListener('keydown', handleEsc)
    return () => document.removeEventListener('keydown', handleEsc)
  }, [loading, onClose])

  const handleSubmit = async (e) => {
    e.preventDefault()
    
    // Validate message
    if (!message.trim()) {
      setError(t('feedback.messageRequired'))
      return
    }

    setError(null)
    setLoading(true)

    try {
      const result = await feedbackAPI.submit(subject, message)
      
      if (result.type === 'SUCCESS') {
        setSuccess(true)
        // Clear form
        setSubject('')
        setMessage('')
        // Close modal after short delay
        setTimeout(() => {
          setSuccess(false)
          onClose()
        }, 2000)
      } else {
        setError(result.error || t('feedback.submitFailed'))
      }
    } catch (err) {
      setError(t('feedback.unexpectedError'))
    } finally {
      setLoading(false)
    }
  }

  const handleClose = () => {
    if (!loading) {
      setSubject('')
      setMessage('')
      setError(null)
      setSuccess(false)
      onClose()
    }
  }

  return (
    <div className="feedback-modal-overlay" onClick={handleClose}>
      <div className="feedback-modal" onClick={(e) => e.stopPropagation()}>
        <div className="feedback-modal-header">
          <h2>{t('feedback.title')}</h2>
          <button 
            className="feedback-modal-close" 
            onClick={handleClose}
            disabled={loading}
            aria-label={t('feedback.close')}
          >
            ×
          </button>
        </div>

        {success ? (
          <div className="feedback-modal-success">
            <div className="feedback-success-icon" aria-hidden>OK</div>
            <p>{t('feedback.success')}</p>
          </div>
        ) : (
          <form className="feedback-modal-form" onSubmit={handleSubmit}>
            {error && (
              <div className="feedback-error-message">
                {error}
              </div>
            )}

            <div className="feedback-form-group">
              <label htmlFor="feedback-subject">{t('feedback.subject')}</label>
              <input
                id="feedback-subject"
                type="text"
                value={subject}
                onChange={(e) => setSubject(e.target.value)}
                placeholder={t('feedback.subjectPlaceholder')}
                maxLength={200}
                disabled={loading}
                className="feedback-input"
              />
            </div>

            <div className="feedback-form-group">
              <label htmlFor="feedback-message">
                {t('feedback.message')} <span className="feedback-required">{t('feedback.required')}</span>
              </label>
              <textarea
                id="feedback-message"
                value={message}
                onChange={(e) => {
                  setMessage(e.target.value)
                  setError(null)
                }}
                placeholder={t('feedback.messagePlaceholder')}
                rows={6}
                maxLength={5000}
                required
                disabled={loading}
                className="feedback-textarea"
              />
              <div className="feedback-char-count">
                {t('feedback.charCount', { count: message.length })}
              </div>
            </div>

            <div className="feedback-modal-actions">
              <button
                type="button"
                onClick={handleClose}
                disabled={loading}
                className="feedback-btn feedback-btn-secondary"
              >
                {t('feedback.cancel')}
              </button>
              <button
                type="submit"
                disabled={loading || !message.trim()}
                className="feedback-btn feedback-btn-primary"
              >
                {loading ? t('feedback.sending') : t('feedback.sendFeedback')}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  )
}

export default FeedbackModal

