import { useTranslation } from 'react-i18next';
import { MoreHorizontal } from 'lucide-react';

import { Button } from '@/shared/components/core/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/shared/components/core/dropdown-menu';
import type { PlannedTransaction } from '@/features/transactions/hooks/usePlannedTransactions';

interface RecurringTransactionActionsProps {
  transaction: PlannedTransaction;
  onEdit: (tx: PlannedTransaction) => void;
  onDelete: (tx: PlannedTransaction) => void;
}

export function RecurringTransactionActions({
  transaction,
  onEdit,
  onDelete,
}: RecurringTransactionActionsProps) {
  const { t } = useTranslation('transactions');

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          type="button"
          variant="ghost"
          size="icon"
          className="size-8"
          aria-label={t('moreOptions')}
        >
          <MoreHorizontal
            size={16}
            aria-hidden="true"
          />
        </Button>
      </DropdownMenuTrigger>

      <DropdownMenuContent
        align="end"
        className="border-border bg-card"
      >
        <DropdownMenuItem
          className="cursor-pointer"
          onClick={() => onEdit(transaction)}
        >
          {t('edit')}
        </DropdownMenuItem>

        <DropdownMenuItem
          className="cursor-pointer text-destructive"
          onClick={() => onDelete(transaction)}
        >
          {t('delete')}
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}