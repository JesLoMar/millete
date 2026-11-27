import { useState } from 'react';
import type { ReactNode } from 'react';
import { useTranslation } from 'react-i18next';
import { useQueryClient } from '@tanstack/react-query';
import {
  AlertTriangle,
  CalendarClock,
  CircleCheck,
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
import { cn } from '@/lib/utils';
import { notify } from '@/shared/utils/notifications/notify';

import { INVESTMENT_QUERY_KEYS } from '../constants';
import { useHealth, useTracking } from '../hooks/useInvestmentQueries';
import { investmentsService } from '../services/investments.service';
import type { HealthIssueSeverity } from '../types';
import { downloadJson, formatDateTime } from '../utils';
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
  marketDataAction: ReactNode;
}

export function StatusPanel({
  onTrackingConfigured,
  marketDataAction,
}: StatusPanelProps) {
  const { t } = useTranslation('investments');
  const queryClient = useQueryClient();
  const trackingQuery = useTracking();
  const healthQuery = useHealth();

  const [isTrackingOpen, setIsTrackingOpen] = useState(false);
  const [isRestoreOpen, setIsRestoreOpen] = useState(false);
  const [isDownloading, setIsDownloading] =
    useState(false);

  const tracking = trackingQuery.data;
  const isConfigured = tracking?.configured === true;

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

  const trackingSection = (
    <section aria-labelledby="investment-tracking-title" className="space-y-3">
      <header className="space-y-1 border-b border-border pb-2">
        <h2 id="investment-tracking-title" className="flex items-center gap-2 text-base font-semibold">
          <CalendarClock className="size-4 text-muted-foreground" aria-hidden="true" />
          {t('tracking.title')}
        </h2>
        <p className="text-xs text-muted-foreground">{t('tracking.description')}</p>
      </header>

      <PanelState
        isLoading={trackingQuery.isLoading}
        isError={trackingQuery.isError}
        onRetry={() => void trackingQuery.refetch()}
        errorLabel={t('tracking.loadError')}
      />

      {!trackingQuery.isLoading && !trackingQuery.isError && (
        <div className="flex flex-wrap items-start justify-between gap-3">
          <div className="space-y-1">
            <div className="flex flex-wrap items-center gap-2">
              <Badge variant={isConfigured ? 'default' : 'warning'}>
                {isConfigured ? t('tracking.configured') : t('tracking.notConfigured')}
              </Badge>
              {tracking?.trackingStartAt && (
                <span className="text-sm">{formatDateTime(tracking.trackingStartAt)}</span>
              )}
            </div>
            <p className="text-xs text-muted-foreground">
              {isConfigured ? t('tracking.immutableHint') : t('tracking.notConfiguredHint')}
            </p>
          </div>
          {!isConfigured && (
            <Button type="button" size="sm" onClick={() => setIsTrackingOpen(true)}>
              {t('tracking.configure')}
            </Button>
          )}
        </div>
      )}
    </section>
  );

  return (
    <div className="grid gap-6 xl:grid-cols-[minmax(0,2fr)_minmax(0,1fr)]">
      <div className="min-w-0 space-y-6">
        <section aria-labelledby="investment-health-title" className="space-y-3">
          <header className="space-y-1 border-b border-border pb-2">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <div className="flex items-center gap-2">
                <AlertTriangle
                  className="size-4 text-muted-foreground"
                  aria-hidden="true"
                />
                <h2 id="investment-health-title" className="text-base font-semibold">
                  {t('health.title')}
                </h2>
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
                  className={cn('size-4', healthQuery.isFetching && 'animate-spin')}
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
          </header>

          <div>
            <PanelState
              isLoading={healthQuery.isLoading}
              isError={healthQuery.isError}
              onRetry={() => void healthQuery.refetch()}
              errorLabel={t('health.loadError')}
            />

            {!healthQuery.isLoading &&
              !healthQuery.isError &&
              issues.length === 0 && (
                <p className="flex items-center gap-2 text-sm text-financial-positive">
                  <CircleCheck className="size-4 shrink-0" aria-hidden="true" />
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
          </div>
        </section>
        {trackingSection}
      </div>

      <div className="min-w-0 space-y-6 xl:border-l xl:border-border xl:pl-4">
        <section aria-labelledby="investment-market-title" className="space-y-3">
          <header className="space-y-1 border-b border-border pb-2">
            <div className="flex items-center gap-2">
              <RefreshCw
                className="size-4 text-muted-foreground"
                aria-hidden="true"
              />
              <h2 id="investment-market-title" className="text-base font-semibold">
                {t('marketData.title')}
              </h2>
            </div>
            <p className="text-xs text-muted-foreground">
              {t('marketData.description')}
            </p>
          </header>

          <p className="text-xs text-muted-foreground">
            {t('marketData.statusHint')}
          </p>
          {marketDataAction}
        </section>

        <section aria-labelledby="investment-ledger-title" className="space-y-3">
          <header className="space-y-1 border-b border-border pb-2">
            <div className="flex items-center gap-2">
              <Database
                className="size-4 text-muted-foreground"
                aria-hidden="true"
              />
              <h2 id="investment-ledger-title" className="text-base font-semibold">
                {t('ledger.title')}
              </h2>
            </div>
            <p className="text-xs text-muted-foreground">
              {t('ledger.description')}
            </p>
          </header>

          <div className="space-y-3">
            <div className="space-y-2">
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

            <div className="space-y-2 border-t border-border pt-3">
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
          </div>
        </section>
      </div>

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
