import axios from 'axios';
import { apiClient } from '@/shared/api/axiosClient';
import type {
  Activity,
  ActivityPage,
  AssetPricePage,
  AssetPricesParams,
  CashBalances,
  ClosedLotPage,
  ConfigureTrackingRequest,
  CreateHoldingRequest,
  CreateUserAssetRequest,
  EditActivityRequest,
  FxRatePage,
  FxRatesParams,
  HealthReport,
  Holding,
  HoldingPage,
  InvestmentLedgerSnapshot,
  InvestmentTrackingSettings,
  ListActivitiesParams,
  ListHoldingsParams,
  Performance,
  PeriodParams,
  Portfolio,
  RefreshMarketDataRequest,
  RegisterActivityRequest,
  RegisterUserAssetPriceRequest,
  ReplaceHoldingHistoryRequest,
  Sector,
  SharedAsset,
  UpdateUserAssetRequest,
  UserAsset,
  UserAssetPrice,
} from '../types';

const BASE = '/investments';

interface CashBalancesResponse {
  balances: CashBalances;
}

function activityIdempotencyKey(
  payload: RegisterActivityRequest,
): string {
  return payload.idempotencyKey || crypto.randomUUID();
}

function withoutIdempotency<T extends { idempotencyKey?: string }>(
  payload: T,
): Omit<T, 'idempotencyKey'> {
  const rest = { ...payload };
  delete rest.idempotencyKey;
  return rest;
}

function withoutDividendMode<T extends { dividendMode?: string }>(
  payload: T,
): Omit<T, 'dividendMode'> {
  const rest = { ...payload };
  delete rest.dividendMode;
  return rest;
}

async function postActivity(
  path: string,
  body: unknown,
  idempotencyKey: string,
): Promise<Activity> {
  const { data } = await apiClient.post<Activity>(
    `${BASE}/activities/${path}`,
    body,
    {
      headers: { 'Idempotency-Key': idempotencyKey },
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
    const { data } = await apiClient.get<Portfolio>(
      `${BASE}/portfolio`,
      { params: { asOf }, signal },
    );
    return data;
  },

  getInvestmentCash: async (
    asOf: string,
    signal?: AbortSignal,
  ): Promise<CashBalances> => {
    const { data } = await apiClient.get<CashBalancesResponse>(
      `${BASE}/cash`,
      { params: { asOf }, signal },
    );
    return data.balances;
  },

  listHoldings: async (
    params: ListHoldingsParams = {},
    signal?: AbortSignal,
  ): Promise<HoldingPage> => {
    const { data } = await apiClient.get<HoldingPage>(
      `${BASE}/holdings`,
      { params, signal },
    );
    return data;
  },

  getActiveHolding: async (
    assetKind: string,
    assetId: string,
    signal?: AbortSignal,
  ): Promise<Holding> => {
    const { data } = await apiClient.get<Holding>(
      `${BASE}/holdings/active`,
      { params: { assetKind, assetId }, signal },
    );
    return data;
  },

  createHolding: async (
    payload: CreateHoldingRequest,
  ): Promise<Holding> => {
    const { data } = await apiClient.post<Holding>(
      `${BASE}/holdings`,
      payload,
      { skipGlobalErrorNotify: true },
    );
    return data;
  },

  replaceHoldingHistory: async (
    holdingId: string,
    payload: ReplaceHoldingHistoryRequest,
  ): Promise<Activity[]> => {
    const { data } = await apiClient.post<Activity[]>(
      `${BASE}/holdings/${holdingId}/history`,
      payload,
      { skipGlobalErrorNotify: true },
    );
    return data;
  },

  getClosedLots: async (
    from: string,
    to: string,
    page: number = 0,
    size: number = 50,
    signal?: AbortSignal,
  ): Promise<ClosedLotPage> => {
    const { data } = await apiClient.get<ClosedLotPage>(
      `${BASE}/lots/closed`,
      { params: { from, to, page, size }, signal },
    );
    return data;
  },

  listActivities: async (
    params: ListActivitiesParams = {},
    signal?: AbortSignal,
  ): Promise<ActivityPage> => {
    const { data } = await apiClient.get<ActivityPage>(
      `${BASE}/activities`,
      { params, signal },
    );
    return data;
  },

  /**
   * Envía cualquier operación creable al endpoint correspondiente.
   * El backend devuelve 201 con la Activity creada.
   */
  registerActivity: async (
    payload: RegisterActivityRequest,
  ): Promise<Activity> => {
    const idempotencyKey = activityIdempotencyKey(payload);

    switch (payload.type) {
      case 'BUY':
        return postActivity(
          'buy',
          withoutIdempotency(payload),
          idempotencyKey,
        );
      case 'SELL':
        return postActivity(
          'sell',
          withoutIdempotency(payload),
          idempotencyKey,
        );
      case 'DEPOSIT':
        return postActivity(
          'deposits',
          withoutIdempotency(payload),
          idempotencyKey,
        );
      case 'WITHDRAW':
        return postActivity(
          'withdrawals',
          withoutIdempotency(payload),
          idempotencyKey,
        );
      case 'DIVIDEND':
        if (payload.dividendMode === 'CASH') {
          return postActivity(
            'dividends',
            withoutDividendMode(withoutIdempotency(payload)),
            idempotencyKey,
          );
        }

        return postActivity(
          'dividends/in-kind',
          withoutDividendMode(withoutIdempotency(payload)),
          idempotencyKey,
        );
      case 'EXCHANGE':
        return postActivity(
          'exchanges',
          withoutIdempotency(payload),
          idempotencyKey,
        );
      case 'SPLIT':
        return postActivity(
          'splits',
          withoutIdempotency(payload),
          idempotencyKey,
        );
    }
  },

  editActivity: async (
    id: string,
    payload: EditActivityRequest,
  ): Promise<Activity> => {
    const { data } = await apiClient.patch<Activity>(
      `${BASE}/activities/${id}`,
      payload,
      { skipGlobalErrorNotify: true },
    );
    return data;
  },

  getPerformance: async (
    params: PeriodParams,
    signal?: AbortSignal,
  ): Promise<Performance> => {
    const { data } = await apiClient.get<Performance>(
      `${BASE}/performance`,
      { params, signal },
    );
    return data;
  },

  // ── Assets ────────────────────────────────────────────────────────────

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

  getSharedAsset: async (
    id: string,
    signal?: AbortSignal,
  ): Promise<SharedAsset> => {
    const { data } = await apiClient.get<SharedAsset>(
      `${BASE}/shared-assets/${id}`,
      { signal },
    );
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

  getUserAsset: async (
    id: string,
    signal?: AbortSignal,
  ): Promise<UserAsset> => {
    const { data } = await apiClient.get<UserAsset>(
      `${BASE}/user-assets/${id}`,
      { signal },
    );
    return data;
  },

  createUserAsset: async (
    payload: CreateUserAssetRequest,
  ): Promise<UserAsset> => {
    const { data } = await apiClient.post<UserAsset>(
      `${BASE}/user-assets`,
      payload,
      { skipGlobalErrorNotify: true },
    );
    return data;
  },

  updateUserAsset: async (
    id: string,
    payload: UpdateUserAssetRequest,
  ): Promise<UserAsset> => {
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
  ): Promise<UserAssetPrice> => {
    const { data } = await apiClient.post<UserAssetPrice>(
      `${BASE}/user-assets/${assetId}/prices`,
      payload,
      { skipGlobalErrorNotify: true },
    );
    return data;
  },

  getSectors: async (signal?: AbortSignal): Promise<Sector[]> => {
    const { data } = await apiClient.get<Sector[]>(
      `${BASE}/sectors`,
      { signal },
    );
    return data;
  },

  // ── Market data ───────────────────────────────────────────────────────

  getAssetPrices: async (
    sharedAssetId: string,
    params: AssetPricesParams,
    signal?: AbortSignal,
  ): Promise<AssetPricePage> => {
    const { data } = await apiClient.get<AssetPricePage>(
      `${BASE}/shared-assets/${sharedAssetId}/prices`,
      { params, signal },
    );
    return data;
  },

  getFxRates: async (
    params: FxRatesParams,
    signal?: AbortSignal,
  ): Promise<FxRatePage> => {
    const { data } = await apiClient.get<FxRatePage>(
      `${BASE}/fx-rates`,
      { params, signal },
    );
    return data;
  },

  refreshMarketData: async (
    payload: RefreshMarketDataRequest,
  ): Promise<void> => {
    await apiClient.post(`${BASE}/market-data/refresh`, payload, {
      skipGlobalErrorNotify: true,
      // El refresh consulta proveedores externos y puede tardar varios minutos.
      timeout: 5 * 60 * 1000,
    });
  },

  // ── Tracking / health ─────────────────────────────────────────────────

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

  configureTracking: async (
    payload: ConfigureTrackingRequest,
  ): Promise<void> => {
    await apiClient.post(`${BASE}/tracking`, payload, {
      skipGlobalErrorNotify: true,
    });
  },

  getHealth: async (signal?: AbortSignal): Promise<HealthReport> => {
    const { data } = await apiClient.get<HealthReport>(
      `${BASE}/health`,
      { signal },
    );
    return data;
  },

  // ── Ledger snapshot ───────────────────────────────────────────────────

  getLedgerSnapshot: async (
    signal?: AbortSignal,
  ): Promise<InvestmentLedgerSnapshot> => {
    const { data } = await apiClient.get<InvestmentLedgerSnapshot>(
      `${BASE}/ledger/snapshot`,
      { signal },
    );
    return data;
  },

  restoreLedger: async (
    snapshot: InvestmentLedgerSnapshot,
  ): Promise<void> => {
    await apiClient.post(`${BASE}/ledger/restore`, snapshot, {
      skipGlobalErrorNotify: true,
    });
  },
};

// Reexport local para que los componentes no dependan del tipo del servicio.
export type { Holding };
