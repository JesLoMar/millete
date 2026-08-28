import { useTranslation } from "react-i18next"
import { Card, CardContent, CardHeader, CardTitle } from "@/shared/components/core/card"
import { ProgressBar } from "@/shared/components/core/progress-bar"
import type { BudgetItem } from "../types"
import type { PeriodFilter } from "@/shared/components/Header"
import { formatCurrency, formatNumber } from '@/shared/utils/i18nFormat'

interface BudgetBarsProps {
  data?: BudgetItem[]
  loading?: boolean
  period?: PeriodFilter
}

const DISPLAY_LIMIT = 5

function getAdjustedBudgetLimit(budgetLimit: number | null | undefined, period?: PeriodFilter): number | null {
  if (!budgetLimit) return null
  switch (period) {
    case "week": return budgetLimit / 4
    case "month": return budgetLimit
    case "year": return budgetLimit * 12
    default: return budgetLimit
  }
}

export function BudgetBars({
  data: externalData,
  loading = false,
  period = "month",
}: BudgetBarsProps) {
  const { t } = useTranslation(['dashboard', 'common'])

  const budgets = externalData || []

  if (loading) {
    return (
      <Card className="col-span-1 md:col-span-5 border">
        <CardHeader>
          <div className="h-6 w-44 bg-muted rounded animate-pulse" />
        </CardHeader>
        <CardContent className="min-h-96">
          <div className="space-y-4">
            {Array.from({ length: DISPLAY_LIMIT }).map((_, i) => (
              <div key={`skeleton-${i}`} className="space-y-2">
                <div className="flex justify-between">
                  <div className="h-4 w-24 bg-muted rounded animate-pulse" />
                  <div className="h-4 w-16 bg-muted rounded animate-pulse" />
                </div>
                <div className="h-2 w-full bg-muted rounded-full animate-pulse" />
                <div className="h-3 w-20 bg-muted rounded animate-pulse ml-auto" />
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    )
  }

  if (budgets.length === 0) {
    return (
      <Card className="col-span-1 md:col-span-5 border">
        <CardHeader>
          <CardTitle className="text-lg font-serif font-semibold">
            {t('dashboard:budget.title')}
          </CardTitle>
        </CardHeader>
        <CardContent className="min-h-96 flex items-center justify-center">
          <p className="text-center text-muted-foreground text-sm">
            {t('dashboard:budget.empty')}
          </p>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card className="col-span-1 md:col-span-5 border">
      <CardHeader>
        <CardTitle className="text-lg font-serif font-semibold">
          {t('dashboard:budget.title')}
        </CardTitle>
      </CardHeader>
      <CardContent className="min-h-96 flex flex-col">
        <div className="flex-1 space-y-4">
          {budgets.map((budget) => {
            const adjustedLimit = getAdjustedBudgetLimit(budget.limit, period)
            const rawLimit = adjustedLimit ?? budget.limit ?? 0
            const percentageValue = rawLimit > 0 ? (budget.spent / rawLimit) * 100 : 0
            const percentage = Math.min(percentageValue, 100)
            const isOverLimit = percentageValue >= 100
            const isNearLimit = percentageValue >= 80 && !isOverLimit
            const exceededAmount = budget.spent - rawLimit

            return (
              <div key={budget.category} className="space-y-1.5">
                <div className="flex justify-between text-sm">
                  <span className="font-medium">{budget.category}</span>
                  <span className="text-muted-foreground text-xs">
                    <span className="font-semibold text-foreground">
                      {formatCurrency(budget.spent)}
                    </span>
                    {" / "}
                    {formatCurrency(rawLimit)}
                  </span>
                </div>

                <ProgressBar
                  value={percentage}
                  max={100}
                  variant={isOverLimit ? "overbudget" : isNearLimit ? "warning" : "default"}
                  size="sm"
                />
                <p className={`text-xs text-right ${
                  isOverLimit
                    ? "text-destructive font-medium"
                    : isNearLimit
                      ? "text-warning"
                      : "text-muted-foreground"
                }`}>
                  {isOverLimit
                    ? t('dashboard:budget.exceededBy', {
                        amount: formatNumber(exceededAmount, { minimumFractionDigits: 0, maximumFractionDigits: 0 })
                      })
                    : t('dashboard:budget.remaining', {
                        amount: formatNumber(rawLimit - budget.spent, { minimumFractionDigits: 0, maximumFractionDigits: 0 })
                      })}
                </p>
              </div>
            )
          })}
        </div>

      </CardContent>
    </Card>
  )
}
