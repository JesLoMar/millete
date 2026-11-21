import { useEffect, useMemo, useState } from "react"
import { useQuery } from "@tanstack/react-query"
import { useTranslation } from "react-i18next"
import { Calendar } from "lucide-react"
import { cn } from "@/lib/utils"
import { profileService } from "@/features/profile/services/profileService"
import type { UserPreferences } from "@/features/profile/types"
import { PeriodSelector, type PeriodFilter } from "@/shared/components/PeriodSelector"

export type { PeriodFilter } from "@/shared/components/PeriodSelector"

interface DashboardHeaderProps {
  className?: string
  onPeriodChange?: (period: PeriodFilter) => void
  defaultPeriod?: PeriodFilter
  hidePeriodSelector?: boolean
}

const DAYS_KEYS = [
  "dashboard:header.days.0",
  "dashboard:header.days.1",
  "dashboard:header.days.2",
  "dashboard:header.days.3",
  "dashboard:header.days.4",
  "dashboard:header.days.5",
  "dashboard:header.days.6",
] as const

const MONTHS_KEYS = [
  "dashboard:header.months.0",
  "dashboard:header.months.1",
  "dashboard:header.months.2",
  "dashboard:header.months.3",
  "dashboard:header.months.4",
  "dashboard:header.months.5",
  "dashboard:header.months.6",
  "dashboard:header.months.7",
  "dashboard:header.months.8",
  "dashboard:header.months.9",
  "dashboard:header.months.10",
  "dashboard:header.months.11",
] as const

function getISOWeek(now: Date): number {
  const date = new Date(Date.UTC(now.getUTCFullYear(), now.getUTCMonth(), now.getUTCDate()))
  const dayNum = date.getUTCDay() || 7
  date.setUTCDate(date.getUTCDate() + 4 - dayNum)
  const yearStart = new Date(Date.UTC(date.getUTCFullYear(), 0, 1))
  return Math.ceil(((date.getTime() - yearStart.getTime()) / 86400000 + 1) / 7)
}

const dateFormatters = new Map<string, Intl.DateTimeFormat>()

function getDateFormatter(timeZone: string): Intl.DateTimeFormat {
  let formatter = dateFormatters.get(timeZone)
  if (!formatter) {
    try {
      formatter = new Intl.DateTimeFormat('en-US', {
        timeZone,
        weekday: 'short',
        year: 'numeric',
        month: 'numeric',
        day: 'numeric',
      })
    } catch {
      formatter = new Intl.DateTimeFormat('en-US', {
        timeZone: Intl.DateTimeFormat().resolvedOptions().timeZone,
        weekday: 'short',
        year: 'numeric',
        month: 'numeric',
        day: 'numeric',
      })
    }
    dateFormatters.set(timeZone, formatter)
  }
  return formatter
}

function formatDate(now: Date, timezone: string) {
  const parts = getDateFormatter(timezone).formatToParts(now)
  const value = (type: Intl.DateTimeFormatPartTypes) =>
    parts.find((part) => part.type === type)?.value ?? '0'
  const weekdayIndex: Record<string, number> = {
    Sun: 0, Mon: 1, Tue: 2, Wed: 3, Thu: 4, Fri: 5, Sat: 6,
  }
  const year = Number(value('year'))
  const month = Number(value('month'))
  const day = Number(value('day'))
  const zonedDate = new Date(Date.UTC(year, month - 1, day))

  return {
    dayOfWeek: weekdayIndex[value('weekday')] ?? zonedDate.getUTCDay(),
    monthIndex: (month - 1) as 0|1|2|3|4|5|6|7|8|9|10|11,
    year,
    day,
    week: getISOWeek(zonedDate),
  }
}

function useNow(): Date {
  const [now, setNow] = useState(() => new Date())
  useEffect(() => {
    const refresh = () => setNow(new Date())
    window.addEventListener('focus', refresh)
    document.addEventListener('visibilitychange', refresh)
    return () => {
      window.removeEventListener('focus', refresh)
      document.removeEventListener('visibilitychange', refresh)
    }
  }, [])
  return now
}

export function Header({
  className,
  onPeriodChange,
  defaultPeriod = "month",
  hidePeriodSelector = false,
}: DashboardHeaderProps) {
  const { t } = useTranslation(['dashboard'])
  const { data: preferences } = useQuery<UserPreferences>({
    queryKey: ['preferences'],
    queryFn: profileService.getPreferences,
  })
  const now = useNow()
  const timezone = preferences?.timezone ?? Intl.DateTimeFormat().resolvedOptions().timeZone
  const date = useMemo(() => formatDate(now, timezone), [now, timezone])

  const handlePeriodChange = (period: PeriodFilter) => {
    onPeriodChange?.(period)
  }

  return (
    <div className={cn(
      "flex flex-col sm:flex-row sm:items-center justify-between gap-3 sm:gap-4 w-full min-w-0",
      className
    )}>
      <div className="min-w-0 flex-1">
        <div className="flex items-center gap-2 text-sm text-foreground/70 overflow-x-auto no-scrollbar whitespace-nowrap py-0.5">
          <Calendar className="size-3.5 sm:size-4 shrink-0 text-muted-foreground/80" />
          <span className="font-medium">
            {t('dashboard:header.dateFormat', {
              month: t(MONTHS_KEYS[date.monthIndex]),
              year: date.year,
              day: date.day,
            })}
          </span>
          <span className="text-muted-foreground/30 px-0.5" aria-hidden="true">•</span>
          <span className="capitalize font-medium">
            {t(DAYS_KEYS[date.dayOfWeek])},
          </span>
          <span className="font-medium">
            {t('dashboard:header.week', { week: date.week })}
          </span>
        </div>
      </div>
      {!hidePeriodSelector && onPeriodChange && (
        <div className="w-full sm:w-auto sm:shrink-0 mt-1 sm:mt-0">
          <PeriodSelector
            period={defaultPeriod}
            onPeriodChange={handlePeriodChange}
          />
        </div>
      )}
    </div>
  )
}
