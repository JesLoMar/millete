import { useState } from "react"
import { useTranslation } from "react-i18next"
import { ChevronLeft, ChevronRight } from "lucide-react"
import { Card, CardContent, CardHeader, CardTitle } from "@/shared/components/core/card"
import { Button } from "@/shared/components/core/button"
import { ProgressBar } from "@/shared/components/core/progress-bar"
import type { BudgetItem } from "../types"
import { formatCurrency, formatNumber } from '@/shared/utils/i18nFormat'

interface BudgetBarsProps {
  data?: BudgetItem[]
  loading?: boolean
}

const ITEMS_PER_PAGE = 5

export function BudgetBars({
  data: externalData,
  loading = false,
}: BudgetBarsProps) {
  const { t } = useTranslation(['dashboard', 'common'])
  const [currentPage, setCurrentPage] = useState(1)
  
  const budgets = externalData || []
  const totalPages = Math.ceil(budgets.length / ITEMS_PER_PAGE)
  const paginatedData = budgets.slice(
    (currentPage - 1) * ITEMS_PER_PAGE,
    currentPage * ITEMS_PER_PAGE
  )

  if (loading) {
    return (
      <Card className="col-span-1 md:col-span-5 border-subtle">
        <CardHeader>
          <div className="h-6 w-44 bg-muted rounded animate-pulse" />
        </CardHeader>
        <CardContent className="min-h-85">
          <div className="space-y-4">
            {Array.from({ length: ITEMS_PER_PAGE }).map((_, i) => (
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
      <Card className="col-span-1 md:col-span-5 border-subtle">
        <CardHeader>
          <CardTitle className="text-lg font-headline font-semibold">
            {t('dashboard:budget.title')}
          </CardTitle>
        </CardHeader>
        <CardContent className="min-h-85 flex items-center justify-center">
          <p className="text-center text-muted-foreground text-sm">
            {t('dashboard:budget.empty')}
          </p>
        </CardContent>
      </Card>
    )
  }

  return (
    <Card className="col-span-1 md:col-span-5 border-subtle">
      <CardHeader>
        <CardTitle className="text-lg font-headline font-semibold">
          {t('dashboard:budget.title')}
        </CardTitle>
      </CardHeader>
      <CardContent className="min-h-85 flex flex-col">
        <div className="flex-1 space-y-4">
          {paginatedData.map((budget) => {
            const isPercentageValid = typeof budget.percentage === 'number' && !isNaN(budget.percentage) && isFinite(budget.percentage)
            const percentageValue = isPercentageValid ? budget.percentage : 0
            const percentage = Math.min(percentageValue, 100)
            const isOverLimit = percentageValue >= 100
            const isNearLimit = percentageValue >= 80 && !isOverLimit
            const exceededAmount = budget.spent - budget.limit

            return (
              <div key={budget.category} className="space-y-1.5">
                <div className="flex justify-between text-sm">
                  <span className="font-medium">{budget.category}</span>
                  <span className="text-muted-foreground text-xs">
                    <span className="font-semibold text-foreground">
                      {formatCurrency(budget.spent)}
                    </span>
                    {" / "}
                    {formatCurrency(budget.limit)}
                  </span>
                </div>
                
                <ProgressBar
                  value={percentage}
                  max={100}
                  variant={isOverLimit ? "overbudget" : isNearLimit ? "warning" : "default"}
                  size="sm"
                />
                
                <p className={`text-[10px] text-right ${
                  isOverLimit
                    ? "text-rose-400 font-medium"
                    : isNearLimit
                      ? "text-amber-400"
                      : "text-muted-foreground"
                }`}>
                  {isOverLimit
                    ? t('dashboard:budget.exceededBy', {
                        amount: formatNumber(exceededAmount, { minimumFractionDigits: 0, maximumFractionDigits: 0 })
                      })
                    : t('dashboard:budget.remaining', {
                        amount: formatNumber(budget.limit - budget.spent, { minimumFractionDigits: 0, maximumFractionDigits: 0 })
                      })}
                </p>
              </div>
            )
          })}
        </div>

        {totalPages > 1 && (
          <div className="flex items-center justify-between pt-2 border-t border-border mt-auto">
            <p className="text-xs text-muted-foreground">
              {currentPage} / {totalPages}
            </p>
            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={() => setCurrentPage(p => Math.max(1, p - 1))}
                disabled={currentPage === 1}
                className="h-7 border-border"
              >
                <ChevronLeft size={14} />
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => setCurrentPage(p => Math.min(totalPages, p + 1))}
                disabled={currentPage === totalPages}
                className="h-7 border-border"
              >
                <ChevronRight size={14} />
              </Button>
            </div>
          </div>
        )}
      </CardContent>
    </Card>
  )
}