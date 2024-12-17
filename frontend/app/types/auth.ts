// Keep your existing AuthStatus
export type AuthStatus = 
  | 'idle' 
  | 'checking' 
  | 'loading' 
  | 'success' 
  | 'error';

// Add these new types
export interface UserData {
  email: string;
  displayName?: string;
}

export interface AuthState {
  isAuthenticated: boolean;
  isLoading: boolean;
  user: UserData | null;
}