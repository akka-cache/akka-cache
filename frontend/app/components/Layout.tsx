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
  useMantineColorScheme
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
};

export default function Layout({ children }: LayoutProps) {
  const [opened, { toggle }] = useDisclosure();
  const navigate = useNavigate();
  const location = useLocation();
  const { colorScheme } = useMantineColorScheme();
  const { handleSignOut } = useAuthActions();
  const { user } = useAuthState();

  const mainLinks: MainLink[] = [
    { icon: IconPlus, label: 'Create Cache', href: '/create-cache' },
    { icon: IconDatabase, label: 'My Caches', href: '/caches' },
    { icon: IconChartLine, label: 'Observability', href: '/observability' },
    { icon: IconBook, label: 'Documentation', href: '/docs' },
  ];

  const bottomLinks: BottomLink[] = [
    { icon: IconSettings, label: 'Settings', href: '/settings' },
    { icon: IconCreditCard, label: 'Billing', href: '/billing' },
    { 
      icon: IconLogout, 
      label: 'Sign Out', 
      onClick: handleSignOut,
      color: 'red' 
    },
  ];

  const renderNavLink = (link: MainLink | BottomLink) => {
    if (link.href) {
      return (
        <NavLink
          key={link.label}
          label={link.label}
          c={colorScheme === 'dark' ? 'dark.9' : 'gray.7'}
          leftSection={
            <link.icon
              style={{
                width: rem(20),
                color: `var(--mantine-color-${colorScheme === 'dark' ? 'akkaAccent-4' : 'akkaAccent-4'})`
              }}
              stroke={1.5}
            />
          }
          active={location.pathname === link.href}
          component={Link}
          to={link.href}
          color={link.color}
          onClick={link.onClick}
        />
      );
    }

    return (
      <NavLink
        key={link.label}
        label={link.label}
        c={colorScheme === 'dark' ? 'dark.9' : 'gray.7'}
        leftSection={
          <link.icon
            style={{
              width: rem(20),
              color: `var(--mantine-color-${colorScheme === 'dark' ? 'akkaAccent-4' : 'akkaAccent-4'})`
            }}
            stroke={1.5}
          />
        }
        color={link.color}
        onClick={link.onClick}
      />
    );
  };

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
        <AppShell.Section>
          <Group pb="md" px="md">
            <Burger opened={opened} onClick={toggle} hiddenFrom="sm" size="sm" />
            <Link
              to="/dashboard"
              className="text-xl font-bold navbar-logo"
              style={{ color: colorScheme === 'dark' ? 'var(--mantine-color-dark-9)' : 'var(--mantine-color-gray-8)' }}
            >
              Akka Cache
            </Link>
          </Group>
        </AppShell.Section>

        <AppShell.Section grow>
          <Stack gap={0}>
            {mainLinks.map(renderNavLink)}
          </Stack>
        </AppShell.Section>

        <AppShell.Section>
          <Stack gap={0}>
            {bottomLinks.map(renderNavLink)}
            <ThemeToggle />
            {user && (
              <Menu position="right-end" shadow="md" width={200}>
                <Menu.Target>
                  <UnstyledButton className="w-full p-3">
                    <Group>
                      <Avatar radius="xl" alt={user.displayName || user.email} />
                      <div style={{ flex: 1 }}>
                        <Text
                          size="sm"
                          fw={500}
                          c={colorScheme === 'dark' ? 'dark.9' : 'gray.7'}
                        >
                          {user.displayName || 'User'}
                        </Text>
                        <Text
                          size="xs"
                          c={colorScheme === 'dark' ? 'gray.6' : 'gray.5'}
                        >
                          {user.email}
                        </Text>
                      </div>
                    </Group>
                  </UnstyledButton>
                </Menu.Target>
                <Menu.Dropdown>
                  <Menu.Label>Account</Menu.Label>
                  <Menu.Item leftSection={<IconUser size={14} />}>
                    Profile
                  </Menu.Item>
                  <Menu.Divider />
                  <Menu.Item
                    leftSection={<IconLogout size={14} />}
                    onClick={handleSignOut}
                    color="red"
                  >
                    Sign Out
                  </Menu.Item>
                </Menu.Dropdown>
              </Menu>
            )}
          </Stack>
        </AppShell.Section>
      </AppShell.Navbar>

      <AppShell.Main>
        {children}
      </AppShell.Main>
    </AppShell>
  );
}