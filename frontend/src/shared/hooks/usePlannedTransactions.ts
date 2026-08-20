import { useCallback } from "react"
import { apiClient } from "@/shared/api/axiosClient"
import { useServerPagination, type PaginatedResponse } from "@/shared/hooks/useServerPagination"

const SERVER_SIZE = 50
const DISPLAY_SIZE = 10

export interface PlannedTransaction {
  id: string
  description: string
  categoryName: string
  categoryId: string | null
  amount: number
  type: "INCOME" | "EXPENSE"
  frequencyType: "DAYS" | "WEEKS" | "MONTHS" | "YEARS"
  frequencyInterval: number
  startDate: string
  endDate: string | null
  lastExecutedDate: string | null
  active: boolean
}

interface UsePlannedTransactionsOptions {
  search?: string
  type?: "all" | "income" | "expense"
  enabled?: boolean
}

export function usePlannedTransactions(options: UsePlannedTransactionsOptions = {}) {
  const { search = "", type = "all", enabled = true } = options

  const fetchPage = useCallback(
    async (page: number): Promise<PaginatedResponse<PlannedTransaction>> => {
      const params = new URLSearchParams({
        page: String(page),
        size: String(SERVER_SIZE),
      })
      if (search.trim()) params.set("search", search.trim())
      if (type !== "all") params.set("type", type.toUpperCase())

      const response = await apiClient.get(`/planned-transactions?${params.toString()}`)
      return response.data
    },
    [search, type]
  )

  return {
    ...useServerPagination<PlannedTransaction>({
      queryKey: ["plannedTransactions", search, type],
      fetchPage,
      serverSize: SERVER_SIZE,
      displaySize: DISPLAY_SIZE,
      enabled,
    }),
    serverSize: SERVER_SIZE,
    displaySize: DISPLAY_SIZE,
  }
}
