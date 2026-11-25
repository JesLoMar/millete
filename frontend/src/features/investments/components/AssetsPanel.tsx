import { useState } from 'react';
import type { ReactNode } from 'react';
import { useTranslation } from 'react-i18next';
import {
  Pencil,
  Search,
  Tag,
  X,
} from 'lucide-react';

import { Badge } from '@/shared/components/core/badge';
import { Button } from '@/shared/components/core/button';
import { Pagination } from '@/shared/components/Pagination';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/core/card';
import { Input } from '@/shared/components/core/input';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/core/table';
import { getLocalCurrencyCode } from '@/shared/utils/i18nFormat';

import { ASSET_PAGE_SIZE } from '../constants';
import { useDebouncedValue } from '../hooks/useDebouncedValue';
import {
  useSharedAssets,
  useUserAssets,
} from '../hooks/useInvestmentQueries';
import type { UserAsset } from '../types';
import { formatDate } from '../utils';
import { AssetFormDialog } from './AssetFormDialog';
import { FxRatesCard } from './FxRatesCard';
import { PanelState } from './PanelState';
import { RegisterAssetPriceDialog } from './RegisterAssetPriceDialog';

interface AssetsPanelProps {
  status: ReactNode;
}

const CATALOG_TYPES = ['CRYPTO', 'ETF', 'STOCK'] as const;
const CATALOG_PAGE_SIZE = 15;
const panelClass = 'min-w-0 gap-3 rounded-none border-0 bg-transparent py-0 shadow-none';
const panelHeaderClass = 'border-b border-border px-0 pb-2';

function assetPage<T>(assets: T[], requestedPage: number, pageSize: number) {
  const total = assets.length;
  const totalPages = Math.max(1, Math.ceil(total / pageSize));
  const currentPage = Math.min(requestedPage, totalPages - 1);
  const offset = currentPage * pageSize;
  return {
    rows: assets.slice(offset, offset + pageSize),
    pagination: {
      currentPage,
      totalPages,
      from: total === 0 ? 0 : offset + 1,
      to: Math.min(offset + pageSize, total),
      total,
    },
  };
}

export function AssetsPanel({
  status,
}: AssetsPanelProps) {
  const { t } = useTranslation('investments');
  const localCurrency = getLocalCurrencyCode();

  const [search, setSearch] = useState('');
  const [userPage, setUserPage] = useState(0);
  const [catalogPage, setCatalogPage] = useState(0);
  const [catalogType, setCatalogType] = useState<(typeof CATALOG_TYPES)[number]>('CRYPTO');
  const debouncedSearch = useDebouncedValue(search.trim(), 300);

  function updateSearch(value: string) {
    setSearch(value);
    setUserPage(0);
    setCatalogPage(0);
  }

  const sharedQuery = useSharedAssets(
    debouncedSearch || undefined,
  );
  const userQuery = useUserAssets(
    debouncedSearch || undefined,
  );

  const [editingAsset, setEditingAsset] = useState<UserAsset | null>(null);
  const [priceAsset, setPriceAsset] =
    useState<UserAsset | null>(null);

  const sharedAssets = (sharedQuery.data ?? []).filter((asset) => asset.type === catalogType);
  const userAssets = userQuery.data ?? [];
  const user = assetPage(userAssets, userPage, ASSET_PAGE_SIZE);
  const shared = assetPage(sharedAssets, catalogPage, CATALOG_PAGE_SIZE);

  return (
    <div className="space-y-4 sm:space-y-6">
      <div className="grid items-start gap-3 lg:grid-cols-[minmax(0,2fr)_minmax(0,1fr)]">
        <Card className={panelClass}>
          <CardContent className="space-y-3 px-0">
          <div className="min-w-0">
            <h2 className="text-lg font-semibold text-foreground">
              {t('assets.title')}
            </h2>
            <p className="text-sm text-muted-foreground">
              {t('assets.description')}
            </p>
          </div>

          <div className="relative">
            <Search
              className="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-muted-foreground"
              aria-hidden="true"
            />
            <Input
              type="search"
              value={search}
              onChange={(event) =>
                updateSearch(event.target.value)
              }
              placeholder={t('assets.searchPlaceholder')}
              aria-label={t('assets.searchPlaceholder')}
              className="pl-9 pr-9"
            />
            {search && (
              <button
                type="button"
                onClick={() => updateSearch('')}
                className="absolute top-1/2 right-2 -translate-y-1/2 rounded p-1 text-muted-foreground hover:text-foreground"
                aria-label={t('common.clear')}
              >
                <X className="size-4" aria-hidden="true" />
              </button>
            )}
          </div>
          </CardContent>
        </Card>
        <div className="min-w-0 lg:justify-self-end">{status}</div>
      </div>

      <div className="grid min-w-0 gap-6 xl:grid-cols-[minmax(0,1fr)_minmax(0,1.2fr)_minmax(0,1fr)]">
      <Card className={panelClass}>
        <CardHeader className={panelHeaderClass}>
          <div className="flex flex-wrap items-center justify-between gap-2">
            <div className="flex items-center gap-2">
              <CardTitle>{t('assets.userTitle')}</CardTitle>
              <Badge variant="secondary">
                {userAssets.length}
              </Badge>
            </div>
            <span className="text-xs text-muted-foreground">
              {t('assets.userCurrencyHint', {
                currency: localCurrency,
              })}
            </span>
          </div>
          <p className="text-xs text-muted-foreground">
            {t('assets.userDescription')}
          </p>
        </CardHeader>

        <CardContent className="px-0">
          <PanelState
            isLoading={userQuery.isLoading}
            isError={userQuery.isError}
            onRetry={() => void userQuery.refetch()}
            errorLabel={t('assets.loadError')}
          />

          {!userQuery.isLoading &&
            !userQuery.isError &&
            userAssets.length === 0 && (
              <div className="flex flex-col items-center gap-2 px-6 py-10 text-center">
                <Tag
                  className="size-7 text-muted-foreground/60"
                  aria-hidden="true"
                />
                <p className="text-sm text-muted-foreground">
                  {debouncedSearch
                    ? t('assets.userEmptyFiltered')
                    : t('assets.userEmpty')}
                </p>
              </div>
            )}

          {!userQuery.isLoading &&
            !userQuery.isError &&
            userAssets.length > 0 && (
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>
                        {t('common.asset')}
                      </TableHead>
                      <TableHead>{t('common.currency')}</TableHead>
                      <TableHead className="text-right">
                        {t('common.actions')}
                      </TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {user.rows.map((asset) => (
                      <TableRow key={asset.id}>
                        <TableCell>
                          <div className="flex min-w-0 flex-col gap-1">
                            <span className="whitespace-normal wrap-anywhere font-medium text-foreground">
                              {asset.name}
                            </span>
                            <div className="flex flex-wrap gap-1">
                              <Badge
                                variant="secondary"
                                className="text-[10px]"
                              >
                                {t(
                                  `assets.types.${asset.type}`,
                                )}
                              </Badge>
                              <Badge
                                variant="outline"
                                className="text-[10px]"
                              >
                                {t(`assets.origins.${asset.origin}`)}
                              </Badge>
                            </div>
                            <span className="whitespace-normal text-xs text-muted-foreground">
                              {asset.sector?.displayName ?? t('common.none')} · {formatDate(asset.createdAt)}
                            </span>
                          </div>
                        </TableCell>
                        <TableCell>
                          <Badge variant="secondary">
                            {asset.currency}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <div className="flex items-center justify-end gap-1">
                            <Button
                              type="button"
                              variant="ghost"
                              size="icon"
                              className="size-8"
                              onClick={() => setPriceAsset(asset)}
                              aria-label={t(
                                'assets.price.action',
                              )}
                            >
                              <Tag
                                className="size-4"
                                aria-hidden="true"
                              />
                            </Button>
                            <Button
                              type="button"
                              variant="ghost"
                              size="icon"
                              className="size-8"
                              onClick={() => setEditingAsset(asset)}
                              aria-label={t('common:actions.edit')}
                            >
                              <Pencil
                                className="size-4"
                                aria-hidden="true"
                              />
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}
          <Pagination {...user.pagination}
            onPrev={() => setUserPage(user.pagination.currentPage - 1)}
            onNext={() => setUserPage(user.pagination.currentPage + 1)}
          />
        </CardContent>
      </Card>

      <Card className={`${panelClass} xl:border-s xl:border-border xl:ps-5`}>
        <CardHeader className={panelHeaderClass}>
          <div className="flex flex-wrap items-center justify-between gap-2">
            <div className="flex items-center gap-2">
              <CardTitle>{t('assets.sharedTitle')}</CardTitle>
              <Badge variant="secondary">
                {sharedAssets.length}
              </Badge>
            </div>
          </div>
          <p className="text-xs text-muted-foreground">
            {t('assets.sharedDescription')}
          </p>
          <div className="flex flex-wrap gap-1">
            {CATALOG_TYPES.map((type) => (
              <Button key={type} variant={catalogType === type ? 'secondary' : 'ghost'} size="sm"
                aria-pressed={catalogType === type}
                onClick={() => { setCatalogType(type); setCatalogPage(0); }}>
                {t(`assets.types.${type}`)}
              </Button>
            ))}
          </div>
        </CardHeader>

        <CardContent className="px-0">
          <PanelState
            isLoading={sharedQuery.isLoading}
            isError={sharedQuery.isError}
            onRetry={() => void sharedQuery.refetch()}
            errorLabel={t('assets.loadError')}
          />

          {!sharedQuery.isLoading &&
            !sharedQuery.isError &&
            sharedAssets.length === 0 && (
              <p className="px-6 py-10 text-center text-sm text-muted-foreground">
                {debouncedSearch || sharedQuery.data?.length
                  ? t('assets.sharedEmptyFiltered')
                  : t('assets.sharedEmpty')}
              </p>
            )}

          {!sharedQuery.isLoading &&
            !sharedQuery.isError &&
            sharedAssets.length > 0 && (
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead className="h-8 px-2">
                        {t('common.asset')}
                      </TableHead>
                      <TableHead className="h-8 px-2">{t('common.currency')}</TableHead>
                      <TableHead className="h-8 px-2">{t('assets.sector')}</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {shared.rows.map((asset) => (
                      <TableRow key={asset.id}>
                        <TableCell className="px-2 py-1.5">
                          <div className="flex min-w-0 flex-col gap-0.5 leading-tight">
                            <span className="whitespace-normal wrap-anywhere font-medium text-foreground">
                              {asset.name}
                            </span>
                            <div className="flex flex-wrap items-center gap-1">
                              {asset.symbol && (
                                <span className="text-xs text-muted-foreground">
                                  {asset.symbol}
                                </span>
                              )}
                              <Badge
                                variant="secondary"
                                className="text-[10px]"
                              >
                                {t(
                                  `assets.types.${asset.type}`,
                                )}
                              </Badge>
                            </div>
                          </div>
                        </TableCell>
                        <TableCell className="px-2 py-1.5">
                          <Badge variant="secondary">
                            {asset.currency}
                          </Badge>
                        </TableCell>
                        <TableCell className="whitespace-normal px-2 py-1.5 text-xs text-muted-foreground">
                          {asset.sector?.displayName ?? t('common.none')}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            )}
          <Pagination {...shared.pagination}
            onPrev={() => setCatalogPage(shared.pagination.currentPage - 1)}
            onNext={() => setCatalogPage(shared.pagination.currentPage + 1)}
          />
        </CardContent>
      </Card>

      <div className="min-w-0 xl:border-s xl:border-border xl:ps-5">
        <FxRatesCard defaultBaseCurrency={localCurrency} />
      </div>
      </div>

      {editingAsset && (
        <AssetFormDialog
          canRegisterPositions={false}
          key={editingAsset.id}
          open
          onOpenChange={(open) => {
            if (!open) {
              setEditingAsset(null);
            }
          }}
          asset={editingAsset}
        />
      )}

      {priceAsset && (
        <RegisterAssetPriceDialog
          key={priceAsset.id}
          asset={priceAsset}
          onOpenChange={(open) => {
            if (!open) {
              setPriceAsset(null);
            }
          }}
        />
      )}

    </div>
  );
}
