import { initializeApp } from "firebase/app";
import { getAuth, browserLocalPersistence } from "firebase/auth";

const firebaseConfig = {
  apiKey: "AIzaSyBWpIU_Cg_Qyw2sOKYuMsUsFB7cRdCkHqQ",
  authDomain: "akka-cache.firebaseapp.com",
  projectId: "akka-cache",
  storageBucket: "akka-cache.firebasestorage.app",
  messagingSenderId: "784163392418",
  appId: "1:784163392418:web:1e05aff9ff51895e4480fb"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
export const auth = getAuth(app);

// Enable persistence
auth.setPersistence(browserLocalPersistence);