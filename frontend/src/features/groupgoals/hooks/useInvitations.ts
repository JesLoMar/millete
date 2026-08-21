import { useMutation, useQueryClient } from '@tanstack/react-query';
import { invitationsService } from '../services/invitations.service';

// Una sola invalidación por prefijo: ['notifications'] cubre la lista completa,
// las variantes ['notifications', 'recent', n], la paginada y ['notifications',
// 'unread-count'] — y invalidate ya refetchea las queries activas, así que
// refetchQueries/setQueryData eran redundantes y podían divergir.
export function useAcceptInvitation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: invitationsService.accept,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['group-goals'] });
    },
  });
}

export function useRejectInvitation() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: invitationsService.reject,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
      queryClient.invalidateQueries({ queryKey: ['group-goals'] });
    },
  });
}
