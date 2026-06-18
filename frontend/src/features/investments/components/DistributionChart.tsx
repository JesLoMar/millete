import { useState } from "react"
import { useTranslation } from "react-i18next"
import { Card, CardContent, CardHeader, CardTitle } from "@/shared/components/core/card"
import { DonutChart } from "@/shared/components/core/donut-chart"
import { ChartTooltip } from "@/shared/components/core/chart-tooltip"
import type { DistributionResponse } from "../types"

interface DistributionChartProps {
  data: DistributionResponse | undefined
  isLoading: boolean
}

function formatCurrency(value: number, lng: string): string {
  if (value >= 1_000_000) return `${(value / 1_000_000).toFixed(1)}M`
  if (value >= 1_000) return `${(value / 1_000).toFixed(1)}k`
  return value.toLocaleString(lng)
}

export function DistributionChart({ data, isLoading }: DistributionChartProps) {
  const { t, i18n } = useTranslation()
  const [tooltip, setTooltip] = useState<{ label: string; value: string; color: string } | null>(null)

  const chartData = data?.distribution || []
  const totalValue = data?.totalValue || 0

  if (isLoading) {
    return (
      <Card className="border-subtle">
        <CardHeader>
          <div className="h-6 w-44 bg-muted rounded animate-pulse" />
        </CardHeader>
        <CardContent className="flex flex-col items-center justify-center gap-4 pt-0 min-h-100">
          <div className="relative size-32 sm:size-40 shrink-0">
            <div className="size-full rounded-full bg-muted/20 animate-pulse" />
          </div>
          <div className="w-full space-y-2">
            {[1, 2, 3, 4].map((i) => (
              <div key={`skeleton-${i}`} className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <div className="size-2.5 rounded-full bg-muted animate-pulse" />
                  <div className="h-4 w-20 bg-muted rounded animate-pulse" />
                </div>
                <div className="h-4 w-16 bg-muted rounded animate-pulse" />
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card className="border-subtle">
      <CardHeader>
        <CardTitle className="text-lg font-headline font-bold">
          {t('investments:distribution')}
        </CardTitle>
      </CardHeader>
      <CardContent className="flex flex-col items-center justify-center gap-4 pt-0 min-h-100">
        <ChartTooltip data={tooltip}>
          <DonutChart
            data={chartData.map((item) => ({
              name: item.name,
              value: item.percentage,
              color: item.color,
              percentage: item.percentage,
            }))}
            size={180}
            thickness={28}
            centerContent={
              <div className="text-center pointer-events-none" style={{ fontFamily: "var(--font-sans)" }}>
                <div className="text-lg font-bold tabular-nums">
                  {formatCurrency(totalValue, i18n.language)} €
                </div>
                <div className="text-[9px] uppercase tracking-widest text-muted-foreground font-bold">
                  {t('investments:total')}
                </div>
              </div>
            }
            onSegmentHover={(item) =>
              setTooltip({
                label: item.name,
                value: `${item.percentage ?? item.value}%`,
                color: item.color,
              })
            }
            onSegmentLeave={() => setTooltip(null)}
          />
        </ChartTooltip>

        <div className="w-full space-y-1.5 sm:space-y-2 max-h-50 overflow-y-auto pr-1">
          {chartData.map((item) => (
            <div
              key={item.name}
              className="flex items-center justify-between text-xs sm:text-sm gap-2 cursor-pointer"
              onMouseEnter={() =>
                setTooltip({
                  label: item.name,
                  value: `${item.percentage}%`,
                  color: item.color,
                })
              }
              onMouseLeave={() => setTooltip(null)}
            >
              <div className="flex items-center gap-1.5 sm:gap-2 min-w-0">
                <span className="size-2 sm:size-2.5 rounded-full shrink-0" style={{ backgroundColor: item.color }} />
                <span className="text-muted-foreground truncate" style={{ fontFamily: "var(--font-sans)" }}>
                  {item.name}
                </span>
              </div>
              <div className="flex items-center gap-1.5 sm:gap-2 font-semibold shrink-0">
                <span
                  className="text-muted-foreground text-[10px] sm:text-xs tabular-nums hidden xs:inline"
                  style={{ fontFamily: "var(--font-sans)" }}
                >
                  ({item.value.toLocaleString(i18n.language)} €)
                </span>
                <span className="text-foreground tabular-nums text-xs sm:text-sm" style={{ fontFamily: "var(--font-sans)" }}>
                  {item.percentage.toLocaleString(i18n.language)}%
                </span>
              </div>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  )
}
