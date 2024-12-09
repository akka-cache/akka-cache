import {
  Links,
  Meta,
  Outlet,
  Scripts,
  ScrollRestoration,
} from "@remix-run/react";
import type { LinksFunction } from "@remix-run/node";
import { MantineProvider, ColorSchemeScript } from '@mantine/core';
import '@mantine/core/styles.css';
import theme from './utils/theme';
import "./styles/tailwind.css";
import { redirect } from '@remix-run/node';
import type { LoaderFunctionArgs } from '@remix-run/node';
import { isSignInWithEmailLink, onAuthStateChanged, signInWithEmailLink, User } from 'firebase/auth';
import { auth } from '~/utils/firebase-config';
import { typedjson } from 'remix-typedjson';

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

function checkAuthState(): Promise<User | null> {
  return new Promise((resolve) => {
    const unsubscribe = onAuthStateChanged(auth, (user) => {
      unsubscribe();
      resolve(user);
    });
  });
}

export async function loader({ request }: LoaderFunctionArgs) {
  const url = new URL(request.url);
  
  // List of public routes that don't require authentication
  const publicRoutes = [
    '/auth/sign-in',
    '/auth/sign-up',
    '/auth/verify-email',
    '/legal/privacy',
    '/legal/terms'
  ];
  
  // Check if this is an email verification link
  const isEmailLink = isSignInWithEmailLink(auth, url.href);
  if (isEmailLink) {
    // Instead of redirecting here, we'll let the client handle the verification
    return typedjson({ isEmailLink: true, verificationUrl: url.href });
  }
  
  // Don't redirect if we're on a public route
  if (publicRoutes.includes(url.pathname)) {
    return null;
  }

  // Check authentication for all other routes (including index '/')
  const user = await checkAuthState();
  if (!user) {
    return redirect('/auth/sign-in');
  }
  
  return typedjson({ 
    user: { 
      email: user.email || null, 
      displayName: user.displayName || null 
    } 
  });
}

export default function App() {
  return (
    <html lang="en">
      <head>
        <meta charSet="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1" />
        <Meta />
        <Links />
        <ColorSchemeScript defaultColorScheme="dark" />
      </head>
      <body>
        <MantineProvider
          defaultColorScheme="dark"
          theme={theme}
        >
          <Outlet />
        </MantineProvider>
        <ScrollRestoration />
        <Scripts />
      </body>
    </html>
  );
}