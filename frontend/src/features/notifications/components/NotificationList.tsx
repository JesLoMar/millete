import { Bell } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useDeleteNotification, useMarkNotificationAsRead, useNotifications } from '../hooks/useNotifications';
import { NotificationItem } from './NotificationItem';

export function NotificationList() {
  const { t } = useTranslation('notifications');
  const { data: notifications, isLoading } = useNotifications();
  const { mutate: markAsRead } = useMarkNotificationAsRead();
  const { mutate: deleteNotification } = useDeleteNotification();

  if (isLoading) {
    return (
      <div className="space-y-3">
        {Array.from({ length: 3 }).map((_, i) => (
          <div key={i} className="h-24 rounded-lg bg-muted animate-pulse" />
        ))}
      </div>
    );
  }

  if (!notifications || notifications.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-12 text-center">
        <Bell className="h-12 w-12 text-muted-foreground/50 mb-4" />
        <p className="text-muted-foreground">{t('empty')}</p>
      </div>
    );
  }

  return (
    <div className="space-y-3">
      {notifications.map((notification) => (
        <NotificationItem
          key={notification.id}
          notification={notification}
          onMarkAsRead={(id) => markAsRead(id)}
          onDelete={(id) => deleteNotification(id)}
        />
      ))}
    </div>
  );
}
