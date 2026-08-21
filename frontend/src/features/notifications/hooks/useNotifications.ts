import { useCallback } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useServerPagination, type PaginatedResponse } from '@/shared/hooks/useServerPagination';
import { notificationsService } from '../services/notifications.service';
import type { Notification } from '../types';

const QUERY_KEY = ['notifications'];
const COUNT_QUERY_KEY = ['notifications', 'unread-count'];
const NOTIFICATION_SERVER_SIZE = 25;
const NOTIFICATION_DISPLAY_SIZE = 5;

export function useNotifications() {
  return useQuery<Notification[]>({
    queryKey: QUERY_KEY,
    queryFn: () => notificationsService.getAll(),
    staleTime: 60_000,
  });
}

export function useRecentNotifications(limit: number) {
  return useQuery<Notification[]>({
    queryKey: [...QUERY_KEY, 'recent', limit],
    queryFn: () => notificationsService.getAll(limit),
    staleTime: 60_000,
  });
}

export function usePaginatedNotifications() {
  const fetchPage = useCallback(
    async (page: number): Promise<PaginatedResponse<Notification>> => {
      return notificationsService.getPaginated(page, NOTIFICATION_SERVER_SIZE);
    },
    []
  );
  return {
    ...useServerPagination<Notification>({
      queryKey: QUERY_KEY,
      fetchPage,
      serverSize: NOTIFICATION_SERVER_SIZE,
      displaySize: NOTIFICATION_DISPLAY_SIZE,
    }),
    serverSize: NOTIFICATION_SERVER_SIZE,
    displaySize: NOTIFICATION_DISPLAY_SIZE,
  };
}

export function useUnreadNotificationsCount() {
  return useQuery<number>({
    queryKey: COUNT_QUERY_KEY,
    queryFn: notificationsService.getUnreadCount,
    staleTime: 60_000,
  });
}

export function useMarkNotificationAsRead() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: notificationsService.markAsRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEY });
    },
  });
}

// Marca todas como leídas con confirmación del backend.
// Optimista: el badge se oculta al instante, pero si el servidor falla
// se restaura el contador anterior (rollback) — el badge nunca miente.
export function useMarkAllNotificationsAsRead() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: notificationsService.markAllAsRead,
    onMutate: async () => {
      await queryClient.cancelQueries({ queryKey: COUNT_QUERY_KEY });
      const previousCount = queryClient.getQueryData<number>(COUNT_QUERY_KEY);
      queryClient.setQueryData(COUNT_QUERY_KEY, 0);
      return { previousCount };
    },
    onError: (_error, _variables, context) => {
      if (context?.previousCount !== undefined) {
        queryClient.setQueryData(COUNT_QUERY_KEY, context.previousCount);
      }
    },
    onSettled: () => {
      // ['notifications'] es prefijo de todas las variantes (lista, recent, paginada y unread-count)
      queryClient.invalidateQueries({ queryKey: QUERY_KEY });
    },
  });
}

export function useDeleteNotification() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: notificationsService.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEY });
    },
  });
}
