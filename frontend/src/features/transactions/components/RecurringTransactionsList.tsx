import { useState } from "react"
import { useTranslation } from "react-i18next"
import { useQueryClient } from "@tanstack/react-query"
import { Search } from "lucide-react"
import { Input } from "@/shared/components/core/input"
import { Button } from "@/shared/components/core/button"
import { Badge } from "@/shared/components/core/badge"
import { cn } from "@/lib/utils"
import { apiClient } from "@/shared/api/axiosClient"
import { usePlannedTransactions, type PlannedTransaction } from "@/shared/hooks/usePlannedTransactions"
import { EditRecurringTransactionDialog } from "./dialogs/EditRecurringTransactionDialog"
import { ConfirmDeletionDialog } from "@/features/categories/components/ConfirmDeletionDialog"
import { RecurringTransactionRow } from "./RecurringTransactionRow"
import { TransactionSkeleton } from "./TransactionSkeleton"
import { TransactionListPagination } from "./TransactionListPagination"
import { FILTERS, FILTER_LABELS, type Filter } from "../constants"

export function RecurringTransactionsList() {
  const { t } = useTranslation()
  const queryClient = useQueryClient()

  const [recurringFilter, setRecurringFilter] = useState<Filter>("all")
  const [searchTerm, setSearchTerm] = useState("")
  const [editingTransaction, setEditingTransaction] = useState<PlannedTransaction | null>(null)
  const [deletingTransaction, setDeletingTransaction] = useState<PlannedTransaction | null>(null)

  const {
    displayItems: transactions,
    displayPage,
    displaySize,
    totalDisplayPages,
    totalElements,
    isLoading,
    nextPage,
    prevPage,
  } = usePlannedTransactions({ search: searchTerm, type: recurringFilter })

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

  if (isLoading && transactions.length === 0) return <TransactionSkeleton rows={5} />

  const from = totalElements === 0 ? 0 : displayPage * displaySize + 1
  const to = Math.min((displayPage + 1) * displaySize, totalElements)

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
            {totalElements}
          </Badge>
        </div>

        <div className="relative w-full sm:w-[320px]">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground" />
          <Input
            placeholder={t('transactions:search')}
            className="pl-10 bg-card border-border h-10"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </div>

      <div className="bg-card border border-border rounded-xl overflow-hidden">
        <div className="flex flex-col">
          {transactions.length === 0 ? (
            <p className="text-center text-muted-foreground py-12 text-sm">
              {t('transactions:recurring.emptyFilter')}
            </p>
          ) : (
            transactions.map((tx) => (
              <RecurringTransactionRow
                key={tx.id}
                transaction={tx}
                onEdit={setEditingTransaction}
                onDelete={setDeletingTransaction}
              />
            ))
          )}
        </div>

        <TransactionListPagination
          currentPage={displayPage}
          totalPages={totalDisplayPages}
          from={from}
          to={to}
          total={totalElements}
          onPrev={prevPage}
          onNext={nextPage}
        />
      </div>

      <EditRecurringTransactionDialog
        key={editingTransaction?.id}
        transaction={editingTransaction}
        open={!!editingTransaction}
        onOpenChange={(open) => { if (!open) setEditingTransaction(null) }}
      />

      <ConfirmDeletionDialog
        open={!!deletingTransaction}
        onOpenChange={(open) => { if (!open) setDeletingTransaction(null) }}
        itemName={deletingTransaction?.description || ""}
        onConfirm={handleDelete}
        title={t('transactions:recurring.deleteTitle')}
        description={t("transactions:recurring.deleteConfirmation", { name: deletingTransaction?.description || "" })}
      />
    </div>
  )
}
