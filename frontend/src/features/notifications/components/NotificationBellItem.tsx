import { memo, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Bell, Users, X } from 'lucide-react';
import { Button } from '@/shared/components/core/button';
import { cn } from '@/lib/utils';
import type { Notification } from '../types';

interface NotificationBellItemProps {
  notification: Notification;
  onMarkAsRead: (id: string) => void;
  onNavigate: () => void;
}

export const NotificationBellItem = memo(function NotificationBellItem({ notification, onMarkAsRead, onNavigate }: NotificationBellItemProps) {
  const { t } = useTranslation(['notifications', 'common']);
  const navigate = useNavigate();

  const getIcon = () => {
    switch (notification.type) {
      case 'GOAL_INVITATION':
        return Users;
      default:
        return Bell;
    }
  };

  const Icon = getIcon();

  const handleClick = useCallback(() => {
    onNavigate();
    if (notification.type === 'GOAL_INVITATION') {
      navigate('/profile');
    } else {
      navigate('/notifications');
    }
  }, [notification.type, navigate, onNavigate]);

  const handleDismiss = useCallback((e: React.MouseEvent) => {
    e.stopPropagation();
    onMarkAsRead(notification.id);
  }, [notification.id, onMarkAsRead]);

  return (
    <button
      type="button"
      className={cn(
        'flex items-center gap-3 p-3 rounded-lg border cursor-pointer transition-colors hover:bg-accent/30 text-left w-full',
        'bg-accent/10 border-accent/40'
      )}
      onClick={handleClick}
    >
      <div className="shrink-0">
        <Icon className="h-5 w-5 text-primary" />
      </div>

      <div className="flex-1 min-w-0">
        <p className="font-medium text-sm truncate">{notification.title}</p>
        <p className="text-xs text-muted-foreground truncate">{notification.message}</p>
      </div>

      <Button
        variant="ghost"
        size="icon"
        className="h-7 w-7 shrink-0 text-muted-foreground hover:text-destructive"
        onClick={handleDismiss}
        title={t('common:actions.close')}
      >
        <X className="h-4 w-4" />
      </Button>
    </button>
  );
});
