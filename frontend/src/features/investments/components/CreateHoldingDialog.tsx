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
import { getLocalCurrencyCode } from '@/shared/utils/i18nFormat';

import { useInvestmentMutations } from '../hooks/useInvestmentMutations';
import { useAssetCatalog } from '../hooks/useInvestmentQueries';
import type { AssetReference } from '../types';
import {
  fromDateTimeInputValue,
  isFutureDateTime,
  nowDateTimeInputValue,
  parseNumericInput,
} from '../utils';
import { AssetPicker } from './AssetPicker';

interface CreateHoldingDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

/**
 * Alta de un Holding: fotografía de una posición que ya existía antes de
 * empezar a usar el módulo. A partir de ese instante se registran operaciones.
 */
export function CreateHoldingDialog({
  open,
  onOpenChange,
}: CreateHoldingDialogProps) {
  const { t } = useTranslation('investments');
  const { createHolding } = useInvestmentMutations();
  const catalog = useAssetCatalog();
  const localCurrency = getLocalCurrencyCode();

  const [asset, setAsset] = useState<AssetReference | null>(null);
  const [snapshotAt, setSnapshotAt] = useState(
    nowDateTimeInputValue,
  );
  const [quantity, setQuantity] = useState('');
  const [acquisitionCost, setAcquisitionCost] = useState('');
  const [showValidation, setShowValidation] = useState(false);

  const entry = catalog.getEntry(asset);
  const assetCurrency = entry?.currency ?? localCurrency;

  const quantityValue = parseNumericInput(quantity);
  const acquisitionCostValue =
    parseNumericInput(acquisitionCost);
  const snapshotIso = snapshotAt
    ? fromDateTimeInputValue(snapshotAt)
    : null;

  const isSnapshotInFuture = isFutureDateTime(snapshotIso);

  const isValid =
    asset != null &&
    snapshotIso != null &&
    !isSnapshotInFuture &&
    quantityValue != null &&
    quantityValue > 0 &&
    acquisitionCostValue != null &&
    acquisitionCostValue >= 0;

  const handleSubmit = async () => {
    if (!isValid) {
      setShowValidation(true);
      return;
    }

    try {
      await createHolding.mutateAsync({
        assetReference: asset as AssetReference,
        snapshotAt: snapshotIso as string,
        quantity: quantityValue as number,
        acquisitionCost: acquisitionCostValue as number,
      });
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
            <DialogTitle>{t('holdings.create.title')}</DialogTitle>
            <DialogDescription>
              {t('holdings.create.description')}
            </DialogDescription>
          </DialogHeader>

          <AssetPicker
            value={asset}
            onChange={setAsset}
            disabled={createHolding.isPending}
          />

          <div className="space-y-2">
            <Label htmlFor="holding-snapshot-at">
              {t('holdings.create.snapshotAt')}
            </Label>
            <Input
              id="holding-snapshot-at"
              type="datetime-local"
              value={snapshotAt}
              max={nowDateTimeInputValue()}
              onChange={(event) =>
                setSnapshotAt(event.target.value)
              }
              disabled={createHolding.isPending}
            />
            <p className="text-xs text-muted-foreground">
              {t('holdings.create.snapshotAtHelp')}
            </p>
          </div>

          <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
            <div className="space-y-2">
              <Label htmlFor="holding-quantity">
                {t('common.quantity')}
              </Label>
              <Input
                id="holding-quantity"
                type="number"
                min="0"
                step="any"
                value={quantity}
                onChange={(event) =>
                  setQuantity(event.target.value)
                }
                disabled={createHolding.isPending}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="holding-cost">
                {t('holdings.create.acquisitionCost', {
                  currency: assetCurrency,
                })}
              </Label>
              <Input
                id="holding-cost"
                type="number"
                min="0"
                step="any"
                value={acquisitionCost}
                onChange={(event) =>
                  setAcquisitionCost(event.target.value)
                }
                disabled={createHolding.isPending}
              />
            </div>
          </div>

          <div className="space-y-1 rounded-md border border-border bg-muted/40 p-3 text-xs text-muted-foreground">
            <p>{t('holdings.create.currencyHint', {
              currency: assetCurrency,
            })}</p>
            <p>{t('holdings.create.singleActiveHint')}</p>
            <p>{t('holdings.create.noActivityHint')}</p>
          </div>

          {showValidation && !isValid && (
            <p className="text-sm text-destructive">
              {isSnapshotInFuture
                ? t('holdings.create.futureSnapshot')
                : t('holdings.create.invalid')}
            </p>
          )}
        </div>

        <DialogFooter>
          <Button
            type="button"
            variant="outline"
            onClick={() => onOpenChange(false)}
            disabled={createHolding.isPending}
          >
            {t('common:cancel')}
          </Button>
          <Button
            type="button"
            onClick={() => void handleSubmit()}
            disabled={createHolding.isPending}
          >
            {createHolding.isPending && (
              <Loader2
                className="size-4 animate-spin"
                aria-hidden="true"
              />
            )}
            {t('holdings.create.submit')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
