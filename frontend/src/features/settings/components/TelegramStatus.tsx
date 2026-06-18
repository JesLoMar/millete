import { useTranslation } from 'react-i18next';
import {
  BotMessageSquare,
  RefreshCw,
  CheckCircle2,
  XCircle,
  AlertCircle,
  Loader2,
} from 'lucide-react';
import { Button } from '@/shared/components/core/button';
import { cn } from '@/lib/utils';
import { SettingsSection } from './SettingsSection';
import { useTelegramStatus } from '../hooks/useTelegramStatus';
import type { TelegramConnectionStatus } from '../types';

interface StatusIndicatorProps {
  status: TelegramConnectionStatus;
}

function StatusIndicator({ status }: StatusIndicatorProps) {
  switch (status) {
    case 'connected':
      return <CheckCircle2 className="h-4 w-4 text-green-500" />;
    case 'disconnected':
      return <XCircle className="h-4 w-4 text-red-500" />;
    case 'loading':
      return <Loader2 className="h-4 w-4 animate-spin text-yellow-500" />;
    case 'error':
      return <AlertCircle className="h-4 w-4 text-destructive" />;
  }
}

export function TelegramStatus() {
  const { t } = useTranslation();
  const { status, username, connectedAt, refetch, isRefetching } = useTelegramStatus();

  return (
    <SettingsSection
      icon={BotMessageSquare}
      title={t('settings:telegram.title')}
      description={t('settings:telegram.description')}
    >
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <StatusIndicator status={status} />
          <span className="text-sm">
            {status === 'connected'
              ? t('settings:telegram.connected', { username: username ?? 'N/A' })
              : t(`settings:telegram.status.${status}`)}
          </span>
          {connectedAt && status === 'connected' && (
            <span className="text-xs text-muted-foreground">
              {t('settings:telegram.since', { date: new Date(connectedAt).toLocaleDateString() })}
            </span>
          )}
        </div>
        <Button
          variant="outline"
          size="sm"
          onClick={() => refetch()}
          disabled={isRefetching}
        >
          <RefreshCw className={cn('h-4 w-4 mr-2', isRefetching && 'animate-spin')} />
          {t('settings:telegram.refresh')}
        </Button>
      </div>
    </SettingsSection>
  );
}
