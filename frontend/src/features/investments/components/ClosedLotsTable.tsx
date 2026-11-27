import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { ReceiptText } from 'lucide-react';

import { Badge } from '@/shared/components/core/badge';
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

import { CLOSED_LOTS_PAGE_SIZE } from '../constants';
import {
  useAssetCatalog,
  useClosedLots,
} from '../hooks/useInvestmentQueries';
import {
  formatDateTime,
  formatQuantity,
} from '../utils';
import { PanelState } from './PanelState';

interface ClosedLotsTableProps {
  from: string;
  to: string;
  enabled?: boolean;
}

export function ClosedLotsTable({
  from,
  to,
  enabled = true,
}: ClosedLotsTableProps) {
  const { t } = useTranslation('investments');
  const catalog = useAssetCatalog();
  const [page, setPage] = useState(0);

  const query = useClosedLots(
    from,
    to,
    page,
    CLOSED_LOTS_PAGE_SIZE,
    enabled,
  );

  const lots = query.data?.content ?? [];
  const totalElements = query.data?.totalElements ?? 0;
  const totalPages = query.data?.totalPages ?? 0;
  const currentPage = query.data?.currentPage ?? page;

  const fromRow =
    totalElements === 0
      ? 0
      : currentPage * CLOSED_LOTS_PAGE_SIZE + 1;
  const toRow = Math.min(
    (currentPage + 1) * CLOSED_LOTS_PAGE_SIZE,
    totalElements,
  );

  return (
    <section aria-labelledby="closed-lots-title" className="min-w-0 space-y-3">
      <header className="space-y-1 border-b border-border pb-2">
        <div className="flex items-center gap-2">
          <ReceiptText
            className="size-4 text-muted-foreground"
            aria-hidden="true"
          />
          <h2 id="closed-lots-title" className="text-base font-semibold">
            {t('closedLots.title')}
          </h2>
          <Badge variant="secondary">{totalElements}</Badge>
        </div>
        <p className="text-xs text-muted-foreground">
          {t('closedLots.description')}
        </p>
      </header>

      <div>
        <PanelState
          isLoading={query.isLoading}
          isError={query.isError}
          onRetry={() => void query.refetch()}
          errorLabel={t('closedLots.loadError')}
        />

        {!query.isLoading &&
          !query.isError &&
          lots.length === 0 && (
            <div className="flex flex-col items-center gap-2 px-6 py-16 text-center">
              <ReceiptText
                className="size-8 text-muted-foreground/60"
                aria-hidden="true"
              />
              <p className="text-sm text-muted-foreground">
                {t('closedLots.empty')}
              </p>
            </div>
          )}

        {!query.isLoading &&
          !query.isError &&
          lots.length > 0 && (
            <>
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead className="min-w-48">
                        {t('common.asset')}
                      </TableHead>
                      <TableHead className="whitespace-nowrap">
                        {t('closedLots.closedAt')}
                      </TableHead>
                      <TableHead className="text-right">
                        {t('common.quantity')}
                      </TableHead>
                      <TableHead className="text-right">
                        {t('closedLots.costBasis')}
                      </TableHead>
                      <TableHead className="text-right">
                        {t('closedLots.proceeds')}
                      </TableHead>
                      <TableHead className="text-right">
                        {t('closedLots.realizedGain')}
                      </TableHead>
                      <TableHead className="min-w-36">
                        {t('common.status')}
                      </TableHead>
                    </TableRow>
                  </TableHeader>

                  <TableBody>
                    {lots.map((lot) => {
                      const entry = catalog.getEntry(
                        lot.assetReference,
                      );
                      const isPositive = lot.realizedGain >= 0;

                      return (
                        <TableRow key={lot.lotId}>
                          <TableCell>
                            <div className="flex min-w-0 flex-col">
                              <span className="truncate font-medium text-foreground">
                                {entry?.name ??
                                  t('common.unknownAsset')}
                              </span>
                              <span className="text-xs text-muted-foreground">
                                {entry?.symbol ??
                                  t('common.none')}
                              </span>
                            </div>
                          </TableCell>

                          <TableCell className="whitespace-nowrap text-muted-foreground">
                            <div className="flex flex-col">
                              <span>
                                {formatDateTime(lot.closedAt)}
                              </span>
                              <span className="text-xs">
                                {t('closedLots.openedAt', {
                                  date: formatDateTime(
                                    lot.openedAt,
                                  ),
                                })}
                              </span>
                            </div>
                          </TableCell>

                          <TableCell className="text-right tabular-nums">
                            {formatQuantity(lot.quantity)}
                          </TableCell>

                          <TableCell className="text-right tabular-nums">
                            {formatCurrency(
                              lot.costBasis,
                              lot.costBasisCurrency,
                            )}
                          </TableCell>

                          <TableCell className="text-right tabular-nums">
                            {formatCurrency(
                              lot.proceeds,
                              lot.proceedsCurrency,
                            )}
                          </TableCell>

                          <TableCell
                            className={cn(
                              'text-right font-medium tabular-nums',
                              isPositive
                                ? 'text-financial-positive'
                                : 'text-financial-negative',
                            )}
                          >
                            {formatCurrency(
                              lot.realizedGain,
                              lot.realizedGainCurrency,
                            )}
                          </TableCell>

                          <TableCell>
                            <div className="flex flex-wrap gap-1">
                              {lot.estimated && (
                                <Badge variant="warning">
                                  {t('portfolio.estimatedShort')}
                                </Badge>
                              )}
                              {lot.historyIncomplete && (
                                <Badge variant="outline">
                                  {t(
                                    'portfolio.historyIncomplete',
                                  )}
                                </Badge>
                              )}
                              {!lot.estimated &&
                                !lot.historyIncomplete && (
                                  <Badge variant="default">
                                    {t('closedLots.complete')}
                                  </Badge>
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
      </div>
    </section>
  );
}
