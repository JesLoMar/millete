import { useTranslation } from 'react-i18next';
import type { ReactNode } from 'react';
import { Pencil, Link2 } from 'lucide-react';

import { Button } from '@/shared/components/core/button';
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/core/dialog';
import { Badge } from '@/shared/components/core/badge';
import { cn } from '@/lib/utils';
import { formatCurrency } from '@/shared/utils/i18nFormat';

import { useAssetCatalog } from '../hooks/useInvestmentQueries';
import type { Activity } from '../types';
import {
  formatDateTime,
  formatPrice,
  formatQuantity,
  isActivityEditable,
} from '../utils';

interface ActivityDetailsDialogProps {
  activity: Activity | null;
  onOpenChange: (open: boolean) => void;
  onEdit: (activity: Activity) => void;
}

function DetailRow({
  label,
  children,
}: {
  label: string;
  children: ReactNode;
}) {
  return (
    <div className="flex items-start justify-between gap-4 border-b border-border/60 py-2 last:border-0">
      <dt className="shrink-0 text-sm text-muted-foreground">
        {label}
      </dt>
      <dd className="min-w-0 text-right text-sm font-medium text-foreground">
        {children}
      </dd>
    </div>
  );
}

function MoneyValue({
  amount,
  currency,
}: {
  amount: number;
  currency: string;
}) {
  return <>{formatCurrency(amount, currency)}</>;
}

export function ActivityDetailsDialog({
  activity,
  onOpenChange,
  onEdit,
}: ActivityDetailsDialogProps) {
  const { t } = useTranslation('investments');
  const catalog = useAssetCatalog();

  if (!activity) {
    return null;
  }

  const entry = catalog.getEntry(activity.assetReference);
  const { details } = activity;
  const displayType =
    activity.type === 'DIVIDEND'
      ? details.inKindDividend
        ? 'DIVIDEND_IN_KIND'
        : 'DIVIDEND_CASH'
      : activity.type;

  return (
    <Dialog open onOpenChange={onOpenChange}>
      <DialogContent className="border-border bg-card sm:max-w-lg">
        <div className="max-h-[85dvh] space-y-4 overflow-y-auto pr-1">
          <DialogHeader>
            <div className="flex flex-wrap items-center gap-2">
              <DialogTitle>
                {t(`activityTypes.${displayType}`)}
              </DialogTitle>
              {activity.linkedTransactionId && (
                <Badge variant="outline" className="gap-1">
                  <Link2 className="size-3" aria-hidden="true" />
                  {t('activities.linkedTransaction')}
                </Badge>
              )}
            </div>
          </DialogHeader>

          <dl>
            <DetailRow label={t('common.date')}>
              {formatDateTime(activity.occurredAt)}
            </DetailRow>

            <DetailRow label={t('common.asset')}>
              {entry?.name ??
                activity.assetReference?.id ??
                t('common.none')}
              {entry?.symbol && (
                <span className="ml-1 text-xs text-muted-foreground">
                  ({entry.symbol})
                </span>
              )}
            </DetailRow>

            {activity.type === 'BUY' ||
            activity.type === 'SELL' ? (
              <>
                {details.trade && (
                  <>
                    <DetailRow label={t('record.quantity')}>
                      {formatQuantity(details.trade.quantity)}
                    </DetailRow>
                    <DetailRow
                      label={t('record.unitPrice')}
                    >
                      <MoneyValue
                        amount={details.trade.unitPrice.amount}
                        currency={
                          details.trade.unitPrice.currency
                        }
                      />
                    </DetailRow>
                    <DetailRow
                      label={t('activities.settlementAmount')}
                    >
                      <MoneyValue
                        amount={
                          details.trade.settlementAmount.amount
                        }
                        currency={
                          details.trade.settlementAmount.currency
                        }
                      />
                    </DetailRow>
                  </>
                )}
              </>
            ) : null}

            {activity.type === 'DIVIDEND' &&
              details.cash != null && (
                <DetailRow label={t('record.amount')}>
                  <MoneyValue
                    amount={details.cash.amount.amount}
                    currency={details.cash.amount.currency}
                  />
                </DetailRow>
              )}

            {activity.type === 'DIVIDEND' &&
              details.inKindDividend != null && (
                <>
                  <DetailRow label={t('record.quantity')}>
                    {formatQuantity(
                      details.inKindDividend.quantity,
                    )}
                  </DetailRow>
                  <DetailRow
                    label={t('activities.referenceUnitPrice')}
                  >
                    <MoneyValue
                      amount={
                        details.inKindDividend
                          .referenceUnitPrice.amount
                      }
                      currency={
                        details.inKindDividend
                          .referenceUnitPrice.currency
                      }
                    />
                  </DetailRow>
                </>
              )}

            {(activity.type === 'DEPOSIT' ||
              activity.type === 'WITHDRAW' ||
              activity.type === 'OPENING_CASH') &&
              details.cash != null && (
                <DetailRow label={t('record.amount')}>
                  <MoneyValue
                    amount={details.cash.amount.amount}
                    currency={details.cash.amount.currency}
                  />
                </DetailRow>
              )}

            {activity.type === 'EXCHANGE' &&
              details.exchange != null && (
                <>
                  <DetailRow label={t('activities.origin')}>
                    <MoneyValue
                      amount={details.exchange.origin.amount}
                      currency={
                        details.exchange.origin.currency
                      }
                    />
                  </DetailRow>
                  <DetailRow
                    label={t('activities.destination')}
                  >
                    <MoneyValue
                      amount={
                        details.exchange.destination.amount
                      }
                      currency={
                        details.exchange.destination.currency
                      }
                    />
                  </DetailRow>
                  {details.exchange.appliedFxRate && (
                    <DetailRow label={t('activities.appliedFxRate')}>
                      {formatPrice(
                        details.exchange.appliedFxRate.rate,
                      )}
                      <span className="ml-1 text-xs text-muted-foreground">
                        {
                          details.exchange.appliedFxRate
                            .baseCurrency
                        }
                        /
                        {
                          details.exchange.appliedFxRate
                            .quoteCurrency
                        }
                      </span>
                    </DetailRow>
                  )}
                </>
              )}

            {activity.type === 'SPLIT' &&
              details.split != null && (
                <DetailRow label={t('record.ratio')}>
                  {formatPrice(details.split.ratio)}
                </DetailRow>
              )}

            {activity.type === 'OPENING_POSITION' &&
              details.openingPosition != null && (
                <>
                  <DetailRow label={t('record.quantity')}>
                    {formatQuantity(
                      details.openingPosition.quantity,
                    )}
                  </DetailRow>
                  <DetailRow
                    label={t('holdings.acquisitionCost')}
                  >
                    <MoneyValue
                      amount={
                        details.openingPosition
                          .acquisitionCost.amount
                      }
                      currency={
                        details.openingPosition
                          .acquisitionCost.currency
                      }
                    />
                  </DetailRow>
                </>
              )}

            {details.trade?.appliedFxRate && (
              <DetailRow label={t('activities.appliedFxRate')}>
                {formatPrice(details.trade.appliedFxRate.rate)}
                <span className="ml-1 text-xs text-muted-foreground">
                  {details.trade.appliedFxRate.baseCurrency}/
                  {details.trade.appliedFxRate.quoteCurrency}
                </span>
              </DetailRow>
            )}

            <DetailRow label={t('common.createdAt')}>
              {formatDateTime(activity.createdAt)}
            </DetailRow>

            {activity.modifiedAt !== activity.createdAt && (
              <DetailRow label={t('common.modifiedAt')}>
                {formatDateTime(activity.modifiedAt)}
              </DetailRow>
            )}

            <DetailRow label={t('common.comment')}>
              <span
                className={cn(
                  'whitespace-pre-wrap',
                  !activity.comment && 'text-muted-foreground',
                )}
              >
                {activity.comment || t('common.none')}
              </span>
            </DetailRow>
          </dl>

          {activity.linkedTransactionId && (
            <p className="rounded-md border border-border bg-muted/40 p-3 text-xs text-muted-foreground">
              {t('activities.linkedTransactionHelp')}
            </p>
          )}
        </div>

        <DialogFooter>
          <Button
            type="button"
            variant="outline"
            onClick={() => onOpenChange(false)}
          >
            {t('common:close')}
          </Button>
          {isActivityEditable(activity) &&
            !activity.linkedTransactionId && (
              <Button
                type="button"
                onClick={() => onEdit(activity)}
              >
                <Pencil
                  className="size-4"
                  aria-hidden="true"
                />
                {t('common:edit')}
              </Button>
            )}
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
