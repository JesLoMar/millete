import { useMutation, useQueryClient } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import { authService } from '../services/auth.service';
import { useAuth } from '../context/AuthContext';
import { notify } from '@/shared/utils/notifications/notify';
import i18n from '@/lib/i18n';
import type { ApiError } from '@/shared/types/api';
import type { RegisterUserRequest, LoginRequest, TokenResponse } from '../types';

export const useRegisterMutation = () => {
  const navigate = useNavigate();
  const { login } = useAuth();
  const queryClient = useQueryClient();

  return useMutation<TokenResponse, ApiError, RegisterUserRequest>({
    mutationFn: async (data: RegisterUserRequest) => {
      await authService.register(data);

      const identifier = data.username || data.email || "";
      if (!identifier) {
        throw new Error(i18n.t('auth.errors.no_identifier'));
      }

      const loginData: LoginRequest = {
        identifier,
        password: data.password,
      };

      try {
        return await authService.login(loginData);
      } catch {
        throw new Error(i18n.t('auth.errors.auto_login_failed'));
      }
    },
    onSuccess: (data) => {
      login(data.token);
      queryClient.invalidateQueries();
      
      notify.success(i18n.t('auth.alerts.register_success'));
      
      navigate('/dashboard', { replace: true });
    },
    onError: (error: ApiError) => {
      const errorMessage = error.response?.data?.message || i18n.t('auth.errors.register_failed');
      notify.error(errorMessage);
    }
  });
};