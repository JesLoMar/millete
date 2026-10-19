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
import {
  CURRENCIES,
  MAX_COMMENT_LENGTH,
  PRIMARY_ACTIVITY_TYPES,
} from '../constants';
import { useInvestmentMutations } from '../hooks/useInvestmentMutations';
import { formatNumber, toLocalDateTimeInput } from '../utils';
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

function parsePositive(value: string): number | null {
  const parsed = parseFloat(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
}

/** El activo es obligatorio en compra/venta y en dividendos en acciones. En efectivo es opcional. */
function requiresAsset(type: ActivityType, dividendMode: DividendForm['mode']): boolean {
  if (type === 'BUY' || type === 'SELL') return true;
  if (type === 'DIVIDEND') return dividendMode === 'units';
  return false;
}

function createIdempotencyKey(): string {
  return crypto.randomUUID();
}

function initialTrade(): TradeForm {
  return { quantity: '', unitPrice: '', currency: DEFAULT_CURRENCY };
}

function initialCash(): CashForm {
  return { amount: '', currency: DEFAULT_CURRENCY };
}

function initialDividend(): DividendForm {
  return {
    mode: 'cash',
    amount: '',
    quantity: '',
    referenceUnitPrice: '',
    currency: DEFAULT_CURRENCY,
  };
}

function CurrencySelect({
  id,
  value,
  onChange,
  disabled,
}: {
  id: string;
  value: string;
  onChange: (value: string) => void;
  disabled: boolean;
}) {
  const { t } = useTranslation('investments');
  return (
    <div className="space-y-2">
      <Label htmlFor={id}>{t('activities.currency')}</Label>
      <Select value={value} onValueChange={onChange} disabled={disabled}>
        <SelectTrigger id={id}>
          <SelectValue />
        </SelectTrigger>
        <SelectContent>
          {CURRENCIES.map((currency) => (
            <SelectItem key={currency} value={currency}>
              {currency}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>
    </div>
  );
}

function ToggleButton({
  active,
  onClick,
  children,
}: {
  active: boolean;
  onClick: () => void;
  children: React.ReactNode;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      aria-pressed={active}
      className={cn(
        'rounded-md border px-3 py-2 text-sm font-medium transition-colors',
        active
          ? 'border-primary bg-primary/10 text-primary'
          : 'border-border bg-card text-muted-foreground hover:bg-accent/50',
      )}
    >
      {children}
    </button>
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
  const [occurredAt, setOccurredAt] = useState(() => toLocalDateTimeInput(new Date()));
  const [comment, setComment] = useState('');
  const [trade, setTrade] = useState<TradeForm>(initialTrade);
  const [cash, setCash] = useState<CashForm>(initialCash);
  const [dividend, setDividend] = useState<DividendForm>(initialDividend);
  // Una clave por formulario: un reintento tras error no duplica la operación.
  const [idempotencyKey, setIdempotencyKey] = useState(createIdempotencyKey);

  const needsAsset = requiresAsset(type, dividend.mode);
  const showAssetPicker = type === 'BUY' || type === 'SELL' || type === 'DIVIDEND';

  const tradeAmount = useMemo(() => {
    if (type !== 'BUY' && type !== 'SELL') return null;
    const quantity = parsePositive(trade.quantity);
    const unitPrice = parsePositive(trade.unitPrice);
    return quantity != null && unitPrice != null ? quantity * unitPrice : null;
  }, [type, trade.quantity, trade.unitPrice]);

  const details: ActivityDetails | null = useMemo(() => {
    switch (type) {
      case 'BUY':
      case 'SELL': {
        const quantity = parsePositive(trade.quantity);
        const unitPrice = parsePositive(trade.unitPrice);
        if (quantity == null || unitPrice == null) return null;
        return { quantity, unitPrice, currency: trade.currency };
      }
      case 'DEPOSIT':
      case 'WITHDRAW': {
        const amount = parsePositive(cash.amount);
        if (amount == null) return null;
        return { amount, currency: cash.currency };
      }
      case 'DIVIDEND': {
        if (dividend.mode === 'cash') {
          const amount = parsePositive(dividend.amount);
          if (amount == null) return null;
          return { amount, currency: dividend.currency };
        }
        const quantity = parsePositive(dividend.quantity);
        const referenceUnitPrice = parsePositive(dividend.referenceUnitPrice);
        if (quantity == null || referenceUnitPrice == null) return null;
        return { quantity, referenceUnitPrice, currency: dividend.currency };
      }
      default:
        return null;
    }
  }, [type, trade, cash, dividend]);

  const isFutureDate = new Date(occurredAt).getTime() > Date.now();
  const isValid =
    details != null &&
    occurredAt !== '' &&
    !isFutureDate &&
    (!needsAsset || assetReference != null);

  const resetForm = useCallback(() => {
    setType('BUY');
    setAssetReference(null);
    setOccurredAt(toLocalDateTimeInput(new Date()));
    setComment('');
    setTrade(initialTrade());
    setCash(initialCash());
    setDividend(initialDividend());
    setIdempotencyKey(createIdempotencyKey());
  }, []);

  const handleSubmit = useCallback(() => {
    if (!isValid || !details) return;

    const payload: RegisterActivityRequest = {
      type,
      // Dividendo en efectivo puede llevar activo opcional; sin activo no se asigna después.
      assetReference: showAssetPicker ? assetReference : null,
      occurredAt: new Date(occurredAt).toISOString(),
      details,
      comment: comment.trim() || null,
      idempotencyKey,
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
    showAssetPicker,
    assetReference,
    occurredAt,
    comment,
    idempotencyKey,
    registerActivity,
    onOpenChange,
    resetForm,
  ]);

  const handleOpenChange = useCallback(
    (nextOpen: boolean) => {
      onOpenChange(nextOpen);
      if (!nextOpen) resetForm();
    },
    [onOpenChange, resetForm],
  );

  const isCashType = type === 'DEPOSIT' || type === 'WITHDRAW';
  const isTradeType = type === 'BUY' || type === 'SELL';
  const disabled = isRegisteringActivity;

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
                <ToggleButton
                  key={activityType}
                  active={type === activityType}
                  onClick={() => setType(activityType)}
                >
                  {t(`activities.types.${activityType}`)}
                </ToggleButton>
              ))}
            </div>
          </div>

          {showAssetPicker && (
            <AssetPicker
              value={assetReference}
              onChange={setAssetReference}
              disabled={disabled}
              optional={!needsAsset}
            />
          )}

          <div className="space-y-2">
            <Label htmlFor="activity-date">{t('activities.occurredAt')}</Label>
            <Input
              id="activity-date"
              type="datetime-local"
              value={occurredAt}
              max={toLocalDateTimeInput(new Date())}
              onChange={(event) => setOccurredAt(event.target.value)}
              disabled={disabled}
            />
            {isFutureDate && (
              <p className="text-xs text-destructive">
                {t('activities.futureDate', {
                  defaultValue: 'La fecha no puede ser futura.',
                })}
              </p>
            )}
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
                  value={trade.quantity}
                  onChange={(event) =>
                    setTrade((prev) => ({ ...prev, quantity: event.target.value }))
                  }
                  disabled={disabled}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="trade-price">{t('activities.unitPrice')}</Label>
                <Input
                  id="trade-price"
                  type="number"
                  step="any"
                  min="0"
                  value={trade.unitPrice}
                  onChange={(event) =>
                    setTrade((prev) => ({ ...prev, unitPrice: event.target.value }))
                  }
                  disabled={disabled}
                />
              </div>
              <div className="col-span-2">
                <CurrencySelect
                  id="trade-currency"
                  value={trade.currency}
                  onChange={(value) => setTrade((prev) => ({ ...prev, currency: value }))}
                  disabled={disabled}
                />
              </div>
              {tradeAmount != null && (
                <p className="col-span-2 text-sm text-muted-foreground">
                  {t('activities.derivedAmount', {
                    defaultValue: 'Importe calculado: {{amount}} {{currency}}',
                    amount: formatNumber(tradeAmount),
                    currency: trade.currency,
                  })}
                </p>
              )}
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
                  value={cash.amount}
                  onChange={(event) =>
                    setCash((prev) => ({ ...prev, amount: event.target.value }))
                  }
                  disabled={disabled}
                />
              </div>
              <CurrencySelect
                id="cash-currency"
                value={cash.currency}
                onChange={(value) => setCash((prev) => ({ ...prev, currency: value }))}
                disabled={disabled}
              />
            </div>
          )}

          {type === 'DIVIDEND' && (
            <div className="space-y-3">
              <div className="grid grid-cols-2 gap-2">
                <ToggleButton
                  active={dividend.mode === 'cash'}
                  onClick={() => setDividend((prev) => ({ ...prev, mode: 'cash' }))}
                >
                  {t('activities.dividendCash')}
                </ToggleButton>
                <ToggleButton
                  active={dividend.mode === 'units'}
                  onClick={() => setDividend((prev) => ({ ...prev, mode: 'units' }))}
                >
                  {t('activities.dividendUnits')}
                </ToggleButton>
              </div>

              {dividend.mode === 'cash' ? (
                <div className="grid grid-cols-2 gap-3">
                  <div className="space-y-2">
                    <Label htmlFor="dividend-amount">{t('activities.amount')}</Label>
                    <Input
                      id="dividend-amount"
                      type="number"
                      step="any"
                      min="0"
                      value={dividend.amount}
                      onChange={(event) =>
                        setDividend((prev) => ({ ...prev, amount: event.target.value }))
                      }
                      disabled={disabled}
                    />
                  </div>
                  <CurrencySelect
                    id="dividend-currency"
                    value={dividend.currency}
                    onChange={(value) =>
                      setDividend((prev) => ({ ...prev, currency: value }))
                    }
                    disabled={disabled}
                  />
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
                      value={dividend.quantity}
                      onChange={(event) =>
                        setDividend((prev) => ({ ...prev, quantity: event.target.value }))
                      }
                      disabled={disabled}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="dividend-price">{t('activities.referenceUnitPrice')}</Label>
                    <Input
                      id="dividend-price"
                      type="number"
                      step="any"
                      min="0"
                      value={dividend.referenceUnitPrice}
                      onChange={(event) =>
                        setDividend((prev) => ({
                          ...prev,
                          referenceUnitPrice: event.target.value,
                        }))
                      }
                      disabled={disabled}
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
              disabled={disabled}
              placeholder={t('activities.commentPlaceholder')}
            />
          </div>
        </div>

        <DialogFooter>
          <Button
            type="button"
            variant="ghost"
            onClick={() => handleOpenChange(false)}
            disabled={disabled}
          >
            {t('common:actions.cancel')}
          </Button>
          <Button
            type="button"
            onClick={handleSubmit}
            disabled={!isValid || disabled}
            className="min-h-10 font-semibold"
          >
            {disabled ? (
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