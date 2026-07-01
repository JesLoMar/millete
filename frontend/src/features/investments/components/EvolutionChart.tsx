import { useMemo, useState } from "react"
import { useTranslation } from "react-i18next"
import { Card, CardContent, CardHeader, CardTitle } from "@/shared/components/core/card"
import { SimpleBarChart } from "@/shared/components/core/simple-bar-chart"
import { ChartTooltip } from "@/shared/components/core/chart-tooltip"
import type { EvolutionResponse } from "../types"

interface EvolutionChartProps {
  data: EvolutionResponse | undefined
  isLoading: boolean
}

export function EvolutionChart({ data: response, isLoading }: EvolutionChartProps) {
  const { t, i18n } = useTranslation()
  const [tooltip, setTooltip] = useState<{ label: string; value: string; color: string } | null>(null)

  const barData = useMemo(() => {
    if (!response?.labels) return []
    return response.labels.map((label, i) => ({
      label,
      value: response.data[i] || 0,
    }))
  }, [response])

  if (isLoading) {
    return (
      <Card className="border h-95">
        <CardContent className="h-full flex items-center justify-center">
          <div className="h-full w-full bg-muted/20 rounded animate-pulse" />
        </CardContent>
      </Card>
    )
  }

  return (
    <Card className="border h-95">
      <CardHeader className="flex flex-row items-center justify-between pb-2">
        <CardTitle className="text-lg font-serif font-bold">{t('investments:evolution')}</CardTitle>
        <span
          className="text-xs text-muted-foreground bg-accent/30 px-3 py-1 rounded-full"
        >
          {t("investments:lastMonths", { count: barData.length })}
        </span>
      </CardHeader>
      <CardContent className="h-75 w-full pt-2">
        <ChartTooltip data={tooltip}>
          <SimpleBarChart
            data={barData}
            height={260}
            barColor="hsl(var(--chart-1))"
            showGrid={false}
            showLabels
            formatValue={(v) => `${v.toLocaleString(i18n.language)} €`}
            onBarHover={(item) =>
              setTooltip(
                item
                  ? { label: item.label, value: `${item.value.toLocaleString(i18n.language)} €`, color: item.color ?? "hsl(var(--chart-1))" }
                  : null
              )
            }
          />
        </ChartTooltip>
      </CardContent>
    </Card>
  )
}