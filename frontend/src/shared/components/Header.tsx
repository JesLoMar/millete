import { useEffect, useState } from "react"
import { useTranslation } from "react-i18next"
import { Calendar } from "lucide-react"
import { cn } from "@/lib/utils"
import { useAuth } from "@/features/auth/context/AuthContext"
import { PeriodSelector, type PeriodFilter } from "@/shared/components/PeriodSelector"

export type { PeriodFilter } from "@/shared/components/PeriodSelector"

interface DashboardHeaderProps {
  className?: string
  onPeriodChange?: (period: PeriodFilter) => void
  defaultPeriod?: PeriodFilter
  hidePeriodSelector?: boolean
}

type GreetingKey =
  | "dashboard:header.greeting.morning"
  | "dashboard:header.greeting.afternoon"
  | "dashboard:header.greeting.evening"

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

function getGreetingKey(now: Date): GreetingKey {
  const hour = now.getHours()
  if (hour < 12) return "dashboard:header.greeting.morning"
  if (hour < 20) return "dashboard:header.greeting.afternoon"
  return "dashboard:header.greeting.evening"
}

// Semana del año según ISO-8601 (la semana 1 es la que contiene el primer
// jueves del año; las semanas empiezan en lunes).
function getISOWeek(now: Date): number {
  const date = new Date(Date.UTC(now.getFullYear(), now.getMonth(), now.getDate()))
  const dayNum = date.getUTCDay() || 7 // domingo pasa de 0 a 7
  date.setUTCDate(date.getUTCDate() + 4 - dayNum) // jueves de esta semana
  const yearStart = new Date(Date.UTC(date.getUTCFullYear(), 0, 1))
  return Math.ceil(((date.getTime() - yearStart.getTime()) / 86400000 + 1) / 7)
}

function formatDate(now: Date) {
  return {
    dayOfWeek: now.getDay() as 0|1|2|3|4|5|6,
    monthIndex: now.getMonth() as 0|1|2|3|4|5|6|7|8|9|10|11,
    year: now.getFullYear(),
    week: getISOWeek(now),
  }
}

// "Reloj" barato: no hay intervalos; la fecha/hora se recalcula en cada render
// (coste trivial) y se fuerza un render al recuperar el foco o la visibilidad,
// para que una sesión abierta de un día para otro no muestre datos congelados.
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
  const { t } = useTranslation(['dashboard', 'nav', 'common'])
  const { user } = useAuth()
  const now = useNow()
  const date = formatDate(now)
  const greetingKey = getGreetingKey(now)
  const userName = user?.name || user?.email?.split("@")[0] || t('nav:guest')

  const handlePeriodChange = (period: PeriodFilter) => {
    onPeriodChange?.(period)
  }

  return (
    <div className={cn(
      "flex flex-col sm:flex-row sm:items-center justify-between gap-3 sm:gap-4 w-full min-w-0",
      className
    )}>
      <div className="space-y-1.5 min-w-0 flex-1">
        <h1 className="text-2xl sm:text-3xl font-semibold tracking-tight text-foreground truncate w-full">
          {t(greetingKey)}, <span className="text-primary">{userName}</span>
        </h1>
        <div className="flex items-center gap-2 text-xs sm:text-sm text-muted-foreground overflow-x-auto no-scrollbar whitespace-nowrap py-0.5">
          <Calendar className="size-3.5 sm:size-4 shrink-0 text-muted-foreground/80" />
          <span className="capitalize font-medium">
            {t(DAYS_KEYS[date.dayOfWeek])}
          </span>
          <span className="text-muted-foreground/30 px-0.5" aria-hidden="true">•</span>
          <span className="capitalize font-medium">
            {t('dashboard:header.dateFormat', {
              month: t(MONTHS_KEYS[date.monthIndex]),
              year: date.year
            })}
          </span>
          <span className="text-muted-foreground/30 px-0.5" aria-hidden="true">•</span>
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
