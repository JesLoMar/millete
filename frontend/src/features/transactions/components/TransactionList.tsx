import { useState } from 'react';
import { useTranslation } from 'react-i18next';

import { ConfirmDeletionDialog } from '@/shared/components/ConfirmDeletionDialog';
import { type PeriodFilter } from '@/shared/components/Header';

import { useTransactionMutations } from '../hooks/useTransactionMutation';
import { useTransactions } from '../hooks/useTransactions';
import { type Filter } from '../constants';
import { EditTransactionDialog } from './dialogs/EditTransactionDialog';
import { TransactionListDesktop } from './TransactionListDesktop';
import { TransactionListFilters } from './TransactionListFilters';
import { TransactionListMobile } from './TransactionListMobile';
import { TransactionListPagination } from './TransactionListPagination';
import { TransactionListSkeleton } from './TransactionListSkeleton';
import type { Transaction } from './types';

interface TransactionListProps {
  period: PeriodFilter;
}

interface ListState {
  editingTransaction: Transaction | null;
  deletingTransaction: Transaction | null;
}

export function TransactionList({ period }: TransactionListProps) {
  const { t } = useTranslation('transactions');
  const { deleteTransaction } = useTransactionMutations();

  const [state, setState] = useState<ListState>({
    editingTransaction: null,
    deletingTransaction: null,
  });

  const [filter, setFilter] = useState<Filter>('all');
  const [searchTerm, setSearchTerm] = useState('');

  const {
    displayItems: transactions,
    displayPage,
    displaySize,
    totalDisplayPages,
    totalElements,
    isLoading,
    nextPage,
    prevPage,
  } = useTransactions({
    search: searchTerm,
    type: filter,
    period,
  });

  const updateState = (updates: Partial<ListState>) => {
    setState((previous) => ({
      ...previous,
      ...updates,
    }));
  };

  const handleDeleteConfirm = async () => {
    const transaction = state.deletingTransaction;

    if (!transaction) {
      return;
    }

    try {
      await deleteTransaction.mutateAsync(transaction.id);
      updateState({ deletingTransaction: null });
    } catch {
      // useTransactionMutations already handles the error notification.
    }
  };

  if (isLoading && transactions.length === 0) {
    return <TransactionListSkeleton />;
  }

  const hasActiveFilters =
    filter !== 'all' || searchTerm.trim().length > 0;

  const from =
    totalElements === 0 ? 0 : displayPage * displaySize + 1;

  const to = Math.min(
    (displayPage + 1) * displaySize,
    totalElements,
  );

  return (
    <div className="space-y-4">
      <TransactionListFilters
        filter={filter}
        searchTerm={searchTerm}
        totalCount={totalElements}
        onFilterChange={setFilter}
        onSearchChange={setSearchTerm}
      />

      <div className="overflow-hidden rounded-xl border border-border bg-card">
        {transactions.length === 0 ? (
          <p className="py-12 text-center text-sm text-muted-foreground">
            {hasActiveFilters
              ? t('noResults', {
                  defaultValue: 'No se encontraron transacciones.',
                })
              : t('empty')}
          </p>
        ) : (
          <>
            <TransactionListDesktop
              transactions={transactions}
              onEdit={(transaction) =>
                updateState({ editingTransaction: transaction })
              }
              onDelete={(transaction) =>
                updateState({ deletingTransaction: transaction })
              }
            />

            <TransactionListMobile
              transactions={transactions}
              onEdit={(transaction) =>
                updateState({ editingTransaction: transaction })
              }
              onDelete={(transaction) =>
                updateState({ deletingTransaction: transaction })
              }
            />
          </>
        )}

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

      <EditTransactionDialog
        key={state.editingTransaction?.id}
        transaction={state.editingTransaction}
        open={state.editingTransaction !== null}
        onOpenChange={(open) => {
          if (!open) {
            updateState({ editingTransaction: null });
          }
        }}
      />

      <ConfirmDeletionDialog
        open={state.deletingTransaction !== null}
        onOpenChange={(open) => {
          if (!open) {
            updateState({ deletingTransaction: null });
          }
        }}
        itemName={state.deletingTransaction?.description ?? ''}
        onConfirm={handleDeleteConfirm}
        isDeleting={deleteTransaction.isPending}
        title={t('deleteTitle')}
        description={t('deleteConfirmation', {
          name: state.deletingTransaction?.description ?? '',
        })}
      />
    </div>
  );
}