import type { ActionFunction } from "@remix-run/node";
import { useActionData } from "@remix-run/react";
import { Card, Text, useMantineColorScheme } from '@mantine/core';
import { sendSignInLinkToEmail } from 'firebase/auth';
import { auth } from '~/utils/firebase-config';
import { createTempEmailSession } from '~/utils/session.server';
import { EmailForm } from '~/components/auth/email-form';
import { Logo, HeaderContent, getAppOrigin } from '~/components/auth/common';
import { useThemeColor } from '~/utils/theme';
import { useOutletContext } from '@remix-run/react';
import { Link } from '@remix-run/react';

type ActionData = {
  success?: boolean;
  message?: string;
  error?: string;
};

export const action: ActionFunction = async ({ request }) => {
  console.log('Starting sign-in action');
  const formData = await request.formData();
  const email = formData.get("email") as string;

  if (!email) {
    console.log('No email provided');
    return Response.json(
      { error: "Email is required" }, 
      { status: 400 }
    );
  }

  try {
    console.log('Creating temp session for email:', email);
    const headers = new Headers();
    headers.append("Set-Cookie", await createTempEmailSession(email));

    const origin = getAppOrigin(request);
    const actionCodeSettings = {
      url: `${origin}/auth/verify-email`,
      handleCodeInApp: true,
    };

    console.log('Sending sign-in link with settings:', actionCodeSettings);

    await sendSignInLinkToEmail(auth, email, actionCodeSettings);
    console.log('Sign-in link sent successfully');

    return Response.json(
      { success: true, message: "Check your email for the sign-in link" },
      { headers }
    );
  } catch (error: any) {
    console.error("Sign-in error details:", {
      name: error.name,
      code: error.code,
      message: error.message,
      stack: error.stack,
      cause: error.cause
    });

    // Return a more specific error message based on the Firebase error code
    let errorMessage = "Failed to send sign-in link. Please try again.";
    if (error.code === 'auth/invalid-email') {
      errorMessage = "Invalid email address.";
    } else if (error.code === 'auth/operation-not-allowed') {
      errorMessage = "Email link sign-in is not enabled. Please contact support.";
    } else if (error.code === 'auth/missing-continue-uri') {
      errorMessage = "Configuration error: Missing continue URL.";
    } else if (error.code === 'auth/invalid-continue-uri') {
      errorMessage = "Configuration error: Invalid continue URL.";
    }

    return Response.json(
      { error: `${errorMessage} (${error.code})` },
      { status: 500 }
    );
  }
}

export default function SignIn() {
  const actionData = useActionData<typeof action>();
  const headingTextColor = useThemeColor('headingText');
  const bodyTextColor = useThemeColor('bodyText');
  const context = useOutletContext<string>();
  const { colorScheme } = useMantineColorScheme();

  const formStatus = actionData?.success ? 'success' 
    : actionData?.error ? 'error' 
    : 'idle';

  if (context === 'right') {
    return (
      <div>
        <Text size="xl" c={bodyTextColor}>More content goes here</Text>
      </div>
    );
  }

  return (
    <div className="w-full max-w-2xl mx-auto p-6">
      <Logo 
        logoUrl={colorScheme === 'dark' ? '/logo-dark.svg' : '/logo-light.svg'} 
        alt="Logo" 
      />
      <HeaderContent 
        title="Welcome Back"
        subtitle="Sign in to your account"
        headingColor={headingTextColor}
        textColor={bodyTextColor}
      />
      
      <Card shadow="md" p="xl" className="w-full mb-8" bg="dark.0">
        <EmailForm
          status={formStatus}
          errorMessage={actionData?.error}
          successMessage={actionData?.message}
        />
        <div className="text-center mt-4">
          <Text size="sm" c={bodyTextColor}>
            Don't have an account?{' '}
            <Link to="/auth/sign-up" className="text-blue-400 hover:underline">
              Sign up
            </Link>
          </Text>
        </div>
      </Card>
    </div>
  );
}