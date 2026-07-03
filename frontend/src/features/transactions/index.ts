export type TransactionType = 'INCOME' | 'EXPENSE';

export interface TransactionResponse {
  id: string;
  userId: string;
  categoryId: string;
  categoryName?: string;
  amount: number;
  type: TransactionType;
  description: string;
  date: string;
  createdAt: string;
  active?: boolean;
}

export interface RegisterTransactionRequest {
  categoryId: string;
  amount: number;
  type: TransactionType;
  description: string;
  date: string;
}
