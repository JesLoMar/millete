import { memo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { TrendingDown, TrendingUp } from 'lucide-react';
import { cn } from '@/lib/utils';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/core/card';
import { formatCurrency } from '@/shared/utils/i18nFormat';
import { formatPercentage } from '../utils';
import {
  PERFORMANCE_PERIODS,
  type PerformancePeriodKey,
} from '../constants';
import { usePerformance } from '../hooks/useInvestmentQueries';

export const PerformanceCard = memo(function PerformanceCard() {
  const { t } = useTranslation('investments');
  const [periodKey, setPeriodKey] =
    useState<PerformancePeriodKey>('1m');

  const { data: performance, isLoading, isError } =
    usePerformance(periodKey);

  const gain =
    performance &&
    performance.openingValue != null &&
    performance.endingValue != null
      ? performance.endingValue - performance.openingValue
      : null;

  const gainPercentage =
    performance &&
    performance.openingValue != null &&
    performance.openingValue !== 0 &&
    gain != null
      ? (gain / performance.openingValue) * 100
      : null;

  const isPositive = gain != null && gain >= 0;

  return (
    <Card>
      <CardHeader className="pb-3">
        <div className="flex items-center justify-between gap-2">
          <CardTitle className="text-sm font-medium">
            {t('performance.title')}
          </CardTitle>
          <div className="flex gap-1">
            {PERFORMANCE_PERIODS.map((period) => (
              <button
                key={period.key}
                type="button"
                onClick={() => setPeriodKey(period.key)}
                className={cn(
                  'rounded px-2 py-1 text-xs font-medium transition-colors',
                  periodKey === period.key
                    ? 'bg-primary text-primary-foreground'
                    : 'text-muted-foreground hover:bg-accent/50',
                )}
              >
                {t(period.labelKey)}
              </button>
            ))}
          </div>
        </div>
      </CardHeader>
      <CardContent>
        {isLoading ? (
          <p className="text-sm text-muted-foreground">
            {t('common:status.loading')}
          </p>
        ) : isError || !performance ? (
          <p className="text-sm text-muted-foreground">
            {t('performance.unavailable')}
          </p>
        ) : performance.status === 'NOT_CALCULABLE' ? (
          <p className="text-sm text-muted-foreground">
            {performance.unavailableReason ?? t('performance.unavailable')}
          </p>
        ) : (
          <div className="space-y-3">
            <div className="flex items-center gap-3">
              {isPositive ? (
                <TrendingUp
                  className="size-6 text-emerald-500"
                  aria-hidden="true"
                />
              ) : (
                <TrendingDown
                  className="size-6 text-destructive"
                  aria-hidden="true"
                />
              )}
              <div>
                <p
                  className={cn(
                    'text-2xl font-semibold',
                    isPositive ? 'text-emerald-500' : 'text-destructive',
                  )}
                >
                  {gain != null && performance.currency
                    ? formatCurrency(gain, performance.currency)
                    : '—'}
                </p>
                <p className="text-xs text-muted-foreground">
                  {gainPercentage != null
                    ? formatPercentage(gainPercentage)
                    : '—'}
                </p>
              </div>
            </div>

            <dl className="grid grid-cols-2 gap-2 text-sm">
              <div>
                <dt className="text-xs text-muted-foreground">
                  {t('performance.openingValue')}
                </dt>
                <dd className="font-medium">
                  {performance.openingValue != null
                    ? formatCurrency(
                        performance.openingValue,
                        performance.currency,
                      )
                    : '—'}
                </dd>
              </div>
              <div>
                <dt className="text-xs text-muted-foreground">
                  {t('performance.endingValue')}
                </dt>
                <dd className="font-medium">
                  {performance.endingValue != null
                    ? formatCurrency(
                        performance.endingValue,
                        performance.currency,
                      )
                    : '—'}
                </dd>
              </div>
              <div>
                <dt className="text-xs text-muted-foreground">
                  {t('performance.contributions')}
                </dt>
                <dd className="font-medium">
                  {performance.contributions != null
                    ? formatCurrency(
                        performance.contributions,
                        performance.currency,
                      )
                    : '—'}
                </dd>
              </div>
              <div>
                <dt className="text-xs text-muted-foreground">
                  {t('performance.withdrawals')}
                </dt>
                <dd className="font-medium">
                  {performance.withdrawals != null
                    ? formatCurrency(
                        performance.withdrawals,
                        performance.currency,
                      )
                    : '—'}
                </dd>
              </div>
              <div>
                <dt className="text-xs text-muted-foreground">
                  {t('performance.realizedGains')}
                </dt>
                <dd className="font-medium">
                  {performance.realizedGains != null
                    ? formatCurrency(
                        performance.realizedGains,
                        performance.currency,
                      )
                    : '—'}
                </dd>
              </div>
              <div>
                <dt className="text-xs text-muted-foreground">
                  {t('performance.dividends')}
                </dt>
                <dd className="font-medium">
                  {performance.cashDividends != null
                    ? formatCurrency(
                        performance.cashDividends,
                        performance.currency,
                      )
                    : '—'}
                </dd>
              </div>
            </dl>

            {performance.estimated && (
              <p className="text-xs text-muted-foreground">
                {t('performance.estimatedNote')}
              </p>
            )}
          </div>
        )}
      </CardContent>
    </Card>
  );
});