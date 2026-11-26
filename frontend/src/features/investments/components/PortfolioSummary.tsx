import { useTranslation } from 'react-i18next';
import { ArrowLeftRight, ArrowUpFromLine, Plus } from 'lucide-react';
import { Badge } from '@/shared/components/core/badge';
import { Button } from '@/shared/components/core/button';
import { cn } from '@/lib/utils';
import { formatCurrency } from '@/shared/utils/i18nFormat';

import type { Portfolio } from '../types';
import {
  formatPercent,
  sumNullable,
} from '../utils';

interface PortfolioSummaryProps {
  portfolio: Portfolio;
  onRecord: (type: 'DEPOSIT' | 'WITHDRAW' | 'EXCHANGE') => void;
}

const CASH_ACTIONS = [
  {
    type: 'DEPOSIT',
    label: 'portfolio.deposit',
    Icon: Plus,
    iconClass: 'text-primary',
  },
  {
    type: 'WITHDRAW',
    label: 'portfolio.withdraw',
    Icon: ArrowUpFromLine,
    iconClass: 'text-destructive',
  },
  {
    type: 'EXCHANGE',
    label: 'portfolio.exchange',
    Icon: ArrowLeftRight,
    iconClass: 'text-muted-foreground',
  },
] as const;

function SummaryMetric({
  title,
  value,
  hint,
  valueClassName,
}: {
  title: string;
  value: string;
  hint?: string;
  valueClassName?: string;
}) {
  return (
    <div className="min-w-0">
      <dt className="text-xs text-muted-foreground">{title}</dt>
      <dd className={cn('break-words text-lg font-semibold tracking-tight tabular-nums', valueClassName)}>
        {value}
      </dd>
      {hint && <dd className="text-xs text-muted-foreground">{hint}</dd>}
    </div>
  );
}

export function PortfolioSummary({
  portfolio,
  onRecord,
}: PortfolioSummaryProps) {
  const { t } = useTranslation('investments');

  const invested = sumNullable(
    portfolio.positions.map((position) => position.costBasis),
  );
  const marketValue = sumNullable(
    portfolio.positions.map(
      (position) => position.marketValue,
    ),
  );
  const unrealized = sumNullable(
    portfolio.positions.map(
      (position) => position.unrealizedGain,
    ),
  );
  const gainPercentage =
    invested > 0 ? (unrealized / invested) * 100 : null;
  const isPositive = unrealized >= 0;

  return (
    <aside className="min-w-0 space-y-3 lg:max-xl:col-span-2 xl:border-l xl:border-border xl:pl-4">
      <div className="flex flex-wrap items-center justify-between gap-2 border-b border-border pb-2">
        <h2 className="text-lg font-semibold text-foreground">
          {t('portfolio.summary')}
        </h2>
        {portfolio.estimated && (
          <Badge variant="warning">
            {t('portfolio.estimated')}
          </Badge>
        )}
      </div>

      <dl className="grid grid-cols-2 gap-x-3 gap-y-4 sm:grid-cols-4 xl:grid-cols-1">
        <SummaryMetric
          title={t('portfolio.totalValue')}
          value={formatCurrency(
            portfolio.totalInLocalCurrency,
            portfolio.localCurrency,
          )}
          hint={t('portfolio.localCurrencyHint', {
            currency: portfolio.localCurrency,
          })}
        />

        <SummaryMetric
          title={t('portfolio.invested')}
          value={formatCurrency(invested, portfolio.localCurrency)}
          hint={t('portfolio.costBasisHint')}
        />

        <SummaryMetric
          title={t('portfolio.marketValue')}
          value={formatCurrency(
            marketValue,
            portfolio.localCurrency,
          )}
          hint={t('portfolio.marketValueHint')}
        />

        <SummaryMetric
          title={t('portfolio.unrealizedGain')}
          value={formatCurrency(
            unrealized,
            portfolio.localCurrency,
          )}
          hint={
            gainPercentage != null
              ? formatPercent(gainPercentage)
              : t('portfolio.noCostBasis')
          }
          valueClassName={
            isPositive ? 'text-primary' : 'text-destructive'
          }
        />
      </dl>
      <p className="text-xs text-muted-foreground">{t('portfolio.disclaimer')}</p>
      <section className="space-y-2 border-t border-border pt-3">
        <h2 className="text-lg font-semibold text-foreground">
          {t('portfolio.cashTitle')}
        </h2>
        <p className="text-xs text-muted-foreground">
          {t('portfolio.cashDescription')}
        </p>
        <div className="flex flex-wrap gap-1 xl:flex-col">
          {CASH_ACTIONS.map(({ type, label, Icon, iconClass }) => (
            <Button
              key={type}
              type="button"
              variant="ghost"
              className="min-h-10 justify-start px-2"
              onClick={() => onRecord(type)}
            >
              <Icon className={cn('size-4', iconClass)} aria-hidden="true" />
              {t(label)}
            </Button>
          ))}
        </div>
      </section>
    </aside>
  );
}
