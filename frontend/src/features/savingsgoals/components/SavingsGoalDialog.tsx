import {
  useRef,
  useState,
  type ReactNode,
} from 'react';

import { useTranslation } from 'react-i18next';

import { PiggyBank, Plus } from 'lucide-react';

import { Button } from '@/shared/components/core/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/shared/components/core/dialog';

import { useCreateSavingsGoal } from '../hooks/useSavingsGoals';

import {
  SavingsGoalForm,
  type SavingsGoalFormInitialValues,
} from './SavingsGoalForm';

interface SavingsGoalDialogProps {
  open?: boolean;
  onOpenChange?: (open: boolean) => void;
  initialValues?: SavingsGoalFormInitialValues;
  notice?: ReactNode;
}

export function SavingsGoalDialog({
  open: controlledOpen,
  onOpenChange: controlledOnOpenChange,
  initialValues,
  notice,
}: SavingsGoalDialogProps = {}) {
  const { t } = useTranslation('savingsGoals');

  const { mutate: createGoal, isPending: isCreating } =
    useCreateSavingsGoal();

  const [internalOpen, setInternalOpen] = useState(false);

  const isControlled = controlledOpen !== undefined;
  const open = isControlled ? controlledOpen : internalOpen;
  const setOpen =
    controlledOnOpenChange ?? setInternalOpen;

  const nameInputRef = useRef<HTMLInputElement>(null);

  const handleOpenChange = (nextOpen: boolean) => {
    if (!nextOpen && isCreating) {
      return;
    }

    setOpen(nextOpen);
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      {!isControlled && (
        <DialogTrigger asChild>
          <Button
            type="button"
            className="h-9 gap-2 bg-primary px-4 font-semibold hover:bg-primary/90"
          >
            <Plus size={16} aria-hidden="true" />
            {t('newGoal')}
          </Button>
        </DialogTrigger>
      )}

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
            <DialogTitle className="flex items-center gap-2 text-xl font-semibold">
              <PiggyBank
                className="size-5 text-primary"
                aria-hidden="true"
              />
              {t('newGoalTitle')}
            </DialogTitle>
          </DialogHeader>

          {notice}

          <SavingsGoalForm
            initialValues={initialValues}
            isSubmitting={isCreating}
            submitLabel={t('create')}
            nameInputRef={nameInputRef}
            onCancel={() => handleOpenChange(false)}
            onSubmit={(values) =>
              createGoal(values, {
                onSuccess: () => setOpen(false),
              })
            }
          />
        </div>
      </DialogContent>
    </Dialog>
  );
}
