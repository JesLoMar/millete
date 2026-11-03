import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import {
  AlertTriangle,
  BarChart3,
  Loader2,
} from 'lucide-react';

import { Badge } from '@/shared/components/core/badge';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/core/card';
import { cn } from '@/lib/utils';
import { formatCurrency } from '@/shared/utils/i18nFormat';

import {
  PERFORMANCE_PERIODS,
  type PerformancePeriodKey,
} from '../constants';
import { usePerformance } from '../hooks/useInvestmentQueries';
import type { Performance } from '../types';
import {
  clampPerformanceRange,
  formatPercent,
  formatSignedCurrency,
  getPerformanceRange,
} from '../utils';
import { ClosedLotsTable } from './ClosedLotsTable';
import { PanelState } from './PanelState';

interface PerformancePanelProps {
  trackingStartAt: string | null;
}

interface MetricCardProps {
  label: string;
  value: string;
  hint?: string;
  tone?: 'default' | 'positive' | 'negative';
}

function MetricCard({
  label,
  value,
  hint,
  tone = 'default',
}: MetricCardProps) {
  return (
    <div className="rounded-lg border border-border bg-card p-4">
      <p className="text-[11px] font-semibold tracking-wider text-muted-foreground uppercase">
        {label}
      </p>
      <p
        className={cn(
          'mt-2 text-lg font-semibold tracking-tight tabular-nums',
          tone === 'positive' && 'text-primary',
          tone === 'negative' && 'text-destructive',
        )}
      >
        {value}
      </p>
      {hint && (
        <p className="mt-1 text-xs text-muted-foreground">
          {hint}
        </p>
      )}
    </div>
  );
}

interface AttributionRowProps {
  label: string;
  value: number | null;
  currency: string;
  signed?: boolean;
}

function AttributionRow({
  label,
  value,
  currency,
  signed = true,
}: AttributionRowProps) {
  return (
    <div className="flex items-center justify-between gap-3 border-b border-border/60 py-2 text-sm last:border-0">
      <dt className="text-muted-foreground">{label}</dt>
      <dd
        className={cn(
          'font-medium tabular-nums',
          value != null &&
            value > 0 &&
            signed &&
            'text-primary',
          value != null &&
            value < 0 &&
            signed &&
            'text-destructive',
        )}
      >
        {value == null
          ? '—'
          : signed
            ? formatSignedCurrency(value, currency)
            : formatCurrency(value, currency)}
      </dd>
    </div>
  );
}

function getTone(
  value: number | null,
): 'default' | 'positive' | 'negative' {
  if (value == null || value === 0) {
    return 'default';
  }

  return value > 0 ? 'positive' : 'negative';
}

function UnavailablePerformance({ performance }: { performance: Performance }) {
  const { t } = useTranslation('investments');

  return (
    <div className="flex flex-col items-center gap-2 rounded-lg border border-dashed border-border px-6 py-12 text-center">
      <AlertTriangle
        className="size-7 text-warning"
        aria-hidden="true"
      />
      <p className="text-sm font-medium text-foreground">
        {t('performance.unavailable')}
      </p>
      {performance.unavailableReason && (
        <p className="max-w-xl text-xs text-muted-foreground">
          {performance.unavailableReason}
        </p>
      )}
    </div>
  );
}

export function PerformancePanel({
  trackingStartAt,
}: PerformancePanelProps) {
  const { t } = useTranslation('investments');
  const [period, setPeriod] =
    useState<PerformancePeriodKey>('ytd');

  const range = useMemo(
    () =>
      clampPerformanceRange(
        getPerformanceRange(period, trackingStartAt),
        trackingStartAt,
      ),
    [period, trackingStartAt],
  );

  const query = usePerformance(range, true);
  const performance = query.data;

  const derivedReturn =
    performance?.openingValue != null &&
    performance.openingValue > 0 &&
    performance.portfolioChange != null
      ? (performance.portfolioChange /
          performance.openingValue) *
        100
      : null;

  return (
    <div className="space-y-4 sm:space-y-6">
      <Card>
        <CardHeader className="border-b border-border">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <div className="flex items-center gap-2">
              <BarChart3
                className="size-4 text-muted-foreground"
                aria-hidden="true"
              />
              <CardTitle>{t('performance.title')}</CardTitle>
            </div>

            <div className="flex flex-wrap gap-1">
              {PERFORMANCE_PERIODS.map((definition) => (
                <button
                  key={definition.key}
                  type="button"
                  onClick={() => setPeriod(definition.key)}
                  className={cn(
                    'rounded-md px-2.5 py-1 text-xs font-medium transition-colors',
                    period === definition.key
                      ? 'bg-primary text-primary-foreground'
                      : 'text-muted-foreground hover:bg-accent/60 hover:text-foreground',
                  )}
                >
                  {t(definition.labelKey)}
                </button>
              ))}
            </div>
          </div>
          <p className="text-xs text-muted-foreground">
            {t('performance.description')}
          </p>
        </CardHeader>

        <CardContent className="space-y-4 pt-4">
          {query.isLoading && (
            <div className="flex justify-center py-12">
              <Loader2
                className="size-7 animate-spin text-muted-foreground"
                aria-hidden="true"
              />
            </div>
          )}

          {query.isError && (
            <PanelState
              isLoading={false}
              isError
              onRetry={() => void query.refetch()}
              errorLabel={t('performance.loadError')}
            />
          )}

          {performance && !query.isError && (
            <>
              {performance.status === 'NOT_CALCULABLE' ? (
                <UnavailablePerformance performance={performance} />
              ) : (
                <>
                  <div className="flex flex-wrap items-center gap-2 text-xs text-muted-foreground">
                    <span>
                      {t('performance.range', {
                        from: new Date(
                          performance.from,
                        ).toLocaleDateString(),
                        to: new Date(
                          performance.to,
                        ).toLocaleDateString(),
                      })}
                    </span>
                    {performance.estimated && (
                      <Badge variant="warning">
                        {t('portfolio.estimated')}
                      </Badge>
                    )}
                    <Badge variant="outline">
                      {t('performance.periodLabel')}
                    </Badge>
                  </div>

                  <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-4">
                    <MetricCard
                      label={t('performance.openingValue')}
                      value={
                        performance.openingValue == null
                          ? '—'
                          : formatCurrency(
                              performance.openingValue,
                              performance.currency,
                            )
                      }
                    />
                    <MetricCard
                      label={t('performance.endingValue')}
                      value={
                        performance.endingValue == null
                          ? '—'
                          : formatCurrency(
                              performance.endingValue,
                              performance.currency,
                            )
                      }
                    />
                    <MetricCard
                      label={t('performance.change')}
                      value={formatSignedCurrency(
                        performance.portfolioChange,
                        performance.currency,
                      )}
                      tone={getTone(
                        performance.portfolioChange,
                      )}
                      hint={t('performance.changeHint')}
                    />
                    <MetricCard
                      label={t('performance.return')}
                      value={formatPercent(derivedReturn)}
                      tone={getTone(derivedReturn)}
                      hint={t('performance.returnHint')}
                    />
                  </div>

                  <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
                    <div>
                      <h3 className="mb-2 text-sm font-semibold text-foreground">
                        {t('performance.flows')}
                      </h3>
                      <dl>
                        <AttributionRow
                          label={t('performance.contributions')}
                          value={performance.contributions}
                          currency={performance.currency}
                          signed={false}
                        />
                        <AttributionRow
                          label={t('performance.withdrawals')}
                          value={performance.withdrawals}
                          currency={performance.currency}
                          signed={false}
                        />
                        <AttributionRow
                          label={t('performance.cashDividends')}
                          value={performance.cashDividends}
                          currency={performance.currency}
                          signed={false}
                        />
                        <AttributionRow
                          label={t('performance.inKindDividends')}
                          value={performance.inKindDividends}
                          currency={performance.currency}
                          signed={false}
                        />
                      </dl>
                    </div>

                    <div>
                      <h3 className="mb-2 text-sm font-semibold text-foreground">
                        {t('performance.effects')}
                      </h3>
                      <dl>
                        <AttributionRow
                          label={t('performance.realizedGains')}
                          value={performance.realizedGains}
                          currency={performance.currency}
                        />
                        <AttributionRow
                          label={t(
                            'performance.unrealizedPriceEffect',
                          )}
                          value={performance.unrealizedPriceEffect}
                          currency={performance.currency}
                        />
                        <AttributionRow
                          label={t('performance.fxEffect')}
                          value={performance.fxEffect}
                          currency={performance.currency}
                        />
                        <AttributionRow
                          label={t(
                            'performance.reconciliationDifference',
                          )}
                          value={
                            performance.reconciliationDifference
                          }
                          currency={performance.currency}
                        />
                      </dl>
                    </div>
                  </div>

                  {performance.reconciliationDifference != null &&
                    Math.abs(
                      performance.reconciliationDifference,
                    ) > 0.01 && (
                      <p className="flex items-start gap-2 rounded-md border border-warning/40 bg-warning/10 p-3 text-xs text-muted-foreground">
                        <AlertTriangle
                          className="mt-0.5 size-4 shrink-0 text-warning"
                          aria-hidden="true"
                        />
                        {t('performance.reconciliationWarning')}
                      </p>
                    )}

                  <p className="text-xs text-muted-foreground">
                    {t('performance.estimatedNote')}
                  </p>
                </>
              )}
            </>
          )}
        </CardContent>
      </Card>

      <ClosedLotsTable
        key={`${range.from}:${range.to}`}
        from={range.from}
        to={range.to}
      />
    </div>
  );
}
