import { useState } from 'react';
import { Card, Checkbox, Text, Button } from '@mantine/core';
import { useUnifiedAuth } from '~/hooks';
import { SignUpForm } from '~/components/auth/sign-up-form';
import { Logo, HeaderContent } from '~/components/auth/common';
import { useThemeColor } from '~/utils/theme';
import { useOutletContext, useLocation, Navigate, Link } from '@remix-run/react';
import type { AuthStatus } from '~/types/auth';

export default function SignUp() {
  const [localErrorMessage, setLocalErrorMessage] = useState('');

  const {
    status,
    errorMessage: authErrorMessage,
    handleSignUp
  } = useUnifiedAuth({
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

  const handleSubmit = async (userData: {
    email: string;
    displayName: string;
    organization: string;
    mobileNumber?: string;
    acceptedTerms: boolean;
  }) => {
    if (!userData.acceptedTerms) {
      setLocalErrorMessage('Please accept the terms and conditions');
      return;
    }
    
    setLocalErrorMessage('');
    try {
      await handleSignUp({
        email: userData.email,
        displayName: userData.displayName,
        organization: userData.organization,
        mobileNumber: userData.mobileNumber
      });
    } catch (error: any) {
      setLocalErrorMessage(error.message);
    }
  };

  return (
    <div className="w-full max-w-2xl mx-auto p-6">
      <Logo />
      <HeaderContent 
        title="Create Account"
        subtitle="Sign up for a new account"
        headingColor={headingTextColor}
        textColor={bodyTextColor}
      />
      
      <Card shadow="md" p="xl" className="w-full mb-8" bg="dark.0">
        <SignUpForm
          onSubmit={handleSubmit}
          status={status}
          errorMessage={localErrorMessage || authErrorMessage}
          successMessage={status === 'success' ? 
            "We've sent you a verification email. Please check your inbox." : 
            undefined}
        />
        
        <div className="mt-4 text-center">
          <Text size="sm" c="dimmed" mb="md">
            Already have an account?
          </Text>
          <Button 
            variant="subtle" 
            size="sm" 
            component={Link} 
            to="/auth/sign-in"
          >
            Sign In
          </Button>
        </div>
      </Card>
    </div>
  );
}