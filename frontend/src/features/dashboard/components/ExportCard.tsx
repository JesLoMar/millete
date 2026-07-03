import { cn } from "@/lib/utils"
import type { LucideIcon } from "lucide-react"

interface ExportCardProps {
  icon: LucideIcon
  label: string
  description?: string
  color: string
  onClick: () => void
  disabled?: boolean
  className?: string
}

export function ExportCard({
  icon: Icon,
  label,
  description,
  color,
  onClick,
  disabled = false,
  className,
}: ExportCardProps) {
  return (
    <button
      type="button"
      onClick={onClick}
      disabled={disabled}
      className={cn(
        "flex flex-col items-center justify-center gap-2 sm:gap-2.5",
        "rounded-xl border border-border/50 bg-card px-2 py-4 sm:py-5",
        "hover:border-primary/30 hover:shadow-sm",
        "transition-all duration-200 group cursor-pointer",
        "disabled:opacity-50 disabled:pointer-events-none",
        className
      )}
    >
      <div className={cn("p-2 sm:p-2.5 rounded-xl transition-all duration-200 shrink-0", color)}>
        <Icon className="size-5 sm:size-5.5" aria-hidden="true" />
      </div>
      <span className="font-medium text-xs sm:text-sm text-foreground text-center">
        {label}
      </span>
      {description && (
        <span className="text-xs sm:text-xs text-muted-foreground text-center leading-tight">
          {description}
        </span>
      )}
    </button>
  )
}
