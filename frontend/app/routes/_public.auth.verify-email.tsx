import { useEffect, useState } from 'react';
import { Card, Text, Loader } from '@mantine/core';
import { useNavigate } from '@remix-run/react';
import { isSignInWithEmailLink, signInWithEmailLink, getAdditionalUserInfo } from 'firebase/auth';
import { auth } from '~/utils/firebase-config';
import { useThemeColor } from '~/utils/theme';

export default function VerifyEmail() {
  const navigate = useNavigate();
  const [status, setStatus] = useState<'loading' | 'error'>('loading');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const bodyTextColor = useThemeColor('bodyText');

  useEffect(() => {
    async function verifyEmail() {
      console.log('Starting email verification...');
      console.log('Current URL:', window.location.href);
      
      if (!isSignInWithEmailLink(auth, window.location.href)) {
        console.log('Invalid verification link');
        setStatus('error');
        setErrorMessage('Invalid verification link');
        navigate('/auth/sign-in', { replace: true });
        return;
      }

      const email = window.localStorage.getItem('emailForSignIn');
      console.log('Retrieved email:', email);
      
      if (!email) {
        console.log('No email found in localStorage');
        setStatus('error');
        setErrorMessage('No email found. Please try signing in again.');
        navigate('/auth/sign-in', { replace: true });
        return;
      }

      try {
        console.log('Attempting to sign in with email link...');
        const result = await signInWithEmailLink(auth, email, window.location.href);
        console.log('Sign in successful:', result);
        
        const idToken = await result.user.getIdToken();
        console.log('Got ID token');

        const response = await fetch('/api/auth/session', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({ 
            idToken,
            userData: {
              email: result.user.email,
              displayName: result.user.displayName || email.split('@')[0]
            }
          }),
        });

        if (!response.ok) {
          throw new Error('Failed to create session');
        }

        console.log('Session created successfully');
        window.localStorage.removeItem('emailForSignIn');
        navigate('/', { replace: true });
      } catch (error) {
        console.error('Verification error:', error);
        setStatus('error');
        setErrorMessage(error instanceof Error ? error.message : 'Verification failed');
        navigate('/auth/sign-in', { replace: true });
      }
    }

    verifyEmail();
  }, [navigate]);

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
            <Text c="red" size="sm">
              {errorMessage}
            </Text>
          </div>
        )}
      </Card>
    </div>
  );
}