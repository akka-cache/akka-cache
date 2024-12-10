import { createContext, useContext } from 'react';

interface User {
  email: string | null;
  displayName: string | null;
}

interface AuthContextType {
  user: User | null;
}

const AuthContext = createContext<AuthContextType | null>(null);

export function AuthProvider({ 
  children, 
  value 
}: { 
  children: React.ReactNode;
  value: AuthContextType;
}) {
  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (context === null) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
} 