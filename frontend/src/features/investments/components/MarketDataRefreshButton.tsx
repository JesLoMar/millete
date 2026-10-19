import { memo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { RefreshCw, Loader2 } from 'lucide-react';
import { Button } from '@/shared/components/core/button';
import { investmentsService } from '../services/investments.service';
import { notify } from '@/shared/utils/notifications/notify';

export const MarketDataRefreshButton = memo(
  function MarketDataRefreshButton() {
    const { t } = useTranslation('investments');
    const [isRefreshing, setIsRefreshing] = useState(false);

    const handleRefresh = async () => {
      if (isRefreshing) return;
      setIsRefreshing(true);
      try {
        const to = new Date().toISOString();
        const from = new Date();
        from.setDate(from.getDate() - 7);

        await investmentsService.refreshMarketData(
          from.toISOString(),
          to,
          ['EUR', 'USD'],
        );
        notify.success(t('alerts.marketDataRefreshed', { count: 1 }));
      } catch {
        notify.error(t('alerts.marketDataRefreshError'));
      } finally {
        setIsRefreshing(false);
      }
    };

    return (
      <Button
        type="button"
        variant="outline"
        size="sm"
        onClick={handleRefresh}
        disabled={isRefreshing}
        className="gap-2"
      >
        {isRefreshing ? (
          <Loader2 className="size-4 animate-spin" aria-hidden="true" />
        ) : (
          <RefreshCw className="size-4" aria-hidden="true" />
        )}
        <span className="hidden sm:inline">
          {t('marketData.refresh')}
        </span>
      </Button>
    );
  },
);