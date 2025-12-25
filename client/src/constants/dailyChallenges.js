/**
 * Daily Challenges Constants
 * 
 * Defines the daily challenges available to users.
 * These are frontend-driven but validated by backend gamification events.
 */

export const DAILY_CHALLENGES = {
  LOG_TODAY: {
    id: 'LOG_TODAY',
    nameKey: 'gamification.challenges.logToday.title',
    descriptionKey: 'gamification.challenges.logToday.description',
    xpReward: 10,
    icon: '📝',
  },
  CONSISTENCY_7: {
    id: 'CONSISTENCY_7',
    nameKey: 'gamification.challenges.consistency7.title',
    descriptionKey: 'gamification.challenges.consistency7.description',
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









