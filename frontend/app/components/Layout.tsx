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

  type LayoutProps = {
    children: React.ReactNode;
    user?: {
      name: string;
      email: string;
      avatar?: string;
    };
  };

  export default function Layout({ children, user }: LayoutProps) {
    const [opened, { toggle }] = useDisclosure();
    const navigate = useNavigate();
    const location = useLocation();
    const { colorScheme } = useMantineColorScheme();

    const handleSignOut = () => {
      navigate('/sign-in');
    };

    const mainLinks = [
      { icon: IconPlus, label: 'Create Cache', href: '/create-cache' },
      { icon: IconDatabase, label: 'My Caches', href: '/caches' },
      { icon: IconChartLine, label: 'Observability', href: '/observability' },
      { icon: IconBook, label: 'Documentation', href: '/docs' },
    ];

    const bottomLinks = [
      { icon: IconSettings, label: 'Settings', href: '/settings' },
      { icon: IconCreditCard, label: 'Billing', href: '/billing' },
    ];

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
              {mainLinks.map((link) => (
                <NavLink
                  key={link.href}
                  component={Link}
                  to={link.href}
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
                />
              ))}
            </Stack>
          </AppShell.Section>

          <AppShell.Section>
            <Stack gap={0}>
              {bottomLinks.map((link) => (
                <NavLink
                  key={link.href}
                  component={Link}
                  to={link.href}
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
                />
              ))}
              <ThemeToggle />
              {user && (
                <Menu position="right-end" shadow="md" width={200}>
                  <Menu.Target>
                    <UnstyledButton className="w-full p-3">
                      <Group>
                        <Avatar src={user.avatar} radius="xl" alt={user.name} />
                        <div style={{ flex: 1 }}>
                          <Text
                            size="sm"
                            fw={500}
                            c={colorScheme === 'dark' ? 'dark.9' : 'gray.7'}
                          >
                            {user.name}
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