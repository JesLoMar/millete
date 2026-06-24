import { useMemo, useState, useEffect } from "react"

interface UsePaginationProps {
  totalItems: number
  itemsPerPage?: number
  initialPage?: number
}

export function usePagination({ totalItems, itemsPerPage = 10, initialPage = 1 }: UsePaginationProps) {
  const [currentPage, setCurrentPage] = useState(initialPage)

  const totalPages = useMemo(() => Math.max(1, Math.ceil(totalItems / itemsPerPage)), [totalItems, itemsPerPage])

  // Reset to page 1 when totalItems changes (e.g., filter applied)
  useEffect(() => {
    setCurrentPage(1)
  }, [totalItems])

  const paginatedRange = useMemo(() => {
    const start = (currentPage - 1) * itemsPerPage
    const end = start + itemsPerPage
    return { start, end }
  }, [currentPage, itemsPerPage])

  const goToPage = (page: number) => {
    setCurrentPage(Math.max(1, Math.min(page, totalPages)))
  }

  const nextPage = () => goToPage(currentPage + 1)
  const prevPage = () => goToPage(currentPage - 1)
  const resetPage = () => setCurrentPage(1)

  return {
    currentPage,
    totalPages,
    paginatedRange,
    goToPage,
    nextPage,
    prevPage,
    resetPage,
  }
}
