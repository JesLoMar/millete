import type { ActivityType, AssetSectorCode, AssetType } from './types';

export const INVESTMENT_QUERY_KEYS = {
  portfolio: ['investmentPortfolio'] as const,
  portfolioAt: (asOf: string) => ['investmentPortfolioAt', asOf] as const,
  holdings: ['investmentHoldings'] as const,
  activities: ['investmentActivities'] as const,
  performance: ['investmentPerformance'] as const,
  closedLots: ['investmentClosedLots'] as const,
  sharedAssets: ['investmentSharedAssets'] as const,
  userAssets: ['investmentUserAssets'] as const,
  userAssetPrices: (assetId: string) =>
    ['investmentUserAssetPrices', assetId] as const,
  tracking: ['investmentTracking'] as const,
  health: ['investmentHealth'] as const,
  sectors: ['investmentSectors'] as const,
} as const;

/** Prefijos de query key para invalidación masiva tras mutaciones */
export const ALL_INVESTMENT_QUERY_PREFIXES = [
  'investmentPortfolio',
  'investmentHoldings',
  'investmentActivities',
  'investmentPerformance',
  'investmentClosedLots',
  'investmentUserAssets',
  'investmentUserAssetPrices',
  'investmentTracking',
  'investmentHealth',
] as const;

export const ACTIVITY_TYPES: ActivityType[] = [
  'BUY',
  'SELL',
  'DIVIDEND',
  'DEPOSIT',
  'WITHDRAW',
  'OPENING_CASH',
  'OPENING_POSITION',
  'SPLIT',
  'EXCHANGE',
];

export const PRIMARY_ACTIVITY_TYPES: ActivityType[] = [
  'BUY',
  'SELL',
  'DIVIDEND',
  'DEPOSIT',
  'WITHDRAW',
];

export const ADVANCED_ACTIVITY_TYPES: ActivityType[] = [
  'SPLIT',
  'EXCHANGE',
  'OPENING_POSITION',
  'OPENING_CASH',
];

/** Tipos de activo según CHECK de la BD V5 */
export const ASSET_TYPES: AssetType[] = [
  'STOCK',
  'CRYPTO',
  'FUND',
  'ETF',
  'REAL_ESTATE',
  'OTHER',
];

/** Sectores del catálogo asset_sectors (V5) */
export const ASSET_SECTORS: AssetSectorCode[] = [
  'TECHNOLOGY',
  'HEALTHCARE',
  'FINANCIALS',
  'CONSUMER',
  'INDUSTRIALS',
  'ENERGY',
  'UTILITIES',
  'MATERIALS',
  'REAL_ESTATE',
  'COMMUNICATIONS',
  'OTHER',
];

export const CURRENCIES = [
  'EUR',
  'USD',
  'GBP',
  'CHF',
  'JPY',
  'CAD',
  'AUD',
] as const;

export const MAX_COMMENT_LENGTH = 500;
export const MAX_PAGE_SIZE = 200;
export const DEFAULT_PAGE_SIZE = 20;

export const PERFORMANCE_PERIODS = [
  { key: '1m', labelKey: 'periods.oneMonth', days: 30 },
  { key: '3m', labelKey: 'periods.threeMonths', days: 90 },
  { key: '6m', labelKey: 'periods.sixMonths', days: 180 },
  { key: 'ytd', labelKey: 'periods.yearToDate', days: 0 },
  { key: '1y', labelKey: 'periods.oneYear', days: 365 },
  { key: 'all', labelKey: 'periods.all', days: 0 },
] as const;

export type PerformancePeriodKey =
  (typeof PERFORMANCE_PERIODS)[number]['key'];