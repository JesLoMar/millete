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
      className={className}
    >
      {hasValidTrend ? (
        <div className="flex items-center gap-1.5">
          <span className={cn("flex items-center text-sm font-medium", trendColor)}>
            {isTrendUp ? (
              <ArrowUpRight className="size-4 mr-0.5" />
            ) : (
              <ArrowDownRight className="size-4 mr-0.5" />
            )}
            {Math.abs(trend)}%
          </span>

          {formattedTrendValue && (
            <span className="text-xs text-muted-foreground">
              {formattedTrendValue}
            </span>
          )}

          {periodLabel && (
            <span className="text-xs text-muted-foreground/80">
              {periodLabel}
            </span>
          )}
        </div>
      ) : (
        <span className="text-xs text-muted-foreground/60 italic">
          {t("metrics.errors.noDataAvailable")}
        </span>
      )}
    </MetricCard>
  )
}