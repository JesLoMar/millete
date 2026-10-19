import { apiClient } from '@/shared/api/axiosClient';
import type {
  Activity,
  ActivityPage,
  ClosedLot,
  ConfigureTrackingRequest,
  CreateUserAssetRequest,
  EditActivityRequest,
  GetPerformanceParams,
  HealthReport,
  Holding,
  InvestmentLedgerSnapshotResponse,
  InvestmentTrackingSettings,
  ListActivitiesParams,
  MarketDataRefreshResult,
  Performance,
  Portfolio,
  RegisterActivityRequest,
  RegisterUserAssetPriceRequest,
  RestoreLedgerRequest,
  Sector,
  SharedAsset,
  UpdateUserAssetRequest,
  UserAsset,
} from '../types';

const BASE = '/investments';

/**
 * Shape genérico de las respuestas paginadas del backend.
 * Coincide con `PaginatedResponseDTO<T>` de Spring.
 */
export interface PaginatedResponse<T> {
  content: T[];
  currentPage: number;
  totalPages: number;
  totalElements: number;
  size: number;
  first: boolean;
  last: boolean;
}

/** Genera una clave de idempotencia para las actividades */
function generateIdempotencyKey(): string {
  return crypto.randomUUID();
}

export const investmentsService = {
  // ----------------------------------------------------------
  // Portfolio → /api/v1/investments/portfolio
  // ----------------------------------------------------------
  getPortfolio: async (
    asOf: string,
    signal?: AbortSignal,
  ): Promise<Portfolio> => {
    const { data } = await apiClient.get<Portfolio>(`${BASE}/portfolio`, {
      params: { asOf },
      signal,
    });
    return data;
  },

  getInvestmentCash: async (
    asOf: string,
    signal?: AbortSignal,
  ): Promise<{ balances: unknown[] }> => {
    const { data } = await apiClient.get(`${BASE}/cash`, {
      params: { asOf },
      signal,
    });
    return data;
  },

  // ----------------------------------------------------------
  // Holdings → /api/v1/investments/holdings
  // ----------------------------------------------------------
  getHoldings: async (
    params?: { assetKind?: string; assetId?: string; page?: number; size?: number },
    signal?: AbortSignal,
  ): Promise<PaginatedResponse<Holding>> => {
    const { data } = await apiClient.get<PaginatedResponse<Holding>>(
      `${BASE}/holdings`,
      { params, signal },
    );
    return data;
  },

  getClosedLots: async (
    from: string,
    to: string,
    page: number = 0,
    size: number = 200,
    signal?: AbortSignal,
  ): Promise<PaginatedResponse<ClosedLot>> => {
    const { data } = await apiClient.get<PaginatedResponse<ClosedLot>>(
      `${BASE}/lots/closed`,
      { params: { from, to, page, size }, signal },
    );
    return data;
  },

  // ----------------------------------------------------------
  // Activities → /api/v1/investments/activities
  // ----------------------------------------------------------
  listActivities: async (
    params: ListActivitiesParams = {},
    signal?: AbortSignal,
  ): Promise<ActivityPage> => {
    const { data } = await apiClient.get<ActivityPage>(`${BASE}/activities`, {
      params,
      signal,
    });
    return data;
  },

  registerBuy: async (payload: RegisterActivityRequest) => {
    const { data } = await apiClient.post<Activity>(
      `${BASE}/activities/buy`,
      {
        occurredAt: payload.occurredAt,
        assetReference: payload.assetReference,
        quantity: (payload.details as any).quantity,
        unitPrice: (payload.details as any).unitPrice,
        settlementCurrency: (payload.details as any).currency,
        comment: payload.comment,
      },
      {
        headers: { 'Idempotency-Key': generateIdempotencyKey() },
        skipGlobalErrorNotify: true,
      },
    );
    return data;
  },

  registerSell: async (payload: RegisterActivityRequest) => {
    const { data } = await apiClient.post<Activity>(
      `${BASE}/activities/sell`,
      {
        occurredAt: payload.occurredAt,
        assetReference: payload.assetReference,
        quantity: (payload.details as any).quantity,
        unitPrice: (payload.details as any).unitPrice,
        settlementCurrency: (payload.details as any).currency,
        comment: payload.comment,
      },
      {
        headers: { 'Idempotency-Key': generateIdempotencyKey() },
        skipGlobalErrorNotify: true,
      },
    );
    return data;
  },

  registerDeposit: async (payload: RegisterActivityRequest) => {
    const { data } = await apiClient.post<Activity>(
      `${BASE}/activities/deposits`,
      {
        occurredAt: payload.occurredAt,
        amount: (payload.details as any).amount,
        currency: (payload.details as any).currency,
        comment: payload.comment,
      },
      {
        headers: { 'Idempotency-Key': generateIdempotencyKey() },
        skipGlobalErrorNotify: true,
      },
    );
    return data;
  },

  registerWithdrawal: async (payload: RegisterActivityRequest) => {
    const { data } = await apiClient.post<Activity>(
      `${BASE}/activities/withdrawals`,
      {
        occurredAt: payload.occurredAt,
        amount: (payload.details as any).amount,
        currency: (payload.details as any).currency,
        comment: payload.comment,
      },
      {
        headers: { 'Idempotency-Key': generateIdempotencyKey() },
        skipGlobalErrorNotify: true,
      },
    );
    return data;
  },

  editActivity: async (id: string, payload: EditActivityRequest) => {
    const { data } = await apiClient.patch<Activity>(
      `${BASE}/activities/${id}`,
      payload,
      { skipGlobalErrorNotify: true },
    );
    return data;
  },

  // ----------------------------------------------------------
  // Performance → /api/v1/investments/performance
  // ----------------------------------------------------------
  getPerformance: async (
    params: GetPerformanceParams,
    signal?: AbortSignal,
  ): Promise<Performance> => {
    const { data } = await apiClient.get<Performance>(
      `${BASE}/performance`,
      { params, signal },
    );
    return data;
  },

  // ----------------------------------------------------------
  // Shared assets → /api/v1/investments/shared-assets
  // ----------------------------------------------------------
  listSharedAssets: async (
    params: { search?: string } = {},
    signal?: AbortSignal,
  ): Promise<SharedAsset[]> => {
    const { data } = await apiClient.get<SharedAsset[]>(
      `${BASE}/shared-assets`,
      { params, signal },
    );
    return data;
  },

  // ----------------------------------------------------------
  // Sectors → /api/v1/investments/sectors
  // ----------------------------------------------------------
  getSectors: async (signal?: AbortSignal): Promise<Sector[]> => {
    const { data } = await apiClient.get<Sector[]>(`${BASE}/sectors`, {
      signal,
    });
    return data;
  },

  // ----------------------------------------------------------
  // User assets → /api/v1/investments/user-assets
  // ----------------------------------------------------------
  listUserAssets: async (
    params: { search?: string } = {},
    signal?: AbortSignal,
  ): Promise<UserAsset[]> => {
    const { data } = await apiClient.get<UserAsset[]>(
      `${BASE}/user-assets`,
      { params, signal },
    );
    return data;
  },

  createUserAsset: async (payload: CreateUserAssetRequest) => {
    const { data } = await apiClient.post<UserAsset>(
      `${BASE}/user-assets`,
      payload,
      { skipGlobalErrorNotify: true },
    );
    return data;
  },

  updateUserAsset: async (id: string, payload: UpdateUserAssetRequest) => {
    const { data } = await apiClient.put<UserAsset>(
      `${BASE}/user-assets/${id}`,
      payload,
      { skipGlobalErrorNotify: true },
    );
    return data;
  },

  registerUserAssetPrice: async (
    assetId: string,
    payload: RegisterUserAssetPriceRequest,
  ) => {
    const { data } = await apiClient.post(
      `${BASE}/user-assets/${assetId}/prices`,
      payload,
      { skipGlobalErrorNotify: true },
    );
    return data;
  },

  // ----------------------------------------------------------
  // Tracking → /api/v1/investments/tracking
  // ----------------------------------------------------------
  getTracking: async (signal?: AbortSignal) => {
    try {
      const { data } = await apiClient.get<InvestmentTrackingSettings>(
        `${BASE}/tracking`,
        { signal, skipGlobalErrorNotify: true },
      );
      return data;
    } catch {
      return null;
    }
  },

  configureTracking: async (payload: ConfigureTrackingRequest) => {
    const { data } = await apiClient.post<InvestmentTrackingSettings>(
      `${BASE}/tracking`,
      payload,
      { skipGlobalErrorNotify: true },
    );
    return data;
  },

  // ----------------------------------------------------------
  // Health → /api/v1/investments/health
  // ----------------------------------------------------------
  getHealth: async (signal?: AbortSignal): Promise<HealthReport> => {
    const { data } = await apiClient.get<HealthReport>(`${BASE}/health`, {
      signal,
    });
    return data;
  },

  // ----------------------------------------------------------
  // Market data → /api/v1/investments/market-data/refresh
  // ----------------------------------------------------------
  refreshMarketData: async (
    from: string,
    to: string,
    currencyPairs: string[] = [],
  ): Promise<void> => {
    await apiClient.post(
      `${BASE}/market-data/refresh`,
      { from, to, currencyPairs },
      { skipGlobalErrorNotify: true },
    );
  },

  // ----------------------------------------------------------
  // Ledger snapshot → /api/v1/investments/ledger
  // ----------------------------------------------------------
  getLedgerSnapshot: async (signal?: AbortSignal) => {
    const { data } = await apiClient.get<InvestmentLedgerSnapshotResponse>(
      `${BASE}/ledger/snapshot`,
      { signal },
    );
    return data;
  },

  restoreLedger: async (payload: RestoreLedgerRequest) => {
    await apiClient.post(`${BASE}/ledger/restore`, payload, {
      skipGlobalErrorNotify: true,
    });
  },
};