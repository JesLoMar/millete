import { Check, X } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { Button } from '@/shared/components/core/button';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/core/table';
import { useNotifications } from '@/features/notifications/hooks/useNotifications';
import { useAcceptInvitation, useRejectInvitation } from '@/features/groupgoals/hooks/useInvitations';
import { SettingsSection } from './SettingsSection';
import { Bell } from 'lucide-react';

export function NotificationsTable() {
  const { t } = useTranslation('userProfile');
  const { data: notifications, isLoading } = useNotifications();
  const { mutate: acceptInvitation, isPending: isAccepting } = useAcceptInvitation();
  const { mutate: rejectInvitation, isPending: isRejecting } = useRejectInvitation();

  const goalInvitations = notifications?.filter(
    (n) => n.type === 'GOAL_INVITATION' && n.actionRequired && !n.actionedAt
  );

  const handleAccept = (invitationId: string) => {
    acceptInvitation(invitationId);
  };

  const handleReject = (invitationId: string) => {
    rejectInvitation(invitationId);
  };

  return (
    <SettingsSection
      icon={Bell}
      title={t('notifications.title')}
      description={t('notifications.description')}
    >
      {isLoading ? (
        <div className="h-32 rounded-lg bg-muted animate-pulse" />
      ) : !goalInvitations || goalInvitations.length === 0 ? (
        <p className="text-muted-foreground text-sm">{t('notifications.empty')}</p>
      ) : (
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>{t('notifications.message')}</TableHead>
              <TableHead className="w-[180px] text-right">{t('notifications.actions')}</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {goalInvitations.map((notification) => (
              <TableRow key={notification.id}>
                <TableCell>
                  <div>
                    <p className="font-medium text-sm">{notification.title}</p>
                    <p className="text-sm text-muted-foreground">{notification.message}</p>
                  </div>
                </TableCell>
                <TableCell className="text-right">
                  <div className="flex justify-end gap-2">
                    <Button
                      size="sm"
                      variant="outline"
                      className="h-8 w-8 p-0"
                      onClick={() => handleAccept(notification.metadata.invitationId)}
                      disabled={isAccepting || isRejecting}
                      title={t('notifications.accept')}
                    >
                      <Check className="h-4 w-4 text-emerald-600" />
                    </Button>
                    <Button
                      size="sm"
                      variant="outline"
                      className="h-8 w-8 p-0"
                      onClick={() => handleReject(notification.metadata.invitationId)}
                      disabled={isAccepting || isRejecting}
                      title={t('notifications.reject')}
                    >
                      <X className="h-4 w-4 text-destructive" />
                    </Button>
                  </div>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      )}
    </SettingsSection>
  );
}
