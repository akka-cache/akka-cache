import { useState } from 'react';
import { useNavigate } from '@remix-run/react';
import { 
  sendSignInLinkToEmail,
  isSignInWithEmailLink,
  signInWithEmailLink,
  getAdditionalUserInfo
} from 'firebase/auth';
import { auth } from '~/utils/firebase-config';
import type { AuthStatus } from '~/types/auth';

interface UseEmailAuthOptions {
  redirectUrl: string;
  onSuccess?: () => void;
  onError?: (error: Error) => void;
}

export function useEmailAuth({ redirectUrl, onSuccess, onError }: UseEmailAuthOptions) {
  const [status, setStatus] = useState<AuthStatus>('idle');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const navigate = useNavigate();

  const sendAuthLink = async (email: string) => {
    setStatus('loading');
    setErrorMessage('');

    try {
      const actionCodeSettings = {
        url: `${window.location.origin}${redirectUrl}`,
        handleCodeInApp: true,
      };

      await sendSignInLinkToEmail(auth, email, actionCodeSettings);
      window.localStorage.setItem('emailForSignIn', email);
      setStatus('success');
      onSuccess?.();
    } catch (error: any) {
      setStatus('error');
      setErrorMessage(error.message);
      onError?.(error);
    }
  };

  const handleEmailLink = async () => {
    if (!isSignInWithEmailLink(auth, window.location.href)) {
      return;
    }

    setStatus('loading');
    const email = window.localStorage.getItem('emailForSignIn');

    if (!email) {
      setStatus('error');
      setErrorMessage('No email found. Please try signing in again.');
      return;
    }

    try {
      const result = await signInWithEmailLink(auth, email, window.location.href);
      const additionalUserInfo = getAdditionalUserInfo(result);
      
      // If this is a new user, they shouldn't be using the sign-in flow
      if (additionalUserInfo?.isNewUser) {
        await auth.signOut();
        setStatus('error');
        setErrorMessage('No account found with this email. Please sign up first.');
        navigate('/auth/sign-up');
        return;
      }

      const idToken = await result.user.getIdToken();

      // Create session
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

      window.localStorage.removeItem('emailForSignIn');
      setStatus('success');
      onSuccess?.();
      navigate('/');
    } catch (error: any) {
      setStatus('error');
      setErrorMessage(error.message);
      onError?.(error);
    }
  };

  return {
    status,
    errorMessage,
    sendAuthLink,
    handleEmailLink,
  };
} 