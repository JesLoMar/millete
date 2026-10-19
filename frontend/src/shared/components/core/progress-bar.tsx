import * as m from 'framer-motion/m';
import { useReducedMotion } from 'framer-motion';
import { cn } from '@/lib/utils'

interface ProgressSegment {
  id: string | number
  value: number
  className?: string
  label?: string
}

interface ProgressBarProps {
  value?: number
  max?: number
  color?: string
  segments?: ProgressSegment[]
  size?: 'sm' | 'md' | 'lg'
  variant?: 'default' | 'overbudget' | 'warning'
  className?: string
  barClassName?: string
  ariaLabel?: string
  ariaValueNow?: number
  ariaValueMin?: number
  ariaValueMax?: number
}

const sizeClasses = {
  sm: 'h-1',
  md: 'h-1.5',
  lg: 'h-2.5 sm:h-3',
}

const variantClasses = {
  default: '',
  overbudget: 'bg-destructive',
  warning: 'bg-accent',
}

const clamp = (n: number, min: number, max: number) =>
  Math.min(Math.max(n, min), max)

const finiteOr = (n: number | undefined, fallback: number) =>
  typeof n === 'number' && Number.isFinite(n) ? n : fallback

export function ProgressBar({
  value,
  max = 100,
  color,
  segments,
  size = 'md',
  variant = 'default',
  className,
  barClassName,
  ariaLabel,
  ariaValueNow,
  ariaValueMin,
  ariaValueMax,
}: ProgressBarProps) {
  const reduceMotion = useReducedMotion()

  const transition = {
    duration: reduceMotion ? 0 : 0.7,
    ease: 'easeOut' as const,
  }

  if (segments && segments.length > 0) {
    const widths = segments.map((segment) =>
      clamp(finiteOr(segment.value, 0), 0, 100),
    )
    const total = clamp(
      widths.reduce((sum, width) => sum + width, 0),
      0,
      100,
    )

    return (
      <div
        role="progressbar"
        aria-label={ariaLabel}
        aria-valuemin={ariaValueMin ?? 0}
        aria-valuemax={ariaValueMax ?? 100}
        aria-valuenow={ariaValueNow ?? Math.round(total)}
        className={cn(
          sizeClasses[size],
          'w-full bg-secondary rounded-full overflow-hidden flex',
          className,
        )}
      >
        {segments.map((segment, index) => (
          <m.div
            key={segment.id}
            className={cn(
              'h-full',
              segment.className,
              barClassName,
            )}
            initial={{ width: 0 }}
            animate={{ width: `${widths[index]}%` }}
            transition={transition}
            title={segment.label}
          />
        ))}
      </div>
    )
  }

  const safeMax = finiteOr(max, 0)
  const hasValidMax = safeMax > 0
  const safeValue = hasValidMax
    ? clamp(finiteOr(value, 0), 0, safeMax)
    : 0
  const percentage = hasValidMax ? (safeValue / safeMax) * 100 : 0

  return (
    <div
      role="progressbar"
      aria-label={ariaLabel}
      aria-valuenow={ariaValueNow}
      aria-valuemin={ariaValueMin ?? 0}
      aria-valuemax={ariaValueMax ?? (hasValidMax ? safeMax : 1)}
      className={cn(
        'relative w-full overflow-hidden rounded-full bg-secondary',
        sizeClasses[size],
        className,
      )}
    >
      <m.div
        className={cn(
          'h-full w-full origin-left rounded-full',
          variant === 'default' && !color && 'bg-primary',
          variant !== 'default' && variantClasses[variant],
          barClassName,
        )}
        style={
          color && variant === 'default'
            ? { backgroundColor: color }
            : undefined
        }
        initial={{ scaleX: 0 }}
        animate={{ scaleX: hasValidMax ? percentage / 100 : 0 }}
        transition={transition}
      />
    </div>
  )
}