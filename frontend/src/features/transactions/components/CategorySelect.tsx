import { Loader2 } from 'lucide-react';
import { useId } from 'react';
import { useTranslation } from 'react-i18next';

import { useCategories } from '@/features/categories/hooks/useCategories';
import { cn } from '@/lib/utils';
import { Label } from '@/shared/components/core/label';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/core/select';

interface CategorySelectProps {
  value: string;
  onValueChange: (value: string) => void;
  className?: string;
}

export function CategorySelect({
  value,
  onValueChange,
  className,
}: CategorySelectProps) {
  const { t } = useTranslation([
    'transactions',
    'common',
    'categories',
  ]);

  const { displayItems: categories, isLoading } = useCategories();

  const selectId = useId();

  return (
    <div className={cn('space-y-2', className)}>
      <Label htmlFor={selectId} className="text-sm font-semibold">
        {t('transactions:category')}
      </Label>

      <Select
        value={value}
        onValueChange={onValueChange}
        disabled={isLoading}
      >
        <SelectTrigger
          id={selectId}
          className="w-full border-border bg-background"
          aria-busy={isLoading}
        >
          {isLoading ? (
            <div className="flex items-center gap-2">
              <Loader2
                size={14}
                className="animate-spin"
                aria-hidden="true"
              />

              <span className="text-muted-foreground">
                {t('common:status.loading')}
              </span>
            </div>
          ) : (
            <SelectValue
              placeholder={t('transactions:selectCategory')}
            />
          )}
        </SelectTrigger>

        <SelectContent className="border-border bg-card">
          {categories.length > 0 ? (
            categories.map((category) => (
              <SelectItem key={category.id} value={category.id}>
                {category.name}
              </SelectItem>
            ))
          ) : (
            <SelectItem value="__no_categories__" disabled>
              {t('categories:empty')}
            </SelectItem>
          )}
        </SelectContent>
      </Select>
    </div>
  );
}