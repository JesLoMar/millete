import type {
  Activity,
  ActivityDetails,
  ActivityType,
  AssetReference,
  CashActivityDetails,
  DividendUnitsDetails,
  ExchangeActivityDetails,
  OpeningPositionDetails,
  SplitActivityDetails,
  TradeActivityDetails,
} from './types';

export function formatActivityDate(iso: string | null | undefined): string {
  if (!iso) return '—';
  try {
    const date = new Date(iso);
    return date.toLocaleDateString(undefined, {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
    });
  } catch {
    return iso.slice(0, 10);
  }
}

export function formatActivityDateTime(
  iso: string | null | undefined,
): string {
  if (!iso) return '—';
  try {
    const date = new Date(iso);
    const datePart = date.toLocaleDateString(undefined, {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
    });
    const timePart = date.toLocaleTimeString(undefined, {
      hour: '2-digit',
      minute: '2-digit',
    });
    return `${datePart} ${timePart}`;
  } catch {
    return iso;
  }
}

export function describeActivityDetails(
  type: ActivityType,
  details: ActivityDetails,
): string {
  switch (type) {
    case 'BUY':
    case 'SELL': {
      const trade = details as TradeActivityDetails;
      return `${trade.quantity} × ${formatNumber(trade.unitPrice)} ${trade.currency}`;
    }
    case 'DIVIDEND': {
      if (isDividendUnits(details)) {
        return `${details.quantity} acciones`;
      }
      const cash = details as CashActivityDetails;
      return `${formatNumber(cash.amount)} ${cash.currency}`;
    }
    case 'DEPOSIT':
    case 'WITHDRAW':
    case 'OPENING_CASH': {
      const cash = details as CashActivityDetails;
      return `${formatNumber(cash.amount)} ${cash.currency}`;
    }
    case 'SPLIT': {
      const split = details as SplitActivityDetails;
      return `${split.ratio}:1`;
    }
    case 'EXCHANGE': {
      const exchange = details as ExchangeActivityDetails;
      return `${formatNumber(exchange.fromAmount)} ${exchange.fromCurrency} → ${formatNumber(exchange.toAmount)} ${exchange.toCurrency}`;
    }
    case 'OPENING_POSITION': {
      const opening = details as OpeningPositionDetails;
      return `${opening.quantity} × ${formatNumber(opening.acquisitionCost)} ${opening.currency}`;
    }
    default:
      return '';
  }
}

function isDividendUnits(
  details: ActivityDetails,
): details is DividendUnitsDetails {
  return 'referenceUnitPrice' in details;
}

export function isCashActivity(type: ActivityType): boolean {
  return (
    type === 'DEPOSIT' ||
    type === 'WITHDRAW' ||
    type === 'OPENING_CASH' ||
    type === 'DIVIDEND'
  );
}

export function getCashSign(type: ActivityType): 1 | -1 {
  return type === 'WITHDRAW' ? -1 : 1;
}

export function formatNumber(value: number | null | undefined): string {
  if (value == null || Number.isNaN(value)) return '—';
  return new Intl.NumberFormat(undefined, {
    maximumFractionDigits: 2,
  }).format(value);
}

export function formatQuantity(value: number | null | undefined): string {
  if (value == null || Number.isNaN(value)) return '—';
  return new Intl.NumberFormat(undefined, {
    maximumFractionDigits: 6,
  }).format(value);
}

export function formatPercentage(
  value: number | null | undefined,
): string {
  if (value == null || Number.isNaN(value)) return '—';
  const sign = value > 0 ? '+' : '';
  return `${sign}${value.toFixed(2)}%`;
}

export function assetReferenceToParams(reference: AssetReference): {
  assetReferenceKind: string;
  assetId: string;
} {
  return {
    assetReferenceKind: reference.kind,
    assetId: reference.id,
  };
}

export function getPerformanceRange(
  periodKey: string,
): { from: string; to: string } {
  const now = new Date();
  const to = now.toISOString();
  let from: Date;

  switch (periodKey) {
    case '1m':
      from = new Date(now);
      from.setDate(from.getDate() - 30);
      break;
    case '3m':
      from = new Date(now);
      from.setDate(from.getDate() - 90);
      break;
    case '6m':
      from = new Date(now);
      from.setDate(from.getDate() - 180);
      break;
    case 'ytd':
      from = new Date(now.getFullYear(), 0, 1);
      break;
    case '1y':
      from = new Date(now);
      from.setFullYear(from.getFullYear() - 1);
      break;
    case 'all':
      from = new Date(2000, 0, 1);
      break;
    default:
      from = new Date(now);
      from.setDate(from.getDate() - 30);
  }

  return { from: from.toISOString(), to };
}

export function getActivityAssetLabel(activity: Activity): string {
  return activity.assetName || activity.assetSymbol || '—';
}