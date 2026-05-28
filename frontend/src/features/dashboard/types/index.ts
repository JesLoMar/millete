// ─── Dashboard Types (Unificados) ─────────────────────────

export interface ChartDataPoint {
  label: string
  amount: number
  fill?: string
}

export interface CategoryData {
  category: string
  value: number
  color: string
}

export interface BudgetItem {
  category: string
  spent: number
  limit: number
  percentage: number
  color: string
}

export interface TransactionItem {
  id: string
  description: string
  category: string
  amount: number
  date: string
  type: "INCOME" | "EXPENSE"
  icon?: string
}

export interface DashboardMetrics {
  balance: number
  income: number
  expenses: number
  savings: number
  balanceTrend: number
  incomeTrend: number
  expensesTrend: number
  savingsTrend: number
}

export interface HistoryResponse {
  period: string
  labels: string[]
  data: number[]
}

export interface CategoryItemResponse {
  name: string
  amount: number
  percentage: number
  transactionCount: number
}

export interface CategoriesResponse {
  totalExpenses: number
  categories: CategoryItemResponse[]
}

export interface BudgetItemResponse {
  categoryId: string
  category: string
  spent: number
  limit: number
  percentage: number
}

export interface BudgetsResponse {
  period: string
  budgets: BudgetItemResponse[]
}

export interface TransactionResponse {
  id: string
  description: string
  category: string
  categoryId: string
  amount: number
  date: string
  type: "INCOME" | "EXPENSE"
}

export interface TransactionsResponse {
  transactions: TransactionResponse[]
}