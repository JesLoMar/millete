import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';

import { apiClient } from '@/shared/api/axiosClient';
import { formatCurrency, formatNumber } from '@/shared/utils/i18nFormat';
import type { PeriodFilter } from '@/shared/components/Header';

interface TransactionMetrics {
  income: number;
  expenses: number;
  balance: number;
  count: number;
  incomeTrend: number;
  expensesTrend: number;
  balanceTrend: number;
  countTrend: number;
}

interface TransactionSummaryProps {
  period: PeriodFilter;
}

export function TransactionSummary({ period }: TransactionSummaryProps) {
  const { t } = useTranslation(['dashboard', 'transactions']);

  const { data: metrics, isLoading } = useQuery<TransactionMetrics>({
    queryKey: ['transactionMetrics', period],
    queryFn: async () => {
      const response = await apiClient.get<TransactionMetrics>(
        '/transactions/metrics',
        {
          params: { period },
        },
      );

      return response.data;
    },
    staleTime: 60_000,
  });

  const periodLabel = t(
    `dashboard:metrics.vsLast${
      period === 'week'
        ? 'Week'
        : period === 'month'
          ? 'Month'
          : 'Year'
    }`,
  );

  const periodName = t(`dashboard:header.period.${period}`);

  const items = [
    { key: 'balance', trend: 'balanceTrend', label: t('dashboard:metrics.balance') },
    { key: 'income', trend: 'incomeTrend', label: t('dashboard:metrics.income', { period: periodName }) },
    { key: 'expenses', trend: 'expensesTrend', label: t('dashboard:metrics.expenses', { period: periodName }) },
    { key: 'count', trend: 'countTrend', label: t('transactions:title') },
  ] as const;

  return (
    <dl className="grid grid-cols-2 gap-x-4 gap-y-4 sm:grid-cols-4 xl:grid-cols-1" aria-busy={isLoading}>
      {items.map(({ key, trend, label }) => {
        const value = metrics?.[key] ?? 0;
        const change = metrics?.[trend] ?? 0;
        const favorable = key === 'expenses' ? change < 0 : change > 0;
        return (
          <div key={key} className="min-w-0">
            <dt className="text-sm font-medium text-muted-foreground">{label}</dt>
            <dd className="min-w-0 text-xl leading-tight font-semibold tabular-nums xl:text-2xl">
              {isLoading ? (
                <span aria-hidden="true" className="block h-8 w-20 max-w-full animate-pulse rounded bg-muted/50 motion-reduce:animate-none" />
              ) : (
                <>
                  <span className="block wrap-break-word">{key === 'count' ? formatNumber(value) : formatCurrency(value)}</span>
                  <span className="flex flex-wrap gap-x-1 text-xs font-normal xl:text-sm">
                    <span className={change === 0 ? 'text-muted-foreground' : favorable ? 'text-financial-positive' : 'text-financial-negative'}>
                      {formatNumber(change / 100, { style: 'percent', signDisplay: 'exceptZero', maximumFractionDigits: 1 })}
                    </span>
                    <span className="text-muted-foreground">{periodLabel}</span>
                  </span>
                </>
              )}
            </dd>
          </div>
        );
      })}
    </dl>
  );
}
