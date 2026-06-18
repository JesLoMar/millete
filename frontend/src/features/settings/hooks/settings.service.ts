import { apiClient } from '@/shared/api/axiosClient';
import type { TelegramStatusResponse } from '../types';

export const settingsService = {
  getTelegramStatus: async (): Promise<TelegramStatusResponse> => {
    const response = await apiClient.get<TelegramStatusResponse>('/settings/telegram/status');
    return response.data;
  }
}
