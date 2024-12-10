import { Card, Text, Button, Group } from '@mantine/core';
import { useSignIn } from '~/hooks';
import { EmailForm } from '~/components/auth/email-form';
import { Logo, HeaderContent } from '~/components/auth/common';
import { useThemeColor } from '~/utils/theme';
import { useOutletContext, Link } from '@remix-run/react';

export default function SignIn() {
  const {
    status,
    errorMessage,
    handleSignIn
  } = useSignIn({
    onError: (error) => {
      console.error('Sign in error:', error);
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

  return (
    <div className="w-full max-w-2xl mx-auto p-6">
      <Logo />
      <HeaderContent 
        title="Welcome Back"
        subtitle="Sign in to your account"
        headingColor={headingTextColor}
        textColor={bodyTextColor}
      />
      
      <Card shadow="md" p="xl" className="w-full mb-8" bg="dark.0">
        <EmailForm
          onSubmit={handleSignIn}
          status={status}
          errorMessage={errorMessage}
          successMessage={status === 'success' ? 
            "We've sent you an email with a sign-in link. Click the link to continue." : 
            undefined}
        />
        
        <div className="mt-4 text-center">
          <Text size="sm" c="dimmed" mb="md">
            Don't have an account?
          </Text>
          <Button 
            variant="subtle" 
            size="sm" 
            component={Link} 
            to="/auth/sign-up"
          >
            Create an Account
          </Button>
        </div>
      </Card>
    </div>
  );
}