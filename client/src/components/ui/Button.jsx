import React, { forwardRef } from 'react'
import { motion } from 'framer-motion'
import clsx from 'clsx'

const variantStyles = {
  primary: 'bg-accent text-white hover:bg-accent/90 focus-visible:ring-accent/60',
  secondary: 'border border-white/15 text-white hover:border-white/35 focus-visible:ring-accent/60',
  ghost: 'text-white/70 hover:text-white focus-visible:ring-accent/60',
  destructive: 'text-red-300 hover:text-red-200 hover:bg-red-500/5 focus-visible:ring-red-400/45',
}

const sizeStyles = {
  sm: 'px-3 py-2 text-sm',
  md: 'px-4 py-2.5 text-base',
  lg: 'px-5 py-3 text-lg',
}

const MotionButton = motion.button

const Button = forwardRef(function Button(
  { variant = 'primary', size = 'md', leftIcon, rightIcon, className, children, type = 'button', ...props },
  ref
) {
  return (
    <MotionButton
      ref={ref}
      type={type}
      whileHover={{ y: -1 }}
      whileTap={{ scale: 0.97 }}
      className={clsx(
        'inline-flex items-center justify-center gap-2 rounded-full font-semibold tracking-tight transition-all duration-200 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-offset-base-900',
        variantStyles[variant],
        sizeStyles[size],
        className
      )}
      {...props}
    >
      {leftIcon && <span className="text-lg">{leftIcon}</span>}
      <span>{children}</span>
      {rightIcon && <span className="text-lg">{rightIcon}</span>}
    </MotionButton>
  )
})

export default Button

