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
  const [dialogs, setDialogs] = useState({
    isCreateOpen: false,
    isInviteOpen: false,
    isGoalEditOpen: false,
    isAddContributionOpen: false,
  })
  const [actions, setActions] = useState({
    editingGoal: null as GoalListItem | null,
    deletingGoal: null as GoalListItem | null,
    editMember: null as ContributionMember | null,
    deleteMemberId: null as string | null,
    deletingMemberName: "",
  })
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
    setDialogs((prev) => ({ ...prev, isCreateOpen: false }))
  }

  const handleEditGoalName = async (newName: string) => {
    if (!actions.editingGoal) return
    try {
      await apiClient.put(`/goals/${actions.editingGoal.id}`, { name: newName })
      await queryClient.invalidateQueries({ queryKey: ["group-goals"] })
      setActions((prev) => ({ ...prev, editingGoal: null }))
      notify.success("Nombre actualizado correctamente")
    } catch (err) {
      const apiError = err as ApiError
      notify.error(apiError?.response?.data?.message || "Error al actualizar el nombre")
    }
  }

  const handleDeleteGoal = async () => {
    if (!actions.deletingGoal) return
    await mutations.deleteGoal.mutateAsync(actions.deletingGoal.id)
    if (selectedGoalId === actions.deletingGoal.id) {
      setSelectedGoalId(null)
    }
    setActions((prev) => ({ ...prev, deletingGoal: null }))
  }

  const handleUpdateGoal = async (monthlyTarget: number, distributionMode: string) => {
    if (!selectedGoalId) return
    await mutations.updateGoal.mutateAsync({
      goalId: selectedGoalId,
      monthlyTarget,
      distributionMode,
    })
    setDialogs((prev) => ({ ...prev, isGoalEditOpen: false }))
  }

  const handleInviteMember = async (identifier: string) => {
    if (!selectedGoalId) return
    await mutations.inviteMember.mutateAsync(identifier)
    setDialogs((prev) => ({ ...prev, isInviteOpen: false }))
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
    setActions((prev) => ({ ...prev, editMember: null }))
  }

  const handleDeleteMember = async () => {
    if (!selectedGoalId || !actions.deleteMemberId) return
    await mutations.deleteMember.mutateAsync({
      goalId: selectedGoalId,
      memberId: actions.deleteMemberId,
    })
    setActions((prev) => ({ ...prev, deleteMemberId: null, deletingMemberName: "" }))
  }

  const openDeleteMember = (memberId: string) => {
    const member = contributionMembers.find(m => m.id === memberId)
    setActions((prev) => ({ ...prev, deleteMemberId: memberId, deletingMemberName: member?.name || "" }))
  }

  const handleAddContribution = async (amount: number) => {
    if (!selectedGoalId) return
    await mutations.addContribution.mutateAsync({ goalId: selectedGoalId, amount })
    setDialogs((prev) => ({ ...prev, isAddContributionOpen: false }))
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
              onCreateClick={() => setDialogs((prev) => ({ ...prev, isCreateOpen: true }))}
              onEditClick={(goal) => setActions((prev) => ({ ...prev, editingGoal: goal }))}
              onDeleteClick={(goal) => setActions((prev) => ({ ...prev, deletingGoal: goal }))}
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
              onInviteClick={() => setDialogs((prev) => ({ ...prev, isInviteOpen: true }))}
              onGoalClick={() => setDialogs((prev) => ({ ...prev, isGoalEditOpen: true }))}
              onEditMember={(member) => setActions((prev) => ({ ...prev, editMember: member }))}
              onDeleteMember={openDeleteMember}
              onModeChange={handleModeChange}
              onAddContribution={() => setDialogs((prev) => ({ ...prev, isAddContributionOpen: true }))}
            />
          ) : (
            <div className="flex items-center justify-center py-12">
              <p className="text-muted-foreground">Cargando detalles del Group Goal...</p>
            </div>
          )}
        </main>
      </div>

      <CreateGroupGoalDialog
        open={dialogs.isCreateOpen}
        onOpenChange={(open) => setDialogs((prev) => ({ ...prev, isCreateOpen: open }))}
        onCreate={handleCreateGoal}
      />

      <EditGoalNameDialog
        key={actions.editingGoal?.id}
        open={!!actions.editingGoal}
        onOpenChange={(open) => !open && setActions((prev) => ({ ...prev, editingGoal: null }))}
        currentName={actions.editingGoal?.name || ""}
        onSave={handleEditGoalName}
      />

      <ConfirmDeletionDialog
        open={!!actions.deletingGoal}
        onOpenChange={(open) => !open && setActions((prev) => ({ ...prev, deletingGoal: null }))}
        itemName={actions.deletingGoal?.name || ""}
        onConfirm={handleDeleteGoal}
        isDeleting={mutations.deleteGoal.isPending}
        title="Eliminar Group Goal"
        description={`¿Estás seguro de que deseas eliminar el Group Goal "${actions.deletingGoal?.name}"? Esta acción no se puede deshacer y todos los miembros serán eliminados.`}
      />

      <UpdateGoalDialog
        key={selectedGoalId}
        open={dialogs.isGoalEditOpen}
        onOpenChange={(open) => setDialogs((prev) => ({ ...prev, isGoalEditOpen: open }))}
        currentMonthlyTarget={selectedGoal?.monthlyTarget || 0}
        currentDistributionMode={selectedGoal?.distributionMode || "EQUITATIVE"}
        onSave={handleUpdateGoal}
      />

      <InviteMemberDialog
        open={dialogs.isInviteOpen}
        onOpenChange={(open) => setDialogs((prev) => ({ ...prev, isInviteOpen: open }))}
        onInvite={handleInviteMember}
      />

      <EditMemberDialog
        member={actions.editMember}
        open={!!actions.editMember}
        onOpenChange={(open) => !open && setActions((prev) => ({ ...prev, editMember: null }))}
        onSave={handleEditMember}
      />

      <ConfirmDeletionDialog
        open={!!actions.deleteMemberId}
        onOpenChange={(open) => !open && setActions((prev) => ({ ...prev, deleteMemberId: null }))}
        itemName={actions.deletingMemberName}
        onConfirm={handleDeleteMember}
        isDeleting={mutations.deleteMember.isPending}
        title="Eliminar miembro"
        description={`¿Estás seguro de que deseas eliminar a "${actions.deletingMemberName}" del Group Goal? Esta acción no se puede deshacer.`}
      />

      <AddContributionDialog
        open={dialogs.isAddContributionOpen}
        onOpenChange={(open) => setDialogs((prev) => ({ ...prev, isAddContributionOpen: open }))}
        onSave={handleAddContribution}
        isSaving={mutations.addContribution.isPending}
      />
    </div>
  )
}
