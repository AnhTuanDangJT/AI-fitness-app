import React from 'react'
import clsx from 'clsx'

const Skeleton = ({ className }) => (
  <div
    className={clsx(
      'relative overflow-hidden rounded-2xl bg-white/5 before:absolute before:inset-0 before:-translate-x-full before:animate-[shimmer_1.6s_infinite] before:bg-gradient-to-r before:from-transparent before:via-white/30 before:to-transparent',
      className
    )}
  />
)

export default Skeleton

