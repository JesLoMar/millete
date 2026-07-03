import { useState, useMemo } from "react"
import { useTranslation } from "react-i18next"
import { useQuery, useQueryClient } from "@tanstack/react-query"
import { apiClient } from "@/shared/api/axiosClient"
import { EditTransactionDialog } from './dialogs/EditTransactionDialog'
import { ConfirmDeletionDialog } from "@/features/categories/components/ConfirmDeletionDialog"
import { type PeriodFilter } from "@/shared/components/Header"
import { usePagination } from "@/features/categories/hooks/usePagination"
import { TransactionListFilters } from "./TransactionListFilters"
import { TransactionListDesktop } from "./TransactionListDesktop"
import { TransactionListMobile } from "./TransactionListMobile"
import { TransactionListPagination } from "./TransactionListPagination"
import { TransactionListSkeleton } from "./TransactionListSkeleton"
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

interface SearchableTransactionListProps {
  transactions: Transaction[]
  onEdit: (tx: Transaction) => void
  onDelete: (tx: Transaction) => void
}

const ITEMS_PER_PAGE = 10

function SearchableTransactionList({ transactions, onEdit, onDelete }: SearchableTransactionListProps) {
  const { t } = useTranslation()
  const [filter, setFilter] = useState<Filter>("all")
  const [searchTerm, setSearchTerm] = useState("")

  const filteredData = useMemo(() => {
    return transactions.filter((tx) => {
      const matchesSearch = tx.description.toLowerCase().includes(searchTerm.toLowerCase())
      const matchesFilter =
        filter === "all" ||
        (filter === "income" && tx.type === "INCOME") ||
        (filter === "expense" && tx.type === "EXPENSE")
      return matchesSearch && matchesFilter
    })
  }, [transactions, filter, searchTerm])

  const { currentPage, totalPages, nextPage, prevPage } = usePagination({
    totalItems: filteredData.length,
    itemsPerPage: ITEMS_PER_PAGE,
    initialPage: 1,
  })

  const paginatedData = useMemo(() => {
    const start = (currentPage - 1) * ITEMS_PER_PAGE
    const end = start + ITEMS_PER_PAGE
    return filteredData.slice(start, end)
  }, [filteredData, currentPage])

  return (
    <div className="space-y-4">
      <TransactionListFilters
        filter={filter}
        searchTerm={searchTerm}
        totalCount={filteredData.length}
        onFilterChange={setFilter}
        onSearchChange={setSearchTerm}
      />

      <div className="bg-card border border-border rounded-xl overflow-hidden">
        {paginatedData.length === 0 ? (
          <p className="text-center text-muted-foreground py-12 text-sm">
            {t('transactions:empty')}
          </p>
        ) : (
          <>
            <TransactionListDesktop
              transactions={paginatedData}
              onEdit={onEdit}
              onDelete={onDelete}
            />
            <TransactionListMobile
              transactions={paginatedData}
              onEdit={onEdit}
              onDelete={onDelete}
            />
          </>
        )}

        <TransactionListPagination
          currentPage={currentPage}
          totalPages={totalPages}
          from={(currentPage - 1) * ITEMS_PER_PAGE + 1}
          to={Math.min(currentPage * ITEMS_PER_PAGE, filteredData.length)}
          total={filteredData.length}
          onPrev={prevPage}
          onNext={nextPage}
        />
      </div>
    </div>
  )
}

export function TransactionList({ period: _period }: TransactionListProps) {
  const { t } = useTranslation()
  const queryClient = useQueryClient()

  const [state, setState] = useState<ListState>({
    editingTransaction: null,
    deletingTransaction: null,
    isDeleting: false,
  })

  const updateState = (updates: Partial<ListState>) => {
    setState(prev => ({ ...prev, ...updates }))
  }

  const { data: transactions = [], isLoading } = useQuery<Transaction[]>({
    queryKey: ['transactions'],
    queryFn: async () => {
      const response = await apiClient.get('/transactions')
      const result: Transaction[] = []
      for (const tx of response.data) {
        if (tx.active === false) continue
        result.push({
          id: tx.id,
          description: tx.description,
          category: tx.categoryName || "Sin categoría",
          categoryColor: tx.categoryColor || null,
          categoryId: tx.categoryId,
          amount: tx.amount,
          date: tx.date,
          type: tx.type,
          active: tx.active
        })
      }
      return result
    },
  })

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

  if (isLoading) {
    return <TransactionListSkeleton />
  }

  return (
    <div className="space-y-4">
      <SearchableTransactionList
        transactions={transactions}
        onEdit={(tx) => updateState({ editingTransaction: tx })}
        onDelete={(tx) => updateState({ deletingTransaction: tx })}
      />

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
