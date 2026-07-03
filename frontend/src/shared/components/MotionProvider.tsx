import type { ReactNode } from "react"
import { LazyMotion, MotionConfig, useReducedMotion, domAnimation } from "framer-motion"

interface MotionProviderProps {
  children: ReactNode
}

export function MotionProvider({ children }: MotionProviderProps) {
  const shouldReduceMotion = useReducedMotion()

  return (
    <LazyMotion features={domAnimation} strict>
      <MotionConfig
        reducedMotion={shouldReduceMotion ? "always" : "user"}
      >
        {children}
      </MotionConfig>
    </LazyMotion>
  )
}
