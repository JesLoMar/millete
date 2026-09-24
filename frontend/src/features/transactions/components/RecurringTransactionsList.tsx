import { useState } from 'react';
import { useTranslation } from 'react-i18next';

import { ConfirmDeletionDialog } from '@/shared/components/ConfirmDeletionDialog';
import {
  usePlannedTransactions,
  type PlannedTransaction,
} from '@/features/transactions/hooks/usePlannedTransactions';

import { useTransactionMutations } from '../hooks/useTransactionMutation';
import type { Filter } from '../constants';
import { EditRecurringTransactionDialog } from './dialogs/EditRecurringTransactionDialog';
import { RecurringTransactionRow } from './RecurringTransactionRow';
import { TransactionListFilters } from './TransactionListFilters';
import { TransactionListPagination } from './TransactionListPagination';
import { TransactionSkeleton } from './TransactionSkeleton';

export function RecurringTransactionsList() {
  const { t } = useTranslation('transactions');
  const { deleteRecurring } = useTransactionMutations();

  const [recurringFilter, setRecurringFilter] =
    useState<Filter>('all');

  const [searchTerm, setSearchTerm] = useState('');

  const [editingTransaction, setEditingTransaction] =
    useState<PlannedTransaction | null>(null);

  const [deletingTransaction, setDeletingTransaction] =
    useState<PlannedTransaction | null>(null);

  const {
    displayItems: transactions,
    displayPage,
    displaySize,
    totalDisplayPages,
    totalElements,
    isLoading,
    nextPage,
    prevPage,
  } = usePlannedTransactions({
    search: searchTerm,
    type: recurringFilter,
  });

  const handleDelete = async () => {
    const transaction = deletingTransaction;

    if (!transaction) {
      return;
    }

    try {
      await deleteRecurring.mutateAsync(transaction.id);
      setDeletingTransaction(null);
    } catch {
      // useTransactionMutations already handles the error notification.
    }
  };

  if (isLoading && transactions.length === 0) {
    return <TransactionSkeleton rows={5} />;
  }

  const from =
    totalElements === 0 ? 0 : displayPage * displaySize + 1;

  const to = Math.min(
    (displayPage + 1) * displaySize,
    totalElements,
  );

  return (
    <div className="space-y-4">
      <TransactionListFilters
        filter={recurringFilter}
        searchTerm={searchTerm}
        totalCount={totalElements}
        onFilterChange={setRecurringFilter}
        onSearchChange={setSearchTerm}
      />

      <div className="overflow-hidden rounded-xl border border-border bg-card">
        <div className="flex flex-col">
          {transactions.length === 0 ? (
            <p className="py-12 text-center text-sm text-muted-foreground">
              {t('recurring.emptyFilter')}
            </p>
          ) : (
            transactions.map((transaction) => (
              <RecurringTransactionRow
                key={transaction.id}
                transaction={transaction}
                onEdit={setEditingTransaction}
                onDelete={setDeletingTransaction}
              />
            ))
          )}
        </div>

        <TransactionListPagination
          currentPage={displayPage}
          totalPages={totalDisplayPages}
          from={from}
          to={to}
          total={totalElements}
          onPrev={prevPage}
          onNext={nextPage}
        />
      </div>

      <EditRecurringTransactionDialog
        key={editingTransaction?.id}
        transaction={editingTransaction}
        open={editingTransaction !== null}
        onOpenChange={(open) => {
          if (!open) {
            setEditingTransaction(null);
          }
        }}
      />

      <ConfirmDeletionDialog
        open={deletingTransaction !== null}
        onOpenChange={(open) => {
          if (!open) {
            setDeletingTransaction(null);
          }
        }}
        itemName={deletingTransaction?.description ?? ''}
        onConfirm={handleDelete}
        isDeleting={deleteRecurring.isPending}
        title={t('recurring.deleteTitle')}
        description={t('recurring.deleteConfirmation', {
          name: deletingTransaction?.description ?? '',
        })}
      />
    </div>
  );
}