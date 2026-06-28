import { apiClient } from '@/shared/api/axiosClient';
import type { Notification, NotificationCountResponse } from '../types';

const BASE = '/notifications';

export const notificationsService = {
  getAll: async (): Promise<Notification[]> => {
    const { data } = await apiClient.get<Notification[]>(BASE);
    return data;
  },

  getUnreadCount: async (): Promise<number> => {
    const { data } = await apiClient.get<NotificationCountResponse>(`${BASE}/unread-count`);
    return data.count;
  },

  markAsRead: async (id: string): Promise<void> => {
    await apiClient.post(`${BASE}/${id}/read`);
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`${BASE}/${id}`);
  },
};
