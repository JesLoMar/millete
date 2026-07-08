import { useQuery } from "@tanstack/react-query"
import { apiClient } from "@/shared/api/axiosClient"
import type { PeriodFilter } from "@/shared/components/PeriodSelector"
import type { InvestmentResponse, InvestmentMetricsData, EvolutionResponse, DistributionResponse } from "../types"

export interface PaginatedInvestmentsResponse {
  content: InvestmentResponse[]
  currentPage: number
  totalPages: number
  totalElements: number
  size: number
  first: boolean
  last: boolean
}

export function useInvestmentQueries(period: PeriodFilter, page: number = 0, size: number = 10) {
  const { data: investmentsData, isLoading: investmentsIsLoading } = useQuery<PaginatedInvestmentsResponse>({
    queryKey: ['investments', page, size],
    queryFn: async () => {
      const response = await apiClient.get(`investments?page=${page}&size=${size}`)
      return response.data
    },
    retry: 1,
    staleTime: 30_000,
  })

  const { data: metricsData, isLoading: metricsIsLoading } = useQuery<InvestmentMetricsData>({
    queryKey: ['investmentMetrics', period],
    queryFn: async () => {
      const response = await apiClient.get(`dashboard/investments/metrics?period=${period}`)
      return response.data
    },
    retry: 1,
    staleTime: 30_000,
  })

  const { data: evolutionData, isLoading: evolutionIsLoading } = useQuery<EvolutionResponse>({
    queryKey: ['investmentEvolution', period],
    queryFn: async () => {
      const res = await apiClient.get(`dashboard/investments/evolution?period=${period}`)
      return res.data
    },
    retry: 1,
    staleTime: 30_000,
  })

  const { data: distributionData, isLoading: distributionIsLoading } = useQuery<DistributionResponse>({
    queryKey: ['investmentDistribution', period],
    queryFn: async () => {
      const response = await apiClient.get(`dashboard/investments/distribution?period=${period}`)
      return response.data
    },
    retry: 1,
    staleTime: 30_000,
  })

  return {
    investments: { data: investmentsData, isLoading: investmentsIsLoading },
    metrics: { data: metricsData, isLoading: metricsIsLoading },
    evolution: { data: evolutionData, isLoading: evolutionIsLoading },
    distribution: { data: distributionData, isLoading: distributionIsLoading },
  }
}
