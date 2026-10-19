import { useCallback, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Plus, Loader2 } from 'lucide-react';
import { cn } from '@/lib/utils';
import { Sidebar } from '@/shared/components/Sidebar';
import { TopNav } from '@/shared/components/TopNav';
import { Header } from '@/shared/components/Header';
import { Button } from '@/shared/components/core/button';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/core/card';
import {
  useCurrentPortfolio,
  useActivities,
  useInvestmentTracking,
} from '../hooks/useInvestmentQueries';
import { PortfolioSummary } from '../components/PortfolioSummary';
import { HoldingsTable } from '../components/HoldingsTable';
import { ActivityHistory } from '../components/ActivityHistory';
import { PerformanceCard } from '../components/PerformanceCard';
import { RecordActivityDialog } from '../components/RecordActivityDialog';
import { MarketDataRefreshButton } from '../components/MarketDataRefreshButton';
import { EmptyPortfolioState } from '../components/EmptyPortfolioState';
import { TrackingSetupDialog } from '../components/TrackingSetupDialog';
import { HealthBanner } from '../components/HealthBanner';

type TabKey = 'positions' | 'activities';

export default function InvestmentsPage() {
  const { t } = useTranslation('investments');
  const [activeTab, setActiveTab] = useState<TabKey>('positions');
  const [isRecordOpen, setIsRecordOpen] = useState(false);
  const [isTrackingSetupOpen, setIsTrackingSetupOpen] = useState(false);

  const {
    data: tracking,
    isLoading: isLoadingTracking,
    isError: isTrackingError,
  } = useInvestmentTracking();

  const isTrackingConfigured = tracking?.configured === true;

  const {
    data: portfolio,
    isLoading: isLoadingPortfolio,
    isError: isPortfolioError,
  } = useCurrentPortfolio(isTrackingConfigured);

  const { data: activitiesPage, isLoading: isLoadingActivities } = useActivities(
    { page: 0, size: 50 },
    isTrackingConfigured,
  );

  const activities = activitiesPage?.content ?? [];
  const hasPositions = (portfolio?.positions.length ?? 0) > 0;
  const hasActivities = activities.length > 0;

  // Sin posiciones (p. ej. solo depósitos) la pestaña de posiciones no aporta nada:
  // se muestra el historial por defecto.
  const effectiveTab: TabKey =
    !hasPositions && activeTab === 'positions' ? 'activities' : activeTab;

  const marketCurrencies = useMemo(
    () =>
      portfolio
        ? Array.from(
            new Set([
              portfolio.localCurrency,
              ...portfolio.positions.map((p) => p.assetCurrency),
            ]),
          )
        : [],
    [portfolio],
  );

  const handleOpenRecord = useCallback(() => setIsRecordOpen(true), []);
  const handleOpenTrackingSetup = useCallback(() => setIsTrackingSetupOpen(true), []);

  // Estado vacío real: sin posiciones y sin operaciones.
  const showEmptyState =
    isTrackingConfigured &&
    !isLoadingPortfolio &&
    !isPortfolioError &&
    !hasPositions &&
    !hasActivities &&
    !isLoadingActivities;

  const showLedger =
    isTrackingConfigured && !isLoadingPortfolio && !isPortfolioError && portfolio != null;

  return (
    <div className="flex min-h-dvh overflow-hidden bg-background">
      <Sidebar />
      <div className="flex min-w-0 flex-1 flex-col">
        <TopNav />
        <main className="flex-1 overflow-y-auto p-4 md:p-6">
          <div className="mx-auto max-w-6xl space-y-6">
            <Header hidePeriodSelector onPeriodChange={undefined} />

            {isLoadingTracking && (
              <div className="flex justify-center py-20">
                <Loader2 className="size-8 animate-spin text-primary" aria-hidden="true" />
              </div>
            )}

            {!isLoadingTracking && isTrackingError && (
              <Card>
                <CardContent className="p-6 text-center">
                  <p className="text-sm text-muted-foreground">{t('common:status.error')}</p>
                </CardContent>
              </Card>
            )}

            {!isLoadingTracking && !isTrackingError && !isTrackingConfigured && (
              <div className="flex flex-col items-center justify-center rounded-lg border border-dashed border-border py-16 text-center">
                <h3 className="mb-1 text-lg font-semibold text-foreground">
                  {t('tracking.notConfiguredTitle')}
                </h3>
                <p className="mb-4 max-w-md text-sm text-muted-foreground">
                  {t('tracking.notConfiguredDescription')}
                </p>
                <Button type="button" onClick={handleOpenTrackingSetup}>
                  {t('tracking.configure')}
                </Button>
              </div>
            )}

            {!isLoadingTracking && isTrackingConfigured && (
              <>
                <div className="flex items-center justify-between gap-2">
                  <h2 className="text-lg font-semibold text-foreground">{t('title')}</h2>
                  <div className="flex items-center gap-2">
                    <MarketDataRefreshButton currencies={marketCurrencies} />
                    <Button type="button" size="sm" onClick={handleOpenRecord} className="gap-2">
                      <Plus className="size-4" aria-hidden="true" />
                      {t('activities.record')}
                    </Button>
                  </div>
                </div>

                <HealthBanner enabled={isTrackingConfigured} />

                {isPortfolioError && (
                  <Card>
                    <CardContent className="p-6 text-center">
                      <p className="text-sm text-muted-foreground">{t('common:status.error')}</p>
                    </CardContent>
                  </Card>
                )}

                {isLoadingPortfolio && (
                  <div className="flex justify-center py-20">
                    <Loader2 className="size-8 animate-spin text-primary" aria-hidden="true" />
                  </div>
                )}

                {showEmptyState && <EmptyPortfolioState onRecordActivity={handleOpenRecord} />}

                {showLedger && portfolio && (
                  <>
                    <PortfolioSummary portfolio={portfolio} />

                    <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
                      <div className="lg:col-span-2">
                        <Card>
                          <CardHeader className="pb-3">
                            <div className="flex items-center justify-between">
                              <CardTitle className="text-sm font-medium">
                                {effectiveTab === 'positions'
                                  ? t('positions.title')
                                  : t('activities.title')}
                              </CardTitle>
                              <div className="flex gap-1">
                                <button
                                  type="button"
                                  onClick={() => setActiveTab('positions')}
                                  className={cn(
                                    'rounded px-2 py-1 text-xs font-medium transition-colors',
                                    effectiveTab === 'positions'
                                      ? 'bg-primary text-primary-foreground'
                                      : 'text-muted-foreground hover:bg-accent/50',
                                  )}
                                >
                                  {t('positions.title')}
                                </button>
                                <button
                                  type="button"
                                  onClick={() => setActiveTab('activities')}
                                  className={cn(
                                    'rounded px-2 py-1 text-xs font-medium transition-colors',
                                    effectiveTab === 'activities'
                                      ? 'bg-primary text-primary-foreground'
                                      : 'text-muted-foreground hover:bg-accent/50',
                                  )}
                                >
                                  {t('activities.title')}
                                </button>
                              </div>
                            </div>
                          </CardHeader>
                          <CardContent>
                            {effectiveTab === 'positions' ? (
                              <HoldingsTable portfolio={portfolio} />
                            ) : isLoadingActivities ? (
                              <div className="flex justify-center py-10">
                                <Loader2 className="size-6 animate-spin text-primary" aria-hidden="true" />
                              </div>
                            ) : (
                              <ActivityHistory activities={activities} />
                            )}
                          </CardContent>
                        </Card>
                      </div>

                      <div>
                        <PerformanceCard />
                      </div>
                    </div>
                  </>
                )}
              </>
            )}
          </div>
        </main>
      </div>

      <RecordActivityDialog open={isRecordOpen} onOpenChange={setIsRecordOpen} />
      <TrackingSetupDialog open={isTrackingSetupOpen} onOpenChange={setIsTrackingSetupOpen} />
    </div>
  );
}