import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Minus, Plus } from 'lucide-react';

import { Badge } from '@/shared/components/core/badge';
import { Button } from '@/shared/components/core/button';
import { Pagination } from '@/shared/components/Pagination';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/core/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/core/table';
import { cn } from '@/lib/utils';
import { formatCurrency } from '@/shared/utils/i18nFormat';

import type {
  AssetCatalog,
} from '../hooks/useInvestmentQueries';
import type {
  AssetReference,
  Portfolio,
} from '../types';
import {
  assetKey,
  formatPercent,
  formatQuantity,
  getPositionGainPercentage,
} from '../utils';

const POSITIONS_PAGE_SIZE = 15;

interface PositionsTableProps {
  portfolio: Portfolio;
  catalog: AssetCatalog;
  onTrade: (
    type: 'BUY' | 'SELL',
    asset: AssetReference,
  ) => void;
  onRegisterPosition: (asset: AssetReference) => void;
}

export function PositionsTable({
  portfolio,
  catalog,
  onTrade,
  onRegisterPosition,
}: PositionsTableProps) {
  const { t } = useTranslation('investments');
  const [page, setPage] = useState(0);
  const positionKeys = new Set(portfolio.positions.map(({ assetReference }) => assetKey(assetReference)));
  const rows = [
    ...portfolio.positions.map((position) => ({ reference: position.assetReference, position })),
    ...catalog.entries
      .filter(({ reference }) => reference.kind === 'USER' && !positionKeys.has(assetKey(reference)))
      .map(({ reference }) => ({ reference, position: null })),
  ];
  const total = rows.length;
  const totalPages = Math.max(1, Math.ceil(total / POSITIONS_PAGE_SIZE));
  const currentPage = Math.min(page, totalPages - 1);
  const offset = currentPage * POSITIONS_PAGE_SIZE;
  const end = Math.min(offset + POSITIONS_PAGE_SIZE, total);

  return (
    <Card className="min-w-0 gap-3 rounded-none border-0 bg-transparent py-0 shadow-none">
      <CardHeader className="border-b border-border px-0 pb-2">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <CardTitle>{t('portfolio.positions')}</CardTitle>
          <Badge variant="secondary">
            {total}
          </Badge>
        </div>
      </CardHeader>

      <CardContent className="px-0">
        {total === 0 ? (
          <p className="px-2 py-4 text-sm text-muted-foreground">
            {t('portfolio.noPositions')}
          </p>
        ) : (
          <>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="min-w-28">
                    {t('common.asset')}
                  </TableHead>
                  <TableHead className="text-right">
                    {t('record.quantity')}
                  </TableHead>
                  <TableHead className="text-right">
                    {t('portfolio.costBasis')}
                  </TableHead>
                  <TableHead className="text-right">
                    {t('portfolio.marketValue')}
                  </TableHead>
                  <TableHead className="text-right">
                    {t('portfolio.unrealizedGain')}
                  </TableHead>
                  <TableHead className="text-right">
                    {t('common.actions')}
                  </TableHead>
                </TableRow>
              </TableHeader>

              <TableBody>
                {rows.slice(offset, end).map(({ reference, position }) => {
                  const entry = catalog.getEntry(reference);
                  const gainPercentage = position ? getPositionGainPercentage(position) : null;
                  const isPositive =
                    (position?.unrealizedGain ?? 0) >= 0;

                  return (
                    <TableRow
                      key={assetKey(reference)}
                    >
                      <TableCell>
                        <div className="flex min-w-0 flex-col gap-1">
                          <div className="flex flex-wrap items-center gap-1.5">
                            <span className="truncate font-medium text-foreground">
                              {entry?.name ??
                                t('common.unknownAsset')}
                            </span>
                            {entry?.type && (
                              <Badge
                                variant="outline"
                                className="text-[10px]"
                              >
                                {t(
                                  `assets.types.${entry.type}`,
                                  {
                                    defaultValue: entry.type,
                                  },
                                )}
                              </Badge>
                            )}
                            {reference.kind === 'USER' && (
                              <Badge
                                variant="secondary"
                                className="text-[10px]"
                              >
                                {t('common.custom')}
                              </Badge>
                            )}
                          </div>
                          <span className="text-xs text-muted-foreground">
                            {[
                              entry?.symbol,
                              entry?.sector?.displayName,
                            ]
                              .filter(Boolean)
                              .join(' · ') ||
                              (position ? t('common.none') : null)}
                          </span>
                          {!position && (
                            <span className="text-xs text-muted-foreground">
                              {t('portfolio.positionNotRecorded', { defaultValue: 'No open position recorded' })}
                            </span>
                          )}
                        </div>
                      </TableCell>

                      <TableCell className="text-right tabular-nums">
                        {position ? formatQuantity(position.quantity) : '—'}
                      </TableCell>

                      <TableCell className="text-right tabular-nums">
                        {position?.costBasis != null
                          ? formatCurrency(
                              position.costBasis,
                              position.valuationCurrency,
                            )
                          : '—'}
                      </TableCell>

                      <TableCell className="text-right tabular-nums">
                        {position?.marketValue != null
                          ? formatCurrency(
                              position.marketValue,
                              position.valuationCurrency,
                            )
                          : '—'}
                        {position && (
                          position.calculationStatus === 'NOT_CALCULABLE' ||
                          position.estimated ||
                          position.historyIncomplete ||
                          position.unavailableReason
                        ) && (
                          <div className="mt-1 flex flex-col items-end gap-1 whitespace-normal">
                            {position.calculationStatus === 'NOT_CALCULABLE' && (
                              <Badge variant="destructive">{t('portfolio.notCalculable')}</Badge>
                            )}
                            {position.estimated && (
                              <Badge variant="warning">{t('portfolio.estimatedShort')}</Badge>
                            )}
                            {position.historyIncomplete && (
                              <Badge variant="outline">{t('portfolio.historyIncomplete')}</Badge>
                            )}
                            {position.unavailableReason && (
                              <span className="text-xs text-muted-foreground">{position.unavailableReason}</span>
                            )}
                          </div>
                        )}
                      </TableCell>

                      <TableCell className="text-right tabular-nums">
                        {position?.unrealizedGain != null ? (
                          <div className="flex flex-col items-end">
                            <span
                              className={cn(
                                'font-medium',
                                isPositive
                                  ? 'text-primary'
                                  : 'text-destructive',
                              )}
                            >
                              {formatCurrency(
                                position.unrealizedGain,
                                position.valuationCurrency,
                              )}
                            </span>
                            <span className="text-xs text-muted-foreground">
                              {formatPercent(gainPercentage)}
                            </span>
                          </div>
                        ) : (
                          '—'
                        )}
                      </TableCell>

                      <TableCell>
                        <div className="flex items-center justify-end gap-1">
                          {!position ? (
                            <Button
                              type="button"
                              variant="outline"
                              size="sm"
                              onClick={() => onRegisterPosition(reference)}
                            >
                              {t('portfolio.registerPosition', { defaultValue: 'Register position' })}
                            </Button>
                          ) : (
                            <>
                              <Button
                                type="button"
                                variant="ghost"
                                size="icon-sm"
                                onClick={() => onTrade('BUY', reference)}
                                aria-label={t('portfolio.buy')}
                              >
                                <Plus className="size-4 text-primary" aria-hidden="true" />
                              </Button>
                              <Button
                                type="button"
                                variant="ghost"
                                size="icon-sm"
                                onClick={() => onTrade('SELL', reference)}
                                aria-label={t('portfolio.sell')}
                              >
                                <Minus className="size-4 text-destructive" aria-hidden="true" />
                              </Button>
                            </>
                          )}
                        </div>
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              from={offset + 1}
              to={end}
              total={total}
              onPrev={() => setPage(Math.max(0, currentPage - 1))}
              onNext={() => setPage(Math.min(totalPages - 1, currentPage + 1))}
            />
          </>
        )}
      </CardContent>
    </Card>
  );
}
