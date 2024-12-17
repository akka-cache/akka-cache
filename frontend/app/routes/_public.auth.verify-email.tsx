import { redirect, type LoaderFunctionArgs } from "@remix-run/node";
import { isSignInWithEmailLink, signInWithEmailLink } from "firebase/auth";
import { auth } from "~/utils/firebase-config";
import { 
  getTempEmailFromSession, 
  getTempSession,
  destroyTempSession, 
  getSession, 
  commitSession 
} from "~/utils/session.server";
import { adminAuth } from "~/utils/firebase-admin.server";
import { Card, Loader, Text } from '@mantine/core';
import { useThemeColor } from '~/utils/theme';

export async function loader({ request }: LoaderFunctionArgs) {
  console.log("Verify Email Loader - Starting");

  if (!isSignInWithEmailLink(auth, request.url)) {
    console.log("Not an email verification link");
    return redirect("/auth/sign-in");
  }

  try {
    const email = await getTempEmailFromSession(request);
    console.log("Retrieved email from temp session:", email);

    if (!email) {
      console.log("No email found in temp session");
      return redirect("/auth/sign-in");
    }

    const result = await signInWithEmailLink(auth, email, request.url);
    console.log("Sign in successful, user:", result.user.email);

    const idToken = await result.user.getIdToken();
    console.log("Got ID token");

    // Clear temporary session
    const headers = new Headers();
    const tempSession = await getTempSession(request.headers.get("Cookie"));
    headers.append("Set-Cookie", await destroyTempSession(tempSession));

    // Create the actual session
    const expiresIn = 60 * 60 * 24 * 5 * 1000; // 5 days
    const sessionCookie = await adminAuth.createSessionCookie(idToken, { expiresIn });
    
    // Get a new session and set the cookie
    const session = await getSession();
    session.set("session", sessionCookie);
    headers.append("Set-Cookie", await commitSession(session));
    
    console.log("Created session cookie");

    return redirect("/", {
      headers,
    });
  } catch (error) {
    console.error("Verify Email Error:", error);
    return redirect("/auth/sign-in");
  }
}

export default function VerifyEmail() {
  const bodyTextColor = useThemeColor('bodyText');

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