import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { apiClient } from '@/shared/api/axiosClient';
import { notify } from '@/shared/utils/notifications/notify';
import type { ApiError } from '@/shared/types/api';
import type { RegisterCategoryRequest, UpdateCategoryRequest } from '../types';

export const useCategoryMutations = () => {
  const { t } = useTranslation();
  const queryClient = useQueryClient();

  const invalidateCategoryQueries = async () => {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['categories'] }),
      queryClient.invalidateQueries({ queryKey: ['budgets'] }),
      queryClient.invalidateQueries({ queryKey: ['categoryExpenses'] }),
      queryClient.invalidateQueries({ queryKey: ['dashboardMetrics'] }),
      queryClient.invalidateQueries({ queryKey: ['transactionMetrics'] }),
      queryClient.invalidateQueries({ queryKey: ['categoryStats'] }),
      queryClient.invalidateQueries({ queryKey: ['historyChart'] }),
      queryClient.invalidateQueries({ queryKey: ['recentTransactions'] }),
    ]);
  };

  const getErrorMessage = (error: unknown, defaultKey: "categories.createError" | "categories.updateError" | "categories.deleteError"): string => {
    const apiError = error as ApiError;
    const backendMessage = apiError?.response?.data?.message;
    if (backendMessage && typeof backendMessage === 'string') {
      return backendMessage;
    }
    return t(defaultKey);
  };

  const createCategory = useMutation({
    mutationFn: async (data: RegisterCategoryRequest) => {
      const response = await apiClient.post('categories', {
        name: data.name.trim(),
        color: data.color,
        budgetLimit: data.budgetLimit ?? null,
      });
      return response.data;
    },
    onSuccess: async () => {
      await invalidateCategoryQueries();
      notify.success(t('categories.createSuccess'));
    },
    onError: (error: unknown) => {
      const message = getErrorMessage(error, 'categories.createError');
      console.error('[createCategory] Error:', message);
      notify.error(message);
    },
  });

  const updateCategory = useMutation({
    mutationFn: async ({ id, data }: { id: string; data: UpdateCategoryRequest }) => {
      const response = await apiClient.put(`categories/${id}`, {
        name: data.name.trim(),
        color: data.color,
        budgetLimit: data.budgetLimit ?? null,
      });
      return response.data;
    },
    onSuccess: async () => {
      await invalidateCategoryQueries();
      notify.success(t('categories.updateSuccess'));
    },
    onError: (error: unknown) => {
      const message = getErrorMessage(error, 'categories.updateError');
      console.error('[updateCategory] Error:', message);
      notify.error(message);
    },
  });

  const deleteCategory = useMutation({
    mutationFn: async (id: string) => {
      await apiClient.delete(`categories/${id}`);
      return id;
    },
    onSuccess: async () => {
      await invalidateCategoryQueries();
      notify.success(t('categories.deleteSuccess'));
    },
    onError: (error: unknown) => {
      const message = getErrorMessage(error, 'categories.deleteError');
      console.error('[deleteCategory] Error:', message);
      notify.error(message);
    }
  });

  return {
    createCategory,
    updateCategory,
    deleteCategory,
    isCreating: createCategory.isPending,
    isUpdating: updateCategory.isPending,
    isDeleting: deleteCategory.isPending,
    invalidateQueries: invalidateCategoryQueries,
  };
};