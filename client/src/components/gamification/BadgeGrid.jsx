import React, { useState } from 'react'
import './Gamification.css'

const JournalIcon = ({ className }) => (
  <svg
    className={className}
    viewBox="0 0 48 48"
    fill="none"
    role="presentation"
    aria-hidden="true"
    focusable="false"
  >
    <rect x="12" y="8" width="18" height="32" rx="4" stroke="currentColor" strokeWidth="2.4" />
    <line x1="16" y1="16" x2="24" y2="16" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    <line x1="16" y1="22" x2="24" y2="22" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
    <path
      d="M28.5 13.5L37.5 22.5L26 34H18V26L28.5 13.5Z"
      stroke="currentColor"
      strokeWidth="2.2"
      strokeLinejoin="round"
      fill="currentColor"
      fillOpacity="0.08"
    />
    <path
      d="M30 16L34 20"
      stroke="currentColor"
      strokeWidth="2.2"
      strokeLinecap="round"
      strokeLinejoin="round"
      opacity="0.8"
    />
  </svg>
)

const FlameIcon = ({ className }) => (
  <svg
    className={className}
    viewBox="0 0 48 48"
    fill="none"
    role="presentation"
    aria-hidden="true"
    focusable="false"
  >
    <path
      d="M24 6C24.5 14 16 16.5 16 26C16 32.6274 20.4772 38 24 38C27.5228 38 32 32.6274 32 26C32 16.5 23.5 14 24 6Z"
      stroke="currentColor"
      strokeWidth="2.6"
      strokeLinecap="round"
      strokeLinejoin="round"
      fill="currentColor"
      fillOpacity="0.08"
    />
    <path
      d="M24 22C25.5 24 28 26.5 28 30C28 33 26.2091 34.5 24 34.5C21.7909 34.5 20 33 20 30C20 27 21.5 24.5 24 22Z"
      stroke="currentColor"
      strokeWidth="2.4"
      strokeLinecap="round"
      strokeLinejoin="round"
      opacity="0.85"
    />
  </svg>
)

const CalendarIcon = ({ className }) => (
  <svg
    className={className}
    viewBox="0 0 48 48"
    fill="none"
    role="presentation"
    aria-hidden="true"
    focusable="false"
  >
    <rect x="9" y="12" width="30" height="26" rx="4" stroke="currentColor" strokeWidth="2.4" />
    <line x1="9" y1="20" x2="39" y2="20" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" />
    <line x1="18" y1="10" x2="18" y2="16" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" />
    <line x1="30" y1="10" x2="30" y2="16" stroke="currentColor" strokeWidth="2.4" strokeLinecap="round" />
    <rect x="16" y="24" width="6" height="6" rx="1.5" fill="currentColor" fillOpacity="0.2" />
    <rect x="26" y="24" width="6" height="6" rx="1.5" fill="currentColor" fillOpacity="0.4" />
  </svg>
)

const TrophyIcon = ({ className }) => (
  <svg
    className={className}
    viewBox="0 0 48 48"
    fill="none"
    role="presentation"
    aria-hidden="true"
    focusable="false"
  >
    <path
      d="M16 10H32V19C32 24.5228 27.5228 29 22 29H20C17.2386 29 15 26.7614 15 24V10"
      stroke="currentColor"
      strokeWidth="2.4"
      strokeLinecap="round"
      strokeLinejoin="round"
      fill="currentColor"
      fillOpacity="0.08"
    />
    <path
      d="M12 10H8V16C8 20.4183 11.5817 24 16 24H17"
      stroke="currentColor"
      strokeWidth="2.2"
      strokeLinecap="round"
      strokeLinejoin="round"
      opacity="0.9"
    />
    <path
      d="M36 10H40V16C40 20.4183 36.4183 24 32 24H31"
      stroke="currentColor"
      strokeWidth="2.2"
      strokeLinecap="round"
      strokeLinejoin="round"
      opacity="0.9"
    />
    <path
      d="M20 29V34C20 35.6569 18.6569 37 17 37H14V40H34V37H31C29.3431 37 28 35.6569 28 34V29"
      stroke="currentColor"
      strokeWidth="2.4"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
    <path
      d="M19 15L22 18L29 11"
      stroke="currentColor"
      strokeWidth="2.4"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
  </svg>
)

const HundredIcon = ({ className }) => (
  <svg
    className={className}
    viewBox="0 0 48 48"
    fill="none"
    role="presentation"
    aria-hidden="true"
    focusable="false"
  >
    <path
      d="M14 18L18 14V34"
      stroke="currentColor"
      strokeWidth="2.6"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
    <path
      d="M25 18C21.5 18 20 21.5 20 26C20 30.5 21.5 34 25 34C28.5 34 30 30.5 30 26C30 21.5 28.5 18 25 18Z"
      stroke="currentColor"
      strokeWidth="2.6"
      strokeLinecap="round"
      strokeLinejoin="round"
      fill="currentColor"
      fillOpacity="0.08"
    />
    <path
      d="M35 18C31.5 18 30 21.5 30 26C30 30.5 31.5 34 35 34C38.5 34 40 30.5 40 26C40 21.5 38.5 18 35 18Z"
      stroke="currentColor"
      strokeWidth="2.6"
      strokeLinecap="round"
      strokeLinejoin="round"
      fill="currentColor"
      fillOpacity="0.08"
    />
    <path
      d="M12 36C16 38.5 20 39.5 24 39.5C28 39.5 32 38.5 36 36"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      opacity="0.6"
    />
  </svg>
)

const CrownIcon = ({ className }) => (
  <svg
    className={className}
    viewBox="0 0 48 48"
    fill="none"
    role="presentation"
    aria-hidden="true"
    focusable="false"
  >
    <path
      d="M12 34L10 20L18 26L24 16L30 26L38 20L36 34H12Z"
      stroke="currentColor"
      strokeWidth="2.4"
      strokeLinecap="round"
      strokeLinejoin="round"
      fill="currentColor"
      fillOpacity="0.08"
    />
    <path
      d="M18 34V38H30V34"
      stroke="currentColor"
      strokeWidth="2.4"
      strokeLinecap="round"
      strokeLinejoin="round"
    />
    <circle cx="10" cy="18" r="2.2" stroke="currentColor" strokeWidth="1.6" fill="currentColor" fillOpacity="0.25" />
    <circle cx="24" cy="14" r="2.2" stroke="currentColor" strokeWidth="1.6" fill="currentColor" fillOpacity="0.25" />
    <circle cx="38" cy="18" r="2.2" stroke="currentColor" strokeWidth="1.6" fill="currentColor" fillOpacity="0.25" />
  </svg>
)

const DEFAULT_ICON = JournalIcon

const BADGE_ICON_COMPONENTS = {
  FIRST_LOG: JournalIcon,
  STREAK_3: FlameIcon,
  STREAK_7: CalendarIcon,
  STREAK_30: TrophyIcon,
  XP_100: HundredIcon,
  XP_500: CrownIcon,
}

const BADGE_VISUALS = {
  FIRST_LOG: {
    gradient: 'linear-gradient(135deg, rgba(96,175,255,0.18), rgba(110,231,255,0.08))',
    color: '#B9E6FF',
    border: 'rgba(96,175,255,0.45)',
    glow: '0 8px 22px rgba(96,175,255,0.35)',
  },
  STREAK_3: {
    gradient: 'linear-gradient(135deg, rgba(255,140,0,0.26), rgba(255,99,71,0.18))',
    color: '#FFB873',
    border: 'rgba(255,140,0,0.5)',
    glow: '0 8px 22px rgba(255,140,0,0.35)',
  },
  STREAK_7: {
    gradient: 'linear-gradient(135deg, rgba(110,231,183,0.25), rgba(59,130,246,0.2))',
    color: '#9FE8D8',
    border: 'rgba(59,130,246,0.45)',
    glow: '0 8px 22px rgba(59,130,246,0.3)',
  },
  STREAK_30: {
    gradient: 'linear-gradient(135deg, rgba(255,215,0,0.28), rgba(255,165,0,0.2))',
    color: '#FFE08A',
    border: 'rgba(255,200,0,0.5)',
    glow: '0 10px 24px rgba(255,200,0,0.35)',
  },
  XP_100: {
    gradient: 'linear-gradient(135deg, rgba(255,99,132,0.28), rgba(255,71,87,0.2))',
    color: '#FF9CB2',
    border: 'rgba(255,107,129,0.55)',
    glow: '0 10px 24px rgba(255,99,132,0.3)',
  },
  XP_500: {
    gradient: 'linear-gradient(135deg, rgba(167,139,250,0.28), rgba(192,132,252,0.2))',
    color: '#D9B5FF',
    border: 'rgba(192,132,252,0.5)',
    glow: '0 10px 26px rgba(192,132,252,0.3)',
  },
}

/**
 * Badge definitions with descriptions
 */
const BADGE_DEFINITIONS = {
  FIRST_LOG: {
    name: 'First Log',
    description: 'Log your first activity or meal',
  },
  STREAK_3: {
    name: '3-Day Streak',
    description: 'Maintain a 3-day streak',
  },
  STREAK_7: {
    name: '7-Day Streak',
    description: 'Maintain a 7-day streak',
  },
  STREAK_30: {
    name: '30-Day Streak',
    description: 'Maintain a 30-day streak',
  },
  XP_100: {
    name: 'Centurion',
    description: 'Reach 100 XP',
  },
  XP_500: {
    name: 'Elite',
    description: 'Reach 500 XP',
  },
}

/**
 * Badge Grid Component
 * 
 * Displays all badges, showing unlocked badges prominently and locked badges greyed out.
 * 
 * @param {Array<string>} badges - Array of unlocked badge IDs
 */
function BadgeGrid({ badges = [] }) {
  const [hoveredBadge, setHoveredBadge] = useState(null)
  const [isExpanded, setIsExpanded] = useState(false)

  // Get all badge IDs in order
  const allBadgeIds = Object.keys(BADGE_DEFINITIONS)

  // Determine which badges to show (all if expanded, or first 3 if collapsed)
  const badgesToShow = isExpanded ? allBadgeIds : allBadgeIds.slice(0, 3)

  return (
    <div className="badge-grid-container">
      <div className="badge-grid-header">
        <h3 className="badge-grid-title">Badges</h3>
        {allBadgeIds.length > 3 && (
          <button
            className="badge-grid-toggle"
            onClick={() => setIsExpanded(!isExpanded)}
            aria-label={isExpanded ? 'Collapse badges' : 'Expand badges'}
          >
            {isExpanded ? 'v' : '>'}
          </button>
        )}
      </div>
      
      <div className="badge-grid">
        {badgesToShow.map((badgeId) => {
          const badge = BADGE_DEFINITIONS[badgeId]
          const isUnlocked = badges.includes(badgeId)
          const IconComponent = BADGE_ICON_COMPONENTS[badgeId] || DEFAULT_ICON
          
          if (!badge) return null
          
          return (
            <div
              key={badgeId}
              className={`badge-item ${isUnlocked ? 'unlocked' : 'locked'}`}
              onMouseEnter={() => setHoveredBadge(badgeId)}
              onMouseLeave={() => setHoveredBadge(null)}
            >
              <div
                className="badge-icon"
                aria-hidden="true"
                data-badge-type={badgeId}
                style={
                  isUnlocked
                    ? {
                        '--badge-icon-gradient': BADGE_VISUALS[badgeId]?.gradient,
                        '--badge-icon-color': BADGE_VISUALS[badgeId]?.color,
                        '--badge-icon-border': BADGE_VISUALS[badgeId]?.border,
                        '--badge-icon-glow': BADGE_VISUALS[badgeId]?.glow,
                      }
                    : undefined
                }
              >
                <IconComponent className="badge-icon-svg" />
              </div>
              <div className="badge-name">{badge.name}</div>
              
              {/* Tooltip on hover */}
              {hoveredBadge === badgeId && (
                <div className="badge-tooltip">
                  <div className="badge-tooltip-content">
                    <div className="badge-tooltip-name">{badge.name}</div>
                    <div className="badge-tooltip-description">
                      {isUnlocked 
                        ? badge.description 
                        : `Unlock: ${badge.description}`}
                    </div>
                  </div>
                </div>
              )}
            </div>
          )
        })}
      </div>
      
      {!isExpanded && allBadgeIds.length > 3 && (
        <div className="badge-grid-footer">
          <button
            className="badge-grid-show-more"
            onClick={() => setIsExpanded(true)}
          >
            Show all badges ({allBadgeIds.length})
          </button>
        </div>
      )}
    </div>
  )
}

export default BadgeGrid










