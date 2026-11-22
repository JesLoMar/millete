import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import type { PeriodFilter } from '@/shared/components/PeriodSelector'
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/core/card'
import { formatCurrency } from '@/shared/utils/i18nFormat'

import type { HistoryPoint } from '../types'

interface HistoryChartProps {
  period: PeriodFilter
  data?: HistoryPoint[]
  loading?: boolean
}

const series = [
  { key: 'income', stroke: 'var(--color-primary)' },
  { key: 'expenses', stroke: 'var(--color-destructive)', dash: '6 4' },
] as const

const grid = [0, 25, 50, 75, 100]
const asDate = (value: string) => new Date(`${value}T00:00:00Z`)

export function HistoryChart({ period, data = [], loading }: HistoryChartProps) {
  const { i18n, t } = useTranslation('dashboard')
  const [selection, setSelection] = useState<string>()
  const dateLabel = (options: Intl.DateTimeFormatOptions) =>
    new Intl.DateTimeFormat(i18n.resolvedLanguage, { timeZone: 'UTC', ...options })
  const longDate = dateLabel({ dateStyle: 'medium' })
  const shortDate = dateLabel(period === 'year' ? { month: 'short' } : { day: 'numeric', month: 'short' })
  const rangeLabel = (point: HistoryPoint) =>
    longDate.formatRange(asDate(point.start), asDate(point.end))

  const active = data.find((point) => point.start === selection) ?? data.at(-1)
  const peak = Math.max(1, ...data.flatMap(({ income, expenses }) => [income, expenses]))
  const magnitude = 10 ** Math.floor(Math.log10(peak))
  const step = Math.ceil(peak / (4 * magnitude) * 10) * magnitude / 10
  const max = step * 4
  const x = (index: number) => ((index + 0.5) * 100) / data.length
  const y = (value: number) => 100 - (value / max) * 100
  const columns = { gridTemplateColumns: `repeat(${data.length}, minmax(0, 1fr))` }

  return (
    <Card className="col-span-1 rounded-none border-0 bg-transparent shadow-none md:col-span-8">
      <CardHeader className="flex flex-row flex-wrap items-center justify-between gap-x-3 gap-y-2 pb-3">
        <CardTitle className="font-serif text-lg font-bold">{t('chart.title')}</CardTitle>
        <span className="rounded-full bg-accent/30 px-3 py-1 text-xs text-muted-foreground">
          {t(`chart.periodLabel.${period}`)}
        </span>
      </CardHeader>
      <CardContent className="min-h-75 min-w-0 pt-2" aria-busy={loading}>
        {loading ? (
          <div className="h-64 animate-pulse rounded bg-muted/20 motion-reduce:animate-none" />
        ) : active ? (
          <figure className="min-w-0 space-y-4">
            <figcaption className="flex flex-wrap items-baseline gap-x-5 gap-y-1 text-xs tabular-nums">
              <span className="basis-full font-medium text-muted-foreground">{rangeLabel(active)}</span>
              {series.map((line) => (
                <span key={line.key} className="inline-flex items-center gap-2">
                  <svg aria-hidden="true" width="20" height="8">
                    <path d="M0 4h20" stroke={line.stroke} strokeWidth="2"
                      strokeDasharray={'dash' in line ? line.dash : undefined} />
                  </svg>
                  <span className="text-muted-foreground">{t(`chart.${line.key}`)}</span>
                  <strong>{formatCurrency(active[line.key])}</strong>
                </span>
              ))}
            </figcaption>
            <div className="grid min-w-0 grid-cols-[auto_minmax(0,1fr)] gap-x-2 sm:gap-x-3">
              <div aria-hidden="true" className="mr-1 flex h-44 min-w-10 flex-col justify-between text-right text-[10px] text-muted-foreground tabular-nums sm:h-52">
                {grid.map((percent) => (
                  <div key={percent} className="h-0 whitespace-nowrap">
                    <span className="block -translate-y-1/2">
                      {formatCurrency(max * (1 - percent / 100), undefined, { compact: true, minimumFractionDigits: 0 })}
                    </span>
                  </div>
                ))}
              </div>
              <div className="relative h-44 min-w-0 sm:h-52">
                <svg aria-hidden="true" className="absolute inset-0 size-full overflow-visible" viewBox="0 0 100 100" preserveAspectRatio="none">
                  {grid.map((percent) => (
                    <path key={percent} d={`M0 ${percent}H100`} className="stroke-border" vectorEffect="non-scaling-stroke" />
                  ))}
                  <path d={`M${x(data.indexOf(active))} 0V100`} className="stroke-muted-foreground/50" vectorEffect="non-scaling-stroke" />
                  {series.map((line) => (
                    <polyline key={line.key} fill="none" stroke={line.stroke} strokeWidth="2.5"
                      strokeDasharray={'dash' in line ? line.dash : undefined}
                      strokeLinecap="round" strokeLinejoin="round" vectorEffect="non-scaling-stroke"
                      points={data.map((point, i) => `${x(i)},${y(point[line.key])}`).join(' ')} />
                  ))}
                </svg>
                <div className="absolute inset-0 grid" style={columns}>
                  {data.map((point) => (
                    <button key={point.start} type="button"
                      aria-label={`${rangeLabel(point)}. ${series.map(({ key }) => `${t(`chart.${key}`)}: ${formatCurrency(point[key])}`).join('. ')}`}
                      aria-pressed={point === active}
                      className="min-w-0 cursor-crosshair rounded-sm focus-visible:outline-2 focus-visible:outline-ring"
                      onPointerEnter={() => setSelection(point.start)}
                      onFocus={() => setSelection(point.start)}
                      onClick={() => setSelection(point.start)} />
                  ))}
                </div>
              </div>
              <div aria-hidden="true" className="col-start-2 mt-2 grid text-center text-[10px] text-muted-foreground sm:text-xs" style={columns}>
                {data.map((point, index) => (
                  <span key={point.start} className={data.length > 7 && index % 2 !== 0 && index !== data.length - 1 ? 'invisible truncate sm:visible' : 'truncate'}>
                    {shortDate.format(asDate(point.start))}
                  </span>
                ))}
              </div>
            </div>
          </figure>
        ) : (
          <p className="grid min-h-64 place-items-center text-sm text-muted-foreground">{t('chart.empty')}</p>
        )}
      </CardContent>
    </Card>
  )
}
