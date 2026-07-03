import { useMutation, useQueryClient } from '@tanstack/react-query';
import { invitationsService } from '../services/invitations.service';
import type { Notification } from '@/features/notifications/types';

export function useAcceptInvitation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: invitationsService.accept,
    onSuccess: (_, invitationId) => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['notifications', 'unread-count'] });
      queryClient.refetchQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['group-goals'] });


      queryClient.setQueryData<Notification[]>(['notifications'], (old) => {
        if (!old) return old;
        return old.map((n) =>
          n.metadata?.invitationId === invitationId || n.metadata?.id === invitationId
            ? { ...n, actionedAt: new Date().toISOString() }
            : n
        );
      });
    },
  });
}

export function useRejectInvitation() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: invitationsService.reject,
    onSuccess: (_, invitationId) => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['notifications', 'unread-count'] });
      queryClient.refetchQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['group-goals'] });


      queryClient.setQueryData<Notification[]>(['notifications'], (old) => {
        if (!old) return old;
        return old.map((n) =>
          n.metadata?.invitationId === invitationId || n.metadata?.id === invitationId
            ? { ...n, actionedAt: new Date().toISOString() }
            : n
        );
      });
    },
  });
}
