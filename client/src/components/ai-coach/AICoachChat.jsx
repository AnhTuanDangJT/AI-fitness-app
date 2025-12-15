import React, { useState, useEffect, useRef } from 'react'
import { useTranslation } from 'react-i18next'
import { aiCoachAPI } from '../../services/api'
import { gamificationAPI } from '../../services/gamificationApi'
import { calculateLevel, getTitle } from '../../utils/levels'
import './AICoachChat.css'

/**
 * AI Coach Chat Component
 * 
 * Interactive chat interface for AI Coach feature.
 * Single intelligent general chat that infers intent from user messages.
 */

function AICoachChat({ userId, onClearChat }) {
  const { t, i18n } = useTranslation()
  
  // Welcome message - appears only once
  const WELCOME_MESSAGE = {
    id: 'welcome',
    role: 'assistant',
    content: t('aiCoach.welcome'),
    timestamp: new Date().toISOString()
  }
  const [messages, setMessages] = useState([])
  const [inputMessage, setInputMessage] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [gamificationStatus, setGamificationStatus] = useState(null)
  const [previousStreak, setPreviousStreak] = useState(null)
  const [previousBadges, setPreviousBadges] = useState(null)
  const messagesEndRef = useRef(null)
  const chatContainerRef = useRef(null)
  const hasInitializedRef = useRef(false)

  // Load chat history and gamification status on mount
  useEffect(() => {
    // Only initialize once
    if (hasInitializedRef.current) return
    
    if (userId) {
      const storageKey = `ai_coach_chat_${userId}`
      const savedChat = localStorage.getItem(storageKey)
      if (savedChat) {
        try {
          const parsed = JSON.parse(savedChat)
          // Only restore messages if they exist and aren't just the welcome message
          const restoredMessages = parsed.messages || []
          if (restoredMessages.length > 0) {
            setMessages(restoredMessages)
          } else {
            // If saved but empty, initialize with welcome
            setMessages([WELCOME_MESSAGE])
          }
        } catch (e) {
          console.error('Error loading chat history:', e)
          setMessages([WELCOME_MESSAGE])
        }
      } else {
        // No saved chat, initialize with welcome message
        setMessages([WELCOME_MESSAGE])
      }
    } else {
      // No userId, initialize with welcome message
      setMessages([WELCOME_MESSAGE])
    }
    
    // Fetch gamification status
    fetchGamificationStatus()
    
    hasInitializedRef.current = true
  }, [userId])

  // Fetch gamification status
  const fetchGamificationStatus = async () => {
    try {
      const result = await gamificationAPI.getGamificationStatus()
      if (result.type === 'SUCCESS') {
        const newStatus = result.data
        
        // Check for new achievements
        checkForAchievements(newStatus)
        
        setGamificationStatus(newStatus)
        setPreviousStreak(newStatus.currentStreakDays)
        setPreviousBadges([...newStatus.badges])
      }
    } catch (err) {
      console.error('Error fetching gamification status:', err)
    }
  }

  // Check for new achievements and add system messages
  const checkForAchievements = (newStatus) => {
    if (!newStatus) return

    const newMessages = []

    // Check for streak milestones
    if (previousStreak !== null) {
      if (newStatus.currentStreakDays === 3 && previousStreak < 3) {
        newMessages.push({
          id: `system_${Date.now()}_streak3`,
          role: 'system',
          content: t('aiCoach.streak3'),
          timestamp: new Date().toISOString()
        })
      }
      if (newStatus.currentStreakDays === 7 && previousStreak < 7) {
        newMessages.push({
          id: `system_${Date.now()}_streak7`,
          role: 'system',
          content: t('aiCoach.streak7'),
          timestamp: new Date().toISOString()
        })
      }
      if (newStatus.currentStreakDays === 0 && previousStreak > 0) {
        // Streak broken - supportive message
        newMessages.push({
          id: `system_${Date.now()}_streakbroken`,
          role: 'system',
          content: t('aiCoach.streakBroken'),
          timestamp: new Date().toISOString()
        })
      }
    }

    // Check for new badges
    if (previousBadges && newStatus.badges) {
      const previousBadgesSet = new Set(previousBadges || [])
      const newBadges = (newStatus.badges || []).filter(badge => !previousBadgesSet.has(badge))
      newBadges.forEach(badgeId => {
        const badgeNames = {
          'FIRST_LOG': t('aiCoach.firstLog'),
          'STREAK_3': t('aiCoach.streak3Badge'),
          'STREAK_7': t('aiCoach.streak7Badge'),
          'STREAK_30': t('aiCoach.streak30Badge'),
          'XP_100': t('aiCoach.xp100'),
          'XP_500': t('aiCoach.xp500'),
        }
        newMessages.push({
          id: `system_${Date.now()}_badge_${badgeId}`,
          role: 'system',
          content: t('aiCoach.badgeUnlocked', { badge: badgeNames[badgeId] || badgeId }),
          timestamp: new Date().toISOString()
        })
      })
    }

    // Add system messages if any
    if (newMessages.length > 0) {
      setMessages(prev => [...prev, ...newMessages])
    }
  }

  // Save chat history to localStorage whenever messages change
  useEffect(() => {
    if (userId && messages.length > 0) {
      const storageKey = `ai_coach_chat_${userId}`
      try {
        localStorage.setItem(storageKey, JSON.stringify({
          messages,
          lastUpdated: new Date().toISOString()
        }))
      } catch (e) {
        console.error('Error saving chat history:', e)
      }
    }
  }, [messages, userId])

  // Expose clear chat function to parent
  const clearChat = React.useCallback(() => {
    // Clear messages state - reset to welcome message only
    const welcomeMsg = {
      id: 'welcome',
      role: 'assistant',
      content: t('aiCoach.welcome'),
      timestamp: new Date().toISOString()
    }
    setMessages([welcomeMsg])
    // Clear localStorage
    if (userId) {
      const storageKey = `ai_coach_chat_${userId}`
      localStorage.removeItem(storageKey)
    }
    // Clear error and input
    setError(null)
    setInputMessage('')
    // Reset initialization flag so welcome message appears
    hasInitializedRef.current = false
  }, [userId, t])

  useEffect(() => {
    if (onClearChat) {
      onClearChat(clearChat)
    }
  }, [onClearChat, clearChat])

  // Auto-scroll to bottom when new messages arrive
  useEffect(() => {
    scrollToBottom()
  }, [messages])

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  const handleSend = async (e) => {
    e.preventDefault()
    
    if (!inputMessage.trim() || loading) {
      return
    }

    const userMessage = inputMessage.trim()
    setInputMessage('')
    setError(null)

    // Refresh gamification status before sending message
    await fetchGamificationStatus()

    // Add user message to chat
    const userMsg = {
      id: `user_${Date.now()}`,
      role: 'user',
      content: userMessage,
      timestamp: new Date().toISOString()
    }
    setMessages(prev => [...prev, userMsg])

    // Show loading indicator
    setLoading(true)
    const loadingMsg = {
      id: `loading_${Date.now()}`,
      role: 'assistant',
      content: '...',
      loading: true,
      timestamp: new Date().toISOString()
    }
    setMessages(prev => [...prev, loadingMsg])

    try {
      // Call API with timeout (25 seconds)
      // Note: Backend should fetch gamification status and inject into AI prompt
      const timeoutPromise = new Promise((_, reject) => {
        setTimeout(() => reject(new Error(t('aiCoach.requestTimeout'))), 25000)
      })
      
      // Get current UI language (en or vi)
      const currentLanguage = i18n.language === 'vi' ? 'vi' : 'en'
      
      const result = await Promise.race([
        aiCoachAPI.chat(userMessage, null, currentLanguage),
        timeoutPromise
      ])

      // Remove loading message
      setMessages(prev => prev.filter(m => !m.loading))

      if (result.type === 'SUCCESS') {
        // Validate response
        if (!result.data || !result.data.assistantMessage) {
          setError(t('aiCoach.emptyResponse'))
          return
        }
        
        // Add assistant response
        const assistantMsg = {
          id: `assistant_${Date.now()}`,
          role: 'assistant',
          content: result.data.assistantMessage,
          actions: result.data.actions,
          timestamp: new Date().toISOString()
        }
        setMessages(prev => [...prev, assistantMsg])
        setError(null) // Clear any previous errors
      } else if (result.type === 'ERROR') {
        setError(result.error || t('aiCoach.failedToSend'))
        // Remove loading message
        setMessages(prev => prev.filter(m => !m.loading))
      }
    } catch (err) {
      console.error('Error sending message:', err)
      
      // Remove loading message
      setMessages(prev => prev.filter(m => !m.loading))
      
      // Handle specific error types
      if (err.message && err.message.includes('timeout')) {
        setError(t('aiCoach.requestTimeout'))
      } else if (err.message && err.message.includes('Network')) {
        setError(t('aiCoach.networkError'))
      } else {
        setError(err.message || t('aiCoach.failedToSend'))
      }
    } finally {
      setLoading(false)
    }
  }

  const handleRetry = () => {
    if (messages.length > 0) {
      const lastUserMessage = [...messages].reverse().find(m => m.role === 'user')
      if (lastUserMessage) {
        setInputMessage(lastUserMessage.content)
        handleSend(new Event('submit'))
      }
    }
  }

  const handleKeyPress = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      handleSend(e)
    }
  }

  return (
    <div className="ai-coach-chat">
      {/* Messages Container */}
      <div className="chat-messages" ref={chatContainerRef}>
        {messages.map((message) => (
          <div
            key={message.id}
            className={`chat-message ${message.role} ${message.loading ? 'loading' : ''}`}
          >
            <div className="message-content">
              {message.loading ? (
                <div className="loading-indicator">
                  <div className="spinner"></div>
                  <span>{t('aiCoach.thinking')}</span>
                </div>
              ) : (
                <>
                  <div className="message-text">{message.content}</div>
                  {message.actions && message.actions.length > 0 && (
                    <div className="message-actions">
                      {message.actions.map((action, idx) => (
                        <button key={idx} className="action-button" disabled>
                          {action}
                        </button>
                      ))}
                    </div>
                  )}
                </>
              )}
            </div>
            {message.role !== 'system' && (
              <div className="message-timestamp">
                {new Date(message.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
              </div>
            )}
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>

      {/* Error Message */}
      {error && (
        <div className="chat-error">
          <span>{error}</span>
          <button onClick={handleRetry} className="retry-button">
            {t('aiCoach.retry')}
          </button>
        </div>
      )}

      {/* Input Area */}
      <form className="chat-input-form" onSubmit={handleSend}>
        <input
          type="text"
          className="chat-input"
          placeholder={t('aiCoach.placeholder')}
          value={inputMessage}
          onChange={(e) => setInputMessage(e.target.value)}
          onKeyPress={handleKeyPress}
          disabled={loading}
        />
        <button
          type="submit"
          className="chat-send-button"
          disabled={!inputMessage.trim() || loading}
        >
          {loading ? '...' : t('aiCoach.send')}
        </button>
      </form>
    </div>
  )
}

export default AICoachChat

