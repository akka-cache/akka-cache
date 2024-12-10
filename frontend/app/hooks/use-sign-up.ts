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

interface UseSignUpOptions {
  onSuccess?: () => void;
  onError?: (error: Error) => void;
}

export function useSignUp({ onSuccess, onError }: UseSignUpOptions) {
  const [status, setStatus] = useState<AuthStatus>('idle');
  const [errorMessage, setErrorMessage] = useState('');
  const navigate = useNavigate();

  const handleSignUp = async (userData: UserData) => {
    setStatus('loading');
    try {
      const actionCodeSettings = {
        url: `${window.location.origin}/finish-sign-up`,
        handleCodeInApp: true
      };

      await sendSignInLinkToEmail(auth, userData.email, actionCodeSettings);
      
      // Store the email and user data for later use
      window.localStorage.setItem('emailForSignIn', userData.email);
      window.localStorage.setItem('pendingUserData', JSON.stringify(userData));
      window.localStorage.setItem('authMode', 'signUp');
      
      setStatus('success');
      navigate('/auth/verify-email');
      onSuccess?.();
    } catch (error: any) {
      console.error('Sign-up error:', error);
      setStatus('error');
      
      if (error.code === 'auth/email-already-in-use') {
        setErrorMessage('An account with this email already exists. Please sign in instead.');
        navigate('/auth/sign-in');
      } else {
        setErrorMessage('Error creating account. Please try again.');
      }
      
      onError?.(error instanceof Error ? error : new Error(error.message));
    }
  };

  const completeSignUp = async () => {
    if (!isSignInWithEmailLink(auth, window.location.href)) {
      return;
    }

    try {
      const email = window.localStorage.getItem('emailForSignIn');
      const pendingUserData = window.localStorage.getItem('pendingUserData');

      if (!email || !pendingUserData) {
        throw new Error('Missing registration information');
      }

      const userData = JSON.parse(pendingUserData);
      const result = await signInWithEmailLink(auth, email, window.location.href);

      // Update the user profile with additional information
      if (userData.displayName) {
        await updateProfile(result.user, {
          displayName: userData.displayName
        });
      }

      // Clean up localStorage
      window.localStorage.removeItem('emailForSignIn');
      window.localStorage.removeItem('pendingUserData');

      setStatus('success');
      navigate('/');
      onSuccess?.();
    } catch (error: any) {
      console.error('Sign-up completion error:', error);
      setStatus('error');
      setErrorMessage('Error completing registration. Please try again.');
      onError?.(error instanceof Error ? error : new Error(error.message));
      navigate('/auth/sign-up');
    }
  };

  return {
    status,
    errorMessage,
    handleSignUp,
    completeSignUp
  };
} 