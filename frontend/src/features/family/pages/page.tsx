import { useState, useMemo, useCallback } from "react"
import { TopNav } from "@/shared/components/TopNav"
import { Sidebar } from "@/shared/components/Sidebar"
import { FamilySelector } from "../components/FamilySelector"
import { FamilyDetail } from "../components/FamilyDetail"
import { CreateFamilyDialog } from "../components/dialogs/CreateFamilyDialog"
import { InviteMemberDialog } from "../components/dialogs/InviteMemberDialog"
import { ChangeGoalDialog } from "../components/dialogs/ChangeGoalDialog"
import { EditMemberDialog } from "../components/dialogs/EditMemberDialog"
import { AddContributionDialog } from "../components/dialogs/AddContributionDialog"
import { ConfirmDeletionDialog } from "@/features/categories/components/ConfirmDeletionDialog"
import { useFamilyQueries } from "../hooks/useFamilyQueries"
import { useFamilyMutations } from "../hooks/useFamilyMutations"
import { calculateContributions } from "../utils"
import type { FamilyMember, ContributionMember } from "../types"

export const FamilyPage = () => {
  const [selectedFamilyId, setSelectedFamilyId] = useState<string | null>(null)
  const [isCreateOpen, setIsCreateOpen] = useState(false)
  const [isInviteOpen, setIsInviteOpen] = useState(false)
  const [isGoalOpen, setIsGoalOpen] = useState(false)
  const [editingMember, setEditingMember] = useState<FamilyMember | null>(null)
  const [isAddContributionOpen, setIsAddContributionOpen] = useState(false)
  const [deletingMemberId, setDeletingMemberId] = useState<string | null>(null)
  const [customPercentages, setCustomPercentages] = useState<Record<string, number>>({})

  const { families, isLoading, selectedFamily } = useFamilyQueries(selectedFamilyId)
  const mutations = useFamilyMutations(selectedFamilyId)

  const totalCustomPercentage = useMemo(() => {
    return Object.values(customPercentages).reduce((sum, p) => sum + p, 0)
  }, [customPercentages])

  const contributions: ContributionMember[] = useMemo(() => {
    if (!selectedFamily) return []
    return calculateContributions(selectedFamily, customPercentages, totalCustomPercentage)
  }, [selectedFamily, customPercentages, totalCustomPercentage])

  const totalContributed = contributions.reduce((sum, m) => sum + m.contributed, 0)
  const monthlyGoal = selectedFamily?.monthlyGoal ?? 0
  const percentageCompleted = monthlyGoal > 0 ? (totalContributed / monthlyGoal) * 100 : 0

  const handleCustomPercentageChange = useCallback((member: ContributionMember, percentage: number) => {
    setCustomPercentages(prev => ({
      ...prev,
      [member.id]: percentage
    }))
    mutations.handleEditMember({
      id: member.id,
      name: member.name,
      role: member.role,
      salary: member.salary,
      customPercentage: percentage,
    })
  }, [mutations])

  const handleCreateFamily = async (name: string, monthlyGoal: number) => {
    await mutations.handleCreateFamily({ name, monthlyGoal })
    setIsCreateOpen(false)
  }

  const handleInviteMember = async (email: string) => {
    await mutations.handleInviteMember(email)
    setIsInviteOpen(false)
  }

  const handleUpdateGoal = async (newGoal: number) => {
    await mutations.handleUpdateGoal(newGoal)
    setIsGoalOpen(false)
  }

  const handleEditMember = async (member: FamilyMember) => {
    await mutations.handleEditMember(member)
    setEditingMember(null)
  }

  const handleDeleteMember = async () => {
    if (!deletingMemberId) return
    await mutations.handleDeleteMember(deletingMemberId)
    setDeletingMemberId(null)
  }

  const getDeletingMemberName = () => {
    const member = contributions.find(m => m.id === deletingMemberId)
    return member?.name || ""
  }

  return (
    <div className="flex h-screen overflow-hidden bg-background">
      <Sidebar />
      <div className="flex-1 flex flex-col overflow-hidden">
        <TopNav />
        <main className="flex-1 overflow-y-auto p-6">
          {!selectedFamilyId ? (
            <FamilySelector
              families={families}
              isLoading={isLoading}
              onSelect={setSelectedFamilyId}
              onCreateClick={() => setIsCreateOpen(true)}
            />
          ) : selectedFamily ? (
            <FamilyDetail
              family={selectedFamily}
              contributions={contributions}
              totalContributed={totalContributed}
              percentageCompleted={percentageCompleted}
              customPercentages={customPercentages}
              onCustomPercentageChange={handleCustomPercentageChange}
              totalCustomPercentage={totalCustomPercentage}
              onBack={() => setSelectedFamilyId(null)}
              onInviteClick={() => setIsInviteOpen(true)}
              onGoalClick={() => setIsGoalOpen(true)}
              onEditMember={setEditingMember}
              onDeleteMember={setDeletingMemberId}
              onModeChange={mutations.handleModeChange}
              onAddContribution={() => setIsAddContributionOpen(true)}
            />
          ) : null}
        </main>
      </div>

      <CreateFamilyDialog open={isCreateOpen} onOpenChange={setIsCreateOpen} onCreate={handleCreateFamily} />
      <InviteMemberDialog open={isInviteOpen} onOpenChange={setIsInviteOpen} onInvite={handleInviteMember} />
      <ChangeGoalDialog open={isGoalOpen} onOpenChange={setIsGoalOpen} currentGoal={selectedFamily?.monthlyGoal || 0} onSave={handleUpdateGoal} />
      <EditMemberDialog member={editingMember} open={!!editingMember} onOpenChange={(open) => { if (!open) setEditingMember(null) }} onSave={handleEditMember} />
      <AddContributionDialog
        open={isAddContributionOpen}
        onOpenChange={setIsAddContributionOpen}
        isSaving={mutations.isAddingContribution}
        onSave={async (amount) => {
          await mutations.handleAddContribution(amount)
        }}
      />
      <ConfirmDeletionDialog
        open={!!deletingMemberId}
        onOpenChange={(open) => { if (!open) setDeletingMemberId(null) }}
        itemName={getDeletingMemberName()}
        onConfirm={handleDeleteMember}
      />
    </div>
  )
}