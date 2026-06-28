import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { AlertTriangle } from 'lucide-react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '@/shared/components/core/dialog';
import { Button } from '@/shared/components/core/button';
import { Input } from '@/shared/components/core/input';
import { Label } from '@/shared/components/core/label';
import { Card, CardContent, CardHeader, CardTitle } from '@/shared/components/core/card';
import { useDeactivateAccount } from '../hooks/useDeactivateAccount';

export function DeleteAccountSection() {
  const { t } = useTranslation('userProfile');
  const { mutate: deactivateAccount, isPending } = useDeactivateAccount();

  const [modalOpen, setModalOpen] = useState(false);
  const [password, setPassword] = useState('');
  const [confirmed, setConfirmed] = useState(false);

  const canSubmit = confirmed && password.trim().length > 0;

  const handleConfirm = () => {
    deactivateAccount({ password }, {
      onSuccess: () => setModalOpen(false),
    });
  };

  const handleClose = () => {
    setModalOpen(false);
    setPassword('');
    setConfirmed(false);
  };

  return (
    <>
      <Card className="border-destructive">
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-lg text-destructive">
            <AlertTriangle className="h-5 w-5" />
            {t('deleteAccount.title')}
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          <p className="text-sm text-muted-foreground">
            {t('deleteAccount.description')}
          </p>
          <Button variant="destructive" onClick={() => setModalOpen(true)}>
            {t('deleteAccount.button')}
          </Button>
        </CardContent>
      </Card>

      <Dialog open={modalOpen} onOpenChange={handleClose}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>{t('deleteAccount.modalTitle')}</DialogTitle>
            <DialogDescription>{t('deleteAccount.modalDescription')}</DialogDescription>
          </DialogHeader>

          <div className="space-y-4 py-2">
            <div className="space-y-2">
              <Label>{t('deleteAccount.password')}</Label>
              <Input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>
            <div className="flex items-start gap-2">
              <input
                type="checkbox"
                id="confirm-delete"
                checked={confirmed}
                onChange={(e) => setConfirmed(e.target.checked)}
                className="mt-1 h-4 w-4 rounded border-input"
              />
              <label htmlFor="confirm-delete" className="text-sm leading-tight">
                {t('deleteAccount.checkbox')}
              </label>
            </div>
          </div>

          <DialogFooter>
            <Button variant="outline" onClick={handleClose} disabled={isPending}>
              {t('common:actions.cancel')}
            </Button>
            <Button
              variant="destructive"
              onClick={handleConfirm}
              disabled={!canSubmit || isPending}
            >
              {isPending ? t('common:actions.deleting') : t('deleteAccount.confirm')}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
