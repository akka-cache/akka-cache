import { useCallback } from 'react';
import { useNavigate } from '@remix-run/react';
import { signOut } from 'firebase/auth';
import { auth } from '~/utils/firebase-config';

export function useAuthActions() {
  const navigate = useNavigate();

  const handleSignOut = useCallback(async () => {
    try {
      await signOut(auth);
      // Clear any auth-related items from localStorage
      window.localStorage.removeItem('emailForSignIn');
      window.localStorage.removeItem('signUpData');
      navigate('/auth/sign-in');
    } catch (error) {
      console.error('Sign out error:', error);
      throw error;
    }
  }, [navigate]);

  return {
    handleSignOut
  };
}