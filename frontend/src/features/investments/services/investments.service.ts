import axios from 'axios';
import { apiClient } from '@/shared/api/axiosClient';
import type {
  Activity,
  ActivityPage,
  CashActivityDetails,
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
  TradeActivityDetails,
  UpdateUserAssetRequest,
  UserAsset,
} from '../types';

const BASE = '/investments';

export interface PaginatedResponse<T> {
  content: T[];
  currentPage: number;
  totalPages: number;
  totalElements: number;
  size: number;
  first: boolean;
  last: boolean;
}

/**
 * La clave se genera una vez por formulario (en el diálogo) para que un reintento
 * tras error de red no duplique la operación en backend.
 */
function resolveIdempotencyKey(payload: RegisterActivityRequest): string {
  return payload.idempotencyKey ?? crypto.randomUUID();
}

async function postTrade(
  side: 'buy' | 'sell',
  payload: RegisterActivityRequest,
): Promise<Activity> {
  const details = payload.details as TradeActivityDetails;
  const { data } = await apiClient.post<Activity>(
    `${BASE}/activities/${side}`,
    {
      occurredAt: payload.occurredAt,
      assetReference: payload.assetReference,
      quantity: details.quantity,
      unitPrice: details.unitPrice,
      settlementCurrency: details.currency,
      comment: payload.comment,
    },
    {
      headers: { 'Idempotency-Key': resolveIdempotencyKey(payload) },
      skipGlobalErrorNotify: true,
    },
  );
  return data;
}

async function postCash(
  path: 'deposits' | 'withdrawals',
  payload: RegisterActivityRequest,
): Promise<Activity> {
  const details = payload.details as CashActivityDetails;
  const { data } = await apiClient.post<Activity>(
    `${BASE}/activities/${path}`,
    {
      occurredAt: payload.occurredAt,
      amount: details.amount,
      currency: details.currency,
      comment: payload.comment,
    },
    {
      headers: { 'Idempotency-Key': resolveIdempotencyKey(payload) },
      skipGlobalErrorNotify: true,
    },
  );
  return data;
}

export const investmentsService = {
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

  // Operaciones soportadas por el formulario actual.
  // Pendiente en backend/servicio: DIVIDEND, SPLIT, EXCHANGE, OPENING_POSITION, OPENING_CASH.
  registerBuy: (payload: RegisterActivityRequest) => postTrade('buy', payload),
  registerSell: (payload: RegisterActivityRequest) => postTrade('sell', payload),
  registerDeposit: (payload: RegisterActivityRequest) =>
    postCash('deposits', payload),
  registerWithdrawal: (payload: RegisterActivityRequest) =>
    postCash('withdrawals', payload),

  editActivity: async (id: string, payload: EditActivityRequest) => {
    const { data } = await apiClient.patch<Activity>(
      `${BASE}/activities/${id}`,
      payload,
      { skipGlobalErrorNotify: true },
    );
    return data;
  },

  getPerformance: async (
    params: GetPerformanceParams,
    signal?: AbortSignal,
  ): Promise<Performance> => {
    const { data } = await apiClient.get<Performance>(`${BASE}/performance`, {
      params,
      signal,
    });
    return data;
  },

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

  getSectors: async (signal?: AbortSignal): Promise<Sector[]> => {
    const { data } = await apiClient.get<Sector[]>(`${BASE}/sectors`, {
      signal,
    });
    return data;
  },

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

  /**
   * Solo un 404 significa "tracking no configurado". Cualquier otro error (red, 500)
   * se propaga para que la UI no confunda una caída del servicio con una configuración vacía.
   */
  getTracking: async (
    signal?: AbortSignal,
  ): Promise<InvestmentTrackingSettings> => {
    try {
      const { data } = await apiClient.get<InvestmentTrackingSettings>(
        `${BASE}/tracking`,
        { signal, skipGlobalErrorNotify: true },
      );
      return data;
    } catch (error) {
      if (axios.isAxiosError(error) && error.response?.status === 404) {
        return { configured: false, trackingStartAt: null };
      }
      throw error;
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

  getHealth: async (signal?: AbortSignal): Promise<HealthReport> => {
    const { data } = await apiClient.get<HealthReport>(`${BASE}/health`, {
      signal,
    });
    return data;
  },

  refreshMarketData: async (
    from: string,
    to: string,
    currencies: string[] = [],
  ): Promise<MarketDataRefreshResult> => {
    const { data } = await apiClient.post<MarketDataRefreshResult>(
      `${BASE}/market-data/refresh`,
      { from, to, currencyPairs: currencies },
      { skipGlobalErrorNotify: true },
    );
    return data;
  },

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