import { useRef, useState } from 'react';

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

import { SavingsGoalForm } from './SavingsGoalForm';

export function SavingsGoalDialog() {
  const { t } = useTranslation(['savingsGoals', 'common']);

  const { mutate: createGoal, isPending: isCreating } =
    useCreateSavingsGoal();

  const [open, setOpen] = useState(false);

  const nameInputRef = useRef<HTMLInputElement>(null);

  // No se puede cerrar (Escape, clic fuera, Cancelar) mientras se guarda: si
  // fallase, el usuario perdería lo que había escrito.
  const handleOpenChange = (nextOpen: boolean) => {
    if (!nextOpen && isCreating) {
      return;
    }

    setOpen(nextOpen);
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogTrigger asChild>
        <Button
          type="button"
          className="h-9 gap-2 bg-primary px-4 font-semibold hover:bg-primary/90"
        >
          <Plus size={16} aria-hidden="true" />
          {t('newGoal')}
        </Button>
      </DialogTrigger>

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

          {/* Se monta al abrir y se desmonta al cerrar: el formulario se
              reinicia solo, también con Cancelar. */}
          <SavingsGoalForm
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
