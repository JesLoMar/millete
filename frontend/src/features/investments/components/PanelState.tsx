import { useTranslation } from 'react-i18next';
import { AlertTriangle, Loader2 } from 'lucide-react';

import { Button } from '@/shared/components/core/button';

interface PanelStateProps {
  isLoading: boolean;
  isError: boolean;
  onRetry?: () => void;
  errorLabel: string;
}

/**
 * Estado de carga/error reutilizable para los paneles de inversiones.
 * Devuelve null cuando no hay nada que mostrar.
 */
export function PanelState({
  isLoading,
  isError,
  onRetry,
  errorLabel,
}: PanelStateProps) {
  const { t } = useTranslation('investments');

  if (isLoading) {
    return (
      <div
        className="flex items-center justify-center py-16"
        role="status"
        aria-live="polite"
      >
        <Loader2
          className="size-7 animate-spin text-muted-foreground"
          aria-hidden="true"
        />
        <span className="sr-only">{t('common.loading')}</span>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="flex flex-col items-center gap-3 px-6 py-12 text-center">
        <AlertTriangle
          className="size-7 text-destructive"
          aria-hidden="true"
        />
        <p className="text-sm text-destructive">{errorLabel}</p>
        {onRetry && (
          <Button
            type="button"
            variant="outline"
            size="sm"
            onClick={onRetry}
          >
            {t('common:actions.retry')}
          </Button>
        )}
      </div>
    );
  }

  return null;
}
