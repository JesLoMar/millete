// ============================================================
// Tipos del módulo de investments — alineados con el esquema V5
// ============================================================

export type AssetReferenceKind = 'SHARED' | 'USER';

export interface AssetReference {
  kind: AssetReferenceKind;
  id: string;
}

// --- Enums alineados con la BD V5 ---

export type ActivityType =
  | 'BUY'
  | 'SELL'
  | 'DIVIDEND'
  | 'SPLIT'
  | 'DEPOSIT'
  | 'WITHDRAW'
  | 'OPENING_CASH'
  | 'OPENING_POSITION'
  | 'EXCHANGE';

/** Tipos de activo según CHECK de shared_assets / user_assets */
export type AssetType =
  | 'STOCK'
  | 'CRYPTO'
  | 'FUND'
  | 'ETF'
  | 'REAL_ESTATE'
  | 'OTHER';

/** Sectores del catálogo asset_sectors (V5) */
export type AssetSectorCode =
  | 'TECHNOLOGY'
  | 'HEALTHCARE'
  | 'FINANCIALS'
  | 'CONSUMER'
  | 'INDUSTRIALS'
  | 'ENERGY'
  | 'UTILITIES'
  | 'MATERIALS'
  | 'REAL_ESTATE'
  | 'COMMUNICATIONS'
  | 'OTHER';

/** Estado de holding según CHECK de holdings (V5): solo ACTIVE o SUPERSEDED */
export type HoldingStatus = 'ACTIVE' | 'SUPERSEDED';

export type LotSource = 'ACTIVITY' | 'HOLDING';

// --- Value objects ---

export interface Money {
  amount: number;
  currency: string;
}

export interface Sector {
  code: AssetSectorCode;
  displayName: string;
  custom: boolean;
}

// --- Activity details (unión discriminada) ---

export interface TradeActivityDetails {
  quantity: number;
  unitPrice: number;
  currency: string;
  fees?: number;
  settlement?: TradeSettlement;
}

export interface TradeSettlement {
  amount: number;
  currency: string;
  fxRate?: number;
}

export interface CashActivityDetails {
  amount: number;
  currency: string;
  fxRate?: number;
}

export interface DividendUnitsDetails {
  quantity: number;
  referenceUnitPrice: number;
  currency: string;
}

export interface SplitActivityDetails {
  ratio: number;
}

export interface ExchangeActivityDetails {
  fromCurrency: string;
  toCurrency: string;
  fromAmount: number;
  toAmount: number;
  fxRate: number;
}

export interface OpeningPositionDetails {
  quantity: number;
  acquisitionCost: number;
  currency: string;
}

export type ActivityDetails =
  | TradeActivityDetails
  | CashActivityDetails
  | DividendUnitsDetails
  | SplitActivityDetails
  | ExchangeActivityDetails
  | OpeningPositionDetails;

// --- Entities ---

export interface Activity {
  id: string;
  type: ActivityType;
  assetReference: AssetReference | null;
  assetName?: string | null;
  assetSymbol?: string | null;
  occurredAt: string;
  createdAt: string;
  modifiedAt: string;
  orderingKey: number;
  details: ActivityDetails;
  comment: string | null;
  linkedTransactionId: string | null;
}

export interface ActivityPage {
  content: Activity[];
  currentPage: number;
  totalPages: number;
  totalElements: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface Holding {
  id: string;
  assetReference: AssetReference;
  assetName: string;
  assetSymbol: string | null;
  quantity: number;
  acquisitionCost: Money;
  status: HoldingStatus;
  snapshotAt: string;
  supersededAt: string | null;
}

export interface ClosedLot {
  id: string;
  assetReference: AssetReference;
  assetName: string;
  assetSymbol: string | null;
  quantity: number;
  acquiredAt: string;
  closedAt: string;
  acquisitionCost: Money;
  proceeds: Money;
  realizedGain: Money;
  realizedGainLocal: number | null;
  holdingDays: number;
}

export interface CashBalance {
  currency: string;
  amount: number;
  estimated: boolean;
}

export interface Position {
  assetReference: AssetReference;
  assetName: string;
  assetSymbol: string | null;
  assetSector: AssetSectorCode | null;
  assetCurrency: string;
  quantity: number;
  acquisitionCost: Money | null;
  localCostBasis: number | null;
  marketValue: Money | null;
  localMarketValue: number | null;
  unrealizedGain: number | null;
  unrealizedGainPercentage: number | null;
  latestPrice: number | null;
  priceTimestamp: string | null;
  calculable: boolean;
  notCalculableReason: string | null;
  historyIncomplete: boolean;
  estimated: boolean;
}

export interface Portfolio {
  asOf: string;
  localCurrency: string;
  cashBalances: CashBalance[];
  positions: Position[];
  totalInLocalCurrency: number;
  estimated: boolean;
}

export type PerformanceStatus =
  | 'CALCULATED'
  | 'NOT_CALCULABLE'
  | 'INSUFFICIENT_DATA';

export interface Performance {
  status: PerformanceStatus;
  unavailableReason: string | null;
  from: string;
  to: string;
  currency: string;
  openingValue: number | null;
  endingValue: number | null;
  openingCapital: number | null;
  contributions: number | null;
  withdrawals: number | null;
  realizedGains: number | null;
  unrealizedPriceEffect: number | null;
  fxEffect: number | null;
  cashDividends: number | null;
  inKindDividends: number | null;
  reconciliationDifference: number | null;
  estimated: boolean;
}

export interface SharedAsset {
  id: string;
  stableCatalogId: string;
  name: string;
  symbol: string | null;
  type: AssetType;
  sector: Sector;
  currency: string;
}

export interface SharedAssetPage {
  content: SharedAsset[];
  currentPage: number;
  totalPages: number;
  totalElements: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface UserAsset {
  id: string;
  name: string;
  type: AssetType;
  sector: Sector | null;
  origin: string;
  currency: string;
  createdAt: string;
  modifiedAt: string;
}

export interface UserAssetPrice {
  id: string;
  unitPrice: number;
  currency: string;
  priceTimestamp: string;
}

// --- Tracking (alineado con investment_tracking_settings) ---

export interface InvestmentTrackingSettings {
  configured: boolean;
  trackingStartAt: string | null;
}

export interface ConfigureTrackingRequest {
  trackingStartAt: string;
}

// --- Health ---

export type HealthIssueSeverity = 'error' | 'warning' | 'info';

export interface HealthIssue {
  code: string;
  resourceId: string | null;
  severity: HealthIssueSeverity;
  message: string;
}

export interface HealthReport {
  checkedAt: string;
  issues: HealthIssue[];
}

// --- Market data ---

export interface MarketDataRefreshResult {
  refreshedAt: string;
  updatedPrices: number;
  updatedFxRates: number;
  failures: string[];
}

// --- Ledger snapshot ---

export interface InvestmentLedgerSnapshotResponse {
  snapshot: unknown;
}

export interface RestoreLedgerRequest {
  snapshot: unknown;
}

// --- Requests ---

export interface RegisterActivityRequest {
  type: ActivityType;
  assetReference?: AssetReference | null;
  occurredAt: string;
  details: ActivityDetails;
  comment?: string | null;
  idempotencyKey?: string;
}

export interface EditActivityRequest {
  occurredAt?: string;
  details?: ActivityDetails;
  comment?: string | null;
  reason?: string | null;
}

export interface CreateUserAssetRequest {
  name: string;
  type: AssetType;
  sectorCode?: AssetSectorCode | null;
  sectorDisplayName?: string | null;
  currency: string;
  origin?: string;
}

export interface UpdateUserAssetRequest {
  name?: string;
  type?: AssetType;
  sectorCode?: AssetSectorCode | null;
  sectorDisplayName?: string | null;
  currency?: string;
}

export interface RegisterUserAssetPriceRequest {
  unitPrice: number;
  currency: string;
  priceTimestamp?: string;
}

// --- Query params ---

export interface ListActivitiesParams {
  page?: number;
  size?: number;
  from?: string;
  to?: string;
  type?: ActivityType;
  assetReferenceKind?: AssetReferenceKind;
  assetId?: string;
}

export interface ListSharedAssetsParams {
  search?: string;
  sector?: AssetSectorCode;
  type?: AssetType;
  page?: number;
  size?: number;
}

export interface GetPerformanceParams {
  from: string;
  to: string;
}