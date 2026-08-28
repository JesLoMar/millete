import { useCallback } from "react"
import { apiClient } from "@/shared/api/axiosClient"
import { useServerPagination, type PaginatedResponse } from "@/shared/hooks/useServerPagination"
import type { PeriodFilter } from "@/shared/components/Header"
import type { Transaction } from "../components/types"

const SERVER_SIZE = 50
const DISPLAY_SIZE = 10

interface TransactionApiItem {
  id: string
  categoryId: string
  categoryName: string
  categoryColor: string | null
  amount: number
  date: string
  type: "INCOME" | "EXPENSE"
  description: string
  active: boolean
}

interface UseTransactionsOptions {
  search?: string
  type?: "all" | "income" | "expense"
  period?: PeriodFilter
  enabled?: boolean
}

export function useTransactions(options: UseTransactionsOptions = {}) {
  const { search = "", type = "all", period = "month", enabled = true } = options

  const fetchPage = useCallback(
    async (page: number): Promise<PaginatedResponse<Transaction>> => {
      const params = new URLSearchParams({
        page: String(page),
        size: String(SERVER_SIZE),
        period,
      })
      if (search.trim()) params.set("search", search.trim())
      if (type !== "all") params.set("type", type.toUpperCase())

      const response = await apiClient.get(`/transactions?${params.toString()}`)
      const data = response.data as PaginatedResponse<TransactionApiItem>
      return {
        ...data,
        content: data.content.map((tx) => ({
          id: tx.id,
          categoryId: tx.categoryId,
          category: tx.categoryName || "Sin categoría",
          categoryColor: tx.categoryColor,
          amount: tx.amount,
          date: tx.date,
          type: tx.type,
          description: tx.description,
          active: tx.active,
        })),
      }
    },
    [search, type, period]
  )

  return {
    ...useServerPagination<Transaction>({
      queryKey: ["transactions", search, type, period ?? ""],
      fetchPage,
      serverSize: SERVER_SIZE,
      displaySize: DISPLAY_SIZE,
      enabled,
    }),
    serverSize: SERVER_SIZE,
    displaySize: DISPLAY_SIZE,
  }
}
