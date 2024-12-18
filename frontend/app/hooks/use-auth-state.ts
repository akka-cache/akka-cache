import { useState, useEffect } from 'react';
import { User, onAuthStateChanged } from 'firebase/auth';
import { auth } from '~/utils/firebase-config';
import type { AuthState, UserData } from '~/types/auth';

export function useAuthState() {
  const [authState, setAuthState] = useState<AuthState>({
    isAuthenticated: false,
    isLoading: true,
    user: null,
  });

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (user: User | null) => {
      setAuthState({
        isAuthenticated: !!user,
        isLoading: false,
        user: user ? {
          email: user.email || '',
          displayName: user.displayName || undefined,
        } : null,
      });
    });

    return () => unsubscribe();
  }, []);

  return authState;
}