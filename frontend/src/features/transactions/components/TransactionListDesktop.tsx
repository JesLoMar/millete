import { useTranslation } from "react-i18next"
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

interface TransactionListDesktopProps {
  transactions: Transaction[]
  onEdit: (tx: Transaction) => void
  onDelete: (tx: Transaction) => void
}

export function TransactionListDesktop({ transactions, onEdit, onDelete }: TransactionListDesktopProps) {
  const { t } = useTranslation()

  return (
    <div className="hidden sm:flex flex-col">
      {transactions.map((tx) => {
        const categoryName = tx.category || "Sin categoría"
        const isIncome = tx.type === "INCOME"
        const isOrphan = !tx.category || tx.category === "Sin categoría"

        return (
          <div
            key={tx.id}
            className="flex items-center gap-4 p-4 hover:bg-accent/30 transition-colors border-b last:border-0 group"
          >
            <div className={cn(
              "p-2.5 rounded-full shrink-0",
              isIncome ? "bg-emerald-500/10 text-emerald-500" : "bg-rose-500/10 text-rose-500"
            )}>
              {isIncome ? <ArrowUpRight size={16} /> : <ArrowDownLeft size={16} />}
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
                {isOrphan ? (
                  <span className="inline-flex items-center gap-1 text-xs text-amber-500">
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
            </div>

            <div className="text-right shrink-0">
              <p className={cn(
                "text-sm font-bold tabular-nums",
                isIncome ? "text-emerald-500" : "text-foreground"
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
        )
      })}
    </div>
  )
}
