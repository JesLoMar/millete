import {
  LazyMotion,
  domAnimation,
  m,
} from 'framer-motion';
import { useTranslation } from 'react-i18next';

import type { PeriodFilter } from '@/shared/components/Header';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/core/card';

import type { BudgetItem } from '../types';

interface BudgetBarsProps {
  data?: BudgetItem[];
  loading?: boolean;
  period?: PeriodFilter;
}

const DISPLAY_LIMIT = 5;

function getAdjustedBudgetLimit(
  budgetLimit: number | null | undefined,
  period: PeriodFilter,
): number {
  if (!budgetLimit) {
    return 0;
  }

  return period === 'week'
    ? budgetLimit / 4
    : period === 'year'
      ? budgetLimit * 12
      : budgetLimit;
}

export function BudgetBars({
  data: externalData,
  loading = false,
  period = 'month',
}: BudgetBarsProps) {
  const { t } = useTranslation([
    'dashboard',
    'common',
  ]);

  const budgets = externalData ?? [];

  if (loading) {
    return (
      <Card className="col-span-1 border md:col-span-5">
        <CardHeader>
          <div className="h-6 w-44 animate-pulse rounded bg-muted" />
        </CardHeader>

        <CardContent className="min-h-96">
          <div className="space-y-4">
            {Array.from({
              length: DISPLAY_LIMIT,
            }).map((_, index) => (
              <div
                key={index}
                className="space-y-2"
              >
                <div className="flex justify-between">
                  <div className="h-4 w-24 animate-pulse rounded bg-muted" />
                  <div className="h-4 w-16 animate-pulse rounded bg-muted" />
                </div>

                <div className="h-3 w-full animate-pulse rounded-full bg-muted" />

                <div className="ml-auto h-3 w-20 animate-pulse rounded bg-muted" />
              </div>
            ))}
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className="col-span-1 border md:col-span-5">
      <CardHeader>
        <CardTitle className="font-serif text-lg font-semibold">
          {t('dashboard:budget.title')}
        </CardTitle>
      </CardHeader>

      <CardContent className="min-h-96">
        {budgets.length === 0 ? (
          <div className="flex min-h-72 items-center justify-center">
            <p className="text-center text-sm text-muted-foreground">
              {t('dashboard:budget.empty')}
            </p>
          </div>
        ) : (
          <LazyMotion features={domAnimation}>
            <div className="space-y-4">
              {budgets.map((budget) => {
                const adjustedLimit =
                  getAdjustedBudgetLimit(
                    budget.limit,
                    period,
                  );

                const percentageValue =
                  adjustedLimit > 0
                    ? (budget.spent / adjustedLimit) * 100
                    : 0;

                const percentage = Math.min(
                  percentageValue,
                  100,
                );

                const isOverLimit =
                  percentageValue >= 100;

                const isNearLimit =
                  percentageValue >= 80 &&
                  !isOverLimit;

                return (
                  <div
                    key={budget.category}
                    className="space-y-1.5"
                  >
                    <div className="flex justify-between gap-4 text-sm">
                      <span className="truncate font-medium">
                        {budget.category}
                      </span>

                      <span className="shrink-0 text-xs text-muted-foreground">
                        <span className="font-semibold text-foreground">
                          {budget.spent.toFixed(2)} €
                        </span>
                        {' / '}
                        {adjustedLimit.toFixed(2)} €
                      </span>
                    </div>

                    <div className="relative h-3 overflow-hidden rounded-full bg-muted">
                      <m.div
                        initial={{ scaleX: 0 }}
                        animate={{
                          scaleX: percentage / 100,
                        }}
                        transition={{
                          duration: 0.5,
                          ease: 'easeOut',
                        }}
                        style={{
                          transformOrigin: 'left center',
                        }}
                        className={
                          isOverLimit
                            ? 'h-full w-full rounded-full bg-destructive'
                            : isNearLimit
                              ? 'h-full w-full rounded-full bg-warning'
                              : 'h-full w-full rounded-full bg-primary'
                        }
                      />
                    </div>

                    <div
                      className={
                        isOverLimit
                          ? 'text-right text-xs font-medium text-destructive'
                          : isNearLimit
                            ? 'text-right text-xs text-warning'
                            : 'text-right text-xs text-muted-foreground'
                      }
                    >
                      {isOverLimit
                        ? t(
                            'dashboard:budget.exceededBy',
                            {
                              amount: Math.max(
                                percentageValue > 100
                                  ? budget.spent -
                                    adjustedLimit
                                  : 0,
                                0,
                              ).toFixed(2),
                            },
                          )
                        : t(
                            'dashboard:budget.remaining',
                            {
                              amount: Math.max(
                                adjustedLimit -
                                  budget.spent,
                                0,
                              ).toFixed(2),
                            },
                          )}
                    </div>
                  </div>
                );
              })}
            </div>
          </LazyMotion>
        )}
      </CardContent>
    </Card>
  );
}