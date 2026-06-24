const STORAGE_PREFIX = 'ms_';

function deriveFingerprint(): string {
  const seed = [
    navigator.userAgent,
    screen.colorDepth,
    screen.width,
    screen.height,
    navigator.language,
    navigator.hardwareConcurrency,
  ].join('|');

  let hash = 0;
  for (let i = 0; i < seed.length; i++) {
    const char = seed.charCodeAt(i);
    hash = ((hash << 5) - hash) + char;
    hash = hash & hash;
  }
  return Math.abs(hash).toString(36);
}

function xorTransform(text: string, key: string): string {
  let result = '';
  for (let i = 0; i < text.length; i++) {
    result += String.fromCharCode(text.charCodeAt(i) ^ key.charCodeAt(i % key.length));
  }
  return result;
}

function encrypt(value: string): string {
  const key = deriveFingerprint();
  return btoa(xorTransform(value, key));
}

function decrypt(value: string): string {
  const key = deriveFingerprint();
  return xorTransform(atob(value), key);
}

function addBase64Padding(base64: string): string {
  const padding = 4 - (base64.length % 4);
  if (padding !== 4) {
    return base64 + '='.repeat(padding);
  }
  return base64;
}

function decodeJwtPayload(token: string): Record<string, unknown> | null {
  try {
    const parts = token.split('.');
    if (parts.length !== 3) return null;
    const payload = parts[1];
    if (!payload) return null;
    const padded = addBase64Padding(payload.replace(/-/g, '+').replace(/_/g, '/'));
    const decoded = atob(padded);
    return JSON.parse(decoded) as Record<string, unknown>;
  } catch {
    return null;
  }
}

function extractSessionId(token: string): string | null {
  const payload = decodeJwtPayload(token);
  if (!payload) return null;
  // El claim puede tener varios nombres comunes
  const sessionId =
    payload.sessionId ??
    payload.sid ??
    payload.jti ??
    payload.sub;
  if (typeof sessionId === 'string') {
    return sessionId;
  }
  return null;
}

export const secureStorage = {
  setItem(key: string, value: string): void {
    try {
      const encrypted = encrypt(value);
      localStorage.setItem(`${STORAGE_PREFIX}${key}`, encrypted);
    } catch {
      localStorage.removeItem(`${STORAGE_PREFIX}${key}`);
    }
  },

  getItem(key: string): string | null {
    try {
      const encrypted = localStorage.getItem(`${STORAGE_PREFIX}${key}`);
      if (!encrypted) return null;
      return decrypt(encrypted);
    } catch {
      localStorage.removeItem(`${STORAGE_PREFIX}${key}`);
      return null;
    }
  },

  removeItem(key: string): void {
    localStorage.removeItem(`${STORAGE_PREFIX}${key}`);
  },

  setToken(token: string): void {
    // Guardar token — si falla, borrar
    try {
      const payload = `${token}::${Date.now()}`;
      const encrypted = encrypt(payload);
      localStorage.setItem(`${STORAGE_PREFIX}token`, encrypted);
    } catch {
      localStorage.removeItem(`${STORAGE_PREFIX}token`);
      return;
    }

    // Extraer y guardar sessionId — si falla, NO borrar el token
    try {
      const sessionId = extractSessionId(token);
      if (sessionId) {
        this.setItem('sessionId', sessionId);
      }
    } catch {
      // Silencioso: el token sigue siendo válido
    }
  },

  getToken(): string | null {
    try {
      const encrypted = localStorage.getItem(`${STORAGE_PREFIX}token`);
      if (!encrypted) return null;
      const decrypted = decrypt(encrypted);
      const [token] = decrypted.split('::');
      return token || null;
    } catch {
      localStorage.removeItem(`${STORAGE_PREFIX}token`);
      return null;
    }
  },

  setUser(user: unknown): void {
    try {
      const encrypted = encrypt(JSON.stringify(user));
      localStorage.setItem(`${STORAGE_PREFIX}user`, encrypted);
    } catch {
      localStorage.removeItem(`${STORAGE_PREFIX}user`);
    }
  },

  getUser<T>(): T | null {
    try {
      const encrypted = localStorage.getItem(`${STORAGE_PREFIX}user`);
      if (!encrypted) return null;
      const decrypted = decrypt(encrypted);
      return JSON.parse(decrypted) as T;
    } catch {
      localStorage.removeItem(`${STORAGE_PREFIX}user`);
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
    localStorage.removeItem(`${STORAGE_PREFIX}token`);
    localStorage.removeItem(`${STORAGE_PREFIX}user`);
    localStorage.removeItem(`${STORAGE_PREFIX}sessionId`);
  },
};
