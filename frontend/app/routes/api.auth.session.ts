import { adminAuth } from "~/utils/firebase-admin.server";
import { getSession, commitSession } from "~/utils/session.server";

const EXPIRES_IN = 60 * 60 * 24 * 5 * 1000; // 5 days

export async function action({ request }: { request: Request }) {
  if (request.method !== 'POST') {
    return new Response(
      JSON.stringify({ message: 'Method not allowed' }), 
      { 
        status: 405,
        headers: {
          'Content-Type': 'application/json',
        },
      }
    );
  }

  try {
    const { idToken } = await request.json();
    const decodedToken = await adminAuth.verifyIdToken(idToken);
    const expiresIn = EXPIRES_IN;

    // Create session cookie
    const sessionCookie = await adminAuth.createSessionCookie(idToken, {
      expiresIn,
    });

    // Get session and set user data
    const session = await getSession();
    session.set("userId", decodedToken.uid);
    session.set("email", decodedToken.email);

    return new Response(
      JSON.stringify({ success: true }),
      {
        headers: {
          'Content-Type': 'application/json',
          "Set-Cookie": await commitSession(session, {
            expires: new Date(Date.now() + expiresIn),
          }),
        },
      }
    );
  } catch (error) {
    console.error("Session creation error:", error);
    return new Response(
      JSON.stringify({ success: false, message: "Invalid ID token" }),
      {
        status: 401,
        headers: {
          'Content-Type': 'application/json',
        },
      }
    );
  }
}