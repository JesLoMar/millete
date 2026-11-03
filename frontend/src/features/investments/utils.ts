import { formatCurrency, formatNumber } from '@/shared/utils/i18nFormat';
import { PERFORMANCE_PERIODS, type PerformancePeriodKey } from './constants';
import type {
  Activity,
  AssetReference,
  AssetType,
  Position,
} from './types';

export function assetKey(reference: AssetReference): string {
  return `${reference.kind}:${reference.id}`;
}

export function isSameAssetReference(
  left: AssetReference | null | undefined,
  right: AssetReference | null | undefined,
): boolean {
  if (!left || !right) {
    return false;
  }

  return left.kind === right.kind && left.id === right.id;
}

export function formatQuantity(value: number | null | undefined): string {
  if (value == null || !Number.isFinite(value)) {
    return '—';
  }

  return formatNumber(value, {
    maximumFractionDigits: 8,
  });
}

export function formatPrice(value: number | null | undefined): string {
  if (value == null || !Number.isFinite(value)) {
    return '—';
  }

  return formatNumber(value, {
    minimumFractionDigits: 2,
    maximumFractionDigits: 6,
  });
}

export function formatPercent(
  value: number | null | undefined,
): string {
  if (value == null || !Number.isFinite(value)) {
    return '—';
  }

  return `${formatNumber(value, {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  })}%`;
}

export function formatSignedCurrency(
  value: number | null | undefined,
  currency: string,
): string {
  if (value == null || !Number.isFinite(value)) {
    return '—';
  }

  const formatted = formatCurrency(Math.abs(value), currency);
  return value >= 0 ? `+${formatted}` : `-${formatted}`;
}

const dateTimeFormatter = new Intl.DateTimeFormat(undefined, {
  dateStyle: 'medium',
  timeStyle: 'short',
});

const dateFormatter = new Intl.DateTimeFormat(undefined, {
  dateStyle: 'medium',
});

export function formatDateTime(
  value: string | null | undefined,
): string {
  if (!value) {
    return '—';
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return '—';
  }

  return dateTimeFormatter.format(date);
}

export function formatDate(value: string | null | undefined): string {
  if (!value) {
    return '—';
  }

  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return '—';
  }

  return dateFormatter.format(date);
}

function pad(value: number): string {
  return String(value).padStart(2, '0');
}

export function toDateTimeInputValue(
  iso: string | null | undefined,
): string {
  if (!iso) {
    return '';
  }

  const date = new Date(iso);

  if (Number.isNaN(date.getTime())) {
    return '';
  }

  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(
    date.getDate(),
  )}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export function toDateInputValue(
  iso: string | null | undefined,
): string {
  if (!iso) {
    return '';
  }

  const date = new Date(iso);

  if (Number.isNaN(date.getTime())) {
    return '';
  }

  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(
    date.getDate(),
  )}`;
}

export function nowDateTimeInputValue(): string {
  return toDateTimeInputValue(new Date().toISOString());
}

export function todayDateInputValue(): string {
  return toDateInputValue(new Date().toISOString());
}

/**
 * Indica si un valor de fecha/hora ya ha pasado. Se aísla aquí para que los
 * componentes no lean el reloj durante el render.
 */
export function isFutureDateTime(
  value: string | null | undefined,
): boolean {
  if (!value) {
    return false;
  }

  const timestamp = new Date(value).getTime();

  return Number.isFinite(timestamp) && timestamp > Date.now();
}

export function fromDateTimeInputValue(value: string): string {
  return new Date(value).toISOString();
}

export function fromDateInputValue(value: string): string {
  const [year, month, day] = value
    .split('-')
    .map((part) => Number(part));

  return new Date(year, month - 1, day).toISOString();
}

export function endOfDateInputValue(value: string): string {
  const [year, month, day] = value
    .split('-')
    .map((part) => Number(part));

  return new Date(
    year,
    month - 1,
    day,
    23,
    59,
    59,
    999,
  ).toISOString();
}

export function isValidCurrencyCode(
  value: string | null | undefined,
): boolean {
  return Boolean(value && /^[A-Z]{3}$/.test(value));
}

/**
 * La performance nunca debe calcularse antes del inicio del tracking: el
 * backend rechaza periodos anteriores y las métricas dejarían de ser
 * comparables.
 */
export function clampPerformanceRange(
  range: { from: string; to: string },
  trackingStartAt: string | null | undefined,
): { from: string; to: string } {
  if (!trackingStartAt) {
    return range;
  }

  const trackingStart = new Date(trackingStartAt);

  if (Number.isNaN(trackingStart.getTime())) {
    return range;
  }

  const from = new Date(range.from);

  if (
    Number.isNaN(from.getTime()) ||
    from.getTime() < trackingStart.getTime()
  ) {
    return { from: trackingStartAt, to: range.to };
  }

  return range;
}

export function getPerformanceRange(
  period: PerformancePeriodKey,
  trackingStartAt: string | null | undefined,
  now: Date = new Date(),
): { from: string; to: string } {
  const to = now.toISOString();

  if (period === 'all') {
    const from = trackingStartAt
      ? new Date(trackingStartAt)
      : new Date(now.getFullYear(), 0, 1);

    return { from: from.toISOString(), to };
  }

  if (period === 'ytd') {
    return {
      from: new Date(now.getFullYear(), 0, 1).toISOString(),
      to,
    };
  }

  const definition = PERFORMANCE_PERIODS.find(
    (item) => item.key === period,
  );

  const days = definition?.days ?? 30;

  return {
    from: new Date(
      now.getTime() - days * 24 * 60 * 60 * 1000,
    ).toISOString(),
    to,
  };
}

export function getMarketDataDefaultRange(days = 30): {
  from: string;
  to: string;
} {
  const to = new Date();
  const from = new Date(
    to.getTime() - days * 24 * 60 * 60 * 1000,
  );

  return {
    from: from.toISOString(),
    to: to.toISOString(),
  };
}

export function downloadJson(
  data: unknown,
  filename: string,
): void {
  const blob = new Blob([JSON.stringify(data, null, 2)], {
    type: 'application/json',
  });
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');

  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}

export async function readJsonFile<T>(file: File): Promise<T> {
  const text = await file.text();
  return JSON.parse(text) as T;
}

export function isActivityEditable(activity: Activity): boolean {
  return (
    activity.type === 'BUY' ||
    activity.type === 'SELL' ||
    activity.type === 'DIVIDEND' ||
    activity.type === 'EXCHANGE' ||
    activity.type === 'SPLIT'
  );
}

export function isCashDividend(activity: Activity): boolean {
  return activity.type === 'DIVIDEND' && activity.details.cash != null;
}

export function isInKindDividend(activity: Activity): boolean {
  return (
    activity.type === 'DIVIDEND' &&
    activity.details.inKindDividend != null
  );
}

export type ActivityDisplayType =
  | 'BUY'
  | 'SELL'
  | 'DIVIDEND_CASH'
  | 'DIVIDEND_IN_KIND'
  | 'DEPOSIT'
  | 'WITHDRAW'
  | 'SPLIT'
  | 'EXCHANGE'
  | 'OPENING_CASH'
  | 'OPENING_POSITION';

export function getActivityDisplayType(
  activity: Activity,
): ActivityDisplayType {
  if (activity.type === 'DIVIDEND') {
    return activity.details.inKindDividend != null
      ? 'DIVIDEND_IN_KIND'
      : 'DIVIDEND_CASH';
  }

  return activity.type;
}

export interface ActivityAmount {
  amount: number;
  currency: string;
}

export function getActivityPrimaryAmount(
  activity: Activity,
): ActivityAmount | null {
  const { details } = activity;

  if (details.trade) {
    return details.trade.settlementAmount;
  }

  if (details.cash) {
    return details.cash.amount;
  }

  if (details.exchange) {
    return details.exchange.origin;
  }

  if (details.openingPosition) {
    return details.openingPosition.acquisitionCost;
  }

  if (details.inKindDividend) {
    return details.inKindDividend.referenceUnitPrice;
  }

  return null;
}

export function getActivityQuantity(
  activity: Activity,
): number | null {
  return (
    activity.details.trade?.quantity ??
    activity.details.inKindDividend?.quantity ??
    activity.details.openingPosition?.quantity ??
    null
  );
}

export function getPositionGainPercentage(
  position: Position,
): number | null {
  if (
    position.costBasis == null ||
    position.costBasis === 0 ||
    position.unrealizedGain == null
  ) {
    return null;
  }

  return (position.unrealizedGain / position.costBasis) * 100;
}

export function sumNullable(
  values: Array<number | null | undefined>,
): number {
  return values.reduce<number>(
    (total, value) =>
      total + (value != null && Number.isFinite(value) ? value : 0),
    0,
  );
}

export function parseNumericInput(value: string): number | null {
  if (value.trim().length === 0) {
    return null;
  }

  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : null;
}

export function getAssetTypeTranslationKey(
  type: AssetType | null | undefined,
): string {
  return type ? `assets.types.${type}` : 'common.unknown';
}

export function getTargetCurrency(
  assetCurrency: string | null | undefined,
  localCurrency: string | null | undefined,
): string {
  return assetCurrency || localCurrency || 'EUR';
}
