import { useMemo, useState, useCallback } from "react"

interface UsePaginationProps {
  totalItems: number
  itemsPerPage?: number
  initialPage?: number
}

export function usePagination({ totalItems, itemsPerPage = 10, initialPage = 1 }: UsePaginationProps) {
  const [currentPage, setCurrentPage] = useState(initialPage)

  const totalPages = useMemo(
    () => Math.max(1, Math.ceil(totalItems / itemsPerPage)),
    [totalItems, itemsPerPage]
  )

  // Clamp currentPage to valid range whenever totalPages changes
  const effectivePage = useMemo(
    () => Math.min(currentPage, totalPages),
    [currentPage, totalPages]
  )

  const paginatedRange = useMemo(() => {
    const start = (effectivePage - 1) * itemsPerPage
    const end = start + itemsPerPage
    return { start, end }
  }, [effectivePage, itemsPerPage])

  const goToPage = useCallback((page: number) => {
    setCurrentPage(Math.max(1, Math.min(page, totalPages)))
  }, [totalPages])

  const nextPage = useCallback(() => {
    setCurrentPage(Math.min(effectivePage + 1, totalPages))
  }, [effectivePage, totalPages])

  const prevPage = useCallback(() => {
    setCurrentPage(Math.max(1, effectivePage - 1))
  }, [effectivePage])

  const resetPage = useCallback(() => {
    setCurrentPage(1)
  }, [])

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
