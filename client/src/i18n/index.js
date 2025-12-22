import i18n from 'i18next'
import { initReactI18next } from 'react-i18next'
import enTranslation from './locales/en.json'
import viTranslation from './locales/vi.json'

// Get saved language from localStorage or default to 'en'
const savedLanguage = localStorage.getItem('i18nextLng') || 'en'
const language = ['en', 'vi'].includes(savedLanguage) ? savedLanguage : 'en'

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
    react: {
      useSuspense: false
    }
  })

// Save language choice to localStorage when it changes
i18n.on('languageChanged', (lng) => {
  localStorage.setItem('i18nextLng', lng)
})

export default i18n








