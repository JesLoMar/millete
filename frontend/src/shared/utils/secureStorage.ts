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

export const secureStorage = {
  setToken(token: string): void {
    try {
      const key = deriveFingerprint();
      const payload = `${token}::${Date.now()}`;
      const encrypted = btoa(xorTransform(payload, key));
      localStorage.setItem(`${STORAGE_PREFIX}token`, encrypted);
    } catch {
      localStorage.removeItem(`${STORAGE_PREFIX}token`);
    }
  },

  getToken(): string | null {
    try {
      const encrypted = localStorage.getItem(`${STORAGE_PREFIX}token`);
      if (!encrypted) return null;
      const key = deriveFingerprint();
      const decrypted = xorTransform(atob(encrypted), key);
      const [token] = decrypted.split('::');
      return token || null;
    } catch {
      localStorage.removeItem(`${STORAGE_PREFIX}token`);
      return null;
    }
  },

  setUser(user: unknown): void {
    try {
      const key = deriveFingerprint();
      const encrypted = btoa(xorTransform(JSON.stringify(user), key));
      localStorage.setItem(`${STORAGE_PREFIX}user`, encrypted);
    } catch {
      localStorage.removeItem(`${STORAGE_PREFIX}user`);
    }
  },

  getUser<T>(): T | null {
    try {
      const encrypted = localStorage.getItem(`${STORAGE_PREFIX}user`);
      if (!encrypted) return null;
      const key = deriveFingerprint();
      const decrypted = xorTransform(atob(encrypted), key);
      return JSON.parse(decrypted) as T;
    } catch {
      localStorage.removeItem(`${STORAGE_PREFIX}user`);
      return null;
    }
  },

  clear(): void {
    localStorage.removeItem(`${STORAGE_PREFIX}token`);
    localStorage.removeItem(`${STORAGE_PREFIX}user`);
  },
};