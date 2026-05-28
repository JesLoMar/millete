import { useQuery } from "@tanstack/react-query"
import { apiClient } from "@/shared/api/axiosClient"

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

export function useCategories() {
  return useQuery<Category[]>({
    queryKey: ['categories'],
    queryFn: async () => {
      const response = await apiClient.get('/categories')
      return response.data.filter((cat: Category) => cat.active !== false)
    },
    staleTime: 5 * 60 * 1000,
  })
}