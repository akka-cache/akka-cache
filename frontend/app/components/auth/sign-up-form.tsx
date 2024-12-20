import { useState } from 'react';
import { TextInput, Stack, Checkbox, Alert, Button, LoadingOverlay } from '@mantine/core';
import { Link, Form } from '@remix-run/react'; 
import { IconCheck, IconAlertCircle } from '@tabler/icons-react';
import type { AuthStatus } from '~/types/auth';
import { useThemeColor } from '~/utils/theme';

interface SignUpFormProps {
  onSubmit: (userData: {
    email: string;
    displayName: string;
    acceptedTerms: boolean;
  }) => Promise<boolean>;
  status: AuthStatus;
  errorMessage?: string;
  successMessage?: string;
}

export function SignUpForm({
  onSubmit,
  status,
  errorMessage,
  successMessage
}: SignUpFormProps) {
  const [email, setEmail] = useState('');
  const [displayName, setDisplayName] = useState('');
  const [acceptedTerms, setAcceptedTerms] = useState(false);

  const bodyTextColor = useThemeColor('bodyText');
  const isLoading = status === 'loading';
  const isSuccess = status === 'success';

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    const isValid = await onSubmit({
      email,
      displayName,
      acceptedTerms
    });

    if (isValid) {
      (e.target as HTMLFormElement).submit();
    }
  };

  return (
    <div className="relative">
      <LoadingOverlay 
        visible={isLoading} 
        loaderProps={{ size: 'md', color: 'blue' }}
        overlayProps={{ blur: 2 }}
      />
      <Form method="post" className="space-y-4" onSubmit={handleSubmit}>
        <Stack gap="md">
          <TextInput
            label="Email"
            name="email" 
            placeholder="Enter your email"
            type="email"
            size="md"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            disabled={isLoading || isSuccess}
            required
            classNames={{
              label: 'text-gray-100'
            }}
          />

          <TextInput
            label="Display Name"
            name="displayName" 
            placeholder="Enter your full name"
            size="md"
            value={displayName}
            onChange={(e) => setDisplayName(e.target.value)}
            disabled={isLoading || isSuccess}
            required
            classNames={{
              label: 'text-gray-100'
            }}
          />

          <input 
            type="hidden" 
            name="acceptedTerms" 
            value={acceptedTerms.toString()} 
          />

          <Checkbox
            label={
              <span style={{ color: bodyTextColor }}>
                I agree to the{' '}
                <Link to="/legal/terms" className="text-blue-400 hover:underline">
                  Terms of Service
                </Link>
                {' '}and{' '}
                <Link to="/legal/privacy" className="text-blue-400 hover:underline">
                  Privacy Policy
                </Link>
              </span>
            }
            checked={acceptedTerms}
            onChange={(e) => setAcceptedTerms(e.target.checked)}
            disabled={isLoading || isSuccess}
            required
          />

          {isSuccess && successMessage && (
            <Alert icon={<IconCheck size={16} />} color="green">
              {successMessage}
            </Alert>
          )}

          {status === 'error' && errorMessage && (
            <Alert icon={<IconAlertCircle size={16} />} color="red">
              {errorMessage}
            </Alert>
          )}

          <Button 
            fullWidth 
            size="md" 
            type="submit"
            loading={isLoading}
            disabled={isSuccess || !acceptedTerms}
          >
            {isLoading ? 'Creating Account...' : 'Create Account'}
          </Button>
        </Stack>
      </Form>
    </div>
  );
}