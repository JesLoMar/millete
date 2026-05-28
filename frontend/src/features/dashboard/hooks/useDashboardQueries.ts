import { useQuery } from "@tanstack/react-query"
import { apiClient } from "@/shared/api/axiosClient"
import { CHART_COLORS, BUDGET_COLORS } from "../constants"
import type { PeriodFilter } from "@/shared/components/Header"
import type {
  DashboardMetrics,
  HistoryResponse,
  CategoriesResponse,
  BudgetsResponse,
  TransactionsResponse,
  ChartDataPoint,
  CategoryData,
  BudgetItem,
  TransactionItem,
} from "../types"

function mapHistoryToChart(response: HistoryResponse): ChartDataPoint[] {
  const hasData = response.data.some((val) => val != null && val !== 0);
  if (!response.labels.length || !hasData) {
    return [];
  }
  return response.labels.map((label, i) => ({
    label,
    amount: response.data[i] || 0,
  }));
}

function mapCategoriesToDonut(response: CategoriesResponse): CategoryData[] {
  const hasData = response.categories.some(
    (cat) => cat.percentage != null && cat.percentage > 0
  );
  if (!response.categories.length || !hasData) {
    return [];
  }
  return response.categories.map((cat, i) => ({
    category: cat.name,
    value: cat.percentage,
    color: CHART_COLORS[i % CHART_COLORS.length],
  }));
}

function mapBudgets(response: BudgetsResponse): BudgetItem[] {
  return response.budgets.map((b) => {
    const colorIndex = Math.abs(hashCode(b.category)) % BUDGET_COLORS.length
    return {
      ...b,
      color: BUDGET_COLORS[colorIndex],
    }
  })
}

function hashCode(str: string): number {
  let hash = 0
  for (let i = 0; i < str.length; i++) {
    const char = str.charCodeAt(i)
    hash = ((hash << 5) - hash) + char
    hash = hash & hash
  }
  return Math.abs(hash)
}

export function useDashboardQueries(period: PeriodFilter) {
  const metrics = useQuery<DashboardMetrics>({
    queryKey: ['dashboardMetrics', period],
    queryFn: async () => {
      const response = await apiClient.get(`/dashboard/metrics?period=${period}`)
      return response.data
    },
    retry: 1,
    staleTime: 30_000,
  })

  const history = useQuery<ChartDataPoint[]>({
    queryKey: ['historyChart', period],
    queryFn: async () => {
      const response = await apiClient.get<HistoryResponse>(`/dashboard/history?period=${period}`)
      return mapHistoryToChart(response.data)
    },
    retry: 1,
    staleTime: 30_000,
  })

  const categories = useQuery<CategoryData[]>({
    queryKey: ['categoryStats', period],
    queryFn: async () => {
      const response = await apiClient.get<CategoriesResponse>(`/dashboard/categories?period=${period}`)
      return mapCategoriesToDonut(response.data)
    },
    retry: 1,
    staleTime: 30_000,
  })

  const budgets = useQuery<BudgetItem[]>({
    queryKey: ['budgets', period],
    queryFn: async () => {
      const response = await apiClient.get<BudgetsResponse>(`/dashboard/budgets?period=${period}`)
      return mapBudgets(response.data)
    },
    retry: 1,
    staleTime: 30_000,
  })

  const recentTransactions = useQuery<TransactionItem[]>({
    queryKey: ['recentTransactions'],
    queryFn: async () => {
      const response = await apiClient.get<TransactionsResponse>('/dashboard/recent-transactions?limit=5')
      return response.data.transactions
    },
    retry: 1,
    staleTime: 30_000,
  })

  return {
    metrics,
    history,
    categories,
    budgets,
    recentTransactions,
  }
}