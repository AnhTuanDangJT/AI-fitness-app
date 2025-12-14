import React, { useState, useEffect, useRef } from 'react'
import AICoachChat from '../components/ai-coach/AICoachChat'
import './AICoach.css'

/**
 * AI Coach Page
 * 
 * Page component that wraps the AI Coach Chat interface.
 * Provides page layout and retrieves user information.
 */
function AICoach() {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)
  const clearChatRef = useRef(null)

  useEffect(() => {
    // Get user info from localStorage
    const userData = localStorage.getItem('user')
    if (userData) {
      try {
        const parsedUser = JSON.parse(userData)
        setUser(parsedUser)
      } catch (e) {
        console.error('Error parsing user data:', e)
      }
    }
    setLoading(false)
  }, [])

  const handleClearChat = () => {
    if (clearChatRef.current) {
      clearChatRef.current()
    }
  }

  // Show loading state while getting user info
  if (loading) {
    return (
      <div className="ai-coach-page">
        <div className="ai-coach-container">
          <div className="loading-container">
            <div className="loading-spinner"></div>
            <p>Loading...</p>
          </div>
        </div>
      </div>
    )
  }

  // Get userId from user object
  const userId = user?.id || user?.userId || null

  return (
    <div className="ai-coach-page">
      <div className="ai-coach-container">
        <div className="ai-coach-header">
          <div className="ai-coach-header-content">
            <div>
              <h1>AI Coach</h1>
              <p>Chat with your personal AI fitness coach</p>
            </div>
            <button 
              className="clear-chat-button"
              onClick={handleClearChat}
              title="Clear chat history"
            >
              Clear Chat
            </button>
          </div>
        </div>
        <div className="ai-coach-content">
          <AICoachChat userId={userId} onClearChat={(clearFn) => { clearChatRef.current = clearFn }} />
        </div>
      </div>
    </div>
  )
}

export default AICoach

