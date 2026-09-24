import { Bell } from 'lucide-react';
import { useTranslation } from 'react-i18next';

import {
  useDeleteNotification,
  useMarkNotificationAsRead,
  useRecentNotifications,
} from '../hooks/useNotifications';
import { NotificationBellItem } from './NotificationBellItem';

const RECENT_LIMIT = 20;

interface NotificationBellListProps {
  onNavigate: () => void;
}

export function NotificationBellList({
  onNavigate,
}: NotificationBellListProps) {
  const { t } = useTranslation('notifications');

  const {
    data: notifications,
    isLoading,
  } = useRecentNotifications(RECENT_LIMIT);

  const { mutate: markAsRead } =
    useMarkNotificationAsRead();

  const { mutate: deleteNotification } =
    useDeleteNotification();

  const allNotifications = notifications ?? [];

  if (isLoading) {
    return (
      <div className="space-y-2" aria-hidden="true">
        {Array.from({ length: 3 }).map((_, index) => (
          <div
            key={`notification-skeleton-${index}`}
            className="h-16 animate-pulse rounded-lg bg-muted"
          />
        ))}
      </div>
    );
  }

  if (allNotifications.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-8 text-center">
        <Bell
          className="mb-3 h-10 w-10 text-muted-foreground/50"
          aria-hidden="true"
        />

        <p className="text-sm text-muted-foreground">
          {t('empty')}
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-2">
      {allNotifications.map((notification) => (
        <NotificationBellItem
          key={notification.id}
          notification={notification}
          onMarkAsRead={markAsRead}
          onDelete={deleteNotification}
          onNavigate={onNavigate}
        />
      ))}
    </div>
  );
}