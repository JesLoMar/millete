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

  setToken(token: string): void {
    try {
      sessionStorage.setItem(`${STORAGE_PREFIX}token`, token);
    } catch {
      sessionStorage.removeItem(`${STORAGE_PREFIX}token`);
    }

    try {
      const payload = token.split('.');
      if (payload.length === 3 && payload[1]) {
        const padded = payload[1].replace(/-/g, '+').replace(/_/g, '/');
        const padLen = 4 - (padded.length % 4);
        const base64 = padLen !== 4 ? padded + '='.repeat(padLen) : padded;
        const decoded = JSON.parse(atob(base64)) as Record<string, unknown>;
        const sessionId = decoded.sessionId ?? decoded.sid ?? decoded.jti ?? decoded.sub;
        if (typeof sessionId === 'string') {
          this.setItem('sessionId', sessionId);
        }
      }
    } catch {
      // Silencioso: el token sigue siendo válido
    }
  },

  getToken(): string | null {
    try {
      return sessionStorage.getItem(`${STORAGE_PREFIX}token`);
    } catch {
      sessionStorage.removeItem(`${STORAGE_PREFIX}token`);
      return null;
    }
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
    sessionStorage.removeItem(`${STORAGE_PREFIX}token`);
    sessionStorage.removeItem(`${STORAGE_PREFIX}user`);
    sessionStorage.removeItem(`${STORAGE_PREFIX}sessionId`);
  },
};
