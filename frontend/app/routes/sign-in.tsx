import { useState, useEffect } from 'react';
import { TextInput, Button, Card, Title, Text, Divider } from '@mantine/core';
import { Link } from '@remix-run/react';
import { useThemeColor } from '~/utils/theme';

declare global {
  interface Window {
    handleGoogleSignIn: (response: GoogleSignInResponse) => Promise<void>;
  }
}

// Add this type definition
interface GoogleSignInResponse {
  credential: string;
  select_by: string;
}

const Logo = () => (
  <Link to="/" className="inline-block mb-12">
    <span className="text-2xl font-bold text-[var(--color-river-blue)]">
      Akka Cache
    </span>
  </Link>
);

const HeaderContent = ({ headingColor, textColor }: { headingColor: string; textColor: string }) => (
  <div className="space-y-3 mb-12">
    <Title order={1} c={headingColor}>
      Enterprise-Grade Serverless Caching
    </Title>
    <Text size="lg" c={textColor}>
      Sign in to continue to Akka Cache
    </Text>
  </div>
);

const SignInForm = ({ mutedTextColor }: { mutedTextColor: string }) => {
  // Initialize Google Sign In
  useEffect(() => {
    // Load Google Sign In script
    const script = document.createElement('script');
    script.src = 'https://accounts.google.com/gsi/client';
    script.async = true;
    script.defer = true;
    document.body.appendChild(script);

    return () => {
      document.body.removeChild(script);
    };
  }, []);

  return (
    <Card 
      shadow="md" 
      p="xl" 
      className="w-full mb-8 hl"
      bg="dark.0"
    >
      <div className="space-y-4">
        <TextInput
          label="Email"
          placeholder="Enter your email"
          size="md"
        />
        
        <Button fullWidth size="md">
          Continue with email
        </Button>

        <Divider label="OR" labelPosition="center" />

        {/* Google Sign In Button */}
        <div 
          id="g_id_onload"
          data-client_id="YOUR_GOOGLE_CLIENT_ID"
          data-context="signin"
          data-ux_mode="popup"
          data-callback="handleGoogleSignIn"
          data-auto_select="false"
          data-itp_support="true"
        />
        
        <div 
          className="g_id_signin"
          data-type="standard"
          data-shape="rectangular"
          data-theme="outline"
          data-text="signin_with"
          data-size="large"
          data-logo_alignment="left"
          data-width="100%"
        />

        <Text size="xs" c={mutedTextColor} ta="center">
          By continuing, you agree to Akka's{' '}
          <Link to="/terms" className="text-[var(--color-river-blue)]">
            Terms of Use
          </Link>{' '}
          and{' '}
          <Link to="/privacy" className="text-[var(--color-river-blue)]">
            Privacy Policy
          </Link>
        </Text>
      </div>
    </Card>
  );
};

const Carousel = ({ textColor }: { textColor: string }) => (
  <div className="h-full flex items-center justify-center p-12 bg-[var(--mantine-color-dark-1)]">
    <Text size="xl" c={textColor}>
      Carousel Content Coming Soon
    </Text>
  </div>
);

export default function SignIn() {
  const headingTextColor = useThemeColor('headingText');
  const bodyTextColor = useThemeColor('bodyText');
  const mutedTextColor = useThemeColor('mutedText');

  // Handle Google Sign In callback
  useEffect(() => {
    window.handleGoogleSignIn = async (response: any) => {
      // Handle the sign-in response here
      console.log('Google sign-in response:', response);
      // You would typically:
      // 1. Send the response.credential to your backend
      // 2. Verify the token
      // 3. Create a session or handle the authentication
    };
  }, []);

  return (
    <div className="flex flex-col lg:flex-row min-h-screen">
      {/* Left Section - Sign In */}
      <div className="w-full lg:w-1/2">
        <div className="flex flex-col min-h-screen p-6 lg:p-12 bg-[var(--mantine-color-dark-0)]">
          <Logo />
          <HeaderContent 
            headingColor={headingTextColor} 
            textColor={bodyTextColor} 
          />
          <SignInForm mutedTextColor={mutedTextColor} />
          
          {/* Mobile Only Learn More Button */}
          <Button
            variant="outline"
            className="mt-auto lg:hidden"
            component={Link}
            to="/learn-more"
          >
            Learn More
          </Button>
        </div>
      </div>

      {/* Right Section - Carousel */}
      <div className="w-full lg:w-1/2 min-h-screen lg:h-screen">
        <Carousel textColor={bodyTextColor} />
      </div>
    </div>
  );
}