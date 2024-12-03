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

export async function loader({ request }: LoaderFunctionArgs) {
  const url = new URL(request.url);
  
  // List of public routes that don't require authentication
  const publicRoutes = ['/sign-in', '/', '/sign-up', '/privacy', '/terms'];
  
  // Don't redirect if we're on a public route
  if (publicRoutes.includes(url.pathname)) {
    return null;
  }
  
  // For now, redirect all other routes to sign-in
  // Later, you'll add authentication check here
  return redirect('/sign-in');
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