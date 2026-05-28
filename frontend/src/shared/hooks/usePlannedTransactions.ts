import { useQuery } from "@tanstack/react-query"
import { apiClient } from "@/shared/api/axiosClient"

export interface PlannedTransaction {
  id: string
  description: string
  categoryName: string
  categoryId: string
  amount: number
  type: "INCOME" | "EXPENSE"
  frequencyType: "DAYS" | "WEEKS" | "MONTHS" | "YEARS"
  frequencyInterval: number
  startDate: string
  endDate: string | null
  active: boolean
}

export function usePlannedTransactions() {
  return useQuery<PlannedTransaction[]>({
    queryKey: ['plannedTransactions'],
    queryFn: async () => {
      const response = await apiClient.get('/planned-transactions')
      return response.data
    },
  })
}