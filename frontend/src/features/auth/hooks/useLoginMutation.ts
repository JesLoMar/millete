import {
  useMutation,
  useQueryClient,
} from '@tanstack/react-query';

import { useTranslation } from 'react-i18next';

import { authService } from '../services/auth.service';
import { useAuth } from '../context/AuthContext';
import { handlePostAuthSuccess } from '../utils/handlePostAuthSuccess';

import type { ApiError } from '@/shared/types/api';
import type { LoginRequest } from '../types';
import { notify } from '@/shared/utils/notifications/notify';

export const useLoginMutation = () => {
  const { login, waitForPendingLogout } = useAuth();
  const queryClient = useQueryClient();
  const { t } = useTranslation();

  return useMutation({
    mutationFn: async (credentials: LoginRequest) => {
      await waitForPendingLogout();

      return authService.login({
        ...credentials,
        identifier: credentials.identifier.trim(),
      });
    },

    onSuccess: async (loginData) => {
      await handlePostAuthSuccess({
        login,
        loginData,
        queryClient,
        successMessage: t('common:actions.welcome'),
      });
    },

    onError: (error: ApiError) => {
      const errorMessage =
        error.response?.data?.message ||
        t('auth:errors.login_failed');

      notify.error(errorMessage);
    },
  });
};