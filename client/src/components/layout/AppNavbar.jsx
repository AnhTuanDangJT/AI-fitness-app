import React from 'react'
import { motion } from 'framer-motion'
import Button from '@/components/ui/Button'

function AppNavbar({
  language = 'en',
  onToggleLanguage,
  onAlignToggle,
  headerAlignmentLabel,
  onEditProfile,
  onDownloadPdf,
  onFeedback,
  onLogout,
  brandTitle = 'AI Fitness',
  brandSubtitle = 'Personal Health HQ',
  languageToggleLabel = 'Toggle language',
  labels = {
    feedback: 'Feedback',
    export: 'Export',
    edit: 'Edit Profile',
    logout: 'Sign out',
  },
}) {
  return (
    <motion.header initial={{ opacity: 0, y: -12 }} animate={{ opacity: 1, y: 0 }} className="sticky top-6 z-20">
      <div className="mx-auto flex max-w-6xl items-center justify-between rounded-3xl border border-white/12 bg-base-900/80 px-8 py-5 backdrop-blur-md">
        <div className="flex flex-col gap-1">
          <span className="text-xs uppercase tracking-[0.4em] text-muted">{brandTitle}</span>
          <p className="text-xl font-semibold text-white">{brandSubtitle}</p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <button
            className="flex items-center gap-2 rounded-full border border-white/12 px-4 py-2 text-sm font-semibold text-white/80 transition hover:border-white/35"
            onClick={onToggleLanguage}
            aria-label={languageToggleLabel}
          >
            {language?.toUpperCase()}
          </button>
          <button
            className="hidden md:flex items-center gap-2 rounded-full border border-white/12 px-4 py-2 text-sm text-white/70 hover:text-white"
            onClick={onAlignToggle}
          >
            {headerAlignmentLabel}
          </button>
          <Button variant="ghost" onClick={onFeedback}>
            {labels.feedback}
          </Button>
          <Button variant="secondary" onClick={onDownloadPdf}>
            {labels.export}
          </Button>
          <Button variant="primary" onClick={onEditProfile}>
            {labels.edit}
          </Button>
          <Button variant="destructive" onClick={onLogout}>
            {labels.logout}
          </Button>
        </div>
      </div>
    </motion.header>
  )
}

export default AppNavbar

