import React from 'react'
import clsx from 'clsx'

const variantStyles = {
  default: 'bg-white/10 text-white/90 border-white/20',
  primary: 'bg-accent/20 text-accent-foreground border-accent/30',
  success: 'bg-emerald-500/20 text-emerald-200 border-emerald-500/30',
  warning: 'bg-amber-500/20 text-amber-200 border-amber-500/30',
  danger: 'bg-rose-500/20 text-rose-200 border-rose-500/30',
  info: 'bg-cyan-500/20 text-cyan-200 border-cyan-500/30',
}

const sizeStyles = {
  sm: 'px-2 py-0.5 text-xs',
  md: 'px-3 py-1 text-sm',
  lg: 'px-4 py-1.5 text-base',
}

export function Badge({ variant = 'default', size = 'md', className, children, ...props }) {
  return (
    <span
      className={clsx(
        'inline-flex items-center justify-center rounded-full border font-semibold tracking-tight',
        variantStyles[variant],
        sizeStyles[size],
        className
      )}
      {...props}
    >
      {children}
    </span>
  )
}

