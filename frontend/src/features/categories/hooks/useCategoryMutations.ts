import {
  useMutation,
  useQueryClient,
} from '@tanstack/react-query';
import axios from 'axios';
import { useTranslation } from 'react-i18next';

import { apiClient } from '@/shared/api/axiosClient';
import { notify } from '@/shared/utils/notifications/notify';

import type {
  RegisterCategoryRequest,
  UpdateCategoryRequest,
} from '../types';

type CategoryMutationErrorKey =
  | 'categories:createError'
  | 'categories:updateError'
  | 'categories:deleteError';

const CATEGORIES_LIST_QUERY_KEY = ['categories'] as const;

const CATEGORY_DEPENDENT_QUERY_KEYS = [
  ['budgets'],
  ['categoryBudgets'],
  ['categoryExpenses'],
  ['dashboardMetrics'],
  ['transactionMetrics'],
  ['categoryStats'],
  ['historyChart'],
  ['recentTransactions'],
] as const;

export const useCategoryMutations = () => {
  const { t } = useTranslation('categories');

  const queryClient = useQueryClient();

  const invalidateCategoryQueries = async () => {
    await Promise.all([
      ...CATEGORY_DEPENDENT_QUERY_KEYS.map((queryKey) =>
        queryClient.invalidateQueries({ queryKey }),
      ),
      queryClient.invalidateQueries({
        queryKey: CATEGORIES_LIST_QUERY_KEY,
      }),
    ]);
  };

  const getErrorMessage = (
    error: unknown,
    defaultKey: CategoryMutationErrorKey,
  ): string => {
    if (axios.isAxiosError(error)) {
      const responseData = error.response?.data;

      if (
        typeof responseData?.message === 'string' &&
        responseData.message.trim()
      ) {
        return responseData.message;
      }

      if (
        typeof responseData?.error === 'string' &&
        responseData.error.trim()
      ) {
        return responseData.error;
      }
    }

    return t(defaultKey);
  };

  const createCategory = useMutation({
    mutationFn: async (
      data: RegisterCategoryRequest,
    ) => {
      const response = await apiClient.post(
        '/categories',
        {
          name: data.name.trim(),
          description: data.description?.trim() || null,
          color: data.color,
          budgetLimit: data.budgetLimit ?? null,
        },
        {
          skipGlobalErrorNotify: true,
        },
      );

      return response.data;
    },

    onSuccess: async () => {
      await invalidateCategoryQueries();
      notify.success(
        t('categories:createSuccess'),
      );
    },

    onError: (error: unknown) => {
      const message = getErrorMessage(
        error,
        'categories:createError',
      );

      console.error(
        '[createCategory] Error:',
        message,
      );

      notify.error(message);
    },
  });

  const updateCategory = useMutation({
    mutationFn: async ({
      id,
      data,
    }: {
      id: string;
      data: UpdateCategoryRequest;
    }) => {
      const response = await apiClient.put(
        `/categories/${id}`,
        {
          name: data.name.trim(),
          description: data.description?.trim() || null,
          color: data.color,
          budgetLimit: data.budgetLimit ?? null,
        },
        {
          skipGlobalErrorNotify: true,
        },
      );

      return response.data;
    },

    onSuccess: async () => {
      await invalidateCategoryQueries();
      notify.success(
        t('categories:updateSuccess'),
      );
    },

    onError: (error: unknown) => {
      const message = getErrorMessage(
        error,
        'categories:updateError',
      );

      console.error(
        '[updateCategory] Error:',
        message,
      );

      notify.error(message);
    },
  });

  const deleteCategory = useMutation({
    mutationFn: async (id: string) => {
      await apiClient.delete(
        `/categories/${id}`,
        {
          skipGlobalErrorNotify: true,
        },
      );

      return id;
    },

    onSuccess: async () => {
      await invalidateCategoryQueries();
      notify.success(
        t('categories:deleteSuccess'),
      );
    },

    onError: (error: unknown) => {
      const message = getErrorMessage(
        error,
        'categories:deleteError',
      );

      console.error(
        '[deleteCategory] Error:',
        message,
      );

      notify.error(message);
    },
  });

  return {
    createCategory,
    updateCategory,
    deleteCategory,
    isCreating: createCategory.isPending,
    isUpdating: updateCategory.isPending,
    isDeleting: deleteCategory.isPending,
  };
};
