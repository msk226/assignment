/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#D9FD01',
        'primary-foreground': '#1A1A1A',
      },
      borderRadius: {
        DEFAULT: '16px',
      }
    },
  },
  plugins: [],
}
