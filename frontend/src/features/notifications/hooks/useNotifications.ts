
import { useCallback } from 'react';
import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query';

import {
  useServerPagination,
  type PaginatedResponse,
} from '@/shared/hooks/useServerPagination';

import { notificationsService } from '../services/notifications.service';
import type { Notification } from '../types';

const NOTIFICATIONS_QUERY_KEY = ['notifications'] as const;

const ALL_QUERY_KEY = [
  ...NOTIFICATIONS_QUERY_KEY,
  'all',
] as const;

const PAGINATED_QUERY_KEY = [
  ...NOTIFICATIONS_QUERY_KEY,
  'paginated',
] as const;

const COUNT_QUERY_KEY = [
  ...NOTIFICATIONS_QUERY_KEY,
  'unread-count',
] as const;

const NOTIFICATION_SERVER_SIZE = 25;
const NOTIFICATION_DISPLAY_SIZE = 5;

export function useNotifications() {
  return useQuery<Notification[]>({
    queryKey: ALL_QUERY_KEY,
    queryFn: ({ signal }) =>
      notificationsService.getAll(undefined, signal),
    staleTime: 60_000,
  });
}

export function useRecentNotifications(limit: number) {
  return useQuery<Notification[]>({
    queryKey: [
      ...NOTIFICATIONS_QUERY_KEY,
      'recent',
      limit,
    ],
    queryFn: ({ signal }) =>
      notificationsService.getAll(limit, signal),
    staleTime: 60_000,
  });
}

export function usePaginatedNotifications() {
  const fetchPage = useCallback(
    (
      page: number,
      signal?: AbortSignal,
    ): Promise<PaginatedResponse<Notification>> =>
      notificationsService.getPaginated(
        page,
        NOTIFICATION_SERVER_SIZE,
        signal,
      ),
    [],
  );

  return {
    ...useServerPagination<Notification>({
      queryKey: PAGINATED_QUERY_KEY,
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
    queryFn: ({ signal }) =>
      notificationsService.getUnreadCount(signal),
    staleTime: 60_000,
  });
}

export function useMarkNotificationAsRead() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: notificationsService.markAsRead,

    onSuccess: () => {
      return queryClient.invalidateQueries({
        queryKey: NOTIFICATIONS_QUERY_KEY,
      });
    },
  });
}

export function useMarkAllNotificationsAsRead() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: notificationsService.markAllAsRead,

    onMutate: async () => {
      await queryClient.cancelQueries({
        queryKey: COUNT_QUERY_KEY,
      });

      const previousCount =
        queryClient.getQueryData<number>(COUNT_QUERY_KEY);

      queryClient.setQueryData(COUNT_QUERY_KEY, 0);

      return { previousCount };
    },

    onError: (_error, _variables, context) => {
      if (context?.previousCount !== undefined) {
        queryClient.setQueryData(
          COUNT_QUERY_KEY,
          context.previousCount,
        );
      }
    },

    onSettled: () => {
      return queryClient.invalidateQueries({
        queryKey: NOTIFICATIONS_QUERY_KEY,
      });
    },
  });
}

export function useDeleteNotification() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: notificationsService.delete,

    onSuccess: () => {
      return queryClient.invalidateQueries({
        queryKey: NOTIFICATIONS_QUERY_KEY,
      });
    },
  });
}
