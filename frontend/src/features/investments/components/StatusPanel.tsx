import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useQueryClient } from '@tanstack/react-query';
import {
  AlertTriangle,
  CalendarClock,
  Database,
  Download,
  Info,
  Loader2,
  RefreshCw,
  Upload,
  XCircle,
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
import { notify } from '@/shared/utils/notifications/notify';
import { getLocalCurrencyCode } from '@/shared/utils/i18nFormat';

import { INVESTMENT_QUERY_KEYS } from '../constants';
import {
  useAssetCatalog,
  useHealth,
  useTracking,
} from '../hooks/useInvestmentQueries';
import { investmentsService } from '../services/investments.service';
import type { HealthIssueSeverity } from '../types';
import { downloadJson, formatDateTime } from '../utils';
import { MarketDataRefreshDialog } from './MarketDataRefreshDialog';
import { PanelState } from './PanelState';
import { RestoreLedgerDialog } from './RestoreLedgerDialog';
import { TrackingSetupDialog } from './TrackingSetupDialog';

const SEVERITY_ORDER: HealthIssueSeverity[] = [
  'error',
  'warning',
  'info',
];

interface StatusPanelProps {
  onTrackingConfigured?: () => void;
}

export function StatusPanel({
  onTrackingConfigured,
}: StatusPanelProps) {
  const { t } = useTranslation('investments');
  const queryClient = useQueryClient();
  const catalog = useAssetCatalog();
  const trackingQuery = useTracking();
  const healthQuery = useHealth();

  const localCurrency = getLocalCurrencyCode();

  const [isTrackingOpen, setIsTrackingOpen] = useState(false);
  const [isMarketDataOpen, setIsMarketDataOpen] =
    useState(false);
  const [isRestoreOpen, setIsRestoreOpen] = useState(false);
  const [isDownloading, setIsDownloading] =
    useState(false);

  const tracking = trackingQuery.data;
  const isConfigured = tracking?.configured === true;

  const assetCurrencies = Array.from(
    new Set(
      catalog.entries
        .map((entry) => entry.currency)
        .filter((currency): currency is string =>
          Boolean(currency),
        ),
    ),
  );

  const issues = [...(healthQuery.data?.issues ?? [])].sort(
    (left, right) =>
      SEVERITY_ORDER.indexOf(left.severity) -
      SEVERITY_ORDER.indexOf(right.severity),
  );

  const handleDownload = async () => {
    setIsDownloading(true);

    try {
      const snapshot = await queryClient.fetchQuery({
        queryKey: INVESTMENT_QUERY_KEYS.ledger,
        queryFn: ({ signal }) =>
          investmentsService.getLedgerSnapshot(signal),
        staleTime: 0,
      });

      downloadJson(
        snapshot,
        `millete-inversiones-${new Date()
          .toISOString()
          .slice(0, 10)}.json`,
      );
      notify.success(t('ledger.downloadSuccess'));
    } catch {
      notify.error(t('ledger.downloadError'));
    } finally {
      setIsDownloading(false);
    }
  };

  return (
    <div className="space-y-4 sm:space-y-6">
      <Card>
        <CardHeader className="border-b border-border">
          <div className="flex items-center gap-2">
            <CalendarClock
              className="size-4 text-muted-foreground"
              aria-hidden="true"
            />
            <CardTitle>{t('tracking.title')}</CardTitle>
          </div>
          <p className="text-xs text-muted-foreground">
            {t('tracking.description')}
          </p>
        </CardHeader>

        <CardContent className="space-y-4 pt-4">
          <PanelState
            isLoading={trackingQuery.isLoading}
            isError={trackingQuery.isError}
            onRetry={() => void trackingQuery.refetch()}
            errorLabel={t('tracking.loadError')}
          />

          {!trackingQuery.isLoading && !trackingQuery.isError && (
            <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
              <div>
                <div className="flex items-center gap-2">
                  <Badge
                    variant={isConfigured ? 'default' : 'warning'}
                  >
                    {isConfigured
                      ? t('tracking.configured')
                      : t('tracking.notConfigured')}
                  </Badge>
                  {tracking?.trackingStartAt && (
                    <span className="text-sm text-foreground">
                      {formatDateTime(tracking.trackingStartAt)}
                    </span>
                  )}
                </div>
                <p className="mt-1 text-xs text-muted-foreground">
                  {isConfigured
                    ? t('tracking.immutableHint')
                    : t('tracking.notConfiguredHint')}
                </p>
              </div>

              {!isConfigured && (
                <Button
                  type="button"
                  onClick={() => setIsTrackingOpen(true)}
                >
                  {t('tracking.configure')}
                </Button>
              )}
            </div>
          )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="border-b border-border">
          <div className="flex flex-wrap items-center justify-between gap-2">
            <div className="flex items-center gap-2">
              <AlertTriangle
                className="size-4 text-muted-foreground"
                aria-hidden="true"
              />
              <CardTitle>{t('health.title')}</CardTitle>
              {issues.length > 0 && (
                <Badge variant="secondary">{issues.length}</Badge>
              )}
            </div>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => void healthQuery.refetch()}
              disabled={healthQuery.isFetching}
              className="gap-1.5"
            >
              <RefreshCw
                className={cn(
                  'size-4',
                  healthQuery.isFetching && 'animate-spin',
                )}
                aria-hidden="true"
              />
              {t('health.recheck')}
            </Button>
          </div>
          <p className="text-xs text-muted-foreground">
            {healthQuery.data
              ? t('health.checkedAt', {
                  date: formatDateTime(
                    healthQuery.data.checkedAt,
                  ),
                })
              : t('health.description')}
          </p>
        </CardHeader>

        <CardContent className="pt-4">
          <PanelState
            isLoading={healthQuery.isLoading}
            isError={healthQuery.isError}
            onRetry={() => void healthQuery.refetch()}
            errorLabel={t('health.loadError')}
          />

          {!healthQuery.isLoading &&
            !healthQuery.isError &&
            issues.length === 0 && (
              <p className="py-6 text-center text-sm text-muted-foreground">
                {t('health.allGood')}
              </p>
            )}

          {!healthQuery.isLoading &&
            !healthQuery.isError &&
            issues.length > 0 && (
              <ul className="divide-y divide-border">
                {issues.map((issue) => {
                  const Icon =
                    issue.severity === 'error'
                      ? XCircle
                      : issue.severity === 'warning'
                        ? AlertTriangle
                        : Info;

                  return (
                    <li
                      key={`${issue.code}-${issue.resourceId ?? 'global'}`}
                      className="flex items-start gap-3 py-2.5 first:pt-0 last:pb-0"
                    >
                      <Icon
                        className={cn(
                          'mt-0.5 size-4 shrink-0',
                          issue.severity === 'error' &&
                            'text-destructive',
                          issue.severity === 'warning' &&
                            'text-warning',
                          issue.severity === 'info' &&
                            'text-muted-foreground',
                        )}
                        aria-hidden="true"
                      />
                      <div className="min-w-0 flex-1">
                        <p className="text-sm font-medium text-foreground">
                          {t(`health.issues.${issue.code}`, {
                            defaultValue: issue.code,
                          })}
                        </p>
                        <p className="text-xs break-words text-muted-foreground">
                          {issue.message}
                        </p>
                        {issue.resourceId && (
                          <p className="mt-0.5 font-mono text-[10px] text-muted-foreground/70">
                            {issue.resourceId}
                          </p>
                        )}
                      </div>
                      <Badge variant="outline">
                        {t(
                          `health.severities.${issue.severity}`,
                          { defaultValue: issue.severity },
                        )}
                      </Badge>
                    </li>
                  );
                })}
              </ul>
            )}
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="border-b border-border">
          <div className="flex items-center gap-2">
            <RefreshCw
              className="size-4 text-muted-foreground"
              aria-hidden="true"
            />
            <CardTitle>{t('marketData.title')}</CardTitle>
          </div>
          <p className="text-xs text-muted-foreground">
            {t('marketData.description')}
          </p>
        </CardHeader>

        <CardContent className="flex flex-col justify-between gap-3 pt-4 sm:flex-row sm:items-center">
          <p className="text-xs text-muted-foreground">
            {t('marketData.statusHint')}
          </p>
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={() => setIsMarketDataOpen(true)}
            className="gap-1.5 self-start"
          >
            <RefreshCw className="size-4" aria-hidden="true" />
            {t('marketData.refresh')}
          </Button>
        </CardContent>
      </Card>

      <Card>
        <CardHeader className="border-b border-border">
          <div className="flex items-center gap-2">
            <Database
              className="size-4 text-muted-foreground"
              aria-hidden="true"
            />
            <CardTitle>{t('ledger.title')}</CardTitle>
          </div>
          <p className="text-xs text-muted-foreground">
            {t('ledger.description')}
          </p>
        </CardHeader>

        <CardContent className="space-y-4 pt-4">
          <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
            <p className="text-xs text-muted-foreground">
              {t('ledger.snapshotHint')}
            </p>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => void handleDownload()}
              disabled={isDownloading}
              className="gap-1.5 self-start"
            >
              {isDownloading ? (
                <Loader2
                  className="size-4 animate-spin"
                  aria-hidden="true"
                />
              ) : (
                <Download
                  className="size-4"
                  aria-hidden="true"
                />
              )}
              {t('ledger.download')}
            </Button>
          </div>

          <div className="flex flex-col justify-between gap-3 rounded-md border border-destructive/30 p-3 sm:flex-row sm:items-center">
            <div>
              <p className="text-sm font-medium text-foreground">
                {t('ledger.restore.actionTitle')}
              </p>
              <p className="text-xs text-muted-foreground">
                {t('ledger.restore.actionHint')}
              </p>
            </div>
            <Button
              type="button"
              variant="outline"
              size="sm"
              onClick={() => setIsRestoreOpen(true)}
              className="gap-1.5 self-start text-destructive hover:text-destructive"
            >
              <Upload className="size-4" aria-hidden="true" />
              {t('ledger.restore.submit')}
            </Button>
          </div>
        </CardContent>
      </Card>

      {isTrackingOpen && (
        <TrackingSetupDialog
          open
          onOpenChange={(open) => {
            if (!open) {
              setIsTrackingOpen(false);
            }
          }}
          onConfigured={() => onTrackingConfigured?.()}
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

      {isRestoreOpen && (
        <RestoreLedgerDialog
          onOpenChange={(open) => {
            if (!open) {
              setIsRestoreOpen(false);
            }
          }}
        />
      )}
    </div>
  );
}
