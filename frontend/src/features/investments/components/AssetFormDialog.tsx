import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Loader2 } from 'lucide-react';

import { Button } from '@/shared/components/core/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/core/dialog';
import { Input } from '@/shared/components/core/input';
import { Label } from '@/shared/components/core/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/core/select';
import { getLocalCurrencyCode } from '@/shared/utils/i18nFormat';

import {
  ASSET_ORIGINS,
  ASSET_TYPES,
} from '../constants';
import { useInvestmentMutations } from '../hooks/useInvestmentMutations';
import { useSectors } from '../hooks/useInvestmentQueries';
import type {
  AssetOrigin,
  AssetType,
  UserAsset,
} from '../types';
import { parseNumericInput } from '../utils';

interface AssetFormDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  asset?: UserAsset | null;
}

export function AssetFormDialog({
  open,
  onOpenChange,
  asset = null,
}: AssetFormDialogProps) {
  const { t } = useTranslation('investments');
  const { createUserAsset, updateUserAsset } =
    useInvestmentMutations();
  const sectorsQuery = useSectors();
  const localCurrency = getLocalCurrencyCode();
  const isEditing = asset != null;

  const [name, setName] = useState(asset?.name ?? '');
  const [type, setType] = useState<AssetType>(
    asset?.type ?? 'STOCK',
  );
  const [sector, setSector] = useState(
    asset?.sector?.code ?? '',
  );
  const [origin, setOrigin] = useState<AssetOrigin>(
    asset?.origin ?? 'ACQUIRED',
  );
  const [initialPrice, setInitialPrice] = useState('0');
  const [showValidation, setShowValidation] = useState(false);

  const initialPriceValue = parseNumericInput(initialPrice);
  const normalizedSector = sector.trim().toUpperCase();

  const isValid =
    name.trim().length > 0 &&
    name.trim().length <= 120 &&
    normalizedSector.length > 0 &&
    normalizedSector.length <= 120 &&
    (isEditing ||
      (initialPriceValue != null && initialPriceValue >= 0));

  const isPending =
    createUserAsset.isPending || updateUserAsset.isPending;

  const handleSubmit = async () => {
    if (!isValid) {
      setShowValidation(true);
      return;
    }

    try {
      if (isEditing) {
        await updateUserAsset.mutateAsync({
          id: asset.id,
          data: {
            name: name.trim(),
            sector: normalizedSector,
          },
        });
      } else {
        await createUserAsset.mutateAsync({
          name: name.trim(),
          type,
          sector: normalizedSector,
          origin,
          initialPrice: initialPriceValue as number,
        });
      }

      onOpenChange(false);
    } catch {
      // La mutation muestra el error y el diálogo permanece abierto.
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="border-border bg-card sm:max-w-lg">
        <div className="max-h-[85dvh] space-y-4 overflow-y-auto pr-1">
          <DialogHeader>
            <DialogTitle>
              {isEditing
                ? t('assets.form.editTitle')
                : t('assets.form.createTitle')}
            </DialogTitle>
            <DialogDescription>
              {isEditing
                ? t('assets.form.editDescription')
                : t('assets.form.createDescription')}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-2">
            <Label htmlFor="asset-name">
              {t('assets.form.name')}
            </Label>
            <Input
              id="asset-name"
              value={name}
              maxLength={120}
              onChange={(event) => setName(event.target.value)}
              placeholder={t('assets.form.namePlaceholder')}
              disabled={isPending}
            />
          </div>

          {!isEditing && (
            <div className="space-y-2">
              <Label>{t('assets.form.type')}</Label>
              <Select
                value={type}
                onValueChange={(value) =>
                  setType(value as AssetType)
                }
                disabled={isPending}
              >
                <SelectTrigger className="w-full">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {ASSET_TYPES.map((assetType) => (
                    <SelectItem
                      key={assetType}
                      value={assetType}
                    >
                      {t(`assets.types.${assetType}`)}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          )}

          <div className="space-y-2">
            <Label htmlFor="asset-sector">
              {t('assets.form.sector')}
            </Label>
            <Input
              id="asset-sector"
              value={sector}
              list="investment-sector-codes"
              maxLength={120}
              onChange={(event) =>
                setSector(event.target.value.toUpperCase())
              }
              placeholder={t('assets.form.sectorPlaceholder')}
              disabled={isPending}
              className="uppercase"
            />
            <datalist id="investment-sector-codes">
              {(sectorsQuery.data ?? []).map((item) => (
                <option key={item.code} value={item.code}>
                  {item.displayName}
                </option>
              ))}
            </datalist>
            <p className="text-xs text-muted-foreground">
              {t('assets.form.sectorHelp')}
            </p>
          </div>

          {!isEditing && (
            <>
              <div className="space-y-2">
                <Label>{t('assets.form.origin')}</Label>
                <Select
                  value={origin}
                  onValueChange={(value) =>
                    setOrigin(value as AssetOrigin)
                  }
                  disabled={isPending}
                >
                  <SelectTrigger className="w-full">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {ASSET_ORIGINS.map((assetOrigin) => (
                      <SelectItem
                        key={assetOrigin}
                        value={assetOrigin}
                      >
                        {t(
                          `assets.origins.${assetOrigin}`,
                        )}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
                <p className="text-xs text-muted-foreground">
                  {t('assets.form.originHelp')}
                </p>
              </div>

              <div className="space-y-2">
                <Label htmlFor="asset-initial-price">
                  {t('assets.form.initialPrice', {
                    currency: localCurrency,
                  })}
                </Label>
                <Input
                  id="asset-initial-price"
                  type="number"
                  min="0"
                  step="any"
                  value={initialPrice}
                  onChange={(event) =>
                    setInitialPrice(event.target.value)
                  }
                  disabled={isPending}
                />
                <p className="text-xs text-muted-foreground">
                  {t('assets.form.initialPriceHelp')}
                </p>
              </div>
            </>
          )}

          {isEditing && (
            <p className="rounded-md border border-border bg-muted/40 p-3 text-xs text-muted-foreground">
              {t('assets.form.editImmutableHint')}
            </p>
          )}

          {showValidation && !isValid && (
            <p className="text-sm text-destructive">
              {t('assets.form.invalid')}
            </p>
          )}
        </div>

        <DialogFooter>
          <Button
            type="button"
            variant="outline"
            onClick={() => onOpenChange(false)}
            disabled={isPending}
          >
            {t('common:cancel')}
          </Button>
          <Button
            type="button"
            onClick={() => void handleSubmit()}
            disabled={isPending}
          >
            {isPending && (
              <Loader2
                className="size-4 animate-spin"
                aria-hidden="true"
              />
            )}
            {isEditing
              ? t('assets.form.submitUpdate')
              : t('assets.form.submitCreate')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
