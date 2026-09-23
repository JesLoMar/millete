import axios from 'axios';

import i18n from '@/lib/i18n';
import { notify } from '@/shared/utils/notifications/notify';

declare module 'axios' {
  export interface AxiosRequestConfig {
    skipGlobalErrorNotify?: boolean;
    skipAuthErrorHandler?: boolean;
  }
}

const API_URL =
  import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

export const apiClient = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
  withCredentials: true,
});

const AUTH_ENDPOINTS = [
  '/auth/login',
  '/auth/register',
  '/auth/logout',
];

const isAuthEndpoint = (url?: string): boolean => {
  if (!url) {
    return false;
  }

  try {
    const normalizedUrl = url.replace(/^\/+/, '');
    const requestUrl = new URL(
      normalizedUrl,
      API_URL.endsWith('/') ? API_URL : `${API_URL}/`,
    );

    const pathname =
      requestUrl.pathname.replace(/\/+$/, '') || '/';

    return AUTH_ENDPOINTS.some((endpoint) => {
      const normalizedEndpoint = endpoint.replace(/\/+$/, '');

      return (
        pathname === normalizedEndpoint ||
        pathname.endsWith(normalizedEndpoint)
      );
    });
  } catch {
    return false;
  }
};

let sessionExpiredNotified = false;

apiClient.interceptors.response.use(
  (response) => {
    sessionExpiredNotified = false;
    return response;
  },
  (error) => {
    const status = error.response?.status;

    if (status === 401) {
      const shouldForceLogout =
        !isAuthEndpoint(error.config?.url) &&
        !error.config?.skipAuthErrorHandler;

      if (shouldForceLogout && !sessionExpiredNotified) {
        sessionExpiredNotified = true;
        window.dispatchEvent(new Event('auth:logout'));
      }

      return Promise.reject(error);
    }

    if (!error.config?.skipGlobalErrorNotify) {
      const errorMessage =
        error.response?.data?.message ||
        error.response?.data?.error ||
        error.message ||
        i18n.t('api:errors.default');

      let description = '';

      if (status === 403) {
        description = i18n.t('api:errors.status_403');
      } else if (status === 404) {
        description = i18n.t('api:errors.status_404');
      } else if (status >= 500) {
        description = i18n.t('api:errors.status_500');
      } else if (error.code === 'ECONNABORTED') {
        description = i18n.t('api:errors.timeout');
      }

      notify.error(errorMessage, { description });
    }

    return Promise.reject(error);
  },
);