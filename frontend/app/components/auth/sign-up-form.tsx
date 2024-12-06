import { useState } from 'react';
import { TextInput, Button, Alert, Text, Stack } from '@mantine/core';
import { IconCheck, IconAlertCircle } from '@tabler/icons-react';
import type { AuthStatus } from '~/types/auth';

interface SignUpFormProps {
  email: string;
  onSubmit: (userData: {
    displayName: string;
    organization: string;
    mobileNumber?: string;
  }) => Promise<void>;
  status: AuthStatus;
  errorMessage?: string;
  successMessage?: string;
}

export function SignUpForm({
  email,
  onSubmit,
  status,
  errorMessage,
  successMessage
}: SignUpFormProps) {
  const [displayName, setDisplayName] = useState('');
  const [organization, setOrganization] = useState('');
  const [mobileNumber, setMobileNumber] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await onSubmit({
      displayName,
      organization,
      mobileNumber: mobileNumber || undefined
    });
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <Stack gap="md">
        <div>
          <Text size="sm" fw={500} className="text-gray-100 mb-1">
            Email
          </Text>
          <Text size="md" className="text-gray-300">
            {email}
          </Text>
        </div>

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

        <TextInput
          label="Organization"
          placeholder="Enter your company or organization name"
          size="md"
          value={organization}
          onChange={(e) => setOrganization(e.target.value)}
          disabled={status === 'loading' || status === 'success'}
          required
          classNames={{
            label: 'text-gray-100'
          }}
        />

        <TextInput
          label="Mobile Number (Optional)"
          placeholder="+1 (555) 555-5555"
          size="md"
          value={mobileNumber}
          onChange={(e) => setMobileNumber(e.target.value)}
          disabled={status === 'loading' || status === 'success'}
          classNames={{
            label: 'text-gray-100'
          }}
        />
      </Stack>
      
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
        disabled={status === 'success'}
      >
        {status === 'loading' ? 'Please wait...' : 'Create Account'}
      </Button>
    </form>
  );
} 