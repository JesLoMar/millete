import { apiClient } from "@/shared/api/axiosClient";
import type { SavingsGoal, CreateSavingsGoalDTO, UpdateSavingsGoalDTO } from "../types";

export const savingsGoalsService = {
  getAll: async (): Promise<SavingsGoal[]> => {
    const { data } = await apiClient.get<SavingsGoal[]>("/savings-goals");
    return data;
  },

  getById: async (id: string): Promise<SavingsGoal> => {
    const { data } = await apiClient.get<SavingsGoal>(`/savings-goals/${id}`);
    return data;
  },

  create: async (dto: CreateSavingsGoalDTO): Promise<SavingsGoal> => {
    const { data } = await apiClient.post<SavingsGoal>("/savings-goals", dto);
    return data;
  },

  update: async (id: string, dto: UpdateSavingsGoalDTO): Promise<SavingsGoal> => {
    const { data } = await apiClient.put<SavingsGoal>(`/savings-goals/${id}`, dto);
    return data;
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`/savings-goals/${id}`);
  },

  addContribution: async (id: string, amount: number): Promise<SavingsGoal> => {
    const { data } = await apiClient.patch(`/savings-goals/${id}/contribute`, { amount });
    return data;
  },
};