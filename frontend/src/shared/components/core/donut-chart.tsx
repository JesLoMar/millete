// src/shared/components/core/donut-chart.tsx
import { cn } from "./utils"

interface DonutData {
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
  centerContent?: React.ReactNode
}

export function DonutChart({ data, size = 160, thickness = 20, className, centerContent }: DonutChartProps) {
  const radius = (size - thickness) / 2
  const circumference = 2 * Math.PI * radius
  const center = size / 2

  // Calcula segmentos
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
          />
        ))}
      </svg>
      {centerContent && (
        <div className="absolute inset-0 flex items-center justify-center">
          {centerContent}
        </div>
      )}
    </div>
  )
}