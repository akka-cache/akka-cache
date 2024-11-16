import { createTheme, MantineThemeOverride, MantineColorsTuple } from '@mantine/core';

const akkaCachePrimary: MantineColorsTuple = [
  "#E6F9F9", "#CCF3F3", "#99E7E7", "#66DBDB",
  "#33CFCF", "#00D8DD", "#00B4B6", "#008D8E",
  "#006667", "#003F40"
];

// Define both light and dark theme overrides
const themeOverride: MantineThemeOverride = {
  colors: {
    akkaCachePrimary,
    akkaAccent: [
      '#fff5d9',
      '#ffebbc',
      '#ffe19f',
      '#ffd782',
      '#ffce4a',
      '#ffb733',
      '#ff9f1a',
      '#ff8800',
      '#cc6600',
      '#994d00',
    ],
    // Light theme colors
    gray: [
      '#ffffff',  // gray.0 - background
      '#f8f9fa',  // gray.1 - sidebar-bg
      '#f4f4f4',  // gray.2
      '#e9ecef',  // gray.3
      '#dee2e6',  // gray.4
      '#6c757d',  // gray.5 - medium gray for secondary text
      '#495057',  // gray.6
      '#343a40',  // gray.7 - dark gray for primary text
      '#212529',  // gray.8
      '#1a1a1a',  // gray.9
    ],
    // Dark theme colors
    dark: [
      '#000000',  // dark.0 - background
      '#1A1A1A',  // dark.1 - sidebar-bg
      '#333333',  // dark.2
      '#4e4e4e',  // dark.3
      '#666666',  // dark.4
      '#888888',  // dark.5
      '#AAAAAA',  // dark.6
      '#CCCCCC',  // dark.7
      '#E1E1E1',  // dark.8
      '#f1f1f1'   // dark.9 - text color in dark mode
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

export default createTheme(themeOverride);
