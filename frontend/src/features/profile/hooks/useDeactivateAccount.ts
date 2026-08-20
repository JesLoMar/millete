import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { notify } from "@/shared/utils/notifications/notify";
import { useAuth } from '@/features/auth/context/AuthContext';
import { profileService } from '../services/profileService';
import type { DeactivateAccountRequest } from '../types';
import type { AxiosError } from 'axios';

export function useDeactivateAccount() {
  const queryClient = useQueryClient();
  const navigate = useNavigate();
  const { logout } = useAuth();

  return useMutation({
    mutationFn: (data: DeactivateAccountRequest) => profileService.deactivateAccount(data),
    onSuccess: async () => {
      await logout();
      queryClient.clear();
      navigate('/login', { replace: true });
      notify.success('Cuenta eliminada correctamente');
    },
    onError: (error: AxiosError<{ message?: string }>) => {
      const message = error.response?.data?.message || 'Error al eliminar la cuenta';
      notify.error(message);
    },
  });
}