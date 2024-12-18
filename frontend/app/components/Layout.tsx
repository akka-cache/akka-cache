import {
  AppShell,
  Burger,
  Group,
  UnstyledButton,
  Menu,
  Avatar,
  NavLink,
  Stack,
  Text,
  rem,
  useMantineColorScheme,
  Paper,
  ActionIcon
} from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import { Link, useNavigate, useLocation } from '@remix-run/react';
import {
  IconLogout,
  IconSettings,
  IconUser,
  IconDatabase,
  IconChartLine,
  IconBook,
  IconCreditCard,
  IconPlus
} from '@tabler/icons-react';
import { ThemeToggle } from './ThemeToggle';
import { useAuthActions, useAuthState } from '~/hooks';

type BaseLink = {
  icon: React.ComponentType<any>;
  label: string;
  color?: string;
  onClick?: () => void;
};

type MainLink = BaseLink & {
  href: string;
};

type BottomLink = BaseLink & {
  href?: string;
};

type LayoutProps = {
  children: React.ReactNode;
  user: any;
};

export default function Layout({ children, user }: LayoutProps) {
  const [opened, { toggle }] = useDisclosure();
  const navigate = useNavigate();
  const location = useLocation();
  const { colorScheme } = useMantineColorScheme();
  const { handleSignOut } = useAuthActions();

  console.log('Layout - Component rendered with user:', user);

  if (!user) {
    console.log('Layout - No user provided');
    return (
      <AppShell
        navbar={{
          width: 300,
          breakpoint: 'sm',
          collapsed: { mobile: !opened }
        }}
        bg={colorScheme === 'dark' ? 'dark.0' : 'gray.0'}
      >
        <AppShell.Navbar p="md" bg={colorScheme === 'dark' ? 'dark.1' : 'gray.1'}>
          <div style={{ padding: '8px', background: '#333', color: 'white' }}>
            Debug - Auth State: Not authenticated
          </div>
        </AppShell.Navbar>
        <AppShell.Main>
          {children}
        </AppShell.Main>
      </AppShell>
    );
  }

  return (
    <AppShell
      navbar={{
        width: 300,
        breakpoint: 'sm',
        collapsed: { mobile: !opened }
      }}
      bg={colorScheme === 'dark' ? 'dark.0' : 'gray.0'}
    >
      <AppShell.Navbar p="md" bg={colorScheme === 'dark' ? 'dark.1' : 'gray.1'}>
        <Stack>
          <NavLink
            label="Create Cache"
            leftSection={<IconPlus style={{ width: rem(20), color: colorScheme === 'dark' ? 'white' : 'black' }} stroke={1.5} />}
            component={Link}
            to="/create-cache"
            active={location.pathname === '/create-cache'}
            c={colorScheme === 'dark' ? 'white' : 'dark'}
          />
          <NavLink
            label="My Caches"
            leftSection={<IconDatabase style={{ width: rem(20), color: colorScheme === 'dark' ? 'white' : 'black' }} stroke={1.5} />}
            component={Link}
            to="/caches"
            active={location.pathname === '/caches'}
            c={colorScheme === 'dark' ? 'white' : 'dark'}
          />
          <NavLink
            label="Observability"
            leftSection={<IconChartLine style={{ width: rem(20), color: colorScheme === 'dark' ? 'white' : 'black' }} stroke={1.5} />}
            component={Link}
            to="/observability"
            active={location.pathname === '/observability'}
            c={colorScheme === 'dark' ? 'white' : 'dark'}
          />
          <NavLink
            label="Documentation"
            leftSection={<IconBook style={{ width: rem(20), color: colorScheme === 'dark' ? 'white' : 'black' }} stroke={1.5} />}
            component={Link}
            to="/docs"
            active={location.pathname === '/docs'}
            c={colorScheme === 'dark' ? 'white' : 'dark'}
          />
        </Stack>

        <Stack mt="auto">
          <NavLink
            label="Settings"
            leftSection={<IconSettings style={{ width: rem(20), color: colorScheme === 'dark' ? 'white' : 'black' }} stroke={1.5} />}
            component={Link}
            to="/settings"
            active={location.pathname === '/settings'}
            c={colorScheme === 'dark' ? 'white' : 'dark'}
          />
          <NavLink
            label="Billing"
            leftSection={<IconCreditCard style={{ width: rem(20), color: colorScheme === 'dark' ? 'white' : 'black' }} stroke={1.5} />}
            component={Link}
            to="/billing"
            active={location.pathname === '/billing'}
            c={colorScheme === 'dark' ? 'white' : 'dark'}
          />
        </Stack>

        <Paper withBorder p="md" mt="md">
          <Group>
            <Avatar color="blue" radius="xl">
              {user.displayName?.[0] || user.email[0]}
            </Avatar>
            <div style={{ flex: 1 }}>
              <Text size="sm" fw={500}>
                {user.displayName || user.email}
              </Text>
              <Text c="dimmed" size="xs">
                {user.email}
              </Text>
            </div>
            <Group gap="xs">
              <ThemeToggle />
              <ActionIcon variant="default" size="lg" onClick={handleSignOut}>
                <IconLogout size={18} />
              </ActionIcon>
            </Group>
          </Group>
        </Paper>
      </AppShell.Navbar>
      <AppShell.Main>
        {children}
      </AppShell.Main>
    </AppShell>
  );
}