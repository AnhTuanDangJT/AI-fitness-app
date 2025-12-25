import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import enTranslation from './locales/en.json'
import viTranslation from './locales/vi.json'
import { humanizeTranslationKey } from '../utils/i18nHelpers'

// Get saved language from localStorage or default to 'en'
const savedLanguage = localStorage.getItem('i18nextLng') || 'en'
const language = ['en', 'vi'].includes(savedLanguage) ? savedLanguage : 'en'
const isDev = import.meta.env?.MODE !== 'production'

i18n
  .use(initReactI18next)
  .init({
    resources: {
      en: {
        translation: enTranslation
      },
      vi: {
        translation: viTranslation
      }
    },
    lng: language,
    fallbackLng: 'en',
    interpolation: {
      escapeValue: false // React already escapes values
    },
    parseMissingKeyHandler: (key) => humanizeTranslationKey(key),
    react: {
      useSuspense: false
    }
  })

// Save language choice to localStorage when it changes
i18n.on('languageChanged', (lng) => {
  localStorage.setItem('i18nextLng', lng)
})

i18n.on('missingKey', (lngs, namespace, key) => {
  if (typeof window === 'undefined' || !isDev) return
  const localeList = Array.isArray(lngs) ? lngs.join(', ') : lngs
  console.warn(`[i18n] Missing key "${key}" in locale(s): ${localeList}`)
})

export default i18n









