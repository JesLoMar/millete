import { useCallback } from 'react';

import { apiClient } from '@/shared/api/axiosClient';
import type { PeriodFilter } from '@/shared/components/Header';
import {
  useServerPagination,
  type PaginatedResponse,
} from '@/shared/hooks/useServerPagination';

import type { Filter } from '../constants';
import type { Transaction } from '../components/types';

const SERVER_SIZE = 50;
const DISPLAY_SIZE = 10;

interface TransactionApiItem {
  id: string;
  categoryId: string;
  categoryName: string | null;
  categoryColor: string | null;
  amount: number;
  date: string;
  type: 'INCOME' | 'EXPENSE';
  description: string;
  active: boolean;
}

interface UseTransactionsOptions {
  search?: string;
  type?: Filter;
  period?: PeriodFilter;
  enabled?: boolean;
}

export function useTransactions(
  options: UseTransactionsOptions = {},
) {
  const {
    search = '',
    type = 'all',
    period = 'month',
    enabled = true,
  } = options;

  const normalizedSearch = search.trim();

  const fetchPage = useCallback(
    async (
      page: number,
    ): Promise<PaginatedResponse<Transaction>> => {
      const params = new URLSearchParams({
        page: String(page),
        size: String(SERVER_SIZE),
        period,
      });

      if (normalizedSearch) {
        params.set('search', normalizedSearch);
      }

      if (type !== 'all') {
        params.set(
          'type',
          type === 'income' ? 'INCOME' : 'EXPENSE',
        );
      }

      const response = await apiClient.get<
        PaginatedResponse<TransactionApiItem>
      >(`/transactions?${params.toString()}`);

      const data = response.data;

      return {
        ...data,
        content: data.content.map((transaction) => ({
          id: transaction.id,
          categoryId: transaction.categoryId,
          category: transaction.categoryName ?? '',
          categoryColor: transaction.categoryColor,
          amount: transaction.amount,
          date: transaction.date,
          type: transaction.type,
          description: transaction.description,
          active: transaction.active,
        })),
      };
    },
    [normalizedSearch, period, type],
  );

  return {
    ...useServerPagination<Transaction>({
      queryKey: [
        'transactions',
        normalizedSearch,
        type,
        period,
      ],
      fetchPage,
      serverSize: SERVER_SIZE,
      displaySize: DISPLAY_SIZE,
      enabled,
    }),
    serverSize: SERVER_SIZE,
    displaySize: DISPLAY_SIZE,
  };
}