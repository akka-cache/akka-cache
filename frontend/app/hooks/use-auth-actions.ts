import { useNavigate } from '@remix-run/react';
import { signOut } from 'firebase/auth';
import { auth } from '~/utils/firebase-config';

export function useAuthActions() {
  const navigate = useNavigate();

  const handleSignOut = async () => {
    try {
      await signOut(auth);
      navigate('/auth/sign-in');
    } catch (error) {
      console.error('Error signing out:', error);
      throw error; // Allow the component to handle the error
    }
  };

  return {
    handleSignOut,
  };
}