import { memo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { AlertTriangle } from 'lucide-react';
import { cn } from '@/lib/utils';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/core/card';
import { Badge } from '@/shared/components/core/badge';
import { formatCurrency } from '@/shared/utils/i18nFormat';
import { PERFORMANCE_PERIODS, type PerformancePeriodKey } from '../constants';
import { usePerformance } from '../hooks/useInvestmentQueries';

const RECONCILIATION_TOLERANCE = 0.01;

interface EffectRowProps {
  label: string;
  value: number | null | undefined;
  currency: string;
}

function EffectRow({ label, value, currency }: EffectRowProps) {
  return (
    <div className="flex items-center justify-between gap-2 py-1.5 text-sm">
      <dt className="text-muted-foreground">{label}</dt>
      <dd
        className={cn(
          'font-medium tabular-nums',
          value != null && value > 0 && 'text-emerald-500',
          value != null && value < 0 && 'text-destructive',
        )}
      >
        {value != null ? formatCurrency(value, currency) : '—'}
      </dd>
    </div>
  );
}

export const PerformanceCard = memo(function PerformanceCard() {
  const { t } = useTranslation('investments');
  const [periodKey, setPeriodKey] = useState<PerformancePeriodKey>('1m');
  const { data: performance, isLoading, isError } = usePerformance(periodKey);

  const hasDifference =
    performance?.reconciliationDifference != null &&
    Math.abs(performance.reconciliationDifference) > RECONCILIATION_TOLERANCE;

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
          <p className="text-sm text-muted-foreground">{t('common:status.loading')}</p>
        ) : isError || !performance ? (
          <p className="text-sm text-muted-foreground">{t('performance.unavailable')}</p>
        ) : performance.status !== 'CALCULATED' ? (
          <p className="text-sm text-muted-foreground">
            {performance.unavailableReason ??
              t('performance.insufficientData', {
                defaultValue: 'Aún no hay historial suficiente para este periodo.',
              })}
          </p>
        ) : (
          <div className="space-y-3">
            <div className="flex flex-wrap gap-1.5">
              {performance.estimated && (
                <Badge variant="secondary" className="text-[11px]">
                  {t('positions.flags.ESTIMATED', { defaultValue: 'Estimado' })}
                </Badge>
              )}
            </div>

            <dl className="divide-y divide-border">
              <EffectRow
                label={t('performance.priceEffect', { defaultValue: 'Variación de precio' })}
                value={performance.unrealizedPriceEffect}
                currency={performance.currency}
              />
              <EffectRow
                label={t('performance.fxEffect', { defaultValue: 'Efecto divisa' })}
                value={performance.fxEffect}
                currency={performance.currency}
              />
              <EffectRow
                label={t('performance.realizedGains')}
                value={performance.realizedGains}
                currency={performance.currency}
              />
              <EffectRow
                label={t('performance.dividends')}
                value={performance.cashDividends}
                currency={performance.currency}
              />
              <EffectRow
                label={t('performance.inKindDividends', { defaultValue: 'Dividendos en especie' })}
                value={performance.inKindDividends}
                currency={performance.currency}
              />
            </dl>

            <dl className="grid grid-cols-2 gap-2 border-t border-border pt-3 text-sm">
              <div>
                <dt className="text-xs text-muted-foreground">{t('performance.openingValue')}</dt>
                <dd className="font-medium">
                  {performance.openingValue != null
                    ? formatCurrency(performance.openingValue, performance.currency)
                    : '—'}
                </dd>
              </div>
              <div>
                <dt className="text-xs text-muted-foreground">{t('performance.endingValue')}</dt>
                <dd className="font-medium">
                  {performance.endingValue != null
                    ? formatCurrency(performance.endingValue, performance.currency)
                    : '—'}
                </dd>
              </div>
              <div>
                <dt className="text-xs text-muted-foreground">{t('performance.contributions')}</dt>
                <dd className="font-medium">
                  {performance.contributions != null
                    ? formatCurrency(performance.contributions, performance.currency)
                    : '—'}
                </dd>
              </div>
              <div>
                <dt className="text-xs text-muted-foreground">{t('performance.withdrawals')}</dt>
                <dd className="font-medium">
                  {performance.withdrawals != null
                    ? formatCurrency(performance.withdrawals, performance.currency)
                    : '—'}
                </dd>
              </div>
            </dl>

            {hasDifference && (
              <p className="flex items-start gap-2 text-xs text-amber-600 dark:text-amber-400">
                <AlertTriangle className="mt-0.5 size-3.5 shrink-0" aria-hidden="true" />
                {t('performance.reconciliationWarning', {
                  amount: formatCurrency(
                    performance.reconciliationDifference!,
                    performance.currency,
                  ),
                  defaultValue:
                    'El desglose no cuadra con los valores del periodo (diferencia {{amount}}). Revisa las operaciones del periodo.',
                })}
              </p>
            )}
          </div>
        )}
      </CardContent>
    </Card>
  );
});