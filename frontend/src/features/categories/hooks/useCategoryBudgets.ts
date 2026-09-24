import { useQuery } from '@tanstack/react-query';

import { apiClient } from '@/shared/api/axiosClient';
import type { PeriodFilter } from '@/shared/components/PeriodSelector';

interface CategoryBudget {
  readonly categoryId: string;
  readonly category: string;
  readonly spent: number;
  readonly limit: number;
  readonly percentage: number;
}

interface CategoryBudgetsResponse {
  readonly period: string;
  readonly budgets: CategoryBudget[];
}

export function useCategoryBudgets(
  period: PeriodFilter,
) {
  return useQuery<CategoryBudgetsResponse>({
    queryKey: ['categoryBudgets', period],
    queryFn: async () => {
      const response =
        await apiClient.get<CategoryBudgetsResponse>(
          `/dashboard/budgets?period=${period}`,
        );

      return response.data;
    },
    staleTime: 60_000,
  });
}