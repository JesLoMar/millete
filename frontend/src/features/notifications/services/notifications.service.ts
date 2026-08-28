import { apiClient } from '@/shared/api/axiosClient';
import type { PaginatedResponse } from '@/shared/hooks/useServerPagination';
import type { Notification, NotificationCountResponse } from '../types';

const BASE = '/notifications';

export const notificationsService = {
  getAll: async (limit?: number): Promise<Notification[]> => {
    const url = limit !== undefined ? `${BASE}?limit=${limit}` : BASE;
    const { data } = await apiClient.get<Notification[]>(url);
    return data;
  },
  getPaginated: async (page: number, size: number): Promise<PaginatedResponse<Notification>> => {
    const { data } = await apiClient.get<PaginatedResponse<Notification>>(
      `${BASE}/paginated?page=${page}&size=${size}`
    );
    return data;
  },
  getUnreadCount: async (): Promise<number> => {
    const { data } = await apiClient.get<NotificationCountResponse>(`${BASE}/unread-count`);
    return data.count;
  },
  markAsRead: async (id: string): Promise<void> => {
    await apiClient.post(`${BASE}/${id}/read`);
  },
  // Marca todas como leídas confirmándolo con el backend.
  // Si el backend añade un endpoint bulk (p. ej. POST /notifications/read-all),
  // basta con reemplazar la implementación aquí.
  markAllAsRead: async (): Promise<void> => {
    const notifications = await notificationsService.getAll();
    const unread = notifications.filter((n) => !n.read);
    await Promise.all(unread.map((n) => notificationsService.markAsRead(n.id)));
  },
  delete: async (id: string): Promise<void> => {
    await apiClient.delete(`${BASE}/${id}`);
  },
};
