# AkkaCache Documentation

This documentation site is built using [Docusaurus](https://docusaurus.io/).

## Project Structure

This is part of a monorepo with the following structure:
```
akka-cache/
├── acdocs/     # Documentation (Docusaurus)
├── backend/    # Java/Akka server code
└── frontend/   # Remix.js frontend
```

## Development

All documentation commands should be run from the `acdocs` directory:

```bash
cd acdocs
```

### Installation

```bash
npm install
```

### Local Development

```bash
npm run start
```

This command starts a local development server and opens up a browser window at `http://localhost:3000/cache/`. Most changes are reflected live without having to restart the server.

### Build

```bash
npm run build
npm run serve  # To test the production build locally
```

## Security Updates

Recent security fixes (Feb 4, 2025):
- Fixed high severity ReDoS vulnerability in `path-to-regexp` (via `express` dependency)
- Updated dependencies to latest secure versions via `npm audit fix`

## Note for Developers

This project previously used Yarn but has been updated to use npm for consistency across the monorepo. The `package-lock.json` file should be committed to ensure dependency consistency.

## Related Projects

- Frontend UI: See the `/frontend` directory for the Remix.js application
- Backend API: See the `/backend` directory for the Akka-based server
