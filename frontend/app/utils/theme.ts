import { createTheme, MantineThemeOverride } from '@mantine/core';

const themeOverride: MantineThemeOverride = {
  colors: {
    light: [
      '#FFFFFF',  // light.0 - background
      '#F8F9FA',  // light.1 - sidebar background
      '#F4F4F4',  // light.2 - card background
      '#E9ECEF',  // light.3 - borders
      '#DEE2E6',  // light.4 - muted elements
      '#ADB5BD',  // light.5 - medium gray for text
      '#868E96',  // light.6 - secondary text
      '#495057',  // light.7 - primary text
      '#212529',  // light.8 - emphasized text
      '#1A1A1A'   // light.9 - dark text
    ],
    dark: [
      '#000000',  // dark.0 - background
      '#1A1A1A',  // dark.1 - sidebar background
      '#333333',  // dark.2 - card background
      '#4E4E4E',  // dark.3 - borders
      '#666666',  // dark.4 - muted elements
      '#888888',  // dark.5 - medium gray for text
      '#AAAAAA',  // dark.6 - secondary text
      '#CCCCCC',  // dark.7 - primary text
      '#E1E1E1',  // dark.8 - emphasized text
      '#F1F1F1'   // dark.9 - light text
    ],
  },
  primaryColor: 'dark',
};

export default createTheme(themeOverride);
