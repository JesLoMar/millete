import { useMutation, useQueryClient } from "@tanstack/react-query"
import { apiClient } from "@/shared/api/axiosClient"
import { useTranslation } from "react-i18next"
import { notify } from "@/shared/utils/notifications/notify"
import type { ApiError } from "@/shared/types/api"

export function useGroupGoalMutations(selectedGoalId: string | null) {
  const queryClient = useQueryClient()
  const { t } = useTranslation()

  const invalidateAll = () => {
    queryClient.invalidateQueries({ queryKey: ["group-goals"] })
    if (selectedGoalId) {
      queryClient.invalidateQueries({ queryKey: ["group-goals", selectedGoalId] })
      queryClient.refetchQueries({ queryKey: ["group-goals", selectedGoalId] })
    }
  }

  const createGoal = useMutation({
    mutationFn: async ({
      name,
      monthlyTarget,
      distributionMode,
    }: {
      name: string
      monthlyTarget: number
      distributionMode: string
    }) => {
      return apiClient.post("/group-goals", {
        name,
        monthlyTarget,
        distributionMode,
      })
    },
    onSuccess: () => {
      invalidateAll()
      notify.success(t("groupGoals.alerts.createSuccess"))
    },
    onError: (err: ApiError) => {
      notify.error(
        err.response?.data?.message || t("groupGoals.alerts.createError")
      )
    },
  })

  const inviteMember = useMutation({
    mutationFn: async (identifier: string) => {
      if (!selectedGoalId) throw new Error("No goal selected")
      return apiClient.post(`/group-goals/${selectedGoalId}/invite`, {
        identifier,
      })
    },
    onSuccess: () => {
      invalidateAll()
      notify.success(t("groupGoals.alerts.inviteSuccess"))
    },
    onError: (err: ApiError) => {
      notify.error(
        err.response?.data?.message || t("groupGoals.alerts.inviteError")
      )
    },
  })

  const updateGoal = useMutation({
    mutationFn: async ({
      goalId,
      monthlyTarget,
      distributionMode,
      name,
    }: {
      goalId: string
      monthlyTarget?: number
      distributionMode?: string
      name?: string
    }) => {
      const payload: Record<string, unknown> = {}
      if (monthlyTarget !== undefined) payload.monthlyTarget = monthlyTarget
      if (distributionMode !== undefined) payload.distributionMode = distributionMode
      if (name !== undefined) payload.name = name

      return apiClient.put(`/group-goals/${goalId}`, payload)
    },
    onSuccess: () => {
      invalidateAll()
      notify.success(t("groupGoals.alerts.goalSuccess"))
    },
    onError: (err: ApiError) => {
      notify.error(
        err.response?.data?.message || t("groupGoals.alerts.goalError")
      )
    },
  })

  const deleteGoal = useMutation({
    mutationFn: async (goalId: string) => {
      return apiClient.delete(`/group-goals/${goalId}`)
    },
    onSuccess: () => {
      invalidateAll()
      notify.success(t("groupGoals.alerts.deleteSuccess"))
    },
    onError: (err: ApiError) => {
      notify.error(
        err.response?.data?.message || t("groupGoals.alerts.deleteError")
      )
    },
  })

  const updateMember = useMutation({
    mutationFn: async ({
      goalId,
      memberId,
      role,
      salary,
      customPercentage,
    }: {
      goalId: string
      memberId: string
      role?: string
      salary?: number
      customPercentage?: number
    }) => {
      const payload: Record<string, unknown> = {}
      if (role !== undefined) payload.role = role
      if (salary !== undefined) payload.salary = salary
      if (customPercentage !== undefined) payload.customPercentage = customPercentage

      return apiClient.put(`/group-goals/${goalId}/members/${memberId}`, payload)
    },
    onSuccess: () => {
      invalidateAll()
      notify.success(t("groupGoals.alerts.memberEditSuccess"))
    },
    onError: (err: ApiError) => {
      notify.error(
        err.response?.data?.message || t("groupGoals.alerts.memberEditError")
      )
    },
  })

  const deleteMember = useMutation({
    mutationFn: async ({
      goalId,
      memberId,
    }: {
      goalId: string
      memberId: string
    }) => {
      return apiClient.delete(`/group-goals/${goalId}/members/${memberId}`)
    },
    onSuccess: () => {
      invalidateAll()
      notify.success(t("groupGoals.alerts.memberDeleteSuccess"))
    },
    onError: (err: ApiError) => {
      notify.error(
        err.response?.data?.message || t("groupGoals.alerts.memberDeleteError")
      )
    },
  })

  const addContribution = useMutation({
    mutationFn: async ({
      goalId,
      amount,
    }: {
      goalId: string
      amount: number
    }) => {
      return apiClient.post(`/group-goals/${goalId}/contributions`, { amount })
    },
    onSuccess: () => {
      invalidateAll()
      notify.success(t("groupGoals.alerts.contributionSuccess"))
    },
    onError: (err: ApiError) => {
      notify.error(
        err.response?.data?.message || t("groupGoals.alerts.contributionError")
      )
    },
  })

  return {
    createGoal,
    inviteMember,
    updateGoal,
    deleteGoal,
    updateMember,
    deleteMember,
    addContribution,
  }
}