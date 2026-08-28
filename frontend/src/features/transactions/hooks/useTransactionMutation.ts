import { useMutation, useQueryClient, type QueryClient } from "@tanstack/react-query"
import { useTranslation } from "react-i18next"
import { apiClient } from "@/shared/api/axiosClient"
import { notify } from "@/shared/utils/notifications/notify"
import type { ApiError } from "@/shared/types/api"
import type { TransactionResponse, RegisterTransactionRequest } from "@/features/transactions/index"

// Fuente única de las queries que dependen de los datos financieros.
// Añadir una décima clave = editar solo este array.
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
] as const

const invalidateFinancialData = (queryClient: QueryClient) =>
  Promise.all(
    FINANCIAL_DATA_QUERY_KEYS.map((queryKey) =>
      queryClient.invalidateQueries({ queryKey: [queryKey] })
    )
  )

export const useTransactionMutations = () => {
  const queryClient = useQueryClient()
  const { t } = useTranslation()

  const createTransaction = useMutation({
    mutationFn: (data: RegisterTransactionRequest) =>
      apiClient.post<TransactionResponse>('transactions', data),
    onSuccess: async () => {
      await invalidateFinancialData(queryClient)
      notify.success(t('transactions:alerts.createSuccess'))
    },
    onError: (err: ApiError) => {
      notify.error(err.response?.data?.message || t('transactions:alerts.createError'))
    },
  })

  const updateTransaction = useMutation({
    mutationFn: ({ id, data }: { id: string; data: Partial<RegisterTransactionRequest> }) =>
      apiClient.put<TransactionResponse>(`transactions/${id}`, data),
    onSuccess: async () => {
      await invalidateFinancialData(queryClient)
      notify.success(t('transactions:alerts.updateSuccess'))
    },
    onError: (err: ApiError) => {
      notify.error(err.response?.data?.message || t('transactions:alerts.updateError'))
    },
  })

  const deleteTransaction = useMutation({
    mutationFn: (id: string) => apiClient.delete(`transactions/${id}`),
    onSuccess: async () => {
      await invalidateFinancialData(queryClient)
      notify.success(t('transactions:alerts.deleteSuccess'))
    },
    onError: (err: ApiError) => {
      notify.error(err.response?.data?.message || t('transactions:alerts.deleteError'))
    },
  })

  const createRecurring = useMutation({
    mutationFn: (data: Record<string, unknown>) => apiClient.post('planned-transactions', data),
    onSuccess: async () => {
      await invalidateFinancialData(queryClient)
      notify.success(t('transactions:alerts.createRecurringSuccess'))
    },
    onError: (err: ApiError) => {
      notify.error(err.response?.data?.message || t('transactions:alerts.createRecurringError'))
    },
  })

  const updateRecurring = useMutation({
    mutationFn: ({ id, data }: { id: string; data: Record<string, unknown> }) =>
      apiClient.put(`planned-transactions/${id}`, data),
    onSuccess: async () => {
      await invalidateFinancialData(queryClient)
      notify.success(t('transactions:alerts.updateRecurringSuccess'))
    },
    onError: (err: ApiError) => {
      notify.error(err.response?.data?.message || t('transactions:alerts.updateRecurringError'))
    },
  })

  const deleteRecurring = useMutation({
    mutationFn: (id: string) => apiClient.delete(`planned-transactions/${id}`),
    onSuccess: async () => {
      await invalidateFinancialData(queryClient)
      notify.success(t('transactions:alerts.deleteRecurringSuccess'))
    },
    onError: (err: ApiError) => {
      notify.error(err.response?.data?.message || t('transactions:alerts.deleteRecurringError'))
    },
  })

  return {
    createTransaction,
    updateTransaction,
    deleteTransaction,
    createRecurring,
    updateRecurring,
    deleteRecurring,
    isCreating: createTransaction.isPending || createRecurring.isPending,
    isUpdating: updateTransaction.isPending || updateRecurring.isPending,
    isDeleting: deleteTransaction.isPending || deleteRecurring.isPending,
  }
}
