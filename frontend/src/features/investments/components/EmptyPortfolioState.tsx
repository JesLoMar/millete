import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import { LineChart } from 'lucide-react';
import { Button } from '@/shared/components/core/button';

interface EmptyPortfolioStateProps {
  onRecordActivity: () => void;
}

export const EmptyPortfolioState = memo(function EmptyPortfolioState({
  onRecordActivity,
}: EmptyPortfolioStateProps) {
  const { t } = useTranslation('investments');

  return (
    <div className="flex flex-col items-center justify-center rounded-lg border border-dashed border-border py-16 text-center">
      <div className="mb-4 rounded-full bg-primary/10 p-4">
        <LineChart className="size-8 text-primary" aria-hidden="true" />
      </div>
      <h3 className="mb-1 text-lg font-semibold text-foreground">
        {t('empty.title')}
      </h3>
      <p className="mb-4 max-w-md text-sm text-muted-foreground">
        {t('empty.description')}
      </p>
      <Button type="button" onClick={onRecordActivity}>
        {t('empty.cta')}
      </Button>
    </div>
  );
});