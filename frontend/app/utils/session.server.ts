import { createCookieSessionStorage } from '@remix-run/node';

const sessionSecret = process.env.SESSION_SECRET || 'default_secret';

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

export const { getSession, commitSession, destroySession } = storage; 