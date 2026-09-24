import { useMutation, useQueryClient } from '@tanstack/react-query';
import axios from 'axios';

import { notify } from '@/shared/utils/notifications/notify';

import { profileService } from '../services/profileService';
import type { UpdateProfileRequest } from '../types';

export function useUpdateProfile() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: UpdateProfileRequest) =>
      profileService.updateProfile(data),

    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['profile'],
      });

      notify.success('Perfil actualizado correctamente');
    },

    onError: (error: unknown) => {
      const message = axios.isAxiosError(error)
        ? error.response?.data?.message ??
          'Error al actualizar el perfil'
        : 'Error al actualizar el perfil';

      notify.error(message);
    },
  });
}