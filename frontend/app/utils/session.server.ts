import { createCookieSessionStorage } from '@remix-run/node';

const sessionSecret = process.env.SESSION_SECRET || 'default_secret';

// Main session storage (your existing code)
const storage = createCookieSessionStorage({
  cookie: {
    name: '__session',
    secure: true,
    secrets: [sessionSecret],
    sameSite: 'lax',
    path: '/',
    httpOnly: true,
  },
});

// Temporary session storage for email verification
const tempStorage = createCookieSessionStorage({
  cookie: {
    name: '__temp_session',
    secure: true,
    secrets: [sessionSecret],
    sameSite: 'lax',
    path: '/',
    httpOnly: true,
    maxAge: 60 * 10, // 10 minutes
  },
});

// Main session exports
export const { getSession, commitSession, destroySession } = storage;

// Temporary session helpers
export const {
  getSession: getTempSession,
  commitSession: commitTempSession,
  destroySession: destroyTempSession,
} = tempStorage;

// Helper functions for temp session
export async function createTempEmailSession(email: string) {
  const session = await getTempSession();
  session.set('tempEmail', email);
  return commitTempSession(session);
}

export async function getTempEmailFromSession(request: Request) {
  const session = await getTempSession(request.headers.get('Cookie'));
  return session.get('tempEmail');
} 