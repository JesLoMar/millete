import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import { RefreshCw, Loader2 } from 'lucide-react';
import { Button } from '@/shared/components/core/button';
import { useInvestmentMutations } from '../hooks/useInvestmentMutations';
import { formatActivityDateTime } from '../utils';

interface MarketDataRefreshButtonProps {
  /** Divisas de la cartera (moneda local + monedas de las posiciones). */
  currencies: string[];
}

/**
 * La market data es infraestructura: se muestra solo la última actualización
 * y un botón secundario. No compite con los valores de la cartera.
 */
export const MarketDataRefreshButton = memo(function MarketDataRefreshButton({
  currencies,
}: MarketDataRefreshButtonProps) {
  const { t } = useTranslation('investments');
  const { refreshMarketData, isRefreshingMarketData } = useInvestmentMutations();

  // Última actualización conocida en esta sesión. El backend no expone aún
  // una marca de la última actualización global de precios.
  const lastRefreshedAt = refreshMarketData.data?.refreshedAt ?? null;

  return (
    <div className="flex items-center gap-2">
      {lastRefreshedAt && (
        <span className="hidden text-xs text-muted-foreground md:inline">
          {t('marketData.lastUpdated', {
            date: formatActivityDateTime(lastRefreshedAt),
            defaultValue: 'Precios: {{date}}',
          })}
        </span>
      )}
      <Button
        type="button"
        variant="ghost"
        size="sm"
        onClick={() => refreshMarketData.mutate({ currencies })}
        disabled={isRefreshingMarketData || currencies.length === 0}
        className="gap-2 text-muted-foreground"
      >
        {isRefreshingMarketData ? (
          <Loader2 className="size-4 animate-spin" aria-hidden="true" />
        ) : (
          <RefreshCw className="size-4" aria-hidden="true" />
        )}
        <span className="hidden sm:inline">
          {t('marketData.refresh', { defaultValue: 'Actualizar precios' })}
        </span>
      </Button>
    </div>
  );
});