// ~/app/routes/_public.auth.sign-up.tsx
import { useState } from 'react';
import { Card, Text } from '@mantine/core';
import { SignUpForm } from '~/components/auth/sign-up-form';
import { Logo, HeaderContent } from '~/components/auth/common';
import { useThemeColor } from '~/utils/theme';
import { useOutletContext, useActionData } from '@remix-run/react';
import type { ActionFunction } from "@remix-run/node";
import { adminAuth } from "~/utils/firebase-admin.server";
import { createTempEmailSession } from "~/utils/session.server";
import type { UserData } from '~/types/auth';
import { getAuth, sendEmailVerification, signInWithCustomToken } from "firebase/auth";

interface FirebaseAuthError extends Error {
  errorInfo?: {
    code: string;
    message: string;
  };
  code?: string;
}

function getVerificationURL(request: Request) {
  const origin = process.env.NODE_ENV === 'production' 
    ? process.env.PUBLIC_URL 
    : 'http://localhost:5173';
    
  return `${origin}/auth/verify-email`;
}

export const action: ActionFunction = async ({ request }) => {
  console.log("🚀 Sign-up action started");
  
  const formData = await request.formData();
  const email = formData.get("email") as string;
  const displayName = formData.get("displayName") as string;
  const acceptedTerms = formData.get("acceptedTerms") === "true";

  console.log("Processing sign-up for:", email);

  if (!email || !displayName) {
    return Response.json(
      { error: "Email and display name are required" },
      { status: 400 }
    );
  }

  if (!acceptedTerms) {
    return Response.json(
      { error: "Please accept the terms and conditions" },
      { status: 400 }
    );
  }

  try {
    // Create the user
    const userRecord = await adminAuth.createUser({
      email,
      displayName,
      emailVerified: false
    });

    console.log("User created:", userRecord.uid);

    // Generate a custom token for the new user
    const customToken = await adminAuth.createCustomToken(userRecord.uid);

    // Set up verification using the client SDK configuration
    const actionCodeSettings = {
      url: getVerificationURL(request),
      handleCodeInApp: true
    };

    // Sign in with custom token and send verification email
    const auth = getAuth();
    const userCredential = await signInWithCustomToken(auth, customToken);
    await sendEmailVerification(userCredential.user, actionCodeSettings);

    console.log("Verification email sent to:", email);

    // Create temporary session
    const headers = new Headers();
    headers.append(
      "Set-Cookie",
      await createTempEmailSession(email)
    );

    return Response.json(
      { success: true },
      { 
        headers,
        status: 200
      }
    );

  } catch (error: any) {
    console.error("Sign up error:", error);
    
    if (error.code === 'auth/email-already-exists') {
      return Response.json(
        { error: "An account with this email already exists" },
        { status: 400 }
      );
    }

    const firebaseError = error as FirebaseAuthError;
    if (firebaseError.errorInfo?.message?.includes('TOO_MANY_ATTEMPTS_TRY_LATER')) {
      return Response.json(
        { error: "Too many attempts. Please try again in a few minutes." },
        { status: 429 }
      );
    }

    return Response.json(
      { error: "Failed to create account. Please try again." },
      { status: 500 }
    );
  }
};

export default function SignUp() {
  const [localErrorMessage, setLocalErrorMessage] = useState('');
  const actionData = useActionData<typeof action>();
  
  const headingTextColor = useThemeColor('headingText');
  const bodyTextColor = useThemeColor('bodyText');
  const context = useOutletContext<string>();

  if (context === 'right') {
    return (
      <div className="p-6">
        {/* Product features content remains unchanged */}
      </div>
    );
  }

  const status = actionData?.success ? 'success' : 
                 actionData?.error || localErrorMessage ? 'error' : 
                 'idle';

  const handleSubmit = async (formData: {
    email: string;
    displayName: string;
    acceptedTerms: boolean;
  }) => {
    if (!formData.acceptedTerms) {
      setLocalErrorMessage('Please accept the terms and conditions');
      return;
    }
    setLocalErrorMessage('');
  };

  return (
    <div className="w-full max-w-2xl mx-auto p-6">
      <Logo />
      <HeaderContent 
        title="Create an Account"
        subtitle="Sign up to get started"
        headingColor={headingTextColor}
        textColor={bodyTextColor}
      />
      
      <Card shadow="md" p="xl" className="w-full mb-8" bg="dark.0">
        <SignUpForm
          onSubmit={handleSubmit}
          status={status}
          errorMessage={localErrorMessage || actionData?.error}
          successMessage={status === 'success' ? 
            "We've sent you an email with a verification link. Click the link to complete your registration." : 
            undefined}
        />
      </Card>
    </div>
  );
}