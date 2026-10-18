import { useTranslation } from 'react-i18next';

interface EmptyStateProps {
  isSearching?: boolean;
}

export const EmptyState = ({
  isSearching = false,
}: EmptyStateProps) => {
  const { t } = useTranslation('savingsGoals');

  return (
    <div className="flex min-h-32 w-full items-center justify-center rounded-lg border border-border bg-surface">
      <p className="text-sm text-muted-foreground">
        {isSearching ? t('noResults') : t('emptyState')}
      </p>
    </div>
  );
};
