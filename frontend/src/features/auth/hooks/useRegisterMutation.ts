import {
  useMutation,
  useQueryClient,
} from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { useTranslation } from 'react-i18next';

import { authService } from '../services/auth.service';
import { useAuth } from '../context/AuthContext';
import { handlePostAuthSuccess } from '../utils/handlePostAuthSuccess';

import { ROUTES } from '@/app/router/routes';
import type {
  RegisterUserRequest,
  LoginRequest,
  LoginResponse,
} from '../types';
import { notify } from '@/shared/utils/notifications/notify';

const getErrorMessage = (
  error: unknown,
  fallback: string,
): string => {
  if (axios.isAxiosError(error)) {
    return (
      error.response?.data?.message ||
      error.response?.data?.error ||
      fallback
    );
  }

  if (
    error instanceof Error &&
    error.message
  ) {
    return error.message;
  }

  return fallback;
};

export const useRegisterMutation = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const queryClient = useQueryClient();
  const { t } = useTranslation();

  return useMutation<
    LoginResponse,
    unknown,
    RegisterUserRequest
  >({
    mutationFn: async (
      data: RegisterUserRequest,
    ) => {
      await authService.register(data);

      const identifier =
        data.username ||
        data.email ||
        '';

      if (!identifier) {
        throw new Error(
          t('auth:errors.no_identifier'),
        );
      }

      const loginData: LoginRequest = {
        identifier,
        password: data.password,
      };

      try {
        return await authService.login(
          loginData,
        );
      } catch {
        throw new Error(
          t(
            'auth:errors.auto_login_failed',
          ),
        );
      }
    },

    onSuccess: async () => {
      await handlePostAuthSuccess({
        login,
        queryClient,
        navigate,
        successMessage: t(
          'auth:alerts.register_success',
        ),
        destination: ROUTES.dashboard,
      });
    },

    onError: (error: unknown) => {
      const message = getErrorMessage(
        error,
        t('auth:errors.register_failed'),
      );

      notify.error(message);
    },
  });
};