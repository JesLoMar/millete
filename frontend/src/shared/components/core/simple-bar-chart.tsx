// src/shared/components/core/simple-bar-chart.tsx
import { useState, useRef, useCallback } from "react"
import { cn } from "./utils"
import { ChartTooltip } from "./chart-tooltip"

interface BarData {
  label: string
  value: number
  color?: string
}

interface SimpleBarChartProps {
  data: BarData[]
  height?: number
  barColor?: string
  showGrid?: boolean
  showLabels?: boolean
  formatValue?: (value: number) => string
  className?: string
}

export function SimpleBarChart({
  data,
  height = 200,
  barColor = "hsl(var(--chart-1))",
  showGrid = true,
  showLabels = true,
  formatValue = (v) => v.toString(),
  className,
}: SimpleBarChartProps) {
  const [tooltip, setTooltip] = useState<{ label: string; value: string; color: string } | null>(null)
  const containerRef = useRef<HTMLDivElement>(null)

  const maxValue = Math.max(...data.map((d) => d.value), 1)
  const barWidth = 100 / data.length
  const padding = 10

  const handleBarHover = useCallback(
    (item: BarData, e: React.MouseEvent) => {
      const rect = containerRef.current?.getBoundingClientRect()
      if (!rect) return
      setTooltip({
        label: item.label,
        value: formatValue(item.value),
        color: item.color ?? barColor,
      })
    },
    [barColor, formatValue]
  )

  return (
    <div ref={containerRef} className={cn("relative w-full", className)} style={{ height }}>
      <ChartTooltip data={tooltip}>
        <svg
          width="100%"
          height={height}
          viewBox={`0 0 ${data.length * (barWidth + padding)} ${height}`}
          preserveAspectRatio="none"
          className="w-full h-full"
        >
          {/* Grid horizontal */}
          {showGrid &&
            [0, 0.25, 0.5, 0.75, 1].map((line) => (
              <line
                key={line}
                x1={0}
                y1={height - line * height}
                x2={data.length * (barWidth + padding)}
                y2={height - line * height}
                stroke="hsl(var(--border))"
                strokeWidth={0.5}
                strokeDasharray="3 3"
              />
            ))}

          {/* Barras */}
          {data.map((item, i) => {
            const barHeight = (item.value / maxValue) * (height - 30)
            const x = i * (barWidth + padding) + padding / 2
            const y = height - barHeight - 20

            return (
              <g key={item.label}>
                <rect
                  x={x}
                  y={y}
                  width={barWidth - padding}
                  height={barHeight}
                  fill={item.color ?? barColor}
                  rx={4}
                  className="hover:opacity-80 transition-opacity cursor-pointer"
                  onMouseEnter={(e) => handleBarHover(item, e)}
                  onMouseLeave={() => setTooltip(null)}
                />
                {showLabels && (
                  <text
                    x={x + (barWidth - padding) / 2}
                    y={height - 5}
                    textAnchor="middle"
                    fill="hsl(var(--muted-foreground))"
                    fontSize={11}
                  >
                    {item.label}
                  </text>
                )}
              </g>
            )
          })}
        </svg>
      </ChartTooltip>
    </div>
  )
}