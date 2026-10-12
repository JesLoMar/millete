import type { QueryClient } from '@tanstack/react-query';

import { notify } from '@/shared/utils/notifications/notify';
import { sessionCache } from '@/shared/utils/sessionCache';
import type { LoginResponse } from '../types';

interface HandlePostAuthSuccessParams {
  login: (userData: LoginResponse) => Promise<void>;
  loginData: LoginResponse;
  queryClient: QueryClient;
  successMessage: string;
}

export const handlePostAuthSuccess = async ({
  login,
  loginData,
  queryClient,
  successMessage,
}: HandlePostAuthSuccessParams): Promise<void> => {
  sessionCache.removeItem('userPreferences');

  await login(loginData);

  queryClient.clear();

  notify.success(successMessage);
};