import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

import { authService } from '../services/auth.service';
import { useAuth } from '../context/AuthContext';
import { ROUTES } from '@/app/router/routes';
import type {
  RegisterUserRequest,
  LoginRequest,
  LoginResponse,
} from '../types';
import type { ApiError } from '@/shared/types/api';
import { notify } from '@/shared/utils/notifications/notify';

const isApiError = (error: ApiError | Error): error is ApiError => {
  return 'response' in error;
};

export const useRegisterMutation = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const queryClient = useQueryClient();
  const { t } = useTranslation();

  return useMutation<LoginResponse, ApiError | Error, RegisterUserRequest>({
    mutationFn: async (data: RegisterUserRequest) => {
      await authService.register(data);

      const identifier = data.username || data.email || '';

      if (!identifier) {
        throw new Error(t('auth:errors.no_identifier'));
      }

      const loginData: LoginRequest = {
        identifier,
        password: data.password,
      };

      try {
        return await authService.login(loginData);
      } catch {
        throw new Error(t('auth:errors.auto_login_failed'));
      }
    },

    onSuccess: async () => {
      await login();
      queryClient.clear();

      notify.success(t('auth:alerts.register_success'));
      navigate(ROUTES.dashboard, { replace: true });
    },

    onError: (error: ApiError | Error) => {
      if (isApiError(error)) {
        const errorMessage =
          error.response?.data?.message ||
          t('auth:errors.register_failed');

        notify.error(errorMessage);
        return;
      }

      notify.error(
        error.message || t('auth:errors.register_failed'),
      );
    },
  });
};