import type { ReactNode } from 'react';
import {
  LazyMotion,
  MotionConfig,
  useReducedMotion,
} from 'framer-motion';

interface MotionProviderProps {
  children: ReactNode;
}

const loadFeatures = () =>
  import('./motionFeatures').then((module) => module.default);

export function MotionProvider({
  children,
}: MotionProviderProps) {
  const shouldReduceMotion = useReducedMotion();

  return (
    <LazyMotion features={loadFeatures} strict>
      <MotionConfig
        reducedMotion={shouldReduceMotion ? 'always' : 'user'}
      >
        {children}
      </MotionConfig>
    </LazyMotion>
  );
}