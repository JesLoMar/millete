import { useMemo, useState } from "react"

interface UsePaginationProps {
  totalItems: number
  itemsPerPage?: number
  initialPage?: number
}

export function usePagination({ totalItems, itemsPerPage = 10, initialPage = 1 }: UsePaginationProps) {
  const [currentPage, setCurrentPage] = useState(initialPage)

  const totalPages = useMemo(() => Math.max(1, Math.ceil(totalItems / itemsPerPage)), [totalItems, itemsPerPage])

  // Clamp the visible page when totalItems changes so users never see an empty page.
  const effectivePage = Math.min(currentPage, totalPages)

  const paginatedRange = useMemo(() => {
    const start = (effectivePage - 1) * itemsPerPage
    const end = start + itemsPerPage
    return { start, end }
  }, [effectivePage, itemsPerPage])

  const goToPage = (page: number) => {
    setCurrentPage(Math.max(1, Math.min(page, totalPages)))
  }

  const nextPage = () => goToPage(currentPage + 1)
  const prevPage = () => goToPage(currentPage - 1)
  const resetPage = () => setCurrentPage(1)

  return {
    currentPage: effectivePage,
    totalPages,
    paginatedRange,
    goToPage,
    nextPage,
    prevPage,
    resetPage,
  }
}
