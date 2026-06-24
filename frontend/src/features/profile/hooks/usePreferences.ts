import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { notify } from "@/shared/utils/notifications/notify";
import { secureStorage } from '@/shared/utils/secureStorage';
import i18n from '@/lib/i18n';
import { profileService } from '../services/profileService';
import type { UserPreferences } from '../types';
import type { AxiosError } from 'axios';

export function usePreferences() {
  const queryClient = useQueryClient();

  const { data: preferences, isLoading, error } = useQuery({
    queryKey: ['preferences'],
    queryFn: profileService.getPreferences,
  });

  const updateMutation = useMutation({
    mutationFn: (data: Partial<UserPreferences>) => profileService.updatePreferences(data),
    onSuccess: (_data, variables) => {
      queryClient.invalidateQueries({ queryKey: ['preferences'] });
      const current = queryClient.getQueryData<UserPreferences>(['preferences']);
      const updated = { ...current, ...variables } as UserPreferences;
      secureStorage.setItem('userPreferences', JSON.stringify(updated));

      if (variables.theme) {
        applyTheme(variables.theme);
      }
      if (variables.language) {
        i18n.changeLanguage(variables.language);
      }

      notify.success('Preferencias guardadas');
    },
    onError: (error: AxiosError<{ message?: string }>) => {
      const message = error.response?.data?.message || 'Error al guardar preferencias';
      notify.error(message);
    },
  });

  return {
    preferences,
    isLoading,
    error,
    updatePreferences: updateMutation.mutate,
    isUpdating: updateMutation.isPending,
  };
}

function applyTheme(theme: 'light' | 'dark' | 'system') {
  const root = document.documentElement;
  if (theme === 'dark') {
    root.classList.add('dark');
  } else if (theme === 'light') {
    root.classList.remove('dark');
  } else {
    const systemDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    if (systemDark) {
      root.classList.add('dark');
    } else {
      root.classList.remove('dark');
    }
  }
}
