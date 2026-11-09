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

import {
  MAX_COMMENT_LENGTH,
  MAX_REASON_LENGTH,
} from '../constants';
import { useInvestmentMutations } from '../hooks/useInvestmentMutations';
import { useAssetCatalog } from '../hooks/useInvestmentQueries';
import type {
  Activity,
  EditActivityDetailsType,
  EditActivityRequest,
} from '../types';
import {
  formatQuantity,
  fromDateTimeInputValue,
  parseNumericInput,
  toDateTimeInputValue,
} from '../utils';

interface EditActivityDialogProps {
  activity: Activity;
  onOpenChange: (open: boolean) => void;
}

function getDetailsType(
  activity: Activity,
): EditActivityDetailsType | null {
  if (
    activity.type === 'BUY' ||
    activity.type === 'SELL'
  ) {
    return 'BUY_SELL';
  }

  if (activity.type === 'DIVIDEND') {
    return activity.details.inKindDividend
      ? 'IN_KIND_DIVIDEND'
      : 'CASH_DIVIDEND';
  }

  if (activity.type === 'EXCHANGE') {
    return 'EXCHANGE';
  }

  if (activity.type === 'SPLIT') {
    return 'SPLIT';
  }

  return null;
}

export function EditActivityDialog({
  activity,
  onOpenChange,
}: EditActivityDialogProps) {
  const { t } = useTranslation('investments');
  const { editActivity } = useInvestmentMutations();
  const catalog = useAssetCatalog();

  const detailsType = getDetailsType(activity);
  const entry = catalog.getEntry(activity.assetReference);
  const localCurrency = getLocalCurrencyCode();
  const assetCurrency = entry?.currency ?? localCurrency;

  const [occurredAt, setOccurredAt] = useState(() =>
    toDateTimeInputValue(activity.occurredAt),
  );
  const [quantity, setQuantity] = useState(() =>
    activity.details.trade
      ? String(activity.details.trade.quantity)
      : activity.details.inKindDividend
        ? String(activity.details.inKindDividend.quantity)
        : '',
  );
  const [unitPrice, setUnitPrice] = useState(() =>
    activity.details.trade
      ? String(activity.details.trade.unitPrice.amount)
      : '',
  );
  const [amount, setAmount] = useState(() =>
    activity.details.cash
      ? String(activity.details.cash.amount.amount)
      : '',
  );
  const [amountOrigin, setAmountOrigin] = useState(() =>
    activity.details.exchange
      ? String(activity.details.exchange.origin.amount)
      : '',
  );
  const [ratio, setRatio] = useState(() =>
    activity.details.split
      ? String(activity.details.split.ratio)
      : '',
  );
  const [comment, setComment] = useState(
    activity.comment ?? '',
  );
  const [reason, setReason] = useState('');
  const [showValidation, setShowValidation] =
    useState(false);

  if (!detailsType) {
    return null;
  }

  const parsedQuantity = parseNumericInput(quantity);
  const parsedUnitPrice = parseNumericInput(unitPrice);
  const parsedAmount = parseNumericInput(amount);
  const parsedAmountOrigin = parseNumericInput(amountOrigin);
  const parsedRatio = parseNumericInput(ratio);

  const isValid =
    occurredAt.length > 0 &&
    reason.trim().length > 0 &&
    (detailsType === 'BUY_SELL'
      ? parsedQuantity != null &&
        parsedQuantity > 0 &&
        parsedUnitPrice != null &&
        parsedUnitPrice > 0
      : detailsType === 'CASH_DIVIDEND'
        ? parsedAmount != null && parsedAmount > 0
        : detailsType === 'IN_KIND_DIVIDEND'
          ? parsedQuantity != null && parsedQuantity > 0
          : detailsType === 'EXCHANGE'
            ? parsedAmountOrigin != null &&
              parsedAmountOrigin > 0
            : detailsType === 'SPLIT'
              ? parsedRatio != null && parsedRatio > 0
              : false);

  const handleSubmit = async () => {
    if (!isValid) {
      setShowValidation(true);
      return;
    }

    const normalizedComment = comment.trim() || null;

    let details: EditActivityRequest['details'];

    if (detailsType === 'BUY_SELL') {
      details = {
        type: 'BUY_SELL',
        quantity: parsedQuantity as number,
        unitPrice: parsedUnitPrice as number,
        comment: normalizedComment,
      };
    } else if (detailsType === 'CASH_DIVIDEND') {
      details = {
        type: 'CASH_DIVIDEND',
        amount: parsedAmount as number,
        comment: normalizedComment,
      };
    } else if (detailsType === 'IN_KIND_DIVIDEND') {
      details = {
        type: 'IN_KIND_DIVIDEND',
        quantity: parsedQuantity as number,
        comment: normalizedComment,
      };
    } else if (detailsType === 'EXCHANGE') {
      details = {
        type: 'EXCHANGE',
        amountOrigin: parsedAmountOrigin as number,
        comment: normalizedComment,
      };
    } else {
      details = {
        type: 'SPLIT',
        ratio: parsedRatio as number,
        comment: normalizedComment,
      };
    }

    try {
      await editActivity.mutateAsync({
        id: activity.id,
        data: {
          occurredAt: fromDateTimeInputValue(occurredAt),
          details,
          reason: reason.trim(),
        },
      });
      onOpenChange(false);
    } catch {
      // La mutation muestra el error.
    }
  };

  return (
    <Dialog open onOpenChange={onOpenChange}>
      <DialogContent className="border-border bg-card sm:max-w-lg">
        <div className="max-h-[85dvh] space-y-4 overflow-y-auto pr-1">
          <DialogHeader>
            <DialogTitle>{t('edit.title')}</DialogTitle>
            <DialogDescription>
              {t('edit.description', {
                asset:
                  entry?.name ??
                  activity.assetReference?.id ??
                  t('common.unknownAsset'),
              })}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-2">
            <Label htmlFor="edit-occurred-at">
              {t('record.occurredAt')}
            </Label>
            <Input
              id="edit-occurred-at"
              type="datetime-local"
              value={occurredAt}
              onChange={(event) =>
                setOccurredAt(event.target.value)
              }
              disabled={editActivity.isPending}
            />
          </div>

          {detailsType === 'BUY_SELL' && (
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="edit-quantity">
                  {t('record.quantity')}
                </Label>
                <Input
                  id="edit-quantity"
                  type="number"
                  min="0"
                  step="any"
                  value={quantity}
                  onChange={(event) =>
                    setQuantity(event.target.value)
                  }
                  disabled={editActivity.isPending}
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="edit-unit-price">
                  {t('record.unitPriceWithCurrency', {
                    currency: assetCurrency,
                  })}
                </Label>
                <Input
                  id="edit-unit-price"
                  type="number"
                  min="0"
                  step="any"
                  value={unitPrice}
                  onChange={(event) =>
                    setUnitPrice(event.target.value)
                  }
                  disabled={editActivity.isPending}
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

          {detailsType === 'CASH_DIVIDEND' &&
            activity.details.cash && (
              <div className="space-y-2">
                <Label htmlFor="edit-amount">
                  {t('record.amount')}
                </Label>
                <div className="flex items-center gap-2">
                  <Input
                    id="edit-amount"
                    type="number"
                    min="0"
                    step="any"
                    value={amount}
                    onChange={(event) =>
                      setAmount(event.target.value)
                    }
                    disabled={editActivity.isPending}
                  />
                  <span className="shrink-0 rounded-md border border-border px-3 py-2 text-sm text-muted-foreground">
                    {activity.details.cash.amount.currency}
                  </span>
                </div>
                <p className="text-xs text-muted-foreground">
                  {t('edit.currencyImmutable')}
                </p>
              </div>
            )}

          {detailsType === 'IN_KIND_DIVIDEND' && (
            <div className="space-y-2">
              <Label htmlFor="edit-in-kind-quantity">
                {t('record.quantity')}
              </Label>
              <Input
                id="edit-in-kind-quantity"
                type="number"
                min="0"
                step="any"
                value={quantity}
                onChange={(event) =>
                  setQuantity(event.target.value)
                }
                disabled={editActivity.isPending}
              />
              <p className="text-xs text-muted-foreground">
                {t('edit.currentQuantity', {
                  quantity: formatQuantity(
                    activity.details.inKindDividend?.quantity,
                  ),
                })}
              </p>
            </div>
          )}

          {detailsType === 'EXCHANGE' &&
            activity.details.exchange && (
              <div className="space-y-2">
                <Label htmlFor="edit-exchange-amount">
                  {t('record.amountOrigin')}
                </Label>
                <Input
                  id="edit-exchange-amount"
                  type="number"
                  min="0"
                  step="any"
                  value={amountOrigin}
                  onChange={(event) =>
                    setAmountOrigin(event.target.value)
                  }
                  disabled={editActivity.isPending}
                />
                <div className="flex flex-wrap gap-2 text-xs text-muted-foreground">
                  <span className="rounded-md border border-border px-2 py-1">
                    {activity.details.exchange.origin.currency}
                  </span>
                  <span aria-hidden="true">→</span>
                  <span className="rounded-md border border-border px-2 py-1">
                    {
                      activity.details.exchange.destination
                        .currency
                    }
                  </span>
                </div>
                <p className="text-xs text-muted-foreground">
                  {t('edit.currencyImmutable')}
                </p>
              </div>
            )}

          {detailsType === 'SPLIT' && (
            <div className="space-y-2">
              <Label htmlFor="edit-ratio">
                {t('record.ratio')}
              </Label>
              <Input
                id="edit-ratio"
                type="number"
                min="0"
                step="any"
                value={ratio}
                onChange={(event) =>
                  setRatio(event.target.value)
                }
                disabled={editActivity.isPending}
              />
              <p className="text-xs text-muted-foreground">
                {t('record.ratioHelp')}
              </p>
            </div>
          )}

          <div className="space-y-2">
            <div className="flex items-center justify-between gap-2">
              <Label htmlFor="edit-comment">
                {t('record.comment')}
              </Label>
              <span className="text-xs text-muted-foreground">
                {comment.length}/{MAX_COMMENT_LENGTH}
              </span>
            </div>
            <textarea
              id="edit-comment"
              value={comment}
              maxLength={MAX_COMMENT_LENGTH}
              onChange={(event) =>
                setComment(event.target.value)
              }
              disabled={editActivity.isPending}
              rows={2}
              className="w-full resize-none rounded-md border border-input bg-transparent px-3 py-2 text-sm shadow-xs outline-none placeholder:text-muted-foreground focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:opacity-50"
            />
          </div>

          <div className="space-y-2">
            <div className="flex items-center justify-between gap-2">
              <Label htmlFor="edit-reason">
                {t('edit.reason')}
              </Label>
              <span className="text-xs text-muted-foreground">
                {reason.length}/{MAX_REASON_LENGTH}
              </span>
            </div>
            <textarea
              id="edit-reason"
              value={reason}
              maxLength={MAX_REASON_LENGTH}
              onChange={(event) =>
                setReason(event.target.value)
              }
              disabled={editActivity.isPending}
              rows={2}
              placeholder={t('edit.reasonPlaceholder')}
              className="w-full resize-none rounded-md border border-input bg-transparent px-3 py-2 text-sm shadow-xs outline-none placeholder:text-muted-foreground focus-visible:border-ring focus-visible:ring-[3px] focus-visible:ring-ring/50 disabled:opacity-50"
            />
            <p className="text-xs text-muted-foreground">
              {t('edit.auditHelp')}
            </p>
          </div>

          {showValidation && !isValid && (
            <p className="text-sm text-destructive">
              {t('edit.requiredFields')}
            </p>
          )}
        </div>

        <DialogFooter>
          <Button
            type="button"
            variant="outline"
            onClick={() => onOpenChange(false)}
            disabled={editActivity.isPending}
          >
            {t('common:actions.cancel')}
          </Button>
          <Button
            type="button"
            onClick={() => void handleSubmit()}
            disabled={editActivity.isPending}
          >
            {editActivity.isPending && (
              <Loader2
                className="size-4 animate-spin"
                aria-hidden="true"
              />
            )}
            {t('common:actions.save')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
