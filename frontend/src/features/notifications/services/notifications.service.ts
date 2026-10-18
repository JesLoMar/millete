
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
    signal?: AbortSignal,
  ): Promise<Notification[]> => {
    const { data } = await apiClient.get<Notification[]>(
      BASE,
      {
        params:
          limit !== undefined
            ? { limit }
            : undefined,
        signal,
      },
    );

    return data;
  },

  getPaginated: async (
    page: number,
    size: number,
    signal?: AbortSignal,
  ): Promise<PaginatedResponse<Notification>> => {
    const { data } = await apiClient.get<
      PaginatedResponse<Notification>
    >(`${BASE}/paginated`, {
      params: {
        page,
        size,
      },
      signal,
    });

    return data;
  },

  getUnreadCount: async (
    signal?: AbortSignal,
  ): Promise<number> => {
    const { data } =
      await apiClient.get<NotificationCountResponse>(
        `${BASE}/unread-count`,
        { signal },
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
