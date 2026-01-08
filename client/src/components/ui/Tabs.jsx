import React from 'react'
import { motion } from 'framer-motion'
import clsx from 'clsx'

export function Tabs({ value, onValueChange, className, children, ...props }) {
  return (
    <div className={clsx('flex gap-1 rounded-2xl bg-white/5 p-1', className)} {...props}>
      {React.Children.map(children, (child) => {
        if (React.isValidElement(child)) {
          return React.cloneElement(child, { activeValue: value, onValueChange })
        }
        return child
      })}
    </div>
  )
}

export function TabsList({ className, children, ...props }) {
  return (
    <div className={clsx('flex gap-1', className)} {...props}>
      {children}
    </div>
  )
}

export function TabsTrigger({ value, activeValue, onValueChange, className, children, ...props }) {
  const isActive = value === activeValue

  return (
    <motion.button
      type="button"
      onClick={() => onValueChange(value)}
      className={clsx(
        'relative px-4 py-2 text-sm font-semibold rounded-xl transition-colors',
        isActive
          ? 'text-white'
          : 'text-white/60 hover:text-white/80',
        className
      )}
      whileHover={{ scale: 1.02 }}
      whileTap={{ scale: 0.98 }}
      {...props}
    >
      {isActive && (
        <motion.div
          layoutId="activeTab"
          className="absolute inset-0 rounded-xl bg-white/10 border border-white/20"
          initial={false}
          transition={{ type: 'spring', bounce: 0.2, duration: 0.6 }}
        />
      )}
      <span className="relative z-10">{children}</span>
    </motion.button>
  )
}

export function TabsContent({ value, activeValue, className, children, ...props }) {
  if (value !== activeValue) return null

  return (
    <motion.div
      initial={{ opacity: 0, y: 10 }}
      animate={{ opacity: 1, y: 0 }}
      exit={{ opacity: 0, y: -10 }}
      transition={{ duration: 0.2 }}
      className={clsx('mt-4', className)}
      {...props}
    >
      {children}
    </motion.div>
  )
}

