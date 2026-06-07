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
  limitExceeded?: boolean;
  active?: boolean;
}

export interface RegisterTransactionRequest {
  categoryId: string; // UUID
  amount: number;
  type: TransactionType;
  description: string;
  date: string; // ISO 8601 Date
}

export type UpdateTransactionRequest = Partial<RegisterTransactionRequest>;