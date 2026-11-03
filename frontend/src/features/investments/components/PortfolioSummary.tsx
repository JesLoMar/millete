import { useTranslation } from 'react-i18next';
import {
  Coins,
  PieChart,
  TrendingDown,
  TrendingUp,
  Wallet,
} from 'lucide-react';
import type { LucideIcon } from 'lucide-react';

import { Badge } from '@/shared/components/core/badge';
import {
  Card,
  CardContent,
} from '@/shared/components/core/card';
import { cn } from '@/lib/utils';
import { formatCurrency } from '@/shared/utils/i18nFormat';

import type { Portfolio } from '../types';
import {
  formatPercent,
  sumNullable,
} from '../utils';

interface PortfolioSummaryProps {
  portfolio: Portfolio;
}

function SummaryCard({
  title,
  value,
  hint,
  icon: Icon,
  iconClassName,
  valueClassName,
}: {
  title: string;
  value: string;
  hint?: string;
  icon: LucideIcon;
  iconClassName: string;
  valueClassName?: string;
}) {
  return (
    <Card className="transition-all duration-300 hover:-translate-y-0.5 hover:border-primary/40 hover:shadow-md">
      <CardContent className="flex h-full flex-col justify-between gap-3 pt-6">
        <div className="flex items-center justify-between gap-2">
          <p className="text-xs font-semibold tracking-wider text-muted-foreground uppercase">
            {title}
          </p>
          <div
            className={cn(
              'flex size-9 shrink-0 items-center justify-center rounded-lg',
              iconClassName,
            )}
          >
            <Icon className="size-4" aria-hidden="true" />
          </div>
        </div>

        <div className="min-w-0">
          <p
            className={cn(
              'truncate text-xl font-semibold tracking-tight tabular-nums sm:text-2xl',
              valueClassName,
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
      </CardContent>
    </Card>
  );
}

export function PortfolioSummary({
  portfolio,
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
    <div className="space-y-3">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <h2 className="text-lg font-semibold text-foreground">
          {t('portfolio.summary')}
        </h2>
        {portfolio.estimated && (
          <Badge variant="warning">
            {t('portfolio.estimated')}
          </Badge>
        )}
      </div>

      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 xl:grid-cols-4">
        <SummaryCard
          title={t('portfolio.totalValue')}
          value={formatCurrency(
            portfolio.totalInLocalCurrency,
            portfolio.localCurrency,
          )}
          hint={t('portfolio.localCurrencyHint', {
            currency: portfolio.localCurrency,
          })}
          icon={Wallet}
          iconClassName="bg-primary/10 text-primary"
        />

        <SummaryCard
          title={t('portfolio.invested')}
          value={formatCurrency(invested, portfolio.localCurrency)}
          hint={t('portfolio.costBasisHint')}
          icon={PieChart}
          iconClassName="bg-accent text-accent-foreground"
        />

        <SummaryCard
          title={t('portfolio.marketValue')}
          value={formatCurrency(
            marketValue,
            portfolio.localCurrency,
          )}
          hint={t('portfolio.marketValueHint')}
          icon={Coins}
          iconClassName="bg-secondary text-secondary-foreground"
        />

        <SummaryCard
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
          icon={isPositive ? TrendingUp : TrendingDown}
          iconClassName={
            isPositive
              ? 'bg-primary/10 text-primary'
              : 'bg-destructive/10 text-destructive'
          }
          valueClassName={
            isPositive ? 'text-primary' : 'text-destructive'
          }
        />
      </div>
    </div>
  );
}
