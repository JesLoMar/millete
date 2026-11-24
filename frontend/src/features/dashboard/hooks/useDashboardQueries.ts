import { useQuery } from '@tanstack/react-query'

import type { PeriodFilter } from '@/shared/components/Header'
import { apiClient } from '@/shared/api/axiosClient'

import { BUDGET_COLORS, CHART_COLORS, RECENT_TRANSACTION_LIMIT } from '../constants'
import type {
  BudgetItem,
  BudgetsResponse,
  CategoriesResponse,
  CategoryData,
  CategoryItemResponse,
  DashboardMetrics,
  HistoryPoint,
  HistoryResponse,
  TransactionItem,
  TransactionsResponse,
} from '../types'

function mapCategoriesToDonut(
  categories: CategoryItemResponse[]
): CategoryData[] {
  const hasData = categories.some(
    (category) =>
      category.percentage != null &&
      category.percentage > 0
  )

  if (!hasData) {
    return []
  }

  return categories.map((category, i) => ({
    category: category.name,
    value: category.percentage,
    color:
      CHART_COLORS[i % CHART_COLORS.length],
  }))
}

function mapBudgets(
  response: BudgetsResponse
): BudgetItem[] {
  return response.budgets.map((budget) => {
    const colorIndex =
      Math.abs(hashCode(budget.category)) %
      BUDGET_COLORS.length

    return {
      ...budget,
      color: BUDGET_COLORS[colorIndex],
    }
  })
}

function hashCode(str: string): number {
  let hash = 0

  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i)
    hash = (hash << 5) - hash + char
    hash = hash & hash
  }

  return Math.abs(hash)
}

export function useDashboardQueries(
  period: PeriodFilter
) {
  const {
    data: metricsData,
    isLoading: metricsIsLoading,
  } = useQuery<DashboardMetrics>({
    queryKey: ['dashboardMetrics', period],
    queryFn: async () => {
      const response = await apiClient.get(
        `dashboard/metrics?period=${period}`
      )

      return response.data
    },
    retry: 1,
    staleTime: 30_000,
  })

  const {
    data: historyData,
    isLoading: historyIsLoading,
  } = useQuery<HistoryPoint[]>({
    queryKey: ['historyChart', period],
    queryFn: async () => {
      const response =
        await apiClient.get<HistoryResponse>(
          `dashboard/history?period=${period}`
        )

      return response.data.points ?? []
    },
    retry: 1,
    staleTime: 30_000,
  })

  const {
    data: categoriesData,
    isLoading: categoriesIsLoading,
  } = useQuery({
    queryKey: ['categoryStats', period],
    queryFn: async () => {
      const response =
        await apiClient.get<CategoriesResponse>(
          `dashboard/categories?period=${period}`
        )

      return {
        expenses: mapCategoriesToDonut(response.data.categories),
        income: mapCategoriesToDonut(response.data.incomeCategories ?? []),
      }
    },
    retry: 1,
    staleTime: 30_000,
  })

  const {
    data: budgetsData,
    isLoading: budgetsIsLoading,
  } = useQuery<BudgetItem[]>({
    queryKey: ['budgets', period],
    queryFn: async () => {
      const response =
        await apiClient.get<BudgetsResponse>(
          `dashboard/budgets?period=${period}`
        )

      return mapBudgets(response.data)
    },
    retry: 1,
    staleTime: 30_000,
  })

  const {
    data: recentTransactionsData,
    isLoading: recentTransactionsIsLoading,
  } = useQuery<TransactionItem[]>({
    queryKey: ['recentTransactions'],
    queryFn: async () => {
      const response =
        await apiClient.get<TransactionsResponse>(
          `dashboard/recent-transactions?limit=${RECENT_TRANSACTION_LIMIT}`
        )

      return response.data.transactions
    },
    retry: 1,
    staleTime: 30_000,
  })

  return {
    metrics: {
      data: metricsData,
      isLoading: metricsIsLoading,
    },
    history: {
      data: historyData,
      isLoading: historyIsLoading,
    },
    categories: {
      data: categoriesData,
      isLoading: categoriesIsLoading,
    },
    budgets: {
      data: budgetsData,
      isLoading: budgetsIsLoading,
    },
    recentTransactions: {
      data: recentTransactionsData,
      isLoading: recentTransactionsIsLoading,
    },
  }
}
