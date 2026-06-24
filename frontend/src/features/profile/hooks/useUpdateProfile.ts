import { useMutation, useQueryClient } from '@tanstack/react-query';
import { notify } from "@/shared/utils/notifications/notify";
import { profileService } from '../services/profileService';
import type { UpdateProfileRequest } from '../types';
import type { AxiosError } from 'axios';

export function useUpdateProfile() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: UpdateProfileRequest) => profileService.updateProfile(data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['profile'] });
      notify.success('Perfil actualizado correctamente');
    },
    onError: (error: AxiosError<{ message?: string }>) => {
      const message = error.response?.data?.message || 'Error al actualizar el perfil';
      notify.error(message);
    },
  });
}
