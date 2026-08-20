import { useCallback } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { apiClient } from "@/shared/api/axiosClient";
import { useServerPagination, type PaginatedResponse } from "@/shared/hooks/useServerPagination";
import { savingsGoalsService } from "../services/savingsGoals.service";
import type { SavingsGoal, CreateSavingsGoalDTO, UpdateSavingsGoalDTO } from "../types";

const SERVER_SIZE = 45;
const DISPLAY_SIZE = 9;

const sanitizeLink = (link?: string): string | undefined => {
  const trimmed = link?.trim();
  if (!trimmed) return undefined;

  const withProtocol = /^https?:\/\//i.test(trimmed) ? trimmed : `https://${trimmed}`;

  try {
    const url = new URL(withProtocol);
    return url.protocol === "http:" || url.protocol === "https:" ? url.href : undefined;
  } catch {
    return undefined;
  }
};

interface UseSavingsGoalsOptions {
  search?: string;
  status?: string;
  enabled?: boolean;
}

export function useSavingsGoals(options: UseSavingsGoalsOptions = {}) {
  const { search = "", status = "", enabled = true } = options;

  const fetchPage = useCallback(
    async (page: number): Promise<PaginatedResponse<SavingsGoal>> => {
      const params = new URLSearchParams({
        page: String(page),
        size: String(SERVER_SIZE),
      });
      if (search.trim()) params.set("search", search.trim());
      if (status.trim()) params.set("status", status.trim().toUpperCase());

      const response = await apiClient.get(`/savings-goals?${params.toString()}`);
      return response.data;
    },
    [search, status]
  );

  return {
    ...useServerPagination<SavingsGoal>({
      queryKey: ["savings-goals", search, status],
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
  const { t } = useTranslation();

  return useMutation({
    mutationFn: (dto: CreateSavingsGoalDTO) =>
      savingsGoalsService.create({ ...dto, link: sanitizeLink(dto.link) }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["savings-goals"] });
      toast.success(t('savingsGoals:alerts.createSuccess'));
    },
    onError: () => {
      toast.error(t('savingsGoals:alerts.createError'));
    },
  });
};

export const useUpdateSavingsGoal = () => {
  const queryClient = useQueryClient();
  const { t } = useTranslation();

  return useMutation({
    mutationFn: ({ id, dto }: { id: string; dto: UpdateSavingsGoalDTO }) =>
      savingsGoalsService.update(id, { ...dto, link: sanitizeLink(dto.link) }),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["savings-goals"] });
      toast.success(t('savingsGoals:alerts.updateSuccess'));
    },
    onError: () => {
      toast.error(t('savingsGoals:alerts.updateError'));
    },
  });
};

export const useDeleteSavingsGoal = () => {
  const queryClient = useQueryClient();
  const { t } = useTranslation();

  return useMutation({
    mutationFn: (id: string) => savingsGoalsService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["savings-goals"] });
      toast.success(t('savingsGoals:alerts.deleteSuccess'));
    },
    onError: () => {
      toast.error(t('savingsGoals:alerts.deleteError'));
    },
  });
};

export const useAddContribution = () => {
  const queryClient = useQueryClient();
  const { t } = useTranslation();

  return useMutation({
    mutationFn: ({ id, amount }: { id: string; amount: number }) =>
      savingsGoalsService.addContribution(id, amount),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["savings-goals"] });
      toast.success(t('savingsGoals:alerts.contributionSuccess'));
    },
    onError: () => {
      toast.error(t('savingsGoals:alerts.contributionError'));
    },
  });
};