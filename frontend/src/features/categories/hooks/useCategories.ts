import {
  useCallback,
  useMemo,
} from 'react';

import { apiClient } from '@/shared/api/axiosClient';
import {
  useServerPagination,
  type PaginatedResponse,
} from '@/shared/hooks/useServerPagination';

import type { Category } from '../types';
import { useDebouncedValue } from './useDebouncedValue';

const SERVER_SIZE = 50;
const DISPLAY_SIZE = 10;
const SEARCH_DEBOUNCE_MS = 300;

interface UseCategoriesOptions {
  search?: string;
  enabled?: boolean;
}

export function useCategories(
  options: UseCategoriesOptions = {},
) {
  const {
    search = '',
    enabled = true,
  } = options;

  // One request per pause in typing, not one per keystroke.
  const normalizedSearch = useDebouncedValue(
    search.trim(),
    SEARCH_DEBOUNCE_MS,
  );

  const categoryQueryKey = useMemo(
    () => ['categories', normalizedSearch] as const,
    [normalizedSearch],
  );

  const fetchPage = useCallback(
    async (
      page: number,
      signal?: AbortSignal,
    ): Promise<PaginatedResponse<Category>> => {
      const params = new URLSearchParams({
        page: String(page),
        size: String(SERVER_SIZE),
      });

      if (normalizedSearch) {
        params.set('search', normalizedSearch);
      }

      const response =
        await apiClient.get<PaginatedResponse<Category>>(
          `/categories?${params.toString()}`,
          { signal },
        );

      return response.data;
    },
    [normalizedSearch],
  );

  return {
    ...useServerPagination<Category>({
      queryKey: categoryQueryKey,
      fetchPage,
      serverSize: SERVER_SIZE,
      displaySize: DISPLAY_SIZE,
      enabled,
    }),
    serverSize: SERVER_SIZE,
    displaySize: DISPLAY_SIZE,
  };
}
