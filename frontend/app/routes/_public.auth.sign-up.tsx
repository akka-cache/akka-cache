import { useState } from 'react';
import { Card, Text } from '@mantine/core';
import { SignUpForm } from '~/components/auth/sign-up-form';
import { Logo, HeaderContent, getAppOrigin } from '~/components/auth/common';
import { useThemeColor } from '~/utils/theme';
import { useOutletContext, useActionData, useNavigation } from '@remix-run/react';
import type { ActionFunction } from "@remix-run/node";
import { adminAuth } from "~/utils/firebase-admin.server";
import { createTempEmailSession } from "~/utils/session.server";
import type { UserData } from '~/types/auth';
import { getAuth, sendEmailVerification, signInWithCustomToken } from "firebase/auth";
import { nanoid } from 'nanoid';
import { Link } from '@remix-run/react';
import { useMantineColorScheme } from '@mantine/core';

interface FirebaseAuthError extends Error {
  errorInfo?: {
    code: string;
    message: string;
  };
  code?: string;
}

function getVerificationURL(request: Request) {
  return `${getAppOrigin(request)}/auth/verify-email`;
}

export const action: ActionFunction = async ({ request }) => {
  console.log("🚀 Sign-up action started");
  
  const formData = await request.formData();
  const email = formData.get("email") as string;
  const displayName = formData.get("displayName") as string;
  const acceptedTerms = formData.get("acceptedTerms") === "true";

  console.log("Processing sign-up for:", email);

  // Add server-side email validation
  if (!email.toLowerCase().endsWith('@akka.io')) {
    return Response.json(
      { error: "Only @akka.io email addresses are allowed to sign up" },
      { status: 403 }
    );
  }

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

    // Determine service tier based on email
    const serviceLevel = email.includes('+gatling@akka.io') ? 'gatling' : 'free';

    // Set custom claims
    const now = new Date().toISOString();
    const customClaims = {
      org: nanoid(8),
      orgName: "not set",
      createdAt: now,
      updatedAt: now,
      serviceLevel: serviceLevel,
      consentScope: "Terms of Service and Privacy Policy",
      consentDate: now
    };

    // Set the custom claims for the user
    await adminAuth.setCustomUserClaims(userRecord.uid, customClaims);
    console.log("Custom claims set for user:", userRecord.uid);

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
  const navigation = useNavigation();
  
  const headingTextColor = useThemeColor('headingText');
  const bodyTextColor = useThemeColor('bodyText');
  const context = useOutletContext<string>();
  const { colorScheme } = useMantineColorScheme();

  if (context === 'right') {
    return (
      <div className="p-6">
        <Text 
          className="mb-4 leading-tight"
          style={{
            fontSize: 'var(--font-size-h1)',
            lineHeight: 'var(--line-height-h1)',
            fontWeight: 'var(--font-weight-h1)'
          }}
          c={headingTextColor}
        >
          Try AkkaCache for free
        </Text>
        <Text 
          className="leading-relaxed max-w-[600px] mb-8"
          style={{
            fontSize: 'var(--font-size-subtitle)',
            lineHeight: 'var(--line-height-subtitle)',
            fontWeight: 'var(--font-weight-subtitle)'
          }}
          c={bodyTextColor}
        >
          An accelerator for key-value data with multi-region replication that does not sacrifice performance as traffic increases.
        </Text>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Left Column */}
          <div className="space-y-6">
            <div>
              <Text fw={700} className="mb-2 hl">Performance</Text>
              <Text c={bodyTextColor}>TBD</Text>
            </div>
            
            <div>
              <Text fw={700} className="mb-2 hl">Multi-language Clients</Text>
              <Text c={bodyTextColor}>
                RESTful interface, Typescript SDK, and Java client library. OpenAI specification to generate additional language clients.
              </Text>
            </div>
            
            <div>
              <Text fw={700} className="mb-2 hl">Batch Operations</Text>
              <Text c={bodyTextColor}>
                Group multiple keys into a common namespace. Execute group insert, get, and delete operations with a single command. Grab all keys in a namespace.
              </Text>
            </div>
            
            <div>
              <Text fw={700} className="mb-2 hl">Guaranteed</Text>
              <Text c={bodyTextColor}>Insert our trust center icons / language?</Text>
            </div>
          </div>

          {/* Right Column */}
          <div className="space-y-6">
            <div>
              <Text fw={700} className="mb-2 hl">Multi-region</Text>
              <Text c={bodyTextColor}>
                Pin data to a single region or replicate across many. Read from any region. Updates routed to originating region.
              </Text>
            </div>
            
            <div>
              <Text fw={700} className="mb-2 hl">99.9999% Availability</Text>
              <Text c={bodyTextColor}>
                Akka resilience guarantee and multi-region availability offers 10ms RTO and virtually unbreakable availability.
              </Text>
            </div>
            
            <div>
              <Text fw={700} className="mb-2 hl">Large Packets</Text>
              <Text c={bodyTextColor}>
                8MB per request data size. Automatic chunking for performance. octet-stream and json mimetypes.
              </Text>
            </div>
          </div>
        </div>
      </div>
    );
  }

  const isSubmitting = navigation.state === 'submitting';
  
  const status = isSubmitting ? 'loading' :
                actionData?.success ? 'success' : 
                actionData?.error || localErrorMessage ? 'error' : 
                'idle';

  const handleSubmit = async (formData: {
    email: string;
    displayName: string;
    acceptedTerms: boolean;
  }) => {
    // Reset any existing error message
    setLocalErrorMessage('');

    // Early validation for terms
    if (!formData.acceptedTerms) {
      setLocalErrorMessage('Please accept the terms and conditions');
      return false; // Prevent form submission
    }

    // Early validation for email domain
    if (!formData.email.toLowerCase().endsWith('@akka.io')) {
      setLocalErrorMessage('Sign up are currently disabled. Please try again after January 8th, 2025.');
      return false; // Prevent form submission
    }

    return true; // Allow form submission
  };

  return (
    <div className="w-full max-w-2xl mx-auto p-6">
      <Logo 
        logoUrl={colorScheme === 'dark' ? '/logo-dark.svg' : '/logo-light.svg'} 
        alt="Logo" 
      />
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
        <div className="text-center mt-4">
          <Text size="sm" c={bodyTextColor}>
            Already have an account?{' '}
            <Link to="/auth/sign-in" className="text-blue-400 hover:underline">
              Sign in
            </Link>
          </Text>
        </div>
      </Card>
    </div>
  );
}