import {
  Links,
  Meta,
  Outlet,
  Scripts,
  ScrollRestoration,
  useLoaderData,
  useNavigate,
} from "@remix-run/react";
import type { LinksFunction } from "@remix-run/node";
import { MantineProvider, ColorSchemeScript } from '@mantine/core';
import '@mantine/core/styles.css';
import theme from './utils/theme';
import "./styles/tailwind.css";
import { redirect } from '@remix-run/node';
import type { LoaderFunctionArgs } from '@remix-run/node';
import { isSignInWithEmailLink, onAuthStateChanged, signInWithEmailLink } from 'firebase/auth';
import { auth } from '~/utils/firebase-config';
import { typedjson } from 'remix-typedjson';
import { useEffect } from 'react';
import { AuthProvider } from '~/contexts/auth-context';

export const links: LinksFunction = () => [
  { rel: "preconnect", href: "https://fonts.googleapis.com" },
  {
    rel: "preconnect",
    href: "https://fonts.gstatic.com",
    crossOrigin: "anonymous",
  },
  {
    rel: "stylesheet",
    href: "https://fonts.googleapis.com/css2?family=Instrument+Sans:ital,wdth,wght@0,75..100,400..700;1,75..100,400..700&family=Roboto+Mono:ital,wght@0,400;0,700;1,400;1,700&display=swap",
  },
];

// Helper function to get current user data
const getCurrentUser = () => {
  return new Promise((resolve, reject) => {
    const unsubscribe = onAuthStateChanged(
      auth,
      (user) => {
        unsubscribe();
        resolve(user ? {
          email: user.email,
          displayName: user.displayName
        } : null);
      },
      reject
    );
  });
};

export async function loader({ request }: LoaderFunctionArgs) {
  const user = await getCurrentUser();
  return typedjson({ user });
}

export default function App() {
  const { user } = useLoaderData<typeof loader>();
  
  return (
    <html lang="en" data-mantine-color-scheme="dark">
      <head>
        <Meta />
        <Links />
        <ColorSchemeScript defaultColorScheme="dark" />
      </head>
      <body>
        <MantineProvider theme={theme} defaultColorScheme="dark">
          <AuthProvider>
            <Outlet />
            <ScrollRestoration />
            <Scripts />
          </AuthProvider>
        </MantineProvider>
      </body>
    </html>
  );
}