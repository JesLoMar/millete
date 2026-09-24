import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useTranslation } from 'react-i18next';

import { useAuth } from '@/features/auth/context/AuthContext';
import { ROUTES } from '@/app/router/routes';
import { notify } from '@/shared/utils/notifications/notify';

import { profileService } from '../services/profileService';
import type { DeactivateAccountRequest } from '../types';

export function useDeactivateAccount() {
  const navigate = useNavigate();
  const { logout } = useAuth();
  const { t } = useTranslation('userProfile');

  return useMutation({
    mutationFn: (
      data: DeactivateAccountRequest,
    ) => profileService.deactivateAccount(data),

    onSuccess: async () => {
      await logout();

      notify.success(
        t('deleteAccount.success'),
      );

      navigate(ROUTES.login, {
        replace: true,
      });
    },

    onError: (error: unknown) => {
      const message = axios.isAxiosError(error)
        ? error.response?.data?.message ??
          t('deleteAccount.error')
        : t('deleteAccount.error');

      notify.error(message);
    },
  });
}