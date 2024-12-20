import { redirect } from "@remix-run/node";
import { adminAuth } from "~/utils/firebase-admin.server";
import { destroySession, getSession } from "~/utils/session.server";

export async function action({ request }: { request: Request }) {
  if (request.method !== 'POST') {
    return new Response('Method not allowed', { status: 405 });
  }

  try {
    // Get the current session
    const session = await getSession(request.headers.get("Cookie"));
    
    // Revoke Firebase session if it exists
    const sessionCookie = request.headers.get("Cookie") || '';
    if (sessionCookie) {
      try {
        const decodedClaims = await adminAuth.verifySessionCookie(sessionCookie);
        await adminAuth.revokeRefreshTokens(decodedClaims.sub);
      } catch (e) {
        // Continue even if session verification fails
        console.log("Session verification failed during sign out:", e);
      }
    }

    // Clear the session cookie
    return redirect("/auth/sign-in", {
      headers: {
        "Set-Cookie": await destroySession(session)
      }
    });
  } catch (error) {
    console.error("Sign out error:", error);
    return redirect("/auth/sign-in");
  }
} 