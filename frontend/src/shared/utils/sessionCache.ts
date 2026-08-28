/**
 * sessionCache — caché de sesión para datos NO sensibles.
 *
 * Contrato (léelo antes de guardar nada aquí):
 * - Es sessionStorage con prefijo: legible por CUALQUIER JS que corra en la
 *   página, incluido un posible XSS. No ofrece ninguna garantía de seguridad.
 * - Uso permitido: datos de display no sensibles (nombre, email, ids de UI).
 * - PROHIBIDO: tokens, contraseñas, credenciales o cualquier secreto.
 *   La sesión real vive en la cookie httpOnly y la valida el backend.
 *
 * (Antes se llamaba "secureStorage"; el nombre sugería una garantía que no existe.)
 */
const STORAGE_PREFIX = 'ms_';

export const sessionCache = {
  setItem(key: string, value: string): void {
    try {
      sessionStorage.setItem(`${STORAGE_PREFIX}${key}`, value);
    } catch {
      sessionStorage.removeItem(`${STORAGE_PREFIX}${key}`);
    }
  },
  getItem(key: string): string | null {
    try {
      return sessionStorage.getItem(`${STORAGE_PREFIX}${key}`);
    } catch {
      sessionStorage.removeItem(`${STORAGE_PREFIX}${key}`);
      return null;
    }
  },
  removeItem(key: string): void {
    sessionStorage.removeItem(`${STORAGE_PREFIX}${key}`);
  },
  setUser(user: unknown): void {
    try {
      sessionStorage.setItem(`${STORAGE_PREFIX}user`, JSON.stringify(user));
    } catch {
      sessionStorage.removeItem(`${STORAGE_PREFIX}user`);
    }
  },
  getUser<T>(): T | null {
    try {
      const raw = sessionStorage.getItem(`${STORAGE_PREFIX}user`);
      return raw ? (JSON.parse(raw) as T) : null;
    } catch {
      sessionStorage.removeItem(`${STORAGE_PREFIX}user`);
      return null;
    }
  },
  setSessionId(sessionId: string): void {
    this.setItem('sessionId', sessionId);
  },
  getSessionId(): string | null {
    return this.getItem('sessionId');
  },
  clear(): void {
    sessionStorage.removeItem(`${STORAGE_PREFIX}user`);
    sessionStorage.removeItem(`${STORAGE_PREFIX}sessionId`);
  },
};
