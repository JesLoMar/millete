import { useEffect, useState } from 'react';
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

import type { SavingsGoal } from '../types';

interface ContributionModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSubmit: (amount: number) => void;
  goal: SavingsGoal | null;
  isSubmitting: boolean;
}

export const ContributionModal = ({
  isOpen,
  onClose,
  onSubmit,
  goal,
  isSubmitting,
}: ContributionModalProps) => {
  const { t } = useTranslation(['savingsGoals', 'common']);

  const [amount, setAmount] = useState('');

  useEffect(() => {
    if (isOpen) {
      setAmount('');
    }
  }, [isOpen]);

  const handleSubmit = (
    event: React.FormEvent<HTMLFormElement>,
  ) => {
    event.preventDefault();

    const parsedAmount = Number(amount);

    if (!Number.isFinite(parsedAmount) || parsedAmount <= 0) {
      return;
    }

    onSubmit(parsedAmount);
  };

  return (
    <Dialog
      open={isOpen}
      onOpenChange={(open) => {
        if (!open && !isSubmitting) {
          onClose();
        }
      }}
    >
      <DialogContent>
        <DialogHeader>
          <DialogTitle>
            {t('addFunds', { name: goal?.name })}
          </DialogTitle>
        </DialogHeader>

        <form
          onSubmit={handleSubmit}
          className="flex flex-col gap-4 py-4"
        >
          <div className="space-y-2">
            <Label htmlFor="savings-goal-contribution-amount">
              {t('amount')}
            </Label>

            <Input
              id="savings-goal-contribution-amount"
              type="number"
              min="0.01"
              step="0.01"
              value={amount}
              onChange={(event) =>
                setAmount(event.target.value)
              }
              disabled={isSubmitting}
            />
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={onClose}
              disabled={isSubmitting}
            >
              {t('common:actions.cancel')}
            </Button>

            <Button
              type="submit"
              disabled={isSubmitting}
            >
              {t('add')}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};