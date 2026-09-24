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

import { useTransactionMutations } from '../../hooks/useTransactionMutation';
import { CategorySelect } from '../CategorySelect';
import { TypeToggle } from '../TypeToggle';
import type { Transaction } from '../types';

interface EditTransactionDialogProps {
  transaction: Transaction | null;
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

interface FormState {
  description: string;
  category: string;
  amount: string;
  type: Transaction['type'];
  error: string | null;
}

function getInitialForm(transaction: Transaction | null): FormState {
  return {
    description: transaction?.description ?? '',
    category: transaction?.categoryId ?? '',
    amount:
      transaction?.amount !== undefined
        ? String(Math.abs(transaction.amount))
        : '',
    type: transaction?.type ?? 'EXPENSE',
    error: null,
  };
}

export function EditTransactionDialog({
  transaction,
  open,
  onOpenChange,
}: EditTransactionDialogProps) {
  const { t } = useTranslation(['transactions', 'common']);
  const { updateTransaction, isUpdating } =
    useTransactionMutations();

  const [form, setForm] = useState<FormState>(() =>
    getInitialForm(transaction),
  );

  const inputRef = useRef<HTMLInputElement>(null);

  const descriptionId = useId();
  const amountId = useId();

  const updateForm = (updates: Partial<FormState>) => {
    setForm((previous) => ({
      ...previous,
      ...updates,
    }));
  };

  const amount = Number(form.amount);

  const isValid =
    transaction !== null &&
    form.description.trim().length > 0 &&
    Number.isFinite(amount) &&
    amount > 0;

  const handleSave = async () => {
    if (!transaction || !isValid) {
      return;
    }

    updateForm({ error: null });

    try {
      await updateTransaction.mutateAsync({
        id: transaction.id,
        data: {
          description: form.description.trim(),
          categoryId: form.category || '',
          amount: Math.abs(amount),
          type: form.type,
          date: transaction.date,
        },
      });

      onOpenChange(false);
    } catch (error) {
      updateForm({
        error:
          error instanceof Error
            ? error.message
            : t('transactions:alerts.updateError'),
      });
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
              {t('transactions:editTitle')}
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
                <Label className="text-sm font-semibold">
                  {t('transactions:type')}
                </Label>

                <TypeToggle
                  value={form.type}
                  onChange={(type) => updateForm({ type })}
                />
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

            <CategorySelect
              value={form.category}
              onValueChange={(category) =>
                updateForm({ category })
              }
            />

            {form.error && (
              <p
                role="alert"
                className="text-center text-sm font-medium text-destructive"
              >
                {form.error}
              </p>
            )}
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