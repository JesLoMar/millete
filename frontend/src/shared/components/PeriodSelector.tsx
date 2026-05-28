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
        "flex p-1 bg-secondary/50 backdrop-blur-sm rounded-xl border border-border/50",
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
              "px-5 py-2 text-sm font-bold uppercase tracking-wider rounded-lg transition-all",
              isActive
                ? "bg-primary text-white shadow-md"
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