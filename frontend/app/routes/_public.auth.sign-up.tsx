import { useState } from 'react';
import { Card, Checkbox, Text } from '@mantine/core';
import { useUnifiedAuth } from '~/hooks';
import { SignUpForm } from '~/components/auth/sign-up-form';
import { Logo, HeaderContent } from '~/components/auth/common';
import { useThemeColor } from '~/utils/theme';
import { useOutletContext, useLocation, Navigate } from '@remix-run/react';
import type { AuthStatus } from '~/types/auth';

export default function SignUp() {
  const [acceptedTerms, setAcceptedTerms] = useState(false);
  const [localErrorMessage, setLocalErrorMessage] = useState('');
  const location = useLocation();
  const email = location.state?.email;

  // Redirect if no email provided
  if (!email) {
    return <Navigate to="/auth/sign-in" replace />;
  }

  const {
    status,
    errorMessage: authErrorMessage,
    handleSignUpSubmit
  } = useUnifiedAuth({
    redirectUrl: '/',
    onSuccess: () => {
      // Optional: Add any success handling here
      setLocalErrorMessage('');
    },
    onError: (error) => {
      setLocalErrorMessage(error.message);
    }
  });

  const headingTextColor = useThemeColor('headingText');
  const bodyTextColor = useThemeColor('bodyText');
  const context = useOutletContext<string>();

  const handleSubmit = async (userData: {
    displayName: string;
    organization: string;
    mobileNumber?: string;
  }) => {
    if (!acceptedTerms) {
      setLocalErrorMessage('Please accept the terms and conditions');
      return;
    }
    
    setLocalErrorMessage('');
    try {
      // Combine the email with the user data
      await handleSignUpSubmit({
        ...userData,
        email
      });
    } catch (error: any) {
      setLocalErrorMessage(error.message);
    }
  };

  const displayErrorMessage = localErrorMessage || authErrorMessage;

  if (context === 'right') {
    return (
      <div>
        <Text size="xl" c={bodyTextColor}>More content goes here</Text>
      </div>
    );
  }

  return (
    <div className="w-full max-w-2xl mx-auto p-6">
      <Logo />
      <HeaderContent 
        title="Complete Your Profile"
        subtitle="Tell us a bit about yourself"
        headingColor={headingTextColor}
        textColor={bodyTextColor}
      />
      
      <Card shadow="md" p="xl" className="w-full mb-8 hl" bg="dark.0">
        <div className="space-y-4">
          <SignUpForm
            email={email}
            onSubmit={handleSubmit}
            status={status}
            errorMessage={displayErrorMessage}
            successMessage="Check your email to complete your account creation!"
          />
          
          <Checkbox
            label="I accept the terms of service and privacy policy"
            checked={acceptedTerms}
            onChange={(e) => setAcceptedTerms(e.currentTarget.checked)}
            classNames={{
              label: 'text-gray-100'
            }}
          />
        </div>
      </Card>
    </div>
  );
}