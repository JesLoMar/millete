export type TransactionType = 'INCOME' | 'EXPENSE';

export interface TransactionResponse {
  id: string; // UUID
  userId: string; // UUID
  categoryId: string; // UUID
  amount: number;
  type: TransactionType;
  description: string;
  date: string; // ISO 8601
  createdAt: string; // ISO 8601
  limitExceeded?: boolean; 
}

export interface RegisterTransactionRequest {
  categoryId: string; // UUID
  amount: number;
  type: TransactionType;
  description: string;
  date: string; // ISO 8601 Date
}

export type UpdateTransactionRequest = Partial<RegisterTransactionRequest>;