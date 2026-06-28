import { Bell, Check, Trash2, Users } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Button } from '@/shared/components/core/button';
import { cn } from '@/lib/utils';
import type { Notification } from '../types';

interface NotificationItemProps {
  notification: Notification;
  onMarkAsRead: (id: string) => void;
  onDelete: (id: string) => void;
}

export function NotificationItem({ notification, onMarkAsRead, onDelete }: NotificationItemProps) {
  const { t } = useTranslation('notifications');

  const getIcon = () => {
    switch (notification.type) {
      case 'GOAL_INVITATION':
        return Users;
      default:
        return Bell;
    }
  };

  const Icon = getIcon();

  return (
    <div
      className={cn(
        'flex items-start gap-3 p-4 rounded-lg border transition-colors',
        notification.read ? 'bg-card/50' : 'bg-accent/30 border-accent'
      )}
    >
      <div className="mt-1 shrink-0">
        <Icon className="h-5 w-5 text-primary" />
      </div>

      <div className="flex-1 min-w-0">
        <p className="font-medium text-sm">{notification.title}</p>
        <p className="text-sm text-muted-foreground mt-1">{notification.message}</p>
        <p className="text-xs text-muted-foreground mt-2">
          {new Date(notification.createdAt).toLocaleString()}
        </p>
      </div>

      <div className="flex flex-col gap-1 shrink-0">
        {!notification.read && (
          <Button
            variant="ghost"
            size="icon"
            className="h-8 w-8"
            onClick={() => onMarkAsRead(notification.id)}
            title={t('markAsRead')}
          >
            <Check className="h-4 w-4" />
          </Button>
        )}
        <Button
          variant="ghost"
          size="icon"
          className="h-8 w-8 text-destructive hover:text-destructive"
          onClick={() => onDelete(notification.id)}
          title={t('delete')}
        >
          <Trash2 className="h-4 w-4" />
        </Button>
      </div>
    </div>
  );
}
