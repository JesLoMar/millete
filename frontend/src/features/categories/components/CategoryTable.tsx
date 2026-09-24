import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { m } from 'framer-motion';
import {
  ChevronLeft,
  ChevronRight,
  Search,
} from 'lucide-react';

import {
  useCategories,
} from '@/features/categories/hooks/useCategories';
import {
  useCategoryBudgets,
} from '@/features/categories/hooks/useCategoryBudgets';
import type { Category } from '@/features/categories/types';
import type { PeriodFilter } from '@/shared/components/PeriodSelector';
import { Input } from '@/shared/components/core/input';
import { Button } from '@/shared/components/core/button';

import { useCategoryExpenses } from '../hooks/useCategoryExpenses';
import { useCategoryMutations } from '../hooks/useCategoryMutations';
import { EditCategoryDialog } from './EditCategoryDialog';
import { ConfirmDeletionDialog } from '../../../shared/components/ConfirmDeletionDialog';
import { CategoryRow } from './CategoryRow';
import { CategoryTableSkeleton } from './CategoryTableSkeleton';

interface CategoryTableProps {
  period: PeriodFilter;
}

const categoryListVariants = {
  hidden: {
    opacity: 0,
  },
  visible: {
    opacity: 1,
    transition: {
      staggerChildren: 0.04,
    },
  },
};

const categoryRowVariants = {
  hidden: {
    opacity: 0,
    x: -20,
  },
  visible: {
    opacity: 1,
    x: 0,
  },
};

const categoryRowTransition = {
  duration: 0.3,
  ease: 'easeOut' as const,
};

export function CategoryTable({
  period,
}: CategoryTableProps) {
  const { t } = useTranslation([
    'categories',
    'dashboard',
    'transactions',
  ]);

  const [searchTerm, setSearchTerm] = useState('');
  const [editingCategory, setEditingCategory] =
    useState<Category | null>(null);
  const [deletingCategory, setDeletingCategory] =
    useState<Category | null>(null);

  const {
    displayItems: categories,
    displayPage,
    displaySize,
    totalDisplayPages,
    totalElements,
    isLoading,
    nextPage,
    prevPage,
  } = useCategories({
    search: searchTerm,
  });

  const { deleteCategory, isDeleting } =
    useCategoryMutations();

  const {
    data: expensesData,
    isError: isExpensesError,
    refetch: refetchExpenses,
    isFetching: isExpensesFetching,
  } = useCategoryExpenses(period);

  const { data: budgetsData } =
    useCategoryBudgets(period);

  const budgetsByCategoryId = useMemo(() => {
    if (!budgetsData?.budgets) {
      return {};
    }

    return budgetsData.budgets.reduce<
      Record<
        string,
        {
          spent: number;
          limit: number;
          percentage: number;
        }
      >
    >((map, budget) => {
      map[budget.categoryId] = {
        spent: budget.spent,
        limit: budget.limit,
        percentage: budget.percentage,
      };

      return map;
    }, {});
  }, [budgetsData]);

  const expensesByCategoryId = useMemo(() => {
    if (!expensesData?.categories) {
      return {};
    }

    return expensesData.categories.reduce<
      Record<string, number>
    >((map, category) => {
      if (category.categoryId) {
        map[category.categoryId] = category.amount;
      }

      return map;
    }, {});
  }, [expensesData]);

  const handleDelete = async () => {
    if (!deletingCategory) {
      return;
    }

    try {
      await deleteCategory.mutateAsync(
        deletingCategory.id,
      );

      setDeletingCategory(null);
    } catch {
      // useCategoryMutations already handles the error notification.
    }
  };

  if (isLoading && categories.length === 0) {
    return <CategoryTableSkeleton />;
  }

  const from =
    totalElements === 0
      ? 0
      : displayPage * displaySize + 1;

  const to = Math.min(
    (displayPage + 1) * displaySize,
    totalElements,
  );

  return (
    <div className="space-y-4">
      {isExpensesError && (
        <div
          role="alert"
          className="flex items-center justify-between gap-4 rounded-lg border border-destructive/30 bg-destructive/5 px-4 py-3"
        >
          <p className="text-sm text-destructive">
            {t('categories:errors.expenses')}
          </p>

          <Button
            variant="outline"
            size="sm"
            onClick={() => refetchExpenses()}
            disabled={isExpensesFetching}
          >
            {t('categories:actions.retry')}
          </Button>
        </div>
      )}

      <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-center">
        <div className="relative w-full sm:w-[320px]">
          <Search
            className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground"
            aria-hidden="true"
          />

          <Input
            placeholder={t('categories:search')}
            aria-label={t('categories:search')}
            className="h-10 border-border bg-card pl-10"
            value={searchTerm}
            onChange={(event) =>
              setSearchTerm(event.target.value)
            }
          />
        </div>

        <div className="flex items-center gap-3">
          <span className="rounded-lg bg-secondary/50 px-3 py-1.5 text-xs font-medium text-muted-foreground">
            {t(
              `dashboard:header.period.${period}`,
            )}
          </span>

          <p className="text-sm text-muted-foreground">
            {t('categories:showing', {
              total: totalElements,
            })}
          </p>
        </div>
      </div>

      <div className="overflow-hidden rounded-xl border border-border bg-card">
        <m.div
          className="flex flex-col"
          initial="hidden"
          animate="visible"
          variants={categoryListVariants}
        >
          {categories.length === 0 ? (
            <p className="py-12 text-center text-sm text-muted-foreground">
              {t('categories:empty')}
            </p>
          ) : (
            categories.map((category) => {
              const budget =
                budgetsByCategoryId[category.id];

              const spent =
                budget?.spent ??
                expensesByCategoryId[category.id] ??
                0;

              const budgetLimit =
                budget?.limit ?? null;

              const percentage =
                budget?.percentage ?? 0;

              return (
                <m.div
                  key={category.id}
                  variants={categoryRowVariants}
                  transition={categoryRowTransition}
                >
                  <CategoryRow
                    category={category}
                    spent={spent}
                    budgetLimit={budgetLimit}
                    percentage={percentage}
                    onEdit={setEditingCategory}
                    onDelete={setDeletingCategory}
                  />
                </m.div>
              );
            })
          )}
        </m.div>

        {totalDisplayPages > 1 && (
          <div className="flex items-center justify-between border-t border-border bg-background/20 px-6 py-4">
            <p className="text-xs font-medium text-muted-foreground">
              {t(
                'transactions:showingInterval',
                {
                  from,
                  to,
                  total: totalElements,
                },
              )}
            </p>

            <div className="flex items-center gap-2">
              <Button
                variant="outline"
                size="sm"
                onClick={prevPage}
                disabled={displayPage === 0}
                className="h-8 border-border"
              >
                <ChevronLeft
                  size={16}
                  aria-hidden="true"
                />
              </Button>

              <span className="min-w-15 text-center text-sm text-muted-foreground">
                {displayPage + 1} /{' '}
                {totalDisplayPages}
              </span>

              <Button
                variant="outline"
                size="sm"
                onClick={nextPage}
                disabled={
                  displayPage >=
                  totalDisplayPages - 1
                }
                className="h-8 border-border"
              >
                <ChevronRight
                  size={16}
                  aria-hidden="true"
                />
              </Button>
            </div>
          </div>
        )}
      </div>

      <EditCategoryDialog
        key={editingCategory?.id}
        category={editingCategory}
        open={!!editingCategory}
        onOpenChange={(open) => {
          if (!open) {
            setEditingCategory(null);
          }
        }}
      />

      <ConfirmDeletionDialog
        open={!!deletingCategory}
        onOpenChange={(open) => {
          if (!open) {
            setDeletingCategory(null);
          }
        }}
        itemName={deletingCategory?.name ?? ''}
        onConfirm={handleDelete}
        isDeleting={isDeleting}
      />
    </div>
  );
}