import { useQuery } from '@tanstack/react-query';

import { profileService } from '../services/profileService';
import type { ProfileResponse } from '../types';

export function useProfile() {
  const {
    data: profile,
    isLoading,
    error,
  } = useQuery<ProfileResponse>({
    queryKey: ['profile'],
    queryFn: profileService.getProfile,
    staleTime: 5 * 60 * 1000,
  });

  return {
    profile,
    isLoading,
    error,
  };
}