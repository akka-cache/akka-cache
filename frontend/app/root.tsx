import {
  Links,
  Meta,
  Outlet,
  Scripts,
  ScrollRestoration,
  useLoaderData,
} from "@remix-run/react";
import type { LinksFunction, LoaderFunctionArgs } from "@remix-run/node";
import { MantineProvider, ColorSchemeScript } from '@mantine/core';
import '@mantine/core/styles.css';
import theme from './utils/theme';
import "./styles/tailwind.css";
import { redirect } from '@remix-run/node';
import { adminAuth } from '~/utils/firebase-admin.server';
import { getSession } from '~/utils/session.server';
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
  {
    rel: "icon",
    type: "image/x-icon",
    href: "/favicon.ico"
  },
  {
    rel: "icon",
    type: "image/png",
    sizes: "16x16",
    href: "/favicon-16x16.png"
  },
  {
    rel: "icon",
    type: "image/png",
    sizes: "32x32",
    href: "/favicon-32x32.png"
  },
  {
    rel: "icon",
    type: "image/png",
    sizes: "96x96",
    href: "/favicon-96x96.png"
  },
];

export async function loader({ request }: LoaderFunctionArgs) {
  console.log("🔍 Root loader - Request headers:", request.headers);
  const session = await getSession(request.headers.get("Cookie"));
  const sessionCookie = session.get("session");
  console.log("🔍 Root loader - Session cookie:", !!sessionCookie);

  if (sessionCookie) {
    try {
      const decodedClaims = await adminAuth.verifySessionCookie(sessionCookie);
      console.log("Root loader - Verified user:", decodedClaims.email);
      
      return { 
        user: {
          email: decodedClaims.email,
          displayName: decodedClaims.name || decodedClaims.email?.split('@')[0]
        }
      };
    } catch (error) {
      console.error("Root loader - Session verification failed:", error);
      return { user: null };
    }
  }

  return { user: null };
}

export default function App() {
  const { user } = useLoaderData<typeof loader>();
  
  // Add debug logging
  console.log("Root component - User data:", user);

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