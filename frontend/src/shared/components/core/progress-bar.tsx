import { m } from "framer-motion"
import { cn } from "@/lib/utils"

interface ProgressSegment {
  id?: string | number
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
  overbudget: "bg-destructive",
  warning: "bg-accent",
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
}: ProgressBarProps) {
  const percentage = value !== undefined ? Math.min((value / max) * 100, 100) : 0

  if (segments && segments.length > 0) {
    return (
      <div
        className={cn(
          sizeClasses[size],
          "w-full bg-secondary rounded-full overflow-hidden flex",
          className
        )}
        aria-label={ariaLabel}
      >
        {segments.map((segment, i) => (
          <m.div
            key={segment.id ?? segment.label ?? i}
            className={cn(
              "h-full",
              segment.className,
              barClassName
            )}
            initial={{ width: 0 }}
            animate={{ width: `${segment.value}%` }}
            transition={{ duration: 0.7, ease: "easeOut" }}
            title={segment.label}
            aria-label={segment.label}
          />
        ))}
      </div>
    )
  }

  return (
    <div className={cn("relative w-full", sizeClasses[size], className)}>
      {}
      <progress
        value={value ?? 0}
        max={max}
        aria-label={ariaLabel}
        className={cn(
          "absolute inset-0 h-full w-full appearance-none rounded-full bg-transparent",
          "[&::-webkit-progress-bar]:rounded-full [&::-webkit-progress-bar]:bg-secondary",
          "[&::-webkit-progress-value]:rounded-full [&::-webkit-progress-value]:bg-transparent",
          "[&::-moz-progress-bar]:rounded-full [&::-moz-progress-bar]:bg-transparent"
        )}
      />

      {}
      <div className="absolute inset-0 h-full w-full rounded-full overflow-hidden pointer-events-none">
        <m.div
          className={cn(
            "h-full rounded-full",
            variant === "default" && !color && "bg-primary",
            variant !== "default" && variantClasses[variant],
            barClassName
          )}
          initial={{ width: 0 }}
          animate={{ width: `${percentage}%` }}
          transition={{ duration: 0.7, ease: "easeOut" }}
          style={{
            ...(color && variant === "default" ? { backgroundColor: color } : {}),
          }}
        />
      </div>
    </div>
  )
}
