import { Check, X } from 'lucide-react';
import { Spinner } from "@/shared/components/Spinner";
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
import { Pagination } from '@/shared/components/Pagination';
import { usePaginatedNotifications } from '@/features/notifications/hooks/useNotifications';
import { useAcceptInvitation, useRejectInvitation } from '@/features/groupgoals/hooks/useInvitations';
import { SettingsSection } from './SettingsSection';
import { Bell } from 'lucide-react';

export function NotificationsTable() {
  const { t } = useTranslation('userProfile');
  const {
    displayItems,
    displayPage,
    displaySize,
    totalDisplayPages,
    totalElements,
    isLoading,
    nextPage,
    prevPage,
  } = usePaginatedNotifications();
  const { mutate: acceptInvitation, isPending: isAccepting } = useAcceptInvitation();
  const { mutate: rejectInvitation, isPending: isRejecting } = useRejectInvitation();

  const goalInvitations = displayItems.filter(
    (n) => n.type === 'GOAL_INVITATION' && n.actionRequired && !n.actionedAt
  );

  const handleAccept = (invitationId: string) => {
    acceptInvitation(invitationId);
  };

  const handleReject = (invitationId: string) => {
    rejectInvitation(invitationId);
  };

  const from = totalElements === 0 ? 0 : displayPage * displaySize + 1;
  const to = Math.min((displayPage + 1) * displaySize, totalElements);

  return (
    <SettingsSection
      icon={Bell}
      title={t('notifications.title')}
      description={t('notifications.description')}
    >
      {isLoading ? (
        <div className="h-32 rounded-lg bg-muted animate-pulse" />
      ) : goalInvitations.length === 0 ? (
        <p className="text-muted-foreground text-sm">{t('notifications.empty')}</p>
      ) : (
        <>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>{t('notifications.message')}</TableHead>
                <TableHead className="w-45 text-right">{t('notifications.actions')}</TableHead>
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
                        {isAccepting ? <Spinner size={16} /> : <Check className="h-4 w-4 text-primary" />}
                      </Button>
                      <Button
                        size="sm"
                        variant="outline"
                        className="h-8 w-8 p-0"
                        onClick={() => handleReject(notification.metadata.invitationId)}
                        disabled={isAccepting || isRejecting}
                        title={t('notifications.reject')}
                      >
                        {isRejecting ? <Spinner size={16} /> : <X className="h-4 w-4 text-destructive" />}
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          <Pagination
            currentPage={displayPage}
            totalPages={totalDisplayPages}
            from={from}
            to={to}
            total={totalElements}
            onPrev={prevPage}
            onNext={nextPage}
          />
        </>
      )}
    </SettingsSection>
  );
}
