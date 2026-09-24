import { cn } from '@/lib/utils';

import { NewRecurringTransactionDialog } from './dialogs/NewRecurringTransactionDialog';
import { NewTransactionDialog } from './dialogs/NewTransactionDialog';

interface TransactionActionsProps {
  className?: string;
}

export function TransactionActions({
  className,
}: TransactionActionsProps) {
  return (
    <div className={cn('flex items-center gap-2 sm:gap-3', className)}>
      <NewRecurringTransactionDialog />
      <NewTransactionDialog />
    </div>
  );
}