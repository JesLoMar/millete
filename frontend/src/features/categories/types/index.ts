export interface Category {
  readonly id: string;
  readonly userId: string;
  readonly name: string;
  readonly description: string | null;
  readonly color: string;
  readonly budgetLimit: number | null;
  readonly createdAt: string;
  readonly active: boolean;
}

export interface RegisterCategoryRequest {
  name: string;
  description?: string | null;
  color: string;
  budgetLimit?: number | null;
}

export interface UpdateCategoryRequest {
  name: string;
  description: string | null;
  color: string;
  budgetLimit: number | null;
}

export interface CategoryExpense {
  readonly categoryId: string | null;
  readonly name: string;
  readonly amount: number;
  readonly percentage: number;
  readonly transactionCount: number;
}

export interface CategoriesExpenseResponse {
  readonly totalExpenses: number;
  readonly categories: CategoryExpense[];
}
