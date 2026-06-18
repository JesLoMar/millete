import type { ReactNode } from "react"
import { cn } from "./utils"

export interface DonutData {
  name: string
  value: number
  color: string
  percentage?: number
}

interface DonutChartProps {
  data: DonutData[]
  size?: number
  thickness?: number
  className?: string
  centerContent?: ReactNode
  onSegmentHover?: (item: DonutData) => void
  onSegmentLeave?: () => void
}

export function DonutChart({
  data,
  size = 160,
  thickness = 20,
  className,
  centerContent,
  onSegmentHover,
  onSegmentLeave,
}: DonutChartProps) {
  const radius = (size - thickness) / 2
  const circumference = 2 * Math.PI * radius
  const center = size / 2

  let accumulated = 0
  const segments = data.map((item) => {
    const percentage = item.percentage ?? item.value
    const length = (percentage / 100) * circumference
    const offset = accumulated
    accumulated += length
    return { ...item, length, offset }
  })

  return (
    <div className={cn("relative inline-flex items-center justify-center", className)} style={{ width: size, height: size }}>
      <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} className="-rotate-90">
        {/* Anillo de fondo (hueco vacío) */}
        <circle
          cx={center}
          cy={center}
          r={radius}
          fill="none"
          stroke="hsl(var(--muted))"
          strokeWidth={thickness}
          className="opacity-30"
        />

        {/* Segmentos */}
        {segments.map((segment) => (
          <circle
            key={segment.name}
            cx={center}
            cy={center}
            r={radius}
            fill="none"
            stroke={segment.color}
            strokeWidth={thickness}
            strokeDasharray={`${segment.length} ${circumference - segment.length}`}
            strokeDashoffset={-segment.offset}
            className="transition-all duration-300 hover:opacity-80 cursor-pointer"
            onMouseEnter={() => onSegmentHover?.(segment)}
            onMouseLeave={() => onSegmentLeave?.()}
          />
        ))}
      </svg>
      {centerContent && (
        <div className="absolute inset-0 flex items-center justify-center pointer-events-none" style={{ fontFamily: "var(--font-sans)" }}>
          {centerContent}
        </div>
      )}
    </div>
  )
}