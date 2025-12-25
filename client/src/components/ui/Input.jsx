import React, { forwardRef } from 'react'
import clsx from 'clsx'

const Input = forwardRef(function Input({ className, label, helper, error, ...props }, ref) {
  return (
    <label className="flex flex-col gap-2 text-sm text-muted">
      {label && <span className="text-white font-medium">{label}</span>}
      <input
        ref={ref}
        className={clsx(
          'w-full rounded-2xl border border-white/10 bg-white/5 px-4 py-3 text-white text-base transition placeholder:text-slate-400 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-accent focus-visible:ring-offset-2 focus-visible:ring-offset-base-900',
          error && 'border-danger text-danger placeholder:text-danger/80',
          className
        )}
        {...props}
      />
      {(helper || error) && (
        <span className={clsx('text-xs', error ? 'text-danger' : 'text-muted')}>
          {error || helper}
        </span>
      )}
    </label>
  )
})

export default Input


