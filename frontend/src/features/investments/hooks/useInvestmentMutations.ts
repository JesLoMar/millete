import { useMutation, useQueryClient } from "@tanstack/react-query"
import { useTranslation } from "react-i18next"
import { apiClient } from "@/shared/api/axiosClient"
import { notify } from "@/shared/utils/notifications/notify"
import type { ApiError } from "@/shared/types/api"

export const useInvestmentMutations = () => {
  const queryClient = useQueryClient()
  const { t } = useTranslation()

  const invalidateAll = async () => {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['investments'] }),
      queryClient.invalidateQueries({ queryKey: ['investmentMetrics'] }),
      queryClient.invalidateQueries({ queryKey: ['investmentEvolution'] }),
      queryClient.invalidateQueries({ queryKey: ['investmentDistribution'] }),
    ])
  }

  const createInvestment = useMutation({
    mutationFn: (data: Record<string, unknown>) => {
      const sanitizedData = { ...data }
      
      if (typeof sanitizedData.purchaseDate === "string") {
        sanitizedData.purchaseDate = sanitizedData.purchaseDate.includes("T")
          ? sanitizedData.purchaseDate
          : `${sanitizedData.purchaseDate}T00:00:00`
      }
      
      return apiClient.post('investments', sanitizedData)
    },
    onSuccess: async () => {
      await invalidateAll()
      notify.success(t('investments.alerts.createSuccess'))
    },
    onError: (err: ApiError) => {
      notify.error(err.response?.data?.message || t('investments.alerts.createError') || "Error al crear la inversión")
    },
  })

  const updatePrice = useMutation({
    mutationFn: ({ id, price }: { id: string; price: number }) =>
      apiClient.patch(`investments/${id}/price`, { newPrice: price }),
    onSuccess: async () => {
      await invalidateAll()
      notify.success(t('investments.alerts.updatePriceSuccess'))
    },
    onError: (err: ApiError) => {
      notify.error(err.response?.data?.message || t('investments.alerts.updatePriceError') || "Error al actualizar el precio")
    },
  })

  const deleteInvestment = useMutation({
    mutationFn: (id: string) => apiClient.delete(`investments/${id}`),
    onSuccess: async () => {
      await invalidateAll()
      notify.success(t('investments.alerts.deleteSuccess'))
    },
    onError: (err: ApiError) => {
      notify.error(err.response?.data?.message || t('investments.alerts.deleteError') || "Error al eliminar la inversión")
    },
  })

  return {
    createInvestment,
    updatePrice,
    deleteInvestment,
    isCreating: createInvestment.isPending,
    isUpdating: updatePrice.isPending,
    isDeleting: deleteInvestment.isPending,
  }
}