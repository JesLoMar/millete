import {
  useEffect,
  useMemo,
  useState,
} from 'react';
import {
  useQuery,
  useQueryClient,
} from '@tanstack/react-query';

export interface PaginatedResponse<T> {
  content: T[];
  currentPage: number;
  totalPages: number;
  totalElements: number;
  size: number;
  first: boolean;
  last: boolean;
}

interface UseServerPaginationOptions<T> {
  queryKey: readonly string[];
  fetchPage: (
    page: number,
  ) => Promise<PaginatedResponse<T>>;
  serverSize: number;
  displaySize: number;
  enabled?: boolean;
}

export function useServerPagination<T>({
  queryKey,
  fetchPage,
  serverSize,
  displaySize,
  enabled = true,
}: UseServerPaginationOptions<T>) {
  const queryClient = useQueryClient();

  const [displayPage, setDisplayPageState] =
    useState(0);

  const serverPage = Math.floor(
    (displayPage * displaySize) /
      serverSize,
  );

  const offsetInChunk =
    (displayPage * displaySize) %
    serverSize;

  const serverQueryKey = [
    ...queryKey,
    String(serverPage),
  ];

  const {
    data,
    isLoading,
    isFetching,
    error,
    refetch,
  } = useQuery<PaginatedResponse<T>>({
    queryKey: serverQueryKey,
    queryFn: () => fetchPage(serverPage),
    staleTime: 30_000,
    placeholderData: (previousData) => previousData,
    enabled,
  });

  const displayItems = useMemo(() => {
    if (!data) {
      return [];
    }

    return data.content.slice(
      offsetInChunk,
      offsetInChunk + displaySize,
    );
  }, [data, offsetInChunk, displaySize]);

  const totalElements =
    data?.totalElements ?? 0;

  const totalDisplayPages = useMemo(
    () =>
      Math.max(
        1,
        Math.ceil(
          totalElements / displaySize,
        ),
      ),
    [totalElements, displaySize],
  );

  const setDisplayPage = (page: number) => {
    setDisplayPageState(
      Math.max(
        0,
        Math.min(
          page,
          totalDisplayPages - 1,
        ),
      ),
    );
  };

  const nextPage = () => {
    setDisplayPage(displayPage + 1);
  };

  const prevPage = () => {
    setDisplayPage(displayPage - 1);
  };

  useEffect(() => {
    if (
      displayPage > 0 &&
      displayPage >= totalDisplayPages
    ) {
      setDisplayPageState(
        totalDisplayPages - 1,
      );
    }
  }, [displayPage, totalDisplayPages]);

  const isLastPageOfChunk =
    ((displayPage + 1) * displaySize) %
      serverSize ===
    0;

  const hasMoreChunks =
    data !== undefined &&
    (serverPage + 1) * serverSize <
      data.totalElements;

  useEffect(() => {
    if (
      !data ||
      isFetching ||
      !isLastPageOfChunk ||
      !hasMoreChunks
    ) {
      return;
    }

    const nextServerPage =
      serverPage + 1;

    queryClient.prefetchQuery({
      queryKey: [
        ...queryKey,
        String(nextServerPage),
      ],
      queryFn: () =>
        fetchPage(nextServerPage),
      staleTime: 30_000,
    });
  }, [
    data,
    isFetching,
    isLastPageOfChunk,
    hasMoreChunks,
    serverPage,
    queryKey,
    fetchPage,
    queryClient,
  ]);

  return {
    displayItems,
    displayPage,
    setDisplayPage,
    nextPage,
    prevPage,
    serverPage,
    offsetInChunk,
    totalDisplayPages,
    totalElements,
    isLoading,
    isFetching,
    error,
    refetch,
  };
}