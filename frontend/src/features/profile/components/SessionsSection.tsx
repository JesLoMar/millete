import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Monitor, X } from 'lucide-react';
import { Button } from '@/shared/components/core/button';
import { Badge } from '@/shared/components/core/badge';
import { SettingsSection } from './SettingsSection';
import { ConfirmDeletionDialog } from '@/features/categories/components/ConfirmDeletionDialog';
import { useAuth } from '@/features/auth/context/AuthContext';
import { useSessions } from '../hooks/useSessions';

export function SessionsSection() {
  const { t } = useTranslation('userProfile');
  const { sessionId: currentSessionId } = useAuth();
  const { sessions, isLoading, deleteSession, isDeletingSession, deleteAllOtherSessions, isDeletingAllSessions } = useSessions();

  const [confirmSessionId, setConfirmSessionId] = useState<string | null>(null);
  const [confirmAllOpen, setConfirmAllOpen] = useState(false);

  const selectedSession = sessions?.find((s) => s.id === confirmSessionId);

  return (
    <SettingsSection
      icon={Monitor}
      title={t('sessions.title')}
      description={t('sessions.description')}
    >
      {isLoading ? (
        <div className="text-sm text-muted-foreground">{t('common:loading')}</div>
      ) : (
        <div className="space-y-4">
          <div className="space-y-2">
            {sessions?.map((session) => {
              const isCurrent = session.id === currentSessionId;
              return (
                <div
                  key={session.id}
                  className="flex items-center justify-between rounded-md border p-3"
                >
                  <div className="flex items-center gap-3">
                    <div className="flex flex-col">
                      <span className="text-sm font-medium">
                        {session.channel === 'WEB' ? t('sessions.web') : t('sessions.telegram')}
                      </span>
                      <span className="text-xs text-muted-foreground">
                        {new Date(session.createdAt).toLocaleString()}
                      </span>
                    </div>
                    {isCurrent && (
                      <Badge variant="default">{t('sessions.current')}</Badge>
                    )}
                  </div>
                  {!isCurrent && (
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => setConfirmSessionId(session.id)}
                    >
                      <X className="h-4 w-4 mr-1" />
                      {t('sessions.close')}
                    </Button>
                  )}
                </div>
              );
            })}
          </div>

          {sessions && sessions.length > 1 && (
            <Button
              variant="destructive"
              size="sm"
              onClick={() => setConfirmAllOpen(true)}
              disabled={isDeletingAllSessions}
            >
              {t('sessions.closeAll')}
            </Button>
          )}
        </div>
      )}

      <ConfirmDeletionDialog
        open={!!confirmSessionId}
        onOpenChange={(open) => !open && setConfirmSessionId(null)}
        itemName={selectedSession?.channel ?? ''}
        title={t('sessions.confirmTitle')}
        description={t('sessions.confirmDescription')}
        onConfirm={() => {
          if (confirmSessionId) {
            deleteSession(confirmSessionId, {
              onSuccess: () => setConfirmSessionId(null),
            });
          }
        }}
        isDeleting={isDeletingSession}
      />

      <ConfirmDeletionDialog
        open={confirmAllOpen}
        onOpenChange={setConfirmAllOpen}
        itemName=""
        title={t('sessions.confirmAllTitle')}
        description={t('sessions.confirmAllDescription')}
        onConfirm={() => {
          deleteAllOtherSessions(undefined, {
            onSuccess: () => setConfirmAllOpen(false),
          });
        }}
        isDeleting={isDeletingAllSessions}
      />
    </SettingsSection>
  );
}
