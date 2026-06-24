import { useQuery } from "@tanstack/react-query"
import { apiClient } from "@/shared/api/axiosClient"
import type { PeriodFilter } from "@/shared/components/PeriodSelector"
import type { InvestmentResponse, InvestmentMetricsData, EvolutionResponse, DistributionResponse } from "../types"

export function useInvestmentQueries(period: PeriodFilter) {
  const { data: investmentsData, isLoading: investmentsIsLoading } = useQuery<InvestmentResponse[]>({
    queryKey: ['investments'],
    queryFn: async () => {
      const response = await apiClient.get('investments')
      return response.data.filter((inv: InvestmentResponse) => inv.active !== false)
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
