import { useCallback } from "react"
import { useServerPagination, type PaginatedResponse } from "@/shared/hooks/useServerPagination"
import { apiClient } from "@/shared/api/axiosClient"
import { useQuery } from "@tanstack/react-query"
import type { PeriodFilter } from "@/shared/components/PeriodSelector"
import type { InvestmentResponse, InvestmentMetricsData, EvolutionResponse, DistributionResponse } from "../types"

const SERVER_SIZE = 50
const DISPLAY_SIZE = 10

export interface PaginatedInvestmentsResponse {
  content: InvestmentResponse[]
  currentPage: number
  totalPages: number
  totalElements: number
  size: number
  first: boolean
  last: boolean
}

interface UseInvestmentsOptions {
  period: PeriodFilter
  search?: string
  type?: string
  enabled?: boolean
}

export function useInvestmentQueries(options: UseInvestmentsOptions) {
  const { period, search = "", type = "all", enabled = true } = options

  const fetchPage = useCallback(
    async (page: number): Promise<PaginatedResponse<InvestmentResponse>> => {
      const params = new URLSearchParams({
        page: String(page),
        size: String(SERVER_SIZE),
      })
      if (search.trim()) params.set("search", search.trim())
      if (type !== "all") params.set("type", type)

      const response = await apiClient.get(`/investments?${params.toString()}`)
      return response.data
    },
    [search, type]
  )

  const pagination = useServerPagination<InvestmentResponse>({
    queryKey: ["investments", search, type],
    fetchPage,
    serverSize: SERVER_SIZE,
    displaySize: DISPLAY_SIZE,
    enabled,
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
    investments: { data: pagination, isLoading: pagination.isLoading },
    metrics: { data: metricsData, isLoading: metricsIsLoading },
    evolution: { data: evolutionData, isLoading: evolutionIsLoading },
    distribution: { data: distributionData, isLoading: distributionIsLoading },
    serverSize: SERVER_SIZE,
    displaySize: DISPLAY_SIZE,
  }
}
