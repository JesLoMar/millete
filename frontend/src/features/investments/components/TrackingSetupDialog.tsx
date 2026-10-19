import { memo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Loader2 } from 'lucide-react';
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
import { useInvestmentMutations } from '../hooks/useInvestmentMutations';

interface TrackingSetupDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

export const TrackingSetupDialog = memo(function TrackingSetupDialog({
  open,
  onOpenChange,
}: TrackingSetupDialogProps) {
  const { t } = useTranslation('investments');
  const { configureTracking, isConfiguringTracking } =
    useInvestmentMutations();

  const [trackingStartAt, setTrackingStartAt] = useState(() =>
    new Date().toISOString().slice(0, 10),
  );

  const handleSubmit = () => {
    configureTracking.mutate(
      {
        trackingStartAt: new Date(trackingStartAt).toISOString(),
      },
      {
        onSuccess: () => onOpenChange(false),
      },
    );
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-md">
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
            onChange={(event) => setTrackingStartAt(event.target.value)}
            disabled={isConfiguringTracking}
          />
        </div>

        <DialogFooter>
          <Button
            type="button"
            variant="ghost"
            onClick={() => onOpenChange(false)}
            disabled={isConfiguringTracking}
          >
            {t('common:actions.cancel')}
          </Button>
          <Button
            type="button"
            onClick={handleSubmit}
            disabled={!trackingStartAt || isConfiguringTracking}
            className="min-h-10 font-semibold"
          >
            {isConfiguringTracking ? (
              <Loader2 className="size-5 animate-spin" aria-hidden="true" />
            ) : (
              t('common:actions.save')
            )}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
});