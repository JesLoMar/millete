import { useTranslation } from "react-i18next"
import { MoreHorizontal, ArrowUpRight, ArrowDownLeft, Calendar } from "lucide-react"
import { Button } from "@/shared/components/ui/button"
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/shared/components/ui/dropdown-menu"
import { cn } from "@/lib/utils"
import type { PlannedTransaction } from "@/shared/hooks/usePlannedTransactions"
import { formatDate, formatFrequency, calculateNextExecution } from "../utils"

interface RecurringTransactionRowProps {
    transaction: PlannedTransaction
    onEdit: (tx: PlannedTransaction) => void
    onDelete: (tx: PlannedTransaction) => void
}

export function RecurringTransactionRow({ transaction: tx, onEdit, onDelete }: RecurringTransactionRowProps) {
    const { t } = useTranslation()
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
                <p className="text-sm font-semibold truncate">{tx.description}</p>
                <div className="flex items-center gap-2 mt-0.5">
                    <Calendar size={12} className="text-muted-foreground shrink-0" />
                    <span className="text-xs text-muted-foreground">{formatFrequency(tx, t)}</span>
                    <span className="size-1 rounded-full bg-border" />
                    <span className="text-xs text-muted-foreground">{formatDate(tx.startDate)}</span>
                    {tx.endDate && (
                        <>
                            <span className="text-xs text-muted-foreground">→</span>
                            <span className="text-xs text-muted-foreground">{formatDate(tx.endDate)}</span>
                            <div className="flex items-center gap-2 mt-1">
                                <span className="text-[10px] text-muted-foreground">
                                    {t("transactions.recurring.nextExecution")}: {calculateNextExecution(tx)}
                                </span>
                            </div>
                        </>
                    )}
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