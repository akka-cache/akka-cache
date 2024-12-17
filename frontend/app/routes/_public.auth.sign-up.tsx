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
      <div className="p-6">
        <Text 
          size="xl" 
          fw={500} 
          c={headingTextColor} 
          mb="md"
          style={{ 
            fontSize: 'var(--font-size-h1)',
            lineHeight: 'var(--line-height-h1)' 
          }}
        >
          Try AkkaCache for free
        </Text>
        <Text size="lg" c={bodyTextColor} mb="xl" maw={600}>
          An accelerator for key-value data with multi-region replication that does not sacrifice 
          performance as traffic increases.
        </Text>
        
        <div className="flex gap-8">
          <div className="flex-1">
            <Text c={bodyTextColor} mb="md">
              <span className="hl">Performance:</span> {' '}
              <span>[Performance metrics to be inserted]</span>
            </Text>
            <Text c={bodyTextColor} mb="md">
              <span className="hl">Multi-language Clients:</span> {' '}
              <span>RESTful interface, Typescript SDK, and Java client library. 
              OpenAI specification to generate additional language clients.</span>
            </Text>
            <Text c={bodyTextColor} mb="md">
              <span className="hl">Batch Operations:</span> {' '}
              <span>Group multiple keys into a common namespace. Execute group insert, 
              get, and delete operations with a single command. Grab all keys in a namespace.</span>
            </Text>
            <Text c={bodyTextColor} mb="md">
              <span className="hl">Guaranteed:</span> {' '}
              <span>Insert our trust center icons / language?</span>
            </Text>
          </div>
          
          <div className="flex-1">
            <Text c={bodyTextColor} mb="md">
              <span className="hl">Multi-region:</span> {' '}
              <span>Pin data to a single region or replicate across many. Read from any 
              region. Updates routed to originating region.</span>
            </Text>
            <Text c={bodyTextColor} mb="md">
              <span className="hl">99.9999% Availability:</span> {' '}
              <span>Akka resilience guarantee and multi-region availability offers 
              10ms RTO and virtually unbreakable availability.</span>
            </Text>
            <Text c={bodyTextColor} mb="md">
              <span className="hl">Large Packets:</span> {' '}
              <span>8MB per request data size. Automatic chunking for performance. 
              octet-stream and json mimetypes.</span>
            </Text>
          </div>
        </div>
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