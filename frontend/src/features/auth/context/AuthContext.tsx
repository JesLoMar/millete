import { createContext, useState, useEffect, useCallback, use, useMemo, useEffectEvent } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import axios from 'axios';
import { apiClient } from '@/shared/api/axiosClient';
import { secureStorage } from '@/shared/utils/secureStorage';
import type { ReactNode } from 'react';

interface User {
  name: string;
  email: string;
}

interface AuthContextType {
  isAuthenticated: boolean;
  isLoading: boolean;
  isOffline: boolean;
  user: User | null;
  sessionId: string | null;
  login: (userData?: User) => Promise<void>;
  logout: () => Promise<void>;
  retryAuth: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

type FetchUserResult =
  | { status: 'ok'; user: User; sessionId: string }
  | { status: 'unauthenticated' }
  | { status: 'network-error' };

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isOffline, setIsOffline] = useState(false);
  const queryClient = useQueryClient();

  const fetchCurrentUser = useCallback(async (): Promise<FetchUserResult> => {
    try {
      const response = await apiClient.get('/auth/me/topnav', {
        skipGlobalErrorNotify: true,
        skipAuthErrorHandler: true,
      });
      const userData = response.data;
      const formattedUser: User = {
        name: userData.username || userData.email?.split('@')[0] || 'Usuario',
        email: userData.email || '',
      };
      const currentSessionId = userData.sessionId ?? '';
      return { status: 'ok', user: formattedUser, sessionId: currentSessionId };
    } catch (error) {
      // 401 = no autenticado; cualquier otro fallo (red caída, 5xx) se trata aparte.
      if (axios.isAxiosError(error) && error.response?.status === 401) {
        return { status: 'unauthenticated' };
      }
      return { status: 'network-error' };
    }
  }, []);

  const logout = useCallback(async () => {
    try {
      await apiClient.post('/auth/logout');
    } catch {
    } finally {
      setUser(null);
      setSessionId(null);
      setIsOffline(false);
      secureStorage.clear();
      queryClient.clear();
    }
  }, [queryClient]);

  const onLogout = useEffectEvent(logout);

  const initAuth = useCallback(async () => {
    setIsLoading(true);
    const result = await fetchCurrentUser();
    if (result.status === 'ok') {
      setUser(result.user);
      setSessionId(result.sessionId);
      secureStorage.setUser(result.user);
      secureStorage.setSessionId(result.sessionId);
      setIsOffline(false);
    } else if (result.status === 'unauthenticated') {
      // Sesión inválida: limpiamos cualquier resto cacheado.
      secureStorage.clear();
      setUser(null);
      setSessionId(null);
      setIsOffline(false);
    } else {
      // Error de red: NO autenticamos con datos cacheados (sesión zombie).
      // El usuario verá la pantalla de "sin conexión" con opción de reintentar.
      setUser(null);
      setSessionId(null);
      setIsOffline(true);
    }
    setIsLoading(false);
  }, [fetchCurrentUser]);

  useEffect(() => {
    initAuth();
    const handleForcedLogout = () => onLogout();
    window.addEventListener('auth:logout', handleForcedLogout);
    return () => window.removeEventListener('auth:logout', handleForcedLogout);
  }, [initAuth]);

  const retryAuth = useCallback(() => {
    void initAuth();
  }, [initAuth]);

  const login = useCallback(
    async (userData?: User): Promise<void> => {
      if (userData) {
        setUser(userData);
        secureStorage.setUser(userData);
      }
      const result = await fetchCurrentUser();
      if (result.status === 'ok') {
        setUser(result.user);
        setSessionId(result.sessionId);
        secureStorage.setUser(result.user);
        secureStorage.setSessionId(result.sessionId);
        setIsOffline(false);
      } else {
        await logout();
        throw new Error('Fallo al obtener perfil tras login');
      }
    },
    [fetchCurrentUser, logout],
  );

  const value = useMemo(() => ({
    isAuthenticated: !!user,
    isLoading,
    isOffline,
    user,
    sessionId,
    login,
    logout,
    retryAuth,
  }), [isLoading, isOffline, user, sessionId, login, logout, retryAuth]);

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = use(AuthContext);
  if (!context) throw new Error('useAuth debe usarse dentro de un AuthProvider');
  return context;
};