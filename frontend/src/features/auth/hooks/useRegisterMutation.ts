import {
  useMutation,
  useQueryClient,
} from '@tanstack/react-query';
import axios from 'axios';
import { useTranslation } from 'react-i18next';

import { authService } from '../services/auth.service';
import { useAuth } from '../context/AuthContext';
import { handlePostAuthSuccess } from '../utils/handlePostAuthSuccess';

import type {
  RegisterUserRequest,
  LoginResponse,
} from '../types';

import { notify } from '@/shared/utils/notifications/notify';

type RegisterFlowResult =
  | {
      kind: 'authenticated';
      response: LoginResponse;
    }
  | {
      kind: 'created-login-failed';
      identifier: string;
    };

interface UseRegisterMutationOptions {
  onAutoLoginFailed?: (identifier: string) => void;
}

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

export const useRegisterMutation = (
  options: UseRegisterMutationOptions = {},
) => {
  const { login, waitForPendingLogout } = useAuth();
  const queryClient = useQueryClient();
  const { t } = useTranslation('auth');

  return useMutation<
    RegisterFlowResult,
    unknown,
    RegisterUserRequest
  >({
    mutationFn: async (data) => {
      const username = data.username?.trim() || '';
      const email = data.email?.trim() || '';

      // Validar antes de crear la cuenta.
      const identifier = username || email;

      if (!identifier) {
        throw new Error(
          t('auth:errors.no_identifier'),
        );
      }

      await authService.register({
        username,
        email,
        password: data.password,
      });

      // A partir de aquí, el registro ya ha sido confirmado.
      try {
        await waitForPendingLogout();

        const response = await authService.login({
          identifier,
          password: data.password,
        });

        return {
          kind: 'authenticated',
          response,
        };
      } catch {
        return {
          kind: 'created-login-failed',
          identifier,
        };
      }
    },

    onSuccess: async (result) => {
      if (result.kind === 'created-login-failed') {
        options.onAutoLoginFailed?.(result.identifier);

        // Texto temporal hasta preparar las traducciones.
        notify.error(
          'La cuenta se ha creado, pero no se ha podido iniciar sesión automáticamente. Inicia sesión con tus credenciales.',
        );

        return;
      }

      await handlePostAuthSuccess({
        login,
        loginData: result.response,
        queryClient,
        successMessage: t(
          'auth:alerts.register_success',
        ),
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