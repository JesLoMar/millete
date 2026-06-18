import { useState } from "react"
import { useTranslation } from "react-i18next"
import { Card, CardContent, CardHeader, CardTitle } from "@/shared/components/core/card"
import { SimpleBarChart } from "@/shared/components/core/simple-bar-chart"
import { ChartTooltip } from "@/shared/components/core/chart-tooltip"
import type { PeriodFilter } from "@/shared/components/PeriodSelector"
import type { ChartDataPoint } from "../types"
import { formatCurrency } from "@/shared/utils/i18nFormat"

interface HistoryChartProps {
  period?: PeriodFilter
  data?: ChartDataPoint[]
  loading?: boolean
}

const MAX_BARS = 12

export function HistoryChart({
  period = "month",
  data: externalData,
  loading = false,
}: HistoryChartProps) {
  const { t } = useTranslation()
  const [tooltip, setTooltip] = useState<{ label: string; value: string; color: string } | null>(null)

  const chartData = externalData || []
  const isTruncated = chartData.length > MAX_BARS
  const displayData = isTruncated ? chartData.slice(-MAX_BARS) : chartData

  const barData = displayData.map((d) => ({
    label: d.label,
    value: d.amount,
  }))

  if (loading) {
    return (
      <Card className="col-span-1 md:col-span-8 border-subtle">
        <CardHeader className="flex flex-row items-center justify-between pb-4">
          <div className="h-6 w-40 bg-muted rounded animate-pulse" />
          <div className="h-6 w-32 bg-muted rounded-full animate-pulse" />
        </CardHeader>
        <CardContent className="h-75 w-full pt-2">
          <div className="h-full w-full bg-muted/20 rounded animate-pulse" />
        </CardContent>
      </Card>
    )
  }

  if (displayData.length === 0) {
    return (
      <Card className="col-span-1 md:col-span-8 border-subtle">
        <CardHeader className="flex flex-row items-center justify-between pb-4">
          <CardTitle className="text-lg font-headline font-bold">
            {t("dashboard.chart.title")}
          </CardTitle>
          <div className="flex items-center gap-2 text-xs text-muted-foreground bg-accent/30 px-3 py-1 rounded-full">
            <span>{t(`dashboard.chart.periodLabel.${period}`)}</span>
          </div>
        </CardHeader>
        <CardContent className="h-75 w-full flex items-center justify-center">
          <p className="text-sm text-muted-foreground">
            {t("dashboard.chart.empty")}
          </p>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card className="col-span-1 md:col-span-8 border-subtle">
      <CardHeader className="flex flex-row items-center justify-between pb-4">
        <CardTitle className="text-lg font-headline font-bold">
          {t("dashboard.chart.title")}
        </CardTitle>
        <div className="flex items-center gap-2 text-xs text-muted-foreground bg-accent/30 px-3 py-1 rounded-full">
          <span>{t(`dashboard.chart.periodLabel.${period}`)}</span>
          {isTruncated && (
            <span className="text-amber-400">
              {t("dashboard.chart.showingLast", { count: MAX_BARS })}
            </span>
          )}
        </div>
      </CardHeader>
      <CardContent className="h-75 w-full pt-2">
        <ChartTooltip data={tooltip}>
          <SimpleBarChart
            data={barData}
            height={260}
            showGrid
            showLabels
            labelAngle={displayData.length > 7 ? -45 : 0}
            formatValue={(v) => formatCurrency(v)}
            onBarHover={(item) =>
              setTooltip(
                item
                  ? { label: item.label, value: formatCurrency(item.value), color: item.color ?? "hsl(var(--chart-1))" }
                  : null
              )
            }
          />
        </ChartTooltip>
      </CardContent>
    </Card>
  )
}