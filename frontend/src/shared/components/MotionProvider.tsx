import type { ReactNode } from "react"
import { LazyMotion, MotionConfig, useReducedMotion, domAnimation } from "framer-motion"

interface MotionProviderProps {
  children: ReactNode
}

/**
 * Proveedor de animaciones del proyecto.
 *
 * - Carga las features de animación del DOM bajo demanda (LazyMotion).
 * - Respeta la preferencia del sistema `prefers-reduced-motion`
 *   desactivando las animaciones de Framer Motion cuando sea necesario.
 */
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
