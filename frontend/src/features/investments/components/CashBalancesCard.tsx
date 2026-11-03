import { useTranslation } from 'react-i18next';
import {
  ArrowLeftRight,
  ArrowDownToLine,
  ArrowUpFromLine,
  Coins,
  Plus,
} from 'lucide-react';

import { Badge } from '@/shared/components/core/badge';
import { Button } from '@/shared/components/core/button';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/core/card';
import { cn } from '@/lib/utils';
import { formatCurrency } from '@/shared/utils/i18nFormat';

import type { CashBalances } from '../types';

interface CashBalancesCardProps {
  balances: CashBalances;
  localCurrency: string;
  onDeposit: () => void;
  onWithdraw: () => void;
  onExchange: () => void;
}

export function CashBalancesCard({
  balances,
  localCurrency,
  onDeposit,
  onWithdraw,
  onExchange,
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
    <Card className="flex h-full flex-col">
      <CardHeader className="border-b border-border">
        <div className="flex items-center justify-between gap-2">
          <CardTitle>{t('portfolio.cashTitle')}</CardTitle>
          <div className="flex size-8 items-center justify-center rounded-lg bg-primary/10 text-primary">
            <Coins className="size-4" aria-hidden="true" />
          </div>
        </div>
        <p className="text-xs text-muted-foreground">
          {t('portfolio.cashDescription')}
        </p>
      </CardHeader>

      <CardContent className="flex flex-1 flex-col justify-between gap-4 pt-4">
        {entries.length === 0 ? (
          <p className="py-6 text-center text-sm text-muted-foreground">
            {t('portfolio.cashEmpty')}
          </p>
        ) : (
          <ul className="divide-y divide-border">
            {entries.map(([currency, amount]) => (
              <li
                key={currency}
                className="flex items-center justify-between gap-3 py-2 first:pt-0 last:pb-0"
              >
                <div className="flex items-center gap-2">
                  <Badge
                    variant={
                      currency === localCurrency
                        ? 'default'
                        : 'secondary'
                    }
                  >
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
                    'text-sm font-semibold tabular-nums',
                    amount < 0
                      ? 'text-destructive'
                      : 'text-foreground',
                  )}
                >
                  {formatCurrency(amount, currency)}
                </span>
              </li>
            ))}
          </ul>
        )}

        <div className="grid grid-cols-1 gap-2">
          <Button
            type="button"
            variant="outline"
            size="sm"
            className="justify-start"
            onClick={onDeposit}
          >
            <Plus className="size-4 text-primary" aria-hidden="true" />
            {t('portfolio.deposit')}
          </Button>
          <Button
            type="button"
            variant="outline"
            size="sm"
            className="justify-start"
            onClick={onWithdraw}
          >
            <ArrowUpFromLine
              className="size-4 text-destructive"
              aria-hidden="true"
            />
            {t('portfolio.withdraw')}
          </Button>
          <Button
            type="button"
            variant="outline"
            size="sm"
            className="justify-start"
            onClick={onExchange}
          >
            <ArrowLeftRight
              className="size-4 text-muted-foreground"
              aria-hidden="true"
            />
            {t('portfolio.exchange')}
          </Button>
        </div>

        <div className="flex items-center justify-between border-t border-border pt-3">
          <span className="text-xs text-muted-foreground">
            {t('portfolio.cashMovements')}
          </span>
          <Button
            type="button"
            variant="ghost"
            size="xs"
            onClick={onDeposit}
          >
            <ArrowDownToLine
              className="size-3.5"
              aria-hidden="true"
            />
            {t('portfolio.recordMovement')}
          </Button>
        </div>
      </CardContent>
    </Card>
  );
}
