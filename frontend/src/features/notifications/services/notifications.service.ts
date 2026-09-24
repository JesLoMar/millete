import { apiClient } from '@/shared/api/axiosClient';
import type { PaginatedResponse } from '@/shared/hooks/useServerPagination';

import type {
  Notification,
  NotificationCountResponse,
} from '../types';

const BASE = '/notifications';

export const notificationsService = {
  getAll: async (
    limit?: number,
  ): Promise<Notification[]> => {
    const { data } = await apiClient.get<Notification[]>(
      BASE,
      {
        params:
          limit !== undefined
            ? { limit }
            : undefined,
      },
    );

    return data;
  },

  getPaginated: async (
    page: number,
    size: number,
  ): Promise<PaginatedResponse<Notification>> => {
    const { data } = await apiClient.get<
      PaginatedResponse<Notification>
    >(`${BASE}/paginated`, {
      params: {
        page,
        size,
      },
    });

    return data;
  },

  getUnreadCount: async (): Promise<number> => {
    const { data } =
      await apiClient.get<NotificationCountResponse>(
        `${BASE}/unread-count`,
      );

    return data.count;
  },

  markAsRead: async (id: string): Promise<void> => {
    await apiClient.post<void>(`${BASE}/${id}/read`);
  },

  markAllAsRead: async (): Promise<void> => {
    const notifications =
      await notificationsService.getAll();

    const unreadNotifications = notifications.filter(
      (notification) => !notification.read,
    );

    await Promise.all(
      unreadNotifications.map((notification) =>
        notificationsService.markAsRead(notification.id),
      ),
    );
  },

  delete: async (id: string): Promise<void> => {
    await apiClient.delete<void>(`${BASE}/${id}`);
  },
};