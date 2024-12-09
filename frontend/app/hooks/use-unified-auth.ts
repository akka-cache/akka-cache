import { useState, useEffect } from 'react';
import { useNavigate } from '@remix-run/react';
import { 
  sendSignInLinkToEmail, 
  isSignInWithEmailLink,
  signInWithEmailLink,
  updateProfile,
  setPersistence,
  browserLocalPersistence,
  getAdditionalUserInfo
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
      console.log('Starting email sign-in for:', email);
      
      const actionCodeSettings = {
        url: `${window.location.origin}/`,  // Direct to home for sign-in
        handleCodeInApp: true,
      };
      
      try {
        await sendSignInLinkToEmail(auth, email, actionCodeSettings);
        console.log('Sign-in link sent successfully');
        
        window.localStorage.setItem('verificationEmail', email);
        window.localStorage.setItem('isSignUp', 'false');
        setStatus('success');
        
      } catch (error: any) {
        console.log('Firebase error response:', {
          code: error.code,
          message: error.message,
          fullError: error
        });
        
        if (error.code === 'auth/invalid-email') {
          throw new Error('Please enter a valid email address');
        }

        // Handle specific Firebase errors
        switch (error.code) {
          case 'auth/invalid-email':
            throw new Error('Please enter a valid email address');
          case 'auth/operation-not-allowed':
            console.error('Email link authentication is not enabled in Firebase');
            throw new Error('Email authentication is not properly configured');
          default:
            throw error;
        }
      }
    } catch (error) {
      console.error('Sign in error:', {
        error,
        message: error instanceof Error ? error.message : 'Unknown error',
        code: error instanceof Error ? (error as any).code : 'unknown',
        stack: error instanceof Error ? error.stack : 'No stack trace'
      });
      setStatus('error');
      setErrorMessage(error instanceof Error ? error.message : 'An error occurred');
      onError?.(error instanceof Error ? error : new Error('An error occurred'));
    }
  };

  const handleSignUpSubmit = async (userData: UserData) => {
    setStatus('checking');
    try {
      console.log('Starting sign-up process for:', userData.email);

      const actionCodeSettings = {
        url: `${window.location.origin}/auth/verify-email`,  // Keep verify-email for sign-up
        handleCodeInApp: true
      };

      try {
        await sendSignInLinkToEmail(auth, userData.email, actionCodeSettings);
        console.log('Verification email sent successfully');

        window.localStorage.setItem('verificationEmail', userData.email);
        window.localStorage.setItem('isSignUp', 'true');
        window.localStorage.setItem('pendingUserData', JSON.stringify(userData));
        
        setStatus('success');
      } catch (error: any) {
        console.error('Sign-up error:', {
          code: error.code,
          message: error.message,
          fullError: error
        });

        if (error.code === 'auth/email-already-in-use') {
          throw new Error('An account with this email already exists. Please sign in instead.');
        }
        
        if (error.code === 'auth/invalid-email') {
          throw new Error('Please enter a valid email address.');
        }

        if (error.code === 'auth/operation-not-allowed') {
          console.error('Email/Password authentication is not enabled in Firebase');
          throw new Error('Email authentication is not properly configured.');
        }

        throw error;
      }
    } catch (error) {
      setStatus('error');
      setErrorMessage(error instanceof Error ? error.message : 'An error occurred');
      onError?.(error instanceof Error ? error : new Error('An error occurred'));
    }
  };

  const handleEmailLink = async () => {
    try {
      const email = window.localStorage.getItem('verificationEmail');
      const isSignUp = window.localStorage.getItem('isSignUp') === 'true';
      
      if (!email) {
        throw new Error('Please provide your email again');
      }

      setStatus('loading');

      // Sign in with email link
      const userCredential = await signInWithEmailLink(auth, email, window.location.href);
      console.log('Successfully signed in with email link');

      // Set user persistence to LOCAL (stays signed in)
      await setPersistence(auth, browserLocalPersistence);

      // Check if this is a new user
      const additionalUserInfo = getAdditionalUserInfo(userCredential);
      const isNewUser = additionalUserInfo?.isNewUser;

      if (isNewUser && !isSignUp) {
        // User doesn't exist but tried to sign in
        console.log('New user detected during sign-in attempt');
        window.localStorage.removeItem('verificationEmail');
        window.localStorage.removeItem('isSignUp');
        navigate('/auth/sign-up', { 
          state: { email },
          replace: true 
        });
        return;
      }

      if (isSignUp && userCredential.user) {
        const pendingUserData = JSON.parse(
          window.localStorage.getItem('pendingUserData') || '{}'
        );

        if (pendingUserData.displayName) {
          await updateProfile(userCredential.user, {
            displayName: pendingUserData.displayName
          });
          console.log('Updated user profile');
        }
      }

      // Clean up
      window.localStorage.removeItem('verificationEmail');
      window.localStorage.removeItem('isSignUp');
      window.localStorage.removeItem('pendingUserData');
      
      setStatus('success');
      console.log('Redirecting to home page');
      navigate('/', { replace: true });
      onSuccess?.();
    } catch (error) {
      console.error('Email link error:', error);
      setStatus('error');
      setErrorMessage(error instanceof Error ? error.message : 'An error occurred');
      
      // Redirect based on the context
      const isSignUp = window.localStorage.getItem('isSignUp') === 'true';
      const redirectPath = isSignUp ? '/auth/sign-up' : '/auth/sign-in';
      
      // Only redirect if there's an actual error
      if (error instanceof Error && 
          !error.message.includes('already signed in')) {
        navigate(redirectPath, { replace: true });
      }
      onError?.(error instanceof Error ? error : new Error('An error occurred'));
    }
  };

  return {
    status,
    errorMessage,
    handleEmailCheck,
    handleSignUpSubmit,
    handleEmailVerification: handleEmailLink,
    handleEmailLink
  };
}