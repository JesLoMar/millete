import axios from 'axios';

import i18n from '@/lib/i18n';

import { notify } from '@/shared/utils/notifications/notify';

declare module 'axios' {
  export interface AxiosRequestConfig {
    skipGlobalErrorNotify?: boolean;
    skipAuthErrorHandler?: boolean;
    authEpoch?: number;
  }
}

const API_URL =
  import.meta.env.VITE_API_URL ||
  'http://localhost:8080/api/v1';

export const apiClient = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
  withCredentials: true,
});

const normalizePath = (path: string): string =>
  path.replace(/\/+$/, '') || '/';

const getApiRelativePath = (
  url?: string,
): string | null => {
  if (!url) {
    return null;
  }

  try {
    const apiBase = new URL(API_URL, window.location.origin);
    const apiBaseHref = `${apiBase.href.replace(/\/+$/, '')}/`;

    const requestUrl = /^https?:\/\//i.test(url)
      ? new URL(url)
      : new URL(url.replace(/^\/+/, ''), apiBaseHref);

    if (requestUrl.origin !== apiBase.origin) {
      return null;
    }

    const basePath = normalizePath(apiBase.pathname);
    let requestPath = normalizePath(requestUrl.pathname);

    if (
      basePath !== '/' &&
      (requestPath === basePath ||
        requestPath.startsWith(`${basePath}/`))
    ) {
      requestPath =
        requestPath.slice(basePath.length) || '/';
    }

    return normalizePath(requestPath);
  } catch {
    return null;
  }
};

const isAuthEndpoint = (url?: string): boolean => {
  const path = getApiRelativePath(url);

  return path === '/auth' || !!path?.startsWith('/auth/');
};

let authEpoch = 0;

export const bumpAuthEpoch = (): void => {
  authEpoch += 1;
};

apiClient.interceptors.request.use(
  (config) => {
    config.authEpoch = authEpoch;

    return config;
  },
  null,
  { synchronous: true },
);

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (axios.isCancel(error) || error?.code === 'ERR_CANCELED') {
      return Promise.reject(error);
    }

    const status = error.response?.status;

    if (status === 401) {
      const shouldForceLogout =
        error.config?.authEpoch === authEpoch &&
        !error.config?.skipAuthErrorHandler &&
        !isAuthEndpoint(error.config?.url);

      if (shouldForceLogout) {
        authEpoch += 1;

        window.dispatchEvent(
          new Event('auth:logout'),
        );
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
      } else if (status === 409) {
        description = i18n.t('api:errors.status_409');
      } else if (status >= 500) {
        description = i18n.t('api:errors.status_500');
      } else if (error.code === 'ECONNABORTED') {
        description = i18n.t('api:errors.timeout');
      }
      notify.error(errorMessage, {
        description,
        id: `api-error:${status ?? error.code ?? 'unknown'}:${errorMessage}`,
      });
    }

    return Promise.reject(error);
  },
);