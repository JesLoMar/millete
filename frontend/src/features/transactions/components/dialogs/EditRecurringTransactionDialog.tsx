import { useId, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';

import { Spinner } from '@/shared/components/Spinner';
import { Button } from '@/shared/components/core/button';
import {
  Dialog,
  DialogContent,
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
import type { PlannedTransaction } from '@/features/transactions/hooks/usePlannedTransactions';

import { FREQUENCY_TYPES } from '../../constants';
import { useTransactionMutations } from '../../hooks/useTransactionMutation';

interface EditRecurringTransactionDialogProps {
  transaction: PlannedTransaction | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

interface FormState {
  description: string;
  amount: string;
  type: 'INCOME' | 'EXPENSE';
  frequencyType: string;
  frequencyInterval: string;
  categoryId: string | null;
}

function getInitialForm(
  transaction: PlannedTransaction | null,
): FormState {
  return {
    description: transaction?.description ?? '',
    amount:
      transaction?.amount !== undefined
        ? String(Math.abs(transaction.amount))
        : '',
    type: transaction?.type ?? 'EXPENSE',
    frequencyType: transaction?.frequencyType ?? 'MONTHS',
    frequencyInterval:
      transaction?.frequencyInterval !== undefined
        ? String(transaction.frequencyInterval)
        : '1',
    categoryId: transaction?.categoryId ?? null,
  };
}

export function EditRecurringTransactionDialog({
  transaction,
  open,
  onOpenChange,
}: EditRecurringTransactionDialogProps) {
  const { t } = useTranslation(['transactions', 'common']);
  const { updateRecurring, isUpdating } =
    useTransactionMutations();

  const [form, setForm] = useState<FormState>(() =>
    getInitialForm(transaction),
  );

  const inputRef = useRef<HTMLInputElement>(null);

  const descriptionId = useId();
  const amountId = useId();
  const frequencyId = useId();
  const intervalId = useId();
  const typeId = useId();

  const updateForm = (updates: Partial<FormState>) => {
    setForm((previous) => ({
      ...previous,
      ...updates,
    }));
  };

  const amount = Number(form.amount);
  const interval = Number(form.frequencyInterval);

  const isValid =
    transaction !== null &&
    form.description.trim().length > 0 &&
    Number.isFinite(amount) &&
    amount > 0 &&
    form.frequencyType.length > 0 &&
    Number.isInteger(interval) &&
    interval > 0;

  const handleSave = async () => {
    if (!transaction || !isValid) {
      return;
    }

    try {
      await updateRecurring.mutateAsync({
        id: transaction.id,
        data: {
          description: form.description.trim(),
          categoryId: form.categoryId,
          amount: Math.abs(amount),
          type: form.type,
          frequencyType: form.frequencyType,
          frequencyInterval: interval,
          startDate: transaction.startDate,
          endDate: transaction.endDate,
        },
      });

      onOpenChange(false);
    } catch {
      // La mutation ya muestra el error mediante notify.error.
      // El diálogo permanece abierto para permitir corregir los datos.
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent
        className="border-border bg-card sm:max-w-md"
        onOpenAutoFocus={(event) => {
          event.preventDefault();
          inputRef.current?.focus();
        }}
      >
        <div className="max-h-[85dvh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="text-xl font-semibold text-foreground">
              {t('transactions:recurring.editTitle')}
            </DialogTitle>
          </DialogHeader>

          <div className="space-y-4 py-2 sm:py-4">
            <div className="space-y-2">
              <Label
                htmlFor={descriptionId}
                className="text-sm font-semibold"
              >
                {t('transactions:description')}
              </Label>

              <Input
                id={descriptionId}
                ref={inputRef}
                value={form.description}
                onChange={(event) =>
                  updateForm({
                    description: event.target.value,
                  })
                }
                placeholder={t(
                  'transactions:descriptionPlaceholder',
                )}
                disabled={isUpdating}
                className="border-border bg-background text-base"
              />
            </div>

            <div className="grid grid-cols-2 gap-3 sm:gap-4">
              <div className="space-y-2">
                <Label
                  htmlFor={typeId}
                  className="text-sm font-semibold"
                >
                  {t('transactions:type')}
                </Label>

                <Select
                  value={form.type}
                  onValueChange={(value) =>
                    updateForm({
                      type: value as 'INCOME' | 'EXPENSE',
                    })
                  }
                  disabled={isUpdating}
                >
                  <SelectTrigger
                    id={typeId}
                    className="border-border bg-background"
                  >
                    <SelectValue />
                  </SelectTrigger>

                  <SelectContent>
                    <SelectItem value="INCOME">
                      {t('income')}
                    </SelectItem>

                    <SelectItem value="EXPENSE">
                      {t('expense')}
                    </SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label
                  htmlFor={amountId}
                  className="text-sm font-semibold"
                >
                  {t('transactions:amount')}
                </Label>

                <Input
                  id={amountId}
                  type="number"
                  min="0.01"
                  step="0.01"
                  value={form.amount}
                  onChange={(event) =>
                    updateForm({
                      amount: event.target.value,
                    })
                  }
                  disabled={isUpdating}
                  className="border-border bg-background text-base"
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3 sm:gap-4">
              <div className="space-y-2">
                <Label
                  htmlFor={frequencyId}
                  className="text-sm font-semibold"
                >
                  {t('recurring.frequency')}
                </Label>

                <Select
                  value={form.frequencyType}
                  onValueChange={(frequencyType) =>
                    updateForm({ frequencyType })
                  }
                  disabled={isUpdating}
                >
                  <SelectTrigger
                    id={frequencyId}
                    className="border-border bg-background"
                  >
                    <SelectValue />
                  </SelectTrigger>

                  <SelectContent>
                    {FREQUENCY_TYPES.map((frequency) => (
                      <SelectItem
                        key={frequency.value}
                        value={frequency.value}
                      >
                        {t(frequency.labelKey)}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              <div className="space-y-2">
                <Label
                  htmlFor={intervalId}
                  className="text-sm font-semibold"
                >
                  {t('recurring.interval')}
                </Label>

                <Input
                  id={intervalId}
                  type="number"
                  min="1"
                  step="1"
                  value={form.frequencyInterval}
                  onChange={(event) =>
                    updateForm({
                      frequencyInterval: event.target.value,
                    })
                  }
                  disabled={isUpdating}
                  className="border-border bg-background text-base"
                />
              </div>
            </div>
          </div>

          <DialogFooter className="sticky bottom-0 gap-2 bg-card pb-1 pt-2">
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              disabled={isUpdating}
              className="border-border"
            >
              {t('common:actions.cancel')}
            </Button>

            <Button
              type="button"
              onClick={handleSave}
              disabled={isUpdating || !isValid}
              className="min-h-11 bg-primary px-6 hover:bg-primary/90"
            >
              {isUpdating ? (
                <Spinner size={20} />
              ) : (
                t('transactions:save')
              )}
            </Button>
          </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  );
}