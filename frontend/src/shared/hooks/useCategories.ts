import { useCallback } from "react"
import { apiClient } from "@/shared/api/axiosClient"
import { useServerPagination, type PaginatedResponse } from "@/shared/hooks/useServerPagination"

const SERVER_SIZE = 50
const DISPLAY_SIZE = 10

export interface Category {
  id: string
  userId: string
  name: string
  color: string
  budgetLimit: number | null
  createdAt: string
  modifiedAt: string
  active: boolean
}

interface UseCategoriesOptions {
  search?: string
  enabled?: boolean
}

export function useCategories(options: UseCategoriesOptions = {}) {
  const { search = "", enabled = true } = options

  const fetchPage = useCallback(
    async (page: number): Promise<PaginatedResponse<Category>> => {
      const params = new URLSearchParams({
        page: String(page),
        size: String(SERVER_SIZE),
      })
      if (search.trim()) params.set("search", search.trim())

      const response = await apiClient.get(`/categories?${params.toString()}`)
      return response.data
    },
    [search]
  )

  return {
    ...useServerPagination<Category>({
      queryKey: ["categories", search],
      fetchPage,
      serverSize: SERVER_SIZE,
      displaySize: DISPLAY_SIZE,
      enabled,
    }),
    serverSize: SERVER_SIZE,
    displaySize: DISPLAY_SIZE,
  }
}
