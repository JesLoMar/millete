import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { notify } from "@/shared/utils/notifications/notify";
import { profileService } from '../services/profileService';
import type { AxiosError } from 'axios';

export function useSessions() {
  const queryClient = useQueryClient();

  const { data: sessions, isLoading, error } = useQuery({
    queryKey: ['sessions'],
    queryFn: profileService.getSessions,
  });

  const deleteSessionMutation = useMutation({
    mutationFn: (sessionId: string) => profileService.deleteSession(sessionId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['sessions'] });
      notify.success('Sesión cerrada correctamente');
    },
    onError: (error: AxiosError<{ message?: string }>) => {
      const message = error.response?.data?.message || 'Error al gestionar sesiones';
      notify.error(message);
    },
  });

  const deleteAllOtherSessionsMutation = useMutation({
    mutationFn: () => profileService.deleteAllOtherSessions(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['sessions'] });
      notify.success('Todas las demás sesiones han sido cerradas');
    },
    onError: (error: AxiosError<{ message?: string }>) => {
      const message = error.response?.data?.message || 'Error al gestionar sesiones';
      notify.error(message);
    },
  });

  return {
    sessions,
    isLoading,
    error,
    deleteSession: deleteSessionMutation.mutate,
    isDeletingSession: deleteSessionMutation.isPending,
    deleteAllOtherSessions: deleteAllOtherSessionsMutation.mutate,
    isDeletingAllSessions: deleteAllOtherSessionsMutation.isPending,
  };
}