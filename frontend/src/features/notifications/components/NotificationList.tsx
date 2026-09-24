import { Bell } from 'lucide-react';
import { useTranslation } from 'react-i18next';

import {
  useDeleteNotification,
  useMarkNotificationAsRead,
  useNotifications,
} from '../hooks/useNotifications';
import { NotificationItem } from './NotificationItem';

export function NotificationList() {
  const { t } = useTranslation('notifications');

  const {
    data: notifications,
    isLoading,
  } = useNotifications();

  const { mutate: markAsRead } =
    useMarkNotificationAsRead();

  const { mutate: deleteNotification } =
    useDeleteNotification();

  if (isLoading) {
    return (
      <div
        className="space-y-3"
        aria-hidden="true"
      >
        {Array.from({ length: 3 }).map((_, index) => (
          <div
            key={`notification-skeleton-${index}`}
            className="h-24 animate-pulse rounded-lg bg-muted"
          />
        ))}
      </div>
    );
  }

  if (!notifications?.length) {
    return (
      <div className="flex flex-col items-center justify-center py-12 text-center">
        <Bell
          className="mb-4 h-12 w-12 text-muted-foreground/50"
          aria-hidden="true"
        />

        <p className="text-muted-foreground">
          {t('empty')}
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-3">
      {notifications.map((notification) => (
        <NotificationItem
          key={notification.id}
          notification={notification}
          onMarkAsRead={markAsRead}
          onDelete={deleteNotification}
        />
      ))}
    </div>
  );
}