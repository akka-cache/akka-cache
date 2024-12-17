import { useState } from 'react';
import { TextInput, Stack, Checkbox, Alert, Button } from '@mantine/core';
import { Link } from '@remix-run/react';
import { IconCheck, IconAlertCircle } from '@tabler/icons-react';
import type { AuthStatus } from '~/types/auth';
import { useThemeColor } from '~/utils/theme';

interface SignUpFormProps {
  onSubmit: (userData: {
    email: string;
    displayName: string;
    acceptedTerms: boolean;
  }) => Promise<void>;
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

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await onSubmit({
      email,
      displayName,
      acceptedTerms
    });
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <Stack gap="md">
        <TextInput
          label="Email"
          placeholder="Enter your email"
          type="email"
          size="md"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          disabled={status === 'loading' || status === 'success'}
          required
          classNames={{
            label: 'text-gray-100'
          }}
        />

        <TextInput
          label="Display Name"
          placeholder="Enter your full name"
          size="md"
          value={displayName}
          onChange={(e) => setDisplayName(e.target.value)}
          disabled={status === 'loading' || status === 'success'}
          required
          classNames={{
            label: 'text-gray-100'
          }}
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
          disabled={status === 'loading' || status === 'success'}
          required
        />

        {status === 'success' && successMessage && (
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
          loading={status === 'checking' || status === 'loading'}
          disabled={status === 'success' || !acceptedTerms}
        >
          Create Account
        </Button>
      </Stack>
    </form>
  );
} 