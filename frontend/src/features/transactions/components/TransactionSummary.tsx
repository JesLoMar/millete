import { useTranslation } from "react-i18next"
import { useQuery } from "@tanstack/react-query"
import { ArrowUpRight, ArrowDownLeft, Scale, Activity } from "lucide-react"
import { apiClient } from "@/shared/api/axiosClient"
import type { PeriodFilter } from "@/shared/components/Header"
import { FormattedMetricCard } from "@/shared/components/FormattedMetricCard"

// ─── Types ────────────────────────────────────────────────
interface TransactionMetrics {
  income: number
  expenses: number
  balance: number
  count: number
  incomeTrend: number
  expensesTrend: number
  balanceTrend: number
  countTrend: number
}

interface TransactionSummaryProps {
  period: PeriodFilter
}

// ─── Component ────────────────────────────────────────────
export function TransactionSummary({ period }: TransactionSummaryProps) {
  const { t } = useTranslation()

  const { data: metrics, isLoading } = useQuery<TransactionMetrics>({
    queryKey: ['transactionMetrics', period],
    queryFn: async (): Promise<TransactionMetrics> => {
      const response = await apiClient.get(`/transactions/metrics?period=${period}`)
      return response.data
    },
  })

  const periodLabel = t(`dashboard.metrics.vsLast${period === "week" ? "Week" : period === "month" ? "Month" : "Year"}`)

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
      <FormattedMetricCard
        title={t("dashboard.metrics.income", { period: t(`dashboard.header.period.${period}`) })}
        value={metrics?.income ?? 0}
        trend={metrics?.incomeTrend ?? 0}
        icon={ArrowUpRight}
        color="bg-emerald-500/10 text-emerald-500"
        periodLabel={periodLabel}
        loading={isLoading}
      />
      <FormattedMetricCard
        title={t("dashboard.metrics.expenses", { period: t(`dashboard.header.period.${period}`) })}
        value={metrics?.expenses ?? 0}
        trend={metrics?.expensesTrend ?? 0}
        icon={ArrowDownLeft}
        color="bg-rose-500/10 text-rose-500"
        periodLabel={periodLabel}
        loading={isLoading}
        invertedTrend
      />
      <FormattedMetricCard
        title={t("dashboard.metrics.balance")}
        value={metrics?.balance ?? 0}
        trend={metrics?.balanceTrend ?? 0}
        icon={Scale}
        color="bg-primary/10 text-primary"
        periodLabel={periodLabel}
        loading={isLoading}
      />
      <FormattedMetricCard
        title={t("transactions.title")}
        value={metrics?.count ?? 0}
        format="number"
        trend={metrics?.countTrend ?? 0}
        icon={Activity}
        color="bg-amber-500/10 text-amber-500"
        periodLabel={periodLabel}
        loading={isLoading}
      />
    </div>
  )
}