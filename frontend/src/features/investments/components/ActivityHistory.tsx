import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import {
  Eye,
  FilterX,
  Loader2,
  Pencil,
  SearchX,
} from 'lucide-react';

import { Badge } from '@/shared/components/core/badge';
import { Button } from '@/shared/components/core/button';
import {
  Card,
  CardContent,
} from '@/shared/components/core/card';
import { Input } from '@/shared/components/core/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/core/select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/core/table';
import { Pagination } from '@/shared/components/Pagination';
import { cn } from '@/lib/utils';
import { formatCurrency } from '@/shared/utils/i18nFormat';

import {
  ACTIVITY_TYPES,
  CURRENCIES,
  DEFAULT_PAGE_SIZE,
} from '../constants';
import { useActivities } from '../hooks/useInvestmentQueries';
import { useAssetCatalog } from '../hooks/useInvestmentQueries';
import type {
  Activity,
  ActivityType,
  AssetReference,
  ListActivitiesParams,
} from '../types';
import {
  formatDateTime,
  formatPrice,
  formatQuantity,
  getActivityDisplayType,
  isActivityEditable,
} from '../utils';
import { ActivityDetailsDialog } from './ActivityDetailsDialog';
import { AssetPicker } from './AssetPicker';
import { EditActivityDialog } from './EditActivityDialog';

function startOfDay(value: string): string | undefined {
  if (!value) {
    return undefined;
  }

  const [year, month, day] = value
    .split('-')
    .map((part) => Number(part));

  return new Date(year, month - 1, day).toISOString();
}

function endOfDay(value: string): string | undefined {
  if (!value) {
    return undefined;
  }

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

const TYPE_BADGE_VARIANT: Record<
  string,
  'default' | 'secondary' | 'destructive' | 'warning' | 'outline'
> = {
  BUY: 'default',
  SELL: 'warning',
  DIVIDEND_CASH: 'secondary',
  DIVIDEND_IN_KIND: 'secondary',
  DEPOSIT: 'outline',
  WITHDRAW: 'outline',
  EXCHANGE: 'outline',
  SPLIT: 'outline',
  OPENING_CASH: 'secondary',
  OPENING_POSITION: 'secondary',
};

export function ActivityHistory() {
  const { t } = useTranslation('investments');
  const catalog = useAssetCatalog();

  const [page, setPage] = useState(0);
  const [type, setType] = useState<ActivityType | 'ALL'>('ALL');
  const [currency, setCurrency] = useState('');
  const [asset, setAsset] = useState<AssetReference | null>(null);
  const [from, setFrom] = useState('');
  const [to, setTo] = useState('');
  const [detailActivity, setDetailActivity] =
    useState<Activity | null>(null);
  const [editingActivity, setEditingActivity] =
    useState<Activity | null>(null);

  const params = useMemo<ListActivitiesParams>(() => {
    const next: ListActivitiesParams = {
      page,
      size: DEFAULT_PAGE_SIZE,
    };

    if (type !== 'ALL') {
      next.type = type;
    }

    if (currency.trim().length > 0) {
      next.currency = currency.trim().toUpperCase();
    }

    if (asset) {
      next.assetKind = asset.kind;
      next.assetId = asset.id;
    }

    const fromValue = startOfDay(from);
    const toValue = endOfDay(to);

    if (fromValue) {
      next.from = fromValue;
    }

    if (toValue) {
      next.to = toValue;
    }

    return next;
  }, [page, type, currency, asset, from, to]);

  const query = useActivities(params);
  const activities = query.data?.content ?? [];
  const totalElements = query.data?.totalElements ?? 0;
  const totalPages = query.data?.totalPages ?? 0;
  const currentPage = query.data?.currentPage ?? page;

  const hasFilters =
    type !== 'ALL' ||
    currency.trim().length > 0 ||
    asset != null ||
    from.length > 0 ||
    to.length > 0;

  const resetFilters = () => {
    setType('ALL');
    setCurrency('');
    setAsset(null);
    setFrom('');
    setTo('');
    setPage(0);
  };

  const fromRow =
    totalElements === 0
      ? 0
      : currentPage * DEFAULT_PAGE_SIZE + 1;
  const toRow = Math.min(
    (currentPage + 1) * DEFAULT_PAGE_SIZE,
    totalElements,
  );

  return (
    <div className="space-y-4">
      <Card>
        <CardContent className="space-y-4 pt-6">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <div className="flex items-center gap-2">
              <h2 className="text-lg font-semibold text-foreground">
                {t('activities.title')}
              </h2>
              <Badge variant="secondary">
                {totalElements}
              </Badge>
            </div>

            {hasFilters && (
              <Button
                type="button"
                variant="ghost"
                size="sm"
                onClick={resetFilters}
                className="gap-1.5 text-muted-foreground"
              >
                <FilterX
                  className="size-4"
                  aria-hidden="true"
                />
                {t('activities.clearFilters')}
              </Button>
            )}
          </div>

          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
            <div className="space-y-1.5">
              <label
                htmlFor="activity-filter-type"
                className="text-xs font-medium text-muted-foreground"
              >
                {t('activities.filterType')}
              </label>
              <Select
                value={type}
                onValueChange={(value) => {
                  setType(value as ActivityType | 'ALL');
                  setPage(0);
                }}
              >
                <SelectTrigger
                  id="activity-filter-type"
                  className="w-full"
                >
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ALL">
                    {t('common.all')}
                  </SelectItem>
                  {ACTIVITY_TYPES.map((activityType) => (
                    <SelectItem
                      key={activityType}
                      value={activityType}
                    >
                      {t(`activityTypes.${activityType}`)}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div className="space-y-1.5">
              <label
                htmlFor="activity-filter-currency"
                className="text-xs font-medium text-muted-foreground"
              >
                {t('activities.filterCurrency')}
              </label>
              <Input
                id="activity-filter-currency"
                list="activity-currencies"
                value={currency}
                maxLength={3}
                placeholder={t('common.all')}
                onChange={(event) => {
                  setCurrency(
                    event.target.value.toUpperCase(),
                  );
                  setPage(0);
                }}
                className="uppercase"
              />
            </div>

            <div className="space-y-1.5">
              <label
                htmlFor="activity-filter-from"
                className="text-xs font-medium text-muted-foreground"
              >
                {t('common.from')}
              </label>
              <Input
                id="activity-filter-from"
                type="date"
                value={from}
                onChange={(event) => {
                  setFrom(event.target.value);
                  setPage(0);
                }}
              />
            </div>

            <div className="space-y-1.5">
              <label
                htmlFor="activity-filter-to"
                className="text-xs font-medium text-muted-foreground"
              >
                {t('common.to')}
              </label>
              <Input
                id="activity-filter-to"
                type="date"
                value={to}
                onChange={(event) => {
                  setTo(event.target.value);
                  setPage(0);
                }}
              />
            </div>

            <div className="sm:col-span-2 lg:col-span-4">
              <AssetPicker
                value={asset}
                onChange={(value) => {
                  setAsset(value);
                  setPage(0);
                }}
                optional
                label={t('activities.filterAsset')}
              />
            </div>
          </div>

          <datalist id="activity-currencies">
            {CURRENCIES.map((code) => (
              <option key={code} value={code} />
            ))}
          </datalist>
        </CardContent>
      </Card>

      <Card className="overflow-hidden">
        {query.isLoading ? (
          <div className="flex items-center justify-center py-16">
            <Loader2
              className="size-7 animate-spin text-muted-foreground"
              aria-hidden="true"
            />
            <span className="sr-only">
              {t('common:status.loading')}
            </span>
          </div>
        ) : query.isError ? (
          <div className="flex flex-col items-center gap-3 px-6 py-12 text-center">
            <p className="text-sm text-destructive">
              {t('activities.loadError')}
            </p>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => void query.refetch()}
            >
              {t('common:actions.retry')}
            </Button>
          </div>
        ) : activities.length === 0 ? (
          <div className="flex flex-col items-center gap-2 px-6 py-16 text-center">
            <SearchX
              className="size-8 text-muted-foreground/60"
              aria-hidden="true"
            />
            <p className="text-sm text-muted-foreground">
              {hasFilters
                ? t('activities.emptyFiltered')
                : t('activities.empty')}
            </p>
          </div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead className="min-w-36">
                      {t('common.date')}
                    </TableHead>
                    <TableHead className="min-w-32">
                      {t('common.type')}
                    </TableHead>
                    <TableHead className="min-w-44">
                      {t('common.asset')}
                    </TableHead>
                    <TableHead className="text-right">
                      {t('record.quantity')}
                    </TableHead>
                    <TableHead className="text-right">
                      {t('common.amount')}
                    </TableHead>
                    <TableHead className="min-w-44">
                      {t('common.comment')}
                    </TableHead>
                    <TableHead className="w-24 text-right">
                      {t('common.actions')}
                    </TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {activities.map((activity) => {
                    const entry = catalog.getEntry(
                      activity.assetReference,
                    );
                    const displayType =
                      getActivityDisplayType(activity);
                    const trade = activity.details.trade;
                    const cash = activity.details.cash;
                    const exchange =
                      activity.details.exchange;
                    const split = activity.details.split;
                    const opening =
                      activity.details.openingPosition;
                    const quantity =
                      trade?.quantity ??
                      activity.details.inKindDividend
                        ?.quantity ??
                      opening?.quantity;

                    return (
                      <TableRow key={activity.id}>
                        <TableCell className="whitespace-nowrap text-muted-foreground">
                          {formatDateTime(
                            activity.occurredAt,
                          )}
                        </TableCell>
                        <TableCell>
                          <Badge
                            variant={
                              TYPE_BADGE_VARIANT[
                                displayType
                              ] ?? 'outline'
                            }
                          >
                            {t(
                              `activityTypes.${displayType}`,
                            )}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <div className="flex min-w-0 flex-col">
                            <span className="truncate font-medium text-foreground">
                              {entry?.name ??
                                activity.assetReference
                                  ?.id ??
                                t('common.none')}
                            </span>
                            {entry?.symbol && (
                              <span className="text-xs text-muted-foreground">
                                {entry.symbol}
                              </span>
                            )}
                          </div>
                        </TableCell>
                        <TableCell className="text-right tabular-nums">
                          {quantity != null
                            ? formatQuantity(quantity)
                            : '—'}
                        </TableCell>
                        <TableCell className="text-right tabular-nums">
                          {trade ? (
                            formatCurrency(
                              trade.settlementAmount.amount,
                              trade.settlementAmount.currency,
                            )
                          ) : cash ? (
                            formatCurrency(
                              cash.amount.amount,
                              cash.amount.currency,
                            )
                          ) : exchange ? (
                            <span className="whitespace-nowrap text-xs">
                              {formatCurrency(
                                exchange.origin.amount,
                                exchange.origin.currency,
                              )}
                              <span className="mx-1 text-muted-foreground">
                                →
                              </span>
                              {formatCurrency(
                                exchange.destination.amount,
                                exchange.destination.currency,
                              )}
                            </span>
                          ) : split ? (
                            `${t('record.ratio')}: ${formatPrice(
                              split.ratio,
                            )}`
                          ) : opening ? (
                            formatCurrency(
                              opening.acquisitionCost.amount,
                              opening.acquisitionCost.currency,
                            )
                          ) : activity.details
                              .inKindDividend ? (
                            formatCurrency(
                              activity.details.inKindDividend
                                .referenceUnitPrice.amount,
                              activity.details.inKindDividend
                                .referenceUnitPrice.currency,
                            )
                          ) : (
                            '—'
                          )}
                        </TableCell>
                        <TableCell>
                          <span
                            className={cn(
                              'line-clamp-2 text-sm',
                              activity.comment
                                ? 'text-muted-foreground'
                                : 'text-muted-foreground/50',
                            )}
                          >
                            {activity.comment ||
                              t('common.none')}
                          </span>
                        </TableCell>
                        <TableCell>
                          <div className="flex items-center justify-end gap-1">
                            <Button
                              type="button"
                              variant="ghost"
                              size="icon"
                              className="size-8"
                              onClick={() =>
                                setDetailActivity(activity)
                              }
                              aria-label={t(
                                'activities.viewDetails',
                              )}
                            >
                              <Eye
                                className="size-4"
                                aria-hidden="true"
                              />
                            </Button>

                            {isActivityEditable(activity) &&
                              !activity.linkedTransactionId && (
                                <Button
                                  type="button"
                                  variant="ghost"
                                  size="icon"
                                  className="size-8"
                                  onClick={() =>
                                    setEditingActivity(
                                      activity,
                                    )
                                  }
                                  aria-label={t(
                                    'common:actions.edit',
                                  )}
                                >
                                  <Pencil
                                    className="size-4"
                                    aria-hidden="true"
                                  />
                                </Button>
                              )}
                          </div>
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            </div>

            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              from={fromRow}
              to={toRow}
              total={totalElements}
              onPrev={() =>
                setPage((value) =>
                  Math.max(0, value - 1),
                )
              }
              onNext={() =>
                setPage((value) =>
                  Math.min(
                    totalPages - 1,
                    value + 1,
                  ),
                )
              }
            />
          </>
        )}
      </Card>

      {detailActivity && (
        <ActivityDetailsDialog
          activity={detailActivity}
          onOpenChange={(open) => {
            if (!open) {
              setDetailActivity(null);
            }
          }}
          onEdit={(activity) => {
            setDetailActivity(null);
            setEditingActivity(activity);
          }}
        />
      )}

      {editingActivity && (
        <EditActivityDialog
          key={editingActivity.id}
          activity={editingActivity}
          onOpenChange={(open) => {
            if (!open) {
              setEditingActivity(null);
            }
          }}
        />
      )}
    </div>
  );
}
