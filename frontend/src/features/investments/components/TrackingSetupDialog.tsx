import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { AlertTriangle, Loader2 } from 'lucide-react';

import { Button } from '@/shared/components/core/button';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/shared/components/core/dialog';
import { Input } from '@/shared/components/core/input';
import { Label } from '@/shared/components/core/label';

import { useInvestmentMutations } from '../hooks/useInvestmentMutations';
import { isFutureDateTime, todayDateInputValue } from '../utils';

interface TrackingSetupDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onConfigured?: () => void;
}

export function TrackingSetupDialog({
  open,
  onOpenChange,
  onConfigured,
}: TrackingSetupDialogProps) {
  const { t } = useTranslation('investments');
  const { configureTracking } = useInvestmentMutations();
  const [trackingStartAt, setTrackingStartAt] = useState(
    todayDateInputValue,
  );

  const isValid =
    trackingStartAt.length > 0 &&
    !isFutureDateTime(`${trackingStartAt}T00:00:00`);

  const handleSubmit = async () => {
    if (!isValid) {
      return;
    }

    try {
      await configureTracking.mutateAsync({
        // Medianoche local del día elegido, no medianoche UTC.
        trackingStartAt: new Date(
          `${trackingStartAt}T00:00:00`,
        ).toISOString(),
      });
      onOpenChange(false);
      onConfigured?.();
    } catch {
      // La mutation muestra el error y el diálogo permanece abierto.
    }
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="border-border bg-card sm:max-w-md">
        <div className="space-y-4">
          <DialogHeader>
            <DialogTitle>{t('tracking.setupTitle')}</DialogTitle>
            <DialogDescription>
              {t('tracking.setupDescription')}
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-2">
            <Label htmlFor="tracking-start">
              {t('tracking.startDate')}
            </Label>
            <Input
              id="tracking-start"
              type="date"
              value={trackingStartAt}
              max={todayDateInputValue()}
              onChange={(event) =>
                setTrackingStartAt(event.target.value)
              }
              disabled={configureTracking.isPending}
            />
            <p className="text-xs text-muted-foreground">
              {t('tracking.setupNotice')}
            </p>
          </div>

          <div className="flex items-start gap-2 rounded-md border border-warning/40 bg-warning/10 p-3 text-xs text-muted-foreground">
            <AlertTriangle
              className="mt-0.5 size-4 shrink-0 text-warning"
              aria-hidden="true"
            />
            {t('tracking.immutableWarning')}
          </div>

          {!isValid && (
            <p className="text-sm text-destructive">
              {t('tracking.invalidDate')}
            </p>
          )}
        </div>

        <DialogFooter>
          <Button
            type="button"
            variant="outline"
            onClick={() => onOpenChange(false)}
            disabled={configureTracking.isPending}
          >
            {t('common:actions.cancel')}
          </Button>
          <Button
            type="button"
            onClick={() => void handleSubmit()}
            disabled={!isValid || configureTracking.isPending}
          >
            {configureTracking.isPending && (
              <Loader2
                className="size-4 animate-spin"
                aria-hidden="true"
              />
            )}
            {t('tracking.configure')}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
