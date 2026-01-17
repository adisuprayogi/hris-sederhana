const path = require('path');

/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    path.join(__dirname, 'src/main/resources/templates/**/*.html'),
    path.join(__dirname, 'src/main/frontend/**/*.{js,ts}')
  ],
  theme: {
    extend: {},
  },
  plugins: [],
}
