import { Form } from '@remix-run/react';
import { TextInput, Button, Alert, Stack } from '@mantine/core';
import { IconCheck, IconAlertCircle } from '@tabler/icons-react';

type FormStatus = 'idle' | 'loading' | 'success' | 'error';

interface EmailFormProps {
  status: FormStatus;
  errorMessage?: string;
  successMessage?: string;
}

export function EmailForm({ 
  status, 
  errorMessage, 
  successMessage 
}: EmailFormProps) {
  return (
    <Form method="post">
      <Stack gap="md">
        <TextInput
          label="Email"
          name="email"
          placeholder="Enter your email"
          type="email"
          size="md"
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
          loading={status === 'loading'}
          disabled={status === 'success'}
        >
          Send Sign In Link
        </Button>
      </Stack>
    </Form>
  );
} 