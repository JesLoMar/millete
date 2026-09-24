import { memo, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Bell,
  Users,
  X,
} from 'lucide-react';
import { useTranslation } from 'react-i18next';

import { ROUTES } from '@/app/router/routes';
import { Button } from '@/shared/components/core/button';
import { cn } from '@/lib/utils';

import type { Notification } from '../types';

interface NotificationBellItemProps {
  notification: Notification;
  onMarkAsRead: (id: string) => void;
  onDelete: (id: string) => void;
  onNavigate: () => void;
}

const getNotificationIcon = (
  type: Notification['type'],
) => {
  switch (type) {
    case 'GOAL_INVITATION':
      return Users;

    case 'SYSTEM':
    default:
      return Bell;
  }
};

export const NotificationBellItem = memo(
  function NotificationBellItem({
    notification,
    onMarkAsRead,
    onDelete,
    onNavigate,
  }: NotificationBellItemProps) {
    const { t } = useTranslation([
      'notifications',
      'common',
    ]);

    const navigate = useNavigate();

    const Icon = getNotificationIcon(
      notification.type,
    );

    const handleClick = useCallback(() => {
      if (!notification.read) {
        onMarkAsRead(notification.id);
      }

      onNavigate();

      if (
        notification.type === 'GOAL_INVITATION'
      ) {
        navigate(
          `${ROUTES.profile}?section=notifications`,
        );
      } else {
        navigate(ROUTES.notifications);
      }
    }, [
      notification.id,
      notification.read,
      notification.type,
      navigate,
      onMarkAsRead,
      onNavigate,
    ]);

    const handleDismiss = useCallback(() => {
      onDelete(notification.id);
    }, [notification.id, onDelete]);

    return (
      <div
        className={cn(
          'flex items-center gap-3 rounded-lg border p-3 transition-colors',
          notification.read
            ? 'border-border bg-card'
            : 'border-accent/40 bg-accent/10',
        )}
      >
        <button
          type="button"
          className="flex min-w-0 flex-1 items-center gap-3 text-left"
          onClick={handleClick}
        >
          <div
            className="relative shrink-0"
            aria-hidden="true"
          >
            <Icon className="h-5 w-5 text-primary" />

            {!notification.read && (
              <span className="absolute -right-0.5 -top-0.5 size-2 rounded-full bg-primary" />
            )}
          </div>

          <div className="min-w-0 flex-1">
            <p
              className={cn(
                'truncate text-sm font-medium',
                !notification.read &&
                  'text-foreground',
              )}
            >
              {notification.title}
            </p>

            <p className="truncate text-xs text-muted-foreground">
              {notification.message}
            </p>
          </div>
        </button>

        <Button
          type="button"
          variant="ghost"
          size="icon"
          className="h-7 w-7 shrink-0 text-muted-foreground hover:text-destructive"
          onClick={handleDismiss}
          title={t('common:actions.delete')}
          aria-label={t('common:actions.delete')}
        >
          <X
            className="h-4 w-4"
            aria-hidden="true"
          />
        </Button>
      </div>
    );
  },
);