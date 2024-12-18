// app/routes/index.tsx
import { Container, Title, Text, Button, Anchor } from '@mantine/core';
import { Link, useLoaderData } from '@remix-run/react';
import { redirect, type LoaderFunctionArgs } from '@remix-run/node';
import Layout from '~/components/Layout';
import { useThemeColor } from '~/utils/theme';
import { adminAuth } from '~/utils/firebase-admin.server';
import { getSession } from '~/utils/session.server';

export async function loader({ request }: LoaderFunctionArgs) {
  const session = await getSession(request.headers.get("Cookie"));
  const sessionCookie = session.get("session");

  if (!sessionCookie) {
    return redirect("/auth/sign-in");
  }

  try {
    const decodedClaims = await adminAuth.verifySessionCookie(sessionCookie);
    return { 
      user: {
        email: decodedClaims.email,
        displayName: decodedClaims.name || decodedClaims.email?.split('@')[0]
      },
      claims: decodedClaims,
      sessionCookie
    };
  } catch (error) {
    console.error("Session verification failed:", error);
    return redirect("/auth/sign-in");
  }
}

export default function Index() {
  const { user, claims, sessionCookie } = useLoaderData<typeof loader>();
  const colors = {
    heading: useThemeColor('headingText'),
    body: useThemeColor('bodyText'),
    secondary: useThemeColor('secondaryText'),
    muted: useThemeColor('mutedText'),
  };

  console.log('Index route - Rendering with user:', user);

  const sampleRequest = `GET /api/your-endpoint HTTP/1.1
Host: your-domain.com
Authorization: Bearer ${sessionCookie}
Accept: application/json`;

  return (
    <Layout user={user}>
      <Container 
        size="lg" 
        className="min-h-screen flex flex-col justify-center items-center"
      >
        <div className="w-full max-w-2xl">
          <Title order={1} className="text-center mb-6" c={colors.heading}>
            Enterprise-Grade Serverless Caching
          </Title>
          <Text size="lg" className="text-center mb-8" c={colors.body}>
            Simplify your architecture
          </Text>
          
          <div className="mt-8 p-4 bg-gray-50 dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700">
            <Text size="sm" className="mb-2 font-semibold text-gray-700 dark:text-gray-200">JWT Token:</Text>
            <pre className="whitespace-pre-wrap overflow-x-auto p-3 bg-white dark:bg-gray-900 rounded border border-gray-100 dark:border-gray-600">
              <code className="text-gray-900 dark:text-gray-100">{sessionCookie}</code>
            </pre>
          </div>

          <div className="mt-4 p-4 bg-gray-50 dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700">
            <Text size="sm" className="mb-2 font-semibold text-gray-700 dark:text-gray-200">Decoded JWT Claims:</Text>
            <pre className="whitespace-pre-wrap overflow-x-auto p-3 bg-white dark:bg-gray-900 rounded border border-gray-100 dark:border-gray-600">
              <code className="text-gray-900 dark:text-gray-100">{JSON.stringify(claims, null, 2)}</code>
            </pre>
          </div>

          <div className="mt-4 p-4 bg-gray-50 dark:bg-gray-800 rounded-lg border border-gray-200 dark:border-gray-700">
            <Text size="sm" className="mb-2 font-semibold text-gray-700 dark:text-gray-200">Sample GET Request:</Text>
            <pre className="whitespace-pre-wrap overflow-x-auto p-3 bg-white dark:bg-gray-900 rounded border border-gray-100 dark:border-gray-600">
              <code className="text-gray-900 dark:text-gray-100">{sampleRequest}</code>
            </pre>
          </div>
        </div>
      </Container>
    </Layout>
  );
}