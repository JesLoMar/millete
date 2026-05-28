import { useMemo } from "react"
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

function getGreetingKey(): string {
  const hour = new Date().getHours()
  if (hour < 12) return "dashboard.header.greeting.morning"
  if (hour < 20) return "dashboard.header.greeting.afternoon"
  return "dashboard.header.greeting.evening"
}

function getCurrentWeek(): number {
  const now = new Date()
  const start = new Date(now.getFullYear(), 0, 1)
  const diff = now.getTime() - start.getTime()
  return Math.ceil((diff / (1000 * 60 * 60 * 24) + start.getDay() + 1) / 7)
}

function formatDate() {
  const now = new Date()
  return {
    dayOfWeek: now.getDay(),
    monthIndex: now.getMonth(),
    year: now.getFullYear(),
    week: getCurrentWeek(),
  }
}

export function Header({
  className, 
  onPeriodChange, 
  defaultPeriod = "month",
  hidePeriodSelector = false,
}: DashboardHeaderProps) {
  const { t } = useTranslation()
  const { user } = useAuth()
  
  const date = useMemo(() => formatDate(), [])
  const greetingKey = useMemo(() => getGreetingKey(), [])
  
  const userName = user?.name || user?.email?.split("@")[0] || t("nav.guest")

  const handlePeriodChange = (period: PeriodFilter) => {
    onPeriodChange?.(period)
  }

  return (
    <div className={cn("flex items-center justify-between", className)}>
      <div className="space-y-1">
        <h1 className="text-2xl font-semibold text-foreground">
          {t(greetingKey)}, <span className="text-primary">{userName}</span>
        </h1>
        <div className="flex items-center gap-2 text-sm text-muted-foreground">
          <Calendar className="size-4" />
          <span className="capitalize">{t(`dashboard.header.days.${date.dayOfWeek}`)}</span>
          <span>{t("dashboard.header.separator")}</span>
          <span className="capitalize">
            {t("dashboard.header.dateFormat", { 
              month: t(`dashboard.header.months.${date.monthIndex}`), 
              year: date.year 
            })}
          </span>
          <span>{t("dashboard.header.separator")}</span>
          <span>{t("dashboard.header.week", { week: date.week })}</span>
        </div>
      </div>

      {!hidePeriodSelector && onPeriodChange && (
        <PeriodSelector 
          period={defaultPeriod} 
          onPeriodChange={handlePeriodChange} 
        />
      )}
    </div>
  )
}