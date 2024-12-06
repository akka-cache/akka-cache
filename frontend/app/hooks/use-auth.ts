import { useNavigate } from '@remix-run/react';
import { auth } from '~/utils/firebase-config';
import { signOut } from 'firebase/auth';

export function useAuth() {
  const navigate = useNavigate();

  const handleSignOut = async () => {
    try {
      await signOut(auth);
      navigate('/auth/sign-in');
    } catch (error) {
      console.error('Error signing out:', error);
    }
  };

  return {
    handleSignOut
  };
} 