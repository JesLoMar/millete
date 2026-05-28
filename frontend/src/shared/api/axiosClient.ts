import axios from 'axios';
import { notify } from '@/shared/utils/notifications/notify';
import { secureStorage } from '@/shared/utils/secureStorage';
import i18n from '@/lib/i18n';

declare module 'axios' {
  export interface AxiosRequestConfig {
    skipGlobalErrorNotify?: boolean;
  }
}

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

export const apiClient = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

// ─── INTERCEPTOR DE REQUEST ────────────────────────────────────
apiClient.interceptors.request.use(
  (config) => {
    const token = secureStorage.getToken();
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

// ─── INTERCEPTOR DE RESPONSE ───────────────────────────────────
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const message = error.response?.data?.message || '';

    if (status === 401) {
      secureStorage.clear();
      window.dispatchEvent(new CustomEvent('auth:logout'));
    }

    // ─── Notificación global de errores ────────────────────────
    if (!error.config?.skipGlobalErrorNotify) {
      const errorMessage =
        message || error.response?.data?.error || error.message || i18n.t('api.errors.default');

      let description = '';

      if (status === 401) {
        description = i18n.t('api.errors.status_401');
      } else if (status === 403) {
        description = i18n.t('api.errors.status_403');
      } else if (status === 404) {
        description = i18n.t('api.errors.status_404');
      } else if (status >= 500) {
        description = i18n.t('api.errors.status_500');
      } else if (error.code === 'ECONNABORTED') {
        description = i18n.t('api.errors.timeout');
      }

      notify.error(errorMessage, { description });
    }

    return Promise.reject(error);
  },
);