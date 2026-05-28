import { useState, useMemo, useEffect, useCallback } from "react"
import { useTranslation } from "react-i18next"
import { useQuery } from "@tanstack/react-query"
import { Search, ChevronLeft, ChevronRight } from "lucide-react"
import { Input } from "@/shared/components/ui/input"
import { Button } from "@/shared/components/ui/button"
import { useCategories, type Category } from "@/shared/hooks/useCategories"
import { useCategoryMutations } from "../hooks/useCategoryMutation"
import { apiClient } from "@/shared/api/axiosClient"
import { EditCategoryDialog } from "./EditCategoryDialog"
import { ConfirmDeletionDialog } from "./ConfirmDeletionDialog"
import { CategoryRow } from "./CategoryRow"
import { CategoryTableSkeleton } from "./CategoryTableSkeleton"
import { usePagination } from "../hooks/usePagination"
import type { PeriodFilter } from "@/shared/components/PeriodSelector"
import type { CategoriesExpenseResponse } from "../types"

interface CategoryTableProps {
  period: PeriodFilter
}

export function CategoryTable({ period }: CategoryTableProps) {
  const { t } = useTranslation()
  const { data: categories = [], isLoading } = useCategories()
  const { deleteCategory, isDeleting } = useCategoryMutations()

  const [searchTerm, setSearchTerm] = useState("")
  const [editingCategory, setEditingCategory] = useState<Category | null>(null)
  const [deletingCategory, setDeletingCategory] = useState<Category | null>(null)

  const filteredData = useMemo(() =>
    categories.filter((cat) =>
      cat.name.toLowerCase().includes(searchTerm.toLowerCase())
    ), [categories, searchTerm])

  const { currentPage, totalPages, paginatedRange, prevPage, nextPage, resetPage } = usePagination({
    totalItems: filteredData.length,
  })

  const paginatedItems = useMemo(() =>
    filteredData.slice(paginatedRange.start, paginatedRange.end),
    [filteredData, paginatedRange])

  useEffect(() => {
    resetPage()
  }, [searchTerm, resetPage])

  const { data: expensesData } = useQuery<CategoriesExpenseResponse>({
    queryKey: ['categoryExpenses', period],
    queryFn: async () => {
      const response = await apiClient.get(`/dashboard/categories?period=${period}`)
      return response.data
    },
  })

  const expensesMap = useMemo(() => {
    if (!expensesData?.categories) return {}
    const map: Record<string, number> = {}
    expensesData.categories.forEach((cat) => {
      map[cat.name] = cat.amount
    })
    return map
  }, [expensesData])

  const getAdjustedBudgetLimit = useCallback((budgetLimit: number | null | undefined): number | null => {
    if (!budgetLimit) return null
    switch (period) {
      case "week": return budgetLimit / 4
      case "month": return budgetLimit
      case "year": return budgetLimit * 12
      default: return budgetLimit
    }
  }, [period])

  const handleDelete = async () => {
    if (!deletingCategory) return

    try {
      await deleteCategory.mutateAsync(deletingCategory.id)
      setDeletingCategory(null)
    } catch {
    }
  }

  const getSpentPercentage = (cat: Category): number => {
    const limit = getAdjustedBudgetLimit(cat.budgetLimit)
    if (!limit) return 0
    const spent = expensesMap[cat.name] || 0
    return Math.min((spent / limit) * 100, 100)
  }

  if (isLoading) return <CategoryTableSkeleton />

  return (
    <div className="space-y-4">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="relative w-full sm:w-[320px]">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground" />
          <Input
            placeholder={t("categories.search")}
            className="pl-10 bg-card border-border h-10"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>
        <div className="flex items-center gap-3">
          <span className="text-xs font-medium text-muted-foreground bg-secondary/50 px-3 py-1.5 rounded-lg">
            {t(`dashboard.header.period.${period}`)}
          </span>
          <p className="text-sm text-muted-foreground">
            {t("categories.showing", { total: filteredData.length })}
          </p>
        </div>
      </div>

      <div className="bg-card border border-border rounded-xl overflow-hidden">
        <div className="flex flex-col">
          {paginatedItems.length === 0 ? (
            <p className="text-center text-muted-foreground py-12 text-sm">
              {t("categories.empty")}
            </p>
          ) : (
            paginatedItems.map((cat) => (
              <CategoryRow
                key={cat.id}
                category={cat}
                spent={expensesMap[cat.name] || 0}
                budgetLimit={getAdjustedBudgetLimit(cat.budgetLimit)}
                percentage={getSpentPercentage(cat)}
                onEdit={setEditingCategory}
                onDelete={setDeletingCategory}
              />
            ))
          )}
        </div>

        {totalPages > 1 && (
          <div className="px-6 py-4 flex items-center justify-between border-t border-border bg-background/20">
            <p className="text-xs text-muted-foreground font-medium">
              {t("transactions.showingInterval", {
                from: (currentPage - 1) * 10 + 1,
                to: Math.min(currentPage * 10, filteredData.length),
                total: filteredData.length,
              })}
            </p>
            <div className="flex items-center gap-2">
              <Button variant="outline" size="sm" onClick={prevPage} disabled={currentPage === 1} className="h-8 border-border">
                <ChevronLeft size={16} />
              </Button>
              <span className="text-sm text-muted-foreground min-w-15 text-center">{currentPage} / {totalPages}</span>
              <Button variant="outline" size="sm" onClick={nextPage} disabled={currentPage === totalPages} className="h-8 border-border">
                <ChevronRight size={16} />
              </Button>
            </div>
          </div>
        )}
      </div>

      <EditCategoryDialog
        category={editingCategory}
        open={!!editingCategory}
        onOpenChange={(open) => { if (!open) setEditingCategory(null) }}
      />

      <ConfirmDeletionDialog
        open={!!deletingCategory}
        onOpenChange={(open) => { if (!open) setDeletingCategory(null) }}
        itemName={deletingCategory?.name || ""}
        onConfirm={handleDelete}
        isDeleting={isDeleting}
      />
    </div>
  )
}