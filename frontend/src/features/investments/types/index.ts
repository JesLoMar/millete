// ============================================================
// Tipos del módulo de inversiones — alineados con la API real
// ============================================================

export type AssetReferenceKind = 'SHARED' | 'USER';

export interface AssetReference {
  kind: AssetReferenceKind;
  id: string;
}

export type ActivityType =
  | 'BUY'
  | 'SELL'
  | 'DIVIDEND'
  | 'DEPOSIT'
  | 'WITHDRAW'
  | 'SPLIT'
  | 'EXCHANGE'
  | 'OPENING_CASH'
  | 'OPENING_POSITION';

export type CreatableActivityType =
  | 'BUY'
  | 'SELL'
  | 'DIVIDEND'
  | 'DEPOSIT'
  | 'WITHDRAW'
  | 'EXCHANGE'
  | 'SPLIT';

export type AssetType =
  | 'STOCK'
  | 'CRYPTO'
  | 'FUND'
  | 'ETF'
  | 'REAL_ESTATE'
  | 'OTHER';

export type AssetOrigin = 'PRE_EXISTING' | 'ACQUIRED';

export type CalculationStatus = 'CALCULABLE' | 'NOT_CALCULABLE';

export type HoldingStatus = 'ACTIVE' | 'SUPERSEDED';

/** Moneda en la que se introduce el precio de una compra/venta. */
export type SettlementCurrency = 'LOCAL' | 'ASSET';

export type HealthIssueSeverity = 'error' | 'warning' | 'info';

// --- Value objects ---

export interface Money {
  amount: number;
  currency: string;
}

export interface AppliedFxRate {
  baseCurrency: string;
  quoteCurrency: string;
  rate: number;
  source: string;
  timestamp: string;
}

export interface Sector {
  code: string;
  displayName: string;
  custom: boolean;
}

// --- Activity details (respuesta plana del backend) ---

export interface TradeActivityDetails {
  quantity: number;
  unitPrice: Money;
  settlementAmount: Money;
  appliedFxRate: AppliedFxRate | null;
}

export interface CashActivityDetails {
  amount: Money;
}

export interface SplitActivityDetails {
  ratio: number;
}

export interface ExchangeActivityDetails {
  origin: Money;
  destination: Money;
  appliedFxRate: AppliedFxRate | null;
}

export interface InKindDividendActivityDetails {
  quantity: number;
  referenceUnitPrice: Money;
}

export interface OpeningPositionActivityDetails {
  quantity: number;
  acquisitionCost: Money;
}

export interface ActivityDetails {
  trade: TradeActivityDetails | null;
  cash: CashActivityDetails | null;
  split: SplitActivityDetails | null;
  exchange: ExchangeActivityDetails | null;
  inKindDividend: InKindDividendActivityDetails | null;
  openingPosition: OpeningPositionActivityDetails | null;
}

// --- Entities ---

export interface Activity {
  id: string;
  type: ActivityType;
  assetReference: AssetReference | null;
  occurredAt: string;
  createdAt: string;
  modifiedAt: string;
  orderingKey: number;
  details: ActivityDetails;
  comment: string | null;
  linkedTransactionId: string | null;
}

export interface Holding {
  id: string;
  userId: string;
  assetReference: AssetReference;
  snapshotAt: string;
  quantity: number;
  acquisitionCost: number;
  currency: string;
  status: HoldingStatus;
  createdAt: string;
  supersededAt: string | null;
}

export interface ClosedLot {
  lotId: string;
  assetReference: AssetReference;
  quantity: number;
  costBasis: number;
  costBasisCurrency: string;
  proceeds: number;
  proceedsCurrency: string;
  realizedGain: number;
  realizedGainCurrency: string;
  openedAt: string;
  closedAt: string;
  estimated: boolean;
  historyIncomplete: boolean;
}

export type CashBalances = Record<string, number>;

export interface Position {
  assetReference: AssetReference;
  quantity: number;
  costBasis: number | null;
  marketValue: number | null;
  unrealizedGain: number | null;
  valuationCurrency: string;
  calculationStatus: CalculationStatus;
  estimated: boolean;
  historyIncomplete: boolean;
  unavailableReason: string | null;
}

export interface Portfolio {
  asOf: string;
  localCurrency: string;
  cashBalances: CashBalances;
  positions: Position[];
  totalInLocalCurrency: number;
  estimated: boolean;
}

export interface Performance {
  from: string;
  to: string;
  currency: string;
  openingValue: number | null;
  endingValue: number | null;
  portfolioChange: number | null;
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
  status: CalculationStatus;
  unavailableReason: string | null;
}

export interface SharedAsset {
  id: string;
  stableCatalogId: string;
  name: string;
  symbol: string | null;
  type: AssetType;
  sector: Sector | null;
  currency: string;
  createdAt: string;
  modifiedAt: string;
}

export interface UserAsset {
  id: string;
  userId: string;
  name: string;
  type: AssetType;
  sector: Sector | null;
  origin: AssetOrigin;
  currency: string;
  createdAt: string;
  modifiedAt: string;
}

export interface UserAssetPrice {
  id: string;
  userAssetId: string;
  unitPrice: number;
  currency: string;
  timestamp: string;
}

export interface InvestmentTrackingSettings {
  configured: boolean;
  trackingStartAt: string | null;
}

// --- Health ---

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

export interface AssetPrice {
  id: string;
  sharedAssetId: string;
  timestamp: string;
  open: number | null;
  high: number | null;
  low: number | null;
  close: number | null;
  adjustedClose: number | null;
  valuationPrice: number;
  volume: number | null;
  currency: string;
  source: string;
  fetchedAt: string;
}

export interface FxRate {
  id: string;
  baseCurrency: string;
  quoteCurrency: string;
  timestamp: string;
  rate: number;
  source: string;
  fetchedAt: string;
}

// --- Ledger snapshot ---

export interface InvestmentLedgerSnapshot {
  version: number;
  trackingStartAt: string;
  userAssets: unknown[];
  userAssetPrices: unknown[];
  holdings: unknown[];
  activities: unknown[];
  audits: unknown[];
}

// --- Paginación ---

export interface PaginatedResponse<T> {
  content: T[];
  currentPage: number;
  totalPages: number;
  totalElements: number;
  size: number;
  first: boolean;
  last: boolean;
}

export type ActivityPage = PaginatedResponse<Activity>;
export type HoldingPage = PaginatedResponse<Holding>;
export type ClosedLotPage = PaginatedResponse<ClosedLot>;
export type AssetPricePage = PaginatedResponse<AssetPrice>;
export type FxRatePage = PaginatedResponse<FxRate>;

// --- Requests: activities ---

export interface BuyActivityRequest {
  occurredAt?: string;
  assetReference: AssetReference;
  quantity: number;
  unitPrice: number;
  settlementCurrency: SettlementCurrency;
  comment?: string | null;
}

export type SellActivityRequest = BuyActivityRequest;

export interface CashDividendActivityRequest {
  occurredAt?: string;
  assetReference?: AssetReference | null;
  amount: number;
  currency: string;
  comment?: string | null;
}

export interface InKindDividendActivityRequest {
  occurredAt?: string;
  assetReference: AssetReference;
  quantity: number;
  comment?: string | null;
}

export interface DepositActivityRequest {
  occurredAt?: string;
  amount: number;
  currency: string;
  comment?: string | null;
}

export type WithdrawalActivityRequest = DepositActivityRequest;

export interface ExchangeActivityRequest {
  occurredAt?: string;
  amountOrigin: number;
  currencyOrigin: string;
  currencyDestination: string;
  comment?: string | null;
}

export interface SplitActivityRequest {
  occurredAt?: string;
  assetReference: AssetReference;
  ratio: number;
  comment?: string | null;
}

/**
 * Unión discriminada que usa el formulario para enviar cualquier operación
 * al endpoint que le corresponde. La clave de idempotencia viaja en el payload
 * para que un reintento tras un error de red no duplique la operación.
 */
export type RegisterActivityRequest =
  | ({ type: 'BUY'; idempotencyKey: string } & BuyActivityRequest)
  | ({ type: 'SELL'; idempotencyKey: string } & SellActivityRequest)
  | ({ type: 'DEPOSIT'; idempotencyKey: string } & DepositActivityRequest)
  | ({ type: 'WITHDRAW'; idempotencyKey: string } & WithdrawalActivityRequest)
  | ({
      type: 'DIVIDEND';
      dividendMode: 'CASH';
      idempotencyKey: string;
    } & CashDividendActivityRequest)
  | ({
      type: 'DIVIDEND';
      dividendMode: 'IN_KIND';
      idempotencyKey: string;
    } & InKindDividendActivityRequest)
  | ({ type: 'EXCHANGE'; idempotencyKey: string } & ExchangeActivityRequest)
  | ({ type: 'SPLIT'; idempotencyKey: string } & SplitActivityRequest);

export type EditActivityDetailsType =
  | 'BUY_SELL'
  | 'CASH_DIVIDEND'
  | 'IN_KIND_DIVIDEND'
  | 'EXCHANGE'
  | 'SPLIT';

export interface EditActivityDetails {
  type: EditActivityDetailsType;
  quantity?: number;
  unitPrice?: number;
  settlementCurrency?: SettlementCurrency;
  amount?: number;
  amountOrigin?: number;
  ratio?: number;
  comment?: string | null;
}

export interface EditActivityRequest {
  occurredAt: string;
  details: EditActivityDetails;
  reason: string;
}

// --- Requests: holdings ---

export interface CreateHoldingRequest {
  assetReference: AssetReference;
  snapshotAt: string;
  quantity: number;
  acquisitionCost: number;
}

export type HistoricalActivityType =
  | 'BUY'
  | 'SELL'
  | 'CASH_DIVIDEND'
  | 'IN_KIND_DIVIDEND'
  | 'SPLIT';

export interface HistoricalActivityRequest {
  occurredAt: string;
  type: HistoricalActivityType;
  quantity?: number;
  unitPrice?: number;
  settlementCurrency?: SettlementCurrency;
  amount?: number;
  currency?: string;
  ratio?: number;
  comment?: string | null;
}

export interface ReplaceHoldingHistoryRequest {
  history: HistoricalActivityRequest[];
}

// --- Requests: assets ---

export interface CreateUserAssetRequest {
  name: string;
  type: AssetType;
  sector: string;
  origin: AssetOrigin;
  initialPrice: number;
}

export interface UpdateUserAssetRequest {
  name: string;
  sector: string;
}

export interface RegisterUserAssetPriceRequest {
  unitPrice: number;
}

// --- Requests: tracking / market data / ledger ---

export interface ConfigureTrackingRequest {
  trackingStartAt: string;
}

export interface RefreshMarketDataRequest {
  from: string;
  to: string;
  currencyPairs: string[];
}

// --- Query params ---

export interface ListActivitiesParams {
  page?: number;
  size?: number;
  type?: ActivityType;
  currency?: string;
  from?: string;
  to?: string;
  assetKind?: AssetReferenceKind;
  assetId?: string;
}

export interface ListHoldingsParams {
  page?: number;
  size?: number;
  assetKind?: AssetReferenceKind;
  assetId?: string;
}

export interface PeriodParams {
  from: string;
  to: string;
}

export interface AssetPricesParams extends PeriodParams {
  page?: number;
  size?: number;
}

export interface FxRatesParams extends PeriodParams {
  baseCurrency: string;
  quoteCurrency: string;
  page?: number;
  size?: number;
}

// --- UI helpers ---

export interface AssetCatalogEntry {
  reference: AssetReference;
  name: string;
  symbol: string | null;
  type: AssetType | null;
  sector: Sector | null;
  currency: string | null;
  isUserAsset: boolean;
}
