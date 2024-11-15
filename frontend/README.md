# Akka Cache Frontend

Modern web application built with Remix, Vite, TypeScript, Tailwind CSS, and Playwright.

## Tech Stack

- **Remix**: React-based web framework with server-side rendering
- **Vite**: Build tool and development server
- **TypeScript**: Type-safe JavaScript
- **Tailwind CSS**: Utility-first CSS framework
- **Playwright**: End-to-end testing framework

## Development

Start the development environment:

```bash
docker compose up frontend-dev --build
```

Access the app at http://localhost:5173

## Testing

Run end-to-end tests:

```bash
docker compose -f docker-compose.test.yml up --build
```

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

# Remove containers
docker compose rm -f

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