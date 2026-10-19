import { memo, useCallback, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Loader2 } from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/core/dialog';
import { Button } from '@/shared/components/core/button';
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
import { PRIMARY_ACTIVITY_TYPES, MAX_COMMENT_LENGTH } from '../constants';
import { useInvestmentMutations } from '../hooks/useInvestmentMutations';
import { AssetPicker } from './AssetPicker';
import type {
  ActivityDetails,
  ActivityType,
  AssetReference,
  RegisterActivityRequest,
} from '../types';

interface RecordActivityDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

interface TradeForm {
  quantity: string;
  unitPrice: string;
  currency: string;
}

interface CashForm {
  amount: string;
  currency: string;
}

interface DividendForm {
  mode: 'cash' | 'units';
  amount: string;
  quantity: string;
  referenceUnitPrice: string;
  currency: string;
}

const DEFAULT_CURRENCY = 'EUR';
const CURRENCY_OPTIONS = ['EUR', 'USD', 'GBP', 'CHF', 'JPY'];

function requiresAsset(type: ActivityType): boolean {
  return (
    type === 'BUY' ||
    type === 'SELL' ||
    type === 'DIVIDEND' ||
    type === 'SPLIT' ||
    type === 'OPENING_POSITION'
  );
}

export const RecordActivityDialog = memo(function RecordActivityDialog({
  open,
  onOpenChange,
}: RecordActivityDialogProps) {
  const { t } = useTranslation('investments');
  const { registerActivity, isRegisteringActivity } = useInvestmentMutations();

  const [type, setType] = useState<ActivityType>('BUY');
  const [assetReference, setAssetReference] = useState<AssetReference | null>(null);
  const [occurredAt, setOccurredAt] = useState(() =>
    new Date().toISOString().slice(0, 16)
  );
  const [comment, setComment] = useState('');

  const [tradeForm, setTradeForm] = useState<TradeForm>({
    quantity: '',
    unitPrice: '',
    currency: DEFAULT_CURRENCY,
  });

  const [cashForm, setCashForm] = useState<CashForm>({
    amount: '',
    currency: DEFAULT_CURRENCY,
  });

  const [dividendForm, setDividendForm] = useState<DividendForm>({
    mode: 'cash',
    amount: '',
    quantity: '',
    referenceUnitPrice: '',
    currency: DEFAULT_CURRENCY,
  });

  const needsAsset = requiresAsset(type);

  const details: ActivityDetails | null = useMemo(() => {
    switch (type) {
      case 'BUY':
      case 'SELL':
      case 'OPENING_POSITION': {
        const quantity = parseFloat(tradeForm.quantity);
        const unitPrice = parseFloat(tradeForm.unitPrice);
        if (Number.isNaN(quantity) || Number.isNaN(unitPrice)) return null;
        return { quantity, unitPrice, currency: tradeForm.currency };
      }
      case 'DEPOSIT':
      case 'WITHDRAW':
      case 'OPENING_CASH': {
        const amount = parseFloat(cashForm.amount);
        if (Number.isNaN(amount)) return null;
        return { amount, currency: cashForm.currency };
      }
      case 'DIVIDEND': {
        if (dividendForm.mode === 'cash') {
          const amount = parseFloat(dividendForm.amount);
          if (Number.isNaN(amount)) return null;
          return { amount, currency: dividendForm.currency };
        }
        const quantity = parseFloat(dividendForm.quantity);
        const referenceUnitPrice = parseFloat(dividendForm.referenceUnitPrice);
        if (Number.isNaN(quantity) || Number.isNaN(referenceUnitPrice)) return null;
        return { quantity, referenceUnitPrice, currency: dividendForm.currency };
      }
      default:
        return null;
    }
  }, [type, tradeForm, cashForm, dividendForm]);

  const isValid = useMemo(() => {
    if (!details) return false;
    if (!occurredAt) return false;
    if (needsAsset && !assetReference) return false;
    return true;
  }, [details, occurredAt, needsAsset, assetReference]);

  const resetForm = useCallback(() => {
    setType('BUY');
    setAssetReference(null);
    setOccurredAt(new Date().toISOString().slice(0, 16));
    setComment('');
    setTradeForm({ quantity: '', unitPrice: '', currency: DEFAULT_CURRENCY });
    setCashForm({ amount: '', currency: DEFAULT_CURRENCY });
    setDividendForm({
      mode: 'cash',
      amount: '',
      quantity: '',
      referenceUnitPrice: '',
      currency: DEFAULT_CURRENCY,
    });
  }, []);

  const handleSubmit = useCallback(() => {
    if (!isValid || !details) return;

    // El hook useInvestmentMutations se encarga de enrutar al endpoint correcto
    // (/activities/buy, /activities/deposits, etc.) y de añadir el Idempotency-Key.
    const payload: RegisterActivityRequest = {
      type,
      assetReference: needsAsset ? assetReference : null,
      occurredAt: new Date(occurredAt).toISOString(),
      details,
      comment: comment.trim() || null,
    };

    registerActivity.mutate(payload, {
      onSuccess: () => {
        onOpenChange(false);
        resetForm();
      },
    });
  }, [
    isValid,
    details,
    type,
    assetReference,
    needsAsset,
    occurredAt,
    comment,
    registerActivity,
    onOpenChange,
    resetForm,
  ]);

  const handleOpenChange = useCallback(
    (nextOpen: boolean) => {
      onOpenChange(nextOpen);
      if (!nextOpen) {
        resetForm();
      }
    },
    [onOpenChange, resetForm]
  );

  const isCashType = type === 'DEPOSIT' || type === 'WITHDRAW' || type === 'OPENING_CASH';
  const isTradeType = type === 'BUY' || type === 'SELL' || type === 'OPENING_POSITION';

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>{t('activities.recordTitle')}</DialogTitle>
        </DialogHeader>

        <div className="space-y-4">
          <div className="space-y-2">
            <Label>{t('activities.type')}</Label>
            <div className="grid grid-cols-2 gap-2 sm:grid-cols-3">
              {PRIMARY_ACTIVITY_TYPES.map((activityType) => (
                <button
                  key={activityType}
                  type="button"
                  onClick={() => setType(activityType)}
                  className={cn(
                    'rounded-md border px-3 py-2 text-sm font-medium transition-colors',
                    type === activityType
                      ? 'border-primary bg-primary/10 text-primary'
                      : 'border-border bg-card text-muted-foreground hover:bg-accent/50'
                  )}
                >
                  {t(`activities.types.${activityType}`)}
                </button>
              ))}
            </div>
          </div>

          {needsAsset && (
            <AssetPicker
              value={assetReference}
              onChange={setAssetReference}
              disabled={isRegisteringActivity}
            />
          )}

          <div className="space-y-2">
            <Label htmlFor="activity-date">{t('activities.occurredAt')}</Label>
            <Input
              id="activity-date"
              type="datetime-local"
              value={occurredAt}
              onChange={(event) => setOccurredAt(event.target.value)}
              disabled={isRegisteringActivity}
            />
          </div>

          {isTradeType && (
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label htmlFor="trade-quantity">{t('activities.quantity')}</Label>
                <Input
                  id="trade-quantity"
                  type="number"
                  step="any"
                  min="0"
                  value={tradeForm.quantity}
                  onChange={(event) =>
                    setTradeForm((prev) => ({ ...prev, quantity: event.target.value }))
                  }
                  disabled={isRegisteringActivity}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="trade-price">{t('activities.unitPrice')}</Label>
                <Input
                  id="trade-price"
                  type="number"
                  step="any"
                  min="0"
                  value={tradeForm.unitPrice}
                  onChange={(event) =>
                    setTradeForm((prev) => ({ ...prev, unitPrice: event.target.value }))
                  }
                  disabled={isRegisteringActivity}
                />
              </div>
              <div className="col-span-2 space-y-2">
                <Label htmlFor="trade-currency">{t('activities.currency')}</Label>
                <Select
                  value={tradeForm.currency}
                  onValueChange={(value) =>
                    setTradeForm((prev) => ({ ...prev, currency: value }))
                  }
                >
                  <SelectTrigger id="trade-currency">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {CURRENCY_OPTIONS.map((currency) => (
                      <SelectItem key={currency} value={currency}>
                        {currency}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
          )}

          {isCashType && (
            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label htmlFor="cash-amount">{t('activities.amount')}</Label>
                <Input
                  id="cash-amount"
                  type="number"
                  step="any"
                  min="0"
                  value={cashForm.amount}
                  onChange={(event) =>
                    setCashForm((prev) => ({ ...prev, amount: event.target.value }))
                  }
                  disabled={isRegisteringActivity}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="cash-currency">{t('activities.currency')}</Label>
                <Select
                  value={cashForm.currency}
                  onValueChange={(value) =>
                    setCashForm((prev) => ({ ...prev, currency: value }))
                  }
                >
                  <SelectTrigger id="cash-currency">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    {CURRENCY_OPTIONS.map((currency) => (
                      <SelectItem key={currency} value={currency}>
                        {currency}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
            </div>
          )}

          {type === 'DIVIDEND' && (
            <div className="space-y-3">
              <div className="grid grid-cols-2 gap-2">
                <button
                  type="button"
                  onClick={() => setDividendForm((prev) => ({ ...prev, mode: 'cash' }))}
                  className={cn(
                    'rounded-md border px-3 py-2 text-sm font-medium transition-colors',
                    dividendForm.mode === 'cash'
                      ? 'border-primary bg-primary/10 text-primary'
                      : 'border-border bg-card text-muted-foreground hover:bg-accent/50'
                  )}
                >
                  {t('activities.dividendCash')}
                </button>
                <button
                  type="button"
                  onClick={() => setDividendForm((prev) => ({ ...prev, mode: 'units' }))}
                  className={cn(
                    'rounded-md border px-3 py-2 text-sm font-medium transition-colors',
                    dividendForm.mode === 'units'
                      ? 'border-primary bg-primary/10 text-primary'
                      : 'border-border bg-card text-muted-foreground hover:bg-accent/50'
                  )}
                >
                  {t('activities.dividendUnits')}
                </button>
              </div>

              {dividendForm.mode === 'cash' ? (
                <div className="grid grid-cols-2 gap-3">
                  <div className="space-y-2">
                    <Label htmlFor="dividend-amount">{t('activities.amount')}</Label>
                    <Input
                      id="dividend-amount"
                      type="number"
                      step="any"
                      min="0"
                      value={dividendForm.amount}
                      onChange={(event) =>
                        setDividendForm((prev) => ({ ...prev, amount: event.target.value }))
                      }
                      disabled={isRegisteringActivity}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="dividend-currency">{t('activities.currency')}</Label>
                    <Select
                      value={dividendForm.currency}
                      onValueChange={(value) =>
                        setDividendForm((prev) => ({ ...prev, currency: value }))
                      }
                    >
                      <SelectTrigger id="dividend-currency">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        {CURRENCY_OPTIONS.map((currency) => (
                          <SelectItem key={currency} value={currency}>
                            {currency}
                          </SelectItem>
                        ))}
                      </SelectContent>
                    </Select>
                  </div>
                </div>
              ) : (
                <div className="grid grid-cols-2 gap-3">
                  <div className="space-y-2">
                    <Label htmlFor="dividend-quantity">{t('activities.quantity')}</Label>
                    <Input
                      id="dividend-quantity"
                      type="number"
                      step="any"
                      min="0"
                      value={dividendForm.quantity}
                      onChange={(event) =>
                        setDividendForm((prev) => ({ ...prev, quantity: event.target.value }))
                      }
                      disabled={isRegisteringActivity}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="dividend-price">{t('activities.referenceUnitPrice')}</Label>
                    <Input
                      id="dividend-price"
                      type="number"
                      step="any"
                      min="0"
                      value={dividendForm.referenceUnitPrice}
                      onChange={(event) =>
                        setDividendForm((prev) => ({
                          ...prev,
                          referenceUnitPrice: event.target.value,
                        }))
                      }
                      disabled={isRegisteringActivity}
                    />
                  </div>
                </div>
              )}
            </div>
          )}

          <div className="space-y-2">
            <Label htmlFor="activity-comment">{t('activities.comment')}</Label>
            <Input
              id="activity-comment"
              value={comment}
              onChange={(event) => setComment(event.target.value)}
              maxLength={MAX_COMMENT_LENGTH}
              disabled={isRegisteringActivity}
              placeholder={t('activities.commentPlaceholder')}
            />
          </div>
        </div>

        <DialogFooter>
          <Button
            type="button"
            variant="ghost"
            onClick={() => handleOpenChange(false)}
            disabled={isRegisteringActivity}
          >
            {t('common:actions.cancel')}
          </Button>
          <Button
            type="button"
            onClick={handleSubmit}
            disabled={!isValid || isRegisteringActivity}
            className="min-h-10 font-semibold"
          >
            {isRegisteringActivity ? (
              <Loader2 className="size-5 animate-spin" aria-hidden="true" />
            ) : (
              t('common:actions.save')
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
});