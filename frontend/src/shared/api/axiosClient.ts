import axios from 'axios';

import i18n from '@/lib/i18n';

import { notify } from '@/shared/utils/notifications/notify';

declare module 'axios' {
  export interface AxiosRequestConfig {
    skipGlobalErrorNotify?: boolean;
    skipAuthErrorHandler?: boolean;
    /** Interno: época de sesión en la que se lanzó la petición. */
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

/**
 * Devuelve la ruta de `url` relativa a la base de la API
 * (p. ej. '/auth/login'), o null si no se puede determinar.
 * Funciona con VITE_API_URL relativa ('/api/v1') y absoluta.
 */
const getApiRelativePath = (
  url?: string,
): string | null => {
  if (!url) {
    return null;
  }

  try {
    // Resuelve la base contra el origen actual: así una URL
    // relativa ('/api/v1') pasa a ser absoluta y `new URL` no falla.
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

/**
 * Época de sesión. Cada petición se marca con la época vigente al
 * lanzarse. Un 401 solo provoca cierre de sesión si su petición
 * pertenece a la época actual; así:
 *  - de N peticiones simultáneas que dan 401 solo la primera cierra
 *    sesión (reclama la época y las demás quedan obsoletas);
 *  - un 401 tardío de una sesión anterior no puede cerrar la nueva.
 * No depende de reseteos por URL: AuthContext la incrementa en
 * cada inicio y cierre de sesión.
 */
let authEpoch = 0;

export const bumpAuthEpoch = (): void => {
  authEpoch += 1;
};

// `synchronous: true` hace que la marca se ponga en el momento de llamar
// a apiClient.get/post (no un microtask después), así una petición
// lanzada justo antes de un cambio de sesión conserva su época.
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
    const status = error.response?.status;

    if (status === 401) {
      const shouldForceLogout =
        error.config?.authEpoch === authEpoch &&
        !error.config?.skipAuthErrorHandler &&
        !isAuthEndpoint(error.config?.url);

      if (shouldForceLogout) {
        // Reclama la época antes de emitir el evento.
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
        description = i18n.t(
          'api:errors.status_403',
        );
      } else if (status === 404) {
        description = i18n.t(
          'api:errors.status_404',
        );
      } else if (status >= 500) {
        description = i18n.t(
          'api:errors.status_500',
        );
      } else if (error.code === 'ECONNABORTED') {
        description = i18n.t(
          'api:errors.timeout',
        );
      }

      notify.error(errorMessage, { description });
    }

    return Promise.reject(error);
  },
);