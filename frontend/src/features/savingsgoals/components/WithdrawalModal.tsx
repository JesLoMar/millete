
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
import { parseAmount, toCents } from '../utils/amounts';

interface WithdrawalModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (amount: number) => void;
  goal: SavingsGoal | null;
  isSubmitting: boolean;
}

interface WithdrawalFormProps {
  goal: SavingsGoal;
  isSubmitting: boolean;
  inputRef: React.Ref<HTMLInputElement>;
  onSubmit: (amount: number) => void;
  onCancel: () => void;
}

function WithdrawalForm({
  goal,
  isSubmitting,
  inputRef,
  onSubmit,
  onCancel,
}: WithdrawalFormProps) {
  const { t } = useTranslation(['savingsGoals', 'common']);

  const [amount, setAmount] = useState('');
  const [submitted, setSubmitted] = useState(false);

  const inputId = useId();
  const hintId = useId();
  const errorId = useId();

  const available = goal.currentAmount;
  const parsedAmount = parseAmount(amount);

  const error =
    parsedAmount === null
      ? 'invalid'
      : toCents(parsedAmount) > toCents(available)
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
          max={available}
          step="0.01"
          value={amount}
          onChange={(event) => setAmount(event.target.value)}
          disabled={isSubmitting || available <= 0}
          aria-invalid={showError}
          aria-describedby={
            showError ? `${hintId} ${errorId}` : hintId
          }
        />

        <p id={hintId} className="text-xs text-muted-foreground">
          {t('availableToWithdraw', {
            amount: formatCurrency(available),
          })}
        </p>

        {showError && (
          <p id={errorId} className="text-xs text-destructive">
            {error === 'exceeds'
              ? t('errors.withdrawExceeds', {
                  amount: formatCurrency(available),
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

        <Button
          type="submit"
          disabled={isSubmitting || available <= 0}
        >
          {t('withdrawAction')}
        </Button>
      </DialogFooter>
    </form>
  );
}

export const WithdrawalModal = ({
  isOpen,
  onClose,
  onSubmit,
  goal,
  isSubmitting,
}: WithdrawalModalProps) => {
  const { t } = useTranslation('savingsGoals');

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
            {t('withdrawFundsTitle', { name: goal?.name })}
          </DialogTitle>
        </DialogHeader>

        {goal && (
          <WithdrawalForm
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
