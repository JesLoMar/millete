import { useState, useMemo, useEffect } from "react"
import { useTranslation } from "react-i18next"
import { useQuery, useQueryClient } from "@tanstack/react-query"
import {
  Search,
  MoreHorizontal,
  ArrowUpRight,
  ArrowDownLeft,
  ChevronLeft,
  ChevronRight
} from "lucide-react"
import { Input } from "@/shared/components/ui/input"
import { Button } from "@/shared/components/ui/button"
import { Badge } from "@/shared/components/ui/badge"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/shared/components/ui/dropdown-menu"
import { cn } from "@/lib/utils"
import { apiClient } from "@/shared/api/axiosClient"
import { EditTransactionDialog } from './dialogs/EditTransactionDialog'
import { CATEGORY_COLORS, FILTERS, FILTER_LABELS, type Filter } from "../constants"
import { formatDate } from "../utils"
import { usePagination } from "@/features/categories/hooks/usePagination"
import type { PeriodFilter } from "@/shared/components/Header"

interface Transaction {
  id: string
  description: string
  category: string
  categoryId: string
  amount: number
  date: string
  type: "INCOME" | "EXPENSE"
  active?: boolean
}

interface TransactionListProps {
  period: PeriodFilter
}

const ITEMS_PER_PAGE = 10

export function TransactionList({ period: _period }: TransactionListProps) {
  const { t } = useTranslation()
  const queryClient = useQueryClient()
  const [filter, setFilter] = useState<Filter>("all")
  const [searchTerm, setSearchTerm] = useState("")
  const [editingTransaction, setEditingTransaction] = useState<Transaction | null>(null)
  
  const { data: transactions = [], isLoading } = useQuery<Transaction[]>({
    queryKey: ['transactions'],
    queryFn: async () => {
      const response = await apiClient.get('/transactions')
      return response.data.filter((tx: Transaction) => tx.active !== false)
    },
  })

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

  const { currentPage, totalPages, goToPage, nextPage, prevPage } = usePagination({
    totalItems: filteredData.length,
    itemsPerPage: ITEMS_PER_PAGE,
    initialPage: 1,
  })

  useEffect(() => {
    goToPage(1)
  }, [filter, searchTerm, goToPage])

  const paginatedData = useMemo(() => {
    const start = (currentPage - 1) * ITEMS_PER_PAGE
    const end = start + ITEMS_PER_PAGE
    return filteredData.slice(start, end)
  }, [filteredData, currentPage])

  const handleFilterChange = (newFilter: Filter) => {
    setFilter(newFilter)
  }

  const handleDelete = async (id: string) => {
    const confirmed = window.confirm(t("transactions.deleteConfirm"))
    if (!confirmed) return
    
    try {
      await apiClient.delete(`/transactions/${id}`)
      queryClient.invalidateQueries({ queryKey: ['transactions'] })
      queryClient.invalidateQueries({ queryKey: ['transactionMetrics'] })
      queryClient.invalidateQueries({ queryKey: ['dashboardMetrics'] })
    } catch (err) {
      console.error("Error al eliminar transacción:", err)
    }
  }

  if (isLoading) {
    return (
      <div className="bg-card border border-border rounded-xl overflow-hidden">
        <div className="p-4 sm:p-6 space-y-4">
          {Array.from({ length: ITEMS_PER_PAGE }).map((_, i) => (
            <div key={`skeleton-${i}`} className="flex items-center gap-3 sm:gap-4">
              <div className="size-8 sm:size-10 rounded-full bg-muted animate-pulse shrink-0" />
              <div className="flex-1 min-w-0 space-y-2">
                <div className="h-4 w-28 sm:w-32 bg-muted rounded animate-pulse" />
                <div className="h-3 w-16 sm:w-20 bg-muted rounded animate-pulse" />
              </div>
              <div className="h-4 w-12 sm:w-16 bg-muted rounded animate-pulse shrink-0" />
            </div>
          ))}
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      {/* FILTROS Y BÚSQUEDA */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 sm:gap-4">
        <div className="flex items-center gap-2 sm:gap-3">
          <div className="flex items-center gap-1 sm:gap-2 bg-card p-1 rounded-lg border border-border">
            {FILTERS.map((f) => (
              <Button
                key={f}
                variant={filter === f ? "secondary" : "ghost"}
                size="sm"
                onClick={() => handleFilterChange(f)}
                className={cn(
                  "rounded-md text-xs sm:text-sm transition-all h-7 sm:h-8 px-2 sm:px-3",
                  filter === f ? "bg-primary/20 text-primary" : "text-muted-foreground"
                )}
              >
                {t(FILTER_LABELS[f])}
              </Button>
            ))}
          </div>
          <Badge variant="outline" className="text-xs shrink-0">
            {filteredData.length}
          </Badge>
        </div>

        <div className="relative w-full sm:w-[320px]">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground" />
          <Input
            placeholder={t("transactions.search")}
            className="pl-10 bg-card border-border h-9 sm:h-10 text-sm"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
      </div>

      {/* LISTA DE TRANSACCIONES */}
      <div className="bg-card border border-border rounded-xl overflow-hidden">
        {paginatedData.length === 0 ? (
          <p className="text-center text-muted-foreground py-12 text-sm">
            {t("transactions.empty")}
          </p>
        ) : (
          <>
            {/* ========= VERSIÓN DESKTOP (sm+): Diseño de tabla horizontal ========= */}
            <div className="hidden sm:flex flex-col">
              {paginatedData.map((tx) => {
                const color = CATEGORY_COLORS[tx.category] || "text-muted-foreground bg-muted/10"
                const isIncome = tx.type === "INCOME"

                return (
                  <div
                    key={tx.id}
                    className="flex items-center gap-4 p-4 hover:bg-accent/30 transition-colors border-b last:border-0 group"
                  >
                    {/* Icono tipo */}
                    <div className={cn(
                      "p-2.5 rounded-full shrink-0",
                      isIncome ? "bg-emerald-500/10 text-emerald-500" : "bg-rose-500/10 text-rose-500"
                    )}>
                      {isIncome ? <ArrowUpRight size={16} /> : <ArrowDownLeft size={16} />}
                    </div>

                    {/* Descripción + categoría */}
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-semibold truncate group-hover:text-primary transition-colors">
                        {tx.description}
                      </p>
                      <div className="flex items-center gap-2 mt-0.5">
                        <span className="text-xs text-muted-foreground">
                          {formatDate(tx.date)}
                        </span>
                        <span className="size-1 rounded-full bg-border" />
                        <Badge variant="outline" className={cn("border-none text-xs font-medium", color)}>
                          {tx.category}
                        </Badge>
                      </div>
                    </div>

                    {/* Importe */}
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

                    {/* Acciones - visible en hover en desktop */}
                    <DropdownMenu>
                      <DropdownMenuTrigger asChild>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="size-8 opacity-0 group-hover:opacity-100 transition-opacity"
                          aria-label={t("transactions.moreOptions")}
                        >
                          <MoreHorizontal size={16} />
                        </Button>
                      </DropdownMenuTrigger>
                      <DropdownMenuContent align="end" className="bg-card border-border">
                        <DropdownMenuItem
                          className="cursor-pointer"
                          onClick={() => setEditingTransaction(tx)}
                        >
                          {t("transactions.edit")}
                        </DropdownMenuItem>
                        <DropdownMenuItem
                          className="text-destructive cursor-pointer"
                          onClick={() => handleDelete(tx.id)}
                        >
                          {t("transactions.delete")}
                        </DropdownMenuItem>
                      </DropdownMenuContent>
                    </DropdownMenu>
                  </div>
                )
              })}
            </div>

            {/* ========= VERSIÓN MÓVIL (<640px): Diseño de tarjetas apiladas ========= */}
            <div className="sm:hidden divide-y divide-border">
              {paginatedData.map((tx) => {
                const color = CATEGORY_COLORS[tx.category] || "text-muted-foreground bg-muted/10"
                const isIncome = tx.type === "INCOME"

                return (
                  <div
                    key={tx.id}
                    className="p-4 hover:bg-accent/30 transition-colors"
                  >
                    {/* Línea superior: Icono + Importe + Acciones */}
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
                          {Math.abs(tx.amount).toLocaleString("es-ES", {
                            minimumFractionDigits: 2,
                            maximumFractionDigits: 2
                          })} €
                        </p>
                      </div>

                      {/* Acciones siempre visibles en móvil */}
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button
                            variant="ghost"
                            size="icon"
                            className="size-8"
                            aria-label={t("transactions.moreOptions")}
                          >
                            <MoreHorizontal size={16} />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end" className="bg-card border-border">
                          <DropdownMenuItem
                            className="cursor-pointer"
                            onClick={() => setEditingTransaction(tx)}
                          >
                            {t("transactions.edit")}
                          </DropdownMenuItem>
                          <DropdownMenuItem
                            className="text-destructive cursor-pointer"
                            onClick={() => handleDelete(tx.id)}
                          >
                            {t("transactions.delete")}
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </div>

                    {/* Línea inferior: Descripción + fecha + categoría */}
                    <p className="text-sm font-medium truncate mb-1.5">
                      {tx.description}
                    </p>
                    <div className="flex items-center gap-2 flex-wrap">
                      <span className="text-xs text-muted-foreground">
                        {formatDate(tx.date)}
                      </span>
                      <span className="size-1 rounded-full bg-border hidden xs:inline-block" />
                      <Badge variant="outline" className={cn("border-none text-xs font-medium", color)}>
                        {tx.category}
                      </Badge>
                    </div>
                  </div>
                )
              })}
            </div>
          </>
        )}

        {/* PAGINACIÓN */}
        {totalPages > 1 && (
          <div className="px-4 sm:px-6 py-3 sm:py-4 flex flex-col xs:flex-row items-center justify-between gap-3 border-t border-border bg-background/20">
            <p className="text-xs text-muted-foreground font-medium text-center xs:text-left">
              {t("transactions.showingInterval", {
                from: (currentPage - 1) * ITEMS_PER_PAGE + 1,
                to: Math.min(currentPage * ITEMS_PER_PAGE, filteredData.length),
                total: filteredData.length
              })}
            </p>
            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={prevPage}
                disabled={currentPage === 1}
                className="h-8 border-border"
              >
                <ChevronLeft size={16} />
              </Button>
              <span className="text-sm text-muted-foreground min-w-12 text-center tabular-nums">
                {currentPage} / {totalPages}
              </span>
              <Button
                variant="outline"
                size="sm"
                onClick={nextPage}
                disabled={currentPage === totalPages}
                className="h-8 border-border"
              >
                <ChevronRight size={16} />
              </Button>
            </div>
          </div>
        )}
      </div>

      <EditTransactionDialog
        transaction={editingTransaction}
        open={!!editingTransaction}
        onOpenChange={(open) => { if (!open) setEditingTransaction(null) }}
      />
    </div>
  )
}