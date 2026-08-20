import { useState, useCallback } from "react"
import { useTranslation } from "react-i18next"
import { TopNav } from "@/shared/components/TopNav"
import { Sidebar } from "@/shared/components/Sidebar"
import { Header, type PeriodFilter } from "@/shared/components/Header"
import { Input } from "@/shared/components/core/input"
import { Loader2 } from "lucide-react"
import { Pagination } from "@/shared/components/Pagination"
import { EmptyState } from "../components/EmptyState"
import { SavingsGoalCard } from "../components/SavingsGoalCard"
import { ContributionModal } from "../components/ContributionModal"
import { SavingsGoalDialog } from "../components/SavingsGoalDialog"
import { SavingsGoalEditDialog } from "../components/SavingsGoalEditDialog"
import { ConfirmDeletionDialog } from "@/features/categories/components/ConfirmDeletionDialog"
import { useSavingsGoals, useAddContribution, useDeleteSavingsGoal } from "../hooks/useSavingsGoals"
import type { SavingsGoal } from "../types"

export const SavingsGoalsPage = () => {
  const { t } = useTranslation()
  const [period, setPeriod] = useState<PeriodFilter>("month")
  const [searchTerm, setSearchTerm] = useState("")
  const [ui, setUi] = useState({
    isContributionOpen: false,
    isEditOpen: false,
    deletingGoal: null as SavingsGoal | null,
  })
  const [selectedGoal, setSelectedGoal] = useState<SavingsGoal | null>(null)

  const {
    displayItems: goals,
    displayPage,
    displaySize,
    totalDisplayPages,
    totalElements,
    isLoading,
    error,
    nextPage,
    prevPage,
  } = useSavingsGoals({ search: searchTerm })
  const { mutateAsync: addContribution } = useAddContribution()
  const { mutateAsync: deleteGoal, isPending: isDeleting } = useDeleteSavingsGoal()

  const handlePeriodChange = useCallback((newPeriod: PeriodFilter) => {
    setPeriod(newPeriod)
  }, [])

  const from = totalElements === 0 ? 0 : displayPage * displaySize + 1
  const to = Math.min((displayPage + 1) * displaySize, totalElements)

  const handleAddContribution = async (amount: number) => {
    if (!selectedGoal) return
    await addContribution({ id: selectedGoal.id, amount })
    setUi((prev) => ({ ...prev, isContributionOpen: false }))
    setSelectedGoal(null)
  }

  const handleDelete = async () => {
    if (!ui.deletingGoal) return
    await deleteGoal(ui.deletingGoal.id)
    setUi((prev) => ({ ...prev, deletingGoal: null }))
  }

  const openContribution = (goal: SavingsGoal) => {
    setSelectedGoal(goal)
    setUi((prev) => ({ ...prev, isContributionOpen: true }))
  }

  const openEdit = (goal: SavingsGoal) => {
    setSelectedGoal(goal)
    setUi((prev) => ({ ...prev, isEditOpen: true }))
  }

  const openDelete = (goal: SavingsGoal) => {
    setUi((prev) => ({ ...prev, deletingGoal: goal }))
  }

  return (
    <div className="flex min-h-dvh overflow-hidden bg-background">
      <Sidebar />
      <div className="flex-1 flex flex-col overflow-hidden pt-16">
        <TopNav />
        <main className="flex-1 overflow-y-auto p-4 sm:p-6 space-y-4 sm:space-y-6">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 sm:gap-4">
            <Header
              onPeriodChange={handlePeriodChange}
              defaultPeriod={period}
              hidePeriodSelector
            />

            <div className="w-full sm:w-auto flex flex-col">
              <SavingsGoalDialog />
            </div>

          </div>

          {isLoading && (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
            </div>
          )}

          {error && (
            <div className="text-center text-sm text-destructive py-8">
              {t('savingsGoals:loadingError')}
            </div>
          )}

          {!isLoading && !error && (!goals || goals.length === 0) && (
            <div className="space-y-4">
              <div className="relative">
                <Input
                  placeholder={t('savingsGoals:searchPlaceholder')}
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full"
                />
                {searchTerm && (
                  <button
                    type="button"
                    onClick={() => setSearchTerm("")}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                    aria-label={t('savingsGoals:clearSearch')}
                  >
                    ✕
                  </button>
                )}
              </div>
              <EmptyState />
            </div>
          )}

          {!isLoading && !error && goals && goals.length > 0 && (
            <div className="space-y-4">
              <div className="relative">
                <Input
                  placeholder={t('savingsGoals:searchPlaceholder')}
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full"
                />
                {searchTerm && (
                  <button
                    type="button"
                    onClick={() => setSearchTerm("")}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                    aria-label={t('savingsGoals:clearSearch')}
                  >
                    ✕
                  </button>
                )}
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                {goals.map((goal) => (
                  <SavingsGoalCard
                    key={goal.id}
                    goal={goal}
                    onAddContribution={openContribution}
                    onEdit={openEdit}
                    onDelete={openDelete}
                  />
                ))}
              </div>

              <Pagination
                currentPage={displayPage}
                totalPages={totalDisplayPages}
                from={from}
                to={to}
                total={totalElements}
                onPrev={prevPage}
                onNext={nextPage}
              />
            </div>
          )}
        </main>
      </div>

      <ContributionModal
        isOpen={ui.isContributionOpen}
        onClose={() => {
          setUi((prev) => ({ ...prev, isContributionOpen: false }))
          setSelectedGoal(null)
        }}
        onSubmit={handleAddContribution}
        goal={selectedGoal}
      />

      <SavingsGoalEditDialog
        key={selectedGoal?.id}
        open={ui.isEditOpen}
        onOpenChange={(open) => {
          setUi((prev) => ({ ...prev, isEditOpen: open }))
          if (!open) setSelectedGoal(null)
        }}
        goal={selectedGoal}
      />

      <ConfirmDeletionDialog
        open={!!ui.deletingGoal}
        onOpenChange={(open) => {
          if (!open) setUi((prev) => ({ ...prev, deletingGoal: null }))
        }}
        itemName={ui.deletingGoal?.name || ""}
        onConfirm={handleDelete}
        isDeleting={isDeleting}
        title={t('savingsGoals:deleteGoalTitle')}
        description={t("savingsGoals.deleteGoalConfirmation", { name: ui.deletingGoal?.name })}
      />
    </div>
  )
}
