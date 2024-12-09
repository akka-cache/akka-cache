import { Card, Text } from '@mantine/core';
import { useUnifiedAuth } from '~/hooks';
import { EmailForm } from '~/components/auth/email-form';
import { Logo, HeaderContent } from '~/components/auth/common';
import { useThemeColor } from '~/utils/theme';
import { useOutletContext } from '@remix-run/react';
import type { AuthStatus } from '~/types/auth';

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
      // Optional: Add any error handling here
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
      
      <Card shadow="md" p="xl" className="w-full mb-8 hl" bg="dark.0">
        <EmailForm
          onSubmit={handleEmailCheck}
          status={status}
          errorMessage={errorMessage}
          successMessage="Check your email for the sign-in link!"
        />
      </Card>
    </div>
  );
}