import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { useTranslation } from "react-i18next";
import { toast } from "sonner";
import { savingsGoalsService } from "../services/savingsGoals.service";
import type { CreateSavingsGoalDTO, UpdateSavingsGoalDTO } from "../types";

export const useSavingsGoals = () => {
  return useQuery({
    queryKey: ["savings-goals"],
    queryFn: savingsGoalsService.getAll,
  });
};

export const useCreateSavingsGoal = () => {
  const queryClient = useQueryClient();
  const { t } = useTranslation();

  return useMutation({
    mutationFn: (dto: CreateSavingsGoalDTO) => savingsGoalsService.create(dto),
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
      savingsGoalsService.update(id, dto),
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
