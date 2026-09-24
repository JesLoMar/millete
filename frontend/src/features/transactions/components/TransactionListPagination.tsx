import { ChevronLeft, ChevronRight } from 'lucide-react';
import { useTranslation } from 'react-i18next';

import { Button } from '@/shared/components/core/button';

interface TransactionListPaginationProps {
  currentPage: number;
  totalPages: number;
  from: number;
  to: number;
  total: number;
  onPrev: () => void;
  onNext: () => void;
}

export function TransactionListPagination({
  currentPage,
  totalPages,
  from,
  to,
  total,
  onPrev,
  onNext,
}: TransactionListPaginationProps) {
  const { t } = useTranslation('transactions');

  if (totalPages <= 1) {
    return null;
  }

  return (
    <div className="flex flex-col items-center justify-between gap-3 border-t border-border bg-background/20 px-4 py-3 sm:flex-row sm:px-6 sm:py-4">
      <p className="text-center text-xs font-medium text-muted-foreground sm:text-left">
        {t('showingInterval', { from, to, total })}
      </p>

      <div className="flex items-center gap-2">
        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={onPrev}
          disabled={currentPage === 0}
          aria-label={t('previousPage', 'Página anterior')}
          className="h-8 border-border"
        >
          <ChevronLeft size={16} aria-hidden="true" />
        </Button>

        <span className="min-w-12 text-center text-sm tabular-nums text-muted-foreground">
          {currentPage + 1} / {totalPages}
        </span>

        <Button
          type="button"
          variant="outline"
          size="sm"
          onClick={onNext}
          disabled={currentPage >= totalPages - 1}
          aria-label={t('nextPage', 'Página siguiente')}
          className="h-8 border-border"
        >
          <ChevronRight size={16} aria-hidden="true" />
        </Button>
      </div>
    </div>
  );
}