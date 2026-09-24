import {
  createContext,
  use,
  useCallback,
  useEffect,
  useEffectEvent,
  useMemo,
  useState,
} from 'react';
import { useQueryClient } from '@tanstack/react-query';
import axios from 'axios';
import type { ReactNode } from 'react';

import { apiClient } from '@/shared/api/axiosClient';
import { sessionCache } from '@/shared/utils/sessionCache';

interface User {
  name: string;
  email: string;
}

interface CurrentUserResponse {
  username?: string;
  email?: string;
  sessionId?: string;
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

type FetchUserResult =
  | {
      status: 'ok';
      user: User;
      sessionId: string;
    }
  | {
      status: 'unauthenticated';
    }
  | {
      status: 'unavailable';
    };

const AuthContext = createContext<AuthContextType | null>(null);

const formatUser = (
  userData: CurrentUserResponse,
): User => ({
  name:
    userData.username ||
    userData.email?.split('@')[0] ||
    'Usuario',
  email: userData.email || '',
});

export const AuthProvider = ({
  children,
}: {
  children: ReactNode;
}) => {
  const [user, setUser] = useState<User | null>(null);
  const [sessionId, setSessionId] =
    useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isOffline, setIsOffline] = useState(false);

  const queryClient = useQueryClient();

  const fetchCurrentUser = useCallback(
    async (): Promise<FetchUserResult> => {
      try {
        const response =
          await apiClient.get<CurrentUserResponse>(
            '/auth/me/topnav',
          );

        const userData = response.data;
        const formattedUser = formatUser(userData);
        const currentSessionId =
          userData.sessionId ?? '';

        return {
          status: 'ok',
          user: formattedUser,
          sessionId: currentSessionId,
        };
      } catch (error) {
        if (
          axios.isAxiosError(error) &&
          error.response?.status === 401
        ) {
          return {
            status: 'unauthenticated',
          };
        }

        return {
          status: 'unavailable',
        };
      }
    },
    [],
  );

  const applyAuthenticatedState = useCallback(
    (
      result: Extract<
        FetchUserResult,
        { status: 'ok' }
      >,
    ) => {
      setUser(result.user);
      setSessionId(result.sessionId);
      setIsOffline(false);

      sessionCache.setUser(result.user);
      sessionCache.setSessionId(result.sessionId);
    },
    [],
  );

  const clearAuthenticatedState = useCallback(() => {
    setUser(null);
    setSessionId(null);
    setIsOffline(false);
    sessionCache.clear();
  }, []);

  const logout = useCallback(async () => {
    try {
      await apiClient.post(
        '/auth/logout',
        undefined,
        {
          skipGlobalErrorNotify: true,
        },
      );
    } catch {
      // El estado local debe limpiarse aunque
      // el logout remoto falle.
    } finally {
      clearAuthenticatedState();
      queryClient.clear();
    }
  }, [clearAuthenticatedState, queryClient]);

  const onLogout = useEffectEvent(logout);

  const initAuth = useCallback(async () => {
    setIsLoading(true);

    const result = await fetchCurrentUser();

    if (result.status === 'ok') {
      applyAuthenticatedState(result);
    } else if (result.status === 'unauthenticated') {
      clearAuthenticatedState();
    } else {
      setUser(null);
      setSessionId(null);
      setIsOffline(true);
    }

    setIsLoading(false);
  }, [
    applyAuthenticatedState,
    clearAuthenticatedState,
    fetchCurrentUser,
  ]);

  useEffect(() => {
    void initAuth();

    const handleForcedLogout = () => {
      onLogout();
    };

    window.addEventListener(
      'auth:logout',
      handleForcedLogout,
    );

    return () => {
      window.removeEventListener(
        'auth:logout',
        handleForcedLogout,
      );
    };
  }, [initAuth, onLogout]);

  const retryAuth = useCallback(() => {
    void initAuth();
  }, [initAuth]);

  const login = useCallback(
    async (userData?: User): Promise<void> => {
      if (userData) {
        setUser(userData);
        sessionCache.setUser(userData);
      }

      const result = await fetchCurrentUser();

      if (result.status === 'ok') {
        applyAuthenticatedState(result);
        return;
      }

      await logout();

      throw new Error(
        'Fallo al obtener perfil tras login',
      );
    },
    [applyAuthenticatedState, fetchCurrentUser, logout],
  );

  const value = useMemo<AuthContextType>(
    () => ({
      isAuthenticated: !!user,
      isLoading,
      isOffline,
      user,
      sessionId,
      login,
      logout,
      retryAuth,
    }),
    [
      isLoading,
      isOffline,
      user,
      sessionId,
      login,
      logout,
      retryAuth,
    ],
  );

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = use(AuthContext);

  if (!context) {
    throw new Error(
      'useAuth debe usarse dentro de un AuthProvider',
    );
  }

  return context;
};