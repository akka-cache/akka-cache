import { useState } from 'react';
import { TextInput, Button, Alert, Stack } from '@mantine/core';
import { IconCheck, IconAlertCircle } from '@tabler/icons-react';
import type { AuthStatus } from '~/types/auth';

interface EmailFormProps {
  onSubmit: (email: string) => Promise<void>;
  status: AuthStatus;
  errorMessage?: string;
  successMessage?: string;
}

export function EmailForm({
  onSubmit,
  status,
  errorMessage,
  successMessage
}: EmailFormProps) {
  const [email, setEmail] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    await onSubmit(email);
  };

  return (
    <form onSubmit={handleSubmit}>
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
          Send Sign In Link
        </Button>
      </Stack>
    </form>
  );
} 