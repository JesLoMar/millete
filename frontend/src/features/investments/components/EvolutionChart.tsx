import { useMemo } from "react"
import { useTranslation } from "react-i18next"
import { Card, CardContent, CardHeader, CardTitle } from "@/shared/components/ui/card"
import { ChartContainer, ChartTooltip, ChartTooltipContent } from "@/shared/components/ui/chart"
import { Bar, BarChart, XAxis, ResponsiveContainer } from "recharts"
import type { EvolutionResponse } from "../types"

interface ChartDataPoint {
  label: string
  value: number
}

interface EvolutionChartProps {
  data: EvolutionResponse | undefined
  isLoading: boolean
}

const chartConfig = {
  value: { label: "Patrimonio", color: "hsl(var(--chart-1))" },
}

export function EvolutionChart({ data: response, isLoading }: EvolutionChartProps) {
  const { t, i18n } = useTranslation()

  const chartData: ChartDataPoint[] = useMemo(() => {
    if (!response?.labels) return []
    return response.labels.map((label, i) => ({ label, value: response.data[i] || 0 }))
  }, [response])

  const barSize = chartData.length <= 7 ? 40 : chartData.length <= 12 ? 30 : 20

  if (isLoading) {
    return (
      <Card className="border-subtle h-95">
        <CardContent className="h-full flex items-center justify-center">
          <div className="h-full w-full bg-muted/20 rounded animate-pulse" />
        </CardContent>
      </Card>
    )
  }

  return (
    <Card className="border-subtle h-95">
      <CardHeader className="flex flex-row items-center justify-between pb-2">
        <CardTitle className="text-lg font-headline font-bold">{t("investments.evolution")}</CardTitle>
        <span className="text-xs text-muted-foreground bg-accent/30 px-3 py-1 rounded-full">
          {t("investments.lastMonths", { count: chartData.length })}
        </span>
      </CardHeader>
      <CardContent className="h-75 w-full pt-2">
        <div className="w-full h-full min-h-65">
          <ChartContainer config={chartConfig} className="h-full w-full">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData} margin={{ top: 10, right: 10, left: 0, bottom: 20 }}>
                <XAxis dataKey="label" axisLine={false} tickLine={false} tick={{ fill: "hsl(var(--muted-foreground))", fontSize: 12 }} dy={5} />
                <ChartTooltip 
                  content={
                    <ChartTooltipContent 
                      hideLabel 
                      formatter={(value) => [`${Number(value).toLocaleString(i18n.language)} €`]}
                    />
                  } 
                  cursor={{ fill: "rgba(255,255,255,0.03)" }} 
                />
                <Bar dataKey="value" radius={[4, 4, 0, 0]} barSize={barSize} fill="var(--color-value)" />
              </BarChart>
            </ResponsiveContainer>
          </ChartContainer>
        </div>
      </CardContent>
    </Card>
  )
}