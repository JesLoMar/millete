import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { notificationsService } from '../services/notifications.service';
import type { Notification } from '../types';

const QUERY_KEY = ['notifications'];
const COUNT_QUERY_KEY = ['notifications', 'unread-count'];

export function useNotifications() {
  return useQuery<Notification[]>({
    queryKey: QUERY_KEY,
    queryFn: notificationsService.getAll,
    staleTime: 60_000,
  });
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
      queryClient.invalidateQueries({ queryKey: COUNT_QUERY_KEY });
    },
  });
}

export function useDeleteNotification() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: notificationsService.delete,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEY });
      queryClient.invalidateQueries({ queryKey: COUNT_QUERY_KEY });
    },
  });
}
