import { useCallback } from 'react';

import { useTranslation } from 'react-i18next';

import {
  useMutation,
  useQueryClient,
  type QueryClient,
} from '@tanstack/react-query';

import { toast } from 'sonner';

import { useServerPagination } from '@/shared/hooks/useServerPagination';

import { savingsGoalsService } from '../services/savingsGoals.service';

import { normalizeHttpLink } from '../utils/links';

import type {
  CreateSavingsGoalDTO,
  SavingsGoal,
  UpdateSavingsGoalDTO,
} from '../types';

const SAVINGS_GOALS_QUERY_KEY = 'savings-goals';

const DASHBOARD_METRICS_QUERY_KEY = [
  'dashboardMetrics',
] as const;

const invalidateSavingsGoalQueries = (
  queryClient: QueryClient,
) =>
  Promise.all([
    queryClient.invalidateQueries({
      queryKey: [SAVINGS_GOALS_QUERY_KEY],
    }),
    queryClient.invalidateQueries({
      queryKey: DASHBOARD_METRICS_QUERY_KEY,
    }),
  ]);

const SERVER_SIZE = 45;
const DISPLAY_SIZE = 9;

interface UseSavingsGoalsOptions {
  search?: string;
  enabled?: boolean;
}

export function useSavingsGoals(
  options: UseSavingsGoalsOptions = {},
) {
  const {
    search = '',
    enabled = true,
  } = options;

  const normalizedSearch = search.trim();

  const fetchPage = useCallback(
    (page: number, signal?: AbortSignal) =>
      savingsGoalsService.getPage(
        {
          page,
          size: SERVER_SIZE,
          search: normalizedSearch,
        },
        signal,
      ),
    [normalizedSearch],
  );

  const pagination = useServerPagination<SavingsGoal>({
    queryKey: [
      SAVINGS_GOALS_QUERY_KEY,
      normalizedSearch,
    ],
    fetchPage,
    serverSize: SERVER_SIZE,
    displaySize: DISPLAY_SIZE,
    enabled,
  });

  return {
    ...pagination,
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
      toast.success(t('alerts.createSuccess'));

      return invalidateSavingsGoalQueries(queryClient);
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
      toast.success(t('alerts.updateSuccess'));

      return invalidateSavingsGoalQueries(queryClient);
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
      toast.success(t('alerts.deleteSuccess'));

      return invalidateSavingsGoalQueries(queryClient);
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
    }) => savingsGoalsService.addContribution(id, amount),

    onSuccess: () => {
      toast.success(t('alerts.contributionSuccess'));

      return invalidateSavingsGoalQueries(queryClient);
    },

    onError: () => {
      toast.error(t('alerts.contributionError'));
    },
  });
};

export const useWithdrawFromGoal = () => {
  const queryClient = useQueryClient();
  const { t } = useTranslation('savingsGoals');

  return useMutation({
    mutationFn: ({
      id,
      amount,
    }: {
      id: string;
      amount: number;
    }) => savingsGoalsService.withdraw(id, amount),

    onSuccess: () => {
      toast.success(t('alerts.withdrawSuccess'));

      return invalidateSavingsGoalQueries(queryClient);
    },

    onError: () => {
      toast.error(t('alerts.withdrawError'));
    },
  });
};