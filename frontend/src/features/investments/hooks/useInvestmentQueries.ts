import { useMemo, useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { investmentsService } from '../services/investments.service';
import { INVESTMENT_QUERY_KEYS } from '../constants';
import { getPerformanceRange } from '../utils';
import type {
  ActivityPage,
  ClosedLot,
  HealthReport,
  Holding,
  InvestmentTrackingSettings,
  ListActivitiesParams,
  Performance,
  Portfolio,
  Sector,
  SharedAsset,
  UserAsset,
} from '../types';

// ------------------------------------------------------------
// Portfolio
// ------------------------------------------------------------
export function useCurrentPortfolio(enabled: boolean = true) {
  const [asOf] = useState(() => new Date().toISOString());

  return useQuery<Portfolio>({
    queryKey: [...INVESTMENT_QUERY_KEYS.portfolio, asOf],
    queryFn: ({ signal }) => investmentsService.getPortfolio(asOf, signal),
    enabled,
    staleTime: 30_000,
    retry: 1,
  });
}

// ------------------------------------------------------------
// Holdings (el backend devuelve PaginatedResponseDTO)
// ------------------------------------------------------------
export function useHoldings(enabled: boolean = true) {
  const query = useQuery({
    queryKey: INVESTMENT_QUERY_KEYS.holdings,
    queryFn: ({ signal }) =>
      investmentsService.getHoldings({ size: 200 }, signal),
    enabled,
    staleTime: 30_000,
    retry: 1,
  });

  return {
    ...query,
    holdings: (query.data?.content ?? []) as Holding[],
  };
}

// ------------------------------------------------------------
// Activities
// ------------------------------------------------------------
export function useActivities(params: ListActivitiesParams = {}) {
  return useQuery<ActivityPage>({
    queryKey: [...INVESTMENT_QUERY_KEYS.activities, params],
    queryFn: ({ signal }) =>
      investmentsService.listActivities({ size: 50, ...params }, signal),
    staleTime: 15_000,
    retry: 1,
  });
}

// ------------------------------------------------------------
// Performance
// ------------------------------------------------------------
export function usePerformance(periodKey: string, enabled: boolean = true) {
  const range = useMemo(() => getPerformanceRange(periodKey), [periodKey]);

  return useQuery<Performance>({
    queryKey: [...INVESTMENT_QUERY_KEYS.performance, periodKey],
    queryFn: ({ signal }) => investmentsService.getPerformance(range, signal),
    enabled,
    staleTime: 60_000,
    retry: 1,
  });
}

// ------------------------------------------------------------
// Closed lots (el backend devuelve PaginatedResponseDTO)
// ------------------------------------------------------------
export function useClosedLots(enabled: boolean = true) {
  const [range] = useState(() => {
    const to = new Date().toISOString();
    const from = new Date();
    from.setFullYear(from.getFullYear() - 1);
    return { from: from.toISOString(), to };
  });

  const query = useQuery({
    queryKey: [...INVESTMENT_QUERY_KEYS.closedLots, range.from],
    queryFn: ({ signal }) =>
      investmentsService.getClosedLots(range.from, range.to, 0, 200, signal),
    enabled,
    staleTime: 60_000,
    retry: 1,
  });

  return {
    ...query,
    lots: (query.data?.content ?? []) as ClosedLot[],
  };
}

// ------------------------------------------------------------
// Shared assets (devuelve array directo)
// ------------------------------------------------------------
export function useSharedAssets(params: { search?: string } = {}) {
  return useQuery<SharedAsset[]>({
    queryKey: [...INVESTMENT_QUERY_KEYS.sharedAssets, params.search],
    queryFn: ({ signal }) => investmentsService.listSharedAssets(params, signal),
    staleTime: 5 * 60_000,
    retry: false,
  });
}

// ------------------------------------------------------------
// User assets (devuelve array directo)
// ------------------------------------------------------------
export function useUserAssets(enabled: boolean = true) {
  return useQuery<UserAsset[]>({
    queryKey: INVESTMENT_QUERY_KEYS.userAssets,
    queryFn: ({ signal }) => investmentsService.listUserAssets({}, signal),
    enabled,
    staleTime: 30_000,
    retry: false,
  });
}

// ------------------------------------------------------------
// Sectors
// ------------------------------------------------------------
export function useSectors(enabled: boolean = true) {
  return useQuery<Sector[]>({
    queryKey: INVESTMENT_QUERY_KEYS.sectors,
    queryFn: ({ signal }) => investmentsService.getSectors(signal),
    enabled,
    staleTime: 30 * 60_000,
    retry: false,
  });
}

// ------------------------------------------------------------
// Tracking
// ------------------------------------------------------------
export function useInvestmentTracking() {
  return useQuery<InvestmentTrackingSettings | null>({
    queryKey: INVESTMENT_QUERY_KEYS.tracking,
    queryFn: ({ signal }) => investmentsService.getTracking(signal),
    staleTime: 5 * 60_000,
    retry: 1,
  });
}

// ------------------------------------------------------------
// Health
// ------------------------------------------------------------
export function useInvestmentHealth(enabled: boolean = false) {
  return useQuery<HealthReport>({
    queryKey: INVESTMENT_QUERY_KEYS.health,
    queryFn: ({ signal }) => investmentsService.getHealth(signal),
    enabled,
    staleTime: 60_000,
    retry: 1,
  });
}