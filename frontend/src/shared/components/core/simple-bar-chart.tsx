import { cn } from "./utils"

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
    labelAngle?: number
    formatValue?: (value: number) => string
    className?: string
    onBarHover?: (item: BarData | null) => void
}

export function SimpleBarChart({
    data,
    height = 260,
    barColor = "hsl(var(--chart-1))",
    showGrid = true,
    showLabels = true,
    labelAngle = 0,
    formatValue = (v) => v.toString(),
    className,
    onBarHover,
}: SimpleBarChartProps) {
    const maxValue = Math.max(...data.map((d) => d.value), 1)
    const barCount = data.length
    const paddingBottom = 30
    const chartAreaHeight = height - paddingBottom
    const minBarHeightForInnerLabel = 36

    const barGapRatio = 0.4
    const gapWidth = (100 * barGapRatio) / (barCount + 1)
    const barWidth = (100 * (1 - barGapRatio)) / barCount

    return (
        <div className={cn("w-full relative", className)} style={{ height }}>
            <svg width="100%" height={height} className="w-full">
                {/* Grid horizontal */}
                {showGrid &&
                    [0, 0.25, 0.5, 0.75, 1].map((line) => {
                        const y = chartAreaHeight * (1 - line)
                        return (
                            <line
                                key={line}
                                x1="0%"
                                y1={y}
                                x2="100%"
                                y2={y}
                                stroke="hsla(var(--border), 0.5)"
                                strokeWidth={1}
                                strokeDasharray="3 3"
                            />
                        )
                    })}

                {/* Barras */}
                {data.map((item, i) => {
                    const barHeight = (item.value / maxValue) * chartAreaHeight
                    const xPercent = gapWidth + i * (barWidth + gapWidth)
                    const y = chartAreaHeight - barHeight
                    const formattedValue = formatValue(item.value)
                    const showInnerLabel = barHeight >= minBarHeightForInnerLabel

                    return (
                        <g key={item.label}>
                            <rect
                                x={`${xPercent}%`}
                                y={y}
                                width={`${barWidth}%`}
                                height={barHeight}
                                fill={item.color ?? barColor}
                                rx={4}
                                className="hover:opacity-80 transition-opacity cursor-pointer"
                                onMouseEnter={() =>
                                    onBarHover?.({
                                        label: item.label,
                                        value: item.value,
                                        color: item.color ?? barColor,
                                    })
                                }
                                onMouseLeave={() => onBarHover?.(null)}
                            />

                            {/* Valor dentro de la barra */}
                            {showInnerLabel && (
                                <text
                                    x={`${xPercent + barWidth / 2}%`}
                                    y={y + barHeight / 2 + 5}
                                    textAnchor="middle"
                                    fill="white"
                                    fontSize={14}
                                    fontWeight={700}
                                    fontFamily="var(--font-sans)"
                                    className="pointer-events-none"
                                >
                                    {formattedValue}
                                </text>
                            )}

                            {/* Valor encima de la barra */}
                            {!showInnerLabel && barHeight > 0 && (
                                <text
                                    x={`${xPercent + barWidth / 2}%`}
                                    y={y - 6}
                                    textAnchor="middle"
                                    fill="hsl(var(--foreground))"
                                    fontSize={12}
                                    fontWeight={600}
                                    fontFamily="var(--font-sans)"
                                    className="pointer-events-none"
                                >
                                    {formattedValue}
                                </text>
                            )}

                            {/* Etiqueta debajo */}
                            {showLabels && (
                                <text
                                    x={`${xPercent + barWidth / 2}%`}
                                    y={height - 6}
                                    textAnchor="middle"
                                    fill="hsl(var(--muted-foreground))"
                                    fontSize={13}
                                    fontWeight={500}
                                    fontFamily="var(--font-sans)"
                                >
                                    {item.label}
                                </text>
                            )}
                        </g>
                    )
                })}
            </svg>
        </div>
    )
}
