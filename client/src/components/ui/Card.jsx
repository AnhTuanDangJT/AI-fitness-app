import React from 'react'
import clsx from 'clsx'

export function Card({ className, children, as: Component = 'section', ...props }) {
  return (
    <Component
      className={clsx(
        'rounded-3xl border border-white/12 bg-base-900/70 backdrop-blur-md transition hover:border-white/20',
        className
      )}
      {...props}
    >
      {children}
    </Component>
  )
}

export function CardHeader({ className, children, ...props }) {
  return (
    <div className={clsx('px-8 pt-8 pb-4 flex flex-col gap-2', className)} {...props}>
      {children}
    </div>
  )
}

export function CardTitle({ className, children, ...props }) {
  return (
    <h3 className={clsx('text-lg font-semibold text-white tracking-tight', className)} {...props}>
      {children}
    </h3>
  )
}

export function CardDescription({ className, children, ...props }) {
  return (
    <p className={clsx('text-sm text-muted', className)} {...props}>
      {children}
    </p>
  )
}

export function CardContent({ className, children, ...props }) {
  return (
    <div className={clsx('px-8 pb-8', className)} {...props}>
      {children}
    </div>
  )
}

