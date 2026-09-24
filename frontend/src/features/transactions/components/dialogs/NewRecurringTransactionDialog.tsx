import { useId, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { RefreshCcw } from 'lucide-react';

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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/core/select';

import { FREQUENCY_TYPES } from '../../constants';
import { useTransactionMutations } from '../../hooks/useTransactionMutation';
import { CategorySelect } from '../CategorySelect';
import { TypeToggle } from '../TypeToggle';

interface FormState {
  description: string;
  category: string;
  amount: string;
  type: 'INCOME' | 'EXPENSE';
  frequencyType: string;
  frequencyInterval: string;
  startDate: string;
  endDate: string;
}

const INITIAL_FORM: FormState = {
  description: '',
  category: '',
  amount: '',
  type: 'EXPENSE',
  frequencyType: '',
  frequencyInterval: '1',
  startDate: '',
  endDate: '',
};

export function NewRecurringTransactionDialog() {
  const { t } = useTranslation(['transactions', 'common', 'auth']);
  const { createRecurring, isCreating } =
    useTransactionMutations();

  const [open, setOpen] = useState(false);
  const [form, setForm] = useState<FormState>(INITIAL_FORM);

  const inputRef = useRef<HTMLInputElement>(null);

  const descriptionId = useId();
  const amountId = useId();
  const frequencyId = useId();
  const intervalId = useId();
  const startDateId = useId();
  const endDateId = useId();
  const typeId = useId();

  const updateForm = (updates: Partial<FormState>) => {
    setForm((previous) => ({
      ...previous,
      ...updates,
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
  const interval = Number(form.frequencyInterval);

  const isValid =
    form.description.trim().length > 0 &&
    form.category.length > 0 &&
    Number.isFinite(amount) &&
    amount > 0 &&
    form.frequencyType.length > 0 &&
    Number.isInteger(interval) &&
    interval > 0 &&
    form.startDate.length > 0 &&
    (!form.endDate || form.endDate >= form.startDate);

  const today = new Date().toISOString().split('T')[0];

  const frequencyUnit =
    form.frequencyType === 'DAYS'
      ? t('transactions:recurring.days')
      : form.frequencyType === 'WEEKS'
        ? t('transactions:recurring.weeks')
        : form.frequencyType === 'MONTHS'
          ? t('transactions:recurring.months')
          : t('transactions:recurring.years');

  const handleSave = async () => {
    if (!isValid) {
      return;
    }

    try {
      const payload: Parameters<
        typeof createRecurring.mutateAsync
      >[0] = {
        categoryId: form.category,
        amount: Math.abs(amount),
        type: form.type,
        description: form.description.trim(),
        frequencyType: form.frequencyType,
        frequencyInterval: interval,
        startDate: form.startDate,
      };

      if (form.endDate) {
        payload.endDate = form.endDate;
      }

      await createRecurring.mutateAsync(payload);

      setOpen(false);
      resetForm();
    } catch {
      // La mutation ya muestra el error mediante notify.error.
      // El diálogo permanece abierto para permitir corregir los datos.
    }
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogTrigger asChild>
        <Button
          variant="outline"
          aria-label={t('transactions:recurring.newTitle')}
          className="h-9 gap-2 border-border bg-card px-3 text-xs font-semibold transition-colors hover:bg-background sm:h-10 sm:px-4 sm:text-sm"
        >
          <RefreshCcw size={15} aria-hidden="true" />

          <span className="hidden xs:inline">
            {t('transactions:recurring.label')}
          </span>

          <span className="xs:hidden">
            {t('transactions:recurring.shortLabel')}
          </span>
        </Button>
      </DialogTrigger>

      <DialogContent
        className="border-border bg-card sm:max-w-125"
        onOpenAutoFocus={(event) => {
          event.preventDefault();
          inputRef.current?.focus();
        }}
      >
        <div className="max-h-[85dvh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="text-lg font-semibold sm:text-xl">
              {t('transactions:recurring.newTitle')}
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
                placeholder={t('transactions:descriptionPlaceholder')}
                value={form.description}
                onChange={(event) =>
                  updateForm({
                    description: event.target.value,
                  })
                }
                disabled={isCreating}
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

                <div id={typeId}>
                  <TypeToggle
                    value={form.type}
                    onChange={(type) => updateForm({ type })}
                  />
                </div>
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
                  placeholder="0.00"
                  value={form.amount}
                  onChange={(event) =>
                    updateForm({
                      amount: event.target.value,
                    })
                  }
                  disabled={isCreating}
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

            <div className="grid grid-cols-2 gap-3 sm:gap-4">
              <div className="space-y-2">
                <Label
                  htmlFor={frequencyId}
                  className="text-sm font-semibold"
                >
                  {t('transactions:recurring.frequency')}
                </Label>

                <Select
                  value={form.frequencyType}
                  onValueChange={(frequencyType) =>
                    updateForm({ frequencyType })
                  }
                  disabled={isCreating}
                >
                  <SelectTrigger
                    id={frequencyId}
                    className="border-border bg-background text-base"
                    aria-label={t(
                      'transactions:recurring.selectFrequency',
                    )}
                  >
                    <SelectValue
                      placeholder={t(
                        'transactions:recurring.selectFrequency',
                      )}
                    />
                  </SelectTrigger>

                  <SelectContent className="border-border bg-card">
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
                  {t('transactions:recurring.interval')}
                </Label>

                <Input
                  id={intervalId}
                  type="number"
                  min="1"
                  step="1"
                  placeholder="1"
                  value={form.frequencyInterval}
                  onChange={(event) =>
                    updateForm({
                      frequencyInterval: event.target.value,
                    })
                  }
                  disabled={isCreating}
                  className="border-border bg-background text-base"
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3 sm:gap-4">
              <div className="space-y-2">
                <Label
                  htmlFor={startDateId}
                  className="text-sm font-semibold"
                >
                  {t('transactions:recurring.startDate')}
                </Label>

                <Input
                  id={startDateId}
                  type="date"
                  value={form.startDate}
                  onChange={(event) =>
                    updateForm({
                      startDate: event.target.value,
                    })
                  }
                  disabled={isCreating}
                  min={today}
                  className="border-border bg-background text-base"
                />
              </div>

              <div className="space-y-2">
                <Label
                  htmlFor={endDateId}
                  className="text-sm font-semibold"
                >
                  {t('transactions:recurring.endDate')}
                  <span className="ml-1 text-xs text-muted-foreground">
                    ({t('auth:form.optional')})
                  </span>
                </Label>

                <Input
                  id={endDateId}
                  type="date"
                  value={form.endDate}
                  onChange={(event) =>
                    updateForm({
                      endDate: event.target.value,
                    })
                  }
                  disabled={isCreating}
                  min={form.startDate || today}
                  className="border-border bg-background text-base"
                />
              </div>
            </div>

            {form.frequencyType && form.startDate && (
              <div className="space-y-1 rounded-lg bg-accent/20 p-3 text-xs text-muted-foreground sm:p-4 sm:text-sm">
                <p className="mb-1.5 flex items-center gap-1.5 font-medium text-foreground">
                  <RefreshCcw
                    size={14}
                    className="shrink-0 text-primary"
                    aria-hidden="true"
                  />
                  {t('transactions:recurring.summary')}
                </p>

                <p className="leading-relaxed">
                  {t('transactions:recurring.summaryText', {
                    description: form.description || '...',
                    amount: form.amount || '0',
                    frequency: form.frequencyInterval,
                    type: frequencyUnit,
                    start: form.startDate,
                    end:
                      form.endDate ||
                      t('transactions:recurring.indefinite'),
                  })}
                </p>
              </div>
            )}
          </div>

          <DialogFooter className="sticky bottom-0 gap-2 bg-card pb-1 pt-2 sm:gap-3">
            <Button
              type="button"
              variant="outline"
              onClick={() => setOpen(false)}
              disabled={isCreating}
              className="min-h-11 border-border"
            >
              {t('common:actions.cancel')}
            </Button>

            <Button
              type="button"
              onClick={handleSave}
              disabled={isCreating || !isValid}
              className="min-h-11 bg-primary px-6 hover:bg-primary/90"
            >
              {isCreating ? (
                <Spinner size={20} />
              ) : (
                t('transactions:add')
              )}
            </Button>
          </DialogFooter>
        </div>
      </DialogContent>
    </Dialog>
  );
}