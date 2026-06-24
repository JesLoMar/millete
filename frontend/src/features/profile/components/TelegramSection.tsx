import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { MessageCircle } from 'lucide-react';
import { Button } from '@/shared/components/core/button';
import { Badge } from '@/shared/components/core/badge';
import { SettingsSection } from './SettingsSection';
import { ConfirmDeletionDialog } from '@/features/categories/components/ConfirmDeletionDialog';
import { useProfile } from '../hooks/useProfile';
import { useTelegramUnlink } from '../hooks/useTelegramUnlink';

export function TelegramSection() {
  const { t } = useTranslation('userProfile');
  const { profile, isLoading } = useProfile();
  const { mutate: unlinkTelegram, isPending } = useTelegramUnlink();

  const [confirmOpen, setConfirmOpen] = useState(false);

  const isLinked = !!profile?.telegramChatId;

  return (
    <SettingsSection
      icon={MessageCircle}
      title={t('telegram.title')}
      description={t('telegram.description')}
      badge={
        isLinked ? (
          <Badge variant="default">{t('telegram.linked')}</Badge>
        ) : (
          <Badge variant="secondary">{t('telegram.notLinked')}</Badge>
        )
      }
    >
      {isLoading ? (
        <div className="text-sm text-muted-foreground">{t('common:loading')}</div>
      ) : (
        <div className="space-y-4">
          <div className="flex items-center gap-2">
            <span className="text-sm text-muted-foreground">{t('telegram.chatId')}</span>
            <span className="text-sm font-medium">
              {profile?.telegramChatId ?? '—'}
            </span>
          </div>
          {isLinked && (
            <Button
              variant="outline"
              size="sm"
              onClick={() => setConfirmOpen(true)}
              disabled={isPending}
            >
              {t('telegram.unlink')}
            </Button>
          )}
        </div>
      )}

      <ConfirmDeletionDialog
        open={confirmOpen}
        onOpenChange={setConfirmOpen}
        itemName="Telegram"
        title={t('telegram.confirmTitle')}
        description={t('telegram.confirmDescription')}
        onConfirm={() => {
          unlinkTelegram(undefined, {
            onSuccess: () => setConfirmOpen(false),
          });
        }}
        isDeleting={isPending}
      />
    </SettingsSection>
  );
}
