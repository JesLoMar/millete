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
        "flex sm:inline-flex p-1 bg-secondary/40 backdrop-blur-md rounded-xl border border-border/40 w-full sm:w-auto min-w-0",
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
              "flex-1 sm:flex-none px-2 sm:px-5 py-2",
              "text-xs sm:text-sm font-bold uppercase tracking-wider",
              "rounded-lg transition-all duration-200 cursor-pointer min-w-17.5 sm:min-w-21.25 h-10 flex items-center justify-center select-none",
              "focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-1",
              isActive
                ? "bg-primary text-primary-foreground shadow-sm font-extrabold"
                : "text-muted-foreground hover:text-foreground hover:bg-accent/40"
            )}
            aria-current={isActive ? "true" : undefined}
          >
            <span className="truncate w-full block text-center">
              {t(option.labelKey)}
            </span>
          </button>
        )
      })}
    </div>
  )
}