import { useTranslation } from "react-i18next"
import { Card, CardContent, CardHeader, CardTitle } from "@/shared/components/ui/card"
import { ChartContainer, ChartTooltip, ChartTooltipContent, type ChartConfig } from "@/shared/components/ui/chart"
import { Pie, PieChart, Cell, ResponsiveContainer } from "recharts"
import type { DistributionResponse } from "../types"

interface DistributionChartProps {
  data: DistributionResponse | undefined
  isLoading: boolean
}

const chartConfig = {
  percentage: { label: "Porcentaje" },
} satisfies ChartConfig

function formatCurrency(value: number, lng: string): string {
  if (value >= 1_000_000) {
    return `${(value / 1_000_000).toFixed(1)}M`
  }
  if (value >= 1_000) {
    return `${(value / 1_000).toFixed(1)}k`
  }
  return value.toLocaleString(lng)
}

export function DistributionChart({ data, isLoading }: DistributionChartProps) {
  const { t, i18n } = useTranslation()

  const chartData = data?.distribution || []
  const totalValue = data?.totalValue || 0

  if (isLoading) {
    return (
      <Card className="border-subtle h-95">
        <CardContent className="h-full flex items-center justify-center">
          <div className="size-48 rounded-full bg-muted/20 animate-pulse" />
        </CardContent>
      </Card>
    )
  }

  return (
    <Card className="border-subtle h-95">
      <CardHeader>
        <CardTitle className="text-lg font-headline font-bold">
          {t("investments.distribution")}
        </CardTitle>
      </CardHeader>
      <CardContent className="flex flex-col items-center justify-center gap-4 h-75 pt-0">
        <div className="relative size-40 shrink-0">
          <ChartContainer config={chartConfig} className="w-full h-full">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie 
                  data={chartData} 
                  cx="50%" 
                  cy="50%" 
                  innerRadius={55} 
                  outerRadius={75} 
                  paddingAngle={5} 
                  dataKey="percentage"
                  nameKey="name" 
                  stroke="none"
                >
                  {chartData.map((entry) => (
                    <Cell key={entry.name} fill={entry.color} />
                  ))}
                </Pie>
                <ChartTooltip 
                  content={
                    <ChartTooltipContent 
                      formatter={(value) => [`${Number(value).toLocaleString(i18n.language)}%`]} 
                    />
                  } 
                />
              </PieChart>
            </ResponsiveContainer>
          </ChartContainer>
          <div className="absolute inset-0 flex items-center justify-center flex-col pointer-events-none">
            <span className="text-2xl font-bold text-foreground">{formatCurrency(totalValue, i18n.language)} €</span>
            <span className="text-[10px] uppercase tracking-widest text-muted-foreground font-bold">{t("investments.total")}</span>
          </div>
        </div>

        {/* Leyenda inferior */}
        <div className="w-full space-y-2 overflow-y-auto pr-1">
          {chartData.map((item) => (
            <div key={item.name} className="flex items-center justify-between text-sm">
              <div className="flex items-center gap-2">
                <span className="size-2.5 rounded-full shrink-0" style={{ backgroundColor: item.color }} />
                <span className="text-muted-foreground truncate max-w-[120px]">{item.name}</span>
              </div>
              <div className="flex items-center gap-2 font-semibold">
                <span className="text-muted-foreground text-xs tabular-nums">
                  ({item.value.toLocaleString(i18n.language)} €)
                </span>
                <span className="text-foreground tabular-nums">
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