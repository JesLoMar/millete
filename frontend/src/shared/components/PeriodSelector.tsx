import { useTranslation } from "react-i18next"
import { cn } from "@/lib/utils"

export type PeriodFilter = "week" | "month" | "year"

interface PeriodSelectorProps {
  period: PeriodFilter
  onPeriodChange: (period: PeriodFilter) => void
  className?: string
}

export function PeriodSelector({ period, onPeriodChange, className }: PeriodSelectorProps) {
  const { t } = useTranslation()

  const options = [
    { value: "week" as PeriodFilter, labelKey: "dashboard.header.period.week" },
    { value: "month" as PeriodFilter, labelKey: "dashboard.header.period.month" },
    { value: "year" as PeriodFilter, labelKey: "dashboard.header.period.year" },
  ]

  return (
    <div 
      className={cn(
        "inline-flex p-1 bg-secondary/50 backdrop-blur-sm rounded-xl border border-border/50",
        "w-full sm:w-auto",
        className
      )}
      role="group"
      aria-label={t("dashboard.header.period.ariaLabel")}
    >
      {options.map((option) => {
        const isActive = period === option.value
        return (
          <button
            key={option.value}
            onClick={() => onPeriodChange(option.value)}
            type="button"
            className={cn(
              "flex-1 sm:flex-none px-3 sm:px-5 py-2",
              "text-xs sm:text-sm font-bold uppercase tracking-wide sm:tracking-wider",
              "rounded-lg transition-all",
              "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-1",
              "min-h-11",
              isActive
                ? "bg-primary text-primary-foreground shadow-md"
                : "text-muted-foreground hover:text-foreground hover:bg-accent/50"
            )}
            aria-current={isActive ? "true" : undefined}
          >
            {t(option.labelKey)}
          </button>
        )
      })}
    </div>
  )
}