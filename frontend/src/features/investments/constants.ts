import type {
  ActivityType,
  AssetOrigin,
  AssetType,
  HistoricalActivityType,
} from './types';

export const INVESTMENT_QUERY_KEYS = {
  portfolio: ['investmentPortfolio'] as const,
  holdings: ['investmentHoldings'] as const,
  activities: ['investmentActivities'] as const,
  performance: ['investmentPerformance'] as const,
  closedLots: ['investmentClosedLots'] as const,
  sharedAssets: ['investmentSharedAssets'] as const,
  userAssets: ['investmentUserAssets'] as const,
  assetPrices: ['investmentAssetPrices'] as const,
  fxRates: ['investmentFxRates'] as const,
  tracking: ['investmentTracking'] as const,
  health: ['investmentHealth'] as const,
  sectors: ['investmentSectors'] as const,
  ledger: ['investmentLedger'] as const,
} as const;

/** Prefijos de query key para invalidación masiva tras mutaciones. */
export const ALL_INVESTMENT_QUERY_PREFIXES = [
  'investmentPortfolio',
  'investmentHoldings',
  'investmentActivities',
  'investmentPerformance',
  'investmentClosedLots',
  'investmentSharedAssets',
  'investmentUserAssets',
  'investmentAssetPrices',
  'investmentFxRates',
  'investmentTracking',
  'investmentHealth',
  'investmentLedger',
] as const;

/** Tipos que se pueden crear desde la interfaz. */
export const CREATABLE_ACTIVITY_TYPES: ActivityType[] = [
  'BUY',
  'SELL',
  'DIVIDEND',
  'DEPOSIT',
  'WITHDRAW',
  'EXCHANGE',
  'SPLIT',
];

export const ACTIVITY_TYPES: ActivityType[] = [
  'BUY',
  'SELL',
  'DIVIDEND',
  'DEPOSIT',
  'WITHDRAW',
  'EXCHANGE',
  'SPLIT',
  'OPENING_CASH',
  'OPENING_POSITION',
];

export const HISTORICAL_ACTIVITY_TYPES: HistoricalActivityType[] = [
  'BUY',
  'SELL',
  'CASH_DIVIDEND',
  'IN_KIND_DIVIDEND',
  'SPLIT',
];

export const ASSET_TYPES: AssetType[] = [
  'STOCK',
  'CRYPTO',
  'FUND',
  'ETF',
  'REAL_ESTATE',
  'OTHER',
];

export const ASSET_ORIGINS: AssetOrigin[] = ['PRE_EXISTING', 'ACQUIRED'];

/**
 * Divisas habituales. El backend acepta cualquier código ISO de 3 letras;
 * los formularios permiten escribirlo manualmente.
 */
export const CURRENCIES = [
  'EUR',
  'USD',
  'GBP',
  'CHF',
  'JPY',
  'CAD',
  'AUD',
  'NZD',
  'CNY',
  'HKD',
  'SGD',
  'SEK',
  'NOK',
  'DKK',
  'PLN',
  'CZK',
  'MXN',
  'BRL',
  'INR',
  'KRW',
  'ZAR',
] as const;

export const MAX_COMMENT_LENGTH = 500;
export const MAX_REASON_LENGTH = 500;
export const MAX_PAGE_SIZE = 200;
export const DEFAULT_PAGE_SIZE = 20;
export const ASSET_PAGE_SIZE = 10;
export const CLOSED_LOTS_PAGE_SIZE = 20;

export const PERFORMANCE_PERIODS = [
  { key: '1m', labelKey: 'performance.periods.oneMonth', days: 30 },
  { key: '3m', labelKey: 'performance.periods.threeMonths', days: 90 },
  { key: '6m', labelKey: 'performance.periods.sixMonths', days: 180 },
  { key: 'ytd', labelKey: 'performance.periods.yearToDate', days: 0 },
  { key: '1y', labelKey: 'performance.periods.oneYear', days: 365 },
  { key: 'all', labelKey: 'performance.periods.all', days: 0 },
] as const;

export type PerformancePeriodKey =
  (typeof PERFORMANCE_PERIODS)[number]['key'];

export type PerformancePeriod = (typeof PERFORMANCE_PERIODS)[number];
