import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useTranslation } from 'react-i18next'

import { apiClient } from '@/shared/api/axiosClient'
import type { ApiError } from '@/shared/types/api'
import { notify } from '@/shared/utils/notifications/notify'

import type {
  RegisterInvestmentRequest,
  UpdateInvestmentPriceRequest,
} from '../types'

export const useInvestmentMutations = () => {
  const queryClient = useQueryClient()
  const { t } = useTranslation()

  const createInvestment = useMutation({
    mutationFn: (data: RegisterInvestmentRequest) => {
      const sanitizedData = { ...data }

      const date = new Date(sanitizedData.purchaseDate)

      sanitizedData.purchaseDate = date.toISOString()

      return apiClient.post(
        '/investments',
        sanitizedData,
        {
          skipGlobalErrorNotify: true,
        },
      )
    },

    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: ['investments'],
        }),
        queryClient.invalidateQueries({
          queryKey: ['investmentMetrics'],
        }),
        queryClient.invalidateQueries({
          queryKey: ['investmentEvolution'],
        }),
        queryClient.invalidateQueries({
          queryKey: ['investmentDistribution'],
        }),
      ])

      notify.success(
        t('investments:alerts.createSuccess'),
      )
    },

    onError: (err: ApiError) => {
      notify.error(
        err.response?.data?.message ||
          t('investments:alerts.createError'),
      )
    },
  })

  const updatePrice = useMutation({
    mutationFn: ({
      id,
      currentPrice,
    }: UpdateInvestmentPriceRequest) =>
      apiClient.patch(
        `investments/${id}/price`,
        {
          newPrice: currentPrice,
        },
        {
          skipGlobalErrorNotify: true,
        },
      ),

    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: ['investments'],
        }),
        queryClient.invalidateQueries({
          queryKey: ['investmentMetrics'],
        }),
        queryClient.invalidateQueries({
          queryKey: ['investmentEvolution'],
        }),
        queryClient.invalidateQueries({
          queryKey: ['investmentDistribution'],
        }),
      ])

      notify.success(
        t('investments:alerts.updatePriceSuccess'),
      )
    },

    onError: (err: ApiError) => {
      notify.error(
        err.response?.data?.message ||
          t('investments:alerts.updatePriceError'),
      )
    },
  })

  const deleteInvestment = useMutation({
    mutationFn: (id: string) =>
      apiClient.delete(
        `investments/${id}`,
        {
          skipGlobalErrorNotify: true,
        },
      ),

    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: ['investments'],
        }),
        queryClient.invalidateQueries({
          queryKey: ['investmentMetrics'],
        }),
        queryClient.invalidateQueries({
          queryKey: ['investmentEvolution'],
        }),
        queryClient.invalidateQueries({
          queryKey: ['investmentDistribution'],
        }),
      ])

      notify.success(
        t('investments:alerts.deleteSuccess'),
      )
    },

    onError: (err: ApiError) => {
      notify.error(
        err.response?.data?.message ||
          t('investments:alerts.deleteError'),
      )
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