/**
 * Level Calculation Utility
 * 
 * Calculates user level and title based on XP.
 * Levels are derived, not stored.
 */

/**
 * Calculate level from XP
 * Level = Math.floor(xp / 100)
 * 
 * @param {number} xp - Current XP value
 * @returns {number} Current level
 */
export function calculateLevel(xp) {
  return Math.floor(xp / 100)
}

/**
 * Get title based on level
 * 
 * @param {number} level - Current level
 * @returns {string} Title
 */
export function getTitle(level) {
  if (level <= 1) {
    return 'Beginner'
  } else if (level >= 2 && level <= 4) {
    return 'Consistent'
  } else if (level >= 5 && level <= 9) {
    return 'Disciplined'
  } else {
    return 'Elite'
  }
}

/**
 * Get title from XP (convenience function)
 * 
 * @param {number} xp - Current XP value
 * @returns {string} Title
 */
export function getTitleFromXP(xp) {
  const level = calculateLevel(xp)
  return getTitle(level)
}








