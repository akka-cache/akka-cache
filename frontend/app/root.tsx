import {
  Links,
  LiveReload,
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
  const url = new URL(request.url);
  
  // Check if this is a sign-in completion
  if (url.pathname === '/' && isSignInWithEmailLink(auth, url.href)) {
    return typedjson({ 
      isEmailLink: true,
      user: await getCurrentUser()
    });
  }
  
  // Check if this is a sign-up completion
  if (url.pathname === '/finishSignUp' && isSignInWithEmailLink(auth, url.href)) {
    return null;
  }

  // For all other routes, return current user state
  const user = await getCurrentUser();
  return typedjson({ user });
}

export default function App() {
  const { isEmailLink, user } = useLoaderData<typeof loader>();
  const navigate = useNavigate();

  useEffect(() => {
    // Handle email link sign-in on the client side
    if (isEmailLink && isSignInWithEmailLink(auth, window.location.href)) {
      const email = window.localStorage.getItem('emailForSignIn');
      if (email) {
        signInWithEmailLink(auth, email, window.location.href)
          .then((result) => {
            window.localStorage.removeItem('emailForSignIn');
            // Force a reload to update the user state
            window.location.reload();
          })
          .catch((error) => {
            console.error('Error completing sign-in:', error);
            navigate('/auth/sign-in');
          });
      }
    }
  }, [isEmailLink, navigate]);

  return (
    <html lang="en" data-mantine-color-scheme="dark">
      <head>
        <Meta />
        <Links />
        <ColorSchemeScript defaultColorScheme="dark" />
      </head>
      <body>
        <MantineProvider theme={theme} defaultColorScheme="dark">
          <AuthProvider value={{ user }}>
            <Outlet />
            <ScrollRestoration />
            <Scripts />
            <LiveReload />
          </AuthProvider>
        </MantineProvider>
      </body>
    </html>
  );
}