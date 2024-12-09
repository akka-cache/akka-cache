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
      console.log('Starting email check for:', email);
      console.log('Firebase auth state:', {
        currentUser: auth.currentUser,
        config: {
          apiKey: auth.config.apiKey,
          authDomain: auth.config.authDomain
        }
      });

      try {
        // First, try to get sign-in methods for the email
        const signInMethods = await fetchSignInMethodsForEmail(auth, email);
        console.log('Sign-in methods:', signInMethods);

        // If no sign-in methods exist, the user doesn't exist
        if (!signInMethods.length) {
          console.log('No sign-in methods found, user does not exist');
          navigate('/auth/sign-up', { 
            state: { email },
            replace: true 
          });
          return;
        }

        // User exists, send sign-in link
        const actionCodeSettings = {
          url: `${window.location.origin}${redirectUrl}`,
          handleCodeInApp: true,
        };
        console.log('User exists, sending sign-in link with settings:', actionCodeSettings);

        await sendSignInLinkToEmail(auth, email, actionCodeSettings);
        console.log('Sign-in link sent successfully');
        
        window.localStorage.setItem('emailForSignIn', email);
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

        if (error.code === 'auth/operation-not-allowed') {
          console.error('Email/Password authentication is not enabled in Firebase');
          throw new Error('Email authentication is not properly configured');
        }

        throw error; // Re-throw other errors
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
        url: `${window.location.origin}/auth/verify-email`,
        handleCodeInApp: true
      };

      try {
        // Send verification email
        await sendSignInLinkToEmail(auth, userData.email, actionCodeSettings);
        console.log('Verification email sent successfully');

        // Store the pending user data and mark this as a sign-up flow
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

  const handleEmailVerification = async () => {
    try {
      const email = window.localStorage.getItem('verificationEmail');
      const isSignUp = window.localStorage.getItem('isSignUp') === 'true';
      const pendingUserDataString = window.localStorage.getItem('pendingUserData');
      
      if (!email) {
        throw new Error('No email found. Please try signing up again.');
      }

      setStatus('loading');
      console.log('Starting email verification for:', email);

      // Verify this is a valid sign-in link
      if (!isSignInWithEmailLink(auth, window.location.href)) {
        throw new Error('Invalid verification link');
      }

      // Complete the sign-in process
      const userCredential = await signInWithEmailLink(auth, email, window.location.href);
      console.log('Email verified successfully');

      // If this is a sign-up flow, update the user profile
      if (isSignUp && userCredential.user && pendingUserDataString) {
        const pendingUserData = JSON.parse(pendingUserDataString);
        console.log('Updating user profile with:', pendingUserData);

        await updateProfile(userCredential.user, {
          displayName: pendingUserData.displayName
        });

        // Here you could also store additional user data in your database
        // await saveUserDataToDatabase(userCredential.user.uid, pendingUserData);
      }
      // Clean up localStorage
      window.localStorage.removeItem('verificationEmail');
      window.localStorage.removeItem('isSignUp');
      window.localStorage.removeItem('pendingUserData');
      
      setStatus('success');
      navigate('/', { replace: true });
      onSuccess?.();
    } catch (error) {
      console.error('Email verification error:', error);
      setStatus('error');
      setErrorMessage(error instanceof Error ? error.message : 'An error occurred');
      navigate('/auth/sign-up', { replace: true });
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

      // Sign in with email link first
      const userCredential = await signInWithEmailLink(auth, email, window.location.href);

      if (isSignUp && userCredential.user) {
        const pendingUserData = JSON.parse(
          window.localStorage.getItem('pendingUserData') || '{}'
        );

        // Update the user profile if needed
        if (pendingUserData.displayName) {
          await updateProfile(userCredential.user, {
            displayName: pendingUserData.displayName
          });
        }
      }

      // Clean up
      window.localStorage.removeItem('verificationEmail');
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
    handleEmailVerification,
    handleEmailLink
  };
}