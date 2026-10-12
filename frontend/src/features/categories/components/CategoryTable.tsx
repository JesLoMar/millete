import {
  useCallback,
  useMemo,
  useState,
} from 'react';
import { useTranslation } from 'react-i18next';
import { m, MotionConfig } from 'framer-motion';
import {
  ChevronLeft,
  ChevronRight,
  Search,
} from 'lucide-react';

import type { PeriodFilter } from '@/shared/components/PeriodSelector';
import { ConfirmDeletionDialog } from '@/shared/components/ConfirmDeletionDialog';
import { Input } from '@/shared/components/core/input';
import { Button } from '@/shared/components/core/button';

import { useCategories } from '../hooks/useCategories';
import { useCategoryExpenses } from '../hooks/useCategoryExpenses';
import { useCategoryMutations } from '../hooks/useCategoryMutations';
import type { Category } from '../types';
import { getPeriodBudgetLimit } from '../utils/budget';
import { CategoryRow } from './CategoryRow';
import { CategoryRowsSkeleton } from './CategoryTableSkeleton';
import { EditCategoryDialog } from './EditCategoryDialog';

interface CategoryTableProps {
  period: PeriodFilter;
}

interface EditTarget {
  category: Category;
  /** Incrementa en cada apertura para reinicializar el formulario. */
  session: number;
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

  /*
   * Conservamos el objetivo mientras el diálogo se cierra.
   * Así no desaparecen el nombre y el título durante
   * la animación de salida.
   */
  const [editTarget, setEditTarget] =
    useState<EditTarget | null>(null);

  const [isEditOpen, setIsEditOpen] =
    useState(false);

  const [deleteTarget, setDeleteTarget] =
    useState<Category | null>(null);

  const [isDeleteOpen, setIsDeleteOpen] =
    useState(false);

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

  /*
   * Usamos el ID de la categoría y no su nombre:
   * el nombre puede cambiar o repetirse.
   */
  const expensesByCategoryId = useMemo(() => {
    const map: Record<string, number> = {};

    for (const category of expensesData?.categories ?? []) {
      if (category.categoryId) {
        map[category.categoryId] = category.amount;
      }
    }

    return map;
  }, [expensesData]);

  const handleEdit = useCallback((category: Category) => {
    setEditTarget((previous) => ({
      category,
      session: (previous?.session ?? 0) + 1,
    }));

    setIsEditOpen(true);
  }, []);

  const handleRequestDelete = useCallback(
    (category: Category) => {
      setDeleteTarget(category);
      setIsDeleteOpen(true);
    },
    [],
  );

  const handleConfirmDelete = async () => {
    if (!deleteTarget || isDeleting) {
      return;
    }

    try {
      await deleteCategory.mutateAsync(deleteTarget.id);

      setIsDeleteOpen(false);
    } catch {
      // La mutación ya muestra la notificación de error.
      // El diálogo permanece abierto para poder reintentar.
    }
  };

  /*
   * Solo mostramos el skeleton de las filas.
   * El buscador y la cabecera nunca se desmontan al buscar.
   */
  const showSkeleton =
    isLoading && categories.length === 0;

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
      {/* Error independiente de la consulta de gastos */}
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
            onClick={() => {
              void refetchExpenses();
            }}
            disabled={isExpensesFetching}
          >
            {t('categories:actions.retry')}
          </Button>
        </div>
      )}

      {/* Cabecera y buscador: distribución original */}
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
            onChange={(event) => {
              setSearchTerm(event.target.value);
            }}
          />
        </div>

        <div className="flex items-center gap-3">
          <span className="rounded-lg bg-secondary/50 px-3 py-1.5 text-xs font-medium text-muted-foreground">
            {t(`dashboard:header.period.${period}`)}
          </span>

          {!showSkeleton && (
            <p className="text-sm text-muted-foreground">
              {t('categories:showing', {
                total: totalElements,
              })}
            </p>
          )}
        </div>
      </div>

      {/* Tabla */}
      <div className="overflow-hidden rounded-xl border border-border bg-card">
        {showSkeleton ? (
          <CategoryRowsSkeleton />
        ) : (
          <MotionConfig reducedMotion="user">
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
                  const spent =
                    expensesByCategoryId[category.id] ?? 0;

                  const budgetLimit =
                    getPeriodBudgetLimit(
                      category.budgetLimit,
                      period,
                    );

                  /*
                   * El porcentaje es real y puede superar el 100 %.
                   * El recorte se realiza únicamente en CategoryRow.
                   */
                  const percentage =
                    budgetLimit !== null
                      ? (spent / budgetLimit) * 100
                      : 0;

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
                        onEdit={handleEdit}
                        onDelete={handleRequestDelete}
                      />
                    </m.div>
                  );
                })
              )}
            </m.div>
          </MotionConfig>
        )}

        {/* Paginación original */}
        {totalDisplayPages > 1 && (
          <div className="flex items-center justify-between border-t border-border bg-background/20 px-6 py-4">
            <p className="text-xs font-medium text-muted-foreground">
              {t('transactions:showingInterval', {
                from,
                to,
                total: totalElements,
              })}
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
                {displayPage + 1} / {totalDisplayPages}
              </span>

              <Button
                variant="outline"
                size="sm"
                onClick={nextPage}
                disabled={
                  displayPage >= totalDisplayPages - 1
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

      {/* Edición: mantiene el objetivo durante el cierre */}
      <EditCategoryDialog
        key={editTarget?.session}
        category={editTarget?.category ?? null}
        open={isEditOpen}
        onOpenChange={(open) => {
          if (!open) {
            setIsEditOpen(false);
          }
        }}
      />

      {/* Eliminación: mantiene el nombre durante el cierre */}
      <ConfirmDeletionDialog
        open={isDeleteOpen}
        onOpenChange={(open) => {
          if (!open && isDeleting) {
            return;
          }

          setIsDeleteOpen(open);
        }}
        itemName={deleteTarget?.name ?? ''}
        onConfirm={handleConfirmDelete}
        isDeleting={isDeleting}
      />
    </div>
  );
}