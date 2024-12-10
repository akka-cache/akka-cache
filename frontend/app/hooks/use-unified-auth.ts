import { useState } from 'react';
import { useNavigate } from '@remix-run/react';
import { 
  sendSignInLinkToEmail,
  isSignInWithEmailLink,
  signInWithEmailLink,
  getAdditionalUserInfo,
  signOut
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
        setErrorMessage('No account found. Would you like to create one?');
        window.localStorage.setItem('pendingSignUpEmail', email);
        navigate('/auth/sign-up');
        return;
      }
      
      setErrorMessage('Error sending sign-in link. Please try again.');
      onError?.(error instanceof Error ? error : new Error(error.message));
    }
  };

  const handleSignUp = async (userData: UserData) => {
    setStatus('loading');
    try {
      const actionCodeSettings = {
        url: `${window.location.origin}/auth/verify-email?mode=signUp`,
        handleCodeInApp: true
      };

      await sendSignInLinkToEmail(auth, userData.email, actionCodeSettings);
      window.localStorage.setItem('emailForSignIn', userData.email);
      window.localStorage.setItem('pendingUserData', JSON.stringify(userData));
      window.localStorage.setItem('authMode', 'signUp');
      setStatus('success');
    } catch (error: any) {
      console.error('Sign-up error:', error);
      setStatus('error');
      setErrorMessage('Error sending sign-up link. Please try again.');
      onError?.(error instanceof Error ? error : new Error(error.message));
    }
  };

  const completeSignIn = async () => {
    try {
      if (isSignInWithEmailLink(auth, window.location.href)) {
        const email = window.localStorage.getItem('emailForSignIn');
        const authMode = window.localStorage.getItem('authMode');

        if (!email) {
          throw new Error('Please provide your email again');
        }

        setStatus('loading');
        
        const result = await signInWithEmailLink(auth, email, window.location.href);
        const additionalInfo = getAdditionalUserInfo(result);
        
        // If this is a new user trying to sign in, redirect them to sign up
        if (additionalInfo?.isNewUser) {
          await signOut(auth);
          window.localStorage.setItem('pendingSignUpEmail', email);
          throw new Error('No account found. Please sign up first.');
        }

        // Clean up storage
        window.localStorage.removeItem('emailForSignIn');
        window.localStorage.removeItem('authMode');
        
        setStatus('success');
        navigate('/');
        onSuccess?.();
      }
    } catch (error: any) {
      console.error('Authentication error:', error);
      setStatus('error');
      setErrorMessage(error.message);
      onError?.(error instanceof Error ? error : new Error(error.message));
      
      // If this was a new user, redirect to sign up
      if (error.message.includes('Please sign up first')) {
        navigate('/auth/sign-up');
      } else {
        navigate('/auth/sign-in');
      }
    }
  };

  return {
    status,
    errorMessage,
    handleSignIn,
    handleSignUp,
    completeSignIn
  };
}