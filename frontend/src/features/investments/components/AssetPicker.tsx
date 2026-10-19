import { memo, useCallback, useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Search } from 'lucide-react';
import { Input } from '@/shared/components/core/input';
import { Label } from '@/shared/components/core/label';
import { cn } from '@/lib/utils';
import { useSharedAssets, useUserAssets } from '../hooks/useInvestmentQueries';
import { isSameAssetReference } from '../utils';
import type { AssetReference } from '../types';

interface AssetPickerProps {
  value: AssetReference | null;
  onChange: (reference: AssetReference | null) => void;
  disabled?: boolean;
  optional?: boolean;
}

interface AssetOption {
  reference: AssetReference;
  name: string;
  symbol: string | null;
  currency: string;
  isUserAsset: boolean;
}

export const AssetPicker = memo(function AssetPicker({
  value,
  onChange,
  disabled = false,
  optional = false,
}: AssetPickerProps) {
  const { t } = useTranslation('investments');
  const [search, setSearch] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');
  // Guarda la opción elegida para mostrarla aunque la búsqueda cambie.
  const [picked, setPicked] = useState<AssetOption | null>(null);

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedSearch(search), 300);
    return () => clearTimeout(timer);
  }, [search]);

  const { data: sharedAssets, isLoading: isLoadingShared, isError: isErrorShared } =
    useSharedAssets({ search: debouncedSearch || undefined });

  const { data: userAssets, isLoading: isLoadingUser } = useUserAssets();

  const term = debouncedSearch.trim().toLowerCase();

  // Los activos personales no llegan filtrados desde la API: se filtran aquí.
  const userOptions = useMemo<AssetOption[]>(
    () =>
      (userAssets ?? [])
        .filter((asset) => !term || asset.name.toLowerCase().includes(term))
        .map((asset) => ({
          reference: { kind: 'USER', id: asset.id },
          name: asset.name,
          symbol: null,
          currency: asset.currency,
          isUserAsset: true,
        })),
    [userAssets, term],
  );

  const sharedOptions = useMemo<AssetOption[]>(
    () =>
      (sharedAssets ?? []).map((asset) => ({
        reference: { kind: 'SHARED', id: asset.id },
        name: asset.name,
        symbol: asset.symbol ?? null,
        currency: asset.currency,
        isUserAsset: false,
      })),
    [sharedAssets],
  );

  const allOptions = useMemo(
    () => [...userOptions, ...sharedOptions],
    [userOptions, sharedOptions],
  );

  const selected: AssetOption | null = useMemo(() => {
    if (!value) return null;
    if (picked && isSameAssetReference(picked.reference, value)) return picked;
    return allOptions.find((o) => isSameAssetReference(o.reference, value)) ?? null;
  }, [value, picked, allOptions]);

  const handleSelect = useCallback(
    (option: AssetOption) => {
      setPicked(option);
      onChange(option.reference);
    },
    [onChange],
  );

  const handleClear = useCallback(() => {
    setPicked(null);
    onChange(null);
  }, [onChange]);

  const isLoading = isLoadingShared || isLoadingUser;

  return (
    <div className="space-y-2">
      <Label htmlFor="asset-search">
        {t('activities.asset')}
        {optional && (
          <span className="ml-1 text-xs text-muted-foreground">
            ({t('activities.optional', { defaultValue: 'opcional' })})
          </span>
        )}
      </Label>

      {value && (
        <div className="flex items-center justify-between rounded-md border border-border bg-muted/50 px-3 py-2">
          <div className="min-w-0 flex-1">
            <p className="truncate text-sm font-medium text-foreground">
              {selected?.name ?? t('activities.selectedAsset', { defaultValue: 'Activo seleccionado' })}
            </p>
            {selected && (
              <p className="text-xs text-muted-foreground">
                {selected.symbol ?? '—'} · {selected.currency}
                {selected.isUserAsset && ` · ${t('activities.userAsset')}`}
              </p>
            )}
          </div>
          <button
            type="button"
            onClick={handleClear}
            disabled={disabled}
            className="ml-2 text-xs text-muted-foreground hover:text-foreground"
          >
            {t('activities.changeAsset', { defaultValue: 'Cambiar' })}
          </button>
        </div>
      )}

      {!value && (
        <>
          <div className="relative">
            <Search
              className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground"
              aria-hidden="true"
            />
            <Input
              id="asset-search"
              type="search"
              placeholder={t('activities.searchAsset')}
              value={search}
              onChange={(event) => setSearch(event.target.value)}
              disabled={disabled}
              className="pl-10"
            />
          </div>

          <div className="max-h-48 overflow-y-auto rounded-md border border-border">
            {isErrorShared && (
              <p className="p-3 text-sm text-muted-foreground">
                {t('activities.assetsLoadError', {
                  defaultValue: 'No se pudo cargar el catálogo. Puedes usar tus activos personales.',
                })}
              </p>
            )}
            {isLoading ? (
              <p className="p-3 text-sm text-muted-foreground">
                {t('common:status.loading')}
              </p>
            ) : allOptions.length === 0 ? (
              <p className="p-3 text-sm text-muted-foreground">
                {t('activities.noAssetsFound')}
              </p>
            ) : (
              <ul className="divide-y divide-border">
                {allOptions.map((option) => (
                  <li key={`${option.reference.kind}-${option.reference.id}`}>
                    <button
                      type="button"
                      onClick={() => handleSelect(option)}
                      disabled={disabled}
                      className={cn(
                        'flex w-full items-center justify-between px-3 py-2 text-left transition-colors',
                        'hover:bg-accent/50 disabled:opacity-50',
                      )}
                    >
                      <div className="min-w-0 flex-1">
                        <p className="truncate text-sm font-medium text-foreground">
                          {option.name}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          {option.symbol ?? '—'} · {option.currency}
                        </p>
                      </div>
                      {option.isUserAsset && (
                        <span className="ml-2 text-xs text-primary">
                          {t('activities.userAsset')}
                        </span>
                      )}
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
});