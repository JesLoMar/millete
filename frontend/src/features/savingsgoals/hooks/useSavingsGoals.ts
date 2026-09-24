import { useCallback } from 'react';

import { useTranslation } from 'react-i18next';

import {
  useMutation,
  useQueryClient,
} from '@tanstack/react-query';

import { toast } from 'sonner';

import { apiClient } from '@/shared/api/axiosClient';

import {
  useServerPagination,
  type PaginatedResponse,
} from '@/shared/hooks/useServerPagination';

import { savingsGoalsService } from '../services/savingsGoals.service';

import { normalizeHttpLink } from '../utils/links';

import type {
  CreateSavingsGoalDTO,
  SavingsGoal,
  UpdateSavingsGoalDTO,
} from '../types';

const SERVER_SIZE = 45;
const DISPLAY_SIZE = 9;

interface UseSavingsGoalsOptions {
  search?: string;
  status?: SavingsGoal['status'] | '';
  enabled?: boolean;
}

export function useSavingsGoals(
  options: UseSavingsGoalsOptions = {},
) {
  const {
    search = '',
    status = '',
    enabled = true,
  } = options;

  const normalizedSearch = search.trim();
  const normalizedStatus = status.trim().toUpperCase();

  const fetchPage = useCallback(
    async (
      page: number,
    ): Promise<PaginatedResponse<SavingsGoal>> => {
      const params = new URLSearchParams({
        page: String(page),
        size: String(SERVER_SIZE),
      });

      if (normalizedSearch) {
        params.set('search', normalizedSearch);
      }

      if (normalizedStatus) {
        params.set('status', normalizedStatus);
      }

      const response = await apiClient.get<
        PaginatedResponse<SavingsGoal>
      >(`/savings-goals?${params.toString()}`);

      return response.data;
    },
    [normalizedSearch, normalizedStatus],
  );

  return {
    ...useServerPagination<SavingsGoal>({
      queryKey: [
        'savings-goals',
        normalizedSearch,
        normalizedStatus,
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

export const useCreateSavingsGoal = () => {
  const queryClient = useQueryClient();
  const { t } = useTranslation('savingsGoals');

  return useMutation({
    mutationFn: (dto: CreateSavingsGoalDTO) =>
      savingsGoalsService.create({
        ...dto,
        link: normalizeHttpLink(dto.link),
      }),

    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['savings-goals'],
      });

      toast.success(t('alerts.createSuccess'));
    },

    onError: () => {
      toast.error(t('alerts.createError'));
    },
  });
};

export const useUpdateSavingsGoal = () => {
  const queryClient = useQueryClient();
  const { t } = useTranslation('savingsGoals');

  return useMutation({
    mutationFn: ({
      id,
      dto,
    }: {
      id: string;
      dto: UpdateSavingsGoalDTO;
    }) =>
      savingsGoalsService.update(id, {
        ...dto,
        link: normalizeHttpLink(dto.link),
      }),

    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['savings-goals'],
      });

      toast.success(t('alerts.updateSuccess'));
    },

    onError: () => {
      toast.error(t('alerts.updateError'));
    },
  });
};

export const useDeleteSavingsGoal = () => {
  const queryClient = useQueryClient();
  const { t } = useTranslation('savingsGoals');

  return useMutation({
    mutationFn: (id: string) =>
      savingsGoalsService.delete(id),

    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['savings-goals'],
      });

      toast.success(t('alerts.deleteSuccess'));
    },

    onError: () => {
      toast.error(t('alerts.deleteError'));
    },
  });
};

export const useAddContribution = () => {
  const queryClient = useQueryClient();
  const { t } = useTranslation('savingsGoals');

  return useMutation({
    mutationFn: ({
      id,
      amount,
    }: {
      id: string;
      amount: number;
    }) =>
      savingsGoalsService.addContribution(
        id,
        amount,
      ),

    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['savings-goals'],
      });

      toast.success(
        t('alerts.contributionSuccess'),
      );
    },

    onError: () => {
      toast.error(
        t('alerts.contributionError'),
      );
    },
  });
};