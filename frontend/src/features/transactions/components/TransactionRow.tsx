import { useTranslation } from "react-i18next"
import { MoreHorizontal, ArrowUpRight, ArrowDownLeft } from "lucide-react"
import { Button } from "@/shared/components/ui/button"
import { Badge } from "@/shared/components/ui/badge"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/shared/components/ui/dropdown-menu"
import { cn } from "@/lib/utils"
import { CATEGORY_COLORS } from "../constants"
import { formatDate } from "../utils"

interface Transaction {
  id: string
  description: string
  category: string
  categoryId: string
  amount: number
  date: string
  type: "INCOME" | "EXPENSE"
}

interface TransactionRowProps {
  transaction: Transaction
  onEdit: (tx: Transaction) => void
  onDelete: (tx: Transaction) => void
}

export function TransactionRow({ transaction: tx, onEdit, onDelete }: TransactionRowProps) {
  const { t } = useTranslation()
  const color = CATEGORY_COLORS[tx.category] || "text-muted-foreground bg-muted/10"
  const isIncome = tx.type === "INCOME"

  return (
    <div className="flex items-center gap-4 p-4 hover:bg-accent/30 transition-colors border-b last:border-0 group">
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
          <span className="text-xs text-muted-foreground">{formatDate(tx.date)}</span>
          <span className="size-1 rounded-full bg-border" />
          <Badge variant="outline" className={cn("border-none text-xs font-medium", color)}>
            {tx.category}
          </Badge>
        </div>
      </div>

      <div className="text-right">
        <p className={cn("text-sm font-bold tabular-nums", isIncome ? "text-emerald-500" : "text-foreground")}>
          {isIncome ? "+" : "-"}
          {Math.abs(tx.amount).toLocaleString("es-ES", { minimumFractionDigits: 2, maximumFractionDigits: 2 })} €
        </p>
      </div>

      <DropdownMenu>
        <DropdownMenuTrigger asChild>
          <Button variant="ghost" size="icon" className="size-8 opacity-0 group-hover:opacity-100 transition-opacity">
            <MoreHorizontal size={16} />
          </Button>
        </DropdownMenuTrigger>
        <DropdownMenuContent align="end" className="bg-card border-border">
          <DropdownMenuItem className="cursor-pointer" onClick={() => onEdit(tx)}>
            {t("transactions.edit")}
          </DropdownMenuItem>
          <DropdownMenuItem className="text-destructive cursor-pointer" onClick={() => onDelete(tx)}>
            {t("transactions.delete")}
          </DropdownMenuItem>
        </DropdownMenuContent>
      </DropdownMenu>
    </div>
  )
}