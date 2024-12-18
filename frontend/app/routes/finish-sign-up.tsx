import { useEffect } from 'react';
import { Card, Text, Loader } from '@mantine/core';
import { useSignUp } from '~/hooks';
import { useThemeColor } from '~/utils/theme';

export default function FinishSignUp() {
  const bodyTextColor = useThemeColor('bodyText');
  const {
    status,
    errorMessage,
    handleSignUp
  } = useSignUp({
    onError: (error) => {
      console.error('Sign-up completion error:', error);
    }
  });

  useEffect(() => {
    handleSignUp({ email: '', displayName: '' });  // Add minimal required user data
  }, [handleSignUp]);

  return (
    <div className="w-full max-w-2xl mx-auto p-6">
      <Card shadow="md" p="xl" className="w-full mb-8" bg="dark.0">
        {status === 'loading' ? (
          <div className="text-center">
            <Loader size="lg" className="mb-4" />
            <Text c={bodyTextColor}>Completing your sign up...</Text>
          </div>
        ) : (
          <div className="text-center">
            {errorMessage && (
              <Text c="red" size="sm">
                {errorMessage}
              </Text>
            )}
          </div>
        )}
      </Card>
    </div>
  );
} 