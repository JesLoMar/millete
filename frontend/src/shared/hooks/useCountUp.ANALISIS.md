# useCountUp — Análisis del warning `react-hooks/set-state-in-effect`

## ¿Qué dice el linter?

El warning aparece en `useCountUp.ts:43`:

```ts
if (safeTarget === 0) {
  currentRef.current = 0
  setDisplayValue(0)  // ← Warning: Avoid calling setState() directly within an effect
  return
}
```

La regla `react-hooks/set-state-in-effect` prohibe llamar a `setState` directamente dentro del cuerpo de un `useEffect`. La razón oficial de React es que los efectos deben **sincronizar** React con sistemas externos (DOM, APIs, timers), no con ellos mismos. Llamar `setState` dentro de un efecto causa **renders en cascada**: el efecto se ejecuta, cambia el estado, eso dispara otro render, y si hay más lógica condicional, puede disparar otro efecto.

## ¿Por qué está mal hecho?

El hook `useCountUp` tiene un `useEffect` que:
1. Compara si `targetValue` cambió
2. Si es 0, llama `setDisplayValue(0)` inmediatamente
3. Si no es 0, programa un `requestAnimationFrame` que a su vez llama `setDisplayValue` en cada frame

El problema es que **todo el estado del contador se maneja dentro del efecto**. El efecto no está sincronizando con un sistema externo; está **siendo** el sistema de animación. Esto es un anti-patrón en React.

## Impacto real

- **Rendimiento:** En componentes con muchos contadores (dashboard con múltiples métricas), cada cambio de `targetValue` dispara un render adicional sincrónico.
- **Consistencia:** Si el componente padre desmonta mientras el `setTimeout`/`requestAnimationFrame` está pendiente, el cleanup del efecto cancela el RAF, pero el estado ya puede haber cambiado.
- **Testabilidad:** Es difícil testear este hook porque la lógica de animación está acoplada al ciclo de vida de React.

## ¿Cómo debería hacerse?

La solución canónica es **separar la animación del estado de React**. Hay dos enfoques:

### Opción A: Usar `useRef` para el valor animado y forzar re-renders con un estado auxiliar

En lugar de guardar el valor animado en `useState`, se guarda en `useRef` y se usa un estado "tick" que solo sirve para forzar re-renders:

```ts
export function useCountUp(targetValue: number, options: UseCountUpOptions = {}) {
  const { duration = 600, delay = 0, easing = easeOutQuart } = options
  const safeTarget = typeof targetValue === "number" && !isNaN(targetValue) ? targetValue : 0

  const valueRef = useRef(safeTarget)
  const [, setTick] = useState(0) // Solo para forzar re-renders
  const rafRef = useRef(0)

  useEffect(() => {
    if (valueRef.current === safeTarget) return

    const startValue = valueRef.current
    const startTime = performance.now() + delay

    const animate = (now: number) => {
      const elapsed = now - startTime
      const progress = Math.min(elapsed / duration, 1)
      const easedProgress = easing(progress)

      valueRef.current = startValue + (safeTarget - startValue) * easedProgress
      setTick(t => t + 1) // Fuerza re-render sin almacenar el valor en estado

      if (progress < 1) {
        rafRef.current = requestAnimationFrame(animate)
      }
    }

    const timeoutId = setTimeout(() => {
      rafRef.current = requestAnimationFrame(animate)
    }, delay)

    return () => {
      clearTimeout(timeoutId)
      cancelAnimationFrame(rafRef.current)
    }
  }, [safeTarget, delay, duration, easing])

  return valueRef.current
}
```

**Problema:** Aunque `setTick` sigue estando en el efecto, el valor real se lee del ref. La regla del linter seguiría quejándose, pero el impacto es menor porque `setTick` no depende del valor anterior.

### Opción B: Usar una librería dedicada (recomendada)

Librerías como `framer-motion` ya tienen hooks optimizados para animaciones numéricas:

```tsx
import { useSpring, useMotionValue, useTransform } from 'framer-motion'

function AnimatedNumber({ value }: { value: number }) {
  const motionValue = useMotionValue(value)
  const springValue = useSpring(motionValue, { stiffness: 100, damping: 30 })
  const display = useTransform(springValue, v => Math.round(v))
  // ...
}
```

Esto delega la animación a la librería, que usa refs internos y optimiza los re-renders.

### Opción C: Custom hook con `useSyncExternalStore` (la solución "correcta" de React)

```ts
import { useSyncExternalStore, useRef, useEffect } from 'react'

function createAnimationStore(targetValue: number, duration: number, delay: number) {
  let value = targetValue
  let listeners: (() => void)[] = []
  let rafId = 0

  function notify() {
    listeners.forEach(l => l())
  }

  function animate(to: number) {
    const start = value
    const startTime = performance.now() + delay

    const step = (now: number) => {
      const elapsed = now - startTime
      const progress = Math.min(elapsed / duration, 1)
      value = start + (to - start) * progress
      notify()
      if (progress < 1) {
        rafId = requestAnimationFrame(step)
      }
    }

    setTimeout(() => {
      rafId = requestAnimationFrame(step)
    }, delay)
  }

  return {
    subscribe(listener: () => void) {
      listeners.push(listener)
      return () => {
        listeners = listeners.filter(l => l !== listener)
      }
    },
    getSnapshot() { return value },
    getServerSnapshot() { return targetValue },
    setTarget(to: number) {
      cancelAnimationFrame(rafId)
      animate(to)
    },
  }
}

export function useCountUp(targetValue: number, options: UseCountUpOptions = {}) {
  const { duration = 600, delay = 0 } = options
  const storeRef = useRef(createAnimationStore(targetValue, duration, delay))

  useEffect(() => {
    storeRef.current.setTarget(targetValue)
  }, [targetValue])

  return useSyncExternalStore(
    storeRef.current.subscribe,
    storeRef.current.getSnapshot,
    storeRef.current.getServerSnapshot
  )
}
```

**Ventaja:** `useSyncExternalStore` está diseñado exactamente para esto: suscribirse a una fuente de datos externa (en este caso, el loop de animación) y sincronizar React con ella. No hay `setState` en el efecto; el efecto solo inicia la animación, y React se entera de los cambios vía el store.

## Recomendación

**Opción C** es la más "correcta" desde el punto de vista de React, pero añade complejidad. **Opción B** (usar framer-motion) es la más pragmática si ya está en el proyecto. **Opción A** es la más rápida de implementar pero no elimina completamente el warning.

Dado que el proyecto ya usa `framer-motion` (se ve en `progress-bar.tsx`), la solución más limpia sería reemplazar `useCountUp` por `useSpring` o `useMotionValue` de framer-motion.
