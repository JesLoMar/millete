import { useTranslation } from "react-i18next"
import { Wallet, TrendingUp, Coins } from "lucide-react"
import { FormattedMetricCard } from "@/shared/components/FormattedMetricCard"
import type { PeriodFilter } from "@/shared/components/PeriodSelector"
import type { InvestmentMetricsData } from "../types"

interface InvestmentMetricsProps {
  data: InvestmentMetricsData | undefined
  isLoading: boolean
  period: PeriodFilter
}

export function InvestmentMetrics({ data: metrics, isLoading, period }: InvestmentMetricsProps) {
  const { t } = useTranslation()
  const periodLabel = t(`dashboard.metrics.vsLast${period === "week" ? "Week" : period === "month" ? "Month" : "Year"}`)

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
      <FormattedMetricCard
        title={t("investments.portfolioValue")}
        value={metrics?.portfolioValue ?? 0}
        trend={metrics?.portfolioTrend ?? 0}
        icon={Wallet}
        color="bg-indigo-500/10 text-indigo-400"
        periodLabel={periodLabel}
        loading={isLoading}
      />
      <FormattedMetricCard
        title={t("investments.monthlyReturn")}
        value={metrics?.monthlyReturn ?? 0}
        trend={metrics?.returnTrend ?? 0}
        icon={TrendingUp}
        color="bg-emerald-500/10 text-emerald-400"
        periodLabel={periodLabel}
        loading={isLoading}
      />
      <div className="overflow-hidden border border-border/30 rounded-lg bg-card/50 opacity-60" title={t("investments.comingSoon")}>
        <div className="p-6">
          <div className="flex items-center justify-between mb-4">
            <p className="text-xs font-semibold uppercase tracking-wider text-muted-foreground/50">{t("investments.dividends")}</p>
            <div className="p-2 rounded-lg bg-amber-500/5 text-amber-400/50"><Coins className="size-5" /></div>
          </div>
          <div className="flex flex-col gap-1">
            <h2 className="text-3xl font-semibold tracking-tight tabular-nums text-muted-foreground/30">0,00 €</h2>
            <div className="flex items-center gap-1.5 mt-2">
              <span className="flex items-center text-sm font-medium text-muted-foreground/40">{t("investments.comingSoon")}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}