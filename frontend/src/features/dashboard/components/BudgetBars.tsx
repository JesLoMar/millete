import { motion } from 'framer-motion';
import { useTranslation } from 'react-i18next';

import type { BudgetItem } from '../types';

interface BudgetBarsProps {
  data?: BudgetItem[];
  loading?: boolean;
  /**
   * Kept for backwards compatibility with existing callers.
   * Budget values are already calculated by the backend.
   */
  period?: string;
}

export function BudgetBars({
  data,
  loading = false,
}: BudgetBarsProps) {
  const { t } = useTranslation('dashboard');

  const budgets = data ?? [];

  if (loading) {
    return (
      <div className="space-y-4">
        {Array.from({ length: 5 }).map((_, index) => (
          <div
            key={index}
            className="animate-pulse space-y-2"
          >
            <div className="flex items-center justify-between gap-4">
              <div className="h-4 w-32 rounded bg-muted" />
              <div className="h-4 w-20 rounded bg-muted" />
            </div>

            <div className="h-3 w-full rounded-full bg-muted" />
          </div>
        ))}
      </div>
    );
  }

  if (budgets.length === 0) {
    return (
      <div className="flex min-h-30 items-center justify-center text-sm text-muted-foreground">
        {t('budgets.noData')}
      </div>
    );
  }

  return (
    <div className="space-y-5">
      {budgets.map((budget) => {
        const isOverBudget = budget.percentage >= 100;

        return (
          <div
            key={budget.category}
            className="space-y-2"
          >
            <div className="flex items-center justify-between gap-4">
              <span className="truncate font-medium">
                {budget.category}
              </span>

              <span className="shrink-0 text-sm text-muted-foreground">
                {budget.spent.toFixed(2)} € / {budget.limit.toFixed(2)} €
              </span>
            </div>

            <div className="relative h-3 overflow-hidden rounded-full bg-muted">
              <motion.div
                initial={{ width: 0 }}
                animate={{
                  width: `${Math.min(budget.percentage, 100)}%`,
                }}
                transition={{
                  duration: 0.5,
                  ease: 'easeOut',
                }}
                className="h-full rounded-full"
                style={{
                  backgroundColor: budget.color,
                }}
              />
            </div>

            <div className="flex items-center justify-between gap-4 text-sm">
              <span
                className={
                  isOverBudget
                    ? 'font-medium text-destructive'
                    : 'text-muted-foreground'
                }
              >
                {budget.percentage.toFixed(1)}%
              </span>

              <span className="text-muted-foreground">
                {isOverBudget
                  ? t('budgets.exceeded')
                  : t('budgets.remaining', {
                      amount: Math.max(budget.limit - budget.spent, 0).toFixed(2),
                    })}
              </span>
            </div>
          </div>
        );
      })}
    </div>
  );
}