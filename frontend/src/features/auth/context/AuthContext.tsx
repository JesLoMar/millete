/* eslint-disable react-refresh/only-export-components */
import { createContext, useState, useEffect, useCallback, use, useMemo, useEffectEvent } from 'react';
import { useQueryClient } from '@tanstack/react-query';
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
  user: User | null;
  sessionId: string | null;
  login: (userData?: User) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const queryClient = useQueryClient();

  const fetchCurrentUser = useCallback(async (): Promise<{ user: User; sessionId: string } | null> => {
    try {
      const response = await apiClient.get('/auth/me/topnav', { skipGlobalErrorNotify: true });
      const userData = response.data;
      const formattedUser: User = {
        name: userData.username || userData.email?.split('@')[0] || 'Usuario',
        email: userData.email || '',
      };
      const currentSessionId = userData.sessionId ?? '';
      return { user: formattedUser, sessionId: currentSessionId };
    } catch {
      return null;
    }
  }, []);

  // ─── LOGOUT (usado por init, interceptor 401, y LogoutListener) ───
  const logout = useCallback(async () => {
    try {
      await apiClient.post('/auth/logout');
    } catch {
      // Ignorar errores de red en logout
    } finally {
      setUser(null);
      setSessionId(null);
      secureStorage.clear();
      queryClient.clear();
    }
  }, [queryClient]);

  // Effect Event: siempre ve el logout más reciente sin ser dependencia reactiva
  const onLogout = useEffectEvent(logout);

  // ─── INICIALIZACIÓN ──────────────────────────────────────────
  useEffect(() => {
    const initAuth = async () => {
      const storedUser = secureStorage.getUser<User>();
      const storedSessionId = secureStorage.getSessionId();

      const current = await fetchCurrentUser();

      if (current) {
        setUser(current.user);
        setSessionId(current.sessionId);
        secureStorage.setUser(current.user);
        secureStorage.setSessionId(current.sessionId);
      } else if (storedUser) {
        // La cookie ya no es válida, pero mantenemos el usuario en memoria
        // hasta que el siguiente request 401 fuerce el logout.
        setUser(storedUser);
        setSessionId(storedSessionId);
      }

      setIsLoading(false);
    };

    initAuth();

    // Escuchar evento de logout forzado desde el interceptor de Axios
    const handleForcedLogout = () => onLogout();
    window.addEventListener('auth:logout', handleForcedLogout);
    return () => window.removeEventListener('auth:logout', handleForcedLogout);
  }, [fetchCurrentUser]);

  // ─── LOGIN ASÍNCRONO ─────────────────────────────────────────
  const login = useCallback(
    async (userData?: User): Promise<void> => {
      if (userData) {
        setUser(userData);
        secureStorage.setUser(userData);
      }

      const current = await fetchCurrentUser();
      if (current) {
        setUser(current.user);
        setSessionId(current.sessionId);
        secureStorage.setUser(current.user);
        secureStorage.setSessionId(current.sessionId);
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
    user,
    sessionId,
    login,
    logout,
  }), [isLoading, user, sessionId, login, logout]);

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
