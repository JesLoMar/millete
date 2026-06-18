import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
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

  return useMutation({
    mutationFn: (dto: CreateSavingsGoalDTO) => savingsGoalsService.create(dto),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["savings-goals"] });
      toast.success("¡Meta de ahorro creada correctamente!");
    },
    onError: () => {
      toast.error("Hubo un error al crear la meta de ahorro.");
    },
  });
};

export const useUpdateSavingsGoal = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, dto }: { id: string; dto: UpdateSavingsGoalDTO }) =>
      savingsGoalsService.update(id, dto),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["savings-goals"] });
      toast.success("¡Meta de ahorro actualizada correctamente!");
    },
    onError: () => {
      toast.error("Hubo un error al actualizar la meta de ahorro.");
    },
  });
};

export const useDeleteSavingsGoal = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (id: string) => savingsGoalsService.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["savings-goals"] });
      toast.success("¡Meta de ahorro eliminada correctamente!");
    },
    onError: () => {
      toast.error("Hubo un error al eliminar la meta de ahorro.");
    },
  });
};

export const useAddContribution = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: ({ id, amount }: { id: string; amount: number }) =>
      savingsGoalsService.addContribution(id, amount),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["savings-goals"] });
      toast.success("¡Aportación añadida correctamente!");
    },
    onError: () => {
      toast.error("Hubo un error al procesar la aportación.");
    },
  });
};