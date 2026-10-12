import { memo } from 'react';
import {
  Edit2,
  MoreHorizontal,
  Trash2,
} from 'lucide-react';
import { useTranslation } from 'react-i18next';

import { cn } from '@/lib/utils';
import { formatCurrency } from '@/shared/utils/i18nFormat';
import { Button } from '@/shared/components/core/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/shared/components/core/dropdown-menu';
import { ProgressBar } from '@/shared/components/core/progress-bar';

import type { Category } from '../types';

interface CategoryRowProps {
  category: Category;
  spent: number;
  /** Presupuesto convertido al periodo seleccionado. */
  budgetLimit: number | null;
  /** Porcentaje real de gasto; puede superar el 100 %. */
  percentage: number;
  onEdit: (category: Category) => void;
  onDelete: (category: Category) => void;
}

interface CategoryActionsProps {
  category: Category;
  onEdit: (category: Category) => void;
  onDelete: (category: Category) => void;
}

const DEFAULT_CATEGORY_COLOR = '#4A6FA5';

export const CategoryRow = memo(function CategoryRow({
  category,
  spent,
  budgetLimit,
  percentage,
  onEdit,
  onDelete,
}: CategoryRowProps) {
  const { t } = useTranslation('categories');

  const hasBudget =
    budgetLimit !== null && budgetLimit > 0;

  // Alcanzar exactamente el límite también se considera excedido.
  const isOverBudget =
    hasBudget && spent >= budgetLimit;

  // El porcentaje mostrado no supera el 100 %.
  const roundedPercentage = Math.min(
    Math.round(percentage),
    100,
  );

  // Solo se recorta la barra visual.
  const barValue = Math.min(percentage, 100);

  const formattedSpent = formatCurrency(spent);

  const formattedBudget = hasBudget
    ? formatCurrency(budgetLimit)
    : '—';

  const categoryColor =
    category.color || DEFAULT_CATEGORY_COLOR;

  const description = category.description?.trim();

  return (
    <div className="group grid grid-cols-[auto_minmax(0,1fr)_auto] items-center gap-x-2.5 gap-y-2 border-b p-3 transition-colors hover:bg-accent/30 last:border-0 sm:flex sm:items-center sm:gap-4 sm:p-4">
      {/* Color de la categoría */}
      <div
        className="size-4 shrink-0 rounded-full sm:size-5"
        style={{ backgroundColor: categoryColor }}
        aria-hidden="true"
      />

      {/* Nombre y descripción */}
      <div className="min-w-0 sm:w-32 sm:flex-none">
        <p className="truncate text-sm font-semibold">
          {category.name}
        </p>

        {description && (
          <p
            className="truncate text-xs text-muted-foreground"
            title={description}
          >
            {description}
          </p>
        )}
      </div>

      {/*
       * Móvil: una fila inferior con progreso e importe.
       * Escritorio: display: contents integra ambos elementos
       * directamente en la fila flex principal.
       */}
      <div className="col-span-3 row-start-2 flex min-w-0 items-center gap-3 sm:contents">
        {/* Progreso y porcentaje */}
        <div className="min-w-0 flex-1 sm:px-2">
          {hasBudget ? (
            <div className="flex flex-col-reverse gap-1 sm:flex-col">
              <span
                className={cn(
                  'text-xs font-medium',
                  isOverBudget
                    ? 'text-destructive'
                    : 'text-muted-foreground',
                )}
              >
                {roundedPercentage}%
              </span>

              <ProgressBar
                value={barValue}
                max={100}
                color={
                  isOverBudget
                    ? undefined
                    : categoryColor
                }
                variant={
                  isOverBudget
                    ? 'overbudget'
                    : 'default'
                }
                size="sm"
                ariaLabel={`${category.name}: ${roundedPercentage}%`}
              />
            </div>
          ) : (
            <p className="text-xs italic text-muted-foreground">
              {t('categories:noBudgetTooltip')}
            </p>
          )}
        </div>

        {/* El ancho fijo se aplica solo a escritorio */}
        <span className="shrink-0 whitespace-nowrap text-right text-xs tabular-nums text-muted-foreground sm:w-40 sm:text-sm">
          {formattedSpent} / {formattedBudget}
        </span>
      </div>

      {/* Acciones: arriba a la derecha en móvil */}
      <div className="col-start-3 row-start-1 -mr-1 shrink-0 sm:mr-0">
        <CategoryActions
          category={category}
          onEdit={onEdit}
          onDelete={onDelete}
        />
      </div>
    </div>
  );
});

function CategoryActions({
  category,
  onEdit,
  onDelete,
}: CategoryActionsProps) {
  const { t } = useTranslation('categories');

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          variant="ghost"
          size="icon"
          className="size-7 shrink-0 sm:size-8"
          aria-label={`${t('edit')} / ${t('delete')}`}
        >
          <MoreHorizontal
            size={15}
            className="sm:size-4"
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
          onClick={() => onEdit(category)}
        >
          <Edit2
            className="mr-2 size-4"
            aria-hidden="true"
          />
          {t('edit')}
        </DropdownMenuItem>

        <DropdownMenuItem
          className="cursor-pointer text-destructive"
          onClick={() => onDelete(category)}
        >
          <Trash2
            className="mr-2 size-4"
            aria-hidden="true"
          />
          {t('delete')}
        </DropdownMenuItem>
      </DropdownMenuContent>
    </DropdownMenu>
  );
}