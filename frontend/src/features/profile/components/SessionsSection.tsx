import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Monitor, X } from 'lucide-react';

import { useAuth } from '@/features/auth/context/AuthContext';
import { ConfirmDeletionDialog } from '@/shared/components/ConfirmDeletionDialog';
import { Badge } from '@/shared/components/core/badge';
import { Button } from '@/shared/components/core/button';

import { useSessions } from '../hooks/useSessions';
import { SettingsSection } from './SettingsSection';

export function SessionsSection() {
  const { t } = useTranslation(['userProfile', 'common']);
  const { sessionId: currentSessionId } = useAuth();

  const {
    sessions,
    isLoading,
    deleteSession,
    isDeletingSession,
    deleteAllOtherSessions,
    isDeletingAllSessions,
  } = useSessions();

  const [confirmSessionId, setConfirmSessionId] =
    useState<string | null>(null);

  const [confirmAllOpen, setConfirmAllOpen] = useState(false);

  const selectedSession = sessions?.find(
    (session) => session.id === confirmSessionId,
  );

  return (
    <SettingsSection
      icon={Monitor}
      title={t('sessions.title')}
      description={t('sessions.description')}
    >
      {isLoading ? (
        <div className="text-sm text-muted-foreground">
          {t('common:loading')}
        </div>
      ) : (
        <div className="space-y-4">
          {sessions && sessions.length > 0 ? (
            <div className="space-y-2">
              {sessions.map((session) => {
                const isCurrent =
                  session.id === currentSessionId;

                return (
                  <div
                    key={session.id}
                    className="flex items-center justify-between gap-4 rounded-md border p-3"
                  >
                    <div className="flex min-w-0 items-center gap-3">
                      <div className="flex min-w-0 flex-col">
                        <span className="text-sm font-medium">
                          {session.channel === 'WEB'
                            ? t('sessions.web')
                            : t('sessions.telegram')}
                        </span>

                        <span className="text-xs text-muted-foreground">
                          {new Date(
                            session.createdAt,
                          ).toLocaleString()}
                        </span>
                      </div>

                      {isCurrent && (
                        <Badge variant="default">
                          {t('sessions.current')}
                        </Badge>
                      )}
                    </div>

                    {!isCurrent && (
                      <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        onClick={() =>
                          setConfirmSessionId(session.id)
                        }
                        disabled={isDeletingSession}
                      >
                        <X
                          className="mr-1 h-4 w-4"
                          aria-hidden="true"
                        />
                        {t('sessions.close')}
                      </Button>
                    )}
                  </div>
                );
              })}
            </div>
          ) : (
            <p className="text-sm text-muted-foreground">
              {t('sessions.empty')}
            </p>
          )}

          {sessions && sessions.length > 1 && (
            <Button
              type="button"
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
        open={confirmSessionId !== null}
        onOpenChange={(open) => {
          if (!open) {
            setConfirmSessionId(null);
          }
        }}
        itemName={selectedSession?.channel ?? ''}
        title={t('sessions.confirmTitle')}
        description={t('sessions.confirmDescription')}
        onConfirm={() => {
          if (!confirmSessionId) {
            return;
          }

          deleteSession(confirmSessionId, {
            onSuccess: () => {
              setConfirmSessionId(null);
            },
          });
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
            onSuccess: () => {
              setConfirmAllOpen(false);
            },
          });
        }}
        isDeleting={isDeletingAllSessions}
      />
    </SettingsSection>
  );
}