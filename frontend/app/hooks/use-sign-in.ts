import { useState, useCallback } from 'react';
import { useNavigate } from '@remix-run/react';
import { sendSignInLinkToEmail } from 'firebase/auth';
import { auth } from '~/utils/firebase-config';
import type { AuthStatus } from '~/types/auth';

interface UseSignInOptions {
  onSuccess?: () => void;
  onError?: (error: Error) => void;
}

export function useSignIn(options: UseSignInOptions = {}) {
  const [status, setStatus] = useState<AuthStatus>('idle');
  const [errorMessage, setErrorMessage] = useState<string>('');
  const navigate = useNavigate();

  const handleSignIn = useCallback(async (email: string) => {
    try {
      setStatus('loading');
      await sendSignInLinkToEmail(auth, email, {
        url: window.location.origin + '/auth/verify-email',
        handleCodeInApp: true
      });
      
      // Store the email for verification
      window.localStorage.setItem('emailForSignIn', email);
      
      setStatus('success');
      options.onSuccess?.();
    } catch (error) {
      setStatus('error');
      const errorMessage = error instanceof Error ? error.message : 'Sign in failed';
      setErrorMessage(errorMessage);
      options.onError?.(error instanceof Error ? error : new Error(errorMessage));
      throw error;
    }
  }, [options]);

  return {
    status,
    errorMessage,
    handleSignIn
  };
} 