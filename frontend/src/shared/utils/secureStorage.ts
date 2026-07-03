const STORAGE_PREFIX = 'ms_';

export const secureStorage = {
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
