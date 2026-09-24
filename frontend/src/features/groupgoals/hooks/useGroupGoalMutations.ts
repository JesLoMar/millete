import { useMutation, useQueryClient } from '@tanstack/react-query';
import axios from 'axios';
import { useTranslation } from 'react-i18next';

import { apiClient } from '@/shared/api/axiosClient';
import { notify } from '@/shared/utils/notifications/notify';

import type { DistributionMode, GoalRole } from '../types';

const GROUP_GOALS_QUERY_KEY = ['group-goals'] as const;
const NOTIFICATIONS_QUERY_KEY = ['notifications'] as const;

interface CreateGoalRequest {
  name: string;
  monthlyTarget: number;
  distributionMode: DistributionMode;
}

interface UpdateGoalRequest {
  goalId: string;
  name?: string;
  monthlyTarget?: number;
  distributionMode?: DistributionMode;
}

interface UpdateMemberRequest {
  goalId: string;
  memberId: string;
  role?: GoalRole;
  salary?: number;
  customPercentage?: number;
}

interface DeleteMemberRequest {
  goalId: string;
  memberId: string;
}

interface AddContributionRequest {
  goalId: string;
  amount: number;
}

function getErrorMessage(
  error: unknown,
  fallback: string,
): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as
      | {
          message?: string;
          error?: string;
        }
      | undefined;

    return (
      data?.message ??
      data?.error ??
      fallback
    );
  }

  if (error instanceof Error && error.message) {
    return error.message;
  }

  return fallback;
}

export function useGroupGoalMutations(
  selectedGoalId: string | null,
) {
  const queryClient = useQueryClient();
  const { t } = useTranslation('groupGoals');

  const invalidateGroupGoals = () =>
    queryClient.invalidateQueries({
      queryKey: GROUP_GOALS_QUERY_KEY,
    });

  const createGoal = useMutation({
    mutationFn: (data: CreateGoalRequest) =>
      apiClient.post('/goals', data, {
        skipGlobalErrorNotify: true,
      }),

    onSuccess: async () => {
      await invalidateGroupGoals();

      notify.success(
        t('alerts.createSuccess'),
      );
    },

    onError: (error: unknown) => {
      notify.error(
        getErrorMessage(
          error,
          t('alerts.createError'),
        ),
      );
    },
  });

  const inviteMember = useMutation({
    mutationFn: (identifier: string) =>
      apiClient.post(
        `/goals/${selectedGoalId}/invitations`,
        { identifier },
        {
          skipGlobalErrorNotify: true,
        },
      ),

    onSuccess: async () => {
      await Promise.all([
        invalidateGroupGoals(),
        queryClient.invalidateQueries({
          queryKey: NOTIFICATIONS_QUERY_KEY,
        }),
      ]);

      notify.success(
        t('alerts.inviteSuccess'),
      );
    },

    onError: (error: unknown) => {
      notify.error(
        getErrorMessage(
          error,
          t('alerts.inviteError'),
        ),
      );
    },
  });

  const updateGoal = useMutation({
    mutationFn: ({
      goalId,
      name,
      monthlyTarget,
      distributionMode,
    }: UpdateGoalRequest) => {
      const payload: {
        name?: string;
        monthlyTarget?: number;
        distributionMode?: DistributionMode;
      } = {};

      if (name !== undefined) {
        payload.name = name;
      }

      if (monthlyTarget !== undefined) {
        payload.monthlyTarget = monthlyTarget;
      }

      if (distributionMode !== undefined) {
        payload.distributionMode =
          distributionMode;
      }

      return apiClient.put(
        `/goals/${goalId}`,
        payload,
        {
          skipGlobalErrorNotify: true,
        },
      );
    },

    onSuccess: async () => {
      await invalidateGroupGoals();

      notify.success(
        t('alerts.goalSuccess'),
      );
    },

    onError: (error: unknown) => {
      notify.error(
        getErrorMessage(
          error,
          t('alerts.goalError'),
        ),
      );
    },
  });

  const deleteGoal = useMutation({
    mutationFn: (goalId: string) =>
      apiClient.delete(
        `/goals/${goalId}`,
        {
          skipGlobalErrorNotify: true,
        },
      ),

    onSuccess: async () => {
      await invalidateGroupGoals();

      notify.success(
        t('alerts.deleteSuccess'),
      );
    },

    onError: (error: unknown) => {
      notify.error(
        getErrorMessage(
          error,
          t('alerts.deleteError'),
        ),
      );
    },
  });

  const updateMember = useMutation({
    mutationFn: ({
      goalId,
      memberId,
      role,
      salary,
      customPercentage,
    }: UpdateMemberRequest) => {
      const payload: {
        role?: GoalRole;
        salary?: number;
        customPercentage?: number;
      } = {};

      if (role !== undefined) {
        payload.role = role;
      }

      if (salary !== undefined) {
        payload.salary = salary;
      }

      if (customPercentage !== undefined) {
        payload.customPercentage =
          customPercentage;
      }

      return apiClient.put(
        `/goals/${goalId}/members/${memberId}`,
        payload,
        {
          skipGlobalErrorNotify: true,
        },
      );
    },

    onSuccess: async () => {
      await invalidateGroupGoals();

      notify.success(
        t('alerts.memberEditSuccess'),
      );
    },

    onError: (error: unknown) => {
      notify.error(
        getErrorMessage(
          error,
          t('alerts.memberEditError'),
        ),
      );
    },
  });

  const deleteMember = useMutation({
    mutationFn: ({
      goalId,
      memberId,
    }: DeleteMemberRequest) =>
      apiClient.delete(
        `/goals/${goalId}/members/${memberId}`,
        {
          skipGlobalErrorNotify: true,
        },
      ),

    onSuccess: async () => {
      await invalidateGroupGoals();

      notify.success(
        t('alerts.memberDeleteSuccess'),
      );
    },

    onError: (error: unknown) => {
      notify.error(
        getErrorMessage(
          error,
          t('alerts.memberDeleteError'),
        ),
      );
    },
  });

  const addContribution = useMutation({
    mutationFn: ({
      goalId,
      amount,
    }: AddContributionRequest) =>
      apiClient.post(
        `/goals/${goalId}/contributions`,
        { amount },
        {
          skipGlobalErrorNotify: true,
        },
      ),

    onSuccess: async () => {
      await invalidateGroupGoals();

      notify.success(
        t('alerts.contributionSuccess'),
      );
    },

    onError: (error: unknown) => {
      notify.error(
        getErrorMessage(
          error,
          t('alerts.contributionError'),
        ),
      );
    },
  });

  return {
    createGoal,
    inviteMember,
    updateGoal,
    deleteGoal,
    updateMember,
    deleteMember,
    addContribution,
  };
}