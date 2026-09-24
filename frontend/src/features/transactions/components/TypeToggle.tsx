import { useTranslation } from 'react-i18next';

import type { TransactionType } from '@/features/transactions/index';
import { cn } from '@/lib/utils';

interface TypeToggleProps {
  value: TransactionType;
  onChange: (type: TransactionType) => void;
}

export function TypeToggle({ value, onChange }: TypeToggleProps) {
  const { t } = useTranslation('transactions');

  return (
    <div className="flex overflow-hidden rounded-lg border border-border">
      <button
        type="button"
        onClick={() => onChange('EXPENSE')}
        aria-pressed={value === 'EXPENSE'}
        className={cn(
          'flex-1 py-2 text-sm font-medium transition-colors',
          value === 'EXPENSE'
            ? 'bg-destructive/20 text-destructive'
            : 'bg-background text-muted-foreground hover:text-foreground',
        )}
      >
        {t('expense')}
      </button>

      <button
        type="button"
        onClick={() => onChange('INCOME')}
        aria-pressed={value === 'INCOME'}
        className={cn(
          'flex-1 py-2 text-sm font-medium transition-colors',
          value === 'INCOME'
            ? 'bg-primary/20 text-primary'
            : 'bg-background text-muted-foreground hover:text-foreground',
        )}
      >
        {t('income')}
      </button>
    </div>
  );
}