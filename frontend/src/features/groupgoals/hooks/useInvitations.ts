import { useMutation, useQueryClient } from '@tanstack/react-query';
import { invitationsService } from '../services/invitations.service';

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
