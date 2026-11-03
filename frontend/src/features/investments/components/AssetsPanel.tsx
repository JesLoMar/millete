import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import {
  LineChart,
  Pencil,
  Plus,
  RefreshCw,
  Search,
  Tag,
  X,
} from 'lucide-react';

import { Badge } from '@/shared/components/core/badge';
import { Button } from '@/shared/components/core/button';
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

import { useDebouncedValue } from '../hooks/useDebouncedValue';
import {
  useSharedAssets,
  useUserAssets,
} from '../hooks/useInvestmentQueries';
import type { SharedAsset, UserAsset } from '../types';
import { formatDate } from '../utils';
import { AssetFormDialog } from './AssetFormDialog';
import { FxRatesCard } from './FxRatesCard';
import { MarketPricesDialog } from './MarketPricesDialog';
import { PanelState } from './PanelState';
import { RegisterAssetPriceDialog } from './RegisterAssetPriceDialog';

interface AssetsPanelProps {
  onOpenMarketDataRefresh: () => void;
}

type AssetFormState =
  | { mode: 'create' }
  | { mode: 'edit'; asset: UserAsset };

export function AssetsPanel({
  onOpenMarketDataRefresh,
}: AssetsPanelProps) {
  const { t } = useTranslation('investments');
  const localCurrency = getLocalCurrencyCode();

  const [search, setSearch] = useState('');
  const debouncedSearch = useDebouncedValue(search.trim(), 300);

  const sharedQuery = useSharedAssets(
    debouncedSearch || undefined,
  );
  const userQuery = useUserAssets(
    debouncedSearch || undefined,
  );

  const [assetForm, setAssetForm] =
    useState<AssetFormState | null>(null);
  const [priceAsset, setPriceAsset] =
    useState<UserAsset | null>(null);
  const [pricesAsset, setPricesAsset] =
    useState<SharedAsset | null>(null);

  const sharedAssets = sharedQuery.data ?? [];
  const userAssets = userQuery.data ?? [];

  return (
    <div className="space-y-4 sm:space-y-6">
      <Card>
        <CardContent className="space-y-4 pt-6">
          <div className="flex flex-col justify-between gap-3 lg:flex-row lg:items-center">
            <div className="min-w-0">
              <h2 className="text-lg font-semibold text-foreground">
                {t('assets.title')}
              </h2>
              <p className="text-sm text-muted-foreground">
                {t('assets.description')}
              </p>
            </div>

            <div className="flex flex-wrap items-center gap-2">
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={onOpenMarketDataRefresh}
                className="gap-1.5"
              >
                <RefreshCw
                  className="size-4"
                  aria-hidden="true"
                />
                {t('marketData.refresh')}
              </Button>
              <Button
                type="button"
                size="sm"
                onClick={() =>
                  setAssetForm({ mode: 'create' })
                }
                className="gap-1.5"
              >
                <Plus className="size-4" aria-hidden="true" />
                {t('assets.newAsset')}
              </Button>
            </div>
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
                setSearch(event.target.value)
              }
              placeholder={t('assets.searchPlaceholder')}
              aria-label={t('assets.searchPlaceholder')}
              className="pl-9 pr-9"
            />
            {search && (
              <button
                type="button"
                onClick={() => setSearch('')}
                className="absolute top-1/2 right-2 -translate-y-1/2 rounded p-1 text-muted-foreground hover:text-foreground"
                aria-label={t('common.clear')}
              >
                <X className="size-4" aria-hidden="true" />
              </button>
            )}
          </div>
        </CardContent>
      </Card>

      <Card className="overflow-hidden">
        <CardHeader className="border-b border-border">
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
                {!debouncedSearch && (
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    onClick={() =>
                      setAssetForm({ mode: 'create' })
                    }
                  >
                    {t('assets.newAsset')}
                  </Button>
                )}
              </div>
            )}

          {!userQuery.isLoading &&
            !userQuery.isError &&
            userAssets.length > 0 && (
              <div className="overflow-x-auto">
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead className="min-w-48">
                        {t('common.asset')}
                      </TableHead>
                      <TableHead>{t('assets.sector')}</TableHead>
                      <TableHead>{t('assets.origin')}</TableHead>
                      <TableHead>{t('common.currency')}</TableHead>
                      <TableHead>{t('common.createdAt')}</TableHead>
                      <TableHead className="w-24 text-right">
                        {t('common.actions')}
                      </TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {userAssets.map((asset) => (
                      <TableRow key={asset.id}>
                        <TableCell>
                          <div className="flex min-w-0 flex-col gap-1">
                            <span className="truncate font-medium text-foreground">
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
                                {t('common.custom')}
                              </Badge>
                            </div>
                          </div>
                        </TableCell>
                        <TableCell className="text-muted-foreground">
                          {asset.sector?.displayName ??
                            t('common.none')}
                        </TableCell>
                        <TableCell>
                          <Badge variant="outline">
                            {t(
                              `assets.origins.${asset.origin}`,
                            )}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <Badge variant="secondary">
                            {asset.currency}
                          </Badge>
                        </TableCell>
                        <TableCell className="whitespace-nowrap text-muted-foreground">
                          {formatDate(asset.createdAt)}
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
                              onClick={() =>
                                setAssetForm({
                                  mode: 'edit',
                                  asset,
                                })
                              }
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
        </CardContent>
      </Card>

      <Card className="overflow-hidden">
        <CardHeader className="border-b border-border">
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
                {debouncedSearch
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
                      <TableHead className="min-w-48">
                        {t('common.asset')}
                      </TableHead>
                      <TableHead>{t('assets.sector')}</TableHead>
                      <TableHead>{t('common.currency')}</TableHead>
                      <TableHead className="min-w-28">
                        {t('common.status')}
                      </TableHead>
                      <TableHead className="w-24 text-right">
                        {t('common.actions')}
                      </TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {sharedAssets.map((asset) => (
                      <TableRow key={asset.id}>
                        <TableCell>
                          <div className="flex min-w-0 flex-col gap-1">
                            <span className="truncate font-medium text-foreground">
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
                        <TableCell className="text-muted-foreground">
                          {asset.sector?.displayName ??
                            t('common.none')}
                        </TableCell>
                        <TableCell>
                          <Badge variant="secondary">
                            {asset.currency}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <Badge variant="outline">
                            {t('assets.catalogAsset')}
                          </Badge>
                        </TableCell>
                        <TableCell>
                          <div className="flex items-center justify-end">
                            <Button
                              type="button"
                              variant="ghost"
                              size="icon"
                              className="size-8"
                              onClick={() =>
                                setPricesAsset(asset)
                              }
                              aria-label={t(
                                'assets.prices.action',
                              )}
                            >
                              <LineChart
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
        </CardContent>
      </Card>

      <FxRatesCard defaultBaseCurrency={localCurrency} />

      {assetForm && (
        <AssetFormDialog
          key={
            assetForm.mode === 'create'
              ? 'create'
              : `edit:${assetForm.asset.id}`
          }
          open
          onOpenChange={(open) => {
            if (!open) {
              setAssetForm(null);
            }
          }}
          asset={
            assetForm.mode === 'edit'
              ? assetForm.asset
              : null
          }
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

      {pricesAsset && (
        <MarketPricesDialog
          key={pricesAsset.id}
          asset={pricesAsset}
          onOpenChange={(open) => {
            if (!open) {
              setPricesAsset(null);
            }
          }}
        />
      )}
    </div>
  );
}
