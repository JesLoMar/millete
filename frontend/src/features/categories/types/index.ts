export interface Category {
  id: string
  userId: string
  name: string
  color: string
  budgetLimit: number | null
  createdAt: string
  modifiedAt: string
  active: boolean
}

export interface RegisterCategoryRequest {
  name: string
  color: string
  budgetLimit?: number | null
}

export interface UpdateCategoryRequest {
  name: string
  color: string
  budgetLimit: number | null
}

export interface CategoryExpense {
  name: string
  amount: number
  percentage: number
}

export interface CategoriesExpenseResponse {
  totalExpenses: number
  categories: CategoryExpense[]
}