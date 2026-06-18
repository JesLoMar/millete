// src/shared/components/core/chart-tooltip.tsx
import { useEffect, useRef, useState, type ReactNode } from "react"
import { cn } from "./utils"

interface TooltipData {
  label: string
  value: string
  color?: string
}

interface ChartTooltipProps {
  children: ReactNode
  data: TooltipData | null
  className?: string
}

export function ChartTooltip({ children, data, className }: ChartTooltipProps) {
  const [open, setOpen] = useState(false)
  const triggerRef = useRef<HTMLDivElement>(null)
  const popoverRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (data) {
      setOpen(true)
      // Cierra automáticamente después de 2s
      const timer = setTimeout(() => setOpen(false), 2000)
      return () => clearTimeout(timer)
    } else {
      setOpen(false)
    }
  }, [data])

  if (!data) return <>{children}</>

  return (
    <div ref={triggerRef} className="relative">
      {children}
      {open && (
        <div
          ref={popoverRef}
          popover="auto"
          className={cn(
            "absolute z-50 rounded-lg border border-border/50 bg-background px-2.5 py-1.5 text-xs shadow-xl",
            "animate-in fade-in-0 zoom-in-95",
            "left-1/2 -translate-x-1/2 -top-2 -translate-y-full",
            className
          )}
        >
          <div className="flex items-center gap-2">
            {data.color && (
              <span className="size-2.5 rounded-full shrink-0" style={{ backgroundColor: data.color }} />
            )}
            <span className="text-muted-foreground">{data.label}</span>
            <span className="font-semibold tabular-nums">{data.value}</span>
          </div>
        </div>
      )}
    </div>
  )
}