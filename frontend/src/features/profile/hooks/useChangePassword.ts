import { useMutation, useQueryClient } from '@tanstack/react-query';
import { notify } from "@/shared/utils/notifications/notify";
import { profileService } from '../services/profileService';
import type { ChangePasswordRequest } from '../types';
import type { AxiosError } from 'axios';

export function useChangePassword() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (data: ChangePasswordRequest) => profileService.changePassword(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['user'] });
      notify.success('Contraseña actualizada correctamente');
    },
    onError: (error: AxiosError<{ message?: string }>) => {
      const message = error.response?.data?.message || 'Error al cambiar la contraseña';
      notify.error(message);
    },
  });
}
