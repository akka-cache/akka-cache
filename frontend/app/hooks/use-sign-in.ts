import { useState } from 'react';
import { useNavigate } from '@remix-run/react';
import { 
  sendSignInLinkToEmail,
  signInWithEmailLink,
  isSignInWithEmailLink,
  signOut,
  getAdditionalUserInfo
} from 'firebase/auth';
import { auth } from '~/utils/firebase-config';
import type { AuthStatus } from '~/types/auth';

interface UseSignInOptions {
  onSuccess?: () => void;
  onError?: (error: Error) => void;
}

export function useSignIn({ onSuccess, onError }: UseSignInOptions) {
  const [status, setStatus] = useState<AuthStatus>('idle');
  const [errorMessage, setErrorMessage] = useState('');
  const navigate = useNavigate();

  const handleSignIn = async (email: string) => {
    setStatus('loading');
    try {
      const actionCodeSettings = {
        url: `${window.location.origin}/`,
        handleCodeInApp: true
      };

      await sendSignInLinkToEmail(auth, email, actionCodeSettings);
      window.localStorage.setItem('emailForSignIn', email);
      window.localStorage.setItem('authMode', 'signIn');
      setStatus('success');
    } catch (error: any) {
      console.error('Sign-in error:', error);
      setStatus('error');
      
      if (error.code === 'auth/user-not-found') {
        setErrorMessage('No account found with this email. Please sign up first.');
        navigate('/auth/sign-up');
      } else {
        setErrorMessage('Error sending sign-in link. Please try again.');
      }
      
      onError?.(error instanceof Error ? error : new Error(error.message));
    }
  };

  const completeSignIn = async () => {
    if (!isSignInWithEmailLink(auth, window.location.href)) {
      return;
    }

    const email = window.localStorage.getItem('emailForSignIn');
    const authMode = window.localStorage.getItem('authMode');

    if (!email) {
      setErrorMessage('Please provide your email again');
      navigate('/auth/sign-in');
      return;
    }

    if (authMode !== 'signIn') {
      setErrorMessage('Invalid authentication flow. Please try signing in again.');
      navigate('/auth/sign-in');
      return;
    }

    setStatus('loading');

    try {
      const result = await signInWithEmailLink(auth, email, window.location.href);
      const additionalInfo = getAdditionalUserInfo(result);
      
      if (additionalInfo?.isNewUser) {
        await signOut(auth);
        setErrorMessage('No account found with this email. Please sign up first.');
        navigate('/auth/sign-up');
        return;
      }

      // Get the ID token and create server session
      const idToken = await result.user.getIdToken();
      const response = await fetch('/api/create-session', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ idToken }),
      });

      if (!response.ok) {
        throw new Error('Failed to create session');
      }

      window.localStorage.removeItem('emailForSignIn');
      window.localStorage.removeItem('authMode');
      
      setStatus('success');
      navigate('/');
      onSuccess?.();
    } catch (error: any) {
      console.error('Sign-in error:', error);
      setStatus('error');
      await signOut(auth);
      
      if (error.code === 'auth/invalid-action-code') {
        setErrorMessage('This sign-in link has expired or is invalid. Please try signing in again.');
      } else {
        setErrorMessage('Error signing in. Please try again.');
      }
      
      onError?.(error instanceof Error ? error : new Error(error.message));
      navigate('/auth/sign-in');
    }
  };

  return {
    status,
    errorMessage,
    handleSignIn,
    completeSignIn
  };
} 