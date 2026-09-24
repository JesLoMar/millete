import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import {
  ArrowDownLeft,
  ArrowUpRight,
  HelpCircle,
  MoreHorizontal,
} from 'lucide-react';
import { m } from 'framer-motion';

import { Badge } from '@/shared/components/core/badge';
import { Button } from '@/shared/components/core/button';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/shared/components/core/dropdown-menu';
import { cn } from '@/lib/utils';
import { formatCurrency } from '@/shared/utils/i18nFormat';

import { formatDate } from '../utils';
import type { Transaction } from './types';

interface TransactionListMobileProps {
  transactions: Transaction[];
  onEdit: (tx: Transaction) => void;
  onDelete: (tx: Transaction) => void;
}

export const TransactionListMobile = memo(function TransactionListMobile({
  transactions,
  onEdit,
  onDelete,
}: TransactionListMobileProps) {
  const { t } = useTranslation('transactions');

  return (
    <div className="divide-y divide-border sm:hidden">
      {transactions.map((tx) => {
        const categoryName = tx.category || 'Sin categoría';
        const isIncome = tx.type === 'INCOME';
        const isOrphan =
          !tx.category || tx.category === 'Sin categoría';

        return (
          <m.div
            key={tx.id}
            className="p-4 transition-colors hover:bg-accent/30"
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.3 }}
          >
            <div className="mb-2 flex items-center justify-between">
              <div className="flex items-center gap-3">
                <div
                  className={cn(
                    'shrink-0 rounded-full p-2',
                    isIncome
                      ? 'bg-primary/10 text-primary'
                      : 'bg-destructive/10 text-destructive',
                  )}
                  aria-hidden="true"
                >
                  {isIncome ? (
                    <ArrowUpRight size={15} />
                  ) : (
                    <ArrowDownLeft size={15} />
                  )}
                </div>

                <p
                  className={cn(
                    'text-base font-bold tabular-nums',
                    isIncome
                      ? 'text-primary'
                      : 'text-foreground',
                  )}
                >
                  {isIncome ? '+' : '-'}
                  {formatCurrency(Math.abs(tx.amount))}
                </p>
              </div>

              <DropdownMenu>
                <DropdownMenuTrigger asChild>
                  <Button
                    type="button"
                    variant="ghost"
                    size="icon"
                    className="size-8"
                    aria-label={t('moreOptions')}
                  >
                    <MoreHorizontal
                      size={16}
                      aria-hidden="true"
                    />
                  </Button>
                </DropdownMenuTrigger>

                <DropdownMenuContent
                  align="end"
                  className="border-border bg-card"
                >
                  <DropdownMenuItem
                    className="cursor-pointer"
                    onClick={() => onEdit(tx)}
                  >
                    {t('edit')}
                  </DropdownMenuItem>

                  <DropdownMenuItem
                    className="cursor-pointer text-destructive"
                    onClick={() => onDelete(tx)}
                  >
                    {t('delete')}
                  </DropdownMenuItem>
                </DropdownMenuContent>
              </DropdownMenu>
            </div>

            <p className="mb-1.5 truncate text-sm font-medium">
              {tx.description}
            </p>

            <div className="flex flex-wrap items-center gap-2">
              <span className="text-xs text-muted-foreground">
                {formatDate(tx.date)}
              </span>

              <span
                className="hidden size-1 rounded-full bg-border xs:inline-block"
                aria-hidden="true"
              />

              {isOrphan ? (
                <span className="inline-flex items-center gap-1 text-xs text-muted-foreground">
                  <HelpCircle
                    size={12}
                    aria-hidden="true"
                  />
                  <span>{categoryName}</span>
                </span>
              ) : (
                <Badge
                  variant="outline"
                  className="border-none text-xs font-medium"
                  style={
                    tx.categoryColor
                      ? {
                          color: tx.categoryColor,
                          backgroundColor: `${tx.categoryColor}20`,
                        }
                      : undefined
                  }
                >
                  {categoryName}
                </Badge>
              )}
            </div>
          </m.div>
        );
      })}
    </div>
  );
});