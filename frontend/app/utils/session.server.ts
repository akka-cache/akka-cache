import { createCookieSessionStorage, Session, SessionData } from '@remix-run/node';

const sessionSecret = process.env.SESSION_SECRET;
if (!sessionSecret) {
  throw new Error('SESSION_SECRET must be set');
}

// Session configuration types
interface SessionConfig {
  maxAge: number;
  secure: boolean;
  secrets: string[];
  sameSite: 'lax' | 'strict' | 'none';
  path: string;
  httpOnly: boolean;
}

// Error types
export class SessionError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'SessionError';
  }
}

export class SessionValidationError extends SessionError {
  constructor(message: string) {
    super(message);
    this.name = 'SessionValidationError';
  }
}

// Constants
const TEMP_SESSION_MAX_AGE = 60 * 15; // 15 minutes
const MAIN_SESSION_MAX_AGE = 60 * 60 * 24 * 5; // 5 days

// Base session configuration
const baseConfig: Partial<SessionConfig> = {
  secure: process.env.NODE_ENV === 'production',
  secrets: [sessionSecret],
  sameSite: 'lax',
  path: '/',
  httpOnly: true,
};

// Main session storage
const storage = createCookieSessionStorage({
  cookie: {
    name: '__session',
    ...baseConfig,
    maxAge: MAIN_SESSION_MAX_AGE,
  },
});

// Temporary session storage for email verification
const tempStorage = createCookieSessionStorage({
  cookie: {
    name: '__temp_session',
    ...baseConfig,
    maxAge: TEMP_SESSION_MAX_AGE,
  },
});

// Main session exports
export const { getSession, commitSession, destroySession } = storage;

// Temporary session exports
export const {
  getSession: getTempSession,
  commitSession: commitTempSession,
  destroySession: destroyTempSession,
} = tempStorage;

// Helper function to validate session data
function validateSessionData(session: Session): boolean {
  if (!session) return false;
  
  const created = session.get('created');
  if (!created) return false;

  const now = Date.now();
  const sessionAge = now - created;

  // Check if session has expired
  if (sessionAge > TEMP_SESSION_MAX_AGE * 1000) {
    return false;
  }

  return true;
}

// Validate temporary session
export async function validateTempSession(session: Session): Promise<void> {
  if (!validateSessionData(session)) {
    throw new SessionValidationError('Session has expired');
  }

  const email = session.get('tempEmail');
  if (!email) {
    throw new SessionValidationError('No email found in session');
  }

  // Validate email format
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailRegex.test(email)) {
    throw new SessionValidationError('Invalid email format in session');
  }
}

// Helper function to create temporary email session
export async function createTempEmailSession(email: string): Promise<string> {
  const session = await getTempSession();
  
  // Validate email
  if (!email || typeof email !== 'string') {
    throw new SessionValidationError('Invalid email provided');
  }

  // Set session data
  session.set('tempEmail', email);
  session.set('created', Date.now());

  // Set flash message for next request
  session.flash('message', 'Check your email for the verification link');

  return commitTempSession(session, {
    expires: new Date(Date.now() + TEMP_SESSION_MAX_AGE * 1000)
  });
}

// Helper function to get temporary email from session
export async function getTempEmailFromSession(request: Request): Promise<string | null> {
  const session = await getTempSession(request.headers.get('Cookie'));
  
  try {
    await validateTempSession(session);
    return session.get('tempEmail');
  } catch (error) {
    if (error instanceof SessionValidationError) {
      return null;
    }
    throw error;
  }
}

// Helper function to get flash messages
export async function getFlashMessage(request: Request): Promise<string | null> {
  const session = await getTempSession(request.headers.get('Cookie'));
  return session.get('message') || null;
}

// Helper function to create main session
export async function createMainSession(sessionData: SessionData): Promise<string> {
  const session = await getSession();
  
  // Set session data
  Object.entries(sessionData).forEach(([key, value]) => {
    session.set(key, value);
  });
  
  // Set creation timestamp
  session.set('created', Date.now());

  return commitSession(session, {
    expires: new Date(Date.now() + MAIN_SESSION_MAX_AGE * 1000),
  });
}

// Helper function to validate main session
export async function validateMainSession(request: Request): Promise<Session> {
  const session = await getSession(request.headers.get('Cookie'));
  
  if (!validateSessionData(session)) {
    throw new SessionValidationError('Main session has expired');
  }

  return session;
}