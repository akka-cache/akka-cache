import { Title, Text } from '@mantine/core';

interface HeaderContentProps {
  title?: string;
  subtitle?: string;
  headingColor?: string;
  textColor?: string;
}

interface LogoProps {
  logoUrl: string;
  alt: string;
}

export function Logo({ logoUrl, alt }: LogoProps) {
  return (
    <div className="flex justify-center mb-8">
      <img src={logoUrl} alt={alt} className="h-auto" />
    </div>
  );
}

export function HeaderContent({ 
  title = 'Welcome Back',
  subtitle = 'Sign in to your account',
  headingColor,
  textColor
}: HeaderContentProps) {
  return (
    <div className="text-center mb-8">
      <Title order={1} c={headingColor} className="mb-2">
        {title}
      </Title>
      <Text size="lg" c={textColor}>
        {subtitle}
      </Text>
    </div>
  );
} 