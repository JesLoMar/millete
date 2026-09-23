/**
 * sessionCache — caché de sesión para datos NO sensibles.
 *
 * Contrato:
 * - Usa sessionStorage con un prefijo propio.
 * - Cualquier JavaScript que se ejecute en la página puede leer estos datos.
 * - No ofrece ninguna garantía de seguridad frente a XSS.
 * - Solo debe almacenar datos de display no sensibles.
 * - Nunca guardar tokens, contraseñas, credenciales ni secretos.
 *
 * La sesión real vive en una cookie httpOnly y es validada por el backend.
 */
const STORAGE_PREFIX = 'ms_';

const getStorageKey = (key: string): string =>
  `${STORAGE_PREFIX}${key}`;

const setStorageItem = (key: string, value: string): void => {
  try {
    sessionStorage.setItem(getStorageKey(key), value);
  } catch {
    // sessionStorage puede no estar disponible o puede haber fallado.
    removeStorageItem(key);
  }
};

const getStorageItem = (key: string): string | null => {
  try {
    return sessionStorage.getItem(getStorageKey(key));
  } catch {
    removeStorageItem(key);
    return null;
  }
};

const removeStorageItem = (key: string): void => {
  try {
    sessionStorage.removeItem(getStorageKey(key));
  } catch {
    // Ignoramos errores de acceso a sessionStorage.
  }
};

export const sessionCache = {
  setItem(key: string, value: string): void {
    setStorageItem(key, value);
  },

  getItem(key: string): string | null {
    return getStorageItem(key);
  },

  removeItem(key: string): void {
    removeStorageItem(key);
  },

  setUser(user: unknown): void {
    try {
      setStorageItem('user', JSON.stringify(user));
    } catch {
      removeStorageItem('user');
    }
  },

  getUser<T>(): T | null {
    const raw = getStorageItem('user');

    if (!raw) {
      return null;
    }

    try {
      return JSON.parse(raw) as T;
    } catch {
      removeStorageItem('user');
      return null;
    }
  },

  setSessionId(sessionId: string): void {
    setStorageItem('sessionId', sessionId);
  },

  getSessionId(): string | null {
    return getStorageItem('sessionId');
  },

  clear(): void {
    removeStorageItem('user');
    removeStorageItem('sessionId');
  },
};