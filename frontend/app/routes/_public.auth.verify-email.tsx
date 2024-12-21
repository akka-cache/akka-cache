import { redirect, type LoaderFunctionArgs } from "@remix-run/node";
import { useLoaderData } from "@remix-run/react";
import { isSignInWithEmailLink, signInWithEmailLink } from "firebase/auth";
import { auth } from "~/utils/firebase-config";
import { 
  getTempEmailFromSession, 
  getTempSession,
  destroyTempSession, 
  getSession, 
  commitSession,
  validateTempSession 
} from "~/utils/session.server";
import { adminAuth } from "~/utils/firebase-admin.server";
import { Card, Loader, Text } from '@mantine/core';
import { useThemeColor } from '~/utils/theme';
import { getAuth } from "firebase/auth";

interface LoaderData {
  error?: string;
}

const SESSION_EXPIRY = 60 * 60 * 24 * 5 * 1000; // 5 days

export async function loader({ request }: LoaderFunctionArgs) {
  console.log("Verify Email Loader - Starting");
  console.log("Request URL:", request.url);
  
  // Use the configured auth instance instead of getting a new one
  if (auth.currentUser) {
    await auth.signOut();
  }

  // Validate that this is a legitimate email verification link
  const isValidLink = isSignInWithEmailLink(auth, request.url);
  console.log("Is valid email link?", isValidLink);
  
  if (!isValidLink) {
    console.log("Not a valid email verification link");
    console.log("URL components:", new URL(request.url));
    return redirect("/auth/sign-in?error=invalid-link");
  }

  try {
    // Get and validate temporary session
    const tempSession = await getTempSession(request.headers.get("Cookie"));
    
    try {
      await validateTempSession(tempSession);
    } catch (error) {
      console.error("Temp session validation failed:", error);
      return redirect("/auth/sign-in?error=session-expired");
    }

    const email = await getTempEmailFromSession(request);
    console.log("Retrieved email from temp session:", email);

    if (!email) {
      console.log("No email found in temp session");
      return redirect("/auth/sign-in?error=no-email");
    }

    // Verify user exists and is verified in Firebase
    try {
      const userRecord = await adminAuth.getUserByEmail(email);
      
      if (!userRecord.emailVerified) {
        console.log("User email not verified in Firebase");
        return redirect("/auth/sign-in?error=email-not-verified");
      }

      if (userRecord.disabled) {
        console.log("User account is disabled");
        return redirect("/auth/sign-in?error=account-disabled");
      }
    } catch (error: any) {
      if (error.code === 'auth/user-not-found') {
        return redirect("/auth/sign-in?error=user-not-found");
      }
      throw error;
    }

    // Attempt to sign in with email link
    try {
      const result = await signInWithEmailLink(auth, email, request.url);
      console.log("Sign in successful, user:", result.user.email);

      // Verify the signed-in user matches the expected email
      if (result.user.email !== email) {
        console.error("Email mismatch:", { expected: email, got: result.user.email });
        throw new Error("Email mismatch");
      }

      const idToken = await result.user.getIdToken();
      console.log("Got ID token");

      // Set up headers for cookie management
      const headers = new Headers();

      // Clear temporary session
      headers.append("Set-Cookie", await destroyTempSession(tempSession));

      // Create the actual session with appropriate security settings
      const sessionCookie = await adminAuth.createSessionCookie(idToken, { 
        expiresIn: SESSION_EXPIRY 
      });

      // Additional validation of the session cookie
      try {
        await adminAuth.verifySessionCookie(sessionCookie);
      } catch (error) {
        console.error("Session cookie verification failed:", error);
        throw new Error("Invalid session cookie");
      }
      
      // Set up the new session
      const session = await getSession();
      session.set("session", sessionCookie);
      
      // Set secure cookie options
      headers.append("Set-Cookie", await commitSession(session, {
        expires: new Date(Date.now() + SESSION_EXPIRY),
        httpOnly: true,
        secure: process.env.NODE_ENV === "production",
        sameSite: "lax"
      }));
      
      console.log("Created session cookie");

      // Redirect to home with success parameter
      return redirect("/?login=success", { headers });
    } catch (error: any) {
      console.error("Sign in with email link failed:", error);
      
      if (error.code === 'auth/invalid-action-code') {
        return redirect("/auth/sign-in?error=invalid-code");
      }
      if (error.code === 'auth/expired-action-code') {
        return redirect("/auth/sign-in?error=expired-code");
      }
      throw error;
    }
  } catch (error: any) {
    console.error("Verify Email Error:", error);
    return Response.json(
      { error: "An unexpected error occurred. Please try signing in again." },
      { status: 500 }
    );
  }
}

export default function VerifyEmail() {
  const bodyTextColor = useThemeColor('bodyText');
  const loaderData = useLoaderData<LoaderData>();

  // If there's an error, show it
  if (loaderData?.error) {
    return (
      <div className="w-full max-w-2xl mx-auto p-6">
        <Card shadow="md" p="xl" className="w-full mb-8" bg="dark.0">
          <div className="text-center">
            <Text c="red" className="mb-4">{loaderData.error}</Text>
            <Text c={bodyTextColor}>
              Please try <a href="/auth/sign-in" className="text-blue-500">signing in</a> again.
            </Text>
          </div>
        </Card>
      </div>
    );
  }

  // Show loading state
  return (
    <div className="w-full max-w-2xl mx-auto p-6">
      <Card shadow="md" p="xl" className="w-full mb-8" bg="dark.0">
        <div className="text-center">
          <Loader size="lg" className="mb-4" />
          <Text c={bodyTextColor}>Verifying your email...</Text>
        </div>
      </Card>
    </div>
  );
}