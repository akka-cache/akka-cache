import { useState, useCallback } from 'react';
import { useNavigate } from '@remix-run/react';
import { sendSignInLinkToEmail } from 'firebase/auth';
import { auth } from '~/utils/firebase-config';
import type { AuthStatus, UserData } from '~/types/auth';

interface UseSignUpOptions {
  onSuccess?: () => void;
  onError?: (error: Error) => void;
}

export function useSignUp(options: UseSignUpOptions = {}) {
  const [status, setStatus] = useState<AuthStatus>('idle');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const navigate = useNavigate();

  const handleSignUp = useCallback(async (userData: UserData) => {
    try {
      setStatus('loading');
      await sendSignInLinkToEmail(auth, userData.email, {
        url: window.location.origin + '/auth/verify-email',
        handleCodeInApp: true
      });
      
      // Store the email and user data for verification
      window.localStorage.setItem('emailForSignIn', userData.email);
      window.localStorage.setItem('signUpData', JSON.stringify(userData));
      
      setStatus('success');
      options.onSuccess?.();
    } catch (error) {
      setStatus('error');
      const errorMessage = error instanceof Error ? error.message : 'Sign up failed';
      setErrorMessage(errorMessage);
      options.onError?.(error instanceof Error ? error : new Error(errorMessage));
      throw error;
    }
  }, [options]);

  return {
    status,
    errorMessage,
    handleSignUp
  };
} 