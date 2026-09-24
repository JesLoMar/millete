import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';

import { useAuth } from '@/features/auth/context/AuthContext';
import { ROUTES } from '@/app/router/routes';
import { notify } from '@/shared/utils/notifications/notify';

import { profileService } from '../services/profileService';
import type { DeactivateAccountRequest } from '../types';

export function useDeactivateAccount() {
  const navigate = useNavigate();
  const { logout } = useAuth();

  return useMutation({
    mutationFn: (data: DeactivateAccountRequest) =>
      profileService.deactivateAccount(data),

    onSuccess: async () => {
      await logout();

      notify.success('Cuenta eliminada correctamente');

      navigate(ROUTES.login, {
        replace: true,
      });
    },

    onError: (error: unknown) => {
      const message = axios.isAxiosError(error)
        ? error.response?.data?.message ??
          'Error al eliminar la cuenta'
        : 'Error al eliminar la cuenta';

      notify.error(message);
    },
  });
}