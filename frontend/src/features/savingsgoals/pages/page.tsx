import { useState, useCallback, useMemo } from "react"
import { useTranslation } from "react-i18next"
import { TopNav } from "@/shared/components/TopNav"
import { Sidebar } from "@/shared/components/Sidebar"
import { Header, type PeriodFilter } from "@/shared/components/Header"
import { PeriodSelector } from "@/shared/components/PeriodSelector"
import { Input } from "@/shared/components/core/input"
import { Loader2 } from "lucide-react"
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
  const [ui, setUi] = useState({
    search: "",
    isContributionOpen: false,
    isEditOpen: false,
    deletingGoal: null as SavingsGoal | null,
  })
  const [selectedGoal, setSelectedGoal] = useState<SavingsGoal | null>(null)

  const { data: goals, isLoading, error } = useSavingsGoals()
  const { mutateAsync: addContribution } = useAddContribution()
  const { mutateAsync: deleteGoal, isPending: isDeleting } = useDeleteSavingsGoal()

  const handlePeriodChange = useCallback((newPeriod: PeriodFilter) => {
    setPeriod(newPeriod)
  }, [])

  const filteredGoals = useMemo(() => {
    if (!goals) return []
    if (!ui.search.trim()) return goals
    const lower = ui.search.toLowerCase()
    return goals.filter((g) => g.name.toLowerCase().includes(lower))
  }, [goals, ui.search])

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
      <div className="flex-1 flex flex-col overflow-hidden">
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

          <div className="relative">
            <Input
              placeholder={t('savingsGoals:searchPlaceholder')}
              value={ui.search}
              onChange={(e) => setUi((prev) => ({ ...prev, search: e.target.value }))}
              className="w-full"
            />
            {ui.search && (
              <button
                type="button"
                onClick={() => setUi((prev) => ({ ...prev, search: "" }))}
                className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                aria-label={t('savingsGoals:clearSearch')}
              >
                ✕
              </button>
            )}
          </div>

          {isLoading && (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
            </div>
          )}

          {error && (
            <div className="text-center text-sm text-red-500 py-8">
              {t('savingsGoals:loadingError')}
            </div>
          )}

          {!isLoading && !error && filteredGoals.length === 0 && <EmptyState />}

          {!isLoading && filteredGoals.length > 0 && (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {filteredGoals.map((goal) => (
                <SavingsGoalCard
                  key={goal.id}
                  goal={goal}
                  onAddContribution={openContribution}
                  onEdit={openEdit}
                  onDelete={openDelete}
                />
              ))}
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
