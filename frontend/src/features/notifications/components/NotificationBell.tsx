import { useState } from 'react';
import { Bell } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Button } from '@/shared/components/core/button';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/core/dialog';
import { useQueryClient } from '@tanstack/react-query';
import { useUnreadNotificationsCount } from '../hooks/useNotifications';
import { NotificationBellList } from './NotificationBellList';

export function NotificationBell() {
  const { t } = useTranslation('notifications');
  const [open, setOpen] = useState(false);
  const { data: count = 0 } = useUnreadNotificationsCount();
  const queryClient = useQueryClient();

  const handleOpenChange = (nextOpen: boolean) => {
    if (nextOpen) {
      queryClient.setQueryData(['notifications', 'unread-count'], 0);
    }
    setOpen(nextOpen);
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <Button
        variant="ghost"
        size="icon"
        className="relative"
        onClick={() => handleOpenChange(true)}
        aria-label={t('title')}
      >
        <Bell className="h-5 w-5" />
        {count > 0 && (
          <span className="absolute -top-1 -right-1 flex h-5 w-5 items-center justify-center rounded-full bg-destructive text-[10px] font-medium text-destructive-foreground">
            {count > 99 ? '99+' : count}
          </span>
        )}
      </Button>
      <DialogContent className="bg-card border-border sm:max-w-md">
        <DialogHeader>
          <DialogTitle>{t('title')}</DialogTitle>
        </DialogHeader>
        <div className="max-h-[60vh] overflow-y-auto py-2">
          <NotificationBellList onNavigate={() => setOpen(false)} />
        </div>
      </DialogContent>
    </Dialog>
  );
}
