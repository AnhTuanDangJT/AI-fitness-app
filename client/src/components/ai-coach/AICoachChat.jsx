import React, { useState, useEffect, useRef } from 'react'
import { aiCoachAPI } from '../../services/api'
import './AICoachChat.css'

/**
 * AI Coach Chat Component
 * 
 * Interactive chat interface for AI Coach feature.
 * Single intelligent general chat that infers intent from user messages.
 */
// Welcome message - appears only once
const WELCOME_MESSAGE = {
  id: 'welcome',
  role: 'assistant',
  content: "Hi, I'm your AI Coach. Ask me anything about fitness, food, or the app.",
  timestamp: new Date().toISOString()
}

function AICoachChat({ userId, onClearChat }) {
  const [messages, setMessages] = useState([])
  const [inputMessage, setInputMessage] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const messagesEndRef = useRef(null)
  const chatContainerRef = useRef(null)
  const hasInitializedRef = useRef(false)

  // Load chat history from localStorage on mount
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
    
    hasInitializedRef.current = true
  }, [userId])

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
      content: "Hi, I'm your AI Coach. Ask me anything about fitness, food, or the app.",
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
  }, [userId])

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
      const timeoutPromise = new Promise((_, reject) => {
        setTimeout(() => reject(new Error('Request timeout. Please try again.')), 25000)
      })
      
      const result = await Promise.race([
        aiCoachAPI.chat(userMessage),
        timeoutPromise
      ])

      // Remove loading message
      setMessages(prev => prev.filter(m => !m.loading))

      if (result.type === 'SUCCESS') {
        // Validate response
        if (!result.data || !result.data.assistantMessage) {
          setError('Received empty response. Please try again.')
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
        setError(result.error || 'Failed to get response. Please try again.')
        // Remove loading message
        setMessages(prev => prev.filter(m => !m.loading))
      }
    } catch (err) {
      console.error('Error sending message:', err)
      
      // Remove loading message
      setMessages(prev => prev.filter(m => !m.loading))
      
      // Handle specific error types
      if (err.message && err.message.includes('timeout')) {
        setError('Request took too long. Please try again.')
      } else if (err.message && err.message.includes('Network')) {
        setError('Network error. Please check your connection and try again.')
      } else {
        setError(err.message || 'Failed to send message. Please try again.')
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
                  <span>AI Coach is thinking...</span>
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
            <div className="message-timestamp">
              {new Date(message.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
            </div>
          </div>
        ))}
        <div ref={messagesEndRef} />
      </div>

      {/* Error Message */}
      {error && (
        <div className="chat-error">
          <span>{error}</span>
          <button onClick={handleRetry} className="retry-button">
            Retry
          </button>
        </div>
      )}

      {/* Input Area */}
      <form className="chat-input-form" onSubmit={handleSend}>
        <input
          type="text"
          className="chat-input"
          placeholder="Ask me anything about workouts, nutrition, or the app..."
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
          {loading ? '...' : 'Send'}
        </button>
      </form>
    </div>
  )
}

export default AICoachChat

