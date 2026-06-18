import { cn } from "@/lib/utils"

interface ProgressSegment {
  value: number
  className?: string
  label?: string
}

interface ProgressBarProps {
  value?: number
  max?: number
  color?: string
  segments?: ProgressSegment[]
  size?: "sm" | "md" | "lg"
  variant?: "default" | "overbudget" | "warning"
  className?: string
  barClassName?: string
  ariaLabel?: string
  ariaValueNow?: number
  ariaValueMin?: number
  ariaValueMax?: number
}

const sizeClasses = {
  sm: "h-1",
  md: "h-1.5",
  lg: "h-2.5 sm:h-3",
}

const variantClasses = {
  default: "",
  overbudget: "bg-rose-500",
  warning: "bg-amber-500",
}

export function ProgressBar({
  value,
  max = 100,
  color,
  segments,
  size = "md",
  variant = "default",
  className,
  barClassName,
  ariaLabel,
  ariaValueNow,
  ariaValueMin = 0,
  ariaValueMax = 100,
}: ProgressBarProps) {
  const percentage = value !== undefined ? Math.min((value / max) * 100, 100) : undefined

  if (segments && segments.length > 0) {
    return (
      <div
        className={cn(
          sizeClasses[size],
          "w-full bg-secondary rounded-full overflow-hidden flex",
          className
        )}
        role="progressbar"
        aria-label={ariaLabel}
        aria-valuenow={ariaValueNow}
        aria-valuemin={ariaValueMin}
        aria-valuemax={ariaValueMax}
      >
        {segments.map((segment, i) => (
          <div
            key={i}
            className={cn(
              "h-full transition-all duration-700",
              segment.className,
              barClassName
            )}
            style={{ width: `${segment.value}%` }}
            title={segment.label}
            aria-label={segment.label}
          />
        ))}
      </div>
    )
  }

  return (
    <div
      className={cn(
        sizeClasses[size],
        "w-full bg-secondary rounded-full overflow-hidden",
        className
      )}
      role="progressbar"
      aria-label={ariaLabel}
      aria-valuenow={ariaValueNow ?? Math.round(percentage ?? 0)}
      aria-valuemin={ariaValueMin}
      aria-valuemax={ariaValueMax}
    >
      <div
        className={cn(
          "h-full transition-all duration-700 rounded-full",
          variant !== "default" && variantClasses[variant],
          barClassName
        )}
        style={{
          width: `${percentage ?? 0}%`,
          ...(color && variant === "default" ? { backgroundColor: color } : {}),
        }}
      />
    </div>
  )
}