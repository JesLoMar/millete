import { useState } from 'react'
import { useTranslation } from 'react-i18next'

import type { PeriodFilter } from '@/shared/components/PeriodSelector'
import { Card, CardContent } from '@/shared/components/core/card'
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

function differenceBands(data: HistoryPoint[], x: (index: number) => number, y: (value: number) => number) {
  return data.slice(1).flatMap((right, index) => {
    const left = data[index]
    const first = left.income - left.expenses
    const last = right.income - right.expenses
    if (first === 0 && last === 0) return []

    const a = x(index)
    const b = x(index + 1)
    const band = (difference: number, points: string, part: number) => ({
      key: `${left.start}-${part}`,
      type: difference > 0 ? 'income' : 'expenses',
      points,
    })
    const leftIncome = `${a},${y(left.income)}`
    const leftExpenses = `${a},${y(left.expenses)}`
    const rightIncome = `${b},${y(right.income)}`
    const rightExpenses = `${b},${y(right.expenses)}`
    if (first * last >= 0) {
      return [band(first || last, `${leftIncome} ${rightIncome} ${rightExpenses} ${leftExpenses}`, 0)]
    }

    const ratio = first / (first - last)
    const intersection = `${a + (b - a) * ratio},${y(left.income + (right.income - left.income) * ratio)}`
    return [
      band(first, `${leftIncome} ${intersection} ${leftExpenses}`, 0),
      band(last, `${intersection} ${rightIncome} ${rightExpenses}`, 1),
    ]
  })
}

export function HistoryChart({ period, data = [], loading }: HistoryChartProps) {
  const { i18n, t } = useTranslation('dashboard')
  const [selection, setSelection] = useState<string>()
  const dateLabel = (options: Intl.DateTimeFormatOptions) =>
    new Intl.DateTimeFormat(i18n.resolvedLanguage, { timeZone: 'UTC', ...options })
  const longDate = dateLabel({ dateStyle: 'medium' })
  const shortDate = dateLabel(period === 'year' ? { month: 'short' } : { day: 'numeric', month: 'short' })
  const rangeLabel = (point: HistoryPoint) =>
    longDate.formatRange(asDate(point.start), asDate(point.end))
  const isMonthly = period === 'month'
  const weekLabel = (start: string) => {
    const monday = asDate(start)
    const sunday = asDate(start)
    sunday.setUTCDate(sunday.getUTCDate() + 6)
    return `${shortDate.format(monday)} - ${shortDate.format(sunday)}`
  }

  const active = data.find((point) => point.start === selection)
  const peak = Math.max(1, ...data.flatMap(({ income, expenses }) => [income, expenses]))
  const magnitude = 10 ** Math.floor(Math.log10(peak))
  const step = Math.ceil(peak / (4 * magnitude) * 10) * magnitude / 10
  const max = step * 4
  const lastIndex = data.length - 1
  const usePlotEdges = isMonthly && lastIndex > 0
  const x = (index: number) => usePlotEdges
    ? (index * 100) / lastIndex
    : ((index + 0.5) * 100) / data.length
  const y = (value: number) => 100 - (value / max) * 100
  const bands = differenceBands(data, x, y)
  // The edge hit areas are half as wide so their inner boundaries stay between points.
  const columns = {
    gridTemplateColumns: usePlotEdges
      ? `minmax(0, 1fr) ${lastIndex > 1 ? `repeat(${lastIndex - 1}, minmax(0, 2fr)) ` : ''}minmax(0, 1fr)`
      : `repeat(${data.length}, minmax(0, 1fr))`,
  }

  return (
    <Card className="h-full min-h-100 rounded-none border-0 bg-transparent py-3 shadow-none">
      <CardContent className="flex min-w-0 flex-1 flex-col px-0 pt-2" aria-busy={loading} aria-label={t('chart.title')}>
        {loading ? (
          <div className="min-h-56 flex-1 animate-pulse rounded bg-muted/20 motion-reduce:animate-none" />
        ) : data.length > 0 ? (
          <figure className="flex min-w-0 flex-1 flex-col" aria-label={t('chart.title')}>
            <div className="grid min-w-0 flex-1 grid-cols-[auto_minmax(0,1fr)] grid-rows-[minmax(14rem,1fr)_auto] gap-x-2 sm:gap-x-3">
              <div aria-hidden="true" className="mr-1 flex min-w-10 flex-col justify-between text-right text-[10px] text-muted-foreground tabular-nums">
                {grid.map((percent) => (
                  <div key={percent} className="h-0 whitespace-nowrap">
                    <span className="block -translate-y-1/2">
                      {formatCurrency(max * (1 - percent / 100), undefined, { compact: true, minimumFractionDigits: 0 })}
                    </span>
                  </div>
                ))}
              </div>
              <div className="relative min-w-0">
                <svg aria-hidden="true" className="absolute inset-0 size-full overflow-visible" viewBox="0 0 100 100" preserveAspectRatio="none">
                  {grid.map((percent) => (
                    <path key={percent} d={`M0 ${percent}H100`} className="stroke-border" vectorEffect="non-scaling-stroke" />
                  ))}
                  {bands.map((band) => (
                    <polygon key={band.key} points={band.points} fillOpacity="0.2"
                      fill={band.type === 'income' ? series[0].stroke : series[1].stroke} />
                  ))}
                  {active && (
                    <path d={`M${x(data.indexOf(active))} 0V100`} className="stroke-muted-foreground/50" vectorEffect="non-scaling-stroke" />
                  )}
                  {series.map((line) => (
                    <polyline key={line.key} fill="none" stroke={line.stroke} strokeWidth="2.5"
                      strokeDasharray={'dash' in line ? line.dash : undefined}
                      strokeLinecap="round" strokeLinejoin="round" vectorEffect="non-scaling-stroke"
                      points={data.map((point, i) => `${x(i)},${y(point[line.key])}`).join(' ')} />
                  ))}
                </svg>
                <div className="absolute inset-0 grid" style={columns}
                  onPointerLeave={(event) => {
                    if (event.pointerType === 'mouse' && !event.currentTarget.contains(document.activeElement)) {
                      setSelection(undefined)
                    }
                  }}
                  onBlur={(event) => {
                    if (!event.currentTarget.contains(event.relatedTarget)) setSelection(undefined)
                  }}>
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
                {active && (
                  <div aria-hidden="true"
                    className="pointer-events-none absolute top-2 z-10 grid w-44 -translate-x-1/2 gap-0.5 rounded-md border border-border bg-popover px-2.5 py-1.5 text-xs text-popover-foreground tabular-nums shadow-sm"
                    style={{ left: `clamp(5.5rem, ${x(data.indexOf(active))}%, calc(100% - 5.5rem))` }}>
                    <span className="text-muted-foreground">{rangeLabel(active)}</span>
                    {series.map(({ key, stroke }) => (
                      <span key={key} className="flex items-center gap-1">
                        <span className="size-1.5 shrink-0 rounded-full" style={{ backgroundColor: stroke }} />
                        <span>{t(`chart.${key}`)}</span>
                        <strong className="ml-auto">{formatCurrency(active[key])}</strong>
                      </span>
                    ))}
                  </div>
                )}
              </div>
              <div aria-hidden="true" className="col-start-2 mt-2 grid text-center text-[10px] text-muted-foreground sm:text-xs" style={columns}>
                {data.map((point, index) => {
                  const alignment = index === 0 ? 'text-start' : index === lastIndex ? 'text-end' : ''
                  const visibility = data.length > 7 && index % 2 !== 0 && index !== lastIndex
                    ? 'invisible truncate sm:visible'
                    : 'truncate'
                  return (
                    <span key={point.start} className={isMonthly ? (usePlotEdges ? alignment : '') : visibility}>
                      {isMonthly ? weekLabel(point.start) : shortDate.format(asDate(point.start))}
                    </span>
                  )
                })}
              </div>
            </div>
          </figure>
        ) : (
          <p className="grid min-h-56 flex-1 place-items-center text-sm text-muted-foreground">{t('chart.empty')}</p>
        )}
      </CardContent>
    </Card>
  )
}
