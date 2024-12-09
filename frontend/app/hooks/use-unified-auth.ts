import { useState, useEffect } from 'react';
import { useNavigate } from '@remix-run/react';
import { 
  sendSignInLinkToEmail, 
  isSignInWithEmailLink,
  signInWithEmailLink,
  updateProfile,
  fetchSignInMethodsForEmail,
  createUserWithEmailAndPassword
} from 'firebase/auth';
import { auth } from '~/utils/firebase-config';
import type { AuthStatus, UserData } from '~/types/auth';

interface UseUnifiedAuthOptions {
  redirectUrl: string;
  onSuccess?: () => void;
  onError?: (error: Error) => void;
}

export function useUnifiedAuth({
  redirectUrl,
  onSuccess,
  onError
}: UseUnifiedAuthOptions) {
  const [status, setStatus] = useState<AuthStatus>('idle');
  const [errorMessage, setErrorMessage] = useState('');
  const navigate = useNavigate();

  // Handle email link verification on component mount
  useEffect(() => {
    if (isSignInWithEmailLink(auth, window.location.href)) {
      handleEmailLink();
    }
  }, []);

  const handleEmailCheck = async (email: string) => {
    setStatus('checking');
    try {
      const signInMethods = await fetchSignInMethodsForEmail(auth, email);
      
      if (signInMethods.length === 0) {
        // New user - redirect to sign-up
        navigate('/auth/sign-up', { 
          state: { email },
          replace: true 
        });
        return;
      }

      // Existing user - send sign-in link
      const actionCodeSettings = {
        url: `${window.location.origin}${redirectUrl}`,
        handleCodeInApp: true
      };

      await sendSignInLinkToEmail(auth, email, actionCodeSettings);
      window.localStorage.setItem('emailForAuth', email);
      window.localStorage.setItem('isSignUp', 'false');
      setStatus('success');
    } catch (error) {
      setStatus('error');
      setErrorMessage(error instanceof Error ? error.message : 'An error occurred');
      onError?.(error instanceof Error ? error : new Error('An error occurred'));
    }
  };

  const handleSignUpSubmit = async (userData: UserData) => {
    setStatus('checking');
    try {
      const signInMethods = await fetchSignInMethodsForEmail(auth, userData.email);
      if (signInMethods.length > 0) {
        throw new Error('User already exists. Please sign in instead.');
      }

      const actionCodeSettings = {
        url: `${window.location.origin}${redirectUrl}`,
        handleCodeInApp: true
      };

      await sendSignInLinkToEmail(auth, userData.email, actionCodeSettings);
      window.localStorage.setItem('emailForAuth', userData.email);
      window.localStorage.setItem('isSignUp', 'true');
      window.localStorage.setItem('pendingUserData', JSON.stringify(userData));
      setStatus('success');
    } catch (error) {
      setStatus('error');
      setErrorMessage(error instanceof Error ? error.message : 'An error occurred');
      onError?.(error instanceof Error ? error : new Error('An error occurred'));
    }
  };

  const handleEmailLink = async () => {
    try {
      const email = window.localStorage.getItem('emailForAuth');
      const isSignUp = window.localStorage.getItem('isSignUp') === 'true';
      
      if (!email) {
        throw new Error('Please provide your email again');
      }

      setStatus('loading');

      if (isSignUp) {
        const pendingUserData = JSON.parse(
          window.localStorage.getItem('pendingUserData') || '{}'
        );

        // Create the user first
        const tempPassword = Math.random().toString(36).slice(-8) + 
                           Math.random().toString(36).slice(-8);
        
        const userCredential = await createUserWithEmailAndPassword(
          auth,
          email,
          tempPassword
        );

        if (userCredential.user && pendingUserData.displayName) {
          await updateProfile(userCredential.user, {
            displayName: pendingUserData.displayName
          });
        }
      }

      // Sign in with email link for both new and existing users
      await signInWithEmailLink(auth, email, window.location.href);

      // Clean up
      window.localStorage.removeItem('emailForAuth');
      window.localStorage.removeItem('isSignUp');
      window.localStorage.removeItem('pendingUserData');
      
      setStatus('success');
      navigate('/', { replace: true });
      onSuccess?.();
    } catch (error) {
      console.error('Email link error:', error);
      setStatus('error');
      setErrorMessage(error instanceof Error ? error.message : 'An error occurred');
      navigate('/auth/sign-in', { replace: true });
      onError?.(error instanceof Error ? error : new Error('An error occurred'));
    }
  };

  return {
    status,
    errorMessage,
    handleEmailCheck,
    handleSignUpSubmit,
    handleEmailLink
  };
}