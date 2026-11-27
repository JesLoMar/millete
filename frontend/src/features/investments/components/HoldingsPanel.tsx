import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { History, Layers } from 'lucide-react';

import { Badge } from '@/shared/components/core/badge';
import { Button } from '@/shared/components/core/button';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/core/table';
import { Pagination } from '@/shared/components/Pagination';
import { formatCurrency } from '@/shared/utils/i18nFormat';

import { DEFAULT_PAGE_SIZE } from '../constants';
import {
  useAssetCatalog,
  useHoldings,
} from '../hooks/useInvestmentQueries';
import type { AssetReference, Holding } from '../types';
import {
  formatDateTime,
  formatQuantity,
} from '../utils';
import { AssetPicker } from './AssetPicker';
import { PanelState } from './PanelState';
import { ReplaceHoldingHistoryDialog } from './ReplaceHoldingHistoryDialog';

export function HoldingsPanel() {
  const { t } = useTranslation('investments');
  const catalog = useAssetCatalog();

  const [page, setPage] = useState(0);
  const [assetFilter, setAssetFilter] =
    useState<AssetReference | null>(null);
  const [historyHolding, setHistoryHolding] =
    useState<Holding | null>(null);

  const query = useHoldings({
    page,
    size: DEFAULT_PAGE_SIZE,
    assetKind: assetFilter?.kind,
    assetId: assetFilter?.id,
  });

  const holdings = query.data?.content ?? [];
  const totalElements = query.data?.totalElements ?? 0;
  const totalPages = query.data?.totalPages ?? 0;
  const currentPage = query.data?.currentPage ?? page;

  const fromRow =
    totalElements === 0
      ? 0
      : currentPage * DEFAULT_PAGE_SIZE + 1;
  const toRow = Math.min(
    (currentPage + 1) * DEFAULT_PAGE_SIZE,
    totalElements,
  );

  const handleFilterChange = (reference: AssetReference | null) => {
    setAssetFilter(reference);
    setPage(0);
  };

  return (
    <div className="grid items-start gap-6 xl:grid-cols-[minmax(0,3fr)_minmax(0,1fr)]">
      <aside
        aria-labelledby="holdings-filters-title"
        className="min-w-0 space-y-3 xl:col-start-2 xl:row-start-1 xl:border-l xl:border-border xl:pl-4"
      >
        <h2
          id="holdings-filters-title"
          className="border-b border-border pb-2 text-base font-semibold"
        >
          {t('activities.filtersTitle')}
        </h2>
        <AssetPicker
          value={assetFilter}
          onChange={handleFilterChange}
          label={t('holdings.filterAsset')}
          optional
        />
      </aside>

      <section
        aria-labelledby="holdings-list-title"
        className="min-w-0 space-y-3 xl:col-start-1 xl:row-start-1"
      >
        <header className="space-y-1 border-b border-border pb-2">
          <div className="flex items-center gap-2">
            <h2 id="holdings-list-title" className="text-base font-semibold">
              {t('holdings.table.title')}
            </h2>
            <Badge variant="secondary">{totalElements}</Badge>
          </div>
          <p className="text-xs text-muted-foreground">
            {t('holdings.description')}{' '}
            {t('holdings.table.description')}
          </p>
        </header>

        <div>
          <PanelState
            isLoading={query.isLoading}
            isError={query.isError}
            onRetry={() => void query.refetch()}
            errorLabel={t('holdings.loadError')}
          />

          {!query.isLoading &&
            !query.isError &&
            holdings.length === 0 && (
              <div className="flex flex-col items-center gap-2 px-6 py-16 text-center">
                <Layers
                  className="size-8 text-muted-foreground/60"
                  aria-hidden="true"
                />
                <p className="text-sm text-muted-foreground">
                  {assetFilter
                    ? t('holdings.emptyFiltered')
                    : t('holdings.empty')}
                </p>
              </div>
            )}

          {!query.isLoading &&
            !query.isError &&
            holdings.length > 0 && (
              <>
                <div className="overflow-x-auto">
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead className="min-w-48">
                          {t('common.asset')}
                        </TableHead>
                        <TableHead className="whitespace-nowrap">
                          {t('holdings.table.snapshotAt')}
                        </TableHead>
                        <TableHead className="text-right">
                          {t('common.quantity')}
                        </TableHead>
                        <TableHead className="text-right">
                          {t('holdings.table.acquisitionCost')}
                        </TableHead>
                        <TableHead className="min-w-28">
                          {t('common.status')}
                        </TableHead>
                        <TableHead className="whitespace-nowrap">
                          {t('common.createdAt')}
                        </TableHead>
                        <TableHead className="w-16 text-right">
                          {t('common.actions')}
                        </TableHead>
                      </TableRow>
                    </TableHeader>

                    <TableBody>
                      {holdings.map((holding) => {
                        const entry = catalog.getEntry(
                          holding.assetReference,
                        );
                        const isActive =
                          holding.status === 'ACTIVE';

                        return (
                          <TableRow key={holding.id}>
                            <TableCell>
                              <div className="flex min-w-0 flex-col gap-1">
                                <span className="truncate font-medium text-foreground">
                                  {entry?.name ??
                                    t('common.unknownAsset')}
                                </span>
                                <div className="flex flex-wrap items-center gap-1">
                                  {entry?.symbol && (
                                    <span className="text-xs text-muted-foreground">
                                      {entry.symbol}
                                    </span>
                                  )}
                                  {entry?.isUserAsset && (
                                    <Badge
                                      variant="outline"
                                      className="text-[10px]"
                                    >
                                      {t('common.custom')}
                                    </Badge>
                                  )}
                                </div>
                              </div>
                            </TableCell>

                            <TableCell className="whitespace-nowrap text-muted-foreground">
                              {formatDateTime(
                                holding.snapshotAt,
                              )}
                            </TableCell>

                            <TableCell className="text-right tabular-nums">
                              {formatQuantity(holding.quantity)}
                            </TableCell>

                            <TableCell className="text-right tabular-nums">
                              {formatCurrency(
                                holding.acquisitionCost,
                                holding.currency,
                              )}
                            </TableCell>

                            <TableCell>
                              <div className="flex flex-wrap gap-1">
                                <Badge
                                  variant={
                                    isActive
                                      ? 'default'
                                      : 'outline'
                                  }
                                >
                                  {isActive
                                    ? t('holdings.status.active')
                                    : t(
                                        'holdings.status.superseded',
                                      )}
                                </Badge>
                              </div>
                              {holding.supersededAt && (
                                <p className="mt-1 text-xs text-muted-foreground">
                                  {formatDateTime(
                                    holding.supersededAt,
                                  )}
                                </p>
                              )}
                            </TableCell>

                            <TableCell className="whitespace-nowrap text-muted-foreground">
                              {formatDateTime(holding.createdAt)}
                            </TableCell>

                            <TableCell>
                              <div className="flex items-center justify-end">
                                {isActive && (
                                  <Button
                                    type="button"
                                    variant="ghost"
                                    size="icon-sm"
                                    onClick={() =>
                                      setHistoryHolding(holding)
                                    }
                                    aria-label={t(
                                      'holdings.history.action',
                                    )}
                                  >
                                    <History
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

      {historyHolding && (
        <ReplaceHoldingHistoryDialog
          key={historyHolding.id}
          holding={historyHolding}
          onOpenChange={(open) => {
            if (!open) {
              setHistoryHolding(null);
            }
          }}
        />
      )}
    </div>
  );
}
