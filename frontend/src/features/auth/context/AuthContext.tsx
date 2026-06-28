/* eslint-disable react-refresh/only-export-components */
import { createContext, useState, useEffect, useCallback, use, useMemo } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/shared/api/axiosClient';
import { secureStorage } from '@/shared/utils/secureStorage';
import type { ReactNode } from 'react';

interface User {
  name: string;
  email: string;
}

interface AuthContextType {
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  user: User | null;
  sessionId: string | null;
  login: (token: string, userData?: User) => Promise<void>;
  logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [token, setToken] = useState<string | null>(null);
  const [user, setUser] = useState<User | null>(null);
  const [sessionId, setSessionId] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const queryClient = useQueryClient();

  // ─── LOGOUT (usado por init, interceptor 401, y LogoutListener) ───
  const logout = useCallback(async () => {
    try {
      await apiClient.post('/auth/logout');
    } catch {
      // Ignorar errores de red en logout
    } finally {
      setToken(null);
      setUser(null);
      setSessionId(null);
      secureStorage.clear();
      queryClient.clear();
    }
  }, [queryClient]);

  // ─── INICIALIZACIÓN ──────────────────────────────────────────
  useEffect(() => {
    const initAuth = async () => {
      const storedToken = secureStorage.getToken();
      const storedUser = secureStorage.getUser<User>();

      if (storedToken) {
        setToken(storedToken);
        setSessionId(secureStorage.getSessionId());

        if (!storedUser) {
          try {
            const response = await apiClient.get('/auth/me/topnav');
            const userData = response.data;
            const formattedUser: User = {
              name: userData.username || userData.email?.split('@')[0] || 'Usuario',
              email: userData.email || '',
            };
            setUser(formattedUser);
            secureStorage.setUser(formattedUser);
          } catch {
            // Token inválido → limpieza total
            await logout();
          }
        } else {
          setUser(storedUser);
        }
      }

      setIsLoading(false);
    };

    initAuth();

    // Escuchar evento de logout forzado desde el interceptor de Axios
    const handleForcedLogout = () => logout();
    window.addEventListener('auth:logout', handleForcedLogout);
    return () => window.removeEventListener('auth:logout', handleForcedLogout);
  }, [logout]);

  // ─── LOGIN ASÍNCRONO ─────────────────────────────────────────
  const login = useCallback(
    async (newToken: string, userData?: User): Promise<void> => {
      setToken(newToken);
      secureStorage.setToken(newToken);
      setSessionId(secureStorage.getSessionId());

      if (userData) {
        setUser(userData);
        secureStorage.setUser(userData);
      } else {
        try {
          const response = await apiClient.get('/auth/me/topnav');
          const data = response.data;
          const fetchedUser: User = {
            name: data.username || data.email?.split('@')[0] || 'Usuario',
            email: data.email || '',
          };
          setUser(fetchedUser);
          secureStorage.setUser(fetchedUser);
        } catch {
          await logout();
          throw new Error('Fallo al obtener perfil tras login');
        }
      }
    },
    [logout],
  );

  const value = useMemo(() => ({
    token,
    isAuthenticated: !!token,
    isLoading,
    user,
    sessionId,
    login,
    logout,
  }), [token, isLoading, user, sessionId, login, logout]);

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
