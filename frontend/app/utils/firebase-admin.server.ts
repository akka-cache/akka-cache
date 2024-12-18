import admin from 'firebase-admin';
import { readFileSync } from 'fs';
import path from 'path';

if (!admin.apps.length) {
  const serviceAccountPath = path.resolve(
    process.cwd(),
    process.env.FIREBASE_SERVICE_ACCOUNT_PATH || './serviceAccountKey.json'
  );
  
  const serviceAccount = JSON.parse(
    readFileSync(serviceAccountPath, 'utf-8')
  );

  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
  });
}

export const adminAuth = admin.auth();