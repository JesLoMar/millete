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

import { useInvestmentMutations } from '../hooks/useInvestmentMutations';
import type { UserAsset } from '../types';
import { parseNumericInput } from '../utils';

interface RegisterAssetPriceDialogProps {
  asset: UserAsset;
  onOpenChange: (open: boolean) => void;
}

export function RegisterAssetPriceDialog({
  asset,
  onOpenChange,
}: RegisterAssetPriceDialogProps) {
  const { t } = useTranslation('investments');
  const { registerUserAssetPrice } = useInvestmentMutations();
  const [unitPrice, setUnitPrice] = useState('');
  const [showValidation, setShowValidation] = useState(false);

  const value = parseNumericInput(unitPrice);
  const isValid = value != null && value >= 0;

  const handleSubmit = async () => {
    if (!isValid) {
      setShowValidation(true);
      return;
    }

    try {
      await registerUserAssetPrice.mutateAsync({
        assetId: asset.id,
        data: { unitPrice: value as number },
      });
      onOpenChange(false);
    } catch {
      // La mutation muestra el error y el diálogo permanece abierto.
    }
  };

  return (
    <Dialog open onOpenChange={onOpenChange}>
      <DialogContent className="border-border bg-card sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{t('assets.price.title')}</DialogTitle>
          <DialogDescription>
            {t('assets.price.description', {
              name: asset.name,
            })}
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-2">
          <Label htmlFor="user-asset-price">
            {t('assets.price.unitPrice', {
              currency: asset.currency,
            })}
          </Label>
          <Input
            id="user-asset-price"
            type="number"
            min="0"
            step="any"
            value={unitPrice}
            onChange={(event) =>
              setUnitPrice(event.target.value)
            }
            disabled={registerUserAssetPrice.isPending}
          />
          <p className="text-xs text-muted-foreground">
            {t('assets.price.help')}
          </p>
        </div>

        {showValidation && !isValid && (
          <p className="text-sm text-destructive">
            {t('assets.price.invalid')}
          </p>
        )}

        <DialogFooter>
          <Button
            type="button"
            variant="outline"
            onClick={() => onOpenChange(false)}
            disabled={registerUserAssetPrice.isPending}
          >
            {t('common:cancel')}
          </Button>
          <Button
            type="button"
            onClick={() => void handleSubmit()}
            disabled={registerUserAssetPrice.isPending}
          >
            {registerUserAssetPrice.isPending && (
              <Loader2
                className="size-4 animate-spin"
                aria-hidden="true"
              />
            )}
            {t('assets.price.submit')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
