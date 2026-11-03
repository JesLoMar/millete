import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { ArrowLeftRight, LineChart } from 'lucide-react';

import { Badge } from '@/shared/components/core/badge';
import { Button } from '@/shared/components/core/button';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/core/card';
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
import { formatNumber } from '@/shared/utils/i18nFormat';

import {
  ASSET_PAGE_SIZE,
  CURRENCIES,
} from '../constants';
import { useFxRates } from '../hooks/useInvestmentQueries';
import type { FxRatePage } from '../types';
import {
  endOfDateInputValue,
  formatDateTime,
  fromDateInputValue,
  isValidCurrencyCode,
  toDateInputValue,
} from '../utils';
import { PanelState } from './PanelState';

interface FxRatesCardProps {
  defaultBaseCurrency: string;
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

export function FxRatesCard({
  defaultBaseCurrency,
}: FxRatesCardProps) {
  const { t } = useTranslation('investments');
  const [baseCurrency, setBaseCurrency] = useState(
    defaultBaseCurrency,
  );
  const [quoteCurrency, setQuoteCurrency] = useState('USD');
  const [from, setFrom] = useState(defaultFrom);
  const [to, setTo] = useState(defaultTo);
  const [page, setPage] = useState(0);

  const normalizedBase = baseCurrency.trim().toUpperCase();
  const normalizedQuote = quoteCurrency.trim().toUpperCase();

  const isRangeValid =
    from.length > 0 &&
    to.length > 0 &&
    new Date(from) <= new Date(to);

  const areCurrenciesValid =
    isValidCurrencyCode(normalizedBase) &&
    isValidCurrencyCode(normalizedQuote) &&
    normalizedBase !== normalizedQuote;

  const params = useMemo(
    () => ({
      baseCurrency: normalizedBase,
      quoteCurrency: normalizedQuote,
      from: fromDateInputValue(from),
      to: endOfDateInputValue(to),
      page,
      size: ASSET_PAGE_SIZE,
    }),
    [normalizedBase, normalizedQuote, from, to, page],
  );

  const query = useFxRates(
    params,
    areCurrenciesValid && isRangeValid,
  );

  const rates = query.data?.content ?? [];
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

  const resetPage = () => setPage(0);

  const handleSwap = () => {
    setBaseCurrency(normalizedQuote);
    setQuoteCurrency(normalizedBase);
    resetPage();
  };

  const pageLabel = (
    data: FxRatePage | undefined,
  ): string => {
    if (!data || data.totalElements === 0) {
      return t('assets.fx.empty');
    }

    return t('assets.fx.showing', {
      from: fromRow,
      to: toRow,
      total: data.totalElements,
    });
  };

  return (
    <Card className="overflow-hidden">
      <CardHeader className="border-b border-border">
        <div className="flex items-center gap-2">
          <LineChart
            className="size-4 text-muted-foreground"
            aria-hidden="true"
          />
          <CardTitle>{t('assets.fx.title')}</CardTitle>
        </div>
        <p className="text-xs text-muted-foreground">
          {t('assets.fx.description')}
        </p>
      </CardHeader>

      <CardContent className="space-y-4 pt-4">
        <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-[1fr_auto_1fr_1fr_1fr] lg:items-end">
          <div className="space-y-2">
            <Label htmlFor="fx-base">
              {t('assets.fx.base')}
            </Label>
            <Input
              id="fx-base"
              value={baseCurrency}
              list="investment-currencies"
              maxLength={3}
              onChange={(event) => {
                setBaseCurrency(
                  event.target.value.toUpperCase(),
                );
                resetPage();
              }}
              className="uppercase"
            />
          </div>

          <div className="flex justify-center lg:pb-1">
            <Button
              type="button"
              variant="outline"
              size="icon"
              onClick={handleSwap}
              aria-label={t('assets.fx.swap')}
            >
              <ArrowLeftRight
                className="size-4"
                aria-hidden="true"
              />
            </Button>
          </div>

          <div className="space-y-2">
            <Label htmlFor="fx-quote">
              {t('assets.fx.quote')}
            </Label>
            <Input
              id="fx-quote"
              value={quoteCurrency}
              list="investment-currencies"
              maxLength={3}
              onChange={(event) => {
                setQuoteCurrency(
                  event.target.value.toUpperCase(),
                );
                resetPage();
              }}
              className="uppercase"
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="fx-from">{t('common.from')}</Label>
            <Input
              id="fx-from"
              type="date"
              value={from}
              onChange={(event) => {
                setFrom(event.target.value);
                resetPage();
              }}
            />
          </div>

          <div className="space-y-2">
            <Label htmlFor="fx-to">{t('common.to')}</Label>
            <Input
              id="fx-to"
              type="date"
              value={to}
              onChange={(event) => {
                setTo(event.target.value);
                resetPage();
              }}
            />
          </div>
        </div>

        <datalist id="investment-currencies">
          {CURRENCIES.map((code) => (
            <option key={code} value={code} />
          ))}
        </datalist>

        {(!areCurrenciesValid || !isRangeValid) && (
          <p className="text-sm text-destructive">
            {!areCurrenciesValid
              ? t('assets.fx.invalidCurrencies')
              : t('assets.fx.invalidRange')}
          </p>
        )}

        <div className="rounded-lg border border-border">
          <PanelState
            isLoading={
              areCurrenciesValid &&
              isRangeValid &&
              query.isLoading
            }
            isError={query.isError}
            onRetry={() => void query.refetch()}
            errorLabel={t('assets.fx.loadError')}
          />

          {areCurrenciesValid &&
            isRangeValid &&
            !query.isLoading &&
            !query.isError &&
            rates.length === 0 && (
              <p className="px-4 py-8 text-center text-sm text-muted-foreground">
                {t('assets.fx.empty')}
              </p>
            )}

          {areCurrenciesValid &&
            isRangeValid &&
            !query.isLoading &&
            !query.isError &&
            rates.length > 0 && (
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>{t('common.date')}</TableHead>
                      <TableHead>{t('assets.fx.pair')}</TableHead>
                      <TableHead className="text-right">
                        {t('assets.fx.rate')}
                      </TableHead>
                      <TableHead>
                        {t('assets.fx.source')}
                      </TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {rates.map((rate) => (
                      <TableRow key={rate.id}>
                        <TableCell className="whitespace-nowrap text-muted-foreground">
                          {formatDateTime(rate.timestamp)}
                        </TableCell>
                        <TableCell>
                          <Badge variant="outline">
                            {rate.baseCurrency}/
                            {rate.quoteCurrency}
                          </Badge>
                        </TableCell>
                        <TableCell className="text-right font-medium tabular-nums">
                          {formatNumber(rate.rate, {
                            maximumFractionDigits: 8,
                          })}
                        </TableCell>
                        <TableCell className="text-muted-foreground">
                          {rate.source}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}
        </div>

        {areCurrenciesValid &&
          isRangeValid &&
          totalElements > 0 && (
            <>
              <p className="text-xs text-muted-foreground">
                {pageLabel(query.data)}
              </p>
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
            </>
          )}
      </CardContent>
    </Card>
  );
}
