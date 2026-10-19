import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import { AlertTriangle, Info } from 'lucide-react';
import { cn } from '@/lib/utils';
import { useInvestmentHealth } from '../hooks/useInvestmentQueries';
import type { HealthIssueSeverity } from '../types';

interface HealthBannerProps {
  enabled: boolean;
}

const SEVERITY_ORDER: HealthIssueSeverity[] = ['error', 'warning', 'info'];

/**
 * Avisos de calidad de datos, presentados como cosas que el usuario puede revisar,
 * no como errores técnicos.
 */
export const HealthBanner = memo(function HealthBanner({ enabled }: HealthBannerProps) {
  const { t } = useTranslation('investments');
  const { data, isError } = useInvestmentHealth(enabled);

  if (!enabled) return null;

  if (isError) {
    return (
      <p className="text-xs text-muted-foreground">
        {t('health.unavailable', {
          defaultValue: 'No se pudo comprobar el estado de tus datos ahora mismo.',
        })}
      </p>
    );
  }

  const issues = [...(data?.issues ?? [])].sort(
    (a, b) => SEVERITY_ORDER.indexOf(a.severity) - SEVERITY_ORDER.indexOf(b.severity),
  );

  if (issues.length === 0) return null;

  const hasError = issues.some((issue) => issue.severity === 'error');

  return (
    <section
      aria-label={t('health.title', { defaultValue: 'Conviene revisar tus datos' })}
      className={cn(
        'rounded-lg border p-4',
        hasError
          ? 'border-destructive/40 bg-destructive/5'
          : 'border-amber-500/40 bg-amber-500/5',
      )}
    >
      <div className="mb-2 flex items-center gap-2">
        <AlertTriangle className="size-4 text-amber-500" aria-hidden="true" />
        <h3 className="text-sm font-medium text-foreground">
          {t('health.title', { defaultValue: 'Conviene revisar tus datos' })}
        </h3>
      </div>
      <ul className="space-y-1.5">
        {issues.map((issue) => {
          const Icon = issue.severity === 'info' ? Info : AlertTriangle;
          return (
            <li
              key={`${issue.code}-${issue.resourceId ?? 'global'}`}
              className="flex items-start gap-2 text-sm"
            >
              <Icon
                className={cn(
                  'mt-0.5 size-3.5 shrink-0',
                  issue.severity === 'error' && 'text-destructive',
                  issue.severity === 'warning' && 'text-amber-500',
                  issue.severity === 'info' && 'text-muted-foreground',
                )}
                aria-hidden="true"
              />
              <span className="text-muted-foreground">{issue.message}</span>
            </li>
          );
        })}
      </ul>
    </section>
  );
});