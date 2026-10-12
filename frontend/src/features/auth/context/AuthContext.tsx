import {
  createContext,
  use,
  useCallback,
  useEffect,
  useEffectEvent,
  useMemo,
  useRef,
  useState,
} from 'react';
import { useQueryClient } from '@tanstack/react-query';
import axios from 'axios';
import type { ReactNode } from 'react';

import { apiClient, bumpAuthEpoch } from '@/shared/api/axiosClient';
import { sessionCache } from '@/shared/utils/sessionCache';

interface User {
  name: string;
  email: string;
}

interface CurrentUserResponse {
  username?: string | null;
  email?: string | null;
  sessionId?: string | null;
}

interface AuthContextType {
  isAuthenticated: boolean;
  isLoading: boolean;
  isOffline: boolean;
  user: User | null;
  sessionId: string | null;
  login: (userData?: CurrentUserResponse) => Promise<void>;
  logout: () => Promise<void>;
  waitForPendingLogout: () => Promise<void>;
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
  const authAttemptRef = useRef(0);
  const logoutPromiseRef = useRef<Promise<void> | null>(null);
  const [user, setUser] = useState<User | null>(null);
  const [sessionId, setSessionId] =
    useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isOffline, setIsOffline] = useState(false);

  const queryClient = useQueryClient();

  const waitForPendingLogout = useCallback(async () => {
    const pending = logoutPromiseRef.current;

    if (pending) {
      await pending;
    }
  }, []);

  const fetchCurrentUser = useCallback(
    async (): Promise<FetchUserResult> => {
      try {
        const response =
          await apiClient.get<CurrentUserResponse>(
            '/auth/me/topnav',
            {
              timeout: 6000,
              skipGlobalErrorNotify: true,
              skipAuthErrorHandler: true,
            },
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
    (result: Extract<FetchUserResult, { status: 'ok' }>) => {
      bumpAuthEpoch();

      setUser(result.user);
      setSessionId(result.sessionId);
      setIsOffline(false);
      setIsLoading(false);

      sessionCache.setUser(result.user);
      sessionCache.setSessionId(result.sessionId);
    },
    [],
  );

  const clearAuthenticatedState = useCallback(() => {
    bumpAuthEpoch();

    setUser(null);
    setSessionId(null);
    setIsOffline(false);
    setIsLoading(false);
    sessionCache.clear();
  }, []);

  const logout = useCallback(async () => {
    // Si ya existe un logout, todas las llamadas comparten la promesa.
    if (logoutPromiseRef.current) {
      await logoutPromiseRef.current;
      return;
    }

    // Invalida las comprobaciones de sesión que sigan en vuelo.
    authAttemptRef.current += 1;

    // La UI se limpia inmediatamente, sin esperar a la red.
    clearAuthenticatedState();
    queryClient.clear();

    const request: Promise<void> = apiClient
      .post(
        '/auth/logout',
        undefined,
        {
          timeout: 3000,
          skipGlobalErrorNotify: true,
          skipAuthErrorHandler: true,
        },
      )
      .then(() => undefined, () => undefined)
      .finally(() => {
        if (logoutPromiseRef.current === request) {
          logoutPromiseRef.current = null;
        }
      });

    logoutPromiseRef.current = request;

    await request;
  }, [clearAuthenticatedState, queryClient]);

  const initAuth = useCallback(async () => {
    const attempt = ++authAttemptRef.current;

    setIsLoading(true);
    setIsOffline(false);

    try {
      const result = await fetchCurrentUser();

      if (attempt !== authAttemptRef.current) {
        return;
      }

      if (result.status === 'ok') {
        applyAuthenticatedState(result);
      } else if (result.status === 'unauthenticated') {
        clearAuthenticatedState();
      } else {
        setIsOffline(true);
      }
    } finally {
      if (attempt === authAttemptRef.current) {
        setIsLoading(false);
      }
    }
  }, [
    applyAuthenticatedState,
    clearAuthenticatedState,
    fetchCurrentUser,
  ]);

  const handleForcedLogout = useEffectEvent(() => {
    void logout();
  });

  useEffect(() => {
    void initAuth();

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
  }, [initAuth]);

  const retryAuth = useCallback(() => {
    void initAuth();
  }, [initAuth]);

  const login = useCallback(
    async (userData?: CurrentUserResponse): Promise<void> => {
      const attempt = ++authAttemptRef.current;

      setIsLoading(false);

      if (userData?.sessionId) {
        applyAuthenticatedState({
          status: 'ok',
          user: formatUser(userData),
          sessionId: userData.sessionId,
        });

        return;
      }
      let result = await fetchCurrentUser();

      if (result.status === 'unavailable') {
        result = await fetchCurrentUser();
      }

      if (attempt !== authAttemptRef.current) {
        throw new Error('AUTH_STATE_CHANGED');
      }

      if (result.status === 'ok') {
        applyAuthenticatedState(result);
        return;
      }

      if (result.status === 'unauthenticated') {
        clearAuthenticatedState();
        throw new Error('AUTH_SESSION_UNAUTHENTICATED');
      }
      setIsOffline(true);
      throw new Error('AUTH_PROFILE_UNAVAILABLE');
    },
    [
      applyAuthenticatedState,
      clearAuthenticatedState,
      fetchCurrentUser,
    ],
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
      waitForPendingLogout,
      retryAuth,
    }),
    [
      isLoading,
      isOffline,
      user,
      sessionId,
      login,
      logout,
      waitForPendingLogout,
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