import { useMutation, useQueryClient } from "@tanstack/react-query"
import { useTranslation } from "react-i18next"
import { apiClient } from "@/shared/api/axiosClient"
import { notify } from "@/shared/utils/notifications/notify"
import type { ApiError } from "@/shared/types/api"
import type { TransactionResponse, RegisterTransactionRequest } from "@/features/transactions/index"

export const useTransactionMutations = () => {
  const queryClient = useQueryClient()
  const { t } = useTranslation()

  const invalidateAll = async () => {
    await Promise.all([
      queryClient.invalidateQueries({ queryKey: ['transactions'] }),
      queryClient.invalidateQueries({ queryKey: ['transactionMetrics'] }),
      queryClient.invalidateQueries({ queryKey: ['dashboardMetrics'] }),
      queryClient.invalidateQueries({ queryKey: ['historyChart'] }),
      queryClient.invalidateQueries({ queryKey: ['categoryStats'] }),
      queryClient.invalidateQueries({ queryKey: ['budgets'] }),
      queryClient.invalidateQueries({ queryKey: ['recentTransactions'] }),
      queryClient.invalidateQueries({ queryKey: ['categoryExpenses'] }),
      queryClient.invalidateQueries({ queryKey: ['plannedTransactions'] }),
    ])
  }

  const checkBudgetLimit = (response: TransactionResponse) => {
    if (response.limitExceeded) {
      notify.warning(
        t('transactions.alerts.budgetWarning') || '¡Has superado el 70% de tu presupuesto mensual!',
        {
          description: t('transactions.alerts.budgetWarningDesc') || 'Revisa tus gastos para evitar superar el límite.',
          duration: 6000,
        }
      )
    }
  }

  const createTransaction = useMutation({
    mutationFn: (data: RegisterTransactionRequest) =>
      apiClient.post<TransactionResponse>('transactions', data),
    onSuccess: async (response) => {
      await invalidateAll()
      notify.success(t('transactions.alerts.createSuccess') || 'Transacción creada correctamente')
      checkBudgetLimit(response.data)
    },
    onError: (err: ApiError) => {
      notify.error(err.response?.data?.message || t('transactions.alerts.createError') || 'Error al crear la transacción')
    },
  })

  const updateTransaction = useMutation({
    mutationFn: ({ id, data }: { id: string; data: Partial<RegisterTransactionRequest> }) =>
      apiClient.put<TransactionResponse>(`transactions/${id}`, data),
    onSuccess: async (response) => {
      await invalidateAll()
      notify.success(t('transactions.alerts.updateSuccess') || 'Transacción actualizada correctamente')
      checkBudgetLimit(response.data)
    },
    onError: (err: ApiError) => {
      notify.error(err.response?.data?.message || t('transactions.alerts.updateError') || 'Error al actualizar la transacción')
    },
  })

  const deleteTransaction = useMutation({
    mutationFn: (id: string) => apiClient.delete(`transactions/${id}`),
    onSuccess: async () => {
      await invalidateAll()
      notify.success(t('transactions.alerts.deleteSuccess') || 'Transacción eliminada correctamente')
    },
    onError: (err: ApiError) => {
      notify.error(err.response?.data?.message || t('transactions.alerts.deleteError') || 'Error al eliminar la transacción')
    },
  })

  const createRecurring = useMutation({
    mutationFn: (data: Record<string, unknown>) => apiClient.post('planned-transactions', data),
    onSuccess: async () => {
      await invalidateAll()
      notify.success(t('transactions.alerts.createRecurringSuccess') || 'Transacción recurrente programada correctamente')
    },
    onError: (err: ApiError) => {
      notify.error(err.response?.data?.message || t('transactions.alerts.createRecurringError') || 'Error al programar la transacción recurrente')
    },
  })

  const updateRecurring = useMutation({
    mutationFn: ({ id, data }: { id: string; data: Record<string, unknown> }) =>
      apiClient.put(`planned-transactions/${id}`, data),
    onSuccess: async () => {
      await invalidateAll()
      notify.success(t('transactions.alerts.updateRecurringSuccess') || 'Transacción recurrente actualizada correctamente')
    },
    onError: (err: ApiError) => {
      notify.error(err.response?.data?.message || t('transactions.alerts.updateRecurringError') || 'Error al actualizar la transacción recurrente')
    },
  })

  const deleteRecurring = useMutation({
    mutationFn: (id: string) => apiClient.delete(`planned-transactions/${id}`),
    onSuccess: async () => {
      await invalidateAll()
      notify.success(t('transactions.alerts.deleteRecurringSuccess') || 'Transacción recurrente eliminada correctamente')
    },
    onError: (err: ApiError) => {
      notify.error(err.response?.data?.message || t('transactions.alerts.deleteRecurringError') || 'Error al eliminar la transacción recurrente')
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