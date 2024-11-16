import { createTheme, MantineThemeOverride, MantineColorsTuple } from '@mantine/core';

// Define custom color schemes for the theme
const akkaCachePrimary: MantineColorsTuple = [
  "#E6F9F9", "#CCF3F3", "#99E7E7", "#66DBDB",
  "#33CFCF", "#00D8DD", "#00B4B6", "#008D8E",
  "#006667", "#003F40"
];

const darkThemeOverride: MantineThemeOverride = {
  colors: {
    akkaCachePrimary,
    dark: [
      '#000000', // background
      '#1A1A1A', // surface level 1
      '#333333', // surface level 2
      '#4e4e4e', // sub headers
      '#666666', // others
      '#888888',
      '#AAAAAA',
      '#CCCCCC',
      '#E1E1E1',
      '#f1f1f1'  // main heading, body text
    ],
  },
  primaryColor: 'akkaCachePrimary',
  headings: {
    fontFamily: 'Inter, sans-serif',
    sizes: {
      h1: { fontSize: '2.25rem', lineHeight: '1.2' },
      h2: { fontSize: '1.75rem', lineHeight: '1.3' },
      h3: { fontSize: '1.5rem', lineHeight: '1.4' },
    }
  }
};

// Export the base theme for light mode if needed similarly
export default createTheme(darkThemeOverride);
