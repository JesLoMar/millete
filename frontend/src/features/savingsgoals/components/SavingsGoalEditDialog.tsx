import { useRef } from 'react';

import { useTranslation } from 'react-i18next';

import { PiggyBank } from 'lucide-react';

import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/core/dialog';

import { useUpdateSavingsGoal } from '../hooks/useSavingsGoals';

import type { SavingsGoal } from '../types';

import { SavingsGoalForm } from './SavingsGoalForm';

interface SavingsGoalEditDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  goal: SavingsGoal | null;
}

export function SavingsGoalEditDialog({
  open,
  onOpenChange,
  goal,
}: SavingsGoalEditDialogProps) {
  const { t } = useTranslation('savingsGoals');

  const { mutate: updateGoal, isPending: isUpdating } =
    useUpdateSavingsGoal();

  const nameInputRef = useRef<HTMLInputElement>(null);

  const handleOpenChange = (nextOpen: boolean) => {
    if (!nextOpen && isUpdating) {
      return;
    }

    onOpenChange(nextOpen);
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent
        className="border-border bg-card sm:max-w-md"
        aria-describedby={undefined}
        onOpenAutoFocus={(event) => {
          event.preventDefault();
          nameInputRef.current?.focus();
        }}
      >
        <div className="max-h-[85dvh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-xl font-semibold text-foreground">
              <PiggyBank
                className="size-5 text-primary"
                aria-hidden="true"
              />
              {t('editGoalTitle')}
            </DialogTitle>
          </DialogHeader>

          {goal && (
            <SavingsGoalForm
              initialGoal={goal}
              minTargetAmount={goal.currentAmount}
              isSubmitting={isUpdating}
              submitLabel={t('saveChanges')}
              nameInputRef={nameInputRef}
              onCancel={() => handleOpenChange(false)}
              onSubmit={(values) =>
                updateGoal(
                  {
                    id: goal.id,
                    dto: values,
                  },
                  {
                    onSuccess: () => onOpenChange(false),
                  },
                )
              }
            />
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}