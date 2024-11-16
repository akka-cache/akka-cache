// app/routes/sign-in.tsx
import { Container, TextInput, PasswordInput, Button, Paper, Title } from '@mantine/core';
import { Form } from '@remix-run/react';

export default function SignIn() {
  return (
    <Container size="xs" className="min-h-screen flex items-center">
      <Paper shadow="md" p="xl" className="w-full">
        <Title order={2} className="text-center mb-6">Sign In</Title>
        <Form method="post">
          <TextInput
            label="Email"
            name="email"
            type="email"
            required
            className="mb-4"
          />
          <PasswordInput
            label="Password"
            name="password"
            required
            className="mb-6"
          />
          <Button type="submit" fullWidth>
            Sign In
          </Button>
        </Form>
      </Paper>
    </Container>
  );
}