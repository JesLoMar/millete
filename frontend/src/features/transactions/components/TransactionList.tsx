import { useState } from "react"
import { useTranslation } from "react-i18next"
import { useQueryClient } from "@tanstack/react-query"
import { apiClient } from "@/shared/api/axiosClient"
import { EditTransactionDialog } from './dialogs/EditTransactionDialog'
import { ConfirmDeletionDialog } from "@/features/categories/components/ConfirmDeletionDialog"
import { type PeriodFilter } from "@/shared/components/Header"
import { TransactionListFilters } from "./TransactionListFilters"
import { TransactionListDesktop } from "./TransactionListDesktop"
import { TransactionListMobile } from "./TransactionListMobile"
import { TransactionListPagination } from "./TransactionListPagination"
import { TransactionListSkeleton } from "./TransactionListSkeleton"
import { useTransactions } from "../hooks/useTransactions"
import { type Filter } from "../constants"
import type { Transaction } from "./types"

interface TransactionListProps {
  period: PeriodFilter
}

interface ListState {
  editingTransaction: Transaction | null
  deletingTransaction: Transaction | null
  isDeleting: boolean
}

export function TransactionList({ period }: TransactionListProps) {
  const { t } = useTranslation()
  const queryClient = useQueryClient()

  const [state, setState] = useState<ListState>({
    editingTransaction: null,
    deletingTransaction: null,
    isDeleting: false,
  })

  const [filter, setFilter] = useState<Filter>("all")
  const [searchTerm, setSearchTerm] = useState("")

  const {
    displayItems: transactions,
    displayPage,
    displaySize,
    totalDisplayPages,
    totalElements,
    isLoading,
    nextPage,
    prevPage,
  } = useTransactions({ search: searchTerm, type: filter, period })

  const updateState = (updates: Partial<ListState>) => {
    setState(prev => ({ ...prev, ...updates }))
  }

  const handleDeleteConfirm = async () => {
    if (!state.deletingTransaction) return

    updateState({ isDeleting: true })
    try {
      await apiClient.delete(`/transactions/${state.deletingTransaction.id}`)
      queryClient.invalidateQueries({ queryKey: ['transactions'] })
      queryClient.invalidateQueries({ queryKey: ['transactionMetrics'] })
      queryClient.invalidateQueries({ queryKey: ['dashboardMetrics'] })
      updateState({ deletingTransaction: null })
    } catch (err) {
      console.error("Error al eliminar transacción:", err)
    } finally {
      updateState({ isDeleting: false })
    }
  }

  if (isLoading && transactions.length === 0) {
    return <TransactionListSkeleton />
  }

  const from = totalElements === 0 ? 0 : displayPage * displaySize + 1
  const to = Math.min((displayPage + 1) * displaySize, totalElements)

  return (
    <div className="space-y-4">
      <TransactionListFilters
        filter={filter}
        searchTerm={searchTerm}
        totalCount={totalElements}
        onFilterChange={setFilter}
        onSearchChange={setSearchTerm}
      />

      <div className="bg-card border border-border rounded-xl overflow-hidden">
        {transactions.length === 0 ? (
          <p className="text-center text-muted-foreground py-12 text-sm">
            {t('transactions:empty')}
          </p>
        ) : (
          <>
            <TransactionListDesktop
              transactions={transactions}
              onEdit={(tx) => updateState({ editingTransaction: tx })}
              onDelete={(tx) => updateState({ deletingTransaction: tx })}
            />
            <TransactionListMobile
              transactions={transactions}
              onEdit={(tx) => updateState({ editingTransaction: tx })}
              onDelete={(tx) => updateState({ deletingTransaction: tx })}
            />
          </>
        )}

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

      <EditTransactionDialog
        key={state.editingTransaction?.id}
        transaction={state.editingTransaction}
        open={!!state.editingTransaction}
        onOpenChange={(open) => { if (!open) updateState({ editingTransaction: null }) }}
      />

      <ConfirmDeletionDialog
        open={!!state.deletingTransaction}
        onOpenChange={(open) => { if (!open) updateState({ deletingTransaction: null }) }}
        itemName={state.deletingTransaction?.description || ""}
        onConfirm={handleDeleteConfirm}
        isDeleting={state.isDeleting}
        title={t('transactions:deleteTitle')}
        description={t("transactions:deleteConfirmation", { name: state.deletingTransaction?.description || "" })}
      />
    </div>
  )
}
