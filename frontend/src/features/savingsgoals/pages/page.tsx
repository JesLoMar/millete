import { useState } from 'react';
import { Loader2 } from 'lucide-react';
import { useTranslation } from 'react-i18next';

import { ConfirmDeletionDialog } from '@/shared/components/ConfirmDeletionDialog';
import { Header } from '@/shared/components/Header';
import { Input } from '@/shared/components/core/input';
import { Pagination } from '@/shared/components/Pagination';
import { Sidebar } from '@/shared/components/Sidebar';
import { TopNav } from '@/shared/components/TopNav';

import { ContributionModal } from '../components/ContributionModal';
import { EmptyState } from '../components/EmptyState';
import { SavingsGoalCard } from '../components/SavingsGoalCard';
import { SavingsGoalDialog } from '../components/SavingsGoalDialog';
import { SavingsGoalEditDialog } from '../components/SavingsGoalEditDialog';
import {
  useAddContribution,
  useDeleteSavingsGoal,
  useSavingsGoals,
} from '../hooks/useSavingsGoals';
import type { SavingsGoal } from '../types';

export const SavingsGoalsPage = () => {
  const { t } = useTranslation('savingsGoals');

  const [searchTerm, setSearchTerm] = useState('');

  const [ui, setUi] = useState({
    isContributionOpen: false,
    isEditOpen: false,
    deletingGoal: null as SavingsGoal | null,
  });

  const [selectedGoal, setSelectedGoal] =
    useState<SavingsGoal | null>(null);

  const {
    displayItems: goals,
    displayPage,
    displaySize,
    totalDisplayPages,
    totalElements,
    isLoading,
    error,
    nextPage,
    prevPage,
  } = useSavingsGoals({
    search: searchTerm,
  });

  const { mutateAsync: addContribution } =
    useAddContribution();

  const {
    mutateAsync: deleteGoal,
    isPending: isDeleting,
  } = useDeleteSavingsGoal();

  const from =
    totalElements === 0
      ? 0
      : displayPage * displaySize + 1;

  const to = Math.min(
    (displayPage + 1) * displaySize,
    totalElements,
  );

  const openContribution = (goal: SavingsGoal) => {
    setSelectedGoal(goal);

    setUi((previous) => ({
      ...previous,
      isContributionOpen: true,
    }));
  };

  const openEdit = (goal: SavingsGoal) => {
    setSelectedGoal(goal);

    setUi((previous) => ({
      ...previous,
      isEditOpen: true,
    }));
  };

  const openDelete = (goal: SavingsGoal) => {
    setUi((previous) => ({
      ...previous,
      deletingGoal: goal,
    }));
  };

  const closeContribution = () => {
    setUi((previous) => ({
      ...previous,
      isContributionOpen: false,
    }));

    setSelectedGoal(null);
  };

  const handleAddContribution = async (amount: number) => {
    if (!selectedGoal) {
      return;
    }

    try {
      await addContribution({
        id: selectedGoal.id,
        amount,
      });

      closeContribution();
    } catch {
    }
  };

  const handleDelete = async () => {
    if (!ui.deletingGoal) {
      return;
    }

    try {
      await deleteGoal(ui.deletingGoal.id);

      setUi((previous) => ({
        ...previous,
        deletingGoal: null,
      }));
    } catch {
    }
  };

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

          {isLoading && (
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
          )}

          {!isLoading && error && (
            <div
              className="py-8 text-center text-sm text-destructive"
              role="alert"
            >
              {t('loadingError')}
            </div>
          )}

          {!isLoading && !error && (
            <div className="space-y-4">
              <div className="relative">
                <Input
                  type="search"
                  placeholder={t('searchPlaceholder')}
                  value={searchTerm}
                  onChange={(event) =>
                    setSearchTerm(event.target.value)
                  }
                  className="w-full"
                />

                {searchTerm && (
                  <button
                    type="button"
                    onClick={() => setSearchTerm('')}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                    aria-label={t('clearSearch')}
                  >
                    ✕
                  </button>
                )}
              </div>

              {goals.length === 0 ? (
                <EmptyState />
              ) : (
                <>
                  <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
                    {goals.map((goal) => (
                      <SavingsGoalCard
                        key={goal.id}
                        goal={goal}
                        onAddContribution={openContribution}
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
          )}
        </main>
      </div>

      <ContributionModal
        isOpen={ui.isContributionOpen}
        onClose={closeContribution}
        onSubmit={handleAddContribution}
        goal={selectedGoal}
      />

      <SavingsGoalEditDialog
        key={selectedGoal?.id}
        open={ui.isEditOpen}
        onOpenChange={(open) => {
          setUi((previous) => ({
            ...previous,
            isEditOpen: open,
          }));

          if (!open) {
            setSelectedGoal(null);
          }
        }}
        goal={selectedGoal}
      />

      <ConfirmDeletionDialog
        open={ui.deletingGoal !== null}
        onOpenChange={(open) => {
          if (!open) {
            setUi((previous) => ({
              ...previous,
              deletingGoal: null,
            }));
          }
        }}
        itemName={ui.deletingGoal?.name ?? ''}
        onConfirm={handleDelete}
        isDeleting={isDeleting}
        title={t('deleteGoalTitle')}
        description={t('deleteGoalConfirmation', {
          name: ui.deletingGoal?.name ?? '',
        })}
      />
    </div>
  );
};