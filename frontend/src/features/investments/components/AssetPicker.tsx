import { memo, useCallback, useEffect, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Search } from 'lucide-react';
import { Input } from '@/shared/components/core/input';
import { Label } from '@/shared/components/core/label';
import { cn } from '@/lib/utils';
import { useSharedAssets, useUserAssets } from '../hooks/useInvestmentQueries';
import type { AssetReference } from '../types';

interface AssetPickerProps {
  value: AssetReference | null;
  onChange: (reference: AssetReference | null) => void;
  disabled?: boolean;
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
}: AssetPickerProps) {
  const { t } = useTranslation('investments');
  const [search, setSearch] = useState('');
  const [debouncedSearch, setDebouncedSearch] = useState('');

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedSearch(search), 300);
    return () => clearTimeout(timer);
  }, [search]);

  const { data: sharedAssets, isLoading: isLoadingShared } = useSharedAssets({
    search: debouncedSearch || undefined,
  });

  const { data: userAssets, isLoading: isLoadingUser } = useUserAssets();

  const options: AssetOption[] = [];

  if (userAssets) {
    for (const asset of userAssets) {
      options.push({
        reference: { kind: 'USER', id: asset.id },
        name: asset.name,
        symbol: null,
        currency: asset.currency,
        isUserAsset: true,
      });
    }
  }

  if (sharedAssets) {
    for (const asset of sharedAssets) {
      options.push({
        reference: { kind: 'SHARED', id: asset.id },
        name: asset.name,
        symbol: asset.symbol ?? null,
        currency: asset.currency,
        isUserAsset: false,
      });
    }
  }

  const selectedOption = options.find(
    (option) =>
      option.reference.kind === value?.kind &&
      option.reference.id === value?.id,
  );

  const handleSelect = useCallback(
    (option: AssetOption) => {
      onChange(option.reference);
    },
    [onChange],
  );

  const isLoading = isLoadingShared || isLoadingUser;

  return (
    <div className="space-y-2">
      <Label htmlFor="asset-search">{t('activities.asset')}</Label>
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

      {selectedOption && (
        <div className="flex items-center justify-between rounded-md border border-border bg-muted/50 px-3 py-2">
          <div className="min-w-0 flex-1">
            <p className="truncate text-sm font-medium text-foreground">
              {selectedOption.name}
            </p>
            <p className="text-xs text-muted-foreground">
              {selectedOption.symbol ?? '—'} · {selectedOption.currency}
              {selectedOption.isUserAsset &&
                ` · ${t('activities.userAsset')}`}
            </p>
          </div>
          <button
            type="button"
            onClick={() => onChange(null)}
            disabled={disabled}
            className="ml-2 text-xs text-muted-foreground hover:text-foreground"
          >
            {t('common:actions.close')}
          </button>
        </div>
      )}

      {!selectedOption && (
        <div className="max-h-48 overflow-y-auto rounded-md border border-border">
          {isLoading ? (
            <p className="p-3 text-sm text-muted-foreground">
              {t('common:status.loading')}
            </p>
          ) : options.length === 0 ? (
            <p className="p-3 text-sm text-muted-foreground">
              {t('activities.noAssetsFound')}
            </p>
          ) : (
            <ul className="divide-y divide-border">
              {options.map((option) => (
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
      )}
    </div>
  );
});