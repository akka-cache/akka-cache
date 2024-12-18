import { adminAuth } from '~/utils/firebase-admin.server';
import { redirect } from '@remix-run/node';
import { getSession } from '~/utils/session.server';

// Basic error class for auth errors
export class AuthError extends Error {
  constructor(message: string, public code?: string) {
    super(message);
    this.name = 'AuthError';
  }
}

export async function requireAuth(request: Request) {
  try {
    const session = await getSession(request.headers.get('Cookie'));
    const sessionCookie = session.get('session');

    if (!sessionCookie) {
      throw new AuthError('No session cookie found', 'auth/no-session');
    }

    // Verify the session cookie
    const decodedClaims = await adminAuth.verifySessionCookie(sessionCookie);
    
    // Get the latest user data and verify status
    const userRecord = await adminAuth.getUser(decodedClaims.uid);
    
    if (!userRecord.emailVerified) {
      throw new AuthError('Email not verified', 'auth/email-not-verified');
    }

    if (userRecord.disabled) {
      throw new AuthError('User account is disabled', 'auth/user-disabled');
    }

    return {
      ...decodedClaims,
      displayName: decodedClaims.name || decodedClaims.email?.split('@')[0]
    };
  } catch (error) {
    if (error instanceof AuthError) {
      console.error('Auth error:', { code: error.code, message: error.message });
      
      if (error.code === 'auth/email-not-verified') {
        throw redirect('/auth/verify-email');
      }
    } else {
      console.error('Unexpected auth error:', error);
    }
    
    throw redirect('/auth/sign-up');
  }
}

// Helper function for non-protected routes that need optional auth
export async function getUser(request: Request) {
  try {
    return await requireAuth(request);
  } catch (error) {
    if (error instanceof Response && error.status === 302) {
      return null;
    }
    throw error;
  }
}