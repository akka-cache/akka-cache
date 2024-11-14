import { test, expect } from '@playwright/test';

test('home page shows welcome message', async ({ page }) => {
  await page.goto('http://localhost:5173/');
  
  await expect(page.getByText('Welcome to Remix')).toBeVisible();
  
  // Test navigation links
  const links = [
    { text: 'Quick Start', visible: true },
    { text: 'Tutorial', visible: true },
    { text: 'Remix Docs', visible: true },
    { text: 'Join Discord', visible: true }
  ];

  for (const { text, visible } of links) {
    await expect(page.getByText(text)).toBeVisible({ visible });
  }
});