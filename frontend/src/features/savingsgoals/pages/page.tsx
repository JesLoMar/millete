import { useCallback, useState } from 'react';

import { Loader2, X } from 'lucide-react';

import { useTranslation } from 'react-i18next';

import { ConfirmDeletionDialog } from '@/shared/components/ConfirmDeletionDialog';
import { Header } from '@/shared/components/Header';
import { Button } from '@/shared/components/core/button';
import { Input } from '@/shared/components/core/input';
import { Pagination } from '@/shared/components/Pagination';
import { Sidebar } from '@/shared/components/Sidebar';
import { TopNav } from '@/shared/components/TopNav';

import { ContributionModal } from '../components/ContributionModal';
import { EmptyState } from '../components/EmptyState';
import { SavingsGoalCard } from '../components/SavingsGoalCard';
import { SavingsGoalDialog } from '../components/SavingsGoalDialog';
import { SavingsGoalEditDialog } from '../components/SavingsGoalEditDialog';
import { WithdrawalModal } from '../components/WithdrawalModal';

import { SEARCH_DEBOUNCE_MS } from '../constants';

import { useDebouncedValue } from '../hooks/useDebouncedValue';

import {
  useAddContribution,
  useDeleteSavingsGoal,
  useSavingsGoals,
  useWithdrawFromGoal,
} from '../hooks/useSavingsGoals';

import type { SavingsGoal } from '../types';

type GoalDialog = {
  kind: 'contribute' | 'withdraw' | 'edit' | 'delete';
  goal: SavingsGoal;
};

export const SavingsGoalsPage = () => {
  const { t } = useTranslation('savingsGoals');

  const [searchTerm, setSearchTerm] = useState('');

  const debouncedSearch = useDebouncedValue(
    searchTerm,
    SEARCH_DEBOUNCE_MS,
  );

  const [dialog, setDialog] = useState<GoalDialog | null>(null);
  const [isDialogOpen, setIsDialogOpen] = useState(false);

  const {
    displayItems: goals,
    displayPage,
    displaySize,
    totalDisplayPages,
    totalElements,
    isLoading,
    isError,
    isTransitioning,
    nextPage,
    prevPage,
    refetch,
  } = useSavingsGoals({
    search: debouncedSearch,
  });

  const {
    mutate: addContribution,
    isPending: isAddingContribution,
  } = useAddContribution();

  const {
    mutate: withdraw,
    isPending: isWithdrawing,
  } = useWithdrawFromGoal();

  const {
    mutate: deleteGoal,
    isPending: isDeleting,
  } = useDeleteSavingsGoal();

  const from =
    totalElements === 0 ? 0 : displayPage * displaySize + 1;

  const to = Math.min(
    (displayPage + 1) * displaySize,
    totalElements,
  );

  const openDialog = useCallback(
    (kind: GoalDialog['kind'], goal: SavingsGoal) => {
      setDialog({ kind, goal });
      setIsDialogOpen(true);
    },
    [],
  );

  const closeDialog = useCallback(() => {
    setIsDialogOpen(false);
  }, []);

  const openContribution = useCallback(
    (goal: SavingsGoal) => openDialog('contribute', goal),
    [openDialog],
  );

  const openWithdraw = useCallback(
    (goal: SavingsGoal) => openDialog('withdraw', goal),
    [openDialog],
  );

  const openEdit = useCallback(
    (goal: SavingsGoal) => openDialog('edit', goal),
    [openDialog],
  );

  const openDelete = useCallback(
    (goal: SavingsGoal) => openDialog('delete', goal),
    [openDialog],
  );

  const handleAddContribution = (amount: number) => {
    if (dialog?.kind !== 'contribute') {
      return;
    }

    addContribution(
      {
        id: dialog.goal.id,
        amount,
      },
      {
        onSuccess: closeDialog,
      },
    );
  };

  const handleWithdraw = (amount: number) => {
    if (dialog?.kind !== 'withdraw') {
      return;
    }

    withdraw(
      {
        id: dialog.goal.id,
        amount,
      },
      {
        onSuccess: closeDialog,
      },
    );
  };

  const handleDelete = () => {
    if (dialog?.kind !== 'delete') {
      return;
    }

    deleteGoal(
      dialog.goal.id,
      {
        onSuccess: closeDialog,
      },
    );
  };

  const isSearching = debouncedSearch.trim().length > 0;

  return (
    <div className="flex min-h-dvh overflow-hidden bg-background">
      <Sidebar />

      <div className="flex flex-1 flex-col overflow-hidden pt-16">
        <TopNav />

        <main className="flex-1 space-y-4 overflow-y-auto p-4 sm:space-y-6 sm:p-6">
          <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center sm:gap-4">
            <Header hidePeriodSelector />

            <div className="flex w-full flex-col sm:w-auto">
              <SavingsGoalDialog />
            </div>
          </div>

          <div className="space-y-4">
            <div className="relative">
              <Input
                type="text"
                role="searchbox"
                autoComplete="off"
                enterKeyHint="search"
                placeholder={t('searchPlaceholder')}
                aria-label={t('searchPlaceholder')}
                value={searchTerm}
                onChange={(event) =>
                  setSearchTerm(event.target.value)
                }
                onKeyDown={(event) => {
                  if (event.key === 'Escape') {
                    setSearchTerm('');
                  }
                }}
                className="w-full pr-10"
              />

              {searchTerm && (
                <button
                  type="button"
                  onClick={() => setSearchTerm('')}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                  aria-label={t('clearSearch')}
                >
                  <X className="size-4" aria-hidden="true" />
                </button>
              )}
            </div>

            {isLoading ? (
              <div
                className="flex items-center justify-center py-12"
                role="status"
                aria-live="polite"
              >
                <Loader2
                  className="size-8 animate-spin text-muted-foreground"
                  aria-hidden="true"
                />

                <span className="sr-only">
                  {t('loading')}
                </span>
              </div>
            ) : isError ? (
              <div
                className="flex flex-col items-center gap-3 py-8 text-center text-sm text-destructive"
                role="alert"
              >
                {t('loadingError')}

                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => void refetch()}
                >
                  {t('retry')}
                </Button>
              </div>
            ) : goals.length === 0 ? (
              <EmptyState isSearching={isSearching} />
            ) : (
              <>
                <div
                  aria-busy={isTransitioning}
                  className={`grid grid-cols-1 gap-4 transition-opacity sm:grid-cols-2 lg:grid-cols-3 ${
                    isTransitioning ? 'opacity-60' : ''
                  }`}
                >
                  {goals.map((goal) => (
                    <SavingsGoalCard
                      key={goal.id}
                      goal={goal}
                      onAddContribution={openContribution}
                      onWithdraw={openWithdraw}
                      onEdit={openEdit}
                      onDelete={openDelete}
                    />
                  ))}
                </div>

                <Pagination
                  currentPage={displayPage}
                  totalPages={totalDisplayPages}
                  from={from}
                  to={to}
                  total={totalElements}
                  onPrev={prevPage}
                  onNext={nextPage}
                />
              </>
            )}
          </div>
        </main>
      </div>

      <ContributionModal
        isOpen={
          isDialogOpen && dialog?.kind === 'contribute'
        }
        onClose={closeDialog}
        onSubmit={handleAddContribution}
        goal={
          dialog?.kind === 'contribute'
            ? dialog.goal
            : null
        }
        isSubmitting={isAddingContribution}
      />

      <WithdrawalModal
        isOpen={
          isDialogOpen && dialog?.kind === 'withdraw'
        }
        onClose={closeDialog}
        onSubmit={handleWithdraw}
        goal={
          dialog?.kind === 'withdraw'
            ? dialog.goal
            : null
        }
        isSubmitting={isWithdrawing}
      />

      <SavingsGoalEditDialog
        open={isDialogOpen && dialog?.kind === 'edit'}
        onOpenChange={(open) => {
          if (!open) {
            closeDialog();
          }
        }}
        goal={
          dialog?.kind === 'edit'
            ? dialog.goal
            : null
        }
      />

      <ConfirmDeletionDialog
        open={isDialogOpen && dialog?.kind === 'delete'}
        onOpenChange={(open) => {
          if (!open && !isDeleting) {
            closeDialog();
          }
        }}
        itemName={
          dialog?.kind === 'delete'
            ? dialog.goal.name
            : ''
        }
        onConfirm={handleDelete}
        isDeleting={isDeleting}
        title={t('deleteGoalTitle')}
        description={t('deleteGoalConfirmation', {
          name:
            dialog?.kind === 'delete'
              ? dialog.goal.name
              : '',
        })}
      />
    </div>
  );
};