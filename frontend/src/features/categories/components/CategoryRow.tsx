import { memo } from 'react';
import { Edit2, MoreHorizontal, Trash2 } from 'lucide-react';
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
  budgetLimit: number | null;
  percentage: number;
  onEdit: (category: Category) => void;
  onDelete: (category: Category) => void;
}

interface CategoryActionsProps {
  category: Category;
  onEdit: (category: Category) => void;
  onDelete: (category: Category) => void;
  compact?: boolean;
}

const DEFAULT_CATEGORY_COLOR = '#4A6FA5';

const CategoryActions = ({
  category,
  onEdit,
  onDelete,
  compact = false,
}: CategoryActionsProps) => {
  const { t } = useTranslation('categories');

  return (
    <DropdownMenu>
      <DropdownMenuTrigger asChild>
        <Button
          variant="ghost"
          size="icon"
          className={cn(
            compact
              ? 'size-7 shrink-0 -mr-1'
              : 'size-8',
          )}
          aria-label={`${t('edit')} / ${t('delete')}`}
        >
          <MoreHorizontal
            size={compact ? 15 : 16}
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
};

export const CategoryRow = memo(function CategoryRow({
  category,
  spent,
  budgetLimit,
  percentage,
  onEdit,
  onDelete,
}: CategoryRowProps) {
  const { t } = useTranslation([
    'categories',
    'common',
  ]);

  const isOverBudget = percentage >= 100;
  const hasBudget =
    budgetLimit !== null && budgetLimit > 0;

  const formattedSpent = formatCurrency(spent);
  const formattedBudget = hasBudget
    ? formatCurrency(budgetLimit ?? 0)
    : '—';

  const categoryColor =
    category.color || DEFAULT_CATEGORY_COLOR;

  return (
    <>
      <div className="group hidden items-center gap-4 border-b p-4 transition-colors hover:bg-accent/30 last:border-0 sm:flex">
        <div
          className="size-5 shrink-0 rounded-full"
          style={{ backgroundColor: categoryColor }}
          aria-hidden="true"
        />

        <div className="w-32 min-w-0">
          <p className="truncate text-sm font-semibold">
            {category.name}
          </p>
        </div>

        <div className="min-w-0 flex-1 px-2">
          {hasBudget ? (
            <div className="space-y-1">
              <div className="flex justify-between text-xs font-medium">
                <span
                  className={cn(
                    isOverBudget
                      ? 'text-destructive'
                      : 'text-muted-foreground',
                  )}
                >
                  {percentage.toFixed(0)}%
                </span>
              </div>

              <ProgressBar
                value={percentage}
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
                ariaLabel={`${category.name}: ${percentage.toFixed(0)}%`}
              />
            </div>
          ) : (
            <p className="text-xs italic text-muted-foreground">
              {t('categories:noBudgetTooltip')}
            </p>
          )}
        </div>

        <div className="w-40 text-right text-sm tabular-nums text-muted-foreground">
          {formattedSpent} / {formattedBudget}
        </div>

        <CategoryActions
          category={category}
          onEdit={onEdit}
          onDelete={onDelete}
        />
      </div>

      <div className="border-b p-3 transition-colors hover:bg-accent/30 last:border-0 sm:hidden">
        <div className="mb-2 flex items-center gap-2.5">
          <div
            className="size-4 shrink-0 rounded-full"
            style={{ backgroundColor: categoryColor }}
            aria-hidden="true"
          />

          <p className="min-w-0 flex-1 truncate text-sm font-semibold">
            {category.name}
          </p>

          <CategoryActions
            category={category}
            onEdit={onEdit}
            onDelete={onDelete}
            compact
          />
        </div>

        <div className="flex items-center gap-3">
          <div className="min-w-0 flex-1">
            {hasBudget ? (
              <div className="space-y-1">
                <ProgressBar
                  value={percentage}
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
                  ariaLabel={`${category.name}: ${percentage.toFixed(0)}%`}
                />

                <p
                  className={cn(
                    'text-xs font-medium',
                    isOverBudget
                      ? 'text-destructive'
                      : 'text-muted-foreground',
                  )}
                >
                  {percentage.toFixed(0)}%
                </p>
              </div>
            ) : (
              <p className="text-xs italic text-muted-foreground">
                {t('categories:noBudgetTooltip')}
              </p>
            )}
          </div>

          <span className="shrink-0 text-right text-xs tabular-nums text-muted-foreground">
            {formattedSpent} / {formattedBudget}
          </span>
        </div>
      </div>
    </>
  );
});