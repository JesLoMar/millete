import { useMutation, useQueryClient } from "@tanstack/react-query"
import { apiClient } from "@/shared/api/axiosClient"
import { useTranslation } from "react-i18next"
import { notify } from "@/shared/utils/notifications/notify"
import type { FamilyMember } from "../types"
import type { ApiError } from "@/shared/types/api"

export function useFamilyMutations(selectedFamilyId: string | null) {
  const queryClient = useQueryClient()
  const { t } = useTranslation()

  const invalidateAll = () => {
    queryClient.invalidateQueries({ queryKey: ['families'] })
    if (selectedFamilyId) {
      queryClient.invalidateQueries({ queryKey: ['family', selectedFamilyId] })
    }
  }

  const createFamily = useMutation({
    mutationFn: async ({ name, monthlyGoal }: { name: string; monthlyGoal: number }) => {
      return apiClient.post('/families', { name, monthlyTarget: monthlyGoal, distributionMode: "EQUITATIVE" })
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['families'] })
      notify.success(t("family.alerts.createSuccess") || "Familia creada correctamente")
    },
    onError: (err: ApiError) => {
      notify.error(err.response?.data?.message || t("family.alerts.createError") || "Error al crear la familia")
    }
  })

  const inviteMember = useMutation({
    mutationFn: async (email: string) => {
      if (!selectedFamilyId) throw new Error("No family selected")
      return apiClient.post(`/families/${selectedFamilyId}/invitations`, { email })
    },
    onSuccess: () => {
      invalidateAll()
      notify.success(t("family.alerts.inviteSuccess") || "Invitación enviada correctamente")
    },
    onError: (err: ApiError) => {
      notify.error(err.response?.data?.message || t("family.alerts.inviteError") || "Error al enviar la invitación")
    }
  })

  const changeMode = useMutation({
    mutationFn: async (mode: string) => {
      if (!selectedFamilyId) throw new Error("No family selected")
      return apiClient.put(`/families/${selectedFamilyId}`, { distributionMode: mode })
    },
    onSuccess: () => {
      invalidateAll()
      notify.success(t("family.alerts.modeSuccess") || "Modo de distribución actualizado")
    },
    onError: (err: ApiError) => {
      invalidateAll()
      notify.error(err.response?.data?.message || t("family.alerts.modeError") || "Error al cambiar el modo")
    }
  })

  const updateGoal = useMutation({
    mutationFn: async (newGoal: number) => {
      if (!selectedFamilyId) throw new Error("No family selected")
      return apiClient.put(`/families/${selectedFamilyId}`, { monthlyTarget: newGoal })
    },
    onSuccess: () => {
      invalidateAll()
      notify.success(t("family.alerts.goalSuccess") || "Objetivo mensual actualizado")
    },
    onError: (err: ApiError) => {
      notify.error(err.response?.data?.message || t("family.alerts.goalError") || "Error al actualizar el objetivo")
    }
  })

  const editMember = useMutation({
    mutationFn: async (member: FamilyMember) => {
      if (!selectedFamilyId) throw new Error("No family selected")
      return apiClient.put(`/families/${selectedFamilyId}/members/${member.id}`, {
        role: member.role,
        salary: member.salary,
        customPercentage: member.customPercentage
      })
    },
    onSuccess: () => {
      invalidateAll()
      notify.success(t("family.alerts.memberEditSuccess") || "Miembro actualizado correctamente")
    },
    onError: (err: ApiError) => {
      notify.error(err.response?.data?.message || t("family.alerts.memberEditError") || "Error al editar el miembro")
    }
  })

  const deleteMember = useMutation({
    mutationFn: async (memberId: string) => {
      if (!selectedFamilyId) throw new Error("No family selected")
      return apiClient.delete(`/families/${selectedFamilyId}/members/${memberId}`)
    },
    onSuccess: () => {
      invalidateAll()
      notify.success(t("family.alerts.memberDeleteSuccess") || "Miembro eliminado de la familia")
    },
    onError: (err: ApiError) => {
      notify.error(err.response?.data?.message || t("family.alerts.memberDeleteError") || "Error al eliminar el miembro")
    }
  })

  const addContribution = useMutation({
    mutationFn: async (amount: number) => {
      if (!selectedFamilyId) throw new Error("No family selected")
      return apiClient.post(`/families/${selectedFamilyId}/contributions`, { amount })
    },
    onSuccess: () => {
      invalidateAll()
      notify.success(t("family.alerts.contributionSuccess") || "Aportación registrada correctamente")
    },
    onError: (err: ApiError) => {
      notify.error(err.response?.data?.message || t("family.alerts.contributionError") || "Error al registrar aportación")
    }
  })

  return {
    handleCreateFamily: createFamily.mutateAsync,
    handleInviteMember: inviteMember.mutateAsync,
    handleModeChange: changeMode.mutateAsync,
    handleUpdateGoal: updateGoal.mutateAsync,
    handleEditMember: editMember.mutateAsync,
    handleDeleteMember: deleteMember.mutateAsync,
    handleAddContribution: addContribution.mutateAsync,
    isCreating: createFamily.isPending,
    isInviting: inviteMember.isPending,
    isChangingMode: changeMode.isPending,
    isUpdatingGoal: updateGoal.isPending,
    isEditingMember: editMember.isPending,
    isDeletingMember: deleteMember.isPending,
    isAddingContribution: addContribution.isPending,
  }
}