import { type LucideIcon, ArrowDownRight, ArrowUpRight } from "lucide-react"
import { useTranslation } from "react-i18next"
import { MetricCard } from "@/shared/components/MetricCard"
import { cn } from "@/lib/utils"
import { formatCurrency, formatNumber } from '@/shared/utils/i18nFormat';

export interface FormattedMetricCardProps {
  title: string
  value: number
  format?: "currency" | "number"
  trend: number
  trendValue?: number
  icon: LucideIcon
  color: string
  periodLabel?: string
  loading?: boolean
  invertedTrend?: boolean
  className?: string
}

function isValidNumber(num: number): boolean {
  return typeof num === "number" && !isNaN(num) && Number.isFinite(num)
}

export function FormattedMetricCard({
  title,
  value,
  format = "currency",
  trend,
  trendValue,
  icon,
  color,
  periodLabel,
  loading = false,
  invertedTrend = false,
  className,
}: FormattedMetricCardProps) {
  const { t } = useTranslation()

  const safeValue = isValidNumber(value) ? value : 0
  const formattedValue = format === "currency"
    ? formatCurrency(safeValue)
    : formatNumber(safeValue)

  const formattedTrendValue = trendValue !== undefined
    ? formatCurrency(isValidNumber(trendValue) ? trendValue : 0)
    : undefined

  const hasValidTrend = isValidNumber(trend)
  const isTrendUp = hasValidTrend && trend >= 0
  const isPositive = invertedTrend ? !isTrendUp : isTrendUp
  const trendColor = isPositive ? "text-emerald-500" : "text-rose-500"

  return (
    <MetricCard
      title={title}
      value={formattedValue}
      icon={icon}
      color={color}
      loading={loading}
      className={cn("w-full min-w-0 flex flex-col justify-between p-4 sm:p-5", className)}
    >
      {hasValidTrend ? (
        <div className="flex items-center flex-wrap gap-x-2 gap-y-1 mt-2 w-full min-w-0">
          <span className={cn("flex items-center text-xs sm:text-sm font-semibold shrink-0", trendColor)}>
            {isTrendUp ? (
              <ArrowUpRight className="size-3.5 mr-0.5 shrink-0" />
            ) : (
              <ArrowDownRight className="size-3.5 mr-0.5 shrink-0" />
            )}
            {Math.abs(trend)}%
          </span>

          {formattedTrendValue && (
            <span className="text-[11px] sm:text-xs text-muted-foreground truncate max-w-full">
              {formattedTrendValue}
            </span>
          )}

          {periodLabel && (
            <span className="text-[11px] sm:text-xs text-muted-foreground/70 truncate max-w-full">
              {periodLabel}
            </span>
          )}
        </div>
      ) : (
        <span className="text-xs text-muted-foreground/60 italic block mt-2">
          {t("metrics.errors.noDataAvailable")}
        </span>
      )}
    </MetricCard>
  )
}