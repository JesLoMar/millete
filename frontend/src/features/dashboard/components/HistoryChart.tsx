import { useTranslation } from "react-i18next"
import { Card, CardContent, CardHeader, CardTitle } from "@/shared/components/ui/card"
import type { ChartConfig } from "@/shared/components/ui/chart"
import { ChartContainer, ChartTooltip, ChartTooltipContent } from "@/shared/components/ui/chart"
import { Bar, BarChart, CartesianGrid, XAxis, ResponsiveContainer } from "recharts"
import type { PeriodFilter } from "@/shared/components/PeriodSelector"
import type { ChartDataPoint } from "../types"
import { formatCurrency } from '@/shared/utils/i18nFormat';

interface HistoryChartProps {
  period?: PeriodFilter
  data?: ChartDataPoint[]
  loading?: boolean
}

const MAX_BARS = 12

const chartConfig = {
  amount: {
    label: "Gasto",
    color: "hsl(var(--chart-1))",
  },
} satisfies ChartConfig

export function HistoryChart({
  period = "month",
  data: externalData,
  loading = false,
}: HistoryChartProps) {
  const { t } = useTranslation()

  const chartData = externalData || []
  const isTruncated = chartData.length > MAX_BARS
  const displayData = isTruncated ? chartData.slice(-MAX_BARS) : chartData

  const barSize = displayData.length <= 7 ? 40 : displayData.length <= 12 ? 30 : 20

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
        <div className="w-full h-full min-h-65">
          <ChartContainer config={chartConfig} className="h-full w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={displayData} margin={{ top: 10, right: 10, left: 0, bottom: 20 }}>
                <CartesianGrid vertical={false} strokeDasharray="3 3" stroke="hsla(var(--border), 0.5)" />
                <XAxis
                  dataKey="label"
                  tickLine={false}
                  tickMargin={10}
                  axisLine={false}
                  tick={{ fill: "hsl(var(--muted-foreground))", fontSize: 12 }}
                  interval={0}
                  angle={displayData.length > 7 ? -45 : 0}
                  textAnchor={displayData.length > 7 ? "end" : "middle"}
                  height={displayData.length > 7 ? 60 : 30}
                />
                <ChartTooltip
                  content={
                    <ChartTooltipContent
                      formatter={(value) => formatCurrency(Number(value))}
                    />
                  }
                />
                <Bar
                  dataKey="amount"
                  fill="var(--color-amount)"
                  radius={[4, 4, 0, 0]}
                  barSize={barSize}
                />
              </BarChart>
            </ResponsiveContainer>
          </ChartContainer>
        </div>
      </CardContent>
    </Card>
  )
}