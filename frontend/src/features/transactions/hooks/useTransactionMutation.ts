import {
  useMutation,
  useQueryClient,
  type QueryClient,
} from '@tanstack/react-query';
import axios from 'axios';
import { useTranslation } from 'react-i18next';

import type {
  RegisterTransactionRequest,
  TransactionResponse,
} from '@/features/transactions/index';
import { apiClient } from '@/shared/api/axiosClient';
import { notify } from '@/shared/utils/notifications/notify';

const FINANCIAL_DATA_QUERY_KEYS = [
  'transactions',
  'transactionMetrics',
  'dashboardMetrics',
  'historyChart',
  'categoryStats',
  'budgets',
  'recentTransactions',
  'categoryExpenses',
  'plannedTransactions',
] as const;

type RecurringTransactionRequest = {
  categoryId: string | null;
  amount: number;
  type: 'INCOME' | 'EXPENSE';
  description: string;
  frequencyType: string;
  frequencyInterval: number;
  startDate: string;
  endDate?: string | null;
};

function invalidateFinancialData(queryClient: QueryClient) {
  return Promise.all(
    FINANCIAL_DATA_QUERY_KEYS.map((queryKey) =>
      queryClient.invalidateQueries({
        queryKey: [queryKey],
      }),
    ),
  );
}

function getErrorMessage(
  error: unknown,
  fallback: string,
): string {
  if (axios.isAxiosError(error)) {
    return (
      error.response?.data?.message ??
      error.response?.data?.error ??
      fallback
    );
  }

  if (error instanceof Error && error.message) {
    return error.message;
  }

  return fallback;
}

export const useTransactionMutations = () => {
  const queryClient = useQueryClient();
  const { t } = useTranslation('transactions');

  const createTransaction = useMutation({
    mutationFn: (data: RegisterTransactionRequest) =>
      apiClient.post<TransactionResponse>(
        'transactions',
        data,
      ),
    onSuccess: async () => {
      await invalidateFinancialData(queryClient);
      notify.success(t('alerts.createSuccess'));
    },
    onError: (error: unknown) => {
      notify.error(
        getErrorMessage(error, t('alerts.createError')),
      );
    },
  });

  const updateTransaction = useMutation({
    mutationFn: ({
      id,
      data,
    }: {
      id: string;
      data: Partial<RegisterTransactionRequest>;
    }) =>
      apiClient.put<TransactionResponse>(
        `transactions/${id}`,
        data,
      ),
    onSuccess: async () => {
      await invalidateFinancialData(queryClient);
      notify.success(t('alerts.updateSuccess'));
    },
    onError: (error: unknown) => {
      notify.error(
        getErrorMessage(error, t('alerts.updateError')),
      );
    },
  });

  const deleteTransaction = useMutation({
    mutationFn: (id: string) =>
      apiClient.delete(`transactions/${id}`),
    onSuccess: async () => {
      await invalidateFinancialData(queryClient);
      notify.success(t('alerts.deleteSuccess'));
    },
    onError: (error: unknown) => {
      notify.error(
        getErrorMessage(error, t('alerts.deleteError')),
      );
    },
  });

  const createRecurring = useMutation({
    mutationFn: (data: RecurringTransactionRequest) =>
      apiClient.post('planned-transactions', data),
    onSuccess: async () => {
      await invalidateFinancialData(queryClient);
      notify.success(t('alerts.createRecurringSuccess'));
    },
    onError: (error: unknown) => {
      notify.error(
        getErrorMessage(
          error,
          t('alerts.createRecurringError'),
        ),
      );
    },
  });

  const updateRecurring = useMutation({
    mutationFn: ({
      id,
      data,
    }: {
      id: string;
      data: RecurringTransactionRequest;
    }) =>
      apiClient.put(
        `planned-transactions/${id}`,
        data,
      ),
    onSuccess: async () => {
      await invalidateFinancialData(queryClient);
      notify.success(t('alerts.updateRecurringSuccess'));
    },
    onError: (error: unknown) => {
      notify.error(
        getErrorMessage(
          error,
          t('alerts.updateRecurringError'),
        ),
      );
    },
  });

  const deleteRecurring = useMutation({
    mutationFn: (id: string) =>
      apiClient.delete(`planned-transactions/${id}`),
    onSuccess: async () => {
      await invalidateFinancialData(queryClient);
      notify.success(t('alerts.deleteRecurringSuccess'));
    },
    onError: (error: unknown) => {
      notify.error(
        getErrorMessage(
          error,
          t('alerts.deleteRecurringError'),
        ),
      );
    },
  });

  return {
    createTransaction,
    updateTransaction,
    deleteTransaction,
    createRecurring,
    updateRecurring,
    deleteRecurring,

    isCreating:
      createTransaction.isPending ||
      createRecurring.isPending,

    isUpdating:
      updateTransaction.isPending ||
      updateRecurring.isPending,

    isDeleting:
      deleteTransaction.isPending ||
      deleteRecurring.isPending,
  };
};