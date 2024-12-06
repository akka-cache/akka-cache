import { useState, useEffect } from 'react';
import { useNavigate } from '@remix-run/react';
import { auth } from '~/utils/firebase-config';
import { 
  sendSignInLinkToEmail, 
  fetchSignInMethodsForEmail,
  isSignInWithEmailLink,
  signInWithEmailLink,
  updateProfile
} from 'firebase/auth';
import type { AuthStatus } from '~/types/auth';

interface UseAuthFlowOptions {
  mode: 'signin' | 'signup';
  redirectUrl: string;
}

interface UserData {
  displayName: string;
  organization: string;
  mobileNumber?: string;
}

export function useAuthFlow({ mode, redirectUrl }: UseAuthFlowOptions) {
  const [status, setStatus] = useState<AuthStatus>('idle');
  const [errorMessage, setErrorMessage] = useState('');
  const navigate = useNavigate();
  
  useEffect(() => {
    const handleEmailLink = async () => {
      if (isSignInWithEmailLink(auth, window.location.href)) {
        try {
          const emailFromStorage = window.localStorage.getItem('emailForSignIn');
          const userDataFromStorage = window.localStorage.getItem('userDataForSignUp');
          
          if (!emailFromStorage) {
            setStatus('error');
            setErrorMessage('Please provide your email again');
            return;
          }

          setStatus('loading');
          
          const result = await signInWithEmailLink(auth, emailFromStorage, window.location.href);
          
          // If this was a sign-up and we have user data, update the profile
          if (userDataFromStorage && result.user) {
            const parsedUserData: UserData = JSON.parse(userDataFromStorage);
            await updateProfile(result.user, {
              displayName: parsedUserData.displayName
            });
            
            // store additional user data in your database here
          }

          // Clear storage
          window.localStorage.removeItem('emailForSignIn');
          window.localStorage.removeItem('userDataForSignUp');
          
          // Redirect to home page
          navigate('/', { replace: true });
        } catch (error: any) {
          setStatus('error');
          setErrorMessage(error.message);
          navigate('/auth/sign-in', { replace: true });
        }
      }
    };

    handleEmailLink();
  }, [navigate]);

  const handleEmailSubmit = async (email: string, userData?: UserData) => {
    setStatus('checking');
    
    const actionCodeSettings = {
      url: `${window.location.origin}${redirectUrl}`,
      handleCodeInApp: true
    };

    try {
      if (mode === 'signin') {
        // Check if user exists first
        const signInMethods = await fetchSignInMethodsForEmail(auth, email);
        
        if (signInMethods.length === 0) {
          // Redirect to sign-up with the email
          navigate('/auth/sign-up', { 
            state: { email },
            replace: true 
          });
          return;
        }
      }

      // Store user data for after email verification
      if (mode === 'signup' && userData) {
        window.localStorage.setItem('userDataForSignUp', JSON.stringify(userData));
      }

      await sendSignInLinkToEmail(auth, email, actionCodeSettings);
      window.localStorage.setItem('emailForSignIn', email);
      setStatus('success');
      
    } catch (error: any) {
      setStatus('error');
      setErrorMessage(error.message || 'An error occurred');
    }
  };

  return {
    status,
    errorMessage,
    handleEmailSubmit
  };
} 