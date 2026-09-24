import { useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Plus } from 'lucide-react';

import { Spinner } from '@/shared/components/Spinner';
import { Button } from '@/shared/components/core/button';
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/shared/components/core/dialog';
import { Input } from '@/shared/components/core/input';
import { Label } from '@/shared/components/core/label';

import type { TransactionType } from '@/features/transactions/index';
import { useTransactionMutations } from '../../hooks/useTransactionMutation';

import { CategorySelect } from '../CategorySelect';
import { TypeToggle } from '../TypeToggle';

interface NewTransactionDialogProps {
  open?: boolean;
  onOpenChange?: (open: boolean) => void;
}

interface FormState {
  description: string;
  category: string;
  amount: string;
  type: TransactionType;
  error: string | null;
}

const INITIAL_FORM: FormState = {
  description: '',
  category: '',
  amount: '',
  type: 'EXPENSE',
  error: null,
};

export function NewTransactionDialog({
  open: controlledOpen,
  onOpenChange: controlledOnOpenChange,
}: NewTransactionDialogProps = {}) {
  const { t } = useTranslation([
    'transactions',
    'common',
  ]);

  const { createTransaction, isCreating } = useTransactionMutations();

  const [internalOpen, setInternalOpen] = useState(false);
  const [form, setForm] = useState<FormState>(INITIAL_FORM);

  const inputRef = useRef<HTMLInputElement>(null);

  const isControlled = controlledOpen !== undefined;
  const open = isControlled ? controlledOpen : internalOpen;
  const setOpen =
    controlledOnOpenChange ?? setInternalOpen;

  const updateForm = (updates: Partial<FormState>) => {
    setForm((previous) => ({
      ...previous,
      ...updates,
      error: updates.error ?? null,
    }));
  };

  const resetForm = () => {
    setForm({ ...INITIAL_FORM });
  };

  const handleOpenChange = (isOpen: boolean) => {
    setOpen(isOpen);

    if (!isOpen) {
      resetForm();
    }
  };

  const amount = Number(form.amount);

  const isValid =
    form.description.trim().length > 0 &&
    form.category.length > 0 &&
    form.amount.trim().length > 0 &&
    Number.isFinite(amount) &&
    amount > 0;

  const handleSave = async () => {
    if (!isValid) {
      return;
    }

    updateForm({ error: null });

    try {
      await createTransaction.mutateAsync({
        description: form.description.trim(),
        categoryId: form.category,
        amount: Math.abs(amount),
        type: form.type,
        date: new Date().toISOString().split('.')[0],
      });

      setOpen(false);
      resetForm();
    } catch (error) {
      const message =
        error instanceof Error
          ? error.message
          : t('createError');

      updateForm({ error: message });
    }
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      {!isControlled && (
        <DialogTrigger asChild>
          <Button className="h-9 gap-2 bg-primary px-3 text-xs font-semibold hover:bg-primary/90 sm:px-4 sm:text-sm">
            <Plus size={15} aria-hidden="true" />

            <span className="hidden xs:inline">
              {t('new')}
            </span>

            <span className="xs:hidden">
              {t('newShort')}
            </span>
          </Button>
        </DialogTrigger>
      )}

      <DialogContent
        className="border-border bg-card sm:max-w-md"
        onOpenAutoFocus={(event) => {
          event.preventDefault();
          inputRef.current?.focus();
        }}
      >
        <div className="max-h-[85dvh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="text-xl font-semibold">
              {t('newTitle')}
            </DialogTitle>
          </DialogHeader>

          <div className="space-y-4 py-2 sm:py-4">
            <div className="space-y-2">
              <Label
                htmlFor={`${inputRef.current?.id ?? 'transaction'}-description`}
                className="text-sm font-semibold"
              >
                {t('description')}
              </Label>

              <Input
                ref={inputRef}
                placeholder={t('descriptionPlaceholder')}
                value={form.description}
                onChange={(event) =>
                  updateForm({
                    description: event.target.value,
                  })
                }
                disabled={isCreating}
                className="border-border bg-background"
              />
            </div>

            <div className="grid grid-cols-2 gap-3 sm:gap-4">
              <div className="space-y-2">
                <Label className="text-sm font-semibold">
                  {t('type')}
                </Label>

                <TypeToggle
                  value={form.type}
                  onChange={(type) => updateForm({ type })}
                />
              </div>

              <div className="space-y-2">
                <Label className="text-sm font-semibold">
                  {t('amount')}
                </Label>

                <Input
                  type="number"
                  placeholder="0.00"
                  value={form.amount}
                  onChange={(event) =>
                    updateForm({
                      amount: event.target.value,
                    })
                  }
                  disabled={isCreating}
                  min="0.01"
                  step="0.01"
                  className="border-border bg-background"
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
                className="text-center text-sm text-destructive"
              >
                {form.error}
              </p>
            )}
          </div>

          <DialogFooter className="sticky bottom-0 gap-2 bg-card pb-1 pt-2">
            <Button
              type="button"
              variant="outline"
              onClick={() => setOpen(false)}
              disabled={isCreating}
              className="border-border"
            >
              {t('common:actions.cancel')}
            </Button>

            <Button
              type="button"
              onClick={handleSave}
              disabled={isCreating || !isValid}
              className="bg-primary px-6 hover:bg-primary/90"
            >
              {isCreating ? (
                <Spinner size={20} />
              ) : (
                t('add')
              )}
            </Button>
          </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  );
}