/* eslint-disable react-refresh/only-export-components */
import { createContext, useState, useEffect, useCallback, useContext } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { apiClient } from '@/shared/api/axiosClient';
import { secureStorage } from '@/shared/utils/secureStorage';
import type { ReactNode } from 'react';

interface User {
  name: string;
  email: string;
  avatar?: string;
  role?: string;
}

interface AuthContextType {
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  user: User | null;
  login: (token: string, userData?: User) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
  const [token, setToken] = useState<string | null>(null);
  const [user, setUser] = useState<User | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const queryClient = useQueryClient();

  // ─── LOGOUT (usado por init, interceptor 401, y LogoutListener) ───
  const logout = useCallback(() => {
    setToken(null);
    setUser(null);
    secureStorage.clear();
    queryClient.clear();
  }, [queryClient]);

  // ─── INICIALIZACIÓN ──────────────────────────────────────────
  useEffect(() => {
    const initAuth = async () => {
      const storedToken = secureStorage.getToken();
      const storedUser = secureStorage.getUser<User>();

      if (storedToken) {
        setToken(storedToken);

        if (!storedUser) {
          try {
            const response = await apiClient.get('/auth/me/topnav');
            const userData = response.data;
            const formattedUser: User = {
              name: userData.username || userData.email?.split('@')[0] || 'Usuario',
              email: userData.email || '',
              role: userData.role,
            };
            setUser(formattedUser);
            secureStorage.setUser(formattedUser);
          } catch {
            // Token inválido → limpieza total
            logout();
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
            role: data.role,
          };
          setUser(fetchedUser);
          secureStorage.setUser(fetchedUser);
        } catch {
          logout();
          throw new Error('Fallo al obtener perfil tras login');
        }
      }
    },
    [logout],
  );

  return (
    <AuthContext.Provider
      value={{
        token,
        isAuthenticated: !!token,
        isLoading,
        user,
        login,
        logout,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth debe usarse dentro de un AuthProvider');
  return context;
};