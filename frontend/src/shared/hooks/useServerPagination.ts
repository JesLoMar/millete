import { useEffect, useMemo, useState } from "react"
import { useQuery, useQueryClient } from "@tanstack/react-query"

export interface PaginatedResponse<T> {
  content: T[]
  currentPage: number
  totalPages: number
  totalElements: number
  size: number
  first: boolean
  last: boolean
}

interface UseServerPaginationOptions<T> {
  queryKey: string[]
  fetchPage: (page: number) => Promise<PaginatedResponse<T>>
  serverSize: number
  displaySize: number
  initialPage?: number
  enabled?: boolean
}

export function useServerPagination<T>({
  queryKey,
  fetchPage,
  serverSize,
  displaySize,
  initialPage = 0,
  enabled = true,
}: UseServerPaginationOptions<T>) {
  const queryClient = useQueryClient()
  const [displayPage, setDisplayPageState] = useState(initialPage)

  const queryKeyString = queryKey.join(",")

  // Reset to the first display page whenever the underlying query/filters change.
  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect -- a new filter makes the current page stale
    setDisplayPageState(0)
  }, [queryKeyString])

  const serverPage = Math.floor((displayPage * displaySize) / serverSize)
  const offsetInChunk = (displayPage * displaySize) % serverSize

  const { data, isLoading, isFetching, error, refetch } = useQuery<PaginatedResponse<T>>({
    queryKey: [...queryKey, String(serverPage)],
    queryFn: () => fetchPage(serverPage),
    staleTime: 30_000,
    placeholderData: (previousData) => previousData,
    enabled,
  })

  const displayItems = useMemo(() => {
    if (!data) return []
    return data.content.slice(offsetInChunk, offsetInChunk + displaySize)
  }, [data, offsetInChunk, displaySize])

  const totalElements = data?.totalElements ?? 0
  const totalDisplayPages = useMemo(
    () => Math.max(1, Math.ceil(totalElements / displaySize)),
    [totalElements, displaySize]
  )

  useEffect(() => {
    if (displayPage > 0 && displayPage >= totalDisplayPages) {
      // eslint-disable-next-line react-hooks/set-state-in-effect -- keep current page in range if total shrinks
      setDisplayPageState(totalDisplayPages - 1)
    }
  }, [displayPage, totalDisplayPages])

  const setDisplayPage = (page: number) => {
    setDisplayPageState(Math.max(0, Math.min(page, totalDisplayPages - 1)))
  }

  const nextPage = () => setDisplayPage(displayPage + 1)
  const prevPage = () => setDisplayPage(displayPage - 1)

  const isLastPageOfChunk = ((displayPage + 1) * displaySize) % serverSize === 0
  const hasMoreChunks = data ? (serverPage + 1) * serverSize < data.totalElements : false

  // Prefetch the next server chunk when we land on the last display page of the current chunk.
  useEffect(() => {
    if (data && !isFetching && isLastPageOfChunk && hasMoreChunks) {
      const nextServerPage = serverPage + 1
      queryClient.prefetchQuery({
        queryKey: [...queryKey, String(nextServerPage)],
        queryFn: () => fetchPage(nextServerPage),
        staleTime: 30_000,
      })
    }
  }, [data, isFetching, isLastPageOfChunk, hasMoreChunks, queryKey, serverPage, fetchPage, queryClient])

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
  }
}
