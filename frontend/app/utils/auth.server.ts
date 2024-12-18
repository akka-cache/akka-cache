import { adminAuth } from '~/utils/firebase-admin.server';
import { redirect } from '@remix-run/node';

export async function requireAuth(request: Request) {
  try {
    const session = await adminAuth.verifySessionCookie(
      request.headers.get('Cookie') || ''
    );

    if (!session) {
      throw redirect('/auth/sign-in');
    }

    return session;
  } catch (error) {
    throw redirect('/auth/sign-in');
  }
}