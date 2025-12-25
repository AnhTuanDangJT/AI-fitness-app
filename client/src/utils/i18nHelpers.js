const loggedMissingKeys = new Set()

export const humanizeTranslationKey = (key = '') => {
  if (!key || typeof key !== 'string') return ''

  const raw = key.includes('.') ? key.split('.').pop() : key
  return raw
    .replace(/([a-z0-9])([A-Z])/g, '$1 $2')
    .replace(/[-_]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    .replace(/\b\w/g, (char) => char.toUpperCase())
}

export const resolveTextWithFallback = (tFunction, key, fallback, options = {}) => {
  if (!key || typeof key !== 'string') {
    return fallback ?? ''
  }

  const translated = tFunction(key, options)
  if (translated && translated !== key) {
    return translated
  }

  const finalFallback = fallback ?? humanizeTranslationKey(key)
  if (typeof window !== 'undefined' && import.meta.env?.MODE !== 'production') {
    if (!loggedMissingKeys.has(key)) {
      console.warn(`[i18n] Missing translation for key "${key}"`)
      loggedMissingKeys.add(key)
    }
  }

  return finalFallback
}

