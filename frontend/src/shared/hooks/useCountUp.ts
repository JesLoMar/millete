import { useEffect, useRef, useState } from "react"

interface UseCountUpOptions {
  duration?: number
  delay?: number
  easing?: (t: number) => number
}

function easeOutQuart(t: number): number {
  return 1 - Math.pow(1 - t, 4)
}

export function useCountUp(
  targetValue: number,
  options: UseCountUpOptions = {}
) {
  const { duration = 600, delay = 0, easing = easeOutQuart } = options

  const safeTarget =
    typeof targetValue === "number" && !isNaN(targetValue) ? targetValue : 0

  const [displayValue, setDisplayValue] = useState(safeTarget)

  const currentRef = useRef(safeTarget)
  const startValueRef = useRef(safeTarget)
  const startTimeRef = useRef<number | null>(null)
  const targetRef = useRef(safeTarget)
  const rafRef = useRef(0)
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null)

  useEffect(() => {
    if (targetRef.current === safeTarget) return

    targetRef.current = safeTarget
    startValueRef.current = currentRef.current
    startTimeRef.current = null

    if (timeoutRef.current) clearTimeout(timeoutRef.current)
    if (rafRef.current) cancelAnimationFrame(rafRef.current)

    if (safeTarget === 0) {
      currentRef.current = 0
      setDisplayValue(0)
      return
    }

    const animate = (now: number) => {
      const elapsed = now - startTimeRef.current!
      const progress = Math.min(elapsed / duration, 1)
      const easedProgress = easing(progress)

      const nextValue =
        startValueRef.current +
        (safeTarget - startValueRef.current) * easedProgress

      currentRef.current = nextValue
      setDisplayValue(nextValue)

      if (progress < 1) {
        rafRef.current = requestAnimationFrame(animate)
      }
    }

    timeoutRef.current = setTimeout(() => {
      startTimeRef.current = performance.now()
      rafRef.current = requestAnimationFrame(animate)
    }, delay)

    return () => {
      if (timeoutRef.current) clearTimeout(timeoutRef.current)
      if (rafRef.current) cancelAnimationFrame(rafRef.current)
    }
  }, [safeTarget, delay, duration, easing])

  return displayValue
}
