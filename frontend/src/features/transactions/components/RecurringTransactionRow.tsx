import { useTranslation } from "react-i18next"
import { MoreHorizontal, ArrowUpRight, ArrowDownLeft, Calendar, Repeat } from "lucide-react"
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
        <>
            <div className="hidden sm:flex items-center gap-4 p-4 hover:bg-accent/30 transition-colors border-b last:border-0 group">
                <div className={cn(
                    "p-2.5 rounded-full shrink-0",
                    isIncome ? "bg-emerald-500/10 text-emerald-500" : "bg-rose-500/10 text-rose-500"
                )}>
                    {isIncome ? <ArrowUpRight size={16} /> : <ArrowDownLeft size={16} />}
                </div>
                <div className="flex-1 min-w-0">
                    <p className="text-sm font-semibold truncate">{tx.description}</p>
                    <div className="flex items-center gap-2 mt-0.5 flex-wrap">
                        <Calendar size={12} className="text-muted-foreground shrink-0" />
                        <span className="text-xs text-muted-foreground">{formatFrequency(tx, t)}</span>
                        <span className="size-1 rounded-full bg-border" />
                        <span className="text-xs text-muted-foreground">{formatDate(tx.startDate)}</span>
                        {tx.endDate && (
                            <>
                                <span className="text-xs text-muted-foreground">→</span>
                                <span className="text-xs text-muted-foreground">{formatDate(tx.endDate)}</span>
                            </>
                        )}
                        <span className="size-1 rounded-full bg-border" />
                        <span className="text-[10px] text-muted-foreground flex items-center gap-1">
                            <Repeat size={10} className="shrink-0" />
                            {calculateNextExecution(tx)}
                        </span>
                    </div>
                </div>
                <div className="text-right shrink-0">
                    <p className={cn("text-sm font-bold tabular-nums", isIncome ? "text-emerald-500" : "text-foreground")}>
                        {isIncome ? "+" : "-"}
                        {Math.abs(tx.amount).toLocaleString("es-ES", { minimumFractionDigits: 2, maximumFractionDigits: 2 })} €
                    </p>
                </div>
                <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                        <Button 
                            variant="ghost" 
                            size="icon" 
                            className="size-8 opacity-0 group-hover:opacity-100 transition-opacity"
                            aria-label={t("transactions.moreOptions")}
                        >
                            <MoreHorizontal size={16} aria-hidden="true" />
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
            <div className="sm:hidden p-4 hover:bg-accent/30 transition-colors border-b last:border-0">
                <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-3">
                        <div className={cn(
                            "p-2 rounded-full shrink-0",
                            isIncome ? "bg-emerald-500/10 text-emerald-500" : "bg-rose-500/10 text-rose-500"
                        )}>
                            {isIncome ? <ArrowUpRight size={15} /> : <ArrowDownLeft size={15} />}
                        </div>
                        <p className={cn(
                            "text-base font-bold tabular-nums",
                            isIncome ? "text-emerald-500" : "text-foreground"
                        )}>
                            {isIncome ? "+" : "-"}
                            {Math.abs(tx.amount).toLocaleString("es-ES", { minimumFractionDigits: 2, maximumFractionDigits: 2 })} €
                        </p>
                    </div>
                    <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                            <Button 
                                variant="ghost" 
                                size="icon" 
                                className="size-8"
                                aria-label={t("transactions.moreOptions")}
                            >
                                <MoreHorizontal size={16} aria-hidden="true" />
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
                <p className="text-sm font-medium truncate mb-1.5">
                    {tx.description}
                </p>
                <div className="flex items-center gap-1.5 flex-wrap text-xs text-muted-foreground">
                    <Calendar size={11} className="shrink-0" />
                    <span>{formatFrequency(tx, t)}</span>
                    <span className="text-muted-foreground/50">·</span>
                    <span>{formatDate(tx.startDate)}</span>
                    {tx.endDate && (
                        <>
                            <span>→</span>
                            <span>{formatDate(tx.endDate)}</span>
                        </>
                    )}
                    <span className="text-muted-foreground/50">·</span>
                    <span className="flex items-center gap-1">
                        <Repeat size={10} className="shrink-0" />
                        {calculateNextExecution(tx)}
                    </span>
                </div>
            </div>
        </>
    )
}