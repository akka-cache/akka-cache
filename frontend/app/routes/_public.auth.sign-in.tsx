import { Card, Text, Button, Group } from '@mantine/core';
import { useUnifiedAuth } from '~/hooks';
import { EmailForm } from '~/components/auth/email-form';
import { Logo, HeaderContent } from '~/components/auth/common';
import { useThemeColor } from '~/utils/theme';
import { useOutletContext } from '@remix-run/react';
import type { AuthStatus } from '~/types/auth';
import { Link } from '@remix-run/react';

export default function SignIn() {
  const {
    status,
    errorMessage,
    handleEmailCheck
  } = useUnifiedAuth({
    redirectUrl: '/',
    onSuccess: () => {
      // Optional: Add any success handling here
    },
    onError: (error) => {
      console.error('Sign in error:', error);
    }
  });

  const headingTextColor = useThemeColor('headingText');
  const bodyTextColor = useThemeColor('bodyText');
  const context = useOutletContext<string>();

  if (context === 'right') {
    return (
      <div>
        <Text size="xl" c={bodyTextColor}>More content goes here</Text>
      </div>
    );
  }

  return (
    <div className="w-full max-w-2xl mx-auto p-6">
      <Logo />
      <HeaderContent 
        headingColor={headingTextColor}
        textColor={bodyTextColor}
      />
      
      <Card shadow="md" p="xl" className="w-full mb-8" bg="dark.0">
        <EmailForm
          onSubmit={handleEmailCheck}
          status={status}
          errorMessage={errorMessage}
          successMessage="Check your email for the sign-in link!"
        />
        
        {status === 'error' && (
          <div className="mt-4 text-center">
            <Text size="sm" c="dimmed" mb="md">
              Having trouble signing in?
            </Text>
            <Group justify="center" gap="sm">
              <Button variant="subtle" size="sm" component={Link} to="/auth/sign-up">
                Create an Account
              </Button>
              <Button variant="subtle" size="sm" onClick={() => window.location.reload()}>
                Try Again
              </Button>
            </Group>
          </div>
        )}
      </Card>
    </div>
  );
}