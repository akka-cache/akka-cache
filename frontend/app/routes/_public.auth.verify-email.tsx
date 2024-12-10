import { useEffect, useState } from 'react';
import { Card, Text, Loader } from '@mantine/core';
import { useNavigate } from '@remix-run/react';
import { 
  isSignInWithEmailLink, 
  signInWithEmailLink,
  getAdditionalUserInfo
} from 'firebase/auth';
import { auth } from '~/utils/firebase-config';
import { useThemeColor } from '~/utils/theme';

export default function VerifyEmail() {
  const navigate = useNavigate();
  const [status, setStatus] = useState<'loading' | 'error'>('loading');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const bodyTextColor = useThemeColor('bodyText');

  useEffect(() => {
    async function verifyEmail() {
      console.log('Starting email verification process...');
      
      try {
        // Step 1: Get the auth action and email
        const authAction = window.localStorage.getItem('authAction');
        const email = window.localStorage.getItem('emailForSignIn');
        
        if (!authAction || !email) {
          throw new Error('Invalid verification attempt. Please try again.');
        }

        // Step 2: Validate the email link
        if (!isSignInWithEmailLink(auth, window.location.href)) {
          throw new Error('Invalid verification link');
        }

        if (authAction === 'signIn') {
          // SIGN IN FLOW
          try {
            const credential = await signInWithEmailLink(auth, email, window.location.href);
            
            // Verify this isn't a new user
            const userInfo = getAdditionalUserInfo(credential);
            if (userInfo?.isNewUser) {
              await auth.signOut();
              throw new Error('No account found. Please sign up first.');
            }

            // Verify required attributes exist
            const user = credential.user;
            if (!user.emailVerified || !user.displayName) {
              await auth.signOut();
              throw new Error('Account not properly configured. Please complete the sign up process.');
            }

            // Create session
            const idToken = await user.getIdToken();
            const response = await fetch('/api/auth/session', {
              method: 'POST',
              headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify({ 
                idToken,
                userData: {
                  email: user.email,
                  displayName: user.displayName
                }
              })
            });

            if (!response.ok) {
              throw new Error('Failed to create session');
            }

            // Success - clean up and redirect
            window.localStorage.removeItem('emailForSignIn');
            window.localStorage.removeItem('authAction');
            navigate('/', { replace: true });

          } catch (error) {
            if (auth.currentUser) {
              await auth.signOut();
            }
            throw error;
          }

        } else if (authAction === 'signUp') {
          // SIGN UP FLOW
          // For sign-up, we don't create the user here
          // Instead, redirect to the final sign-up step with the verified email
          window.localStorage.removeItem('emailForSignIn');
          window.localStorage.removeItem('authAction');
          navigate('/auth/sign-up/complete?email=' + encodeURIComponent(email), { replace: true });

        } else {
          throw new Error('Invalid authentication action');
        }

      } catch (error) {
        console.error('Verification error:', error);
        
        const errorMsg = error instanceof Error ? error.message : 'Verification failed';
        setStatus('error');
        setErrorMessage(errorMsg);
        
        // Clean up storage
        window.localStorage.removeItem('emailForSignIn');
        window.localStorage.removeItem('authAction');
        
        // Redirect based on the original action
        const redirectPath = window.localStorage.getItem('authAction') === 'signUp' 
          ? '/auth/sign-up' 
          : '/auth/sign-in';
        
        navigate(redirectPath + '?error=' + encodeURIComponent(errorMsg), { replace: true });
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