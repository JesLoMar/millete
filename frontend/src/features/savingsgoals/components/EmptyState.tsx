import { useTranslation } from 'react-i18next';

export const EmptyState = () => {
  const { t } = useTranslation('savingsGoals');

  return (
    <div className="flex min-h-32 w-full items-center justify-center rounded-lg border border-border bg-surface">
      <p className="text-sm text-muted-foreground">
        {t('emptyState')}
      </p>
    </div>
  );
};