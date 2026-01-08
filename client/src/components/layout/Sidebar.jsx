import React, { useState, useEffect } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { motion, AnimatePresence } from 'framer-motion'
import {
  LayoutDashboard,
  UtensilsCrossed,
  User,
  Settings,
  MessageSquare,
  Menu,
  X,
  ChevronRight,
} from 'lucide-react'
import clsx from 'clsx'
import { useTranslation } from 'react-i18next'

const navItems = [
  {
    label: 'dashboard.title',
    path: '/dashboard',
    icon: LayoutDashboard,
  },
  {
    label: 'mealPlan.title',
    path: '/meal-plan',
    icon: UtensilsCrossed,
  },
  {
    label: 'appPage.profile',
    path: '/app/profile',
    icon: User,
  },
  {
    label: 'appPage.settings',
    path: '/profile/edit',
    icon: Settings,
  },
  {
    label: 'appPage.aiCoach',
    path: '/ai-coach',
    icon: MessageSquare,
  },
]

export function Sidebar() {
  const { t } = useTranslation()
  const location = useLocation()
  const [isMobileOpen, setIsMobileOpen] = useState(false)
  const [isMobile, setIsMobile] = useState(false)

  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth < 1024)
      if (window.innerWidth >= 1024) {
        setIsMobileOpen(false)
      }
    }
    checkMobile()
    window.addEventListener('resize', checkMobile)
    return () => window.removeEventListener('resize', checkMobile)
  }, [])

  const NavLink = ({ item, isActive }) => {
    const Icon = item.icon
    return (
      <Link
        to={item.path}
        onClick={() => setIsMobileOpen(false)}
        className={clsx(
          'group relative flex items-center gap-3 rounded-xl px-4 py-3 text-sm font-semibold transition-all',
          isActive
            ? 'bg-accent/20 text-white shadow-lg shadow-accent/20'
            : 'text-white/70 hover:bg-white/5 hover:text-white'
        )}
      >
        {isActive && (
          <motion.div
            layoutId="activeNav"
            className="absolute inset-0 rounded-xl bg-accent/10 border border-accent/20"
            initial={false}
            transition={{ type: 'spring', bounce: 0.2, duration: 0.6 }}
          />
        )}
        <Icon className={clsx('relative z-10 h-5 w-5', isActive && 'text-accent')} />
        <span className="relative z-10">{t(item.label)}</span>
        {isActive && (
          <ChevronRight className="relative z-10 ml-auto h-4 w-4 text-accent" />
        )}
      </Link>
    )
  }

  const SidebarContent = () => (
    <aside className="flex h-full flex-col border-r border-white/10 bg-base-900/80 backdrop-blur-md">
      {/* Logo/Brand */}
      <div className="border-b border-white/10 px-6 py-6">
        <div className="flex items-center gap-3">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-gradient-to-br from-accent to-accent/70">
            <LayoutDashboard className="h-5 w-5 text-white" />
          </div>
          <div className="flex flex-col">
            <span className="text-xs font-semibold uppercase tracking-wider text-white/60">
              {t('dashboard.title', 'AI Fitness')}
            </span>
            <span className="text-sm font-semibold text-white">Health HQ</span>
          </div>
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 space-y-2 overflow-y-auto px-4 py-6">
        {navItems.map((item) => (
          <NavLink
            key={item.path}
            item={item}
            isActive={location.pathname === item.path || location.pathname.startsWith(item.path + '/')}
          />
        ))}
      </nav>

      {/* Footer */}
      <div className="border-t border-white/10 px-6 py-4">
        <p className="text-xs text-white/50">
          {t('common.version', 'Version 1.0.0')}
        </p>
      </div>
    </aside>
  )

  if (isMobile) {
    return (
      <>
        {/* Mobile Menu Button */}
        <button
          onClick={() => setIsMobileOpen(!isMobileOpen)}
          className="fixed left-4 top-4 z-50 flex h-10 w-10 items-center justify-center rounded-xl bg-base-900/90 border border-white/10 backdrop-blur-md lg:hidden"
          aria-label="Toggle menu"
        >
          {isMobileOpen ? (
            <X className="h-5 w-5 text-white" />
          ) : (
            <Menu className="h-5 w-5 text-white" />
          )}
        </button>

        {/* Mobile Sidebar Overlay */}
        <AnimatePresence>
          {isMobileOpen && (
            <>
              <motion.div
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                exit={{ opacity: 0 }}
                onClick={() => setIsMobileOpen(false)}
                className="fixed inset-0 z-40 bg-black/60 backdrop-blur-sm lg:hidden"
              />
              <motion.aside
                initial={{ x: -280 }}
                animate={{ x: 0 }}
                exit={{ x: -280 }}
                transition={{ type: 'spring', damping: 25, stiffness: 200 }}
                className="fixed left-0 top-0 z-40 h-full w-70 lg:hidden"
              >
                <SidebarContent />
              </motion.aside>
            </>
          )}
        </AnimatePresence>
      </>
    )
  }

  return (
    <div className="hidden lg:block lg:w-70">
      <SidebarContent />
    </div>
  )
}

