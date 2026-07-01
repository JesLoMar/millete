import { Bell } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useNotifications, useMarkNotificationAsRead } from '../hooks/useNotifications';
import { NotificationBellItem } from './NotificationBellItem';

interface NotificationBellListProps {
  onNavigate: () => void;
}

export function NotificationBellList({ onNavigate }: NotificationBellListProps) {
  const { t } = useTranslation('notifications');
  const { data: notifications, isLoading } = useNotifications();
  const { mutate: markAsRead } = useMarkNotificationAsRead();

  // Solo mostrar notificaciones no leídas en la campana
  const unreadNotifications = notifications?.filter((n) => !n.read) ?? [];

  if (isLoading) {
    return (
      <div className="space-y-2">
        {Array.from({ length: 3 }).map((_, i) => (
          <div key={i} className="h-16 rounded-lg bg-muted animate-pulse" />
        ))}
      </div>
    );
  }

  if (unreadNotifications.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-8 text-center">
        <Bell className="h-10 w-10 text-muted-foreground/50 mb-3" />
        <p className="text-muted-foreground text-sm">{t('empty')}</p>
      </div>
    );
  }

  return (
    <div className="space-y-2">
      {unreadNotifications.map((notification) => (
        <NotificationBellItem
          key={notification.id}
          notification={notification}
          onMarkAsRead={(id) => markAsRead(id)}
          onNavigate={onNavigate}
        />
      ))}
    </div>
  );
}
