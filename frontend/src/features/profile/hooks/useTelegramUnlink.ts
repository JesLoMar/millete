import { useMutation, useQueryClient } from '@tanstack/react-query';
import { notify } from "@/shared/utils/notifications/notify";
import { profileService } from '../services/profileService';
import type { AxiosError } from 'axios';

export function useTelegramUnlink() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: () => profileService.unlinkTelegram(),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['profile'] });
      notify.success('Telegram desvinculado correctamente');
    },
    onError: (error: AxiosError<{ message?: string }>) => {
      const message = error.response?.data?.message || 'Error al desvincular Telegram';
      notify.error(message);
    },
  });
}
