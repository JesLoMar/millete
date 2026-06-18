import { useState, useMemo, useCallback } from "react"
import { TopNav } from "@/shared/components/TopNav"
import { Sidebar } from "@/shared/components/Sidebar"
import { GroupGoalSelector } from "@/features/groupgoals/components/GroupGoalSelector"
import { GroupGoalDetail } from "@/features/groupgoals/components/GroupGoalDetail"
import { CreateGroupGoalDialog } from "@/features/groupgoals/components/dialogs/CreateGroupGoalDialog"
import { InviteMemberDialog } from "@/features/groupgoals/components/dialogs/InviteMemberDialog"
import { UpdateGoalDialog } from "@/features/groupgoals/components/dialogs/UpdateGoalDialog"
import { EditMemberDialog } from "@/features/groupgoals/components/dialogs/EditMemberDialog"
import { EditGoalNameDialog } from "@/features/groupgoals/components/dialogs/EditGoalNameDialog"
import { AddContributionDialog } from "@/features/groupgoals/components/dialogs/AddContributionDialog"
import { ConfirmDeletionDialog } from "@/features/categories/components/ConfirmDeletionDialog"
import { useGroupGoalQueries } from "@/features/groupgoals/hooks/useGroupGoalQueries"
import { useGroupGoalMutations } from "@/features/groupgoals/hooks/useGroupGoalMutations"
import { calculateContributions } from "@/features/groupgoals/utils"
import { notify } from "@/shared/utils/notifications/notify"
import { apiClient } from "@/shared/api/axiosClient"
import { useQueryClient } from "@tanstack/react-query"
import type { ContributionMember, GoalListItem } from "@/features/groupgoals/types"
import type { ApiError } from "@/shared/types/api"

export const GroupGoalsPage = () => {
  const queryClient = useQueryClient()

  const [selectedGoalId, setSelectedGoalId] = useState<string | null>(null)
  const [isCreateOpen, setIsCreateOpen] = useState(false)
  const [isInviteOpen, setIsInviteOpen] = useState(false)
  const [isGoalEditOpen, setIsGoalEditOpen] = useState(false)
  const [isAddContributionOpen, setIsAddContributionOpen] = useState(false)
  const [editingGoal, setEditingGoal] = useState<GoalListItem | null>(null)
  const [deletingGoal, setDeletingGoal] = useState<GoalListItem | null>(null)
  const [editMember, setEditMember] = useState<ContributionMember | null>(null)
  const [deleteMemberId, setDeleteMemberId] = useState<string | null>(null)
  const [deletingMemberName, setDeletingMemberName] = useState("")
  const [customPercentages, setCustomPercentages] = useState<Record<string, number>>({})

  const { goals, isLoading: isLoadingList, selectedGoal } = useGroupGoalQueries(selectedGoalId)
  const mutations = useGroupGoalMutations(selectedGoalId)

  // 1º - Calcular totalCustomPercentage PRIMERO
  const totalCustomPercentage = useMemo(() => {
    return Object.values(customPercentages).reduce((sum, p) => sum + p, 0)
  }, [customPercentages])

  // 2º - Luego contributionMembers que usa totalCustomPercentage
  const contributionMembers: ContributionMember[] = useMemo(() => {
    if (!selectedGoal) return []
    return calculateContributions(selectedGoal, customPercentages, totalCustomPercentage)
  }, [selectedGoal, customPercentages, totalCustomPercentage])

  const totalContributed = contributionMembers.reduce((sum, m) => sum + m.contributed, 0)
  const percentageCompleted = selectedGoal
    ? selectedGoal.monthlyTarget > 0
      ? (totalContributed / selectedGoal.monthlyTarget) * 100
      : 0
    : 0

  const handleCreateGoal = async (name: string, monthlyTarget: number, distributionMode: string) => {
    await mutations.createGoal.mutateAsync({ name, monthlyTarget, distributionMode })
    setIsCreateOpen(false)
  }

  const handleEditGoalName = async (newName: string) => {
    if (!editingGoal) return
    try {
      await apiClient.put(`/goals/${editingGoal.id}`, { name: newName })
      queryClient.invalidateQueries({ queryKey: ["group-goals"] })
      notify.success("Nombre actualizado correctamente")
    } catch (err) {
      const apiError = err as ApiError
      notify.error(apiError?.response?.data?.message || "Error al actualizar el nombre")
    }
  }

  const handleDeleteGoal = async () => {
    if (!deletingGoal) return
    await mutations.deleteGoal.mutateAsync(deletingGoal.id)
    if (selectedGoalId === deletingGoal.id) {
      setSelectedGoalId(null)
    }
    setDeletingGoal(null)
  }

  const handleUpdateGoal = async (monthlyTarget: number, distributionMode: string) => {
    if (!selectedGoalId) return
    await mutations.updateGoal.mutateAsync({
      goalId: selectedGoalId,
      monthlyTarget,
      distributionMode,
    })
    setIsGoalEditOpen(false)
  }

  const handleInviteMember = async (identifier: string) => {
    if (!selectedGoalId) return
    await mutations.inviteMember.mutateAsync(identifier)
    setIsInviteOpen(false)
  }

  const handleEditMember = async (memberId: string, role: string, salary: number, customPercentage?: number) => {
    if (!selectedGoalId) return
    await mutations.updateMember.mutateAsync({
      goalId: selectedGoalId,
      memberId,
      role,
      salary,
      customPercentage,
    })
    setEditMember(null)
  }

  const handleDeleteMember = async () => {
    if (!selectedGoalId || !deleteMemberId) return
    await mutations.deleteMember.mutateAsync({
      goalId: selectedGoalId,
      memberId: deleteMemberId,
    })
    setDeleteMemberId(null)
    setDeletingMemberName("")
  }

  const openDeleteMember = (memberId: string) => {
    const member = contributionMembers.find(m => m.id === memberId)
    setDeleteMemberId(memberId)
    setDeletingMemberName(member?.name || "")
  }

  const handleAddContribution = async (amount: number) => {
    if (!selectedGoalId) return
    await mutations.addContribution.mutateAsync({ goalId: selectedGoalId, amount })
    setIsAddContributionOpen(false)
  }

  const handleCustomPercentageChange = useCallback(
    (member: ContributionMember, percentage: number) => {
      setCustomPercentages((prev) => ({ ...prev, [member.userId]: percentage }))
    },
    []
  )

  const handleModeChange = (mode: string) => {
    if (!selectedGoalId) return
    mutations.updateGoal.mutate({ goalId: selectedGoalId, distributionMode: mode })
  }

  return (
    <div className="flex min-h-dvh overflow-hidden bg-background">
      <Sidebar />
      <div className="flex-1 flex flex-col overflow-hidden">
        <TopNav />
        <main className="flex-1 overflow-y-auto p-4 sm:p-6">
          {!selectedGoalId ? (
            <GroupGoalSelector
              goals={goals || []}
              isLoading={isLoadingList}
              onSelect={setSelectedGoalId}
              onCreateClick={() => setIsCreateOpen(true)}
              onEditClick={setEditingGoal}
              onDeleteClick={setDeletingGoal}
            />
          ) : selectedGoal ? (
            <GroupGoalDetail
              goal={selectedGoal}
              contributions={contributionMembers}
              totalContributed={totalContributed}
              percentageCompleted={percentageCompleted}
              customPercentages={customPercentages}
              onCustomPercentageChange={handleCustomPercentageChange}
              totalCustomPercentage={totalCustomPercentage}
              onBack={() => setSelectedGoalId(null)}
              onInviteClick={() => setIsInviteOpen(true)}
              onGoalClick={() => setIsGoalEditOpen(true)}
              onEditMember={setEditMember}
              onDeleteMember={openDeleteMember}
              onModeChange={handleModeChange}
              onAddContribution={() => setIsAddContributionOpen(true)}
            />
          ) : (
            <div className="flex items-center justify-center py-12">
              <p className="text-muted-foreground">Cargando detalles del Group Goal...</p>
            </div>
          )}
        </main>
      </div>

      <CreateGroupGoalDialog
        open={isCreateOpen}
        onOpenChange={setIsCreateOpen}
        onCreate={handleCreateGoal}
      />

      <EditGoalNameDialog
        open={!!editingGoal}
        onOpenChange={(open) => !open && setEditingGoal(null)}
        currentName={editingGoal?.name || ""}
        onSave={handleEditGoalName}
      />

      <ConfirmDeletionDialog
        open={!!deletingGoal}
        onOpenChange={(open) => !open && setDeletingGoal(null)}
        itemName={deletingGoal?.name || ""}
        onConfirm={handleDeleteGoal}
        isDeleting={mutations.deleteGoal.isPending}
        title="Eliminar Group Goal"
        description={`¿Estás seguro de que deseas eliminar el Group Goal "${deletingGoal?.name}"? Esta acción no se puede deshacer y todos los miembros serán eliminados.`}
      />

      <UpdateGoalDialog
        open={isGoalEditOpen}
        onOpenChange={setIsGoalEditOpen}
        currentMonthlyTarget={selectedGoal?.monthlyTarget || 0}
        currentDistributionMode={selectedGoal?.distributionMode || "EQUITATIVE"}
        onSave={handleUpdateGoal}
      />

      <InviteMemberDialog
        open={isInviteOpen}
        onOpenChange={setIsInviteOpen}
        onInvite={handleInviteMember}
      />

      <EditMemberDialog
        member={editMember}
        open={!!editMember}
        onOpenChange={(open) => !open && setEditMember(null)}
        onSave={handleEditMember}
      />

      <ConfirmDeletionDialog
        open={!!deleteMemberId}
        onOpenChange={(open) => !open && setDeleteMemberId(null)}
        itemName={deletingMemberName}
        onConfirm={handleDeleteMember}
        isDeleting={mutations.deleteMember.isPending}
        title="Eliminar miembro"
        description={`¿Estás seguro de que deseas eliminar a "${deletingMemberName}" del Group Goal? Esta acción no se puede deshacer.`}
      />

      <AddContributionDialog
        open={isAddContributionOpen}
        onOpenChange={setIsAddContributionOpen}
        onSave={handleAddContribution}
        isSaving={mutations.addContribution.isPending}
      />
    </div>
  )
}