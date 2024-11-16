import { Container, Title, Text, Button, Anchor } from '@mantine/core';
import { Link } from '@remix-run/react';
import Layout from '~/components/Layout';

export default function Index() {
  return (
    <Layout>
      <Container size="lg" className="min-h-screen flex flex-col justify-center">
        <Title order={1} className="text-center mb-6">
          Enterprise-Grade Serverless Caching 
        </Title>
        
        <Text size="lg" className="text-center mb-8" c="dimmed">
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
          
          <Text size="sm">
            Don't have an account?{' '}
            <Anchor component={Link} to="/sign-up">
              Sign Up
            </Anchor>
          </Text>

          <Text size="xs" c="dimmed" className="text-center mt-4 max-w-md">
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