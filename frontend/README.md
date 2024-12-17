# Akka Cache Frontend

Modern web application built with Remix, Vite, TypeScript, Tailwind CSS, and Playwright.

## Tech Stack

- **Remix**: React-based web framework with server-side rendering
- **Vite**: Build tool and development server
- **TypeScript**: Type-safe JavaScript
- **Tailwind CSS**: Utility-first CSS framework
- **Playwright**: End-to-end testing framework

## Prerequisites

Before starting the development environment, ensure you have:

1. A Firebase service account key file (`serviceAccountKey.json`)
   - Obtain this from your Firebase Console (Project Settings > Service Accounts)
   - Place it in the `/frontend` directory (not in `/frontend/app`)
   - The file path should be `frontend/serviceAccountKey.json`

## Development

Start the development environment:

```bash
docker compose up frontend-dev --build
```

Access the app at http://localhost:5173

## Testing

Run end-to-end tests independently:

```bash
# Build and run tests
docker build -t frontend-test -f Dockerfile.test .
docker run frontend-test

# Or use docker compose
docker compose -f docker-compose.test.yml up --build
```

Note: Tests can be run independently from the development environment, as testing dependencies are isolated in their own container.

## Production

Build and run production environment:

```bash
docker compose up frontend-prod --build
```

Access the production build at http://localhost:3000

## Docker Commands

```bash
# Stop all containers
docker compose stop

# Remove containers, networks, and volumes
docker compose down -v

# Clean up networks
docker network prune

# View logs
docker compose logs

# Rebuild specific service
docker compose up <service-name> --build
```

## Project Structure

```
frontend/
├── app/            # Application source code
├── tests/          # Test files
│   └── e2e/       # End-to-end tests
├── Dockerfile.dev  # Development environment
├── Dockerfile.prod # Production environment
└── docker-compose.yml
```