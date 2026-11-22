import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import type { CategoryData } from '../types'

import { ChartTooltip } from '@/shared/components/core/chart-tooltip'
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/core/card'
import { DonutChart } from '@/shared/components/core/donut-chart'

interface CategoryDonutProps {
  data?: CategoryData[]
  loading?: boolean
  title?: string
  emptyText?: string
}

const panelClass = 'h-full min-h-80 rounded-none border-0 bg-transparent shadow-none'
const ringSize = 'size-[clamp(9rem,min(34svh,64vw),22rem)]!'

export function CategoryDonut({
  data: externalData,
  loading = false,
  title,
  emptyText,
}: CategoryDonutProps) {
  const { t } = useTranslation('dashboard');

  const chartData = externalData || []

  const [activeItem, setActiveItem] = useState<{
    label: string
    value: string
    color: string
  } | null>(null)

  if (loading) {
    return (
      <Card className={panelClass}>
        <CardHeader>
          <div className="h-6 w-36 bg-muted rounded animate-pulse" />
        </CardHeader>

        <CardContent className="flex flex-1 flex-col items-center justify-center pt-0">
          <div className={`${ringSize} max-w-full animate-pulse rounded-full bg-muted/20 motion-reduce:animate-none`} />

          <div className="grid grid-cols-2 gap-x-4 gap-y-2 w-full mt-4">
            {[1, 2, 3, 4].map((i) => (
              <div
                key={`skeleton-${i}`}
                className="flex items-center gap-2"
              >
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
      <Card className={panelClass}>
        <CardHeader>
          <CardTitle className="text-lg font-serif font-bold">
            {title || t('dashboard:donut.title')}
          </CardTitle>
        </CardHeader>

        <CardContent className="flex flex-1 items-center justify-center">
          <p className="text-sm text-muted-foreground">
            {emptyText ?? t('dashboard:donut.empty')}
          </p>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card className={panelClass}>
      <CardHeader>
        <CardTitle className="text-lg font-serif font-bold">
          {title || t('dashboard:donut.title')}
        </CardTitle>
      </CardHeader>

      <CardContent className="flex flex-1 flex-col items-center justify-center pt-0">
        <ChartTooltip data={activeItem}>
          <DonutChart
            data={chartData.map((item) => ({
              name: item.category,
              value: item.value,
              color: item.color,
              percentage: item.value,
            }))}
            size={240}
            thickness={32}
            className={`${ringSize} max-w-full hover:cursor-pointer [&_svg]:size-full`}
            onSegmentHover={(item) =>
              setActiveItem({
                label: item.name,
                value: `${item.value}%`,
                color: item.color,
              })
            }
            onSegmentLeave={() => setActiveItem(null)}
          />
        </ChartTooltip>

        <div className="grid grid-cols-2 gap-x-4 gap-y-1.5 w-full mt-4">
          {chartData.map((item) => (
            <div
              key={item.category}
              className="flex items-center gap-2 group cursor-pointer"
              onMouseEnter={() =>
                setActiveItem({
                  label: item.category,
                  value: `${item.value}%`,
                  color: item.color,
                })
              }
              onMouseLeave={() => setActiveItem(null)}
            >
              <div
                className="size-2.5 rounded-full shrink-0 group-hover:scale-125 transition-transform"
                style={{ backgroundColor: item.color }}
              />

              <span className="text-xs text-muted-foreground truncate group-hover:text-foreground transition-colors">
                {item.category}
              </span>

              <span className="text-xs font-semibold ml-auto tabular-nums">
                {item.value}%
              </span>
            </div>
          ))}
        </div>
      </CardContent>
    </Card>
  )
}
