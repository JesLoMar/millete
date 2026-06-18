import { useQuery } from '@tanstack/react-query';
import { settingsService } from './settings.service';
import type { TelegramStatusResponse, TelegramConnectionStatus } from '../types';

export interface UseTelegramStatusResult {
  status: TelegramConnectionStatus;
  username?: string;
  connectedAt?: string;
  refetch: () => void;
  isRefetching: boolean;
}

export function useTelegramStatus(): UseTelegramStatusResult {
  const { data, refetch, isRefetching, isLoading, isError } = useQuery<TelegramStatusResponse>({
    queryKey: ['telegram-status'],
    queryFn: settingsService.getTelegramStatus,
  });

  const status: TelegramConnectionStatus = isLoading
    ? 'loading'
    : isError
    ? 'error'
    : data?.connected
    ? 'connected'
    : 'disconnected';

  return {
    status,
    username: data?.username,
    connectedAt: data?.connectedAt,
    refetch,
    isRefetching,
  };
}
