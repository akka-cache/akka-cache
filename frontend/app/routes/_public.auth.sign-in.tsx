import { Card, Text, Button, Group } from '@mantine/core';
import { useAuthState } from '~/hooks/use-auth-state';
import { useSignIn } from '~/hooks/use-sign-in';
import { EmailForm } from '~/components/auth/email-form';
import { Logo, HeaderContent } from '~/components/auth/common';
import { useThemeColor } from '~/utils/theme';
import { useOutletContext, Link } from '@remix-run/react';
import { useState } from 'react';

type AuthStatus = 'idle' | 'loading' | 'success' | 'error';

export default function SignIn() {
  const [status, setStatus] = useState<AuthStatus>('idle');
  const [errorMessage, setErrorMessage] = useState('');
  const { user } = useAuthState();
  const { handleSignIn } = useSignIn({
    onSuccess: () => {
      console.log('Sign-in link sent successfully');
    },
    onError: (error) => {
      console.error('Sign-in error:', error);
    }
  });

  const headingTextColor = useThemeColor('headingText');
  const bodyTextColor = useThemeColor('bodyText');
  const context = useOutletContext<string>();

  const handleSubmit = async (email: string) => {
    try {
      setStatus('loading');
      await handleSignIn(email);
      setStatus('success');
    } catch (error: unknown) {
      setStatus('error');
      setErrorMessage(error instanceof Error ? error.message : 'Sign in failed');
      console.error('Sign in error:', error);
    }
  };

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
        title="Welcome Back"
        subtitle="Sign in to your account"
        headingColor={headingTextColor}
        textColor={bodyTextColor}
      />
      
      <Card shadow="md" p="xl" className="w-full mb-8" bg="dark.0">
        <EmailForm
          onSubmit={handleSubmit}
          status={status}
          errorMessage={errorMessage}
          successMessage={status === 'success' ? 
            "We've sent you an email with a sign-in link. Click the link to continue." : 
            undefined}
        />
        
        <div className="mt-4 text-center">
          <Text size="sm" c="dimmed" mb="md">
            Don't have an account?
          </Text>
          <Button 
            variant="subtle" 
            size="sm" 
            component={Link} 
            to="/auth/sign-up"
          >
            Create an Account
          </Button>
        </div>
      </Card>
    </div>
  );
}