import { useMutation } from '@tanstack/react-query';
import axios from 'axios';

import { notify } from '@/shared/utils/notifications/notify';

import { profileService } from '../services/profileService';
import type { ChangePasswordRequest } from '../types';

export function useChangePassword() {
  return useMutation({
    mutationFn: (data: ChangePasswordRequest) =>
      profileService.changePassword(data),

    onSuccess: () => {
      notify.success('Contraseña actualizada correctamente');
    },

    onError: (error: unknown) => {
      const message = axios.isAxiosError(error)
        ? error.response?.data?.message ??
          'Error al cambiar la contraseña'
        : 'Error al cambiar la contraseña';

      notify.error(message);
    },
  });
}