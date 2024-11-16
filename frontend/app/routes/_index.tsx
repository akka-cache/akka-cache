import { Container, Title, Text, Button, Anchor } from '@mantine/core';
import { Link } from '@remix-run/react';
import Layout from '~/components/Layout';
import { useMantineColorScheme } from '@mantine/core';

export default function Index() {
  const { colorScheme } = useMantineColorScheme();

  return (
    <Layout>
      <Container size="lg" className="min-h-screen flex flex-col justify-center">
        <Title order={1} className="text-center mb-6" c={colorScheme === 'dark' ? 'dark.9' : 'gray.8'}>
          Enterprise-Grade Serverless Caching
        </Title>

        <Text
          size="lg"
          className="text-center mb-8"
          c={colorScheme === 'dark' ? 'gray.6' : 'gray.5'}
        >
          Simplify your architecture
        </Text>

        <div className="flex flex-col items-center gap-4">
          <Button
            component={Link}
            to="/sign-in"
            size="lg"
          >
            Sign In
          </Button>

          <Text size="sm" c={colorScheme === 'dark' ? 'dark.9' : 'gray.7'}>
            Don't have an account?{' '}
            <Anchor component={Link} to="/sign-up">
              Sign Up
            </Anchor>
          </Text>

          <Text
            size="xs"
            c={colorScheme === 'dark' ? 'gray.6' : 'gray.5'}
            className="text-center mt-4 max-w-md"
          >
            By signing up, you acknowledge that you agree to our{' '}
            <Anchor component={Link} to="/privacy">
              Privacy Policy
            </Anchor>{' '}
            and{' '}
            <Anchor component={Link} to="/terms">
              Terms of Service
            </Anchor>
            .
          </Text>
        </div>
      </Container>
    </Layout>
  );
}