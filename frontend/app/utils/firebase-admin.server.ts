import admin from 'firebase-admin';
import { readFileSync } from 'fs';
import { resolve } from 'path';

if (!admin.apps.length) {
  if (process.env.NODE_ENV === 'production') {
    try {
      // In production, parse the service account from environment variable
      const serviceAccount = JSON.parse(process.env.FIREBASE_SERVICE_ACCOUNT || '{}');
      admin.initializeApp({
        credential: admin.credential.cert(serviceAccount),
      });
    } catch (error) {
      console.error('Error initializing Firebase Admin:', error);
      throw error;
    }
  } else {
    // In development, read from file
    const serviceAccountPath = resolve(
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
}

export const adminAuth = admin.auth();