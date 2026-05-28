import { useTranslation } from "react-i18next"
import { Card, CardContent, CardHeader, CardTitle } from "@/shared/components/ui/card"
import type { ChartConfig } from "@/shared/components/ui/chart"
import { ChartContainer, ChartTooltip, ChartTooltipContent } from "@/shared/components/ui/chart"
import { Pie, PieChart, Cell, ResponsiveContainer } from "recharts"
import type { CategoryData } from "../types"

interface CategoryDonutProps {
  data?: CategoryData[]
  loading?: boolean
  title?: string
}

const chartConfig = {
  value: {
    label: "Porcentaje",
  },
} satisfies ChartConfig

export function CategoryDonut({
  data: externalData,
  loading = false,
  title,
}: CategoryDonutProps) {
  const { t } = useTranslation()
  const chartData = externalData || []

  if (loading) {
    return (
      <Card className="col-span-1 md:col-span-4 border-subtle">
        <CardHeader>
          <div className="h-6 w-36 bg-muted rounded animate-pulse" />
        </CardHeader>
        <CardContent className="h-75 flex flex-col items-center justify-center pt-0">
          <div className="size-40 rounded-full bg-muted/20 animate-pulse" />
          <div className="grid grid-cols-2 gap-x-4 gap-y-2 w-full mt-4">
            {[1, 2, 3, 4].map((i) => (
              <div key={`skeleton-${i}`} className="flex items-center gap-2">
                <div className="size-2 rounded-full bg-muted animate-pulse" />
                <div className="h-3 w-20 bg-muted rounded animate-pulse" />
                <div className="h-3 w-8 bg-muted rounded animate-pulse ml-auto" />
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    )
  }

  if (chartData.length === 0) {
    return (
      <Card className="col-span-1 md:col-span-4 border-subtle">
        <CardHeader>
          <CardTitle className="text-lg font-headline font-bold">
            {title || t("dashboard.donut.title")}
          </CardTitle>
        </CardHeader>
        <CardContent className="h-75 flex items-center justify-center">
          <p className="text-sm text-muted-foreground">
            {t("dashboard.donut.empty")}
          </p>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card className="col-span-1 md:col-span-4 border-subtle">
      <CardHeader>
        <CardTitle className="text-lg font-headline font-bold">
          {title || t("dashboard.donut.title")}
        </CardTitle>
      </CardHeader>
      <CardContent className="flex flex-col items-center justify-center pt-0">
        <div className="w-full h-55">
          <ChartContainer config={chartConfig} className="h-full w-full">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <ChartTooltip
                  content={
                    <ChartTooltipContent
                      formatter={(value, name) => [`${value}%`, name]}
                    />
                  }
                />
                <Pie
                  data={chartData}
                  dataKey="value"
                  nameKey="category"
                  innerRadius={55}
                  outerRadius={75}
                  paddingAngle={3}
                  stroke="none"
                  cornerRadius={3}
                >
                  {chartData.map((entry) => (
                    <Cell
                      key={entry.category}
                      fill={entry.color}
                      className="hover:opacity-80 transition-opacity cursor-pointer"
                    />
                  ))}
                </Pie>
              </PieChart>
            </ResponsiveContainer>
          </ChartContainer>
        </div>

        {/* Leyenda debajo del gráfico original */}
        <div className="grid grid-cols-2 gap-x-4 gap-y-1.5 w-full mt-4">
          {chartData.map((item) => (
            <div
              key={item.category}
              className="flex items-center gap-2 group cursor-pointer"
              title={`${item.category}: ${item.value}%`}
            >
              <div
                className="size-2.5 rounded-full shrink-0 group-hover:scale-125 transition-transform"
                style={{ backgroundColor: item.color }}
              />
              <span className="text-[11px] text-muted-foreground truncate group-hover:text-foreground transition-colors">
                {item.category}
              </span>
              <span className="text-[11px] font-semibold ml-auto tabular-nums">
                {item.value}%
              </span>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  )
}