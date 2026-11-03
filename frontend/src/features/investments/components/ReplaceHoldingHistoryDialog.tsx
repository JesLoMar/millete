import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import {
  AlertTriangle,
  Check,
  Loader2,
  Plus,
  Trash2,
  X,
} from 'lucide-react';

import { Badge } from '@/shared/components/core/badge';
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
import {
  formatCurrency,
  getLocalCurrencyCode,
} from '@/shared/utils/i18nFormat';

import {
  CURRENCIES,
  HISTORICAL_ACTIVITY_TYPES,
  MAX_COMMENT_LENGTH,
} from '../constants';
import { useInvestmentMutations } from '../hooks/useInvestmentMutations';
import { useAssetCatalog } from '../hooks/useInvestmentQueries';
import { replayHistoryRows } from '../replay';
import type {
  HistoricalActivityRequest,
  HistoricalActivityType,
  Holding,
  SettlementCurrency,
} from '../types';
import {
  formatDateTime,
  fromDateTimeInputValue,
  parseNumericInput,
  toDateTimeInputValue,
} from '../utils';

interface ReplaceHoldingHistoryDialogProps {
  holding: Holding;
  onOpenChange: (open: boolean) => void;
}

interface HistoryRow {
  id: string;
  occurredAt: string;
  type: HistoricalActivityType;
  quantity: string;
  unitPrice: string;
  settlementCurrency: SettlementCurrency;
  amount: string;
  currency: string;
  ratio: string;
  comment: string;
}

function createRow(
  type: HistoricalActivityType,
  snapshotAt: string,
  currency: string,
): HistoryRow {
  return {
    id: crypto.randomUUID(),
    occurredAt: toDateTimeInputValue(snapshotAt),
    type,
    quantity: '',
    unitPrice: '',
    settlementCurrency: 'ASSET',
    amount: '',
    currency,
    ratio: '',
    comment: '',
  };
}

function isRowValid(row: HistoryRow): boolean {
  if (!row.occurredAt) {
    return false;
  }

  const quantity = parseNumericInput(row.quantity);
  const unitPrice = parseNumericInput(row.unitPrice);
  const amount = parseNumericInput(row.amount);
  const ratio = parseNumericInput(row.ratio);

  switch (row.type) {
    case 'BUY':
    case 'SELL':
      return (
        quantity != null &&
        quantity > 0 &&
        unitPrice != null &&
        unitPrice > 0
      );
    case 'CASH_DIVIDEND':
      return (
        amount != null &&
        amount > 0 &&
        /^[A-Z]{3}$/.test(row.currency.trim().toUpperCase())
      );
    case 'IN_KIND_DIVIDEND':
      return quantity != null && quantity > 0;
    case 'SPLIT':
      return ratio != null && ratio > 0;
  }
}

function toRequestRow(
  row: HistoryRow,
): HistoricalActivityRequest {
  const base: HistoricalActivityRequest = {
    occurredAt: fromDateTimeInputValue(row.occurredAt),
    type: row.type,
    comment: row.comment.trim() || null,
  };

  if (row.type === 'BUY' || row.type === 'SELL') {
    return {
      ...base,
      quantity: parseNumericInput(row.quantity) as number,
      unitPrice: parseNumericInput(row.unitPrice) as number,
      settlementCurrency: row.settlementCurrency,
    };
  }

  if (row.type === 'CASH_DIVIDEND') {
    return {
      ...base,
      amount: parseNumericInput(row.amount) as number,
      currency: row.currency.trim().toUpperCase(),
    };
  }

  if (row.type === 'IN_KIND_DIVIDEND') {
    return {
      ...base,
      quantity: parseNumericInput(row.quantity) as number,
    };
  }

  return {
    ...base,
    ratio: parseNumericInput(row.ratio) as number,
  };
}

function approxEquals(left: number, right: number): boolean {
  return Math.abs(left - right) <= Math.max(1e-6, Math.abs(right) * 1e-9);
}

function MatchIndicator({
  matches,
  unknown = false,
}: {
  matches: boolean;
  unknown?: boolean;
}) {
  if (unknown) {
    return (
      <Badge variant="warning">
        <AlertTriangle className="size-3" aria-hidden="true" />
      </Badge>
    );
  }

  return matches ? (
    <Badge variant="default">
      <Check className="size-3" aria-hidden="true" />
    </Badge>
  ) : (
    <Badge variant="destructive">
      <X className="size-3" aria-hidden="true" />
    </Badge>
  );
}

export function ReplaceHoldingHistoryDialog({
  holding,
  onOpenChange,
}: ReplaceHoldingHistoryDialogProps) {
  const { t } = useTranslation('investments');
  const { replaceHoldingHistory } = useInvestmentMutations();
  const catalog = useAssetCatalog();
  const localCurrency = getLocalCurrencyCode();
  const entry = catalog.getEntry(holding.assetReference);
  const assetCurrency = entry?.currency ?? holding.currency;

  const [rows, setRows] = useState<HistoryRow[]>(() => [
    createRow('BUY', holding.snapshotAt, assetCurrency),
  ]);
  const [showValidation, setShowValidation] = useState(false);

  const updateRow = (id: string, patch: Partial<HistoryRow>) => {
    setRows((previous) =>
      previous.map((row) =>
        row.id === id ? { ...row, ...patch } : row,
      ),
    );
  };

  const addRow = (type: HistoricalActivityType) => {
    setRows((previous) => [
      ...previous,
      createRow(type, holding.snapshotAt, assetCurrency),
    ]);
  };

  const removeRow = (id: string) => {
    setRows((previous) =>
      previous.filter((row) => row.id !== id),
    );
  };

  const snapshotTime = new Date(holding.snapshotAt).getTime();

  const rowsAfterSnapshot = rows.filter((row) => {
    if (!row.occurredAt) {
      return true;
    }

    return (
      new Date(fromDateTimeInputValue(row.occurredAt)).getTime() >
      snapshotTime
    );
  });

  const allRowsValid = rows.length > 0 && rows.every(isRowValid);
  const datesValid = rowsAfterSnapshot.length === 0;

  const replay = replayHistoryRows(
    rows.map((row) => ({
      occurredAt: row.occurredAt
        ? fromDateTimeInputValue(row.occurredAt)
        : '',
      type: row.type,
      quantity: parseNumericInput(row.quantity),
      unitPrice: parseNumericInput(row.unitPrice),
      settlementCurrency: row.settlementCurrency,
      amount: parseNumericInput(row.amount),
      currency: row.currency,
      ratio: parseNumericInput(row.ratio),
    })),
    assetCurrency,
    localCurrency,
  );

  const quantityMatches = approxEquals(
    replay.quantity,
    holding.quantity,
  );
  const costMatches =
    replay.acquisitionCost != null &&
    approxEquals(replay.acquisitionCost, holding.acquisitionCost);
  const costUnknown = replay.acquisitionCost == null;

  const canSubmit =
    allRowsValid &&
    datesValid &&
    !replay.oversold &&
    !replay.invalidSplit &&
    quantityMatches &&
    (costMatches || costUnknown) &&
    !replaceHoldingHistory.isPending;

  const handleSubmit = async () => {
    if (!canSubmit) {
      setShowValidation(true);
      return;
    }

    const history = [...rows]
      .map((row) => ({
        row,
        timestamp: new Date(
          fromDateTimeInputValue(row.occurredAt),
        ).getTime(),
      }))
      .sort((left, right) => left.timestamp - right.timestamp)
      .map(({ row }) => toRequestRow(row));

    try {
      await replaceHoldingHistory.mutateAsync({
        holdingId: holding.id,
        data: { history },
      });
      onOpenChange(false);
    } catch {
      // La mutation muestra el error y el diálogo permanece abierto.
    }
  };

  return (
    <Dialog open onOpenChange={onOpenChange}>
      <DialogContent className="border-border bg-card sm:max-w-3xl">
        <div className="max-h-[85dvh] space-y-4 overflow-y-auto pr-1">
          <DialogHeader>
            <DialogTitle>
              {t('holdings.history.title', {
                asset:
                  entry?.name ?? t('common.unknownAsset'),
              })}
            </DialogTitle>
            <DialogDescription>
              {t('holdings.history.description')}
            </DialogDescription>
          </DialogHeader>

          <div className="grid grid-cols-1 gap-3 rounded-lg border border-border bg-muted/30 p-3 text-sm sm:grid-cols-3">
            <div>
              <p className="text-[11px] font-semibold tracking-wider text-muted-foreground uppercase">
                {t('holdings.table.snapshotAt')}
              </p>
              <p className="mt-1 font-medium text-foreground">
                {formatDateTime(holding.snapshotAt)}
              </p>
            </div>
            <div>
              <p className="text-[11px] font-semibold tracking-wider text-muted-foreground uppercase">
                {t('common.quantity')}
              </p>
              <p className="mt-1 font-medium tabular-nums text-foreground">
                {holding.quantity}
              </p>
            </div>
            <div>
              <p className="text-[11px] font-semibold tracking-wider text-muted-foreground uppercase">
                {t('holdings.table.acquisitionCost')}
              </p>
              <p className="mt-1 font-medium tabular-nums text-foreground">
                {formatCurrency(
                  holding.acquisitionCost,
                  holding.currency,
                )}
              </p>
            </div>
          </div>

          {catalog.isError && (
            <p className="text-xs text-muted-foreground">
              {t('common.catalogUnavailable')}
            </p>
          )}

          <div className="flex flex-wrap gap-1.5">
            {HISTORICAL_ACTIVITY_TYPES.map((type) => (
              <Button
                key={type}
                type="button"
                variant="outline"
                size="sm"
                onClick={() => addRow(type)}
                disabled={replaceHoldingHistory.isPending}
                className="gap-1"
              >
                <Plus className="size-3.5" aria-hidden="true" />
                {t(`holdings.history.types.${type}`)}
              </Button>
            ))}
          </div>

          {rows.length === 0 && (
            <p className="rounded-md border border-dashed border-border px-4 py-8 text-center text-sm text-muted-foreground">
              {t('holdings.history.emptyRows')}
            </p>
          )}

          <div className="space-y-3">
            {rows.map((row, index) => {
              const rowValid = isRowValid(row);
              const rowInFuture =
                row.occurredAt.length > 0 &&
                new Date(
                  fromDateTimeInputValue(row.occurredAt),
                ).getTime() > snapshotTime;

              return (
                <div
                  key={row.id}
                  className={cn(
                    'space-y-3 rounded-lg border p-3',
                    rowValid && !rowInFuture
                      ? 'border-border'
                      : 'border-destructive/40',
                  )}
                >
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <div className="flex items-center gap-2">
                      <Badge variant="secondary">
                        {index + 1}
                      </Badge>
                      <Select
                        value={row.type}
                        onValueChange={(value) =>
                          updateRow(row.id, {
                            type: value as HistoricalActivityType,
                          })
                        }
                        disabled={replaceHoldingHistory.isPending}
                      >
                        <SelectTrigger className="h-8 w-48">
                          <SelectValue />
                        </SelectTrigger>
                        <SelectContent>
                          {HISTORICAL_ACTIVITY_TYPES.map((type) => (
                            <SelectItem key={type} value={type}>
                              {t(
                                `holdings.history.types.${type}`,
                              )}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </div>

                    <Button
                      type="button"
                      variant="ghost"
                      size="icon-sm"
                      onClick={() => removeRow(row.id)}
                      disabled={replaceHoldingHistory.isPending}
                      aria-label={t('holdings.history.removeRow')}
                    >
                      <Trash2
                        className="size-4 text-destructive"
                        aria-hidden="true"
                      />
                    </Button>
                  </div>

                  <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-4">
                    <div className="space-y-1.5 lg:col-span-2">
                      <Label htmlFor={`history-date-${row.id}`}>
                        {t('common.date')}
                      </Label>
                      <Input
                        id={`history-date-${row.id}`}
                        type="datetime-local"
                        value={row.occurredAt}
                        max={toDateTimeInputValue(
                          holding.snapshotAt,
                        )}
                        onChange={(event) =>
                          updateRow(row.id, {
                            occurredAt: event.target.value,
                          })
                        }
                        disabled={
                          replaceHoldingHistory.isPending
                        }
                      />
                      {rowInFuture && (
                        <p className="text-xs text-destructive">
                          {t('holdings.history.afterSnapshot')}
                        </p>
                      )}
                    </div>

                    {(row.type === 'BUY' ||
                      row.type === 'SELL' ||
                      row.type === 'IN_KIND_DIVIDEND') && (
                      <div className="space-y-1.5">
                        <Label
                          htmlFor={`history-quantity-${row.id}`}
                        >
                          {t('common.quantity')}
                        </Label>
                        <Input
                          id={`history-quantity-${row.id}`}
                          type="number"
                          min="0"
                          step="any"
                          value={row.quantity}
                          onChange={(event) =>
                            updateRow(row.id, {
                              quantity: event.target.value,
                            })
                          }
                          disabled={
                            replaceHoldingHistory.isPending
                          }
                        />
                      </div>
                    )}

                    {(row.type === 'BUY' ||
                      row.type === 'SELL') && (
                      <>
                        <div className="space-y-1.5">
                          <Label
                            htmlFor={`history-price-${row.id}`}
                          >
                            {t('holdings.history.unitPrice')}
                          </Label>
                          <Input
                            id={`history-price-${row.id}`}
                            type="number"
                            min="0"
                            step="any"
                            value={row.unitPrice}
                            onChange={(event) =>
                              updateRow(row.id, {
                                unitPrice:
                                  event.target.value,
                              })
                            }
                            disabled={
                              replaceHoldingHistory.isPending
                            }
                          />
                        </div>

                        <div className="space-y-1.5 lg:col-span-2">
                          <Label>
                            {t(
                              'holdings.history.settlementCurrency',
                            )}
                          </Label>
                          <Select
                            value={row.settlementCurrency}
                            onValueChange={(value) =>
                              updateRow(row.id, {
                                settlementCurrency:
                                  value as SettlementCurrency,
                              })
                            }
                            disabled={
                              replaceHoldingHistory.isPending
                            }
                          >
                            <SelectTrigger className="w-full">
                              <SelectValue />
                            </SelectTrigger>
                            <SelectContent>
                              <SelectItem value="ASSET">
                                {t(
                                  'holdings.history.assetCurrency',
                                  { currency: assetCurrency },
                                )}
                              </SelectItem>
                              <SelectItem value="LOCAL">
                                {t(
                                  'holdings.history.localCurrency',
                                  { currency: localCurrency },
                                )}
                              </SelectItem>
                            </SelectContent>
                          </Select>
                          {row.settlementCurrency === 'LOCAL' &&
                            assetCurrency !== localCurrency && (
                              <p className="text-xs text-muted-foreground">
                                {t(
                                  'holdings.history.localCostHint',
                                )}
                              </p>
                            )}
                        </div>
                      </>
                    )}

                    {row.type === 'CASH_DIVIDEND' && (
                      <>
                        <div className="space-y-1.5">
                          <Label
                            htmlFor={`history-amount-${row.id}`}
                          >
                            {t('holdings.history.amount')}
                          </Label>
                          <Input
                            id={`history-amount-${row.id}`}
                            type="number"
                            min="0"
                            step="any"
                            value={row.amount}
                            onChange={(event) =>
                              updateRow(row.id, {
                                amount: event.target.value,
                              })
                            }
                            disabled={
                              replaceHoldingHistory.isPending
                            }
                          />
                        </div>
                        <div className="space-y-1.5">
                          <Label
                            htmlFor={`history-currency-${row.id}`}
                          >
                            {t('common.currency')}
                          </Label>
                          <Input
                            id={`history-currency-${row.id}`}
                            list="investment-currencies"
                            maxLength={3}
                            value={row.currency}
                            onChange={(event) =>
                              updateRow(row.id, {
                                currency:
                                  event.target.value.toUpperCase(),
                              })
                            }
                            disabled={
                              replaceHoldingHistory.isPending
                            }
                            className="uppercase"
                          />
                        </div>
                      </>
                    )}

                    {row.type === 'SPLIT' && (
                      <div className="space-y-1.5">
                        <Label
                          htmlFor={`history-ratio-${row.id}`}
                        >
                          {t('holdings.history.ratio')}
                        </Label>
                        <Input
                          id={`history-ratio-${row.id}`}
                          type="number"
                          min="0"
                          step="any"
                          value={row.ratio}
                          onChange={(event) =>
                            updateRow(row.id, {
                              ratio: event.target.value,
                            })
                          }
                          disabled={
                            replaceHoldingHistory.isPending
                          }
                        />
                      </div>
                    )}

                    <div className="space-y-1.5 sm:col-span-2 lg:col-span-4">
                      <Label
                        htmlFor={`history-comment-${row.id}`}
                      >
                        {t('holdings.history.comment')}
                      </Label>
                      <Input
                        id={`history-comment-${row.id}`}
                        value={row.comment}
                        maxLength={MAX_COMMENT_LENGTH}
                        onChange={(event) =>
                          updateRow(row.id, {
                            comment: event.target.value,
                          })
                        }
                        disabled={
                          replaceHoldingHistory.isPending
                        }
                        placeholder={t(
                          'holdings.history.commentPlaceholder',
                        )}
                      />
                    </div>
                  </div>

                  {row.type === 'IN_KIND_DIVIDEND' && (
                    <p className="text-xs text-muted-foreground">
                      {t('holdings.history.inKindPriceHint')}
                    </p>
                  )}
                </div>
              );
            })}
          </div>

          <datalist id="investment-currencies">
            {CURRENCIES.map((code) => (
              <option key={code} value={code} />
            ))}
          </datalist>

          <div className="space-y-2 rounded-lg border border-border p-3">
            <h3 className="text-sm font-semibold text-foreground">
              {t('holdings.history.summaryTitle')}
            </h3>

            <div className="grid grid-cols-1 gap-2 text-sm sm:grid-cols-2">
              <div className="flex items-center justify-between gap-2">
                <span className="text-muted-foreground">
                  {t('holdings.history.reconstructedQuantity')}
                </span>
                <span className="flex items-center gap-1.5 font-medium tabular-nums">
                  {replay.quantity.toFixed(8).replace(/\.?0+$/, '')}
                  <MatchIndicator matches={quantityMatches} />
                </span>
              </div>

              <div className="flex items-center justify-between gap-2">
                <span className="text-muted-foreground">
                  {t('holdings.history.reconstructedCost')}
                </span>
                <span className="flex items-center gap-1.5 font-medium tabular-nums">
                  {replay.acquisitionCost == null
                    ? t('holdings.history.costUnknown')
                    : formatCurrency(
                        replay.acquisitionCost,
                        assetCurrency,
                      )}
                  <MatchIndicator
                    matches={costMatches}
                    unknown={costUnknown}
                  />
                </span>
              </div>
            </div>

            {replay.oversold && (
              <p className="flex items-start gap-2 text-xs text-destructive">
                <AlertTriangle
                  className="mt-0.5 size-3.5 shrink-0"
                  aria-hidden="true"
                />
                {t('holdings.history.oversold')}
              </p>
            )}

            {replay.invalidSplit && (
              <p className="flex items-start gap-2 text-xs text-destructive">
                <AlertTriangle
                  className="mt-0.5 size-3.5 shrink-0"
                  aria-hidden="true"
                />
                {t('holdings.history.invalidSplit')}
              </p>
            )}

            {costUnknown && (
              <p className="flex items-start gap-2 text-xs text-muted-foreground">
                <AlertTriangle
                  className="mt-0.5 size-3.5 shrink-0 text-warning"
                  aria-hidden="true"
                />
                {t('holdings.history.costUnknownHint')}
              </p>
            )}

            {!costUnknown && !costMatches && (
              <p className="text-xs text-destructive">
                {t('holdings.history.costMismatch')}
              </p>
            )}

            {!quantityMatches && (
              <p className="text-xs text-destructive">
                {t('holdings.history.quantityMismatch')}
              </p>
            )}

            <p className="text-xs text-muted-foreground">
              {t('holdings.history.replaceWarning')}
            </p>
          </div>

          {showValidation && !canSubmit && (
            <p className="text-sm text-destructive">
              {t('holdings.history.invalid')}
            </p>
          )}
        </div>

        <DialogFooter>
          <Button
            type="button"
            variant="outline"
            onClick={() => onOpenChange(false)}
            disabled={replaceHoldingHistory.isPending}
          >
            {t('common:cancel')}
          </Button>
          <Button
            type="button"
            onClick={() => void handleSubmit()}
            disabled={!canSubmit}
          >
            {replaceHoldingHistory.isPending && (
              <Loader2
                className="size-4 animate-spin"
                aria-hidden="true"
              />
            )}
            {t('holdings.history.submit')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
