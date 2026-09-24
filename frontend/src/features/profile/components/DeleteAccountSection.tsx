import { useId, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { AlertTriangle } from 'lucide-react';

import { Spinner } from '@/shared/components/Spinner';
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from '@/shared/components/core/card';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/core/dialog';
import { Button } from '@/shared/components/core/button';
import { Input } from '@/shared/components/core/input';
import { Label } from '@/shared/components/core/label';

import { useDeactivateAccount } from '../hooks/useDeactivateAccount';

export function DeleteAccountSection() {
  const { t } = useTranslation('userProfile');

  const {
    mutate: deactivateAccount,
    isPending,
  } = useDeactivateAccount();

  const [modalOpen, setModalOpen] = useState(false);
  const [password, setPassword] = useState('');
  const [confirmed, setConfirmed] = useState(false);

  const passwordId = useId();
  const confirmationId = useId();

  const canSubmit =
    confirmed && password.trim().length > 0;

  const resetForm = () => {
    setPassword('');
    setConfirmed(false);
  };

  const handleOpenChange = (open: boolean) => {
    setModalOpen(open);

    if (!open) {
      resetForm();
    }
  };

  const handleConfirm = () => {
    if (!canSubmit) {
      return;
    }

    deactivateAccount(
      { password },
      {
        onSuccess: () => {
          setModalOpen(false);
          resetForm();
        },
      },
    );
  };

  return (
    <>
      <Card className="border-destructive">
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-lg text-destructive">
            <AlertTriangle
              className="h-5 w-5"
              aria-hidden="true"
            />
            {t('deleteAccount.title')}
          </CardTitle>
        </CardHeader>

        <CardContent className="space-y-4">
          <p className="text-sm text-muted-foreground">
            {t('deleteAccount.description')}
          </p>

          <Button
            type="button"
            variant="destructive"
            onClick={() => setModalOpen(true)}
          >
            {t('deleteAccount.button')}
          </Button>
        </CardContent>
      </Card>

      <Dialog
        open={modalOpen}
        onOpenChange={handleOpenChange}
      >
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle>
              {t('deleteAccount.modalTitle')}
            </DialogTitle>

            <DialogDescription>
              {t('deleteAccount.modalDescription')}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-4 py-2">
            <div className="space-y-2">
              <Label htmlFor={passwordId}>
                {t('deleteAccount.password')}
              </Label>

              <Input
                id={passwordId}
                type="password"
                value={password}
                onChange={(event) =>
                  setPassword(event.target.value)
                }
                disabled={isPending}
              />
            </div>

            <div className="flex items-start gap-2">
              <input
                id={confirmationId}
                type="checkbox"
                checked={confirmed}
                onChange={(event) =>
                  setConfirmed(event.target.checked)
                }
                disabled={isPending}
                className="mt-1 h-4 w-4 rounded border-input"
              />

              <label
                htmlFor={confirmationId}
                className="text-sm leading-tight"
              >
                {t('deleteAccount.checkbox')}
              </label>
            </div>
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => handleOpenChange(false)}
              disabled={isPending}
              className="border-border"
            >
              {t('common:actions.cancel')}
            </Button>

            <Button
              type="button"
              variant="destructive"
              onClick={handleConfirm}
              disabled={!canSubmit || isPending}
            >
              {isPending ? (
                <Spinner size={20} />
              ) : (
                t('deleteAccount.confirm')
              )}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}