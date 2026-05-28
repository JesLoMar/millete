import { useState, useMemo } from "react"
import { useTranslation } from "react-i18next"
import { useQueryClient } from "@tanstack/react-query"
import { Search } from "lucide-react"
import { Input } from "@/shared/components/ui/input"
import { Button } from "@/shared/components/ui/button"
import { Badge } from "@/shared/components/ui/badge"
import { cn } from "@/lib/utils"
import { apiClient } from "@/shared/api/axiosClient"
import { usePlannedTransactions, type PlannedTransaction } from "@/shared/hooks/usePlannedTransactions"
import { EditRecurringTransactionDialog } from "./dialogs/EditRecurringTransactionDialog"
import { ConfirmDeletionDialog } from "@/features/categories/components/ConfirmDeletionDialog"
import { RecurringTransactionRow } from "./RecurringTransactionRow"
import { TransactionSkeleton } from "./TransactionSkeleton"
import { FILTERS, FILTER_LABELS, type Filter } from "../constants"

export function RecurringTransactionsList() {
  const { t } = useTranslation()
  const queryClient = useQueryClient()
  const { data: transactions = [], isLoading } = usePlannedTransactions()
  const [recurringFilter, setRecurringFilter] = useState<Filter>("all")
  const [searchTerm, setSearchTerm] = useState("")
  const [editingTransaction, setEditingTransaction] = useState<PlannedTransaction | null>(null)
  const [deletingTransaction, setDeletingTransaction] = useState<PlannedTransaction | null>(null)

  const handleDelete = async () => {
    if (!deletingTransaction) return

    try {
      await apiClient.delete(`/planned-transactions/${deletingTransaction.id}`)
      queryClient.invalidateQueries({ queryKey: ['plannedTransactions'] })
      setDeletingTransaction(null)
    } catch (err) {
      console.error("Error al eliminar transacción recurrente:", err)
    }
  }

  const filteredTransactions = useMemo(() => {
    return transactions.filter((tx) => {
      const matchesSearch = tx.description.toLowerCase().includes(searchTerm.toLowerCase())
      const matchesFilter =
        recurringFilter === "all" ||
        (recurringFilter === "income" && tx.type === "INCOME") ||
        (recurringFilter === "expense" && tx.type === "EXPENSE")
      return matchesSearch && matchesFilter
    })
  }, [transactions, recurringFilter, searchTerm])

  if (isLoading) return <TransactionSkeleton rows={5} />

  if (transactions.length === 0) return null

  return (
    <div className="space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex items-center gap-3">
          <div className="flex items-center gap-2 bg-card p-1 rounded-lg border border-border">
            {FILTERS.map((f) => (
              <Button
                key={f}
                variant={recurringFilter === f ? "secondary" : "ghost"}
                size="sm"
                onClick={() => setRecurringFilter(f)}
                className={cn(
                  "rounded-md text-sm transition-all h-8",
                  recurringFilter === f ? "bg-primary/20 text-primary" : "text-muted-foreground"
                )}
              >
                {t(FILTER_LABELS[f])}
              </Button>
            ))}
          </div>
          <Badge variant="outline" className="text-xs">
            {filteredTransactions.length}
          </Badge>
        </div>

        <div className="relative w-full sm:w-[320px]">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground" />
          <Input
            placeholder={t("transactions.search")}
            className="pl-10 bg-card border-border h-10"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </div>

      <div className="bg-card border border-border rounded-xl overflow-hidden">
        <div className="flex flex-col">
          {filteredTransactions.length === 0 ? (
            <p className="text-center text-muted-foreground py-12 text-sm">
              {t("transactions.recurring.emptyFilter")}
            </p>
          ) : (
            filteredTransactions.map((tx) => (
              <RecurringTransactionRow
                key={tx.id}
                transaction={tx}
                onEdit={setEditingTransaction}
                onDelete={setDeletingTransaction}
              />
            ))
          )}
        </div>
      </div>

      <EditRecurringTransactionDialog
        transaction={editingTransaction}
        open={!!editingTransaction}
        onOpenChange={(open) => { if (!open) setEditingTransaction(null) }}
      />

      <ConfirmDeletionDialog
        open={!!deletingTransaction}
        onOpenChange={(open) => { if (!open) setDeletingTransaction(null) }}
        itemName={deletingTransaction?.description || ""}
        onConfirm={handleDelete}
      />
    </div>
  )
}