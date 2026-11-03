import { useTranslation } from 'react-i18next';

import type { AssetCatalog } from '../hooks/useInvestmentQueries';
import type {
  AssetReference,
  CreatableActivityType,
  Portfolio,
} from '../types';
import { CashBalancesCard } from './CashBalancesCard';
import { PortfolioSummary } from './PortfolioSummary';
import { PositionsTable } from './PositionsTable';

interface PortfolioPanelProps {
  portfolio: Portfolio;
  catalog: AssetCatalog;
  onTrade: (
    type: 'BUY' | 'SELL',
    asset: AssetReference,
  ) => void;
  onRecord: (type: CreatableActivityType) => void;
}

export function PortfolioPanel({
  portfolio,
  catalog,
  onTrade,
  onRecord,
}: PortfolioPanelProps) {
  const { t } = useTranslation('investments');

  return (
    <div className="space-y-4 sm:space-y-6">
      <PortfolioSummary portfolio={portfolio} />

      <div className="grid grid-cols-1 gap-4 xl:grid-cols-3">
        <div className="min-w-0 xl:col-span-2">
          <PositionsTable
            portfolio={portfolio}
            catalog={catalog}
            onTrade={onTrade}
          />
        </div>

        <div className="min-w-0">
          <CashBalancesCard
            balances={portfolio.cashBalances}
            localCurrency={portfolio.localCurrency}
            onDeposit={() => onRecord('DEPOSIT')}
            onWithdraw={() => onRecord('WITHDRAW')}
            onExchange={() => onRecord('EXCHANGE')}
          />
        </div>
      </div>

      <p className="text-xs text-muted-foreground">
        {t('portfolio.disclaimer')}
      </p>
    </div>
  );
}
