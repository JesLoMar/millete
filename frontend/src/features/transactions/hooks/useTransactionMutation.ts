import {
  useMutation,
  useQueryClient,
} from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';

import { apiClient } from '@/shared/api/axiosClient';
import type { ApiError } from '@/shared/types/api';
import { notify } from '@/shared/utils/notifications/notify';

export const useInvestmentMutations = () => {
  const queryClient = useQueryClient();
  const { t } = useTranslation('investments');

  const invalidateInvestmentQueries = async () => {
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
    ]);
  };

  const createInvestment = useMutation({
    mutationFn: (data: Record<string, unknown>) => {
      const sanitizedData = { ...data };

      if (
        typeof sanitizedData.purchaseDate ===
        'string'
      ) {
        const date = new Date(
          sanitizedData.purchaseDate as string,
        );

        sanitizedData.purchaseDate =
          date.toISOString();
      }

      return apiClient.post(
        '/investments',
        sanitizedData,
        {
          skipGlobalErrorNotify: true,
        },
      );
    },

    onSuccess: async () => {
      await invalidateInvestmentQueries();

      notify.success(
        t('alerts.createSuccess'),
      );
    },

    onError: (error: ApiError) => {
      notify.error(
        error.response?.data?.message ||
          t('alerts.createError'),
      );
    },
  });

  const updatePrice = useMutation({
    mutationFn: ({
      id,
      price,
    }: {
      id: string;
      price: number;
    }) =>
      apiClient.patch(
        `/investments/${id}/price`,
        {
          newPrice: price,
        },
        {
          skipGlobalErrorNotify: true,
        },
      ),

    onSuccess: async () => {
      await invalidateInvestmentQueries();

      notify.success(
        t('alerts.updatePriceSuccess'),
      );
    },

    onError: (error: ApiError) => {
      notify.error(
        error.response?.data?.message ||
          t('alerts.updatePriceError'),
      );
    },
  });

  const deleteInvestment = useMutation({
    mutationFn: (id: string) =>
      apiClient.delete(
        `/investments/${id}`,
        {
          skipGlobalErrorNotify: true,
        },
      ),

    onSuccess: async () => {
      await invalidateInvestmentQueries();

      notify.success(
        t('alerts.deleteSuccess'),
      );
    },

    onError: (error: ApiError) => {
      notify.error(
        error.response?.data?.message ||
          t('alerts.deleteError'),
      );
    },
  });

  return {
    createInvestment,
    updatePrice,
    deleteInvestment,
    isCreating: createInvestment.isPending,
    isUpdating: updatePrice.isPending,
    isDeleting: deleteInvestment.isPending,
  };
};