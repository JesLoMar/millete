import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Search, X } from 'lucide-react';

import { Input } from '@/shared/components/core/input';
import { Label } from '@/shared/components/core/label';
import { Badge } from '@/shared/components/core/badge';
import { cn } from '@/lib/utils';

import { useDebouncedValue } from '../hooks/useDebouncedValue';
import {
  useAssetCatalog,
  useSharedAssets,
  useUserAssets,
} from '../hooks/useInvestmentQueries';
import type { AssetReference } from '../types';
import { assetKey } from '../utils';

interface AssetPickerProps {
  value: AssetReference | null;
  onChange: (reference: AssetReference | null) => void;
  disabled?: boolean;
  optional?: boolean;
  label?: string;
  className?: string;
}

interface AssetOption {
  reference: AssetReference;
  name: string;
  symbol: string | null;
  type: string | null;
  sector: string | null;
  currency: string;
  isUserAsset: boolean;
}

export function AssetPicker({
  value,
  onChange,
  disabled = false,
  optional = false,
  label,
  className,
}: AssetPickerProps) {
  const { t } = useTranslation('investments');
  const [search, setSearch] = useState('');
  const debouncedSearch = useDebouncedValue(search.trim(), 300);

  const sharedQuery = useSharedAssets(
    debouncedSearch || undefined,
  );
  const userQuery = useUserAssets(
    debouncedSearch || undefined,
  );
  const catalog = useAssetCatalog();

  const options = useMemo<AssetOption[]>(() => {
    const shared = (sharedQuery.data ?? []).map<AssetOption>(
      (asset) => ({
        reference: { kind: 'SHARED', id: asset.id },
        name: asset.name,
        symbol: asset.symbol,
        type: asset.type,
        sector: asset.sector?.displayName ?? null,
        currency: asset.currency,
        isUserAsset: false,
      }),
    );

    const user = (userQuery.data ?? []).map<AssetOption>(
      (asset) => ({
        reference: { kind: 'USER', id: asset.id },
        name: asset.name,
        symbol: null,
        type: asset.type,
        sector: asset.sector?.displayName ?? null,
        currency: asset.currency,
        isUserAsset: true,
      }),
    );

    return [...shared, ...user];
  }, [sharedQuery.data, userQuery.data]);

  const selected = useMemo<AssetOption | null>(() => {
    if (!value) {
      return null;
    }

    const found = options.find(
      (option) =>
        assetKey(option.reference) === assetKey(value),
    );

    if (found) {
      return found;
    }

    const entry = catalog.getEntry(value);

    if (!entry) {
      return null;
    }

    return {
      reference: entry.reference,
      name: entry.name,
      symbol: entry.symbol,
      type: entry.type,
      sector: entry.sector?.displayName ?? null,
      currency: entry.currency ?? '',
      isUserAsset: entry.isUserAsset,
    };
  }, [value, options, catalog]);

  const isLoading =
    sharedQuery.isLoading || userQuery.isLoading;

  return (
    <div className={cn('space-y-2', className)}>
      <div className="flex items-center justify-between gap-2">
        <Label>
          {label ?? t('common.asset')}
          {optional && (
            <span className="text-xs font-normal text-muted-foreground">
              ({t('common.optional')})
            </span>
          )}
        </Label>

        {value && (
          <button
            type="button"
            onClick={() => onChange(null)}
            disabled={disabled}
            className="text-xs font-medium text-muted-foreground transition-colors hover:text-foreground disabled:opacity-50"
          >
            {t('common.clear')}
          </button>
        )}
      </div>

      {value ? (
        <div className="flex items-center justify-between gap-3 rounded-md border border-border bg-muted/40 px-3 py-2">
          <div className="min-w-0 flex-1">
            <p className="truncate text-sm font-medium text-foreground">
              {selected?.name ?? t('common.unknownAsset')}
            </p>
            <p className="truncate text-xs text-muted-foreground">
              {[
                selected?.symbol,
                selected?.sector,
                selected?.currency,
              ]
                .filter(Boolean)
                .join(' · ') || t('common.unknownAsset')}
            </p>
          </div>

          <div className="flex shrink-0 items-center gap-1.5">
            {selected?.type && (
              <Badge variant="secondary" className="text-[10px]">
                {t(`assets.types.${selected.type}`, {
                  defaultValue: selected.type,
                })}
              </Badge>
            )}
            {selected?.isUserAsset && (
              <Badge variant="outline" className="text-[10px]">
                {t('common.custom')}
              </Badge>
            )}
          </div>
        </div>
      ) : (
        <>
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
              placeholder={t('common.searchAssets')}
              disabled={disabled}
              className="pl-9"
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

          <div className="max-h-44 overflow-y-auto rounded-md border border-border">
            {isLoading ? (
              <p className="p-3 text-sm text-muted-foreground">
                {t('common.loading')}
              </p>
            ) : options.length === 0 ? (
              <p className="p-3 text-sm text-muted-foreground">
                {t('common.noAssetsFound')}
              </p>
            ) : (
              <ul className="divide-y divide-border">
                {options.map((option) => (
                  <li key={assetKey(option.reference)}>
                    <button
                      type="button"
                      onClick={() => onChange(option.reference)}
                      disabled={disabled}
                      className="flex w-full items-center justify-between gap-3 px-3 py-2 text-left transition-colors hover:bg-accent/50 disabled:opacity-50"
                    >
                      <div className="min-w-0 flex-1">
                        <p className="truncate text-sm font-medium text-foreground">
                          {option.name}
                        </p>
                        <p className="truncate text-xs text-muted-foreground">
                          {[
                            option.symbol,
                            option.sector,
                            option.currency,
                          ]
                            .filter(Boolean)
                            .join(' · ')}
                        </p>
                      </div>

                      <div className="flex shrink-0 items-center gap-1.5">
                        {option.type && (
                          <Badge
                            variant="secondary"
                            className="text-[10px]"
                          >
                            {t(`assets.types.${option.type}`, {
                              defaultValue: option.type,
                            })}
                          </Badge>
                        )}
                        {option.isUserAsset && (
                          <Badge
                            variant="outline"
                            className="text-[10px]"
                          >
                            {t('common.custom')}
                          </Badge>
                        )}
                      </div>
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </>
      )}
    </div>
  );
}
