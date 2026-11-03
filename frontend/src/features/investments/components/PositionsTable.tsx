import { useTranslation } from 'react-i18next';
import { Minus, Plus } from 'lucide-react';

import { Badge } from '@/shared/components/core/badge';
import { Button } from '@/shared/components/core/button';
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
  formatPercent,
  formatQuantity,
  getPositionGainPercentage,
} from '../utils';

interface PositionsTableProps {
  portfolio: Portfolio;
  catalog: AssetCatalog;
  onTrade: (
    type: 'BUY' | 'SELL',
    asset: AssetReference,
  ) => void;
}

export function PositionsTable({
  portfolio,
  catalog,
  onTrade,
}: PositionsTableProps) {
  const { t } = useTranslation('investments');

  return (
    <Card className="overflow-hidden">
      <CardHeader className="border-b border-border">
        <div className="flex flex-wrap items-center justify-between gap-2">
          <CardTitle>{t('portfolio.positions')}</CardTitle>
          <Badge variant="secondary">
            {portfolio.positions.length}
          </Badge>
        </div>
      </CardHeader>

      <CardContent className="px-0">
        {portfolio.positions.length === 0 ? (
          <p className="px-6 py-10 text-center text-sm text-muted-foreground">
            {t('portfolio.noPositions')}
          </p>
        ) : (
          <div className="overflow-x-auto">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead className="min-w-56">
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
                  <TableHead className="min-w-40">
                    {t('common.status')}
                  </TableHead>
                  <TableHead className="w-28 text-right">
                    {t('common.actions')}
                  </TableHead>
                </TableRow>
              </TableHeader>

              <TableBody>
                {portfolio.positions.map((position) => {
                  const entry = catalog.getEntry(
                    position.assetReference,
                  );
                  const gainPercentage =
                    getPositionGainPercentage(position);
                  const isPositive =
                    (position.unrealizedGain ?? 0) >= 0;

                  return (
                    <TableRow
                      key={`${position.assetReference.kind}:${position.assetReference.id}`}
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
                            {entry?.isUserAsset && (
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
                              t('common.none')}
                          </span>
                        </div>
                      </TableCell>

                      <TableCell className="text-right tabular-nums">
                        {formatQuantity(position.quantity)}
                      </TableCell>

                      <TableCell className="text-right tabular-nums">
                        {position.costBasis != null
                          ? formatCurrency(
                              position.costBasis,
                              position.valuationCurrency,
                            )
                          : '—'}
                      </TableCell>

                      <TableCell className="text-right tabular-nums">
                        {position.marketValue != null
                          ? formatCurrency(
                              position.marketValue,
                              position.valuationCurrency,
                            )
                          : '—'}
                      </TableCell>

                      <TableCell className="text-right tabular-nums">
                        {position.unrealizedGain != null ? (
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
                        <div className="flex flex-wrap gap-1">
                          {position.calculationStatus ===
                          'NOT_CALCULABLE' ? (
                            <Badge variant="destructive">
                              {t(
                                'portfolio.notCalculable',
                              )}
                            </Badge>
                          ) : (
                            <Badge variant="default">
                              {t('portfolio.calculable')}
                            </Badge>
                          )}
                          {position.estimated && (
                            <Badge variant="warning">
                              {t('portfolio.estimatedShort')}
                            </Badge>
                          )}
                          {position.historyIncomplete && (
                            <Badge variant="outline">
                              {t(
                                'portfolio.historyIncomplete',
                              )}
                            </Badge>
                          )}
                        </div>
                        {position.unavailableReason && (
                          <p className="mt-1 text-xs text-muted-foreground">
                            {position.unavailableReason}
                          </p>
                        )}
                      </TableCell>

                      <TableCell>
                        <div className="flex items-center justify-end gap-1">
                          <Button
                            type="button"
                            variant="ghost"
                            size="icon-sm"
                            onClick={() =>
                              onTrade(
                                'BUY',
                                position.assetReference,
                              )
                            }
                            aria-label={t('portfolio.buy')}
                          >
                            <Plus
                              className="size-4 text-primary"
                              aria-hidden="true"
                            />
                          </Button>
                          <Button
                            type="button"
                            variant="ghost"
                            size="icon-sm"
                            onClick={() =>
                              onTrade(
                                'SELL',
                                position.assetReference,
                              )
                            }
                            aria-label={t('portfolio.sell')}
                          >
                            <Minus
                              className="size-4 text-destructive"
                              aria-hidden="true"
                            />
                          </Button>
                        </div>
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          </div>
        )}
      </CardContent>
    </Card>
  );
}
