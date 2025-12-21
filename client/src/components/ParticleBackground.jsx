import React, { useEffect, useRef } from 'react'
import './ParticleBackground.css'

/**
 * ParticleBackground Component
 * 
 * Creates an animated futuristic AI particle background
 * with metallic silver and neon blue particles floating
 * in a deep space environment.
 */
function ParticleBackground() {
  const canvasRef = useRef(null)

  useEffect(() => {
    const canvas = canvasRef.current
    if (!canvas) return

    const reducedMotionQuery = window.matchMedia('(prefers-reduced-motion: reduce)')
    if (reducedMotionQuery.matches) {
      return
    }

    const ctx = canvas.getContext('2d')
    let animationFrameId
    let particles = []

    // Set canvas size
    const resizeCanvas = () => {
      const scale = window.devicePixelRatio || 1
      canvas.width = window.innerWidth * scale
      canvas.height = window.innerHeight * scale
      canvas.style.width = '100%'
      canvas.style.height = '100%'
      ctx.setTransform(scale, 0, 0, scale, 0, 0)
      rebuildParticles()
    }

    const getParticleCount = () => {
      const area = window.innerWidth * window.innerHeight
      const base = Math.floor(area / 40000)
      const isNarrow = window.innerWidth < 1024
      return Math.max(30, Math.min(isNarrow ? 60 : 120, base))
    }

    resizeCanvas()
    window.addEventListener('resize', resizeCanvas)
    const handleReducedMotion = (event) => {
      if (event.matches) {
        cancelAnimationFrame(animationFrameId)
        window.removeEventListener('resize', resizeCanvas)
        particles = []
      }
    }
    if (typeof reducedMotionQuery.addEventListener === 'function') {
      reducedMotionQuery.addEventListener('change', handleReducedMotion)
    } else if (typeof reducedMotionQuery.addListener === 'function') {
      reducedMotionQuery.addListener(handleReducedMotion)
    }

    // Particle class
    class Particle {
      constructor() {
        this.reset()
        this.y = Math.random() * canvas.height
      }

      reset() {
        this.x = Math.random() * canvas.width
        this.y = canvas.height + Math.random() * 200
        this.size = Math.random() * 2.5 + 0.8
        this.speedY = Math.random() * 0.4 + 0.15
        this.speedX = (Math.random() - 0.5) * 0.5
        this.opacity = Math.random() * 0.5 + 0.3
        this.rotation = Math.random() * 360
        this.rotationSpeed = (Math.random() - 0.5) * 1.2
        // Choose particle type: silver or neon blue
        this.type = Math.random() > 0.5 ? 'silver' : 'neon'
      }

      update() {
        this.y -= this.speedY
        this.x += this.speedX + Math.sin(this.y * 0.01) * 0.5
        this.rotation += this.rotationSpeed

        // Reset particle if it goes off screen
        if (this.y < -this.size || this.x < -this.size || this.x > canvas.width + this.size) {
          this.reset()
          this.y = canvas.height + Math.random() * 100
        }
      }

      draw() {
        ctx.save()
        ctx.translate(this.x, this.y)
        ctx.rotate((this.rotation * Math.PI) / 180)
        ctx.globalAlpha = this.opacity

        if (this.type === 'silver') {
          // Metallic silver particle
          const gradient = ctx.createLinearGradient(-this.size, -this.size, this.size, this.size)
          gradient.addColorStop(0, 'rgba(192, 192, 192, 0.8)')
          gradient.addColorStop(0.5, 'rgba(192, 192, 192, 0.5)')
          gradient.addColorStop(1, 'rgba(192, 192, 192, 0.2)')
          ctx.fillStyle = gradient
          ctx.shadowBlur = 6
          ctx.shadowColor = 'rgba(192, 192, 192, 0.5)'
        } else {
          // Neon blue particle
          const gradient = ctx.createRadialGradient(0, 0, 0, 0, 0, this.size)
          gradient.addColorStop(0, 'rgba(96, 175, 255, 0.9)')
          gradient.addColorStop(0.5, 'rgba(96, 175, 255, 0.5)')
          gradient.addColorStop(1, 'rgba(96, 175, 255, 0)')
          ctx.fillStyle = gradient
          ctx.shadowBlur = 9
          ctx.shadowColor = 'rgba(96, 175, 255, 0.8)'
        }

        // Draw particle as a diamond/hexagon shape for futuristic look
        ctx.beginPath()
        ctx.moveTo(0, -this.size)
        ctx.lineTo(this.size * 0.7, 0)
        ctx.lineTo(0, this.size)
        ctx.lineTo(-this.size * 0.7, 0)
        ctx.closePath()
        ctx.fill()

        // Add glow effect
        ctx.globalAlpha = this.opacity * 0.3
        ctx.fillStyle = this.type === 'silver' 
          ? 'rgba(192, 192, 192, 0.3)'
          : 'rgba(96, 175, 255, 0.3)'
        ctx.beginPath()
        ctx.arc(0, 0, this.size * 1.5, 0, Math.PI * 2)
        ctx.fill()

        ctx.restore()
      }
    }

    // Initialize particles
    const rebuildParticles = () => {
      const targetCount = getParticleCount()
      particles = []
      for (let i = 0; i < targetCount; i++) {
        particles.push(new Particle())
      }
    }

    rebuildParticles()

    // Animation loop
    const animate = () => {
      ctx.clearRect(0, 0, canvas.width, canvas.height)

      particles.forEach((particle) => {
        particle.update()
        particle.draw()
      })

      animationFrameId = requestAnimationFrame(animate)
    }

    animate()

    // Cleanup
    return () => {
      window.removeEventListener('resize', resizeCanvas)
      if (typeof reducedMotionQuery.removeEventListener === 'function') {
        reducedMotionQuery.removeEventListener('change', handleReducedMotion)
      } else if (typeof reducedMotionQuery.removeListener === 'function') {
        reducedMotionQuery.removeListener(handleReducedMotion)
      }
      cancelAnimationFrame(animationFrameId)
    }
  }, [])

  return (
    <canvas
      ref={canvasRef}
      className="particle-background"
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        width: '100%',
        height: '100%',
        zIndex: 0,
        pointerEvents: 'none',
      }}
    />
  )
}

export default ParticleBackground

