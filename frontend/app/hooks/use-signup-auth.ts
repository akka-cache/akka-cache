import { useState } from 'react';
import { useNavigate } from '@remix-run/react';
import { 
  sendSignInLinkToEmail,
  isSignInWithEmailLink,
  signInWithEmailLink,
  updateProfile
} from 'firebase/auth';
import { auth } from '~/utils/firebase-config';
import type { AuthStatus, UserData } from '~/types/auth';

interface UseSignupAuthOptions {
  redirectUrl: string;
  onSuccess?: () => void;
  onError?: (error: Error) => void;
}

export function useSignupAuth({ redirectUrl, onSuccess, onError }: UseSignupAuthOptions) {
  const [status, setStatus] = useState<AuthStatus>('idle');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const navigate = useNavigate();

  const sendSignUpLink = async (userData: UserData) => {
    setStatus('loading');
    setErrorMessage('');

    try {
      const actionCodeSettings = {
        url: `${window.location.origin}${redirectUrl}`,
        handleCodeInApp: true,
      };

      await sendSignInLinkToEmail(auth, userData.email, actionCodeSettings);
      // Store both email and user data for later use
      window.localStorage.setItem('emailForSignIn', userData.email);
      window.localStorage.setItem('signUpData', JSON.stringify(userData));
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
    const signUpDataString = window.localStorage.getItem('signUpData');

    if (!email || !signUpDataString) {
      setStatus('error');
      setErrorMessage('No email found. Please try signing up again.');
      return;
    }

    try {
      const result = await signInWithEmailLink(auth, email, window.location.href);
      const signUpData = JSON.parse(signUpDataString);

      // Update user profile with additional data
      await updateProfile(result.user, {
        displayName: signUpData.displayName
      });

      const idToken = await result.user.getIdToken();

      // Create session
      const response = await fetch('/api/auth/session', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ 
          idToken,
          userData: signUpData // Send additional user data to be stored server-side if needed
        }),
      });

      if (!response.ok) {
        throw new Error('Failed to create session');
      }

      // Clean up localStorage
      window.localStorage.removeItem('emailForSignIn');
      window.localStorage.removeItem('signUpData');
      
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
    sendSignUpLink,
    handleEmailLink,
  };
} 