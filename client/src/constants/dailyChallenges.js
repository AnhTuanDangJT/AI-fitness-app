/**
 * Daily Challenges Constants
 * 
 * Defines the daily challenges available to users.
 * These are frontend-driven but validated by backend gamification events.
 */

export const DAILY_CHALLENGES = {
  LOG_TODAY: {
    id: 'LOG_TODAY',
    name: 'Log Today',
    description: 'Log something today',
    xpReward: 10,
    icon: '📝',
  },
  CONSISTENCY_7: {
    id: 'CONSISTENCY_7',
    name: '7-Day Consistency',
    description: 'Maintain a 7-day streak',
    xpReward: 50,
    icon: '🔥',
  },
}

/**
 * Get all challenge IDs
 */
export function getAllChallengeIds() {
  return Object.keys(DAILY_CHALLENGES)
}

/**
 * Get challenge by ID
 */
export function getChallengeById(id) {
  return DAILY_CHALLENGES[id] || null
}


