import { useTranslation } from 'react-i18next';
import {
  AlertTriangle,
  ArrowRight,
  CheckCircle2,
  Info,
  XCircle,
} from 'lucide-react';

import { Badge } from '@/shared/components/core/badge';
import { Button } from '@/shared/components/core/button';
import { cn } from '@/lib/utils';

import { useHealth } from '../hooks/useInvestmentQueries';
import type { HealthIssueSeverity } from '../types';

interface HealthBannerProps {
  enabled: boolean;
  onOpenStatus: () => void;
}

const SEVERITY_ORDER: HealthIssueSeverity[] = [
  'error',
  'warning',
  'info',
];

const VISIBLE_ISSUES = 3;

export function HealthBanner({
  enabled,
  onOpenStatus,
}: HealthBannerProps) {
  const { t } = useTranslation('investments');
  const query = useHealth();
  const report = query.data;

  if (!enabled || query.isLoading) {
    return null;
  }

  if (query.isError) {
    return (
      <p className="text-xs text-muted-foreground">
        {t('health.unavailable')}
      </p>
    );
  }

  const issues = [...(report?.issues ?? [])].sort(
    (left, right) =>
      SEVERITY_ORDER.indexOf(left.severity) -
      SEVERITY_ORDER.indexOf(right.severity),
  );

  if (issues.length === 0) {
    return (
      <div className="flex items-center gap-2 rounded-lg border border-border bg-card px-4 py-3 text-sm text-muted-foreground">
        <CheckCircle2
          className="size-4 text-primary"
          aria-hidden="true"
        />
        {t('health.allGood')}
      </div>
    );
  }

  const hasError = issues.some(
    (issue) => issue.severity === 'error',
  );

  return (
    <section
      aria-label={t('health.title')}
      className={cn(
        'rounded-lg border p-4',
        hasError
          ? 'border-destructive/40 bg-destructive/5'
          : 'border-warning/40 bg-warning/10',
      )}
    >
      <div className="flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <AlertTriangle
            className={cn(
              'size-4',
              hasError ? 'text-destructive' : 'text-warning',
            )}
            aria-hidden="true"
          />
          <h3 className="text-sm font-medium text-foreground">
            {t('health.title')}
          </h3>
          <Badge variant={hasError ? 'destructive' : 'warning'}>
            {issues.length}
          </Badge>
        </div>

        <Button
          type="button"
          variant="ghost"
          size="sm"
          onClick={onOpenStatus}
          className="gap-1 text-xs"
        >
          {t('health.openStatus')}
          <ArrowRight className="size-3.5" aria-hidden="true" />
        </Button>
      </div>

      <ul className="mt-2 space-y-1.5">
        {issues.slice(0, VISIBLE_ISSUES).map((issue) => {
          const Icon =
            issue.severity === 'error'
              ? XCircle
              : issue.severity === 'warning'
                ? AlertTriangle
                : Info;

          return (
            <li
              key={`${issue.code}-${issue.resourceId ?? 'global'}`}
              className="flex items-start gap-2 text-sm"
            >
              <Icon
                className={cn(
                  'mt-0.5 size-3.5 shrink-0',
                  issue.severity === 'error' &&
                    'text-destructive',
                  issue.severity === 'warning' && 'text-warning',
                  issue.severity === 'info' &&
                    'text-muted-foreground',
                )}
                aria-hidden="true"
              />
              <span className="text-muted-foreground">
                {t(`health.issues.${issue.code}`, {
                  defaultValue: issue.message,
                })}
              </span>
            </li>
          );
        })}
      </ul>

      {issues.length > VISIBLE_ISSUES && (
        <p className="mt-2 text-xs text-muted-foreground">
          {t('health.moreIssues', {
            count: issues.length - VISIBLE_ISSUES,
          })}
        </p>
      )}
    </section>
  );
}
