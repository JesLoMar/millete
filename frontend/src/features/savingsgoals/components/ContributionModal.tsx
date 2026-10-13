import {
  useId,
  useRef,
  useState,
  type FormEvent,
} from 'react';
import { useTranslation } from 'react-i18next';

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
import { formatCurrency } from '@/shared/utils/i18nFormat';

import type { SavingsGoal } from '../types';
import {
  getRemainingAmount,
  parseAmount,
  toCents,
} from '../utils/amounts';

interface ContributionModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (amount: number) => void;
  goal: SavingsGoal | null;
  isSubmitting: boolean;
}

interface ContributionFormProps {
  goal: SavingsGoal;
  isSubmitting: boolean;
  inputRef: React.Ref<HTMLInputElement>;
  onSubmit: (amount: number) => void;
  onCancel: () => void;
}

/** Su estado se reinicia solo: se monta al abrir y se desmonta al cerrar. */
function ContributionForm({
  goal,
  isSubmitting,
  inputRef,
  onSubmit,
  onCancel,
}: ContributionFormProps) {
  const { t } = useTranslation(['savingsGoals', 'common']);

  const [amount, setAmount] = useState('');
  const [submitted, setSubmitted] = useState(false);

  const inputId = useId();
  const hintId = useId();
  const errorId = useId();

  const remaining = getRemainingAmount(goal);
  const parsedAmount = parseAmount(amount);

  const error =
    parsedAmount === null
      ? 'invalid'
      : toCents(parsedAmount) > toCents(remaining)
        ? 'exceeds'
        : null;

  const showError = submitted && error !== null;

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (isSubmitting) {
      return;
    }

    if (parsedAmount === null || error !== null) {
      setSubmitted(true);
      return;
    }

    onSubmit(parsedAmount);
  };

  return (
    <form
      onSubmit={handleSubmit}
      noValidate
      className="flex flex-col gap-4 py-4"
    >
      <div className="space-y-2">
        <Label htmlFor={inputId}>{t('amount')}</Label>

        <Input
          id={inputId}
          ref={inputRef}
          type="number"
          inputMode="decimal"
          min="0.01"
          max={remaining}
          step="0.01"
          value={amount}
          onChange={(event) => setAmount(event.target.value)}
          disabled={isSubmitting}
          aria-invalid={showError}
          aria-describedby={
            showError ? `${hintId} ${errorId}` : hintId
          }
        />

        <p id={hintId} className="text-xs text-muted-foreground">
          {t('remaining', { amount: formatCurrency(remaining) })}
        </p>

        {showError && (
          <p id={errorId} className="text-xs text-destructive">
            {error === 'exceeds'
              ? t('errors.contributionExceeds', {
                  amount: formatCurrency(remaining),
                })
              : t('errors.amountInvalid')}
          </p>
        )}
      </div>

      <DialogFooter>
        <Button
          type="button"
          variant="outline"
          onClick={onCancel}
          disabled={isSubmitting}
        >
          {t('common:actions.cancel')}
        </Button>

        <Button type="submit" disabled={isSubmitting}>
          {t('add')}
        </Button>
      </DialogFooter>
    </form>
  );
}

export const ContributionModal = ({
  isOpen,
  onClose,
  onSubmit,
  goal,
  isSubmitting,
}: ContributionModalProps) => {
  const { t } = useTranslation(['savingsGoals', 'common']);

  const inputRef = useRef<HTMLInputElement>(null);

  return (
    <Dialog
      open={isOpen}
      onOpenChange={(open) => {
        if (!open && !isSubmitting) {
          onClose();
        }
      }}
    >
      <DialogContent
        aria-describedby={undefined}
        onOpenAutoFocus={(event) => {
          event.preventDefault();
          inputRef.current?.focus();
        }}
      >
        <DialogHeader>
          <DialogTitle>
            {t('addFunds', { name: goal?.name })}
          </DialogTitle>
        </DialogHeader>

        {goal && (
          <ContributionForm
            goal={goal}
            isSubmitting={isSubmitting}
            inputRef={inputRef}
            onSubmit={onSubmit}
            onCancel={onClose}
          />
        )}
      </DialogContent>
    </Dialog>
  );
};
