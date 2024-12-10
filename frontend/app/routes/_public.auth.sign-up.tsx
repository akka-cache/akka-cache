import { useState } from 'react';
import { Card, Text } from '@mantine/core';
import { useSignupAuth } from '~/hooks/use-signup-auth';
import { SignUpForm } from '~/components/auth/sign-up-form';
import { Logo, HeaderContent } from '~/components/auth/common';
import { useThemeColor } from '~/utils/theme';
import { useOutletContext } from '@remix-run/react';
import type { UserData } from '~/types/auth';

export default function SignUp() {
  const [localErrorMessage, setLocalErrorMessage] = useState('');
  
  const {
    status,
    errorMessage: authErrorMessage,
    sendSignUpLink
  } = useSignupAuth({
    redirectUrl: '/auth/verify-email',
    onError: (error) => {
      setLocalErrorMessage(error.message);
    }
  });

  const headingTextColor = useThemeColor('headingText');
  const bodyTextColor = useThemeColor('bodyText');
  const context = useOutletContext<string>();

  if (context === 'right') {
    return (
      <div>
        <Text size="xl" c={bodyTextColor}>More content goes here</Text>
      </div>
    );
  }

  const handleSubmit = async (formData: {
    email: string;
    displayName: string;
    organization: string;
    mobileNumber?: string;
    acceptedTerms: boolean;
  }) => {
    if (!formData.acceptedTerms) {
      setLocalErrorMessage('Please accept the terms and conditions');
      return;
    }
    
    setLocalErrorMessage('');
    const userData: UserData = {
      email: formData.email,
      displayName: formData.displayName,
      organization: formData.organization,
      mobileNumber: formData.mobileNumber
    };

    try {
      await sendSignUpLink(userData);
    } catch (error: any) {
      setLocalErrorMessage(error.message);
    }
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
          errorMessage={localErrorMessage || authErrorMessage}
          successMessage={status === 'success' ? 
            "We've sent you an email with a verification link. Click the link to complete your registration." : 
            undefined}
        />
      </Card>
    </div>
  );
}