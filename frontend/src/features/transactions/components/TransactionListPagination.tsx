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
    <div className="flex flex-wrap items-center justify-between gap-2 border-t border-border px-1 py-2">
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
          aria-label={t('previousPage')}
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
          aria-label={t('nextPage')}
          className="h-8 border-border"
        >
          <ChevronRight size={16} aria-hidden="true" />
        </Button>
      </div>
    </div>
  );
}
