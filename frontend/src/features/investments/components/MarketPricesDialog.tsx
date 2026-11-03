import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';

import { Badge } from '@/shared/components/core/badge';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/core/dialog';
import { Input } from '@/shared/components/core/input';
import { Label } from '@/shared/components/core/label';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/core/table';
import { Pagination } from '@/shared/components/Pagination';
import { formatCurrency, formatNumber } from '@/shared/utils/i18nFormat';

import {
  ASSET_PAGE_SIZE,
} from '../constants';
import { useAssetPrices } from '../hooks/useInvestmentQueries';
import type { SharedAsset } from '../types';
import {
  endOfDateInputValue,
  formatDateTime,
  formatPrice,
  fromDateInputValue,
  toDateInputValue,
} from '../utils';
import { PanelState } from './PanelState';

interface MarketPricesDialogProps {
  asset: SharedAsset;
  onOpenChange: (open: boolean) => void;
}

function defaultFrom(): string {
  return toDateInputValue(
    new Date(
      Date.now() - 30 * 24 * 60 * 60 * 1000,
    ).toISOString(),
  );
}

function defaultTo(): string {
  return toDateInputValue(new Date().toISOString());
}

export function MarketPricesDialog({
  asset,
  onOpenChange,
}: MarketPricesDialogProps) {
  const { t } = useTranslation('investments');
  const [from, setFrom] = useState(defaultFrom);
  const [to, setTo] = useState(defaultTo);
  const [page, setPage] = useState(0);

  const isRangeValid =
    from.length > 0 &&
    to.length > 0 &&
    new Date(from) <= new Date(to);

  const params = useMemo(
    () => ({
      from: fromDateInputValue(from),
      to: endOfDateInputValue(to),
      page,
      size: ASSET_PAGE_SIZE,
    }),
    [from, to, page],
  );

  const query = useAssetPrices(
    asset.id,
    params,
    isRangeValid,
  );

  const prices = query.data?.content ?? [];
  const totalElements = query.data?.totalElements ?? 0;
  const totalPages = query.data?.totalPages ?? 0;
  const currentPage = query.data?.currentPage ?? page;

  const fromRow =
    totalElements === 0
      ? 0
      : currentPage * ASSET_PAGE_SIZE + 1;
  const toRow = Math.min(
    (currentPage + 1) * ASSET_PAGE_SIZE,
    totalElements,
  );

  return (
    <Dialog open onOpenChange={onOpenChange}>
      <DialogContent className="border-border bg-card sm:max-w-3xl">
        <div className="max-h-[85dvh] space-y-4 overflow-y-auto pr-1">
          <DialogHeader>
            <DialogTitle>
              {t('assets.prices.title', { name: asset.name })}
            </DialogTitle>
            <DialogDescription>
              {t('assets.prices.description')}
            </DialogDescription>
          </DialogHeader>

          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="prices-from">
                {t('common.from')}
              </Label>
              <Input
                id="prices-from"
                type="date"
                value={from}
                onChange={(event) => {
                  setFrom(event.target.value);
                  setPage(0);
                }}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="prices-to">
                {t('common.to')}
              </Label>
              <Input
                id="prices-to"
                type="date"
                value={to}
                onChange={(event) => {
                  setTo(event.target.value);
                  setPage(0);
                }}
              />
            </div>
          </div>

          {!isRangeValid && (
            <p className="text-sm text-destructive">
              {t('assets.prices.invalidRange')}
            </p>
          )}

          <div className="overflow-hidden rounded-lg border border-border">
            <PanelState
              isLoading={isRangeValid && query.isLoading}
              isError={query.isError}
              onRetry={() => void query.refetch()}
              errorLabel={t('assets.prices.loadError')}
            />

            {isRangeValid &&
              !query.isLoading &&
              !query.isError &&
              prices.length === 0 && (
                <p className="px-4 py-8 text-center text-sm text-muted-foreground">
                  {t('assets.prices.empty')}
                </p>
              )}

            {isRangeValid &&
              !query.isLoading &&
              !query.isError &&
              prices.length > 0 && (
                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>
                          {t('common.date')}
                        </TableHead>
                        <TableHead className="text-right">
                          {t('assets.prices.close')}
                        </TableHead>
                        <TableHead className="text-right">
                          {t('assets.prices.valuation')}
                        </TableHead>
                        <TableHead className="text-right">
                          {t('assets.prices.volume')}
                        </TableHead>
                        <TableHead>
                          {t('assets.prices.source')}
                        </TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {prices.map((price) => (
                        <TableRow key={price.id}>
                          <TableCell className="whitespace-nowrap text-muted-foreground">
                            {formatDateTime(price.timestamp)}
                          </TableCell>
                          <TableCell className="text-right tabular-nums">
                            {price.close == null
                              ? '—'
                              : formatPrice(price.close)}
                          </TableCell>
                          <TableCell className="text-right font-medium tabular-nums">
                            {formatCurrency(
                              price.valuationPrice,
                              price.currency,
                            )}
                          </TableCell>
                          <TableCell className="text-right tabular-nums">
                            {price.volume == null
                              ? '—'
                              : formatNumber(price.volume, {
                                  maximumFractionDigits: 0,
                                })}
                          </TableCell>
                          <TableCell>
                            <Badge variant="outline">
                              {price.source}
                            </Badge>
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                </div>
              )}
          </div>

          {isRangeValid && totalElements > 0 && (
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              from={fromRow}
              to={toRow}
              total={totalElements}
              onPrev={() =>
                setPage((value) => Math.max(0, value - 1))
              }
              onNext={() =>
                setPage((value) =>
                  Math.min(totalPages - 1, value + 1),
                )
              }
            />
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}
