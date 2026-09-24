import { AlertTriangle } from 'lucide-react';
import { useTranslation } from 'react-i18next';

import { Spinner } from '@/shared/components/Spinner';
import { Button } from '@/shared/components/core/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/core/dialog';

interface ConfirmDeletionDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  itemName: string;
  onConfirm: () => void;
  isDeleting?: boolean;
  title?: string;
  description?: string;
}

export function ConfirmDeletionDialog({
  open,
  onOpenChange,
  itemName,
  onConfirm,
  isDeleting = false,
  title,
  description,
}: ConfirmDeletionDialogProps) {
  const { t } = useTranslation([
    'categories',
    'common',
  ]);

  const dialogTitle =
    title ?? t('categories:deleteTitle');

  const dialogDescription =
    description ??
    t('categories:deleteConfirmation', {
      name: itemName,
    });

  return (
    <Dialog
      open={open}
      onOpenChange={onOpenChange}
    >
      <DialogContent className="border-border bg-card sm:max-w-md">
        <DialogHeader>
          <div className="mx-auto mb-4 w-fit rounded-full bg-destructive/10 p-3">
            <AlertTriangle
              className="size-8 text-destructive"
              aria-hidden="true"
            />
          </div>

          <DialogTitle className="text-center text-xl font-semibold">
            {dialogTitle}
          </DialogTitle>

          <DialogDescription className="pt-2 text-center">
            {dialogDescription}
          </DialogDescription>
        </DialogHeader>

        <DialogFooter className="gap-2 sm:justify-center">
          <Button
            type="button"
            variant="outline"
            onClick={() => onOpenChange(false)}
            disabled={isDeleting}
            className="border-border"
          >
            {t('common:actions.cancel')}
          </Button>

          <Button
            type="button"
            variant="destructive"
            onClick={onConfirm}
            disabled={isDeleting}
            className="gap-2"
          >
            {isDeleting ? (
              <Spinner size={20} />
            ) : (
              t('common:actions.delete')
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}