import { useQuery } from '@tanstack/react-query';
import { profileService } from '../services/profileService';

export function useProfile() {
  const { data: profile, isLoading, error } = useQuery({
    queryKey: ['profile'],
    queryFn: profileService.getProfile,
    staleTime: 5 * 60 * 1000,
  });

  return { profile, isLoading, error };
}
