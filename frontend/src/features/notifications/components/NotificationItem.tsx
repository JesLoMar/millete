import { memo, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { Bell, Check, Trash2, Users } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Button } from '@/shared/components/core/button';
import { cn } from '@/lib/utils';
import { useAcceptInvitation, useRejectInvitation } from '@/features/groupgoals/hooks/useInvitations';
import { notify } from '@/shared/utils/notifications/notify';
import type { Notification } from '../types';

interface NotificationItemProps {
  notification: Notification;
  onMarkAsRead: (id: string) => void;
  onDelete: (id: string) => void;
}

export const NotificationItem = memo(function NotificationItem({ notification, onMarkAsRead, onDelete }: NotificationItemProps) {
  const { t } = useTranslation(['notifications', 'common', 'groupGoals']);
  const navigate = useNavigate();

  const { mutate: acceptInvitation, isPending: isAccepting } = useAcceptInvitation();
  const { mutate: rejectInvitation, isPending: isRejecting } = useRejectInvitation();

  const isGoalInvitation = notification.type === 'GOAL_INVITATION';
  const isActionLoading = isAccepting || isRejecting;

  const invitationId = notification.metadata?.invitationId || notification.metadata?.id;

  const getIcon = () => {
    switch (notification.type) {
      case 'GOAL_INVITATION':
        return Users;
      default:
        return Bell;
    }
  };

  const Icon = getIcon();

  const handleAccept = useCallback(() => {
    if (!invitationId) return;
    acceptInvitation(invitationId, {
      onSuccess: () => {
        notify.success(t('groupGoals:invitationAccepted'));
        onMarkAsRead(notification.id);
      },
      onError: () => {
        notify.error(t('groupGoals:invitationError'));
      },
    });
  }, [invitationId, acceptInvitation, notification.id, onMarkAsRead, t]);

  const handleReject = useCallback(() => {
    if (!invitationId) return;
    rejectInvitation(invitationId, {
      onSuccess: () => {
        onMarkAsRead(notification.id);
      },
      onError: () => {
        notify.error(t('groupGoals:invitationError'));
      },
    });
  }, [invitationId, rejectInvitation, notification.id, onMarkAsRead, t]);

  const handleNavigate = useCallback(() => {
    navigate('/profile?section=notifications');
  }, [navigate]);
    if (isGoalInvitation && notification.metadata?.goalId) {
      navigate(`/group-goals?goalId=${notification.metadata.goalId}`);
    } else if (isGoalInvitation) {
      navigate('/profile?section=notifications');
    }
  }, [isGoalInvitation, notification.metadata?.goalId, navigate]);
    if (isGoalInvitation && notification.metadata?.goalId) {
      navigate(`/group-goals?goalId=${notification.metadata.goalId}`);
    } else if (isGoalInvitation) {
      navigate('/profile');
    }
  }, [isGoalInvitation, notification.metadata?.goalId, navigate]);

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

        {}
        {isGoalInvitation && !notification.read && invitationId && (
          <div className="flex items-center gap-2 mt-3">
            <Button
              size="sm"
              className="h-8 text-xs"
              onClick={handleAccept}
              disabled={isActionLoading}
            >
              {t('common:actions.accept')}
            </Button>
            <Button
              size="sm"
              variant="outline"
              className="h-8 text-xs"
              onClick={handleReject}
              disabled={isActionLoading}
            >
              {t('common:actions.reject')}
            </Button>
            <Button
              size="sm"
              variant="ghost"
              className="h-8 text-xs"
              onClick={handleNavigate}
            >
              {t('common:actions.view')}
            </Button>
          </div>
        )}
      </div>

      <div className="flex flex-col gap-1 shrink-0">
        {!notification.read && (
          <Button
            variant="ghost"
            size="icon"
            className="h-8 w-8"
            onClick={() => onMarkAsRead(notification.id)}
            title={t('notifications:markAsRead')}
          >
            <Check className="h-4 w-4" />
          </Button>
        )}
        <Button
          variant="ghost"
          size="icon"
          className="h-8 w-8 text-destructive hover:text-destructive"
          onClick={() => onDelete(notification.id)}
          title={t('notifications:delete')}
        >
          <Trash2 className="h-4 w-4" />
        </Button>
      </div>
    </div>
  );
});
