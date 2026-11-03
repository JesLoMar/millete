import { useMemo } from 'react';
import {
  keepPreviousData,
  useQuery,
} from '@tanstack/react-query';
import { investmentsService } from '../services/investments.service';
import { INVESTMENT_QUERY_KEYS } from '../constants';
import { assetKey } from '../utils';
import type {
  AssetCatalogEntry,
  AssetPricesParams,
  AssetReference,
  ClosedLotPage,
  FxRatePage,
  FxRatesParams,
  HealthReport,
  HoldingPage,
  InvestmentLedgerSnapshot,
  InvestmentTrackingSettings,
  ListActivitiesParams,
  ListHoldingsParams,
  Performance,
  PeriodParams,
  Portfolio,
  Sector,
  SharedAsset,
  UserAsset,
} from '../types';

export function usePortfolio(
  asOf: string,
  enabled: boolean = true,
) {
  return useQuery<Portfolio>({
    queryKey: [...INVESTMENT_QUERY_KEYS.portfolio, asOf],
    queryFn: ({ signal }) =>
      investmentsService.getPortfolio(asOf, signal),
    enabled,
    staleTime: 30_000,
    retry: 1,
  });
}

export function useInvestmentCash(
  asOf: string,
  enabled: boolean = true,
) {
  return useQuery({
    queryKey: ['investmentCash', asOf],
    queryFn: ({ signal }) =>
      investmentsService.getInvestmentCash(asOf, signal),
    enabled,
    staleTime: 30_000,
    retry: 1,
  });
}

export function useActivities(
  params: ListActivitiesParams = {},
) {
  return useQuery({
    queryKey: [...INVESTMENT_QUERY_KEYS.activities, params],
    queryFn: ({ signal }) =>
      investmentsService.listActivities(params, signal),
    placeholderData: keepPreviousData,
    staleTime: 15_000,
  });
}

export function useHoldings(
  params: ListHoldingsParams = {},
  enabled: boolean = true,
) {
  return useQuery<HoldingPage>({
    queryKey: [...INVESTMENT_QUERY_KEYS.holdings, params],
    queryFn: ({ signal }) =>
      investmentsService.listHoldings(params, signal),
    enabled,
    placeholderData: keepPreviousData,
    staleTime: 15_000,
  });
}

export function useClosedLots(
  from: string,
  to: string,
  page: number,
  size: number,
  enabled: boolean = true,
) {
  return useQuery<ClosedLotPage>({
    queryKey: [
      ...INVESTMENT_QUERY_KEYS.closedLots,
      from,
      to,
      page,
      size,
    ],
    queryFn: ({ signal }) =>
      investmentsService.getClosedLots(
        from,
        to,
        page,
        size,
        signal,
      ),
    enabled: enabled && Boolean(from) && Boolean(to),
    placeholderData: keepPreviousData,
    staleTime: 15_000,
  });
}

export function usePerformance(
  params: PeriodParams,
  enabled: boolean = true,
) {
  return useQuery<Performance>({
    queryKey: [
      ...INVESTMENT_QUERY_KEYS.performance,
      params.from,
      params.to,
    ],
    queryFn: ({ signal }) =>
      investmentsService.getPerformance(params, signal),
    enabled:
      enabled &&
      Boolean(params.from) &&
      Boolean(params.to) &&
      new Date(params.from) < new Date(params.to),
    staleTime: 30_000,
    retry: 1,
  });
}

export function useSharedAssets(search?: string) {
  return useQuery<SharedAsset[]>({
    queryKey: [
      ...INVESTMENT_QUERY_KEYS.sharedAssets,
      search ?? '',
    ],
    queryFn: ({ signal }) =>
      investmentsService.listSharedAssets(
        search ? { search } : {},
        signal,
      ),
    staleTime: 5 * 60_000,
  });
}

export function useUserAssets(search?: string) {
  return useQuery<UserAsset[]>({
    queryKey: [
      ...INVESTMENT_QUERY_KEYS.userAssets,
      search ?? '',
    ],
    queryFn: ({ signal }) =>
      investmentsService.listUserAssets(
        search ? { search } : {},
        signal,
      ),
    staleTime: 2 * 60_000,
  });
}

export function useSectors() {
  return useQuery<Sector[]>({
    queryKey: INVESTMENT_QUERY_KEYS.sectors,
    queryFn: ({ signal }) =>
      investmentsService.getSectors(signal),
    staleTime: 10 * 60_000,
  });
}

export interface AssetCatalog {
  entries: AssetCatalogEntry[];
  byKey: Map<string, AssetCatalogEntry>;
  isLoading: boolean;
  isError: boolean;
  getEntry: (
    reference: AssetReference | null | undefined,
  ) => AssetCatalogEntry | null;
}

/**
 * Catálogo combinado de activos compartidos y propios. La API no incluye
 * nombre/símbolo en las posiciones ni en las actividades, así que se enriquece
 * en cliente con estas dos listas.
 */
export function useAssetCatalog(): AssetCatalog {
  const sharedQuery = useSharedAssets();
  const userQuery = useUserAssets();

  const entries = useMemo<AssetCatalogEntry[]>(() => {
    const shared = (sharedQuery.data ?? []).map<AssetCatalogEntry>(
      (asset) => ({
        reference: { kind: 'SHARED', id: asset.id },
        name: asset.name,
        symbol: asset.symbol,
        type: asset.type,
        sector: asset.sector,
        currency: asset.currency,
        isUserAsset: false,
      }),
    );

    const user = (userQuery.data ?? []).map<AssetCatalogEntry>(
      (asset) => ({
        reference: { kind: 'USER', id: asset.id },
        name: asset.name,
        symbol: null,
        type: asset.type,
        sector: asset.sector,
        currency: asset.currency,
        isUserAsset: true,
      }),
    );

    return [...shared, ...user];
  }, [sharedQuery.data, userQuery.data]);

  const byKey = useMemo(
    () =>
      new Map(
        entries.map((entry) => [
          assetKey(entry.reference),
          entry,
        ]),
      ),
    [entries],
  );

  return {
    entries,
    byKey,
    isLoading: sharedQuery.isLoading || userQuery.isLoading,
    isError: sharedQuery.isError || userQuery.isError,
    getEntry: (reference) =>
      reference ? byKey.get(assetKey(reference)) ?? null : null,
  };
}

export function useTracking() {
  return useQuery<InvestmentTrackingSettings>({
    queryKey: INVESTMENT_QUERY_KEYS.tracking,
    queryFn: ({ signal }) =>
      investmentsService.getTracking(signal),
    staleTime: 60_000,
    retry: 1,
  });
}

export function useHealth() {
  return useQuery<HealthReport>({
    queryKey: INVESTMENT_QUERY_KEYS.health,
    queryFn: ({ signal }) =>
      investmentsService.getHealth(signal),
    staleTime: 60_000,
    retry: 1,
  });
}

export function useAssetPrices(
  sharedAssetId: string | null,
  params: AssetPricesParams,
  enabled: boolean = true,
) {
  return useQuery({
    queryKey: [
      ...INVESTMENT_QUERY_KEYS.assetPrices,
      sharedAssetId,
      params,
    ],
    queryFn: ({ signal }) =>
      investmentsService.getAssetPrices(
        sharedAssetId as string,
        params,
        signal,
      ),
    enabled:
      enabled &&
      Boolean(sharedAssetId) &&
      Boolean(params.from) &&
      Boolean(params.to),
    placeholderData: keepPreviousData,
    staleTime: 60_000,
  });
}

export function useFxRates(
  params: FxRatesParams,
  enabled: boolean = true,
) {
  return useQuery<FxRatePage>({
    queryKey: [
      ...INVESTMENT_QUERY_KEYS.fxRates,
      params,
    ],
    queryFn: ({ signal }) =>
      investmentsService.getFxRates(params, signal),
    enabled:
      enabled &&
      Boolean(params.baseCurrency) &&
      Boolean(params.quoteCurrency) &&
      Boolean(params.from) &&
      Boolean(params.to),
    placeholderData: keepPreviousData,
    staleTime: 60_000,
  });
}

export function useLedgerSnapshot(enabled: boolean = false) {
  return useQuery<InvestmentLedgerSnapshot>({
    queryKey: INVESTMENT_QUERY_KEYS.ledger,
    queryFn: ({ signal }) =>
      investmentsService.getLedgerSnapshot(signal),
    enabled,
    staleTime: Infinity,
    retry: 1,
  });
}
