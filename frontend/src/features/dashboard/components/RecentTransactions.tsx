import { useTranslation } from "react-i18next"
import { useNavigate } from "react-router-dom"
import { Card, CardContent, CardHeader, CardTitle } from "@/shared/components/ui/card"
import { Badge } from "@/shared/components/ui/badge"
import { ArrowDownRight, ArrowUpRight, ShoppingCart } from "lucide-react"
import { CATEGORY_ICONS, CATEGORY_COLORS } from "../constants"
import { formatDate } from "../utils"
import type { TransactionItem } from "../types"
import { formatCurrency } from '@/shared/utils/i18nFormat';

interface RecentTransactionsProps {
  data?: TransactionItem[]
  loading?: boolean
  limit?: number
}

export function RecentTransactions({
  data: externalData,
  loading = false,
  limit = 5,
}: RecentTransactionsProps) {
  const { t } = useTranslation()
  const navigate = useNavigate()

  const transactions = (externalData || []).slice(0, limit)
  const hasExternalData = !!externalData

  if (loading) {
    return (
      <Card className="col-span-1 md:col-span-7 border-subtle">
        <CardHeader className="flex flex-row items-center justify-between">
          <div className="h-6 w-40 bg-muted rounded animate-pulse" />
          <div className="h-4 w-20 bg-muted rounded animate-pulse" />
        </CardHeader>
        <CardContent className="p-0">
          {Array.from({ length: limit }).map((_, i) => (
            <div key={`skeleton-${i}`} className="flex items-center gap-4 p-4 border-b">
              <div className="size-10 rounded-full bg-muted animate-pulse" />
              <div className="flex-1 space-y-2">
                <div className="h-4 w-32 bg-muted rounded animate-pulse" />
                <div className="h-3 w-24 bg-muted rounded animate-pulse" />
              </div>
              <div className="h-4 w-16 bg-muted rounded animate-pulse" />
            </div>
          ))}
        </CardContent>
      </Card>
    )
  }

  return (
    <Card className="col-span-1 md:col-span-7 border-subtle">
      <CardHeader className="flex flex-row items-center justify-between">
        <CardTitle className="text-lg font-headline font-bold">
          {t("dashboard.transactions.title")}
        </CardTitle>
        {hasExternalData && (
          <button
            onClick={() => navigate("/transactions")}
            className="text-sm text-primary hover:underline transition-colors"
          >
            {t("dashboard.transactions.viewAll")}
          </button>
        )}
      </CardHeader>
      <CardContent className="p-0">
        <div className="flex flex-col">
          {transactions.length === 0 ? (
            <p className="text-center text-muted-foreground py-8 text-sm">
              {t("dashboard.transactions.empty")}
            </p>
          ) : (
            transactions.map((tx) => {
              const categoryKey = tx.category || "other"
              const Icon = CATEGORY_ICONS[categoryKey] || ShoppingCart
              const color = CATEGORY_COLORS[categoryKey] || "text-muted-foreground bg-muted/10"
              const isExpense = tx.type === "EXPENSE"

              return (
                <div
                  key={tx.id}
                  onClick={() => navigate(`/transactions?id=${tx.id}`)}
                  className="flex items-center gap-4 p-4 hover:bg-accent/30 transition-colors border-b last:border-0 group cursor-pointer"
                >
                  <div className={`p-3 rounded-full ${color}`}>
                    <Icon className="size-5" />
                  </div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-semibold truncate group-hover:text-primary transition-colors">
                      {tx.description}
                    </p>
                    <div className="flex items-center gap-2 mt-0.5">
                      <span className="text-xs text-muted-foreground">
                        {formatDate(tx.date)}
                      </span>
                      <span className="size-1 rounded-full bg-border" />
                      <span className="text-xs text-muted-foreground">{tx.category}</span>
                    </div>
                  </div>
                  <div className="text-right">
                    <p className={`text-sm font-bold flex items-center gap-1 ${isExpense ? "text-foreground" : "text-emerald-500"}`}>
                      {isExpense
                        ? <ArrowDownRight className="size-3.5" />
                        : <ArrowUpRight className="size-3.5" />
                      }
                      {isExpense ? "" : "+"}
                      {formatCurrency(tx.amount)}
                    </p>
                    <Badge
                      variant="outline"
                      className={`mt-1 text-[10px] h-4 py-0 font-normal ${isExpense ? "opacity-60" : "text-emerald-500 border-emerald-500/30"}`}
                    >
                      {isExpense ? t("dashboard.transactions.expense") : t("dashboard.transactions.income")}
                    </Badge>
                  </div>
                </div>
              )
            })
          )}
        </div>
      </CardContent>
    </Card>
  )
}