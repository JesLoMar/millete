import { useTranslation } from 'react-i18next';
import { Badge } from '@/shared/components/core/badge';
import { cn } from '@/lib/utils';
import { formatCurrency } from '@/shared/utils/i18nFormat';

import type { CashBalances } from '../types';

interface CashBalancesCardProps {
  balances: CashBalances;
  localCurrency: string;
}

export function CashBalancesCard({
  balances,
  localCurrency,
}: CashBalancesCardProps) {
  const { t } = useTranslation('investments');

  const entries = Object.entries(balances).sort(
    ([leftCurrency, leftAmount], [rightCurrency, rightAmount]) => {
      if (leftCurrency === localCurrency) return -1;
      if (rightCurrency === localCurrency) return 1;

      const amountOrder = rightAmount - leftAmount;

      if (amountOrder !== 0) {
        return amountOrder;
      }

      return leftCurrency.localeCompare(rightCurrency);
    },
  );

  return (
    <section className="min-w-0 space-y-3 lg:border-l lg:border-border lg:pl-4">
      <h2 className="border-b border-border pb-2 text-lg font-semibold text-foreground">
        {t('portfolio.cashBalancesTitle')}
      </h2>
      {entries.length === 0 ? (
        <p className="py-2 text-sm text-muted-foreground">
          {t('portfolio.cashEmpty')}
        </p>
      ) : (
        <ul className="divide-y divide-border">
          {entries.map(([currency, amount]) => (
            <li
              key={currency}
              className="flex flex-wrap items-center justify-between gap-x-3 gap-y-1 py-2 first:pt-0 last:pb-0"
            >
              <div className="flex items-center gap-2">
                <Badge variant={currency === localCurrency ? 'default' : 'secondary'}>
                  {currency}
                </Badge>
                {currency === localCurrency && (
                  <span className="text-[10px] font-medium tracking-wide text-muted-foreground uppercase">
                    {t('portfolio.local')}
                  </span>
                )}
              </div>
              <span
                className={cn(
                  'ml-auto text-right text-sm font-semibold tabular-nums',
                  amount < 0 ? 'text-financial-negative' : 'text-foreground',
                )}
              >
                {formatCurrency(amount, currency)}
              </span>
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
