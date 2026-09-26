import type { NavigateFunction } from 'react-router-dom';
import type { QueryClient } from '@tanstack/react-query';

import { notify } from '@/shared/utils/notifications/notify';
import { sessionCache } from '@/shared/utils/sessionCache';

interface HandlePostAuthSuccessParams {
  login: () => Promise<void>;
  queryClient: QueryClient;
  navigate: NavigateFunction;
  successMessage: string;
  destination: string;
}

export const handlePostAuthSuccess = async ({
  login,
  queryClient,
  navigate,
  successMessage,
  destination,
}: HandlePostAuthSuccessParams): Promise<void> => {
  sessionCache.removeItem('userPreferences');
  await login();
  queryClient.clear();

  notify.success(successMessage);
  navigate(destination, {
    replace: true,
  });
};
