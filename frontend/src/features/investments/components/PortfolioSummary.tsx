import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import { Wallet, TrendingUp, Banknote } from 'lucide-react';
import { Card, CardContent } from '@/shared/components/core/card';
import { formatCurrency } from '@/shared/utils/i18nFormat';
import type { Portfolio } from '../types';

interface PortfolioSummaryProps {
  portfolio: Portfolio;
}

export const PortfolioSummary = memo(function PortfolioSummary({
  portfolio,
}: PortfolioSummaryProps) {
  const { t } = useTranslation('investments');

  const totalCash = portfolio.cashBalances.reduce(
    (sum, balance) => sum + balance.amount,
    0,
  );

  const totalInvested = portfolio.positions.reduce(
    (sum, position) => sum + (position.localCostBasis ?? 0),
    0,
  );

  return (
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
      <Card>
        <CardContent className="flex items-center gap-3 p-4">
          <div className="rounded-lg bg-primary/10 p-2.5">
            <Wallet className="size-5 text-primary" aria-hidden="true" />
          </div>
          <div className="min-w-0 flex-1">
            <p className="text-xs text-muted-foreground">
              {t('summary.totalValue')}
            </p>
            <p className="truncate text-xl font-semibold text-foreground">
              {formatCurrency(
                portfolio.totalInLocalCurrency,
                portfolio.localCurrency,
              )}
            </p>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent className="flex items-center gap-3 p-4">
          <div className="rounded-lg bg-primary/10 p-2.5">
            <TrendingUp className="size-5 text-primary" aria-hidden="true" />
          </div>
          <div className="min-w-0 flex-1">
            <p className="text-xs text-muted-foreground">
              {t('summary.investedCapital')}
            </p>
            <p className="truncate text-xl font-semibold text-foreground">
              {formatCurrency(totalInvested, portfolio.localCurrency)}
            </p>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent className="flex items-center gap-3 p-4">
          <div className="rounded-lg bg-primary/10 p-2.5">
            <Banknote className="size-5 text-primary" aria-hidden="true" />
          </div>
          <div className="min-w-0 flex-1">
            <p className="text-xs text-muted-foreground">
              {t('summary.cashBalance')}
            </p>
            <p className="truncate text-xl font-semibold text-foreground">
              {formatCurrency(totalCash, portfolio.localCurrency)}
            </p>
          </div>
        </CardContent>
      </Card>
    </div>
  );
});