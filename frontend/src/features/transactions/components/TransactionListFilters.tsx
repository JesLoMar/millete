import { useTranslation } from 'react-i18next';
import { Search } from 'lucide-react';

import { Badge } from '@/shared/components/core/badge';
import { Button } from '@/shared/components/core/button';
import { Input } from '@/shared/components/core/input';
import { cn } from '@/lib/utils';

import { FILTERS, FILTER_LABELS, type Filter } from '../constants';

interface TransactionListFiltersProps {
  filter: Filter;
  searchTerm: string;
  totalCount: number;
  onFilterChange: (filter: Filter) => void;
  onSearchChange: (searchTerm: string) => void;
}

export function TransactionListFilters({
  filter,
  searchTerm,
  totalCount,
  onFilterChange,
  onSearchChange,
}: TransactionListFiltersProps) {
  const { t } = useTranslation('transactions');

  return (
    <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center sm:gap-4">
      <div className="flex items-center gap-2 sm:gap-3">
        <div className="flex items-center gap-1 rounded-lg border border-border bg-card p-1 sm:gap-2">
          {FILTERS.map((currentFilter) => {
            const isActive = filter === currentFilter;

            return (
              <Button
                key={currentFilter}
                type="button"
                variant={isActive ? 'secondary' : 'ghost'}
                size="sm"
                onClick={() => onFilterChange(currentFilter)}
                aria-pressed={isActive}
                className={cn(
                  'h-7 rounded-md px-2 text-xs transition-all sm:h-8 sm:px-3 sm:text-sm',
                  isActive
                    ? 'bg-primary/20 text-primary'
                    : 'text-muted-foreground',
                )}
              >
                {t(FILTER_LABELS[currentFilter])}
              </Button>
            );
          })}
        </div>

        <Badge variant="outline" className="shrink-0 text-xs">
          {totalCount}
        </Badge>
      </div>

      <div className="relative w-full sm:w-[320px]">
        <Search
          className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground"
          aria-hidden="true"
        />

        <Input
          type="search"
          placeholder={t('search')}
          value={searchTerm}
          onChange={(event) => onSearchChange(event.target.value)}
          className="h-9 border-border bg-card pl-10 text-sm sm:h-10"
        />
      </div>
    </div>
  );
}