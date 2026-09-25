import {
  useMutation,
  useQueryClient,
} from '@tanstack/react-query';
import axios from 'axios';
import { useTranslation } from 'react-i18next';

import { notify } from '@/shared/utils/notifications/notify';

import { profileService } from '../services/profileService';
import type { ChangePasswordRequest } from '../types';

export function useChangePassword() {
  const { t } = useTranslation('userProfile');
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (data: ChangePasswordRequest) =>
      profileService.changePassword(data),

    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: ['sessions'],
      });

      notify.success(
        t('changePassword.success'),
      );
    },

    onError: (error: unknown) => {
      const message = axios.isAxiosError(error)
        ? error.response?.data?.message ??
          t('changePassword.error')
        : t('changePassword.error');

      notify.error(message);
    },
  });
}