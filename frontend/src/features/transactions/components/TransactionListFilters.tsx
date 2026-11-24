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
    <div className="flex min-w-0 flex-wrap items-center gap-2">
      <div className="flex max-w-full flex-wrap items-center gap-2">
        <div className="flex flex-wrap items-center gap-1">
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
                  'h-8 rounded-md px-2 text-xs transition-colors',
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

      <div className="relative min-w-36 flex-1">
        <Search
          className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground"
          aria-hidden="true"
        />

        <Input
          type="search"
          placeholder={t('search')}
          value={searchTerm}
          onChange={(event) => onSearchChange(event.target.value)}
          aria-label={t('search')}
          className="h-8 border-border bg-transparent pl-9 text-sm"
        />
      </div>
    </div>
  );
}
