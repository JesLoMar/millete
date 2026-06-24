import { useQuery } from '@tanstack/react-query';
import { profileService } from '../services/profileService';

export function useProfile() {
  const { data: profile, isLoading, error } = useQuery({
    queryKey: ['profile'],
    queryFn: profileService.getProfile,
  });

  return { profile, isLoading, error };
}
