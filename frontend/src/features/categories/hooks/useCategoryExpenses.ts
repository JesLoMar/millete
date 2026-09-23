import { useQuery } from '@tanstack/react-query';

import { apiClient } from '@/shared/api/axiosClient';
import type { PeriodFilter } from '@/shared/components/PeriodSelector';

import type { CategoriesExpenseResponse } from '../types';

export function useCategoryExpenses(
  period: PeriodFilter,
) {
  return useQuery<CategoriesExpenseResponse>({
    queryKey: ['categoryExpenses', period],
    queryFn: async () => {
      const response =
        await apiClient.get<CategoriesExpenseResponse>(
          `/dashboard/categories?period=${period}`,
        );

      return response.data;
    },
    staleTime: 60_000,
  });
}