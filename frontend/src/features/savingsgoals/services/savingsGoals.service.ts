import { apiClient } from '@/shared/api/axiosClient';
import type { PaginatedResponse } from '@/shared/hooks/useServerPagination';

import type {
  CreateSavingsGoalDTO,
  SavingsGoal,
  UpdateSavingsGoalDTO,
} from '../types';

interface GetSavingsGoalsPageParams {
  page: number;
  size: number;
  search?: string;
}

export const savingsGoalsService = {
  getAll: async (): Promise<SavingsGoal[]> => {
    const { data } = await apiClient.get<SavingsGoal[]>(
      '/savings-goals',
    );

    return data;
  },

  getPage: async (
    { page, size, search }: GetSavingsGoalsPageParams,
    signal?: AbortSignal,
  ): Promise<PaginatedResponse<SavingsGoal>> => {
    const { data } = await apiClient.get<
      PaginatedResponse<SavingsGoal>
    >('/savings-goals', {
      params: {
        page,
        size,
        ...(search ? { search } : {}),
      },
      signal,
      skipGlobalErrorNotify: true,
    });

    return data;
  },

  getById: async (id: string): Promise<SavingsGoal> => {
    const { data } = await apiClient.get<SavingsGoal>(
      `/savings-goals/${id}`,
    );

    return data;
  },

  create: async (
    dto: CreateSavingsGoalDTO,
  ): Promise<SavingsGoal> => {
    const { data } = await apiClient.post<SavingsGoal>(
      '/savings-goals',
      dto,
      {
        skipGlobalErrorNotify: true,
      },
    );

    return data;
  },

  update: async (
    id: string,
    dto: UpdateSavingsGoalDTO,
  ): Promise<SavingsGoal> => {
    const { data } = await apiClient.patch<SavingsGoal>(
      `/savings-goals/${id}`,
      dto,
      {
        skipGlobalErrorNotify: true,
      },
    );

    return data;
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/savings-goals/${id}`, {
      skipGlobalErrorNotify: true,
    });
  },

  addContribution: async (
    id: string,
    amount: number,
  ): Promise<SavingsGoal> => {
    const { data } = await apiClient.patch<SavingsGoal>(
      `/savings-goals/${id}/contribute`,
      { amount },
      {
        skipGlobalErrorNotify: true,
      },
    );

    return data;
  },

  withdraw: async (
    id: string,
    amount: number,
  ): Promise<SavingsGoal> => {
    const { data } = await apiClient.patch<SavingsGoal>(
      `/savings-goals/${id}/withdraw`,
      { amount },
      {
        skipGlobalErrorNotify: true,
      },
    );

    return data;
  },
};