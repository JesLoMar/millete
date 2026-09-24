import {
  useMutation,
  useQuery,
  useQueryClient,
} from '@tanstack/react-query';
import axios from 'axios';
import { useTranslation } from 'react-i18next';

import { notify } from '@/shared/utils/notifications/notify';

import { profileService } from '../services/profileService';
import type { SessionResponse } from '../types';

export function useSessions() {
  const queryClient = useQueryClient();
  const { t } = useTranslation('userProfile');

  const {
    data: sessions,
    isLoading,
    error,
  } = useQuery<SessionResponse[]>({
    queryKey: ['sessions'],
    queryFn: profileService.getSessions,
  });

  const deleteSessionMutation = useMutation({
    mutationFn: (sessionId: string) =>
      profileService.deleteSession(sessionId),

    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['sessions'],
      });

      notify.success(
        t('sessions.success'),
      );
    },

    onError: (error: unknown) => {
      const message = axios.isAxiosError(error)
        ? error.response?.data?.message ??
          t('sessions.error')
        : t('sessions.error');

      notify.error(message);
    },
  });

  const deleteAllOtherSessionsMutation =
    useMutation({
      mutationFn: () =>
        profileService.deleteAllOtherSessions(),

      onSuccess: () => {
        queryClient.invalidateQueries({
          queryKey: ['sessions'],
        });

        notify.success(
          t('sessions.successAll'),
        );
      },

      onError: (error: unknown) => {
        const message = axios.isAxiosError(error)
          ? error.response?.data?.message ??
            t('sessions.error')
          : t('sessions.error');

        notify.error(message);
      },
    });

  return {
    sessions,
    isLoading,
    error,

    deleteSession:
      deleteSessionMutation.mutate,
    isDeletingSession:
      deleteSessionMutation.isPending,

    deleteAllOtherSessions:
      deleteAllOtherSessionsMutation.mutate,
    isDeletingAllSessions:
      deleteAllOtherSessionsMutation.isPending,
  };
}