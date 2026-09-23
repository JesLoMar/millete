import { useCallback } from 'react';

import { apiClient } from '@/shared/api/axiosClient';
import {
  useServerPagination,
  type PaginatedResponse,
} from '@/shared/hooks/useServerPagination';

import type { Category } from '../types';

const SERVER_SIZE = 50;
const DISPLAY_SIZE = 10;

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

  const normalizedSearch = search.trim();

  const fetchPage = useCallback(
    async (
      page: number,
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
        );

      return response.data;
    },
    [normalizedSearch],
  );

  return {
    ...useServerPagination<Category>({
      queryKey: ['categories', normalizedSearch],
      fetchPage,
      serverSize: SERVER_SIZE,
      displaySize: DISPLAY_SIZE,
      enabled,
    }),
    serverSize: SERVER_SIZE,
    displaySize: DISPLAY_SIZE,
  };
}