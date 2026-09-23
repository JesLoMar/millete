import { useMutation, useQueryClient } from '@tanstack/react-query';
import {
  useLocation,
  useNavigate,
  useSearchParams,
} from 'react-router-dom';
import { useTranslation } from 'react-i18next';

import { authService } from '../services/auth.service';
import { useAuth } from '../context/AuthContext';
import { PROTECTED_ROUTE_PATHS, ROUTES } from '@/app/router/routes';
import type { ApiError } from '@/shared/types/api';
import type { LoginRequest } from '../types';
import { notify } from '@/shared/utils/notifications/notify';

function sanitizeRedirect(raw: string | null | undefined): string | null {
  if (!raw) {
    return null;
  }

  if (/^(?:https?:)?\/\//i.test(raw) || /^javascript:/i.test(raw)) {
    return null;
  }

  if (!raw.startsWith('/') || raw.startsWith('//')) {
    return null;
  }

  const basePath = raw.split('?')[0].split('#')[0];

  const isAllowed = PROTECTED_ROUTE_PATHS.some(
    (route) =>
      basePath === route || basePath.startsWith(`${route}/`),
  );

  return isAllowed ? raw : null;
}

export const useLoginMutation = () => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const queryClient = useQueryClient();
  const [searchParams] = useSearchParams();
  const { t } = useTranslation();

  return useMutation({
    mutationFn: (credentials: LoginRequest) =>
      authService.login(credentials),

    onSuccess: async () => {
      await login();
      queryClient.clear();

      notify.success(t('common:actions.welcome'));

      const fromState = location.state?.from?.pathname
        ? location.state.from.pathname +
          (location.state.from.search || '') +
          (location.state.from.hash || '')
        : null;

      const fromQuery = searchParams.get('redirect');
      const rawRedirect = fromState || fromQuery;
      const safeRedirect = sanitizeRedirect(rawRedirect);

      navigate(safeRedirect || ROUTES.dashboard, { replace: true });
    },

    onError: (error: ApiError) => {
      const errorMessage =
        error.response?.data?.message ||
        t('auth:errors.login_failed');

      notify.error(errorMessage);
    },
  });
};