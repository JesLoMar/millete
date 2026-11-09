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
import { cn } from '@/lib/utils';
import { getLocalCurrencyCode } from '@/shared/utils/i18nFormat';

import {
  CREATABLE_ACTIVITY_TYPES,
  CURRENCIES,
  MAX_COMMENT_LENGTH,
} from '../constants';
import { useInvestmentMutations } from '../hooks/useInvestmentMutations';
import { useAssetCatalog } from '../hooks/useInvestmentQueries';
import type {
  AssetReference,
  CreatableActivityType,
  RegisterActivityRequest,
} from '../types';
import {
  fromDateTimeInputValue,
  nowDateTimeInputValue,
  parseNumericInput,
} from '../utils';
import { AssetPicker } from './AssetPicker';

interface RecordActivityDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  initialType?: CreatableActivityType;
  initialAsset?: AssetReference | null;
}

type DividendMode = 'CASH' | 'IN_KIND';

interface FormState {
  type: CreatableActivityType;
  dividendMode: DividendMode;
  asset: AssetReference | null;
  occurredAt: string;
  quantity: string;
  unitPrice: string;
  amount: string;
  currency: string;
  amountOrigin: string;
  currencyOrigin: string;
  currencyDestination: string;
  ratio: string;
  comment: string;
  idempotencyKey: string;
}

function createInitialState(
  initialType: CreatableActivityType,
  initialAsset: AssetReference | null,
): FormState {
  const localCurrency = getLocalCurrencyCode();

  return {
    type: initialType,
    dividendMode: 'CASH',
    asset: initialAsset,
    occurredAt: nowDateTimeInputValue(),
    quantity: '',
    unitPrice: '',
    amount: '',
    currency: localCurrency,
    amountOrigin: '',
    currencyOrigin: localCurrency,
    currencyDestination: 'USD',
    ratio: '',
    comment: '',
    idempotencyKey: crypto.randomUUID(),
  };
}

export function RecordActivityDialog({
  open,
  onOpenChange,
  initialType = 'BUY',
  initialAsset = null,
}: RecordActivityDialogProps) {
  const { t } = useTranslation('investments');
  const { registerActivity } = useInvestmentMutations();
  const catalog = useAssetCatalog();

  const [form, setForm] = useState<FormState>(() =>
    createInitialState(initialType, initialAsset),
  );
  const [showValidation, setShowValidation] = useState(false);

  const selectedAsset = catalog.getEntry(form.asset);
  const isDividend = form.type === 'DIVIDEND';
  const isTrade =
    form.type === 'BUY' || form.type === 'SELL';
  const isCashDividend =
    isDividend && form.dividendMode === 'CASH';
  const isInKindDividend =
    isDividend && form.dividendMode === 'IN_KIND';
  const needsAsset =
    isTrade ||
    isInKindDividend ||
    form.type === 'SPLIT';
  const showsAssetPicker =
    needsAsset || isCashDividend;

  const update = (patch: Partial<FormState>) => {
    setForm((previous) => ({ ...previous, ...patch }));
  };

  const reset = () => {
    setForm(createInitialState('BUY', null));
    setShowValidation(false);
  };

  const handleOpenChange = (nextOpen: boolean) => {
    if (!nextOpen && registerActivity.isPending) {
      return;
    }

    if (!nextOpen) {
      reset();
    }

    onOpenChange(nextOpen);
  };

  const quantity = parseNumericInput(form.quantity);
  const unitPrice = parseNumericInput(form.unitPrice);
  const amount = parseNumericInput(form.amount);
  const amountOrigin = parseNumericInput(form.amountOrigin);
  const ratio = parseNumericInput(form.ratio);

  const currency = form.currency.trim().toUpperCase();
  const currencyOrigin = form.currencyOrigin
    .trim()
    .toUpperCase();
  const currencyDestination = form.currencyDestination
    .trim()
    .toUpperCase();

  const isValid =
    form.occurredAt.length > 0 &&
    (isTrade
      ? Boolean(form.asset) &&
        quantity != null &&
        quantity > 0 &&
        unitPrice != null &&
        unitPrice > 0
      : isCashDividend
        ? amount != null &&
          amount > 0 &&
          /^[A-Z]{3}$/.test(currency)
        : isInKindDividend
          ? Boolean(form.asset) &&
            quantity != null &&
            quantity > 0
          : form.type === 'DEPOSIT' ||
              form.type === 'WITHDRAW'
            ? amount != null &&
              amount > 0 &&
              /^[A-Z]{3}$/.test(currency)
            : form.type === 'EXCHANGE'
              ? amountOrigin != null &&
                amountOrigin > 0 &&
                /^[A-Z]{3}$/.test(currencyOrigin) &&
                /^[A-Z]{3}$/.test(currencyDestination) &&
                currencyOrigin !== currencyDestination
              : form.type === 'SPLIT'
                ? Boolean(form.asset) &&
                  ratio != null &&
                  ratio > 0
                : false);

  const handleSubmit = async () => {
    if (!isValid) {
      setShowValidation(true);
      return;
    }

    const occurredAt = fromDateTimeInputValue(
      form.occurredAt,
    );
    const comment = form.comment.trim() || null;

    let payload: RegisterActivityRequest;

    if (form.type === 'BUY' || form.type === 'SELL') {
      payload = {
        type: form.type,
        idempotencyKey: form.idempotencyKey,
        occurredAt,
        assetReference: form.asset as AssetReference,
        quantity: quantity as number,
        unitPrice: unitPrice as number,
        comment,
      };
    } else if (isCashDividend) {
      payload = {
        type: 'DIVIDEND',
        dividendMode: 'CASH',
        idempotencyKey: form.idempotencyKey,
        occurredAt,
        assetReference: form.asset,
        amount: amount as number,
        currency,
        comment,
      };
    } else if (isInKindDividend) {
      payload = {
        type: 'DIVIDEND',
        dividendMode: 'IN_KIND',
        idempotencyKey: form.idempotencyKey,
        occurredAt,
        assetReference: form.asset as AssetReference,
        quantity: quantity as number,
        comment,
      };
    } else if (
      form.type === 'DEPOSIT' ||
      form.type === 'WITHDRAW'
    ) {
      payload = {
        type: form.type,
        idempotencyKey: form.idempotencyKey,
        occurredAt,
        amount: amount as number,
        currency,
        comment,
      };
    } else if (form.type === 'EXCHANGE') {
      payload = {
        type: 'EXCHANGE',
        idempotencyKey: form.idempotencyKey,
        occurredAt,
        amountOrigin: amountOrigin as number,
        currencyOrigin,
        currencyDestination,
        comment,
      };
    } else {
      payload = {
        type: 'SPLIT',
        idempotencyKey: form.idempotencyKey,
        occurredAt,
        assetReference: form.asset as AssetReference,
        ratio: ratio as number,
        comment,
      };
    }

    try {
      await registerActivity.mutateAsync(payload);
      reset();
      onOpenChange(false);
    } catch {
      // La mutation muestra el error; mantenemos el diálogo abierto.
    }
  };

  const localCurrency = getLocalCurrencyCode();
  const assetCurrency =
    selectedAsset?.currency ?? localCurrency;

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="border-border bg-card sm:max-w-lg">
        <div className="max-h-[85dvh] space-y-4 overflow-y-auto pr-1">
          <DialogHeader>
            <DialogTitle>{t('record.title')}</DialogTitle>
            <DialogDescription>
              {t('record.description')}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-2">
            <Label>{t('record.type')}</Label>
            <Select
              value={form.type}
              onValueChange={(value) =>
                update({
                  type: value as CreatableActivityType,
                })
              }
              disabled={registerActivity.isPending}
            >
              <SelectTrigger className="w-full">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {CREATABLE_ACTIVITY_TYPES.map((type) => (
                  <SelectItem key={type} value={type}>
                    {t(`record.types.${type}`)}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {isDividend && (
            <div className="space-y-2">
              <Label>{t('record.dividendMode')}</Label>
              <div className="grid grid-cols-2 gap-2">
                {(
                  [
                    ['CASH', 'record.dividendCash'],
                    ['IN_KIND', 'record.dividendInKind'],
                  ] as const
                ).map(([mode, labelKey]) => (
                  <button
                    key={mode}
                    type="button"
                    onClick={() =>
                      update({ dividendMode: mode })
                    }
                    disabled={registerActivity.isPending}
                    className={cn(
                      'rounded-md border px-3 py-2 text-sm font-medium transition-colors',
                      form.dividendMode === mode
                        ? 'border-primary bg-primary/10 text-primary'
                        : 'border-border text-muted-foreground hover:bg-accent/50 hover:text-foreground',
                    )}
                  >
                    {t(labelKey)}
                  </button>
                ))}
              </div>
            </div>
          )}

          {showsAssetPicker && (
            <AssetPicker
              value={form.asset}
              onChange={(asset) => update({ asset })}
              disabled={registerActivity.isPending}
              optional={isCashDividend}
            />
          )}

          <div className="space-y-2">
            <Label htmlFor="activity-occurred-at">
              {t('record.occurredAt')}
            </Label>
            <Input
              id="activity-occurred-at"
              type="datetime-local"
              value={form.occurredAt}
              onChange={(event) =>
                update({ occurredAt: event.target.value })
              }
              disabled={registerActivity.isPending}
            />
          </div>

          {isTrade && (
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="trade-quantity">
                  {t('record.quantity')}
                </Label>
                <Input
                  id="trade-quantity"
                  type="number"
                  min="0"
                  step="any"
                  value={form.quantity}
                  onChange={(event) =>
                    update({ quantity: event.target.value })
                  }
                  disabled={registerActivity.isPending}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="trade-price">
                  {t('record.unitPriceWithCurrency', {
                    currency: assetCurrency,
                  })}
                </Label>
                <Input
                  id="trade-price"
                  type="number"
                  min="0"
                  step="any"
                  value={form.unitPrice}
                  onChange={(event) =>
                    update({ unitPrice: event.target.value })
                  }
                  disabled={registerActivity.isPending}
                />
              </div>

              <p className="text-xs text-muted-foreground sm:col-span-2">
                {t('record.unitPriceHelp', {
                  assetCurrency,
                  localCurrency,
                })}
              </p>
            </div>
          )}

          {(isCashDividend ||
            form.type === 'DEPOSIT' ||
            form.type === 'WITHDRAW') && (
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="cash-amount">
                  {t('record.amount')}
                </Label>
                <Input
                  id="cash-amount"
                  type="number"
                  min="0"
                  step="any"
                  value={form.amount}
                  onChange={(event) =>
                    update({ amount: event.target.value })
                  }
                  disabled={registerActivity.isPending}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="cash-currency">
                  {t('record.currency')}
                </Label>
                <Input
                  id="cash-currency"
                  list="investment-currencies"
                  maxLength={3}
                  value={form.currency}
                  onChange={(event) =>
                    update({
                      currency:
                        event.target.value.toUpperCase(),
                    })
                  }
                  disabled={registerActivity.isPending}
                  className="uppercase"
                />
              </div>
            </div>
          )}

          {isInKindDividend && (
            <div className="space-y-2">
              <Label htmlFor="in-kind-quantity">
                {t('record.quantity')}
              </Label>
              <Input
                id="in-kind-quantity"
                type="number"
                min="0"
                step="any"
                value={form.quantity}
                onChange={(event) =>
                  update({ quantity: event.target.value })
                }
                disabled={registerActivity.isPending}
              />
              <p className="text-xs text-muted-foreground">
                {t('record.inKindHelp')}
              </p>
            </div>
          )}

          {form.type === 'EXCHANGE' && (
            <div className="space-y-3">
              <div className="space-y-2">
                <Label htmlFor="exchange-amount">
                  {t('record.amountOrigin')}
                </Label>
                <Input
                  id="exchange-amount"
                  type="number"
                  min="0"
                  step="any"
                  value={form.amountOrigin}
                  onChange={(event) =>
                    update({
                      amountOrigin: event.target.value,
                    })
                  }
                  disabled={registerActivity.isPending}
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-2">
                  <Label htmlFor="exchange-origin">
                    {t('record.currencyOrigin')}
                  </Label>
                  <Input
                    id="exchange-origin"
                    list="investment-currencies"
                    maxLength={3}
                    value={form.currencyOrigin}
                    onChange={(event) =>
                      update({
                        currencyOrigin:
                          event.target.value.toUpperCase(),
                      })
                    }
                    disabled={registerActivity.isPending}
                    className="uppercase"
                  />
                </div>

                <div className="space-y-2">
                  <Label htmlFor="exchange-destination">
                    {t('record.currencyDestination')}
                  </Label>
                  <Input
                    id="exchange-destination"
                    list="investment-currencies"
                    maxLength={3}
                    value={form.currencyDestination}
                    onChange={(event) =>
                      update({
                        currencyDestination:
                          event.target.value.toUpperCase(),
                      })
                    }
                    disabled={registerActivity.isPending}
                    className="uppercase"
                  />
                </div>
              </div>

              <p className="text-xs text-muted-foreground">
                {t('record.exchangeHelp')}
              </p>
            </div>
          )}

          {form.type === 'SPLIT' && (
            <div className="space-y-2">
              <Label htmlFor="split-ratio">
                {t('record.ratio')}
              </Label>
              <Input
                id="split-ratio"
                type="number"
                min="0"
                step="any"
                value={form.ratio}
                onChange={(event) =>
                  update({ ratio: event.target.value })
                }
                disabled={registerActivity.isPending}
              />
              <p className="text-xs text-muted-foreground">
                {t('record.ratioHelp')}
              </p>
            </div>
          )}

          <div className="space-y-2">
            <div className="flex items-center justify-between gap-2">
              <Label htmlFor="activity-comment">
                {t('record.comment')}
              </Label>
              <span className="text-xs text-muted-foreground">
                {form.comment.length}/{MAX_COMMENT_LENGTH}
              </span>
            </div>
            <textarea
              id="activity-comment"
              value={form.comment}
              maxLength={MAX_COMMENT_LENGTH}
              onChange={(event) =>
                update({ comment: event.target.value })
              }
              disabled={registerActivity.isPending}
              rows={3}
              placeholder={t('record.commentPlaceholder')}
              className="w-full resize-none rounded-md border border-input bg-transparent px-3 py-2 text-sm shadow-xs outline-none placeholder:text-muted-foreground focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:opacity-50"
            />
          </div>

          {showValidation && !isValid && (
            <p className="text-sm text-destructive">
              {t('record.requiredFields')}
            </p>
          )}

          <datalist id="investment-currencies">
            {CURRENCIES.map((code) => (
              <option key={code} value={code} />
            ))}
          </datalist>
        </div>

        <DialogFooter>
          <Button
            type="button"
            variant="outline"
            onClick={() => handleOpenChange(false)}
            disabled={registerActivity.isPending}
          >
            {t('common:actions.cancel')}
          </Button>
          <Button
            type="button"
            onClick={() => void handleSubmit()}
            disabled={registerActivity.isPending}
          >
            {registerActivity.isPending && (
              <Loader2
                className="size-4 animate-spin"
                aria-hidden="true"
              />
            )}
            {t('record.submit')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
