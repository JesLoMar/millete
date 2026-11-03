import { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Loader2, Plus, RefreshCw } from 'lucide-react';

import { Header } from '@/shared/components/Header';
import { Sidebar } from '@/shared/components/Sidebar';
import { TopNav } from '@/shared/components/TopNav';
import { Button } from '@/shared/components/core/button';
import {
  Card,
  CardContent,
} from '@/shared/components/core/card';
import { getLocalCurrencyCode } from '@/shared/utils/i18nFormat';

import type {
  AssetReference,
  CreatableActivityType,
} from '../types';
import type { InvestmentTab } from '../components/InvestmentTabs';
import { ActivityHistory } from '../components/ActivityHistory';
import { AssetsPanel } from '../components/AssetsPanel';
import { HealthBanner } from '../components/HealthBanner';
import { HoldingsPanel } from '../components/HoldingsPanel';
import { InvestmentTabs } from '../components/InvestmentTabs';
import { MarketDataRefreshDialog } from '../components/MarketDataRefreshDialog';
import { PanelState } from '../components/PanelState';
import { PerformancePanel } from '../components/PerformancePanel';
import { PortfolioPanel } from '../components/PortfolioPanel';
import { RecordActivityDialog } from '../components/RecordActivityDialog';
import { StatusPanel } from '../components/StatusPanel';
import { TrackingSetupDialog } from '../components/TrackingSetupDialog';
import {
  useAssetCatalog,
  usePortfolio,
  useTracking,
} from '../hooks/useInvestmentQueries';

interface RecordRequest {
  type: CreatableActivityType;
  asset: AssetReference | null;
}

const GATED_TABS: InvestmentTab[] = [
  'portfolio',
  'activity',
  'performance',
  'holdings',
];

export default function InvestmentsPage() {
  const { t } = useTranslation('investments');
  const [selectedTab, setSelectedTab] =
    useState<InvestmentTab | null>(null);
  const [recordRequest, setRecordRequest] =
    useState<RecordRequest | null>(null);
  const [isTrackingSetupOpen, setIsTrackingSetupOpen] =
    useState(false);
  const [isMarketDataOpen, setIsMarketDataOpen] =
    useState(false);

  // El instante de valoración se fija al montar la página para que las
  // peticiones no cambien de clave en cada render.
  const [asOf] = useState(() => new Date().toISOString());

  const trackingQuery = useTracking();
  const isConfigured =
    trackingQuery.data?.configured === true;

  const portfolioQuery = usePortfolio(asOf, isConfigured);
  const catalog = useAssetCatalog();

  const localCurrency = getLocalCurrencyCode();

  const assetCurrencies = useMemo(
    () =>
      Array.from(
        new Set(
          catalog.entries
            .map((entry) => entry.currency)
            .filter((currency): currency is string =>
              Boolean(currency),
            ),
        ),
      ),
    [catalog.entries],
  );

  const activeTab: InvestmentTab =
    selectedTab ?? (isConfigured ? 'portfolio' : 'assets');

  const showTrackingGate =
    !isConfigured && GATED_TABS.includes(activeTab);

  const handleOpenRecord = (
    type: CreatableActivityType,
    asset: AssetReference | null = null,
  ) => {
    setRecordRequest({ type, asset });
  };

  const renderGatedContent = () => {
    if (showTrackingGate) {
      return (
        <Card>
          <CardContent className="flex flex-col items-center gap-3 py-16 text-center">
            <h3 className="text-lg font-semibold text-foreground">
              {t('tracking.notConfiguredTitle')}
            </h3>
            <p className="max-w-md text-sm text-muted-foreground">
              {t('tracking.notConfiguredDescription')}
            </p>
            <Button
              type="button"
              onClick={() => setIsTrackingSetupOpen(true)}
            >
              {t('tracking.configure')}
            </Button>
          </CardContent>
        </Card>
      );
    }

    switch (activeTab) {
      case 'portfolio':
        return (
          <>
            <PanelState
              isLoading={portfolioQuery.isLoading}
              isError={portfolioQuery.isError}
              onRetry={() => void portfolioQuery.refetch()}
              errorLabel={t('portfolio.loadError')}
            />

            {!portfolioQuery.isLoading &&
              !portfolioQuery.isError &&
              portfolioQuery.data && (
                <PortfolioPanel
                  portfolio={portfolioQuery.data}
                  catalog={catalog}
                  onTrade={(type, asset) =>
                    handleOpenRecord(type, asset)
                  }
                  onRecord={(type) =>
                    handleOpenRecord(type)
                  }
                />
              )}
          </>
        );
      case 'activity':
        return <ActivityHistory />;
      case 'performance':
        return (
          <PerformancePanel
            trackingStartAt={
              trackingQuery.data?.trackingStartAt ?? null
            }
          />
        );
      case 'holdings':
        return <HoldingsPanel />;
      case 'assets':
        return (
          <AssetsPanel
            onOpenMarketDataRefresh={() =>
              setIsMarketDataOpen(true)
            }
          />
        );
      case 'status':
        return (
          <StatusPanel
            onTrackingConfigured={() =>
              setSelectedTab('portfolio')
            }
          />
        );
    }
  };

  return (
    <div className="flex min-h-dvh overflow-hidden bg-background">
      <Sidebar />
      <div className="flex flex-1 flex-col overflow-hidden pt-16">
        <TopNav />
        <main className="flex-1 space-y-4 overflow-y-auto p-4 sm:space-y-6 sm:p-6">
          <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center sm:gap-4">
            <Header hidePeriodSelector />

            <div className="flex flex-wrap items-center gap-2">
              <Button
                type="button"
                variant="outline"
                size="sm"
                onClick={() => setIsMarketDataOpen(true)}
                className="gap-1.5"
              >
                <RefreshCw
                  className="size-4"
                  aria-hidden="true"
                />
                {t('marketData.refresh')}
              </Button>
              <Button
                type="button"
                size="sm"
                onClick={() => handleOpenRecord('BUY')}
                disabled={!isConfigured}
                className="gap-1.5"
              >
                <Plus className="size-4" aria-hidden="true" />
                {t('activities.record')}
              </Button>
            </div>
          </div>

          <div className="space-y-1">
            <h2 className="text-xl font-semibold tracking-tight text-foreground">
              {t('title')}
            </h2>
            <p className="text-sm text-muted-foreground">
              {t('subtitle')}
            </p>
          </div>

          {trackingQuery.isLoading && (
            <div className="flex justify-center py-20">
              <Loader2
                className="size-8 animate-spin text-primary"
                aria-hidden="true"
              />
            </div>
          )}

          {!trackingQuery.isLoading && trackingQuery.isError && (
            <Card>
              <CardContent className="flex flex-col items-center gap-3 py-12 text-center">
                <p className="text-sm text-destructive">
                  {t('tracking.loadError')}
                </p>
                <Button
                  type="button"
                  variant="outline"
                  size="sm"
                  onClick={() => void trackingQuery.refetch()}
                >
                  {t('common:actions.retry')}
                </Button>
              </CardContent>
            </Card>
          )}

          {!trackingQuery.isLoading && !trackingQuery.isError && (
            <>
              <InvestmentTabs
                active={activeTab}
                onChange={setSelectedTab}
              />

              <HealthBanner
                enabled={isConfigured}
                onOpenStatus={() => setSelectedTab('status')}
              />

              {renderGatedContent()}
            </>
          )}
        </main>
      </div>

      {recordRequest && (
        <RecordActivityDialog
          key={`${recordRequest.type}:${
            recordRequest.asset
              ? `${recordRequest.asset.kind}:${recordRequest.asset.id}`
              : 'none'
          }`}
          open
          onOpenChange={(open) => {
            if (!open) {
              setRecordRequest(null);
            }
          }}
          initialType={recordRequest.type}
          initialAsset={recordRequest.asset}
        />
      )}

      {isTrackingSetupOpen && (
        <TrackingSetupDialog
          open
          onOpenChange={(open) => {
            if (!open) {
              setIsTrackingSetupOpen(false);
            }
          }}
          onConfigured={() => setSelectedTab('portfolio')}
        />
      )}

      {isMarketDataOpen && (
        <MarketDataRefreshDialog
          localCurrency={localCurrency}
          assetCurrencies={assetCurrencies}
          onOpenChange={(open) => {
            if (!open) {
              setIsMarketDataOpen(false);
            }
          }}
        />
      )}
    </div>
  );
}
