import { useEffect, useState } from 'react';
import { Card, Text, Loader, Button } from '@mantine/core';
import { useUnifiedAuth } from '~/hooks';
import { Logo } from '~/components/auth/common';
import { useThemeColor } from '~/utils/theme';
import { useNavigate, useOutletContext } from '@remix-run/react';

export default function VerifyEmail() {
  console.log('VerifyEmail component mounted');
  const navigate = useNavigate();
  const [isVerificationLink, setIsVerificationLink] = useState(false);
  const context = useOutletContext<string>();
  
  const {
    status,
    errorMessage,
    handleEmailLink
  } = useUnifiedAuth({
    redirectUrl: '/',
    onSuccess: () => {
      console.log('Verification successful');
    },
    onError: (error) => {
      console.error('Verification error:', error);
    }
  });

  const headingTextColor = useThemeColor('headingText');
  const bodyTextColor = useThemeColor('bodyText');

  useEffect(() => {
    if (typeof window !== 'undefined') {
      const isEmailLink = window.location.href.includes('mode=signIn');
      setIsVerificationLink(isEmailLink);
    }
  }, []);

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
      
      <Card shadow="md" p="xl" className="w-full mb-8" bg="dark.0">
        <div className="text-center">
          {!isVerificationLink ? (
            <>
              <Text size="lg" c={headingTextColor} className="mb-4">
                This page is for email verification.
              </Text>
              <Button 
                onClick={() => navigate('/auth/sign-in')}
                size="md"
              >
                Go to Sign In
              </Button>
            </>
          ) : status === 'loading' || status === 'checking' ? (
            <>
              <Loader size="lg" className="mx-auto mb-4" />
              <Text size="lg" c={headingTextColor}>Verifying your email...</Text>
            </>
          ) : status === 'error' ? (
            <>
              <Text size="lg" c="red" className="text-center mb-4">
                {errorMessage || 'An error occurred during verification'}
              </Text>
              <Button 
                onClick={() => navigate('/auth/sign-in')}
                size="md"
              >
                Return to Sign In
              </Button>
            </>
          ) : (
            <Text size="lg" c={bodyTextColor}>
              Email verified successfully! Redirecting...
            </Text>
          )}
        </div>
      </Card>
    </div>
  );
} 