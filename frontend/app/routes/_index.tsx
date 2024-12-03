// app/routes/index.tsx
import { Container, Title, Text, Button, Anchor } from '@mantine/core';
import { Link } from '@remix-run/react';
import Layout from '~/components/Layout';
import { useThemeColor } from '~/utils/theme';

const MainHeading = ({ color }: { color: string }) => (
  <Title order={1} className="text-center mb-6" c={color}>
    Enterprise-Grade Serverless Caching
  </Title>
);

const Subtitle = ({ color }: { color: string }) => (
  <Text size="lg" className="text-center mb-8" c={color}>
    Simplify your architecture
  </Text>
);

const SignInSection = ({ secondaryTextColor }: { secondaryTextColor: string }) => (
  <>
    <Button component={Link} to="/sign-in" size="lg">
      Sign In
    </Button>

    <Text size="sm" c={secondaryTextColor}>
      Don't have an account?{' '}
      <Anchor component={Link} to="/sign-up">
        Sign Up
      </Anchor>
    </Text>
  </>
);

export default function Index() {
  const colors = {
    heading: useThemeColor('headingText'),
    body: useThemeColor('bodyText'),
    secondary: useThemeColor('secondaryText'),
    muted: useThemeColor('mutedText'),
  };

  return (
    <Layout>
      <Container 
        size="lg" 
        className="min-h-screen flex flex-col justify-center items-center"
      >
        <div className="w-full max-w-2xl">
          <MainHeading color={colors.heading} />
          <Subtitle color={colors.body} />
          
          <div className="flex flex-col items-center space-y-4">
            <SignInSection secondaryTextColor={colors.secondary} />
          </div>
        </div>
      </Container>
    </Layout>
  );
}