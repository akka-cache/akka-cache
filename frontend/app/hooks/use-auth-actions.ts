import { useNavigate } from '@remix-run/react';
import { auth } from '~/utils/firebase-config';

export function useAuthActions() {
  const navigate = useNavigate();

  const handleSignOut = async () => {
    try {
      // Sign out from Firebase client
      await auth.signOut();
      
      // Call server-side sign out
      await fetch("/api/auth/sign-out", {
        method: "POST",
        credentials: "same-origin"
      });

      // Navigate to sign-in page
      navigate("/auth/sign-in");
    } catch (error) {
      console.error("Sign out error:", error);
      // Still navigate to sign-in even if there's an error
      navigate("/auth/sign-in");
    }
  };

  return { handleSignOut };
}