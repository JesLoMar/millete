import { memo, type ReactNode } from 'react';
import { useTranslation } from 'react-i18next';
import { Wallet, TrendingUp, Banknote, type LucideIcon } from 'lucide-react';
import { Card, CardContent } from '@/shared/components/core/card';
import { Badge } from '@/shared/components/core/badge';
import { formatCurrency } from '@/shared/utils/i18nFormat';
import type { Portfolio } from '../types';

interface PortfolioSummaryProps {
  portfolio: Portfolio;
}

interface SummaryCardProps {
  icon: LucideIcon;
  label: string;
  value: string;
  badge?: ReactNode;
  hint?: string;
}

function SummaryCard({ icon: Icon, label, value, badge, hint }: SummaryCardProps) {
  return (
    <Card>
      <CardContent className="flex items-center gap-3 p-4">
        <div className="rounded-lg bg-primary/10 p-2.5">
          <Icon className="size-5 text-primary" aria-hidden="true" />
        </div>
        <div className="min-w-0 flex-1">
          <div className="flex items-center gap-2">
            <p className="text-xs text-muted-foreground">{label}</p>
            {badge}
          </div>
          <p className="truncate text-xl font-semibold text-foreground">{value}</p>
          {hint && <p className="truncate text-xs text-muted-foreground">{hint}</p>}
        </div>
      </CardContent>
    </Card>
  );
}

export const PortfolioSummary = memo(function PortfolioSummary({
  portfolio,
}: PortfolioSummaryProps) {
  const { t } = useTranslation('investments');
  const { localCurrency } = portfolio;

  const estimatedBadge = portfolio.estimated ? (
    <Badge variant="secondary" className="text-[11px]">
      {t('positions.flags.ESTIMATED', { defaultValue: 'Estimado' })}
    </Badge>
  ) : null;

  // Coste: solo posiciones con coste calculable.
  const withCost = portfolio.positions.filter((p) => p.localCostBasis != null);
  const invested = withCost.reduce((sum, p) => sum + (p.localCostBasis ?? 0), 0);
  const missingCost = portfolio.positions.length - withCost.length;

  // Efectivo: `cashBalances` es un mapa divisa -> importe.
  // Solo se suma la moneda local; las demás se muestran aparte sin convertir.
  const cashBalances = portfolio.cashBalances ?? {};
  const localCash = cashBalances[localCurrency] ?? 0;
  const otherCash = Object.entries(cashBalances).filter(
    ([currency]) => currency !== localCurrency,
  );

  return (
    <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
      <SummaryCard
        icon={Wallet}
        label={t('summary.totalValue')}
        value={formatCurrency(portfolio.totalInLocalCurrency, localCurrency)}
        badge={estimatedBadge}
        hint={
          portfolio.estimated
            ? t('summary.estimatedHint', { defaultValue: 'Incluye valoraciones estimadas' })
            : undefined
        }
      />

      <SummaryCard
        icon={TrendingUp}
        label={t('summary.investedCapital')}
        value={formatCurrency(invested, localCurrency)}
        hint={
          missingCost > 0
            ? t('summary.missingCostHint', {
                count: missingCost,
                defaultValue: '{{count}} posición(es) sin coste calculable',
              })
            : undefined
        }
      />

      <SummaryCard
        icon={Banknote}
        label={t('summary.cashBalance')}
        value={formatCurrency(localCash, localCurrency)}
        hint={
          otherCash.length > 0
            ? otherCash
                .map(([currency, amount]) => formatCurrency(amount, currency))
                .join(' · ')
            : undefined
        }
      />
    </div>
  );
});