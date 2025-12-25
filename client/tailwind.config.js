/** @type {import('tailwindcss').Config} */
export default {
  content: [
    './index.html',
    './src/**/*.{js,jsx,ts,tsx}'
  ],
  theme: {
    fontFamily: {
      sans: ['Inter', 'Sohne', 'SF Pro Display', 'Segoe UI', 'system-ui', 'sans-serif'],
    },
    extend: {
      colors: {
        base: {
          900: '#050713',
          800: '#080b1d',
          700: '#0f172a',
          600: '#111b33',
          500: '#1e2742',
          400: '#2c3552',
        },
        accent: {
          DEFAULT: '#7C3AED',
          foreground: '#F9FAFB',
          muted: '#B794F6',
        },
        success: '#22c55e',
        warning: '#facc15',
        danger: '#f87171',
        muted: '#94a3b8',
        border: 'rgba(148, 163, 184, 0.25)',
        card: '#0f172a',
      },
      boxShadow: {
        'soft-xl': '0 24px 60px rgba(8, 12, 24, 0.65)',
        'soft-lg': '0 18px 40px rgba(15, 23, 42, 0.45)',
        'card': '0 10px 35px rgba(8, 11, 20, 0.4)',
        'glow': '0 0 25px rgba(124, 58, 237, 0.35)',
      },
      borderRadius: {
        xl: '1.5rem',
        '2xl': '1.75rem',
      },
      backgroundImage: {
        'ai-card': 'radial-gradient(circle at 20% 20%, rgba(124, 58, 237, 0.35), transparent 55%), radial-gradient(circle at 80% 0%, rgba(56, 189, 248, 0.35), transparent 50%)',
        'hero-glow': 'radial-gradient(circle at top, rgba(124,58,237,0.35), transparent 50%)',
      },
      keyframes: {
        fadeInUp: {
          '0%': { opacity: 0, transform: 'translateY(12px)' },
          '100%': { opacity: 1, transform: 'translateY(0)' },
        },
        pulseBorder: {
          '0%': { opacity: 0.35 },
          '50%': { opacity: 0.9 },
          '100%': { opacity: 0.35 },
        },
        shimmer: {
          '0%': { backgroundPosition: '-468px 0' },
          '100%': { backgroundPosition: '468px 0' },
        },
      },
      animation: {
        'fade-in-up': 'fadeInUp 0.6s ease forwards',
        'pulse-border': 'pulseBorder 2.6s ease-in-out infinite',
        'shimmer': 'shimmer 1.5s linear infinite',
      },
    },
  },
  plugins: [],
}

