import React from 'react'
import { motion } from 'framer-motion'
import Button from './Button'

function EmptyState({
  title,
  description,
  actionLabel,
  onAction,
  secondaryActionLabel,
  onSecondaryAction,
}) {
  return (
    <motion.div
      initial={{ opacity: 0, y: 8 }}
      animate={{ opacity: 1, y: 0 }}
      className="flex flex-col items-center gap-3 rounded-2xl border border-dashed border-white/15 bg-white/5 px-8 py-10 text-center"
    >
      <div className="space-y-2">
        <p className="text-xs uppercase tracking-[0.35em] text-white/60">Status</p>
        <p className="text-lg font-semibold text-white">{title}</p>
        <p className="mt-1 max-w-sm text-sm text-muted">{description}</p>
      </div>
      <div className="flex flex-wrap items-center justify-center gap-3">
        {actionLabel && (
          <Button variant="secondary" onClick={onAction}>
            {actionLabel}
          </Button>
        )}
        {secondaryActionLabel && (
          <Button variant="ghost" onClick={onSecondaryAction}>
            {secondaryActionLabel}
          </Button>
        )}
      </div>
    </motion.div>
  )
}

export default EmptyState

