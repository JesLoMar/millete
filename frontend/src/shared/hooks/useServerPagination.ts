import { useEffect, useMemo, useRef, useState } from 'react';
import { useQuery, useQueryClient } from '@tanstack/react-query';

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
    signal?: AbortSignal,
  ) => Promise<PaginatedResponse<T>>;
  serverSize: number;
  displaySize: number;
  enabled?: boolean;
}

interface DisplayPageState {
  queryIdentity: string;
  page: number;
}

const STALE_TIME = 30_000;

const lastPageIndex = (
  totalElements: number,
  displaySize: number,
) => Math.max(0, Math.ceil(totalElements / displaySize) - 1);

function assertValidSizes(serverSize: number, displaySize: number) {
  if (
    !Number.isInteger(serverSize) ||
    !Number.isInteger(displaySize) ||
    serverSize < 1 ||
    displaySize < 1 ||
    displaySize > serverSize
  ) {
    throw new Error(
      `useServerPagination: tamaños inválidos (serverSize=${serverSize}, displaySize=${displaySize}). ` +
        'Ambos deben ser enteros positivos y displaySize <= serverSize.',
    );
  }
}

export function useServerPagination<T>({
  queryKey,
  fetchPage,
  serverSize,
  displaySize,
  enabled = true,
}: UseServerPaginationOptions<T>) {
  assertValidSizes(serverSize, displaySize);

  const queryClient = useQueryClient();

  const queryIdentity = JSON.stringify(queryKey);
  const baseKey = useMemo(
    () => JSON.parse(queryIdentity) as string[],
    [queryIdentity],
  );

  const fetchPageRef = useRef(fetchPage);
  useEffect(() => {
    fetchPageRef.current = fetchPage;
  }, [fetchPage]);

  const [pageState, setPageState] = useState<DisplayPageState>(
    () => ({ queryIdentity, page: 0 }),
  );

  if (pageState.queryIdentity !== queryIdentity) {
    setPageState({ queryIdentity, page: 0 });
  }

  const requestedPage =
    pageState.queryIdentity === queryIdentity ? pageState.page : 0;

  const startIndex = requestedPage * displaySize;
  const firstChunk = Math.floor(startIndex / serverSize);
  const lastChunk = Math.floor(
    (startIndex + displaySize - 1) / serverSize,
  );
  const offsetInChunk = startIndex - firstChunk * serverSize;

  const primary = useQuery<PaginatedResponse<T>>({
    queryKey: [...baseKey, String(firstChunk)],
    queryFn: ({ signal }) => fetchPage(firstChunk, signal),
    staleTime: STALE_TIME,
    placeholderData: (previousData) => previousData,
    enabled,
  });

  const primaryData = primary.isPlaceholderData
    ? undefined
    : primary.data;

  const needsTail =
    primaryData !== undefined &&
    lastChunk > firstChunk &&
    lastChunk * serverSize < primaryData.totalElements;

  const tail = useQuery<PaginatedResponse<T>>({
    queryKey: [...baseKey, String(lastChunk)],
    queryFn: ({ signal }) => fetchPage(lastChunk, signal),
    staleTime: STALE_TIME,
    enabled: enabled && needsTail,
  });

  const tailData = needsTail ? tail.data : undefined;

  const maxPage =
    primaryData !== undefined
      ? lastPageIndex(primaryData.totalElements, displaySize)
      : 0;

  const isOutOfRange =
    primaryData !== undefined && requestedPage > maxPage;

  if (isOutOfRange) {
    setPageState({ queryIdentity, page: maxPage });
  }

  const isReady =
    primaryData !== undefined &&
    !isOutOfRange &&
    (!needsTail || tailData !== undefined);

  const freshItems = useMemo(() => {
    if (!isReady || primaryData === undefined) {
      return undefined;
    }

    const merged = tailData
      ? [...primaryData.content, ...tailData.content]
      : primaryData.content;

    return merged.slice(offsetInChunk, offsetInChunk + displaySize);
  }, [isReady, primaryData, tailData, offsetInChunk, displaySize]);

  const lastItemsRef = useRef<T[]>([]);
  if (freshItems !== undefined) {
    lastItemsRef.current = freshItems;
  }
  const displayItems = freshItems ?? lastItemsRef.current;

  const totalElements = primary.data?.totalElements ?? 0;
  const totalDisplayPages = Math.max(
    1,
    Math.ceil(totalElements / displaySize),
  );

  const goToPage = (resolve: (current: number) => number) => {
    setPageState((previous) => {
      const current =
        previous.queryIdentity === queryIdentity ? previous.page : 0;
      const requested = Math.trunc(resolve(current));
      const next = Math.min(
        Math.max(0, Number.isFinite(requested) ? requested : 0),
        totalDisplayPages - 1,
      );

      if (
        previous.queryIdentity === queryIdentity &&
        previous.page === next
      ) {
        return previous;
      }

      return { queryIdentity, page: next };
    });
  };

  const setDisplayPage = (page: number) => goToPage(() => page);
  const nextPage = () => goToPage((current) => current + 1);
  const prevPage = () => goToPage((current) => current - 1);

  const nextChunk = (needsTail ? lastChunk : firstChunk) + 1;
  const shouldPrefetch =
    isReady &&
    primaryData !== undefined &&
    nextChunk * serverSize < primaryData.totalElements &&
    (lastChunk > firstChunk || offsetInChunk >= serverSize / 2);

  useEffect(() => {
    if (!shouldPrefetch) {
      return;
    }

    void queryClient.prefetchQuery({
      queryKey: [...baseKey, String(nextChunk)],
      queryFn: ({ signal }) =>
        fetchPageRef.current(nextChunk, signal),
      staleTime: STALE_TIME,
    });
  }, [shouldPrefetch, nextChunk, baseKey, queryClient]);

  const isError = primary.isError || (needsTail && tail.isError);

  const refetch = async () => {
    await Promise.all([
      primary.refetch(),
      needsTail ? tail.refetch() : Promise.resolve(),
    ]);
  };

  const isTransitioning =
    enabled && !isReady && !isError && !primary.isLoading;

  return {
    displayItems,
    displayPage: requestedPage,
    setDisplayPage,
    nextPage,
    prevPage,
    serverPage: firstChunk,
    offsetInChunk,
    totalDisplayPages,
    totalElements,
    isLoading: primary.isLoading,
    isFetching: primary.isFetching || (needsTail && tail.isFetching),
    isError,
    isTransitioning,
    isPlaceholderData: isTransitioning,
    error: primary.error ?? (needsTail ? tail.error : null),
    refetch,
  };
}