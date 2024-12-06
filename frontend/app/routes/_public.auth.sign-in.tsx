import { useEffect, useState } from 'react';
import { TextInput, Button, Card, Title, Text, Divider, Alert } from '@mantine/core';
import { Link, useOutletContext, useNavigate } from '@remix-run/react';
import { useThemeColor } from '~/utils/theme';
import { auth } from '~/utils/firebase-config';
import { 
  sendSignInLinkToEmail, 
  isSignInWithEmailLink, 
  signInWithEmailLink,
  signInWithCredential,
  onAuthStateChanged,
  GoogleAuthProvider
} from 'firebase/auth';
import { IconCheck, IconAlertCircle } from '@tabler/icons-react';

interface GoogleSignInResponse {
  credential: string;
  select_by: string;
}

declare global {
  interface Window {
    handleGoogleSignIn: (response: GoogleSignInResponse) => Promise<void>;
  }
}

const Logo = () => (
  <Link to="/" className="inline-block mb-12">
    <span className="text-2xl font-bold text-[var(--color-river-blue)]">
      Akka Cache
    </span>
  </Link>
);

const HeaderContent = ({ headingColor, textColor }: { headingColor: string; textColor: string }) => (
  <div className="space-y-3 mb-12">
    <Title order={1} c={headingColor}>
      Enterprise-Grade Serverless Caching
    </Title>
    <Text size="lg" c={textColor}>
      Sign in to continue to Akka Cache
    </Text>
  </div>
);

const SignInForm = ({ mutedTextColor }: { mutedTextColor: string }) => {
  const [email, setEmail] = useState('');
  const [status, setStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle');
  const [errorMessage, setErrorMessage] = useState('');
  const [isCheckingEmailLink, setIsCheckingEmailLink] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const unsubscribe = onAuthStateChanged(auth, (user) => {
      if (user) {
        navigate('/', { replace: true });
      }
    });
    return () => unsubscribe();
  }, [navigate]);

  useEffect(() => {
    if (isSignInWithEmailLink(auth, window.location.href)) {
      const savedEmail = window.localStorage.getItem('emailForSignIn');
      
      if (savedEmail) {
        signInWithEmailLink(auth, savedEmail, window.location.href)
          .then(() => {
            window.localStorage.removeItem('emailForSignIn');
            navigate('/', { replace: true });
          })
          .catch((error) => {
            setStatus('error');
            setErrorMessage(error.message);
          })
          .finally(() => {
            setIsCheckingEmailLink(false);
          });
      } else {
        setStatus('error');
        setErrorMessage('Please provide your email again for verification.');
        setIsCheckingEmailLink(false);
      }
    } else {
      setIsCheckingEmailLink(false);
    }
  }, [navigate]);

  const handleEmailSignIn = async (e: React.FormEvent) => {
    e.preventDefault();
    setStatus('loading');
    
    const actionCodeSettings = {
      url: window.location.origin + '/auth/sign-in',
      handleCodeInApp: true
    };

    try {
      await sendSignInLinkToEmail(auth, email, actionCodeSettings);
      window.localStorage.setItem('emailForSignIn', email);
      setStatus('success');
    } catch (error: any) {
      setStatus('error');
      setErrorMessage(error.message || 'Failed to send sign-in link');
    }
  };

  if (isCheckingEmailLink) {
    return (
      <Card shadow="md" p="xl" className="w-full mb-8" bg="dark.0">
        <div className="flex justify-center items-center p-8">
          <Text>Verifying sign-in link...</Text>
        </div>
      </Card>
    );
  }

  return (
    <Card 
      shadow="md" 
      p="xl" 
      className="w-full mb-8 hl"
      bg="dark.0"
    >
      <form onSubmit={handleEmailSignIn} className="space-y-4">
        <TextInput
          label="Email"
          placeholder="Enter your email"
          size="md"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          disabled={status === 'loading' || status === 'success'}
          required
          type="email"
          classNames={{
            label: 'text-gray-100'
          }}
        />
        
        {status === 'success' && (
          <Alert icon={<IconCheck size={16} />} title="Check your email!" color="green">
            We've sent you a sign-in link. Please check your email to continue.
          </Alert>
        )}

        {status === 'error' && (
          <Alert icon={<IconAlertCircle size={16} />} title="Error" color="red">
            {errorMessage}
          </Alert>
        )}

        <Button 
          fullWidth 
          size="md" 
          type="submit"
          loading={status === 'loading'}
          disabled={status === 'success'}
        >
          {status === 'loading' ? 'Sending link...' : 'Continue with email'}
        </Button>

        <Divider label="OR" labelPosition="center" />

        <div 
          id="g_id_onload"
          data-client_id="YOUR_GOOGLE_CLIENT_ID"
          data-context="signin"
          data-ux_mode="popup"
          data-callback="handleGoogleSignIn"
          data-auto_select="false"
          data-itp_support="true"
        />
        
        <div 
          className="g_id_signin"
          data-type="standard"
          data-shape="rectangular"
          data-theme="outline"
          data-text="signin_with"
          data-size="large"
          data-logo_alignment="left"
          data-width="100%"
        />

        <Text size="xs" c={mutedTextColor} ta="center">
          By continuing, you agree to Akka's{' '}
          <Link to="/legal/terms" className="text-[var(--color-river-blue)]">
            Terms of Use
          </Link>{' '}
          and{' '}
          <Link to="/legal/privacy" className="text-[var(--color-river-blue)]">
            Privacy Policy
          </Link>
        </Text>
      </form>
    </Card>
  );
};

export default function SignIn() {
  const headingTextColor = useThemeColor('headingText');
  const bodyTextColor = useThemeColor('bodyText');
  const mutedTextColor = useThemeColor('mutedText');
  const context = useOutletContext<string>();
  const navigate = useNavigate();

  useEffect(() => {
    window.handleGoogleSignIn = async (response: GoogleSignInResponse) => {
      try {
        const { credential } = response;
        if (!credential) throw new Error('Invalid credential');
        
        const googleCredential = GoogleAuthProvider.credential(credential);
        const result = await signInWithCredential(auth, googleCredential);
        
        if (!result.user) throw new Error('Sign in failed');
        navigate('/', { replace: true });
      } catch (error: any) {
        console.error('Google sign in error:', error);
      }
    };
  }, [navigate]);

  if (context === 'right') {
    return (
      <div>
        <Text size="xl" c={bodyTextColor}>More content goes here</Text>
      </div>
    );
  }

  return (
    <div>
      <Logo />
      <HeaderContent 
        headingColor={headingTextColor} 
        textColor={bodyTextColor} 
      />
      <SignInForm mutedTextColor={mutedTextColor} />
      
      <Button
        variant="outline"
        className="mt-8 lg:hidden"
        component={Link}
        to="/learn-more"
      >
        Learn More
      </Button>
    </div>
  );
}