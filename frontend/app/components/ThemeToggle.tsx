import { ActionIcon, useMantineColorScheme } from '@mantine/core';
import { IconSun, IconMoon } from '@tabler/icons-react';

export function ThemeToggle() {
  const { colorScheme, setColorScheme } = useMantineColorScheme();
  const isDark = colorScheme === 'dark';

  const toggleColorScheme = () => {
    const newScheme = isDark ? 'light' : 'dark';
    setColorScheme(newScheme);
    document.documentElement.style.colorScheme = newScheme;
  };

  return (
    <ActionIcon 
      variant="default" 
      onClick={toggleColorScheme} 
      size="lg"
      aria-label="Toggle color scheme"
    >
      {isDark ? <IconSun size={18} /> : <IconMoon size={18} />}
    </ActionIcon>
  );
}