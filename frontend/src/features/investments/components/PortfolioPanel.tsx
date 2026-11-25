import { useState } from 'react';

import type { AssetCatalog } from '../hooks/useInvestmentQueries';
import type {
  AssetReference,
  CreatableActivityType,
  Portfolio,
} from '../types';
import { CashBalancesCard } from './CashBalancesCard';
import { CreateHoldingDialog } from './CreateHoldingDialog';
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
  const [openingAsset, setOpeningAsset] = useState<AssetReference | null>(null);

  return (
    <div className="grid items-start gap-4 lg:grid-cols-[minmax(0,2fr)_minmax(15rem,1fr)] xl:grid-cols-[minmax(0,2fr)_minmax(15rem,1fr)_minmax(11rem,0.6fr)]">
      <PositionsTable
        portfolio={portfolio}
        catalog={catalog}
        onTrade={onTrade}
        onRegisterPosition={setOpeningAsset}
      />

      <div className="min-w-0">
        <CashBalancesCard
          balances={portfolio.cashBalances}
          localCurrency={portfolio.localCurrency}
          onDeposit={() => onRecord('DEPOSIT')}
          onWithdraw={() => onRecord('WITHDRAW')}
          onExchange={() => onRecord('EXCHANGE')}
        />
      </div>

      <PortfolioSummary portfolio={portfolio} />
      {openingAsset && (
        <CreateHoldingDialog
          open
          initialAsset={openingAsset}
          onOpenChange={(open) => { if (!open) setOpeningAsset(null); }}
        />
      )}
    </div>
  );
}
