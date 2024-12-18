import { createContext, useContext, useEffect, useState } from 'react';
import { onAuthStateChanged } from 'firebase/auth';
import { useNavigate, useLoaderData } from '@remix-run/react';
import { auth } from '~/utils/firebase-config';
import type { AuthState, UserData } from '~/types/auth';

interface AuthContextType extends AuthState {
  handleSignOut: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType>({
  isAuthenticated: false,
  isLoading: true,
  user: null,
  handleSignOut: async () => {},
});

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const navigate = useNavigate();
  const data = useLoaderData<{ user: UserData | null }>();
  
  console.log('AuthProvider - Initial data from loader:', data);
  
  // Initialize with server-side state
  const [authState, setAuthState] = useState<AuthState>(() => {
    const initialState = {
      isAuthenticated: !!data?.user,
      isLoading: false,
      user: data?.user || null,
    };
    console.log('AuthProvider - Setting initial state:', initialState);
    return initialState;
  });

  // Update state when server data changes
  useEffect(() => {
    console.log('AuthProvider - Data changed:', data);
    console.log('AuthProvider - Current state:', authState);
    
    if (data?.user) {
      console.log('AuthProvider - Updating state with user:', data.user);
      setAuthState({
        isAuthenticated: true,
        isLoading: false,
        user: data.user,
      });
    }
  }, [data]);

  const handleSignOut = async () => {
    try {
      await auth.signOut();
      
      await fetch("/api/auth/sign-out", {
        method: "POST",
        credentials: "same-origin"
      });

      setAuthState({
        isAuthenticated: false,
        isLoading: false,
        user: null,
      });

      navigate("/auth/sign-in");
    } catch (error) {
      console.error("Sign out error:", error);
      navigate("/auth/sign-in");
    }
  };

  console.log('AuthProvider - Rendering with state:', authState);

  return (
    <AuthContext.Provider value={{ ...authState, handleSignOut }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuthState = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuthState must be used within an AuthProvider');
  }
  console.log('useAuthState - Returning state:', context);
  return {
    isAuthenticated: context.isAuthenticated,
    isLoading: context.isLoading,
    user: context.user,
  };
};

export const useAuthActions = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuthActions must be used within an AuthProvider');
  }
  return { handleSignOut: context.handleSignOut };
}; 