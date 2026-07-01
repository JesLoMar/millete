import { memo } from "react"
import { useTranslation } from "react-i18next"
import { m } from "framer-motion"
import { ArrowUpRight, ArrowDownLeft, MoreHorizontal, HelpCircle } from "lucide-react"
import { Button } from "@/shared/components/core/button"
import { Badge } from "@/shared/components/core/badge"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/shared/components/core/dropdown-menu"
import { cn } from "@/lib/utils"
import { formatDate } from "../utils"
import type { Transaction } from "./types"

interface TransactionListMobileProps {
  transactions: Transaction[]
  onEdit: (tx: Transaction) => void
  onDelete: (tx: Transaction) => void
}

export const TransactionListMobile = memo(function TransactionListMobile({ transactions, onEdit, onDelete }: TransactionListMobileProps) {
  const { t } = useTranslation()

  return (
    <div className="sm:hidden divide-y divide-border">
      {transactions.map((tx) => {
        const categoryName = tx.category || "Sin categoría"
        const isIncome = tx.type === "INCOME"
        const isOrphan = !tx.category || tx.category === "Sin categoría"

        return (
          <m.div
            key={tx.id}
            className="p-4 hover:bg-accent/30 transition-colors"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.3 }}
          >
            <div className="flex items-center justify-between mb-2">
              <div className="flex items-center gap-3">
                <div className={cn(
                  "p-2 rounded-full shrink-0",
                  isIncome ? "bg-primary/10 text-primary" : "bg-destructive/10 text-destructive"
                )}>
                  {isIncome ? <ArrowUpRight size={15} /> : <ArrowDownLeft size={15} />}
                </div>
                <p className={cn(
                  "text-base font-bold tabular-nums",
                  isIncome ? "text-primary" : "text-foreground"
                )}>
                  {isIncome ? "+" : "-"}
                  {Math.abs(tx.amount).toLocaleString("es-ES", {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2
                  })} €
                </p>
              </div>

              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button
                    variant="ghost"
                    size="icon"
                    className="size-8"
                    aria-label={t('transactions:moreOptions')}
                  >
                    <MoreHorizontal size={16} aria-hidden="true" />
                  </Button>
                </DropdownMenuTrigger>
                <DropdownMenuContent align="end" className="bg-card border-border">
                  <DropdownMenuItem
                    className="cursor-pointer"
                    onClick={() => onEdit(tx)}
                  >
                    {t('transactions:edit')}
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    className="text-destructive cursor-pointer"
                    onClick={() => onDelete(tx)}
                  >
                    {t('transactions:delete')}
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>

            <p className="text-sm font-medium truncate mb-1.5">
              {tx.description}
            </p>
            <div className="flex items-center gap-2 flex-wrap">
              <span className="text-xs text-muted-foreground">
                {formatDate(tx.date)}
              </span>
              <span className="size-1 rounded-full bg-border hidden xs:inline-block" />
              {isOrphan ? (
                <span className="inline-flex items-center gap-1 text-xs text-muted-foreground">
                  <HelpCircle size={12} aria-hidden="true" />
                  <span>{categoryName}</span>
                </span>
              ) : (
                <Badge
                  variant="outline"
                  className="border-none text-xs font-medium"
                  style={tx.categoryColor ? {
                    color: tx.categoryColor,
                    backgroundColor: `${tx.categoryColor}20`
                  } : undefined}
                >
                  {categoryName}
                </Badge>
              )}
            </div>
          </m.div>
        )
      })}
    </div>
  )
});
