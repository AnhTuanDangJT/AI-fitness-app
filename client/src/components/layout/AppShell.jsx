import React from 'react'
import { Sidebar } from './Sidebar'
import { motion } from 'framer-motion'

export function AppShell({ children }) {
  return (
    <div className="flex h-screen overflow-hidden bg-base-900">
      <Sidebar />
      <main className="flex-1 overflow-y-auto">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.3 }}
          className="h-full"
        >
          {children}
        </motion.div>
      </main>
    </div>
  )
}

