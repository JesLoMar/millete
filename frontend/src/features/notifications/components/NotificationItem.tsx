import { memo, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Bell,
  Check,
  Trash2,
  Users,
} from 'lucide-react';
import { useTranslation } from 'react-i18next';

import { ROUTES } from '@/app/router/routes';
import {
  useAcceptInvitation,
  useRejectInvitation,
} from '@/features/groupgoals/hooks/useInvitations';
import { Button } from '@/shared/components/core/button';
import { notify } from '@/shared/utils/notifications/notify';
import { cn } from '@/lib/utils';

import type { Notification } from '../types';

interface NotificationItemProps {
  notification: Notification;
  onMarkAsRead: (id: string) => void;
  onDelete: (id: string) => void;
}

function getNotificationIcon(
  type: Notification['type'],
) {
  switch (type) {
    case 'GOAL_INVITATION':
      return Users;

    case 'SYSTEM':
    default:
      return Bell;
  }
}

export const NotificationItem = memo(
  function NotificationItem({
    notification,
    onMarkAsRead,
    onDelete,
  }: NotificationItemProps) {
    const { t } = useTranslation([
      'notifications',
      'common',
      'groupGoals',
    ]);

    const navigate = useNavigate();

    const {
      mutate: acceptInvitation,
      isPending: isAccepting,
    } = useAcceptInvitation();

    const {
      mutate: rejectInvitation,
      isPending: isRejecting,
    } = useRejectInvitation();

    const isGoalInvitation =
      notification.type === 'GOAL_INVITATION';

    const isActionLoading =
      isAccepting || isRejecting;

    const invitationId =
      notification.metadata?.invitationId ??
      notification.metadata?.id;

    const Icon = getNotificationIcon(
      notification.type,
    );

    const handleAccept = useCallback(() => {
      if (!invitationId) {
        return;
      }

      acceptInvitation(invitationId, {
        onSuccess: () => {
          notify.success(
            t('groupGoals:invitationAccepted'),
          );

          onMarkAsRead(notification.id);
        },
        onError: () => {
          notify.error(
            t('groupGoals:invitationError'),
          );
        },
      });
    }, [
      acceptInvitation,
      invitationId,
      notification.id,
      onMarkAsRead,
      t,
    ]);

    const handleReject = useCallback(() => {
      if (!invitationId) {
        return;
      }

      rejectInvitation(invitationId, {
        onSuccess: () => {
          onMarkAsRead(notification.id);
        },
        onError: () => {
          notify.error(
            t('groupGoals:invitationError'),
          );
        },
      });
    }, [
      invitationId,
      notification.id,
      onMarkAsRead,
      rejectInvitation,
      t,
    ]);

    const handleNavigate = useCallback(() => {
      navigate(
        `${ROUTES.profile}?section=notifications`,
      );
    }, [navigate]);

    const handleMarkAsRead = useCallback(() => {
      onMarkAsRead(notification.id);
    }, [notification.id, onMarkAsRead]);

    const handleDelete = useCallback(() => {
      onDelete(notification.id);
    }, [notification.id, onDelete]);

    return (
      <div
        className={cn(
          'flex items-start gap-3 rounded-lg border p-4 transition-colors',
          notification.read
            ? 'bg-card/50'
            : 'border-accent bg-accent/30',
        )}
      >
        <div
          className="mt-1 shrink-0"
          aria-hidden="true"
        >
          <Icon className="h-5 w-5 text-primary" />
        </div>

        <div className="min-w-0 flex-1">
          <p className="text-sm font-medium">
            {notification.title}
          </p>

          <p className="mt-1 text-sm text-muted-foreground">
            {notification.message}
          </p>

          <p className="mt-2 text-xs text-muted-foreground">
            {new Date(
              notification.createdAt,
            ).toLocaleString()}
          </p>

          {isGoalInvitation &&
            !notification.read &&
            invitationId && (
              <div className="mt-3 flex items-center gap-2">
                <Button
                  type="button"
                  size="sm"
                  className="h-8 text-xs"
                  onClick={handleAccept}
                  disabled={isActionLoading}
                >
                  {t('common:actions.accept')}
                </Button>

                <Button
                  type="button"
                  size="sm"
                  variant="outline"
                  className="h-8 text-xs"
                  onClick={handleReject}
                  disabled={isActionLoading}
                >
                  {t('common:actions.reject')}
                </Button>

                <Button
                  type="button"
                  size="sm"
                  variant="ghost"
                  className="h-8 text-xs"
                  onClick={handleNavigate}
                  disabled={isActionLoading}
                >
                  {t('common:actions.view')}
                </Button>
              </div>
            )}
        </div>

        <div className="flex shrink-0 flex-col gap-1">
          {!notification.read && (
            <Button
              type="button"
              variant="ghost"
              size="icon"
              className="h-8 w-8"
              onClick={handleMarkAsRead}
              title={t('notifications:markAsRead')}
              aria-label={t(
                'notifications:markAsRead',
              )}
            >
              <Check
                className="h-4 w-4"
                aria-hidden="true"
              />
            </Button>
          )}

          <Button
            type="button"
            variant="ghost"
            size="icon"
            className="h-8 w-8 text-destructive hover:text-destructive"
            onClick={handleDelete}
            title={t('notifications:delete')}
            aria-label={t('notifications:delete')}
          >
            <Trash2
              className="h-4 w-4"
              aria-hidden="true"
            />
          </Button>
        </div>
      </div>
    );
  },
);