import { useQuery } from "@tanstack/react-query"
import { apiClient } from "@/shared/api/axiosClient"
import type { PeriodFilter } from "@/shared/components/PeriodSelector"
import type { InvestmentResponse, InvestmentMetricsData, EvolutionResponse, DistributionResponse } from "../types"

export function useInvestmentQueries(period: PeriodFilter) {
  const investments = useQuery<InvestmentResponse[]>({
    queryKey: ['investments'],
    queryFn: async () => {
      const response = await apiClient.get('investments')
      return response.data.filter((inv: InvestmentResponse) => inv.active !== false)
    },
    retry: 1,
    staleTime: 30_000,
  })

  const metrics = useQuery<InvestmentMetricsData>({
    queryKey: ['investmentMetrics', period],
    queryFn: async () => {
      const response = await apiClient.get(`dashboard/investments/metrics?period=${period}`)
      return response.data
    },
    retry: 1,
    staleTime: 30_000,
  })

  const evolution = useQuery<EvolutionResponse>({
    queryKey: ['investmentEvolution', period],
    queryFn: async () => {
      const res = await apiClient.get(`dashboard/investments/evolution?period=${period}`)
      return res.data
    },
    retry: 1,
    staleTime: 30_000,
  })

  const distribution = useQuery<DistributionResponse>({
    queryKey: ['investmentDistribution', period],
    queryFn: async () => {
      const response = await apiClient.get(`dashboard/investments/distribution?period=${period}`)
      return response.data
    },
    retry: 1,
    staleTime: 30_000,
  })

  return { investments, metrics, evolution, distribution }
}