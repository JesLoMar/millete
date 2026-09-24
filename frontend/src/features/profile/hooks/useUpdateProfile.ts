import {
  useMutation,
  useQueryClient,
} from '@tanstack/react-query';
import axios from 'axios';
import { useTranslation } from 'react-i18next';

import { notify } from '@/shared/utils/notifications/notify';

import { profileService } from '../services/profileService';
import type { UpdateProfileRequest } from '../types';

export function useUpdateProfile() {
  const queryClient = useQueryClient();
  const { t } = useTranslation('userProfile');

  return useMutation({
    mutationFn: (
      data: UpdateProfileRequest,
    ) => profileService.updateProfile(data),

    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ['profile'],
      });

      notify.success(
        t('personalInfo.success'),
      );
    },

    onError: (error: unknown) => {
      const message = axios.isAxiosError(error)
        ? error.response?.data?.message ??
          t('personalInfo.error')
        : t('personalInfo.error');

      notify.error(message);
    },
  });
}