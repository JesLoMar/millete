import { Bell } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useRecentNotifications, useMarkNotificationAsRead, useDeleteNotification } from '../hooks/useNotifications';
import { NotificationBellItem } from './NotificationBellItem';

const RECENT_LIMIT = 20;

interface NotificationBellListProps {
  onNavigate: () => void;
}

export function NotificationBellList({ onNavigate }: NotificationBellListProps) {
  const { t } = useTranslation('notifications');
  const { data: notifications, isLoading } = useRecentNotifications(RECENT_LIMIT);
  const { mutate: markAsRead } = useMarkNotificationAsRead();
  const { mutate: deleteNotification } = useDeleteNotification();

  // Se muestran leídas y no leídas (NotificationBellItem las estila distinto);
  // marcar como leída ya no hace desaparecer la tarjeta del panel.
  // Eliminar (X) sí la quita de la lista.
  const allNotifications = notifications ?? [];

  if (isLoading) {
    return (
      <div className="space-y-2">
        {Array.from({ length: 3 }).map((_, i) => (
          <div key={i} className="h-16 rounded-lg bg-muted animate-pulse" />
        ))}
      </div>
    );
  }

  if (allNotifications.length === 0) {
    return (
      <div className="flex flex-col items-center justify-center py-8 text-center">
        <Bell className="h-10 w-10 text-muted-foreground/50 mb-3" />
        <p className="text-muted-foreground text-sm">{t('empty')}</p>
      </div>
    );
  }

  return (
    <div className="space-y-2">
      {allNotifications.map((notification) => (
        <NotificationBellItem
          key={notification.id}
          notification={notification}
          onMarkAsRead={(id) => markAsRead(id)}
          onDelete={(id) => deleteNotification(id)}
          onNavigate={onNavigate}
        />
      ))}
    </div>
  );
}
