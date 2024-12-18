import { Title, Text } from '@mantine/core';

interface HeaderContentProps {
  title?: string;
  subtitle?: string;
  headingColor?: string;
  textColor?: string;
}

export function Logo() {
  return (
    <div className="flex justify-center mb-8">
      {/* Replace with your actual logo component */}
      <img 
        src="/images/logo.svg" 
        alt="Logo" 
        className="h-12 w-auto"
      />
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