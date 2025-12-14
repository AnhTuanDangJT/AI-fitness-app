import React from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute'
import ParticleBackground from './components/ParticleBackground'
import Login from './pages/Login'
import Signup from './pages/Signup'
import VerifyEmail from './pages/VerifyEmail'
import Dashboard from './pages/Dashboard'
import AppPage from './pages/App'
import ProfilePage from './pages/ProfilePage'
import ProfileSetup from './pages/ProfileSetup'
import EditProfile from './pages/EditProfile'
import MealPlan from './pages/MealPlan'
import MealPreferences from './pages/MealPreferences'
import AICoach from './pages/AICoach'
import './App.css'

function App() {
  return (
    <div className="app">
      <ParticleBackground />
      <Routes>
        {/* Public routes */}
        <Route path="/login" element={<Login />} />
        <Route path="/signup" element={<Signup />} />
        <Route path="/verify-email" element={<VerifyEmail />} />
        
        {/* Protected routes - require authentication */}
        <Route
          path="/app"
          element={
            <ProtectedRoute>
              <AppPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/app/profile"
          element={
            <ProtectedRoute>
              <ProfilePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile-setup"
          element={
            <ProtectedRoute>
              <ProfileSetup />
            </ProtectedRoute>
          }
        />
        <Route
          path="/profile/edit"
          element={
            <ProtectedRoute>
              <EditProfile />
            </ProtectedRoute>
          }
        />
        <Route
          path="/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/app/dashboard"
          element={
            <ProtectedRoute>
              <Dashboard />
            </ProtectedRoute>
          }
        />
        <Route
          path="/meal-plan"
          element={
            <ProtectedRoute>
              <MealPlan />
            </ProtectedRoute>
          }
        />
        <Route
          path="/app/meal-plan"
          element={
            <ProtectedRoute>
              <MealPlan />
            </ProtectedRoute>
          }
        />
        <Route
          path="/meal-preferences"
          element={
            <ProtectedRoute>
              <MealPreferences />
            </ProtectedRoute>
          }
        />
        <Route
          path="/ai-coach"
          element={
            <ProtectedRoute>
              <AICoach />
            </ProtectedRoute>
          }
        />
        
        {/* Default redirect */}
        <Route path="/" element={<Navigate to="/login" replace />} />
      </Routes>
    </div>
  )
}

export default App

