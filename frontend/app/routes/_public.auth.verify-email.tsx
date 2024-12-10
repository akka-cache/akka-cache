import { useEffect, useState } from 'react';
import { useNavigate } from '@remix-run/react';
import { onAuthStateChanged } from 'firebase/auth';
import { auth } from '~/utils/firebase-config';
import { useUnifiedAuth } from '~/hooks';
import { Card, Text, Loader } from '@mantine/core';
import { useThemeColor } from '~/utils/theme';

export default function VerifyEmail() {
  const navigate = useNavigate();
  const [verificationEmail, setVerificationEmail] = useState<string | null>(null);
  const bodyTextColor = useThemeColor('bodyText');
  
  const {
    status,
    errorMessage,
    handleEmailLink
  } = useUnifiedAuth({
    redirectUrl: '/',
    onSuccess: () => {
      console.log('Account creation successful');
    },
    onError: (error) => {
      console.error('Verification error:', error);
    }
  });

  useEffect(() => {
    // Check if this is an email verification link
    const email = window.localStorage.getItem('verificationEmail');
    const authAction = window.localStorage.getItem('authAction');
    setVerificationEmail(email);

    if (window.location.href.includes('emailLink=')) {
      handleEmailLink();
    }

    // Set up auth state listener
    const unsubscribe = onAuthStateChanged(auth, (user) => {
      if (user) {
        // User is signed in, redirect to home
        navigate('/', { replace: true });
      }
    });

    return () => unsubscribe();
  }, [navigate, handleEmailLink]);

  return (
    <div className="w-full max-w-2xl mx-auto p-6">
      <Card shadow="md" p="xl" className="w-full mb-8" bg="dark.0">
        {status === 'loading' ? (
          <div className="text-center">
            <Loader size="lg" className="mb-4" />
            <Text c={bodyTextColor}>Verifying your email...</Text>
          </div>
        ) : (
          <div className="text-center">
            <Text c={bodyTextColor} mb="md">
              {status === 'success' 
                ? 'Email verified successfully!' 
                : `Please check your email (${verificationEmail}) and click the verification link.`}
            </Text>
            {errorMessage && (
              <Text c="red" size="sm">
                {errorMessage}
              </Text>
            )}
          </div>
        )}
      </Card>
    </div>
  );
}